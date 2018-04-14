package edu.ksu.wheatgenetics.survey;

import java.util.ArrayList;

/**
 * Created by trschmitz on 3/27/2018.
 */

public class Plot {
    private long id;
    private String name;
    private String user;
    private String timestamp;
    private String centroid;
    private ArrayList<Point> points = new ArrayList<>();

    public Plot(/*int _id, */String _name, String _user, String _timestamp) {
        //id = _id;
        name = _name;
        user = _user;
        timestamp = _timestamp;
    }

    public void addPoint(Point newPt) {
        points.add(newPt);
    }
    public ArrayList<Point> getPoints() {
        return points;
    }
    public void setID(long ID) {
        id = ID;
    }

    public String toString() {
        calcCentroid();
        return "Name: " + name +
                "\nTimestamp: " + timestamp +
                "\nCentroid: " + centroid;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getCentroid() {
        calcCentroid();
        return centroid;
    }
    public String getName() {
        return name;
    }
    public String getUser() {
        return user;
    }
    public String getTimestamp() {
        return timestamp;
    }

    private void calcCentroid() {
        //only calculates the center of the points, not really the centroid of the shape
        float sumLat = 0;
        float sumLon = 0;
        for (Point p: points) {
            if (p.getLatitude() != null && p.getLatitude() != null) {
                sumLat+= Float.parseFloat(p.getLatitude());
                sumLon+= Float.parseFloat(p.getLongitude());
            }
        }
        float centroidLat = sumLat / points.size();
        float centroidLng = sumLon / points.size();
        if (centroidLat != 0 && centroidLng != 0) {
            centroid = "Lat/Long: " + centroidLat + "/" + centroidLng;
        } else {
            centroid = "unable to calculate";
        }
    }
}
