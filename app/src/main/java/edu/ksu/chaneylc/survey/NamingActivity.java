package edu.ksu.chaneylc.survey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import edu.ksu.chaneylc.seedmapper.R;

/**
 * Created by Chaney on 4/14/2017.
 */

public class NamingActivity extends AppCompatActivity {

    private SparseArray<String> _idArray;
    private SparseArray<String> _locArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_naming);

        if (_idArray == null)
            _idArray = new SparseArray<String>();

        if (_locArray == null)
            _locArray = new SparseArray<String>();

        final Intent fromHostIntent = getIntent();

        if (fromHostIntent.hasExtra(SurveyConstants.LOC_STRING_ARRAY)) {

            final ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this, R.layout.row);
            final ArrayList<String> msgArray =
                    fromHostIntent.getStringArrayListExtra(SurveyConstants.LOC_STRING_ARRAY);
            final int msgSize = msgArray.size();
            for (int i = 0; i < msgSize; i = i + 1) {
                locAdapter.add(msgArray.get(i));
            }
            final ListView lv = (ListView) findViewById(R.id.listView);
            lv.setAdapter(locAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((EditText) findViewById(R.id.inputText)).requestFocus();
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                }
            });

        }
    }
}
