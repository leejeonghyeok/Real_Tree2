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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.util.ArrayList;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class fragment2_test extends Fragment{
    GoogleMap map;
    private DatabaseReference mDatabase;

    List<Marker> mMarkerList = new ArrayList<>();
    List<LatLng> mPointList = new ArrayList<>();
    //Marker trmark=null;
    public OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map=googleMap;
        }

    };



    static Marker currentMarker=null;


    private void startLocationService() {

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

            //onLop.latitude=location.getLatitude();
            //onLop.longitude=location.getLongitude();
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            LatLng onLop=new LatLng(latitude,longitude);

            String message = "내 위치 -> Latitude : " + onLop.latitude + "\nLongitude:" + onLop.longitude;
            Log.d("Map", message);

            showCurrentLocation(onLop.latitude, onLop.longitude);
            markeron();

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
        currentMarker=map.addMarker(markerOptions);


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPoint, 17);

        map.moveCamera(cameraUpdate);
    }
    private void markeron(){
        Context context=this.getContext();
        //Marker trmark=null;
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mapoptions=new MarkerOptions();
                mapoptions.title("~~나무");
                Double latitude2=point.latitude;
                Double longitude2=point.longitude;

                mapoptions.snippet("DBH : ~~~ cm" );
                mapoptions.position(new LatLng(latitude2,longitude2));
                BitmapDrawable bitmapdraw=(BitmapDrawable) context.getResources().getDrawable(R.drawable.treeicon);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
                mapoptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                map.animateCamera(CameraUpdateFactory.newLatLng(point));
                Marker trmark=map.addMarker(mapoptions);
                mMarkerList.add(trmark);
                mPointList.add(point);

            }
        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point2) {
                int i;
                for (i = 0; i < mPointList.size(); i++)
                {
                    if(mPointList.get(i)==point2)
                    {
                        mMarkerList.get(i).remove();
                        mPointList.remove(i);
                        mMarkerList.remove(i);
                    }
                }
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //map=GoogleMap;

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
            startLocationService();
        }
    }
}
