package edu.skku.treearium.Activity.MainPackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.skku.treearium.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static edu.skku.treearium.Activity.MainActivity.finalUserId;
import static edu.skku.treearium.Activity.MainActivity.fstore;
//import static edu.skku.treearium.Activity.MainActivity.geolist;

public class fragment2_test extends Fragment{
    GoogleMap map;
    public static int datasize;
    public static List<GeoPoint> geolist=new ArrayList<>();
    public static List<Double> dbhlist=new ArrayList<>();
    public static List<String> namelist=new ArrayList<>();
    public static List<String> splist=new ArrayList<>();
    public static List<Double> helist=new ArrayList<>();
    List<Marker> mMarkerList = new ArrayList<>();
    List<LatLng> mPointList = new ArrayList<>();
    static Marker currentMarker=null;
    public OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map=googleMap;
        }
    };

    private void startLocationService() {
        geolist.clear();

        Context context=this.getContext();
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            int chk1 = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
            int chk2 = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

            Location location = null;
            if (chk1 == PackageManager.PERMISSION_GRANTED && chk2 == PackageManager.PERMISSION_GRANTED) {
                location = manager.getLastKnownLocation(GPS_PROVIDER);

            } else {
                return;
            }

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng currentp=new LatLng(latitude,longitude);
            }

            GPSListener gpsListener = new GPSListener();

            long minTime = 1000;
            float minDistance = 0;
            manager.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance, gpsListener);
            manager.requestLocationUpdates(NETWORK_PROVIDER, minTime, minDistance, gpsListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            LatLng onLop=new LatLng(latitude,longitude);

            String message = "내 위치 -> Latitude : " + onLop.latitude + "\nLongitude:" + onLop.longitude;
            Log.d("Map", message);

            showCurrentLocation(onLop.latitude, onLop.longitude);
            getgeo(finalUserId);
            maketree();
            System.out.println("여기까진 실햄"+onLop.latitude+" "+onLop.longitude);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 17));
        String markerTitle = "내위치";
        String markerSnippet = "위치정보가 확인되었습니다."+latitude+"\n"+longitude;

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(curPoint);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(false);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker=map.addMarker(markerOptions);


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPoint, 17);

        map.moveCamera(cameraUpdate);
    }
    public void markeron2(LatLng point,String name,String sp, Double dbh){
        Context context=this.getContext();
        MarkerOptions mapoptions=new MarkerOptions();
        mapoptions.title("이름 : "+name);
        Double latitude2=point.latitude;
        Double longitude2=point.longitude;
        mapoptions.snippet("DBH : "+dbh + ", 학종 : " + sp);
        mapoptions.position(new LatLng(latitude2,longitude2));
        BitmapDrawable bitmapdraw=(BitmapDrawable) context.getResources().getDrawable(R.drawable.treeicon);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
        mapoptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        map.animateCamera(CameraUpdateFactory.newLatLng(point));
        Marker tr2mark=map.addMarker(mapoptions);
        mMarkerList.add(tr2mark);
        mPointList.add(point);
    }

    public static void thesize(String userid)
    {
        System.out.print(userid);
        fstore.collection("tree").document(userid).get().addOnCompleteListener(task -> {
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
        });
    }

    public static void getgeo(String userid)
    {
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
                                System.out.print("끼값들 : "+keyname+"원소들 : "+objname);
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
                                    String sp=(String)objname;
                                    Double hei=Double.parseDouble(sp);
                                    helist.add(hei);
                                }
                            }
                        }
                        System.out.println("   리스트  "+geolist);
                    }
                }
            }
        });
    }


    public void maketree()
    {

        if(geolist!=null&&geolist.size()<=datasize)
        {
            for(int i=0;i<geolist.size();i++)
            {

                double newla = geolist.get(i).getLatitude();
                double newlo=geolist.get(i).getLongitude();
                LatLng geoLatlng = new LatLng(newla,newlo);
                String name=namelist.get(i);
                String sp=splist.get(i);
                Double dbh=dbhlist.get(i);
                System.out.println(i+"i번쨰 원소!!"+geolist.get(i));
                markeron2(geoLatlng,name,sp,dbh);
            }
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_fragment2_test, container, false);
    }
    public boolean checkForGpsProvider() {
        Context context=this.getContext();
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
            if(!checkForGpsProvider())
            {
                Toast.makeText(this.getContext(), "위치 정보를 켜야 합니다.", Toast.LENGTH_LONG).show();
                Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
            geolist.clear();//초기화
            thesize(finalUserId);
            startLocationService();
        }
    }
}
