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
    private String centroid = null;
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
    public long getID() { return id; }

    public String toString() {
        if (centroid == null) calcCentroid();
        return "ID: " + id +
                "\nName: " + name +
                "\nUser: " + user +
                "\nTimestamp: " + timestamp +
                "\nCentroid " + centroid;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getCentroid() {
        if (centroid == null) calcCentroid();
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
            if (p.getLatitude() != null && p.getLongitude() != null && !p.getLatitude().equals("null") && !p.getLatitude().equals("null")) {
                sumLat += Float.parseFloat(p.getLatitude());
                sumLon += Float.parseFloat(p.getLongitude());
            }
        }
        float centroidLat = sumLat / (float)points.size();
        float centroidLng = sumLon / (float)points.size();
        if (centroidLat != 0 && centroidLng != 0 && !Float.isNaN(centroidLat) && !Float.isNaN(centroidLng)) {
            centroid = "Lat/Long: " + centroidLat + "/" + centroidLng;
        } else {
            centroid = "unable to calculate";
        }
    }
}
