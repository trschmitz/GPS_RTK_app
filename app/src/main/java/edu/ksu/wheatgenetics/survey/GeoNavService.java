package edu.ksu.wheatgenetics.survey;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Chaney on 2/9/2017.
 */

public class GeoNavService extends Service implements SensorEventListener {

    private SparseArray<Pair<Double, Double>> _coordinates;
    private HashMap<String, double[]> _mapCoordinates;
    private Location _prevLocation;
    private AccurateLocationListener _ll;
    private LocationManager _lm;
    private SensorManager _sm;
    private Float _max_accuracy, _min_distance;
    private Sensor _accelerometer, _magnetometer;
    private Long _min_time;
    private float[] _gravity;
    private float[] _geomagnetic;
    private float azimuth;
    private final double thetaThresh = Math.PI / 6;
    private final double rmin = 0.003; //add distance constraint to Impact Zone
    private final double rmax = 0.033;

    @Override
    public void onCreate() {

        super.onCreate();

        _mapCoordinates = null;
        _coordinates = null;
        _ll = new AccurateLocationListener();
        _max_accuracy = Float.MAX_VALUE;
        _min_distance = Float.MIN_VALUE;
        _min_time = Long.MIN_VALUE;
        _sm = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        _accelerometer = _sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _magnetometer = _sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        _lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        _sm.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        _sm.registerListener(this, _magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        _prevLocation = getLastKnownLocation();
        requestLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            if (intent.hasExtra(GeoNavConstants.MAP_EXTRA)) {
                _coordinates = null;
                _mapCoordinates = (HashMap<String, double[]>)
                        intent.getSerializableExtra(GeoNavConstants.MAP_EXTRA);
            } else if (intent.hasExtra("array")) {
                _mapCoordinates = null;
                _coordinates = new SparseArray<>();
                final double[] coords = intent.getDoubleArrayExtra("array");
                final int size = coords.length;
                if (size > 1) {
                    for (int i = 1; i < size; i = i + 2) {
                        _coordinates.setValueAt(_coordinates.size(), new Pair<Double, Double>(
                                coords[i - 1], coords[i]
                        ));
                    }
                }
            } else if (intent.hasExtra("csv")) {
                //TODO handle csv data.
            }

            if (intent.hasExtra("Accuracy")) {
                _max_accuracy = intent.getFloatExtra("Accuracy", Float.MAX_VALUE);
            }
            if (intent.hasExtra("Distance")) {
                _min_distance = intent.getFloatExtra("Distance", Float.MIN_VALUE);
                removeUpdates(this);
                requestLocationUpdates();
            }
            if (intent.hasExtra("Time")) {
                _min_time = intent.getLongExtra("Time", Long.MIN_VALUE);
                removeUpdates(this);
                requestLocationUpdates();
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Location getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            return _lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        return null;
    }

    private void requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            _lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            _min_time, _min_distance, _ll);
        }
    }

    private void removeUpdates(Context ctx) {

        if (ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            _lm.removeUpdates(_ll);
        }
    }

    private void broadcastLocation(Location l) {

        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent(GeoNavConstants.BROADCAST_LOCATION)
                    .putExtra(GeoNavConstants.LOCATION, l)
        );
    }

    private void broadcastPlotId(String id) {

        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent(GeoNavConstants.BROADCAST_PLOT_ID)
                    .putExtra(GeoNavConstants.PLOT_ID, id)
        );
    }

    private void broadcastAccuracy(float a) {

        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent(GeoNavConstants.BROADCAST_ACCURACY)
                        .putExtra(GeoNavConstants.ACCURACY, a)
        );
    }

    private void broadcastUserLocation() {

        broadcastLocation(_prevLocation);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //android API requires accelerometer and magnetometer values in order to calculate
        //the orientation of the device.
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            _gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            _geomagnetic = event.values;
        }

        //once we have these values the API can be called to get the device's azimuth,
        // which is measured by radians from true North.
        if (_gravity != null && _geomagnetic != null) {

            final float R[] = new float[9];
            final float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, _gravity, _geomagnetic)) {
                final float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0]; //radians from true North

                //if we have a location and the user has requested a set of coordinates
                //we can begin the calculation by first setting a 'start' variable representing
                //the user's location, and finding the bearing from the user location to
                //each coordinate requested by the user.
                if (_prevLocation != null) {

                    final Location start = new Location("user location");
                    start.setLatitude(_prevLocation.getLatitude());
                    start.setLongitude(_prevLocation.getLongitude());

                    //greedy algorithm to find closest point, first point is set to inf
                    double closest_distance = Double.MAX_VALUE;
                    Location closest_point = new Location("closest point to user");

                    if (_coordinates != null) {
                        for (int i = 0; i < _coordinates.size(); i++) {
                            final Location l = new Location("temp");
                            l.setLatitude(_coordinates.get(i).first);
                            l.setLongitude(_coordinates.get(i).second);
                            if (checkThetaThreshold(l)) {
                                final double distance = LatLngUtil.distanceHaversine(start, l);
                                if (closest_distance > distance) {
                                    closest_distance = distance;
                                    closest_point = l;
                                }
                            }
                        }
                        broadcastLocation(closest_point);
                    } else if (_mapCoordinates != null) {
                        final Iterator i = _mapCoordinates.entrySet().iterator();
                        String closest_id = null;
                        while(i.hasNext()) {
                            final Map.Entry me = (Map.Entry) i.next();
                            final Location l = new Location("temp");
                            final double[] coords = (double[]) me.getValue();
                            l.setLatitude(coords[0]);
                            l.setLongitude(coords[1]);
                            if (checkThetaThreshold(l)) {
                                final double distance = LatLngUtil.distanceHaversine(start, l);
                                if (closest_distance > distance) {
                                    closest_distance = distance;
                                    closest_id = (String) me.getKey();
                                }
                            }
                        }
                        broadcastPlotId(closest_id);
                    }
                }
            }
        }
    }

    private boolean checkThetaThreshold(Location l) {

        if (_prevLocation != null) {
            //find the direction from user to target.
            final double userToMarkerBearing = Math.toRadians(_prevLocation.bearingTo(l));
            //test if the direction found above is within a threshold from our user's bearing (azimuth)
            if (userToMarkerBearing >= azimuth - thetaThresh &&
                    userToMarkerBearing <= azimuth + thetaThresh) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class AccurateLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            //check if accuracy is below the maximum requested accuracy
            if (location != null)
                if (location.hasAccuracy() && location.getAccuracy() <= _max_accuracy) {
                    _prevLocation = location;
                    broadcastLocation(location);
                    broadcastAccuracy(location.getAccuracy());
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
