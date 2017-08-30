package edu.ksu.wheatgenetics.survey;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.List;

/**
 * Created by Chaney on 4/20/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    public static String PERSON = "edu.ksu.wheatgenetics.survey.PERSON";
    public static String EXPERIMENT = "edu.ksu.wheatgenetics.survey.EXPERIMENT_ID";
    public static String MIN_ACCURACY = "edu.ksu.wheatgenetics.survey.MIN_ACCURACY";
    public static String MIN_DISTANCE = "edu.ksu.wheatgenetics.survey.MIN_DISTANCE";
    public static String MIN_TIME = "edu.ksu.wheatgenetics.survey.MIN_TIME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().getThemedContext();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}