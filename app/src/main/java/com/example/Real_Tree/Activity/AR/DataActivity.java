package com.example.Real_Tree.Activity.AR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.example.Real_Tree.R;

public class DataActivity extends AppCompatActivity {

    EditText editName, editDBH, editLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        editName = (EditText)findViewById(R.id.editName);
        editDBH = (EditText)findViewById(R.id.editDBH);
        editLocation = (EditText)findViewById(R.id.editLocation);

        Intent ARIntent = getIntent();
        String name = ARIntent.getStringExtra("name");
        editName.setText(name);
        String dbh = ARIntent.getStringExtra("dbh");
        editName.setText(dbh);
        String location = ARIntent.getStringExtra("location");
        editName.setText(location);
    }
}