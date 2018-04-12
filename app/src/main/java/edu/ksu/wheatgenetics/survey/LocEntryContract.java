package edu.ksu.wheatgenetics.survey;

import android.provider.BaseColumns;

/**
 * Created by chaneylc on 8/30/2017.
 */

final class LocEntryContract {

    private LocEntryContract() {}

    static class LocEntry implements BaseColumns {
        //static final String TABLE_NAME = "SURVEYV2";
        static final String TABLE_NAME_PLOT = "PLOTS";
        static final String TABLE_NAME = "POINTS";

        /* TODO something similar but replace with your DB schema
        static final String COLUMN_NAME_ID = "ID";
        static final String COLUMN_NAME_EXPERIMENT_ID = "experiment_id";
        static final String COLUMN_NAME_SAMPLE_ID = "sample_id";
        static final String COLUMN_NAME_LATITUDE = "latitude";
        static final String COLUMN_NAME_LONGITUDE = "longitude";
        static final String COLUMN_NAME_PERSON = "person";
        static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        */
        static final String PTS_COL_NAME_ID = "id";
        static final String PTS_COL_NAME_RTK = "rtk";
        static final String PTS_COL_NAME_LAT = "latitude";
        static final String PTS_COL_NAME_LNG = "longitude";
        static final String PTS_COL_NAME_ACCURACY = "accuracy";

        static final String PLOTS_COL_NAME_ID = "id";
        static final String PLOTS_COL_NAME_NAME = "name";
        static final String PLOTS_COL_NAME_USER = "user";
        static final String PLOTS_COL_NAME_TIMESTAMP = "timestamp";
        static final String PLOTS_COL_NAME_CENTROID = "centroid";
    }

    //TODO
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
            "CREATE TABLE " + LocEntry.TABLE_NAME + " (" +
                    LocEntry.PTS_COL_NAME_ID + " INTEGER PRIMARY KEY," +
                    LocEntry.PTS_COL_NAME_RTK + " TEXT," +
                    LocEntry.PTS_COL_NAME_LAT + " TEXT," +
                    LocEntry.PTS_COL_NAME_LNG + " TEXT," +
                    LocEntry.PTS_COL_NAME_ACCURACY + " TEXT);";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LocEntry.TABLE_NAME;
            //"DROP TABLE IF EXISTS " + LocEntry.TABLE_NAME_POINTS;
}
