package edu.ksu.wheatgenetics.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SparseArray<LatLng> mLocationArray;
    private SparseArray<String> mIdArray;
    private LatLng mStartLocation;

    private LocEntryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        if (mLocationArray == null) {
            mLocationArray = new SparseArray<>();
        }

        if (mIdArray == null)
            mIdArray = new SparseArray<>();

        loadDatabase();
    }

    private synchronized void loadDatabase() {

        mDbHelper = new LocEntryDbHelper(this);

        //keep track of most NE and most SE locations for latlngbounds
        //LatLng mostNE = null;
        //LatLng mostSW = null;

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
               /* if (mostNE == null) mostNE = latlng;
                if (mostSW == null) mostSW = latlng;
                else if(mostNE.latitude < latlng.latitude &&
                        mostNE.longitude < latlng.longitude) mostNE = latlng;
                else if(mostSW.latitude > latlng.latitude &&
                        mostSW.longitude > latlng.longitude) mostSW = latlng;*/
                mLocationArray.append(mLocationArray.size(), latlng);
                mIdArray.append(mIdArray.size(), id);

            } while(cursor.moveToNext());
        }

        //after db is finished loading populate the map
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mDbHelper = new LocEntryDbHelper(this);

        drawUI();

        setupView();
    }

    private void drawUI() {

        if (mMap != null) {
            mMap.clear();

            if (mLocationArray != null) {
                final int locSize = mLocationArray.size();
                if (locSize > 0) {
                    for (int i = 0; i < locSize; i = i + 1) {
                        drawMarker(mLocationArray.get(mLocationArray.keyAt(i)));
                    }
                }
            }
        }
    }

    //function to modify camera for initial view
    private void setupView() {
        //add test begin point at East Stadium, KSU
        if (mMap != null) {
            mMap.moveCamera(
                    CameraUpdateFactory.newLatLng(mStartLocation)
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
}