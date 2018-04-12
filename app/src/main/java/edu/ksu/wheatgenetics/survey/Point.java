package edu.ksu.wheatgenetics.survey;

/**
 * Created by trschmitz on 3/26/2018.
 */

public class Point {
    private int id;
    private String rtk;
    private String latitude;
    private String longitude;
    private String accuracy;

    public Point(int _id, String _rtk, String lat, String lng, String acc) {
        id=_id;
        rtk=_rtk;
        latitude=lat;
        longitude=lng;
        accuracy=acc;
    }

    public String toString() {
        return "ID: " + id +
                "\nLat/Lng: " + latitude + "/" + longitude;
    }

    public String getLatitude() {
        return latitude;
    }
    public String getLongitude() {
        return longitude;
    }

}
