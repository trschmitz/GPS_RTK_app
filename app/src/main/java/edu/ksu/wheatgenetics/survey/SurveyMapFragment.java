package edu.ksu.wheatgenetics.survey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;

/**
 * Created by chaneylc on 10/16/2017.
 */

public class SurveyMapFragment extends SupportMapFragment {

    public SurveyMapFragment() {
        super();
    }

    public static SurveyMapFragment newInstance(){
        return new SurveyMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);
        return v;
    }
}
