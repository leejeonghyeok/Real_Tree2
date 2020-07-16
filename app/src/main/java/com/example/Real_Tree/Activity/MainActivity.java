package com.example.Real_Tree.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.Real_Tree.Activity.AR.HelloArActivity;
import com.example.Real_Tree.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bnv;
    NavController  nc;
    DrawerLayout drawerlayout;
    FloatingActionButton arbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerlayout = findViewById(R.id.drawerlayout);
        bnv = (BottomNavigationView)findViewById(R.id.bottomNavigation);
        nc= Navigation.findNavController(this,R.id.fragment);
        NavigationUI.setupWithNavController(bnv,nc);

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerlayout.openDrawer(GravityCompat.START);
            }
        });

        findViewById(R.id.arbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HelloArActivity.class);
                startActivity(intent);
            }
        });
    }
}