package edu.ksu.wheatgenetics.survey;

import android.Manifest;
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
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static edu.ksu.wheatgenetics.survey.SurveyConstants.BROADCAST_BT_CONNECTION;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.BROADCAST_BT_OUTPUT;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.BT_CONNECTION;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.BT_OUTPUT;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.MESSAGE_READ;
import static edu.ksu.wheatgenetics.survey.SurveyConstants.PERMISSION_REQUEST;

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

    //bluetooth
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ConnectedThread mConnectedThread;

    //nmea parser
    private NmeaParser mNmeaParser;

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

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_toolbar, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_connect_bluetooth:
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                findPairedBTDevice();
                return true;
            case R.id.action_map_locations:
                final Intent mapsActivity = new Intent(this, MapsActivity.class);
                startActivity(mapsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                        String value = bluetoothDevicesAdapter.getItem(devices.getCheckedItemPosition());
                        if (value != null) {
                            mBluetoothDevice = bluetoothMap.get(value);
                            new ConnectThread(mBluetoothDevice).start();
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
                } else {
                    Toast.makeText(MainActivity.this, "Entry must have a name and location.", Toast.LENGTH_SHORT).show();
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

       // final Intent geoNavServiceIntent = new Intent(this, GeoNavService.class);
       // startService(geoNavServiceIntent);

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

            if (intent.hasExtra(BT_OUTPUT)) {

                final String raw = intent.getStringExtra(BT_OUTPUT);
                mNmeaParser.feed(raw);
                mLastLongitude = mNmeaParser.getLongitude();
                mLastLatitude = mNmeaParser.getLatitude();
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
