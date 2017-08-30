package edu.ksu.wheatgenetics.survey;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private SparseArray<LatLng> _locArray;
    private SparseArray<String> _idArray;
    private AccurateLocationListener _ll;
    private LocationManager _lm;
    private Context _ctx;
    private LatLng _prevLocation;
    private double _prevAccuracy;

    private LocEntryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        ActivityCompat.requestPermissions(
                this,
                SurveyConstants.permissions,
                100
        );

        if (_ctx == null)
            _ctx = this;

        if (_locArray == null) {
            _locArray = new SparseArray<>();
        }

        if (_idArray == null)
            _idArray = new SparseArray<>();

        if (_ll == null)
            _ll = new AccurateLocationListener();

        if (_lm == null)
            _lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        _prevAccuracy = 10.0;

        requestLocationUpdates();
    }

    private synchronized void loadDatabase() {

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
                _locArray.append(_locArray.size(), latlng);
                _idArray.append(_idArray.size(), id);

            } while(cursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int resultCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(resultCode, permissions, grantResults);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.newInstance(new GoogleMapOptions()
                .compassEnabled(true)
                .mapType(GoogleMap.MAP_TYPE_SATELLITE));
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, SurveyConstants.SETTINGS_INTENT_REQ);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
                builder.setTitle("Name this location.");
                final EditText input = new EditText(_ctx);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                final LatLng innerLatLng = latLng;
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = input.getText().toString();
                        if (!value.isEmpty()) {
                            _locArray.append(_locArray.size(), innerLatLng);
                            _idArray.append(_idArray.size(), value);
                            drawUI();
                        }
                    }
                });

                builder.show();
            }
        });

        mDbHelper = new LocEntryDbHelper(this);

        loadDatabase();

        drawUI();
        setupView();
    }

    private void drawUI() {

        mMap.clear();

        if (_locArray != null) {
            final int locSize = _locArray.size();
            if (locSize > 0) {
                for (int i = 0; i < locSize; i = i + 1) {
                    drawCross(_locArray.get(_locArray.keyAt(i)));
                }
            }
        }

        if (_prevLocation != null)
            drawUser(_prevLocation);
    }

    //function to modify camera for initial view
    private void setupView() {
        //add test begin point at East Stadium, KSU
        mMap.moveCamera(
                CameraUpdateFactory.newLatLng(new LatLng(39.187248, -96.583852))
        );
    }

    private LatLng getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final Location l = _lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return new LatLng(l.getLatitude(), l.getLongitude());
        }

        return null;
    }

    private void requestLocationUpdates() {

        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_ctx);
        final float min_distance = Float.valueOf(sharedPrefs.getString(SettingsActivity.MIN_DISTANCE, "1.0"));
        final long min_time = Long.valueOf(sharedPrefs.getString(SettingsActivity.MIN_TIME, "2000"));

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            _lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    min_time, min_distance, _ll);
        }
    }

    private void removeUpdates(Context ctx) {

        if (ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            _lm.removeUpdates(_ll);
        }
    }

    private void drawCross(LatLng latLng) {

        final double lat = latLng.latitude;
        final double lng = latLng.longitude;
        if(mMap != null) {
            mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(lat + 0.00005, lng + 0.00005))
                    .add(new LatLng(lat - 0.00005, lng - 0.00005)));
            mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(lat + 0.00005, lng - 0.00005))
                    .add(new LatLng(lat - 0.00005, lng + 0.00005)));
        }
    }

    private void drawUser(LatLng latLng) {

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
        final double min_accuracy = Double.valueOf(sharedPref.getString(SettingsActivity.MIN_ACCURACY, "10"));

        final double lat = latLng.latitude;
        final double lng = latLng.longitude;

        if (mMap != null) {

            mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(1.0));
            mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .fillColor(Color.argb(126, 0, 255, 0))
                    .radius(_prevAccuracy));

        }
    }

    private boolean isExternalStorageWritable() {

        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);

    }

    private boolean isExternalStorageReadable() {

        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);

    }

    private class AccurateLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
            final double min_accuracy = Double.valueOf(sharedPref.getString(SettingsActivity.MIN_ACCURACY, "10"));
            //check if accuracy is below the maximum requested accuracy
            if (location != null)
                if (location.hasAccuracy() && location.getAccuracy() <= min_accuracy) {
                    _prevAccuracy = location.getAccuracy();
                    _prevLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    drawUI();
                }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}