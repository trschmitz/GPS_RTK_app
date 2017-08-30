package edu.ksu.wheatgenetics.survey;

import android.location.Location;

/**
 * Created by Chaney on 1/30/2017.
 */

class LatLngUtil {

    /* uses the Haversine method to calculate distance between two GPS coordinates */
    static Double distanceHaversine(Location a, Location b) {

        final double lata = a.getLatitude();
        final double lnga = a.getLongitude();
        final double latb = b.getLatitude();
        final double lngb = b.getLongitude();
        final double R = 6371.0; //radius of the Earth
        final double latDst = Math.toRadians(latb - lata);
        final double lngDst = Math.toRadians(lngb - lnga);
        final double A = Math.sin(latDst / 2) * Math.sin(latDst / 2)
                + Math.cos(Math.toRadians(lata)) * Math.cos(Math.toRadians(latb))
                * Math.sin(lngDst / 2) * Math.sin(lngDst / 2);
        final double c = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1 - A));
        double dst = R * c * 1000.0;
        //double height = el1 - el2;
        //dst = Math.pow(dst, 2);
        //return Math.sqrt(dst);
        return dst;
    }

    static Location geodesicDestination(Location start, double bearing, double distance) {

        final double latRads = Math.toRadians(start.getLatitude());
        final double lngRads = Math.toRadians(start.getLongitude()); //(Degrees * Math.PI) / 180.0;
        //final double bearing = azimuth;//location.getBearing(); //created weighted vector with bearing...?
        final double R = 6371.0; //radius of the Earth
        final double angDst = distance / 6371.0; // d/R distance to point B over Earth's radius
        final double lat2 = Math.asin(Math.sin(latRads) * Math.cos(angDst) +
                Math.cos(latRads) * Math.sin(angDst) * Math.cos(bearing));
        final double lng2 = lngRads + Math.atan2(Math.sin(bearing) * Math.sin(angDst) * Math.cos(latRads),
                Math.cos(angDst) - Math.sin(latRads) * Math.sin(lat2));

        final Location l = new Location("end point");
        l.setLatitude(Math.toDegrees(lat2));
        l.setLongitude(Math.toDegrees(lng2));
        return l;
    }
}
