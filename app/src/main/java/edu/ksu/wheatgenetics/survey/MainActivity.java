package edu.ksu.wheatgenetics.survey;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

/**
 * Created by chaneylc on 8/30/2017.
 */

public class MainActivity extends AppCompatActivity {

    private LocEntryDbHelper mDbHelper;

    private String mLastLatitude, mLastLongitude;

    private TextView mLocTextView;
    private ListView mIdListView;
    private EditText mIdInputEditText;
    private Button mSubmitLocButton;
    private DrawerLayout mDrawerLayout;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onPostCreate(Bundle savedInstanceBundle) {
        super.onPostCreate(savedInstanceBundle);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 100);

        mLastLatitude = mLastLongitude = null;

        mDbHelper = new LocEntryDbHelper(this);

        mIdListView = (ListView) findViewById(R.id.idListView);
        mLocTextView = (TextView) findViewById(R.id.locationTextView);
        mIdInputEditText = (EditText) findViewById(R.id.idInputEditText);
        mSubmitLocButton = (Button) findViewById(R.id.submitInputButton);
        mSubmitLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIdInputEditText.getText().toString().isEmpty()
                        && mLastLatitude != null
                        && mLastLongitude != null) {
                    submitToDb();
                }
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().getThemedContext();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        // Setup drawer view
        setupDrawerContent(nvDrawer);
        setupDrawer();

        final Intent geoNavServiceIntent = new Intent(this, GeoNavService.class);
        startService(geoNavServiceIntent);

        /*final Intent sharedFilePicker = new Intent(Intent.ACTION_PICK);
        sharedFilePicker.setType("text/*");
        startActivityForResult(sharedFilePicker, 0);*/

        /*
        //first method
        //create bundle of flattened lat/lng values
        // s.a LatLng1= -103.4, -96.5; LatLng2= -105.4, -96.3 is defined as
        //new double[] {-103.4, -96.5, -105.4, -96.3}
        final Intent fieldMappingIntent = new Intent(this, GeoNavService.class);
        //sending a flattened array of coordinates
        fieldMappingIntent.putExtra("array", new double[] {1.0, 2.0, 3.0, 4.0});
        //updating accuracy in meters
        fieldMappingIntent.putExtra("Accuracy", Float.MAX_VALUE);
        //updating time in ms
        fieldMappingIntent.putExtra("Time", Long.MIN_VALUE);
        //updating distance in meters
        fieldMappingIntent.putExtra("Distance", Float.MIN_VALUE);
        //second method
        //create map plot id, and value is lat/lng coordinates
        //following map contains the 8 cardinal boundary points for East Stadium Manhattan, KS
        //Q1 - Q4 are approximate cartesian quadrant midpoints
        final HashMap<String, double[]> idMap = new HashMap<>();
        idMap.put("N", new double[] {39.187959, -96.584348});
        idMap.put("NW", new double[] {39.187988, -96.584680});
        idMap.put("W", new double[] {39.187500, -96.584705});
        idMap.put("SW", new double[] {39.187006, -96.584697});
        idMap.put("S", new double[] {39.187006, -96.584297});
        idMap.put("SE", new double[] {39.187024, -96.583954});
        idMap.put("E", new double[] {39.187492, -96.583929});
        idMap.put("NE", new double[] {39.187968, -96.583946});
        idMap.put("Q1", new double[] {39.187791, -96.584093});
        idMap.put("Q2", new double[] {39.187706, -96.584554});
        idMap.put("Q3", new double[] {39.187268, -96.584509});
        idMap.put("Q4", new double[] {39.187268, -96.584093});
        fieldMappingIntent.putExtra("map", idMap);
        startService(fieldMappingIntent); */
        final IntentFilter filter = new IntentFilter();
        filter.addAction(GeoNavConstants.BROADCAST_LOCATION);
        filter.addAction(GeoNavConstants.BROADCAST_ACCURACY);
        filter.addAction(GeoNavConstants.BROADCAST_PLOT_ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new ResponseReceiver(),
                filter
        );

        updateListView();
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            public void onDrawerClosed(View view) {
            }

        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.maps_activity:
                final Intent mapsActivity = new Intent(this, MapsActivity.class);
                startActivity(mapsActivity);
                break;
            case R.id.nav_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, SurveyConstants.SETTINGS_INTENT_REQ);
                break;
        }

        mDrawerLayout.closeDrawers();
    }

    private synchronized void submitToDb() {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String userName = prefs.getString(SettingsActivity.PERSON, "Default");
        final String experimentId = prefs.getString(SettingsActivity.EXPERIMENT, "Default");

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final ContentValues entry = new ContentValues();
        entry.put(LocEntryContract.LocEntry.COLUMN_NAME_SAMPLE_ID, mIdInputEditText.getText().toString());
        entry.put(LocEntryContract.LocEntry.COLUMN_NAME_LATITUDE, mLastLatitude);
        entry.put(LocEntryContract.LocEntry.COLUMN_NAME_LONGITUDE, mLastLongitude);
        entry.put(LocEntryContract.LocEntry.COLUMN_NAME_PERSON, userName);
        entry.put(LocEntryContract.LocEntry.COLUMN_NAME_EXPERIMENT_ID, experimentId);

        final long newRowId = db.insert(LocEntryContract.LocEntry.TABLE_NAME, null, entry);

        updateListView();
    }

    private synchronized void updateListView() {

        final ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(this, R.layout.row);

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final Cursor cursor = db.rawQuery("SELECT sample_id FROM " + LocEntryContract.LocEntry.TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                final String id = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.COLUMN_NAME_SAMPLE_ID)
                );
                newAdapter.add(id);
            } while(cursor.moveToNext());
        }
        cursor.close();

        mIdListView.setAdapter(newAdapter);
    }

    private class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(GeoNavConstants.LOCATION)) {
                final Location l = ((Location) intent.getExtras()
                        .get(GeoNavConstants.LOCATION));
                if (l != null) {
                    mLastLatitude = String.valueOf(l.getLatitude());
                    mLastLongitude = String.valueOf(l.getLongitude());
                    mLocTextView.setText("Latitude: " + mLastLatitude + "\n"
                                        +"Longitude: " + mLastLongitude);
                }
            }

            /*if (intent.hasExtra(GeoNavConstants.PLOT_ID)) {
                ((TextView) findViewById(R.id.idText)).setText(
                        (String) intent.getExtras()
                                .get(GeoNavConstants.PLOT_ID)
                );
            }

            if (intent.hasExtra(GeoNavConstants.ACCURACY)) {
                ((TextView) findViewById(R.id.accuracyText)).setText(
                        String.valueOf(intent.getExtras()
                                .get(GeoNavConstants.ACCURACY))
                );
            }*/
        }
    }
}
