package edu.skku.treearium.Activity;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import edu.skku.treearium.Activity.AR.ArActivity;
import edu.skku.treearium.Activity.MainPackage.Fragment1;
import edu.skku.treearium.Activity.Search.SearchActivity;
import edu.skku.treearium.Activity.login.LoginActivity;
import edu.skku.treearium.R;
import edu.skku.treearium.helpers.LocationHelper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.skku.treearium.Activity.MainPackage.fragment2_test.dbhlist;
import static edu.skku.treearium.Activity.MainPackage.fragment2_test.geolist;
import static edu.skku.treearium.Activity.MainPackage.fragment2_test.helist;
import static edu.skku.treearium.Activity.MainPackage.fragment2_test.namelist;
import static edu.skku.treearium.Activity.MainPackage.fragment2_test.splist;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    BottomNavigationView bnv;
    NavController  nc;
    FloatingActionButton btn;
    DrawerLayout drawerLayout;
    public static FirebaseFirestore fstore;
    FirebaseAuth mFirebaseAuth;
    String userID;
    ImageButton mSearchBtn;
    public static String finalUserId;
    public static int datasize;
    TextView musername, museremail;
    private long lastTimeBackPressed;;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNavigationViewListener();
        //네비게이션 바
        bnv = (BottomNavigationView)findViewById(R.id.bottomNavigation);
        nc = Navigation.findNavController(this,R.id.fragment);
        NavigationUI.setupWithNavController(bnv,nc);

        //AR 버튼
        btn = findViewById(R.id.arbutton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentAr = new Intent(MainActivity.this, ArActivity.class);
                try {
                    startActivity(intentAr);
                } catch (ActivityNotFoundException e) {
                    System.out.println("error");
                }
            }
        });
        mFirebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID= mFirebaseAuth.getCurrentUser().getUid();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationlayout);
        View headerView = navigationView.getHeaderView(0);
        museremail=(TextView) headerView.findViewById(R.id.userdraweremail);
        musername=(TextView) headerView.findViewById(R.id.userdrawername);
        DocumentReference docRef = fstore.collection("users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        musername.setText(document.getString("fName"));
                        museremail.setText(document.getString("email"));
                        finalUserId=userID;
                        thesize(finalUserId);
                        getgeo(finalUserId);
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
            }
        });

        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout);

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        mSearchBtn= (ImageButton) findViewById(R.id.search_go_btn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this , SearchActivity.class));
            }
        });

    }
    public static void getgeo(String userid)
    {
//.document("CurvSurf").collection("Tree")
        System.out.print("아이디 = " + userid);//성공
        fstore.collection("tree").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> listS = new ArrayList<>();
                        List<Object> listO = new ArrayList<>();

                        Map<String, Object> map = document.getData();
                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                String key=entry.getKey();
                                Object object=entry.getValue();
                                listS.add(key);
                                listO.add(object);
                                System.out.print("키값들 : "+key);
                            }
                        }

                        for (Object s : listO) {
                            Map<String,Object> map2=(Map)s;
                            for(Map.Entry<String,Object> entry:map2.entrySet())
                            {
                                GeoPoint fgeo=null;
                                String keyname=entry.getKey();
                                Object objname=entry.getValue();
                                System.out.print("key값들 : "+keyname+"원소들 : "+objname);
                                if(keyname.equals("treeLocation"))
                                {
                                    fgeo=(GeoPoint)objname;
                                    System.out.print("포인트 과연? "+fgeo);
                                    geolist.add(fgeo);
                                }
                                else if(keyname.equals("treeName"))
                                {
                                    String name=(String)objname;
                                    namelist.add(name);
                                }
                                else if(keyname.equals("treeDBH"))
                                {
                                    String fi=(String)objname;
                                    Double dbh=Double.parseDouble(fi);
                                    dbhlist.add(dbh);
                                }
                                else if(keyname.equals("treeSpecies"))
                                {
                                    String sp=(String)objname;
                                    splist.add(sp);
                                }
                                else if(keyname.equals("treeHeight"))
                                {
                                    if(objname!=null) {
                                        String sp = (String) objname;
                                        Double hei = Double.parseDouble(sp);
                                        helist.add(hei);
                                    }
                                }
                            }
                        }
                        System.out.println("   리스트  "+geolist);
                    }
                }
            }
        });
    }
    public static void thesize(String userid)
    {
        System.out.print(userid);
        fstore.collection("tree").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> listS = new ArrayList<>();
                        Map<String, Object> map = document.getData();
                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                String key=entry.getKey();
                                listS.add(key);
                            }
                            datasize=listS.size();
                        }
                    }
                }
            }
        });
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.Logout: {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this , LoginActivity.class));
                this.finish();
                break;
            }
            case R.id.hello: {
                Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();
                break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class App extends Application {

        private static Context mContext;

        @Override
        public void onCreate() {
            super.onCreate();
            mContext = this;
        }

        public static Context getContext(){
            return mContext;
        }
    }


    public void onResume() {
        super.onResume();
        {
            if (!LocationHelper.hasLocationPermission(this)) {
                LocationHelper.requestLocationPermission(this);
                return;
            }
          //  getgeo(finalUserId);
        }
    }


    @Override
    public void onBackPressed() {
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout);
        if(drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
        else {
            //super.onBackPressed();
            if (System.currentTimeMillis() - lastTimeBackPressed < 2000)
            {
                finish();
                return;
            }
            Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            lastTimeBackPressed = System.currentTimeMillis();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!LocationHelper.hasLocationPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!LocationHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                LocationHelper.launchPermissionSettings(this);
            }
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            finish();
        }
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationlayout);
        navigationView.setNavigationItemSelectedListener(this);
    }
}

