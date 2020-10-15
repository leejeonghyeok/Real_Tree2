package edu.skku.treearium.Activity.MainPackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.skku.treearium.Activity.MyMainFabFragment;
import edu.skku.treearium.Activity.Search.MyFabFragment;
import edu.skku.treearium.Activity.Search.Trees;
import edu.skku.treearium.Activity.Search.TreesData;
import edu.skku.treearium.R;
import edu.skku.treearium.Utils.TreesContent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static edu.skku.treearium.Activity.MainActivity.datasize;
import static edu.skku.treearium.Activity.MainActivity.finalUserId;
import static edu.skku.treearium.Activity.MainActivity.fstore;
//import static edu.skku.treearium.Activity.MainActivity.thesize;
//import static edu.skku.treearium.Activity.MainActivity.geolist;

//implements OnMarkerClickListener
public class fragment2_test extends Fragment implements GoogleMap.OnMarkerClickListener, AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener{
    GoogleMap map;
    int len;
    int il = 0;
    int l;
    View finalview;
    DrawerLayout drawerLayout2;
    public static List<GeoPoint> geolist = new ArrayList<>();
    public static List<Double> dbhlist = new ArrayList<>();
    public static List<String> namelist = new ArrayList<>();
    public static List<String> splist = new ArrayList<>();
    public static List<Double> helist = new ArrayList<>();
    List<Marker> mMarkerList = new ArrayList<>();
    List<LatLng> mPointList = new ArrayList<>();
    static Marker currentMarker = null;


    //세중추가------------------------------------------

    MyMainFabFragment dialogFrag;

    public static TreesData tData;
    public static List<Trees> tList = new ArrayList<>();
    public static ArrayMap<String, List<String>> applied_filters = new ArrayMap<>();

    //--------------------------------------------------

    public OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                    TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                    title.setText(marker.getTitle());

                    TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                    //TextView snippet2=((TextView)infoWindow.findViewById(R.id.snippet2));
                    snippet.setText(marker.getSnippet());
                    //snippet2.setText(marker.getSnippet());
                    return null;
                }
            });
        }
    };



    //Button mybtn=(Button)findViewById(R.id.mybtn);

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
            }

            GPSListener gpsListener = new GPSListener();

