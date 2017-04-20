package edu.ksu.wheatgenetics.survey;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

/**
 * Created by Chaney on 4/20/2017.
 */

public class SettingsActivity extends PreferenceActivity {

    public static String MIN_ACCURACY = "edu.ksu.wheatgenetics.survey.MIN_ACCURACY";
    public static String MIN_DISTANCE = "edu.ksu.wheatgenetics.survey.MIN_DISTANCE";
    public static String MIN_TIME = "edu.ksu.wheatgenetics.survey.MIN_TIME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasHeaders()) {

        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public static class PrefsLocationListenerFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            //PreferenceManager.setDefaultValues(getActivity(),
            //       R.xml.preferences, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_location_listener);
        }
    }

    @Override
    protected boolean isValidFragment(String fragName) {
        return PrefsLocationListenerFragment.class.getName().equals(fragName);
    }
}