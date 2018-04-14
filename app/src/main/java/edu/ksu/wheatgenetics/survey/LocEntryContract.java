package edu.ksu.wheatgenetics.survey;

import android.provider.BaseColumns;

/**
 * Created by chaneylc on 8/30/2017.
 */

final class LocEntryContract {

    private LocEntryContract() {}

    static class LocEntry implements BaseColumns {
        //static final String TABLE_NAME = "SURVEYV2";
        static final String TABLE_NAME_PLOTS = "PLOTS";
        static final String TABLE_NAME_POINTS = "POINTS";
        static final String TABLE_NAME_PLOT_POINT = "PLOT_POINT";

        /* TODO something similar but replace with your DB schema
        static final String COLUMN_NAME_ID = "ID";
        static final String COLUMN_NAME_EXPERIMENT_ID = "experiment_id";
        static final String COLUMN_NAME_SAMPLE_ID = "sample_id";
        static final String COLUMN_NAME_LATITUDE = "latitude";
        static final String COLUMN_NAME_LONGITUDE = "longitude";
        static final String COLUMN_NAME_PERSON = "person";
        static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        */

        static final String PLOTS_COL_PLOT_ID = "plot_id";
        static final String PLOTS_COL_NAME = "name";
        static final String PLOTS_COL_USER = "user";
        static final String PLOTS_COL_TIMESTAMP = "timestamp";
        static final String PLOTS_COL_CENTROID = "centroid";

        static final String POINTS_COL_POINT_ID = "point_id";
        static final String POINTS_COL_RTK = "rtk";
        static final String POINTS_COL_LAT = "latitude";
        static final String POINTS_COL_LNG = "longitude";
        static final String POINTS_COL_ACCURACY = "accuracy";

        static final String PLOT_POINT_COL_PLOT_ID = "plot_id";
        static final String PLOT_POINT_COL_POINT_ID = "point_id";
    }
    //static final String SQL_CREATE_ENTRIES = "";
    /*static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LocEntry.TABLE_NAME + " (" +
                    LocEntry._ID + " INTEGER PRIMARY KEY," +
                    LocEntry.COLUMN_NAME_EXPERIMENT_ID + " TEXT," +
                    LocEntry.COLUMN_NAME_SAMPLE_ID + " TEXT," +
                    LocEntry.COLUMN_NAME_LATITUDE + " TEXT," +
                    LocEntry.COLUMN_NAME_LONGITUDE + " TEXT," +
                    LocEntry.COLUMN_NAME_PERSON + " TEXT," +
                    LocEntry.COLUMN_NAME_TIMESTAMP + " TEXT);";*/
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LocEntry.TABLE_NAME_PLOTS + " (" +
                    LocEntry.PLOTS_COL_PLOT_ID + " INTEGER PRIMARY KEY," +
                    LocEntry.PLOTS_COL_NAME + " TEXT UNIQUE," +
                    LocEntry.PLOTS_COL_USER + " TEXT," +
                    LocEntry.PLOTS_COL_TIMESTAMP + " TEXT," +
                    LocEntry.PLOTS_COL_CENTROID + " TEXT );" +

            "CREATE TABLE " + LocEntry.TABLE_NAME_POINTS + " (" +
                    LocEntry.POINTS_COL_POINT_ID + " INTEGER PRIMARY KEY," +
                    LocEntry.POINTS_COL_RTK + " TEXT," +
                    LocEntry.POINTS_COL_LAT + " TEXT," +
                    LocEntry.POINTS_COL_LNG + " TEXT," +
                    LocEntry.POINTS_COL_ACCURACY + " TEXT );" +

            "CREATE TABLE " + LocEntry.TABLE_NAME_PLOT_POINT + " (" +
                    LocEntry.PLOT_POINT_COL_PLOT_ID + " INTEGER," +
                    LocEntry.PLOT_POINT_COL_POINT_ID + " INTEGER," +
                    "PRIMARY KEY(" + LocEntry.PLOT_POINT_COL_PLOT_ID + "," +
                                     LocEntry.PLOT_POINT_COL_POINT_ID + ")," +
                    "FOREIGN KEY(" + LocEntry.PLOT_POINT_COL_PLOT_ID + ") " +
                        "REFERENCES " + LocEntry.TABLE_NAME_PLOTS + "(" +
                        LocEntry.PLOTS_COL_PLOT_ID + ")," +
                    "FOREIGN KEY(" + LocEntry.PLOT_POINT_COL_POINT_ID + ") " +
                        "REFERENCES " + LocEntry.TABLE_NAME_POINTS + "(" +
                        LocEntry.POINTS_COL_POINT_ID + ")  );";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LocEntry.TABLE_NAME_PLOTS + ";" +
            "DROP TABLE IF EXISTS " + LocEntry.TABLE_NAME_POINTS + ";" +
            "DROP TABLE IF EXISTS " + LocEntry.TABLE_NAME_PLOT_POINT + ";";
}