//
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
            maketree();

            if(il==0)
            {
                //LatLng point=new LatLng(geolist.get(len-1).getLatitude(),geolist.get(len-1).getLongitude());
                if(tList.size()!=0) {
                    LatLng point = new LatLng(tList.get(tList.size() - 1).getTreeLocation().getLatitude(), tList.get(tList.size() - 1).getTreeLocation().getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, 17);
                    map.moveCamera(cameraUpdate);
                    //map.animateCamera(CameraUpdateFactory.newLatLng(point));//마지막에만 카메라 이동하도록 고쳐야함
                    il = il + 1;
                }
            }
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
    public boolean btnbool(View view,LatLng curPoint)
    {
        ImageButton imageButton=(ImageButton)view.findViewById(R.id.mybtn);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view1)
            {
                view1=view;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 17));
                if(l==1)
                {
                    Toast.makeText(getContext(), "지급부터 내 위치 자동 찾기 기능을 종료합니다", Toast.LENGTH_LONG).show();
                    l=0;

                }
                else if(l==0)
                {
                    Toast.makeText(getContext(), "지금부터 내 위치 자동 찾기를 시작합니다", Toast.LENGTH_LONG).show();
                    l=1;
                }
            }
        });
        if(l==1)
        {
            System.out.print("트루다");
            return true;
        }
        else
        {
            System.out.print("퍼스다");
            return false;
        }
    }


    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);
        //      map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 17));
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

        l=0;
        if(btnbool(finalview, curPoint))
        {
            System.out.print("bool실행");
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPoint, 17);
            map.moveCamera(cameraUpdate);
        }
    }

    public void markeron2(LatLng point,String name,String sp, Double dbh){
        Context context=this.getContext();
        MarkerOptions mapoptions=new MarkerOptions();
        mapoptions.title("이름 : "+name);
        Double latitude2=point.latitude;
        Double longitude2=point.longitude;
        mapoptions.snippet("DBH : "+dbh + ", 학종 : " + sp);
        mapoptions.position(new LatLng(latitude2,longitude2));
        mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.tree_icon_foreground));
        System.out.println(latitude2);
        //map.animateCamera(CameraUpdateFactory.newLatLng(point));//마지막에만 카메라 이동하도록 고쳐야함
        Marker tr2mark=map.addMarker(mapoptions);
        map.setOnMarkerClickListener(this);
        //if(onMarkerClick(tr2mark))

        //map.setOnMarkerClickListener(this);
        mMarkerList.add(tr2mark);
        mPointList.add(point);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(
                getActivity(),R.style.BottomSheetDialogTheme
        );
        View bottomSheetView= LayoutInflater.from(getContext()).inflate(
                R.layout.bottom_sheet_background,(LinearLayout)getView().findViewById(R.id.bottomSheetContainer2));
        //bottomSheetView.findViewById(R.id.bottomname).
        EditText edit4 = bottomSheetView.findViewById(R.id.bottomname1);
        //String edit4Text = String.format("%.2f m", sp);
        //edit4.setText(sp);
        bottomSheetView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"정보 창을 닫습니다",Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
        return true;
    }


    public void maketree()
    {
        System.out.println("들어가서 팅기나?");
        /*if(geolist!=null&&geolist.size()<=datasize)
        {
            len=0;
            for(int i=0;i<geolist.size();i++)
            {
                double newla = geolist.get(i).getLatitude();
                double newlo=geolist.get(i).getLongitude();
                LatLng geoLatlng = new LatLng(newla,newlo);
                String name=namelist.get(i);
                String sp=splist.get(i);
                Double dbh=dbhlist.get(i);
                System.out.println(i+"i번쨰 쫘표!!"+geolist.get(i));
                System.out.println(i+"i번쨰 latlng!!"+geoLatlng);
                System.out.println(i+"i번쨰 name!!"+name);
                System.out.println(i+"i번쨰 sp!!"+sp);
                System.out.println(i+"i번쨰 dbh!!"+dbh);
                //markeron2(geoLatlng,name,sp,dbh);
                len=len+1;
            }
        }
        else if(geolist.size()==0)
        {

        }*/


        if(tList!=null && tList.size() != 0)
        {
            for(int i=0; i<tList.size(); i++){
                Trees tree = tList.get(i);
                LatLng geoLatlng = new LatLng(tree.getTreeLocation().getLatitude(),tree.getTreeLocation().getLongitude());
                markeron2(geoLatlng,tree.getTreeName(),tree.getTreeSpecies(),Double.parseDouble(tree.getTreeDbh()));
            }

        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        finalview= inflater.inflate(R.layout.fragment_fragment2_test, container, false);
        //세중-------------------------------------------------
        final FloatingActionButton fab = (FloatingActionButton) finalview.findViewById(R.id.filterBtnMap);
        dialogFrag = MyMainFabFragment.newInstance();
        dialogFrag.setParentFab(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFrag.setCallbacks(fragment2_test.this);
                dialogFrag.show(getActivity().getSupportFragmentManager(), dialogFrag.getTag());

            }
        });

        tData = TreesContent.getTrees();
        tList = TreesContent.getTrees().getAllTrees();

        //세중-------------------------------------------------

        return finalview;

    }


    //세중추가------------------------------------------
    @Override
    public void onResult(Object result) {
        Log.d("k9res", "onResult: " + result.toString());
        if (result.toString().equalsIgnoreCase("swiped_down")) {
            //do something or nothing
        } else {
            if (result != null) {
                ArrayMap<String, List<String>> applied_filters = (ArrayMap<String, List<String>>) result;
                if (applied_filters.size() != 0) {
                    List<Trees> filteredList = tData.getAllTrees();
                    System.out.println(filteredList);
                    //iterate over arraymap
                    for (Map.Entry<String, List<String>> entry : applied_filters.entrySet()) {
                        Log.d("k9res", "entry.key: " + entry.getKey());
                        switch (entry.getKey()) {
                            case "dbh":
                                filteredList = tData.getDBHFilteredtrees(entry.getValue(), filteredList);
                                //System.out.println("dbh로 필터 리스트 채움");
                                break;
                            case "height":
                                filteredList = tData.getHeightFilteredtrees(entry.getValue(), filteredList);
                                //System.out.println("height로 필터 리스트 채움");
                                break;
                            case "species":
                                filteredList = tData.getSpeciesFilteredtrees(entry.getValue(), filteredList);
                                //System.out.println("종으로 필터 리스트 채움");
                                break;
                            case "landmark":
                                filteredList = tData.getLandMarkFilteredtrees(entry.getValue(), filteredList);
                                break;

                        }
                    }
                    Log.d("k9res", "new size: " + filteredList.size());
                    tList.clear(); //비우고
                    tList.addAll(filteredList);// 필터해서
                    System.out.println("필터 후 tList" + tList);
                    map.clear();
                    maketree();


                } else {
                    tList.addAll(tData.getAllTrees()); //다 넣고
                    map.clear();
                    maketree();
                }
            }
            //handle result
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (dialogFrag.isAdded()) {
            dialogFrag.dismiss();
            dialogFrag.show(getActivity().getSupportFragmentManager(), dialogFrag.getTag());
        }

    }
    //세중추가-------------------------------------------------

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
            //System.out.print("LIST : "+geolist);
            startLocationService();
            //System.out.print("2LIST : "+geolist);
        }
    }

    public ArrayMap<String, List<String>> getApplied_filters() {
        return applied_filters;
    }

    public TreesData gettData() {
        return tData;
    }


    @Override
    public void onOpenAnimationStart() {
        Log.d("aah_animation", "onOpenAnimationStart: ");
    }

    @Override
    public void onOpenAnimationEnd() {
        Log.d("aah_animation", "onOpenAnimationEnd: ");

    }

    @Override
    public void onCloseAnimationStart() {
        Log.d("aah_animation", "onCloseAnimationStart: ");

    }

    @Override
    public void onCloseAnimationEnd() {
        Log.d("aah_animation", "onCloseAnimationEnd: ");

    }
}