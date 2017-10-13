package edu.ksu.wheatgenetics.survey;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by chaneylc on 10/13/2017.
 */

public class NmeaParser {

    private int mNumberOfSatellites;
    private String mTimestamp, mLatitude, mLongitude;

    NmeaParser() {
        mNumberOfSatellites = 0;
    }

    public void feed(String nmea) {
        final String[] tokens = nmea.split(",");
        switch (tokens[0]) {
            case "$GPGSV":
                mNumberOfSatellites = Integer.parseInt(tokens[3], 10);
                break;
            case "$GPGGA":
                mTimestamp = tokens[1];
                mLatitude = tokens[2] + tokens[3];
                mLongitude = tokens[4] + tokens[5];

        }
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    @Override
    public String toString() {
        return "Number of satellites: " + mNumberOfSatellites
                + "\n" + "Timestamp: " + mTimestamp
                + "\n" + "Latitude: " + mLatitude
                + "\n" + "Longitude: " + mLongitude;
    }
}
