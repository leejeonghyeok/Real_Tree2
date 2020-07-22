package edu.skku.treearium.Activity.AR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import edu.skku.treearium.R;

public class DataActivity extends AppCompatActivity {

    EditText editName, editSpecies, editDBH, editLocation; //, editMemo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // location?
        editName = (EditText)findViewById(R.id.editName);
        editSpecies = (EditText)findViewById(R.id.editSpecies);
        editDBH = (EditText)findViewById(R.id.editDBH);
        editLocation = (EditText)findViewById(R.id.editLocation);
        //editMemo = (EditText)findViewById(R.id.editMemo);

        Intent ARIntent = getIntent();
        String dbh = ARIntent.getStringExtra("DBH");
        editName.setText(dbh);
    }
}