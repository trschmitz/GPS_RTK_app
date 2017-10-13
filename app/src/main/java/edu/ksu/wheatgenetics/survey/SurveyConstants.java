package edu.ksu.wheatgenetics.survey;

import android.Manifest;

class SurveyConstants {

    //package prefix
    final static String prefix ="edu.ksu.wheatgenetics.survey.";

    //requests
    final static int SETTINGS_INTENT_REQ = 100;
    final static int PERMISSION_REQUEST = 101;

    //messages
    final static int MESSAGE_READ = 200;

    //broadcasts
    final static String BROADCAST_BT_OUTPUT = prefix + "BROADCAST_BT_OUTPUT";
    final static String BROADCAST_BT_CONNECTION = prefix + "BROADCAST_BT_CONNECTION";
    final static String LOC_STRING_ARRAY = prefix + "LOC_STRING_ARRAY";

    //intents
    final static String BT_OUTPUT = prefix + "BT_OUTPUT";
    final static String BT_CONNECTION = prefix + "BT_CONNECTION";

    //permission array
    final static String[] permissions = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                                        Manifest.permission.BLUETOOTH};
}
