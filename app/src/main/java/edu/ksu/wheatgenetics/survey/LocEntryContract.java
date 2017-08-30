package edu.ksu.wheatgenetics.survey;

import android.provider.BaseColumns;

/**
 * Created by chaneylc on 8/30/2017.
 */

final class LocEntryContract {

    private LocEntryContract() {}

    /*
    Database consists of single table with id, experiment_id,
    sample_id, latitude, longitude, person, timestamp
     */
    static class LocEntry implements BaseColumns {
        static final String TABLE_NAME = "SURVEY";
        static final String COLUMN_NAME_ID = "ID";
        static final String COLUMN_NAME_EXPERIMENT_ID = "experiment_id";
        static final String COLUMN_NAME_SAMPLE_ID = "sample_id";
        static final String COLUMN_NAME_LATITUDE = "latitude";
        static final String COLUMN_NAME_LONGITUDE = "longitude";
        static final String COLUMN_NAME_PERSON = "person";
        static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LocEntry.TABLE_NAME + " (" +
                    LocEntry._ID + " INTEGER PRIMARY KEY," +
                    LocEntry.COLUMN_NAME_EXPERIMENT_ID + " TEXT," +
                    LocEntry.COLUMN_NAME_SAMPLE_ID + " TEXT," +
                    LocEntry.COLUMN_NAME_LATITUDE + " TEXT," +
                    LocEntry.COLUMN_NAME_LONGITUDE + " TEXT," +
                    LocEntry.COLUMN_NAME_PERSON + " TEXT," +
                    LocEntry.COLUMN_NAME_TIMESTAMP + " TEXT);";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LocEntry.TABLE_NAME;
}
