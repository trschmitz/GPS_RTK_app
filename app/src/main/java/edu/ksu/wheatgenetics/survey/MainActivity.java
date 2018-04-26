package edu.ksu.wheatgenetics.survey;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;

import static edu.ksu.wheatgenetics.survey.SurveyConstants.BROADCAST_BT_CONNECTION;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.BROADCAST_BT_OUTPUT;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.BT_CONNECTION;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.BT_OUTPUT;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.MESSAGE_READ;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.PERMISSION_REQUEST;

/**
 * Created by chaneylc on 8/30/2017.
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //db helper variable and prepared statement declarations
    private LocEntryDbHelper mDbHelper;
    private PreparedStatement sqlSelectSampleId;

    //db columns
    private String mLastLatitude, mLastLongitude;
    private String mLastTimestamp;

    //location to save Survey data s.a exporting lat/lng .csv
    private File mSurveyDirectory;

    //survey UI variables
    private TextView mLocTextView;
    private ListView mPointListView;
    //private EditText mIdInputEditText;
    private Button mSubmitInputButton;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    //newPlot and newPoints variables for while in-process of creating
    private ArrayList<Point> newPoints;
    private Plot newPlot;
    //global var that holds all plots that are in db,
    //including new ones that were added
    private ArrayList<Plot> allPlots = new ArrayList<>();
    private String newPlotNameText;
    private Marker mCurLocationMarker;

    //bluetooth device variables
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ConnectedThread mConnectedThread;

    private GoogleMap mMap;
    private SparseArray<LatLng> mLocationArray;
    private SparseArray<String> mIdArray;
    private LatLng mStartLocation;

    //nmea parser
    private NmeaParser mNmeaParser;

    //state of the action bar, 0 is default with add plot button
    //1 is finish add plot button
    private int mActionBarState = 0;

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
    public boolean onCreateOptionsMenu(Menu m) {

        if (mActionBarState == 0) {
            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.activity_main_toolbar, m);
        } else {
            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.activity_plot_toolbar, m);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //ArrayList<Point> newPoints = new ArrayList<Point>();
        //need a cancel button and a corresponding case
        switch (item.getItemId()) {

            /* TODO
            create logic/variables to store locations while in this state
            I recommend creating an ArrayList and a class 'Point', the add button will add
            the Point to the list. You should probably add some UI interactions with the ListView
            to edit its name.
            also make the list view and add button visible
            */
            case R.id.startPlotButton: {

                mActionBarState = 1;
                invalidateOptionsMenu();
                //ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams)mPointListView.getLayoutParams();
               // lp.height=300;
                //mPointListView.setLayoutParams(lp);
                //mPointListView.setVisibility(View.VISIBLE);
                //show add point button too - where to put...?
                newPoints = new ArrayList<>(); //clear it cause starting new plot
                //mIdInputEditText.setVisibility(View.VISIBLE);
                mSubmitInputButton.setVisibility(View.VISIBLE);
                mPointListView.setAdapter(null);

                return true;
            }

            /*TODO
            You will need to create a separate array list here for all of your plot locations.
            Create a 'Plot' class which will have multiple 'Point' classes. Each time this finish button
            is pressed one new Plot class will be created which is populated by the array list from above.
            also make the list view and add button invisible
             */
            case R.id.finishPlotButton: {

                mActionBarState = 0;
                invalidateOptionsMenu();
                //ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams)mPointListView.getLayoutParams();
                //lp.height=0;
                //mPointListView.setLayoutParams(lp);
                //mIdInputEditText.setVisibility(View.GONE);
                mSubmitInputButton.setVisibility(View.GONE);

                if (newPoints.size() < 3) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Cannot create plot - must have at least 3 points");
                    builder.show();
                    //don't actually add the plot
                    //mPointListView.setAdapter(null);
                    //mPointListView.setVisibility(View.GONE);
                    allPlotsToListView();
                    return true;
                }
                newPlot = new Plot("newPlot", "trs", mLastTimestamp);
                for (Point p: newPoints) {
                    newPlot.addPoint(p);
                }

                newPlotNameText = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter Plot Name");
                //set up the input
                final EditText input = new EditText(this);
                //Specify type of input expected: text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                //set up buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().isEmpty()) { //a name has been entered
                            newPlotNameText = input.getText().toString();
                            newPlot.setName(newPlotNameText);
                            ArrayAdapter<Plot> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.row);
                            adapter.add(newPlot);
                            mPointListView.setAdapter(adapter);
                            submitToDb(newPlot);
                            //either way, our action here is still to display all successfully saved Plots
                        } else { //empty text, no name was entered
                            Toast.makeText(MainActivity.this, "Plot not created, no name was entered", Toast.LENGTH_SHORT).show();
                        }
                        //if submitToDb threw an error, it will have notified the user from there so we still display all SAVED plots
                        //also, if they didn't enter a name, we still want to display all saved plots
                        allPlotsToListView();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel(); //don't save the plot -then we don't need a cancel button :) (at least not for now)
                        //also clear everything from listview and hide it and the addpoint elems
                        //replace listview with successfully saved plots
                        allPlotsToListView();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                        allPlotsToListView();
                    }
                });
                builder.show();

                return true;
            }
        }

        return false;
    }

    private void allPlotsMarkMap() {
        for (Plot p: allPlots ) {
            p.markMap(mMap);
        }
    }

    private void allPlotsToListView() {
        ArrayAdapter<Plot> adapter = new ArrayAdapter<>(this, R.layout.row);
        for (int i = allPlots.size()-1; i>=0; i--) {
            adapter.add(allPlots.get(i));
        }
        mPointListView.setAdapter(adapter);
    }

    private void addPtToListView() {
        newPoints.add(new Point("rtk", mLastLatitude, mLastLongitude, "addPt2LVacc"));
        ArrayAdapter<Point> adapter = new ArrayAdapter<>(this, R.layout.row);
        for (Point p:newPoints) {
            adapter.add(p);
        }
        mPointListView.setAdapter(adapter);
    }

    private void findPairedBTDevice() {

        if (mBluetoothAdapter != null) {

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (!pairedDevices.isEmpty()) {
                final Map<String, BluetoothDevice> bluetoothMap = new HashMap<>();
                final ArrayAdapter<String> bluetoothDevicesAdapter =
                        new ArrayAdapter<String>(MainActivity.this, R.layout.row);
                for (BluetoothDevice bd : pairedDevices) {
                    bluetoothMap.put(bd.getName(), bd);
                    bluetoothDevicesAdapter.add(bd.getName());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose your paired bluetooth device.");
                final ListView devices = new ListView(MainActivity.this);
                devices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                devices.setSelector(R.drawable.list_selector_focus);
                devices.setAdapter(bluetoothDevicesAdapter);
                builder.setView(devices);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (devices.getCheckedItemCount() > 0) {
                            String value = bluetoothDevicesAdapter.getItem(devices.getCheckedItemPosition());
                            if (value != null) {
                                mBluetoothDevice = bluetoothMap.get(value);
                                new ConnectThread(mBluetoothDevice).start();
                            }
                        }
                    }
                });

                builder.show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, SurveyConstants.permissions, PERMISSION_REQUEST);

        mNmeaParser = new NmeaParser();

        mLastLatitude = mLastLongitude = mLastTimestamp = null;

        mDbHelper = new LocEntryDbHelper(this);

        initializeUI();

        final Intent geoNavServiceIntent = new Intent(this, GeoNavService.class);
        startService(geoNavServiceIntent);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_BT_OUTPUT);
        filter.addAction(GeoNavConstants.BROADCAST_LOCATION);
        filter.addAction(GeoNavConstants.BROADCAST_ACCURACY);
        filter.addAction(GeoNavConstants.BROADCAST_PLOT_ID);
        mLocalBroadcastManager.registerReceiver(
                new ResponseReceiver(),
                filter
        );

        if (isExternalStorageWritable())
            mSurveyDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Survey");
        if (!mSurveyDirectory.isDirectory()) {
            mSurveyDirectory.mkdirs();
        }

        if (mLocationArray == null) {
            mLocationArray = new SparseArray<>();
        }

        if (mIdArray == null)
            mIdArray = new SparseArray<>();

        loadDatabase();
        //allPlotsToListView();

    }

    private synchronized void loadDatabase() {

        mDbHelper = new LocEntryDbHelper(this);

        //TODO something similar to this but retrieve all stored Plot objects

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final Cursor cursor = db.rawQuery("SELECT " +
                LocEntryContract.LocEntry.PLOTS_COL_PLOT_ID + "," +
                LocEntryContract.LocEntry.PLOTS_COL_NAME + ", " +
                LocEntryContract.LocEntry.PLOTS_COL_USER + ", " +
                LocEntryContract.LocEntry.PLOTS_COL_TIMESTAMP + " FROM " +
                LocEntryContract.LocEntry.TABLE_NAME_PLOTS, null);
        ArrayList<Plot> dbPlots = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                final String plot_id = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.PLOTS_COL_PLOT_ID)
                );
                final String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.PLOTS_COL_NAME)
                );
                final String user = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.PLOTS_COL_USER)
                );
                final String timestamp = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.PLOTS_COL_TIMESTAMP)
                );
                Plot nextPlot = new Plot(name,user,timestamp);
                nextPlot.setID(Long.parseLong(plot_id));
                dbPlots.add(nextPlot);
                //android.util.Log.v("CursorPlot", nextPlot.toString());

            } while (cursor.moveToNext());
        }
        cursor.close();

        for (int i = 0; i<dbPlots.size(); i++ ) {
            Plot curPlot = dbPlots.get(i);

            final Cursor pointCursor = db.rawQuery("SELECT " +
                    LocEntryContract.LocEntry.POINTS_COL_POINT_ID + "," +
                    LocEntryContract.LocEntry.POINTS_COL_RTK + "," +
                    LocEntryContract.LocEntry.POINTS_COL_LAT + "," +
                    LocEntryContract.LocEntry.POINTS_COL_LNG + "," +
                    LocEntryContract.LocEntry.POINTS_COL_ACCURACY + " from " +
                            LocEntryContract.LocEntry.TABLE_NAME_POINTS + " where " +
                            LocEntryContract.LocEntry.POINTS_COL_POINT_ID + " in " +
                    "(select " + LocEntryContract.LocEntry.PLOT_POINT_COL_POINT_ID +
                    " from " + LocEntryContract.LocEntry.TABLE_NAME_PLOT_POINT +
                    " where " + LocEntryContract.LocEntry.PLOT_POINT_COL_PLOT_ID +
                    " = " + curPlot.getID() + ");" , null );

                    /*
                    "(" + LocEntryContract.LocEntry.TABLE_NAME_POINTS + " left outer join " +
                    LocEntryContract.LocEntry.TABLE_NAME_PLOT_POINT + " on " +
                    LocEntryContract.LocEntry.TABLE_NAME_PLOT_POINT + "." +
                    LocEntryContract.LocEntry.PLOT_POINT_COL_POINT_ID + "=" +
                    LocEntryContract.LocEntry.TABLE_NAME_POINTS + "." +
                    LocEntryContract.LocEntry.POINTS_COL_POINT_ID + ") " +
                    "where " + LocEntryContract.LocEntry.PLOT_POINT_COL_PLOT_ID +
                    "=" + Long.parseLong(plot_id), null);
                    */
            if (pointCursor.moveToFirst()) {
                do {
                    final String point_id = pointCursor.getString(
                            pointCursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.POINTS_COL_POINT_ID)
                    );
                    final String rtk = pointCursor.getString(
                            pointCursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.POINTS_COL_RTK)
                    );
                    final String latitude = pointCursor.getString(
                            pointCursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.POINTS_COL_LAT)
                    );
                    final String longitude = pointCursor.getString(
                            pointCursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.POINTS_COL_LNG)
                    );
                    final String accuracy = pointCursor.getString(
                            pointCursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.POINTS_COL_ACCURACY)
                    );
                    Point nextPoint = new Point(rtk,latitude,longitude,accuracy);
                    nextPoint.setID(Long.parseLong(point_id));
                    //android.util.Log.v("CursorPt", nextPoint.toString());
                    curPlot.addPoint(nextPoint);

                } while (pointCursor.moveToNext());
            }
            pointCursor.close();

        }
        //add dbPlots to allPlots
        allPlots.addAll(dbPlots);
        allPlotsToListView();

        /*
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("SELECT latitude, longitude, sample_id FROM " + LocEntryContract.LocEntry.TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                final String id = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.COLUMN_NAME_SAMPLE_ID)
                );
                final String lat = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.COLUMN_NAME_LATITUDE)
                );
                final String lng = cursor.getString(
                        cursor.getColumnIndexOrThrow(LocEntryContract.LocEntry.COLUMN_NAME_LONGITUDE)
                );
                final LatLng latlng = new LatLng(Double.valueOf(lat), Double.valueOf(lng));
                mStartLocation = latlng;

                mLocationArray.append(mLocationArray.size(), latlng);
                mIdArray.append(mIdArray.size(), id);

            } while(cursor.moveToNext());
        }
        */

        //after db is finished loading populate the map
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        android.util.Log.v("onMapReady", "is being called");
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mDbHelper = new LocEntryDbHelper(this);

        drawUI();

        setupView();
    }

    /* TODO
    Display the id for each Plot you've saved in the centroid of each plot.
    Display the user location.
    If in Survey mode, maybe display a line to the closest plot.
    If in Plot mode display each plotted point.
     */
    private void drawUI() {

        if (mMap != null) {
            mMap.clear();
            allPlotsMarkMap();
        }
    }

    //function to modify camera for initial view
    private void setupView() {
        //add test begin point at East Stadium, KSU
        if (mMap != null) {
            mMap.moveCamera(
                    CameraUpdateFactory.newLatLng(new LatLng(39.190439,-96.584222))
            );
            mCurLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.190439, -96.584222))
                .zIndex(10.0f)
            );
        }
    }

    private void drawMarker(LatLng latLng) {

        final double lat = latLng.latitude;
        final double lng = latLng.longitude;
        if(mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(latLng));
        }
    }

    private void initializeUI() {

        mPointListView = findViewById(R.id.pointListView);
        mLocTextView = findViewById(R.id.locationTextView);
        //mIdInputEditText = findViewById(R.id.idInputEditText);
        mSubmitInputButton = findViewById(R.id.submitInputButton);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mPointListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionBarState == 1) return; //TODO: later, make this so they can remove a point?

                final Plot plot = (Plot) parent.getAdapter().getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(plot.getName());
                String message = "Plot info:" +
                        "\n" + plot.toString();
                for (int i = 0; i<plot.getPoints().size(); i++) {
                    message += "\n\nPoint " + (i+1) + ":\n" + plot.getPoints().get(i).toString();
                }

                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText input = new EditText(MainActivity.this);
                //Specify type of input expected: text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("new Plot name");

                final TextView text = new TextView(MainActivity.this);
                text.setText(message);
                text.setMovementMethod(new ScrollingMovementMethod());

                layout.addView(input);
                layout.addView(text);
                builder.setView(layout);

                builder.setNeutralButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //rename
                        if (!input.getText().toString().isEmpty()) { //a name has been entered
                            String renameText = input.getText().toString();
                            String oldName = plot.getName();
                            plot.setName(renameText);
                            if (!renameInDb(plot, renameText)) {
                                //unsuccessful, revert name and display toast
                                plot.setName(oldName);
                                Toast.makeText(MainActivity.this, "Error: could not rename plot", Toast.LENGTH_SHORT).show();
                            }
                            allPlotsToListView();
                        } else { //empty text, no name was entered
                            Toast.makeText(MainActivity.this, "Plot not renamed, no name was entered", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Delete plot", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete

                        DialogInterface.OnClickListener myDialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //call delete from DB
                                        if (!deleteFromDB(plot)) {
                                            //not successful
                                            Toast.makeText(MainActivity.this, "Deletion not successful", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Successfully deleted",Toast.LENGTH_SHORT).show();
                                        }
                                        allPlotsToListView();
                                        break;
                                    /*case DialogInterface.BUTTON_NEGATIVE:
                                        //do nothing?
                                        Toast.makeText(MainActivity.this, "No clicked", Toast.LENGTH_SHORT).show();
                                        break;*/
                                }
                            }
                        };

                        AlertDialog.Builder areYouSure = new AlertDialog.Builder(MainActivity.this);
                        areYouSure.setMessage("Are you sure you want to delete this plot?");
                        areYouSure.setPositiveButton("Yes", myDialogClickListener);
                        areYouSure.setNegativeButton("No", myDialogClickListener);
                        areYouSure.show();
                    }
                });
                builder.setPositiveButton("Ok", null); //close?
                builder.show();
            }
        });

        mSubmitInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!mIdInputEditText.getText().toString().isEmpty()
                        && mLastLatitude != null
                        && mLastLongitude != null) {
                    //submitToDb();
                    addPtToListView();
                } else {
                    Toast.makeText(MainActivity.this, "Entry must have a name and location.", Toast.LENGTH_SHORT).show();
                }*/ //TODO: uncomment this - left out for debugging purposes
                addPtToListView(); //also adds to newPoints array
            }
        });

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().getThemedContext();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Setup drawer view
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);
        setupDrawer();
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
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

           /* case R.id.nav_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, SurveyConstants.SETTINGS_INTENT_REQ);
                break;
            case R.id.action_navbar_export:
                askUserExportFileName();
                break;*/
            case R.id.action_connect_bluetooth:
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                findPairedBTDevice();
                break;
        }

        mDrawerLayout.closeDrawers();
    }

    private synchronized boolean deleteFromDB(Plot plotToDelete) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            //delete from PLOTS and POINTS, PLOT_POINT should cascade delete
            int plotsDeleted = db.delete(LocEntryContract.LocEntry.TABLE_NAME_PLOTS,
                    LocEntryContract.LocEntry.PLOTS_COL_PLOT_ID + "=" + plotToDelete.getID(), null);
            int plot_ptsDeleted = db.delete(LocEntryContract.LocEntry.TABLE_NAME_PLOT_POINT,
                    LocEntryContract.LocEntry.PLOT_POINT_COL_PLOT_ID + "=" + plotToDelete.getID(), null);
            int pointsDeleted = 0;
            for (Point p: plotToDelete.getPoints()) {
                pointsDeleted += db.delete(LocEntryContract.LocEntry.TABLE_NAME_POINTS,
                        LocEntryContract.LocEntry.POINTS_COL_POINT_ID + "=" + p.getId(), null);
            }
            if (plotsDeleted != 1 || pointsDeleted != plotToDelete.getPoints().size() || plot_ptsDeleted != plotToDelete.getPoints().size()) {
                Toast.makeText(this, "wrong num rows deleted. plots: " + plot_ptsDeleted + ", pts: " + pointsDeleted + ", plot_point: " + plot_ptsDeleted, Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("wrong num rows deleted");
                builder.show();
                return false;
            }
            //otherwise, all was successful
            allPlots.remove(plotToDelete);
            plotToDelete.unMarkMap();
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Toast.makeText(this,"error: " + e.getMessage(),Toast.LENGTH_LONG).show();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private synchronized boolean renameInDb(Plot plotToRename, String newName) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues entry = new ContentValues();
            entry.put(LocEntryContract.LocEntry.PLOTS_COL_NAME, newName);
            int affected = db.update(LocEntryContract.LocEntry.TABLE_NAME_PLOTS, entry,
                    LocEntryContract.LocEntry.PLOTS_COL_PLOT_ID + "=" +
            plotToRename.getID(), null);

            if (affected < 1) {
                return false;
            } else if (affected > 1) {
                Toast.makeText(this, "Update error: " + affected + " rows updated", Toast.LENGTH_LONG).show();
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }
    private synchronized int submitToDb(Plot plotToSubmit) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String userName = prefs.getString(SettingsActivity.PERSON, "Default");
        final String experimentId = prefs.getString(SettingsActivity.EXPERIMENT, "Default");

        if (!experimentId.isEmpty() && !userName.isEmpty() /*&& mLastLatitude != null && mLastLongitude != null*/) {
            final SQLiteDatabase db = mDbHelper.getWritableDatabase();
            //final ContentValues entry = new ContentValues();

            /* you will have values like this, but not exactly, remember to edit the LocEntryContract file with your DB Schema
            entry.put(LocEntryContract.LocEntry.COLUMN_NAME_SAMPLE_ID, mIdInputEditText.getText().toString());
            entry.put(LocEntryContract.LocEntry.COLUMN_NAME_LATITUDE, mLastLatitude);
            entry.put(LocEntryContract.LocEntry.COLUMN_NAME_LONGITUDE, mLastLongitude);
            entry.put(LocEntryContract.LocEntry.COLUMN_NAME_PERSON, userName);
            entry.put(LocEntryContract.LocEntry.COLUMN_NAME_EXPERIMENT_ID, experimentId);
            entry.put(LocEntryContract.LocEntry.COLUMN_NAME_TIMESTAMP, mLastTimestamp);

            final long newRowId = db.insert(LocEntryContract.LocEntry.TABLE_NAME, null, entry);
            */
            db.beginTransaction();
            try {
                // foreach point in plot, add the points
                ArrayList<Long> pointRowIDs = new ArrayList<>();

                for (Point pointToSubmit : plotToSubmit.getPoints()) {
                    final ContentValues pointEntry = new ContentValues();
                    //rtk, lat, long, accuracy - id will be auto-generated so don't include
                    pointEntry.put(LocEntryContract.LocEntry.POINTS_COL_RTK, pointToSubmit.getRtk());
                    pointEntry.put(LocEntryContract.LocEntry.POINTS_COL_LAT, pointToSubmit.getLatitude());
                    pointEntry.put(LocEntryContract.LocEntry.POINTS_COL_LNG, pointToSubmit.getLongitude());
                    pointEntry.put(LocEntryContract.LocEntry.POINTS_COL_ACCURACY, pointToSubmit.getAccuracy());

                    long newPointRowId = db.insertOrThrow(LocEntryContract.LocEntry.TABLE_NAME_POINTS, null, pointEntry);
                    //save ID to update the Points after all DB writes successful
                    pointRowIDs.add(newPointRowId);
                }
                // if all pts added successfully, add plot
                final ContentValues plotEntry = new ContentValues();
                plotEntry.put(LocEntryContract.LocEntry.PLOTS_COL_NAME, plotToSubmit.getName());
                plotEntry.put(LocEntryContract.LocEntry.PLOTS_COL_USER, plotToSubmit.getUser());
                plotEntry.put(LocEntryContract.LocEntry.PLOTS_COL_TIMESTAMP, plotToSubmit.getTimestamp());
                plotEntry.put(LocEntryContract.LocEntry.PLOTS_COL_CENTROID, plotToSubmit.getCentroid());

                long newPlotRowId = db.insertOrThrow(LocEntryContract.LocEntry.TABLE_NAME_PLOTS, null, plotEntry);

                //after plot, add the rows to the PLOT_POINT relation table
                for(int i = 0; i<pointRowIDs.size(); i++) {
                    final ContentValues plotPointEntry = new ContentValues();
                    plotPointEntry.put(LocEntryContract.LocEntry.PLOT_POINT_COL_PLOT_ID, newPlotRowId);
                    plotPointEntry.put(LocEntryContract.LocEntry.PLOT_POINT_COL_POINT_ID, pointRowIDs.get(i));

                    db.insertOrThrow(LocEntryContract.LocEntry.TABLE_NAME_PLOT_POINT, null, plotPointEntry);
                }
                //if here, must've all been successful
                db.setTransactionSuccessful();
                //update all the plot/point IDs
                plotToSubmit.setID(newPlotRowId);
                int i = 0;
                for (Point p: plotToSubmit.getPoints()) {
                    p.setID(pointRowIDs.get(i));
                    i++;
                }
                allPlots.add(plotToSubmit);
                plotToSubmit.calcCentroid();
                plotToSubmit.markMap(mMap);

                return 1;
            } catch (Exception e) {
                //was an error
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Error inserting data in database " + e.getMessage());
                builder.show();
                return -1;
            } finally {
                db.endTransaction();
            }
        }
        return -2; //didn't make it inside the ifs to do the inserts
    }

    private boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState()
        )) return true;
        return false;
    }

    private synchronized void askUserExportFileName() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose name for exported file.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (!value.isEmpty()) {
                    if (isExternalStorageWritable()) {
                        try {
                            final File output = new File(mSurveyDirectory, value + ".csv");
                            final FileOutputStream fstream = new FileOutputStream(output);
                            final SQLiteDatabase db = mDbHelper.getReadableDatabase();
                            //final String table = LocEntryContract.LocEntry.TABLE_NAME;
                            final String table = LocEntryContract.LocEntry.TABLE_NAME_POINTS; //TODO: review this method, previously the above line was used
                            //final Cursor cursor = db.rawQuery("SElECT * FROM SURVEY", null);
                            final Cursor cursor = db.query(table, null, null, null, null, null, null);
                            //first write header line
                            final String[] headers = cursor.getColumnNames();
                            for (int i = 0; i < headers.length; i++) {
                                if (i != 0) fstream.write(",".getBytes());
                                fstream.write(headers[i].getBytes());
                            }
                            fstream.write("\n".getBytes());
                            //populate text file with current database values
                            if (cursor.moveToFirst()) {
                                do {
                                    for (int i = 0; i < headers.length; i++) {
                                        if (i != 0) fstream.write(",".getBytes());
                                        final String val = cursor.getString(
                                                cursor.getColumnIndexOrThrow(headers[i])
                                        );
                                        if (val == null) fstream.write("null".getBytes());
                                        else fstream.write(val.getBytes());
                                    }
                                    fstream.write("\n".getBytes());
                                } while (cursor.moveToNext());
                            }

                            cursor.close();
                            fstream.flush();
                            fstream.close();

                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    } //error toast
                } //use default name
            }
        });

        builder.show();

    }

    //Receives GPS updates from a bluetooth device or the phone GPS
    private class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(GeoNavConstants.LOCATION)) {
                final Location l = ((Location) intent.getExtras()
                        .get(GeoNavConstants.LOCATION));
                if (l != null) {
                    mLastLatitude = String.valueOf(l.getLatitude());
                    mLastLongitude = String.valueOf(l.getLongitude());
                    mLastTimestamp = getTime();
                    mLocTextView.setText("Lat/Lng: " + mLastLatitude + " / " + mLastLongitude);

                    mCurLocationMarker.setPosition(new LatLng(Double.parseDouble(mLastLatitude), Double.parseDouble(mLastLongitude)));
                }
            }

            if (intent.hasExtra(BT_OUTPUT)) {

                final String raw = intent.getStringExtra(BT_OUTPUT);
                mNmeaParser.feed(raw);
                mLastLongitude = mNmeaParser.getLongitude();
                mLastLatitude = mNmeaParser.getLatitude();
                mLastTimestamp = getTime();
                mLocTextView.setText(mNmeaParser.toString());
                // mTextView.setText(raw);
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

    //helper function for returning current time s.a 2017-10-19 08:22:18
    private String getTime() {

        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd hh-mm-ss", Locale.getDefault());
        return sdf.format(c.getTime());
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;

        ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mBluetoothDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            } catch (IOException e) {
                Log.e("CONNECT THREAD", "Socket's create() method failed", e);
            }

            mmSocket = tmp;
        }

        public void run() {

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("CONNECT THREAD: RUN", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            if (mConnectedThread != null && (mConnectedThread.isAlive()
                    || mConnectedThread.isDaemon() || mConnectedThread.isInterrupted()))
                mConnectedThread.cancel();
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("CONNECT THREAD : CANCEL", "Could not close the client socket", e);
            }
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message input) {

            final String raw = (String) input.obj;
            switch (input.what) {
                case SurveyConstants.MESSAGE_READ:

                    //progress is complete, send the final message
                    mLocalBroadcastManager.sendBroadcast(
                            new Intent(BROADCAST_BT_OUTPUT)
                                    .putExtra(BT_OUTPUT, raw));

                    break;
            }
        }
    };

    /* sends data to the Service handler */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("CONNECTED THREAD: in", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("CONNECTED THREAD: out", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            //broadcast that the device is connected
            mLocalBroadcastManager.sendBroadcast(
                    new Intent(BROADCAST_BT_CONNECTION)
                            .putExtra(BT_CONNECTION, true)
            );
        }

        public void run() {

            mmBuffer = new byte[256];
            int bytes = 0; // bytes returned from read()

            while (true) {
                try {
                    //keep track of buffer index, only update when \r\n is found
                    mmBuffer[bytes] = (byte) mmInStream.read();

                    //first check if we have at least two bytes
                    //next check if the last two bytes where newline and carriage return
                    if (bytes > 1 && mmBuffer[bytes - 1] == '\r' && mmBuffer[bytes] == '\n') {
                        //build string from index 0 to bytes of buffer
                        final String msg = new String(mmBuffer, 0, bytes - 1);
                        Message readMsg = mHandler.obtainMessage(
                                MESSAGE_READ, bytes, -1,
                                msg);
                        readMsg.sendToTarget();
                        bytes = 0;
                    } else bytes++;

                } catch (IOException e) {

                    Log.d("CONNECTED THREAD: run", "Input stream was disconnected", e);

                    mLocalBroadcastManager.sendBroadcast(
                            new Intent(BROADCAST_BT_CONNECTION)
                                    .putExtra(BT_CONNECTION, false));

                    break;
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("CONNECTED : cancel", "Could not close the connect socket", e);
            }
        }
    }
}
