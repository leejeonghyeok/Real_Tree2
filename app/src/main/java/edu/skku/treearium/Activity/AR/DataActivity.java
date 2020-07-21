package edu.skku.treearium.Activity.AR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import edu.skku.treearium.R;

public class DataActivity extends AppCompatActivity {

    EditText editName, editDBH, editLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // 이름, DBH, 위치는 미리 받아와 EditText에 init해줌
        // location은 map이랑 나중에 연결시켜줘야 할 듯
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