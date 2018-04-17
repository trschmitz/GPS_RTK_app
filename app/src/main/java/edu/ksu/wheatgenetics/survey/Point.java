package edu.ksu.wheatgenetics.survey;

/**
 * Created by trschmitz on 3/26/2018.
 */

public class Point {
    private long id;
    private String rtk;
    private String latitude;
    private String longitude;
    private String accuracy;

    public Point(/*int _id, */String _rtk, String lat, String lng, String acc) {
        //id=_id;
        rtk=_rtk;
        latitude=lat;
        longitude=lng;
        accuracy=acc;
    }

    public String toString() {
        return "ID: " + id +
                "\nRTK: " + rtk +
                "\nLat/Lng: " + latitude + "/" + longitude +
                "\nAcc: " + accuracy;
    }

    public String getLatitude() {
        return latitude;
    }
    public String getLongitude() {
        return longitude;
    }
    public String getRtk() {
        return rtk;
    }
    public String getAccuracy() {
        return accuracy;
    }
    public long getId() {
        return id;
    }
    public void setID(long ID) {
        id = ID;
    }

}
