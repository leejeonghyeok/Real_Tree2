package edu.skku.treearium.Activity.MainPackage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.skku.treearium.Activity.MyMainFabFragment;
import edu.skku.treearium.Activity.Search.Trees;
import edu.skku.treearium.Activity.Search.TreesData;
import edu.skku.treearium.R;
import edu.skku.treearium.Utils.TreesContent;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
//import static edu.skku.treearium.Activity.MainActivity.thesize;
//import static edu.skku.treearium.Activity.MainActivity.geolist;

//implements OnMarkerClickListener
//GoogleMap.OnMarkerClickListener,<--이거 오류나면 imp해야함
public class fragment2_test extends Fragment implements  AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener{
    GoogleMap map;
    int len;
    int il = 0;
    int l;
    int Mlens=0;
    int ff=0;
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
            //selectM();
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

    public void onstat(View view)
    {
        ImageButton imageButton2=(ImageButton)view.findViewById(R.id.statisticalbtn);
        imageButton2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                v=view;
                BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(
                        getActivity(),R.style.BottomSheetDialogTheme
                );
                View bottomSheetView= LayoutInflater.from(getContext()).inflate(
                        R.layout.layout_bottom_map_show,(LinearLayout)getView().findViewById(R.id.bottomSheetContainer));


                TextView mapbottomname = bottomSheetView.findViewById(R.id.mapbottomname);//지역
                TextView mapbottomlocation = bottomSheetView.findViewById(R.id.mapbottomlocation);//위도경도
                TextView mapbottomtreenum = bottomSheetView.findViewById(R.id.mapbottomtreenum);//몇그루
                TextView mapbottomtreespec1 = bottomSheetView.findViewById(R.id.mapbottomtreespec1);//은행
                TextView mapbottomtreespec2 = bottomSheetView.findViewById(R.id.mapbottomtreespec2);//벚꽃
                TextView mapbottomtreespec3 = bottomSheetView.findViewById(R.id.mapbottomtreespec3);//기타
                mapbottomtreenum.setText(tList.size()+"그루");
                if(tList.size()!=0) {

                    int k = 0;
                    for (int i = 0; i < tList.size(); i++) {
                        if(tList.get(i).getTreeSpecies().equals("은행"))
                            k++;
                    }
                    float per = (float) k / (float) tList.size();
                    per = per * 100;
                    mapbottomtreespec1.setText((int)per + "%");


                    k = 0;
                    for (int i = 0; i < tList.size(); i++) {
                        if(tList.get(i).getTreeSpecies().equals("벚꽃"))
                            k++;
                    }
                    per = (float) k / (float) tList.size();
                    per = per * 100;
                    mapbottomtreespec2.setText((int)per + "%");


                    k = 0;
                    for (int i = 0; i < tList.size(); i++) {
                        if(tList.get(i).getTreeSpecies().equals("기타"))
                            k++;
                    }
                    per = (float) k / (float) tList.size();
                    per = per * 100;
                    mapbottomtreespec3.setText((int)per + "%");
                } else {
                    mapbottomtreespec1.setText("");
                    mapbottomtreespec2.setText("");
                    mapbottomtreespec3.setText("");
                }

                if (tList.size()!=0){
                    int s = 0;
                    int m = 0;
                    int l = 0;
                    for (int i=0; i<tList.size(); i++)
                    {
                        if (Double.parseDouble(tList.get(i).getTreeDbh()) < 16)
                        {
                            s++;
                        } else if (Double.parseDouble(tList.get(i).getTreeDbh()) < 29 && Double.parseDouble(tList.get(i).getTreeDbh()) > 16)
                        {
                            m++;
                        } else {
                            l++;
                        }
                    }
                    TextView mapbottomtreedbh1 = bottomSheetView.findViewById(R.id.mapbottomtreedbh1);//소경목 6~16
                    TextView mapbottomtreedbh2 = bottomSheetView.findViewById(R.id.mapbottomtreedbh2);//중경목 16~29
                    TextView mapbottomtreedbh3 = bottomSheetView.findViewById(R.id.mapbottomtreedbh3);//대경목 29~

                    int tmp = (int)((float)s / (float)tList.size())*100;
                    mapbottomtreedbh1.setText(tmp + "%");
                    tmp = (int)((float)m / (float)tList.size())*100;
                    mapbottomtreedbh2.setText(tmp + "%");
                    tmp = (int)((float)l / (float)tList.size())*100;
                    mapbottomtreedbh3.setText(tmp + "%");
                }


                if (tList.size()!=0){
                    int sh = 0;
                    int mh = 0;
                    int lh = 0;
                    for (int i=0; i<tList.size(); i++)
                    {
                        if (Double.parseDouble(tList.get(i).getTreeHeight()) < 5)
                        {
                            sh++;
                        } else if (Double.parseDouble(tList.get(i).getTreeHeight()) < 10 && Double.parseDouble(tList.get(i).getTreeHeight()) > 5)
                        {
                            mh++;
                        } else {
                            lh++;
                        }
                    }
                    TextView mapbottomtreeh1 = bottomSheetView.findViewById(R.id.mapbottomtreeh1);//10m 이상
                    TextView mapbottomtreeh2 = bottomSheetView.findViewById(R.id.mapbottomtreeh2);//5m 이상
                    TextView mapbottomtreeh3 = bottomSheetView.findViewById(R.id.mapbottomtreeh3);//~5m

                    int tmp = (int)((float)lh / (float)tList.size())*100;
                    mapbottomtreeh1.setText(tmp + "%");
                    tmp = (int)((float)mh / (float)tList.size())*100;
                    mapbottomtreeh2.setText(tmp + "%");
                    tmp = (int)((float)sh / (float)tList.size())*100;
                    mapbottomtreeh3.setText(tmp + "%");
                }


                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
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
        MarkerOptions mapoptions=new MarkerOptions();
        mapoptions.title("이름 : "+name);
        Double latitude2=point.latitude;
        Double longitude2=point.longitude;
        mapoptions.snippet("DBH : "+dbh + ", 학종 : " + sp);
        mapoptions.position(new LatLng(latitude2,longitude2));
        //지도 나무 아이콘
        if(sp.equals("은행")){
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ginkgo_icon_foreground));
        }else if(sp.equals("단풍")){
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.maple_launcher_foreground));
        }else {
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.tree_icon_foreground));
        }
        System.out.println(latitude2);
        //map.animateCamera(CameraUpdateFactory.newLatLng(point));//마지막에만 카메라 이동하도록 고쳐야함
        Marker tr2mark=map.addMarker(mapoptions);
        //map.setOnMarkerClickListener(this);
        //onMarkerClick(tr2mark);
        //onMarkerClick(tr2mark);

        //map.setOnMarkerClickListener(this);

        mMarkerList.add(tr2mark);
        Mlens=Mlens+1;
        mPointList.add(point);
        selectM();
    }
    public void selectM()//ㅈ댐 이거 다 똑같이 치수가 나옴
    {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker marker) {

                int i;
                for(i=0;i<Mlens-1;i++)
                {
                    if(mMarkerList.get(i)==marker)
                    {
                        System.out.println("이힝"+i);
                        System.out.println("이건 몇번째 i일까"+i);
                        break;
                    }
                }

                //tList.get(i%tList.size());

                BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(
                        getActivity(),R.style.BottomSheetDialogTheme
                );
                View bottomSheetView= LayoutInflater.from(getContext()).inflate(
                        R.layout.bottom_sheet_background,(LinearLayout)getView().findViewById(R.id.bottomSheetContainer2));


                EditText sp1 = bottomSheetView.findViewById(R.id.setsp1);
                sp1.setText(tList.get(i%tList.size()).getTreeSpecies());

                EditText he1=bottomSheetView.findViewById(R.id.sethe2);
                he1.setText(tList.get(i%tList.size()).getTreeHeight());

                EditText dbh1=bottomSheetView.findViewById(R.id.setdbh3);
                dbh1.setText(tList.get(i%tList.size()).getTreeDbh());

                EditText ung1=bottomSheetView.findViewById(R.id.setung4);
                ung1.setText("못구함");

                EditText gung1=bottomSheetView.findViewById(R.id.setgung5);
                if(Double.parseDouble(tList.get(i%tList.size()).getTreeDbh())<6)
                {
                    gung1.setText("치수");
                }
                else if(Double.parseDouble(tList.get(i%tList.size()).getTreeDbh())<16&&Double.parseDouble(tList.get(i%tList.size()).getTreeDbh())>6)
                {
                    gung1.setText("소경목");
                }
                else if(Double.parseDouble(tList.get(i%tList.size()).getTreeDbh())>16&&Double.parseDouble(tList.get(i%tList.size()).getTreeDbh())<29)
                {
                    gung1.setText("중경목");
                }
                else if(Double.parseDouble(tList.get(i%tList.size()).getTreeDbh())>=29)
                {
                    gung1.setText("대경목");
                }

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
        });
    }
    /*@Override
    public boolean onMarkerClick(Marker marker) {
        int i;
        for(i=0;i<Mlens-1;i++)
        {
            if(mMarkerList.get(i)==marker)
            {
                break;
            }
        }

        tList.get(i);

        BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(
                getActivity(),R.style.BottomSheetDialogTheme
        );
        View bottomSheetView= LayoutInflater.from(getContext()).inflate(
                R.layout.bottom_sheet_background,(LinearLayout)getView().findViewById(R.id.bottomSheetContainer2));


        EditText sp1 = bottomSheetView.findViewById(R.id.setsp1);
        sp1.setText(tList.get(i).getTreeSpecies());

        EditText he1=bottomSheetView.findViewById(R.id.sethe2);
        he1.setText(tList.get(i).getTreeHeight());

        EditText dbh1=bottomSheetView.findViewById(R.id.setdbh3);
        dbh1.setText(tList.get(i).getTreeDbh());

        EditText ung1=bottomSheetView.findViewById(R.id.setung4);
        ung1.setText("못구함");

        EditText gung1=bottomSheetView.findViewById(R.id.setgung5);
        if(Double.parseDouble(tList.get(i).getTreeDbh())<6)
        {
            gung1.setText("치수");
        }
        else if(Double.parseDouble(tList.get(i).getTreeDbh())<16&&Double.parseDouble(tList.get(i).getTreeDbh())>6)
        {
            gung1.setText("소경목");
        }
        else if(Double.parseDouble(tList.get(i).getTreeDbh())>16&&Double.parseDouble(tList.get(i).getTreeDbh())<29)
        {
            gung1.setText("중경목");
        }
        else if(Double.parseDouble(tList.get(i).getTreeDbh())>=29)
        {
            gung1.setText("대경목");
        }
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
    }*/



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
            //ff=1;
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
            onstat(view);

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