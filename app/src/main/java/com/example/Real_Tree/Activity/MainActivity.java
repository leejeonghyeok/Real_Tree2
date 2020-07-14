package com.example.Real_Tree.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.Real_Tree.Activity.AR.ArActivity;
import com.example.Real_Tree.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bnv;
    NavController  nc;
    Button arbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //네비게이션 바
        bnv = (BottomNavigationView)findViewById(R.id.bottomNavigation);
        nc = Navigation.findNavController(this,R.id.fragment);
        NavigationUI.setupWithNavController(bnv,nc);

        //AR 버튼
        /*arbtn = (Button)findViewById(R.id.arbutton);
        arbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intentAr = new Intent(MainActivity.this, ArActivity.class);
                startActivity(intentAr);
            }
        });
        */
    }
}