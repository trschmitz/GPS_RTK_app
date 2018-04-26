package edu.ksu.wheatgenetics.survey;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

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
    private LatLng llCentroid = null;
    private ArrayList<Point> points = new ArrayList<>();
    private Marker centroidMarker;
    private Polygon outlinePoly;

    Plot(String _name, String _user, String _timestamp) {
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
        String result =  "ID: " + id +
                "\nName: " + name +
                "\nUser: " + user +
                "\nTimestamp: " + timestamp +
                "\nCentroid " + centroid;
        if (llCentroid != null ) {
            result += "\nLLCentroid: " + llCentroid.toString();
        }
        return result;
    }

    public void setName(String _name) {
        name = _name;
        if (centroidMarker != null) {
            centroidMarker.setTitle(name);
        }
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

    public void calcCentroid() {
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
            llCentroid = new LatLng(centroidLat, centroidLng);
        } else {
            centroid = "unable to calculate";
            llCentroid = null;
        }
    }

    public void markMap(GoogleMap mMap) {
        if (llCentroid == null) calcCentroid();
        if (mMap != null) {
            if (llCentroid != null) {
                centroidMarker = mMap.addMarker(new MarkerOptions()
                        .position(llCentroid)
                        .title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                centroidMarker.setPosition(llCentroid);

            }
            PolygonOptions polyOptions = new PolygonOptions();
            for (Point p: points) {
                if (p.getLatitude() == null || p.getLatitude().equals("null")) continue;
                polyOptions.add(new LatLng(Double.parseDouble(p.getLatitude()),Double.parseDouble(p.getLongitude())));
            }
            polyOptions.strokeColor(Color.BLUE);
            if (polyOptions.getPoints().size() > 0) {
                outlinePoly = mMap.addPolygon(polyOptions);
            }
        }
    }
    public void unMarkMap() {
        if (centroidMarker != null) {
            centroidMarker.remove();
        }
        if (outlinePoly != null) {
            outlinePoly.remove();
        }
    }
}
