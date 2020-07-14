package com.example.Real_Tree.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.Real_Tree.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView btnnavigationview;
    NavController  navcontroller;
    DrawerLayout drawerlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnnavigationview = (BottomNavigationView)findViewById(R.id.bottomNavigation);
        navcontroller= Navigation.findNavController(this,R.id.fragment);
        NavigationUI.setupWithNavController(btnnavigationview,navcontroller);
        final DrawerLayout drawerlayout = (DrawerLayout)findViewById(R.id.drawer);

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerlayout.openDrawer(GravityCompat.START);
            }
        });




    }
}