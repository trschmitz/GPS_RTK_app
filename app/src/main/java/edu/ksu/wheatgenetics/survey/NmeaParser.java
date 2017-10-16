package edu.ksu.wheatgenetics.survey;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by chaneylc on 10/13/2017.
 */

class NmeaParser {

    private String mPrevSentence;
    private String mNumberOfSatellites;
    private String mTimestamp, mLatitude, mLongitude, mFixQuality,
        mHorizontalDOP, mAltitude, mMeanSeaLevel;

    NmeaParser() {
    }

    void feed(String nmea) {
        final String[] tokens = nmea.split(",");
        switch (tokens[0]) {
            case "$GPGSV":
                mPrevSentence = "GSV";
                mNumberOfSatellites = tokens[3];
                break;
            case "$GPGGA":
                mPrevSentence = "GGA";
                mTimestamp = tokens[1];
                mLatitude = tokens[3].equals("S") ? "-" + tokens[2] : tokens[2];
                mLongitude =  tokens[5].equals("W") ? "-" + tokens[4] : tokens[4];
                parseFixQuality(tokens[6]);
                mNumberOfSatellites = tokens[7];
                mHorizontalDOP = tokens[8];
                mAltitude = tokens[9] + tokens[10];
                mMeanSeaLevel = tokens[11] + tokens[12];
                break;
            case "$RMC":
                mPrevSentence = "RMC";
                mTimestamp = tokens[1];
                mLatitude = tokens[4].equals("S") ? "-" + tokens[3] : tokens[3];
                mLongitude = tokens[6].equals("W") ? "-" + tokens[5] : tokens[5];
                break;
            case "$LCGLL": //GPS may be emulating Loran data
            case "$GPGLL":
                mPrevSentence = "GLL";
                mLatitude = tokens[2].equals("S") ? "-" + tokens[1] : tokens[1];
                mLongitude = tokens[4].equals("W") ? "-" + tokens[3] : tokens[3];
                mTimestamp = tokens[5];

        }
    }

    private void parseFixQuality(String fix) {
        switch (fix) {
            case "0":
                mFixQuality = "invalid";
                break;
            case "1":
                mFixQuality = "GPS";
                break;
            case "2":
                mFixQuality = "DGPS";
                break;
            case "3":
                mFixQuality = "PPS";
                break;
            case "4":
                mFixQuality = "RTK";
                break;
            case "5":
                mFixQuality = "Float RTK";
                break;
            case "6":
                mFixQuality = "estimated";
                break;
            case "7":
                mFixQuality = "manual input mode";
                break;
            case "8":
                mFixQuality = "simulation";
                break;
        }
    }

    String getLatitude() {
        return mLatitude;
    }

    String getLongitude() {
        return mLongitude;
    }

    @Override
    public String toString() {
        return "Number of satellites: " + mNumberOfSatellites
                + "\n" + "Timestamp: " + mTimestamp
                + "\n" + "Latitude: " + mLatitude
                + "\n" + "Longitude: " + mLongitude
                + "\n" + "HDOP: " + mHorizontalDOP
                + "\n" + "Altitude: " + mAltitude
                + "\n" + "Mean Sea Level: " + mMeanSeaLevel;
    }
}
