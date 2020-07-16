/*package org.techtown.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;


import java.util.ArrayList;
import java.util.List;

import static android.location.LocationManager.*;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {

    SupportMapFragment mapFragment;
    GoogleMap map;
    private Marker currentMarker = null;
    Marker trmark= null;
    Marker selectedMarker=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                startLocationService();
            }
        });

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AutoPermissions.Companion.loadAllPermissions(this, 100);

    }



    private void setDefaultLocation() {

        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(false);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = map.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 17);
        map.moveCamera(cameraUpdate);
    }


    private void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            int chk1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int chk2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            Location location = null;
            if (chk1 == PackageManager.PERMISSION_GRANTED && chk2 == PackageManager.PERMISSION_GRANTED) {
                location = manager.getLastKnownLocation(GPS_PROVIDER);

            } else {
                return;
            }


            if (location != null) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

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

            String message = "내 위치 -> Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.d("Map", message);

            showCurrentLocation(latitude, longitude);
            markeron();
            //markerdel();

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


        //Toast.makeText(getApplicationContext(), "실행", Toast.LENGTH_LONG).show();
        String markerTitle = "내위치";
        String markerSnippet = "위치정보가 확인되었습니다.";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(curPoint);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(false);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = map.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPoint, 17);

        map.moveCamera(cameraUpdate);


        /*(Marker m=null)
        {
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    m.remove();
                    return false;
                }
            });
        }

     */
    //}
    /*
    public void markeron(){
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mapoptions=new MarkerOptions();
                mapoptions.title("좌표");
                Double latitude2=point.latitude;
                Double longitude2=point.longitude;
                mapoptions.snippet("나무 좌표 lat : "+latitude2+"\nlong : "+longitude2);
                mapoptions.position(new LatLng(latitude2,longitude2));
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.treeicon);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
                mapoptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                map.animateCamera(CameraUpdateFactory.newLatLng(point));
                trmark=map.addMarker(mapoptions);
            }
        });
    }

    /*private void changeSelectedMarker(Marker marker) {
        // 선택한 마커 표시
        if (marker != null) {
            selectedMarker = addMarker(marker, true);
            marker.remove();
        }


    }*/


    /*public void markerdel(){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                MarkerOptions mapoptions=new MarkerOptions();
                Double latitude2=point.latitude;
                Double longitude2=point.latitude;

                if(new LatLng(latitude2,longitude2) !=null)
                {

                    ma.remove(ma.size()-1);
                    i=i-1;
                }
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
    }


    @Override
    public void onDenied(int i, String[] strings) {
    }

    @Override
    public void onGranted(int i, String[] strings) {
        Toast.makeText(this, "permissions granted : " + strings.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }
}
*/