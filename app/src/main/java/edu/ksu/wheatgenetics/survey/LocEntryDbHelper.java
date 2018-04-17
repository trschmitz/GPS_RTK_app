package edu.ksu.wheatgenetics.survey;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_CREATE_ENTRIES;
//import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_DELETE_ENTRIES;
import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_CREATE_PLOTS;
import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_CREATE_POINTS;
import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_CREATE_PLOT_POINT;
import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_DELETE_PLOT_POINT;
import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_DELETE_POINTS;
import static edu.ksu.wheatgenetics.survey.LocEntryContract.SQL_DELETE_PLOTS;

/**
 * Created by chaneylc on 8/30/2017.
 */

public class LocEntryDbHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "LocEntryReader.db";

    LocEntryDbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PLOTS);
        db.execSQL(SQL_CREATE_POINTS);
        db.execSQL(SQL_CREATE_PLOT_POINT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PLOT_POINT);
        db.execSQL(SQL_DELETE_POINTS);
        db.execSQL(SQL_DELETE_PLOTS);
        onCreate(db);
    }
}
