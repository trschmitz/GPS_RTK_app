package edu.ksu.wheatgenetics.survey;

import android.Manifest;

public class SurveyConstants {
    final static int SETTINGS_INTENT_REQ = 100;
    final static String LOC_STRING_ARRAY = "edu.ksu.wheatgenetics.survey.LOC_STRING_ARRAY";
    final static String[] permissions = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.ACCESS_FINE_LOCATION};
}
