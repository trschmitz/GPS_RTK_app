package edu.ksu.chaneylc.survey;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileWriter;

import edu.ksu.chaneylc.seedmapper.R;

public class MapsActivity extends FragmentActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private SparseArray<LatLng> _locArray;
    private SparseArray<String> _idArray;
    private Context _ctx;
    private int _namedCount;
    private File _dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        requestPermission();

        if (_ctx == null)
            _ctx = this;

        if (_locArray == null) {
            _locArray = new SparseArray<>();
        }

        if (_idArray == null)
            _idArray = new SparseArray<>();

        _namedCount = _idArray.size();

        //create google api client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(LocationServices.API)
                    .build();
        }

        ((Button) findViewById(R.id.saveButton))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (_dir != null) {
                            final String filename = _dir.getAbsolutePath().toString() +
                                    File.separator + "coordinates.csv";
                            final File f = new File(filename);
                            final FileWriter writer;
                            try {
                                if (f.exists() && !f.isDirectory()) {
                                    writer = new FileWriter(filename, true);
                                    final int size = _locArray.size();
                                    for (int i = 0; i < size; i = i + 1) {
                                        final LatLng tmp = _locArray.get(_locArray.keyAt(i));
                                        writer.append(_idArray.get(_idArray.keyAt(i)));
                                        writer.append(",");
                                        writer.append(String.valueOf(tmp.latitude));
                                        writer.append(",");
                                        writer.append(String.valueOf(tmp.longitude));
                                        writer.append("\n");
                                    }
                                    writer.close();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        ((EditText) findViewById(R.id.inputText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                final Button writeButton = (Button) findViewById(R.id.writeButton);
                if (s != null && s.length() > 0) {
                    final int size = _idArray.size();
                    for (int i = 0; i < size; i = i + 1) {
                        if (s.equals(_idArray.get(_idArray.keyAt(i)))) {
                            writeButton.setEnabled(false);
                            break;
                        }
                    }
                    writeButton.setEnabled(true);
                } else writeButton.setEnabled(false);
            }
        });

        ((Button) findViewById(R.id.writeButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FIFO naming
                final EditText et = (EditText) findViewById(R.id.inputText);
                _idArray.append(_namedCount++, et.getText().toString());

                if (_namedCount < _locArray.size())
                    et.setHint(_locArray.get(_locArray.keyAt(_namedCount)).toString());
                else et.setHint(null);
                et.setText("");
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (_dir == null)
            _dir = _ctx.getDir("Survey", Context.MODE_PRIVATE);
    }

    public String buildCoordinatesString() {

        final StringBuilder sb = new StringBuilder();
        sb.append("Latitude, Longitude"); //append header line
        if (_locArray != null && _locArray.size() > 0) {
            final int locSize = _locArray.size();
            for (int i = 0; i < locSize; i = i + 2) {
                sb.append("\n");
                sb.append(_locArray.get(_locArray.keyAt(i)).toString());
                sb.append(",");
                sb.append(_locArray.get(_locArray.keyAt(i+1)).toString());
            }
        }
        return sb.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
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
    }

    //function to modify camera for initial view
    private void setupView() {
        //add test begin point at East Stadium, KSU
        mMap.moveCamera(
                CameraUpdateFactory.newLatLng(new LatLng(39.187248, -96.583852))
        );
    }

    //uses device service to update GPS coordinates and calls this callback
    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            Log.d(TAG, "onLocationChanged(" + location + ")");

            drawUI();

        }
    }

    private void getPrevLoc() {

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocUpdates();
        } else requestPermission();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                100
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //get location manager, request permissions to access GPS if needed
        final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (PackageManager.PERMISSION_GRANTED !=
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
                startLocUpdates();
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    private void startLocUpdates() {

        final int UPDATE_INTERVAL = 10000;
        final int FASTEST_INTERVAL = 5000;
        final float SMALLEST_DISPLACEMENT = 10f;
        final LocationRequest locRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(UPDATE_INTERVAL)
                .setSmallestDisplacement(SMALLEST_DISPLACEMENT)
                .setFastestInterval(FASTEST_INTERVAL);
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locRequest, (com.google.android.gms.location.LocationListener) this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapClick(LatLng latLng) {

        _locArray.append(_locArray.size(), latLng);
        _idArray.append(_idArray.size(), String.valueOf(latLng.hashCode()));

        final EditText et = (EditText) findViewById(R.id.inputText);
        if (et.getHint() == null || et.getHint().toString().isEmpty()) {
            et.setHint(latLng.toString());
        }
        drawUI();
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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getPrevLoc();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
