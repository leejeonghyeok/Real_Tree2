package edu.imtl.BlueKare.Activity.MainPackage;

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

import edu.imtl.BlueKare.Activity.MyMainFabFragment;
import edu.imtl.BlueKare.Activity.Search.Trees;
import edu.imtl.BlueKare.Activity.Search.TreesData;
import edu.imtl.BlueKare.R;
import edu.imtl.BlueKare.Utils.TreesContent;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
//import static edu.skku.treearium.Activity.MainActivity.thesize;
//import static edu.skku.treearium.Activity.MainActivity.geolist;

//implements OnMarkerClickListener
//GoogleMap.OnMarkerClickListener,<--이거 오류나면 imp해야함

/******************* Map View class ***********************/
public class fragment2_test extends Fragment implements AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener {
    GoogleMap map;
    int len;
    int il = 0;
    int l;
    int Mlens = 0;
    int ff = 0;
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


    MyMainFabFragment dialogFrag;

    public static TreesData tData;
    public static List<Trees> tList = new ArrayList<>();
    public static ArrayMap<String, List<String>> applied_filters = new ArrayMap<>();


    //make map
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


            //Run when marker is clicked
            //Information is uploaded to the bottom sheet
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    marker.getPosition();
                    System.out.println("marker postion" + marker.getPosition());
                    System.out.println("get snippet" + marker.getSnippet());

                    int i;
                    for (i = 0; i < tList.size() - 1; i++) {
                        if (marker.getSnippet().equals(tList.get(i).getTime())) {
                            System.out.println("이힝" + i);
                            System.out.println("이건 몇번째 i일까" + i);
                            break;
                        }
                    }

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                            getActivity(), R.style.BottomSheetDialogTheme
                    );
                    View bottomSheetView = LayoutInflater.from(getContext()).inflate(
                            R.layout.bottom_sheet_background, (LinearLayout) getView().findViewById(R.id.bottomSheetContainer2));


                    EditText sp1 = bottomSheetView.findViewById(R.id.setsp1);
                    sp1.setText(tList.get(i).getTreeSpecies());


                    EditText he1 = bottomSheetView.findViewById(R.id.sethe2);

                    String tmp = String.format("%.2f", Float.parseFloat(tList.get(i).getTreeHeight()));
                    he1.setText(tmp + " m");


                    EditText dbh1 = bottomSheetView.findViewById(R.id.setdbh3);
                    tmp = String.format("%.2f", Float.parseFloat(tList.get(i).getTreeDbh()));
                    dbh1.setText(tmp + " cm");

                    EditText ung1 = bottomSheetView.findViewById(R.id.setung4);
                    ung1.setText("못구함");

                    EditText gung1 = bottomSheetView.findViewById(R.id.setgung5);
                    if (Double.parseDouble(tList.get(i).getTreeDbh()) < 6) {
                        gung1.setText("치수");
                    } else if (Double.parseDouble(tList.get(i).getTreeDbh()) < 16 && Double.parseDouble(tList.get(i).getTreeDbh()) > 6) {
                        gung1.setText("소경목");
                    } else if (Double.parseDouble(tList.get(i).getTreeDbh()) > 16 && Double.parseDouble(tList.get(i).getTreeDbh()) < 29) {
                        gung1.setText("중경목");
                    } else if (Double.parseDouble(tList.get(i).getTreeDbh()) >= 29) {
                        gung1.setText("대경목");
                    }


                    bottomSheetView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Toast.makeText(getContext(),"정보 창을 닫습니다",Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                    return true;
                }
            });


        }
    };


    private void startLocationService() {
        Context context = this.getContext();
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


    //GPS 사용
    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            map.clear();
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            LatLng onLop = new LatLng(latitude, longitude);

            String message = "내 위치 -> Latitude : " + onLop.latitude + "\nLongitude:" + onLop.longitude;
            Log.d("Map", message);

            showCurrentLocation(onLop.latitude, onLop.longitude);
            maketree();

            if (il == 0) {
                //LatLng point=new LatLng(geolist.get(len-1).getLatitude(),geolist.get(len-1).getLongitude());
                if (tList.size() != 0) {
                    LatLng point = new LatLng(tList.get(tList.size() - 1).getTreeLocation().getLatitude(), tList.get(tList.size() - 1).getTreeLocation().getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, 17);
                    map.moveCamera(cameraUpdate);
                    il = il + 1;
                }
            }
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

    //Tree Information Class for CO2 Calculator
    class WoodForCo2 {
        public boolean isShrub;
        //교목 or 관목
        public boolean isneedleLeaf;
        //활엽수 or 침엽수
        public int number;
        public float averDBH;

        public boolean isShrub() {
            return isShrub;
        }

        public void setShrub(boolean shrub) {
            isShrub = shrub;
        }

        public boolean isIsneedleLeaf() {
            return isneedleLeaf;
        }

        public void setIsneedleLeaf(boolean isneedleLeaf) {
            this.isneedleLeaf = isneedleLeaf;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public float getAverDBH() {
            return averDBH;
        }

        public void setAverDBH(float averDBH) {
            this.averDBH = averDBH;
        }

        public WoodForCo2(boolean isShrub, boolean isneedleLeaf, int number, float averDBH) {
            this.isShrub = isShrub;
            this.isneedleLeaf = isneedleLeaf;
            this.number = number;
            this.averDBH = averDBH;
        }
    }

    //Calculate the tree's board feet using dbh and height
    public int boardFeetCalculator(float dbh, float height) {
        float in = (float) (dbh * 0.393701);//cm to inch
        float ft = (float) (height * 3.28084); //meter to ft

        int temp = (int) (in / 2);
        int indbh = temp * 2; // 2, 4, 6, 8 ~~~
        System.out.println("dbh = " + dbh);
        System.out.println("temp = " + temp);
        System.out.println("indbh = " + indbh);

        temp = (int) ft / 8;
        int ftheight = temp;// 내가 쓸 값의 두배로 나옴. 인덱스로 쓸거라 상관 없음

        /***********
         * 12 inch -> 30cm
         * 42 inch -> 106cm
         * 1/2 -> 8ft
         * 1 -> 16ft
         * 16ft -> 4.8 m
         */

        //[dbh][height]
        //12~42 inch
        //1/2 16-Foot Logs -> 4 16-Foot Logs
        int treeTableDoyle[][] = {
                {20, 30, 40, 50, 60},
                {30, 50, 70, 80, 90, 100},
                {40, 70, 100, 120, 140, 160, 180, 190},
                {60, 100, 130, 160, 200, 220, 240, 260},
                {80, 130, 180, 220, 260, 330, 320, 360},
                {100, 170, 230, 280, 340, 380, 420, 460},
                {130, 220, 290, 360, 430, 490, 540, 600},
                {160, 260, 360, 440, 520, 590, 660, 740},
                {190, 320, 430, 520, 620, 710, 800, 880},
                {230, 380, 510, 630, 740, 840, 940, 1040},
                {270, 440, 590, 730, 860, 990, 1120, 1220},
                {300, 510, 680, 850, 1000, 1140, 1300, 1440},
                {350, 580, 780, 970, 1140, 1310, 1480, 1640},
                {390, 660, 880, 1100, 1290, 1480, 1680, 1860},
                {430, 740, 990, 1230, 1450, 1660, 1880, 2080},
                {470, 830, 1100, 1370, 1620, 1860, 2100, 2320}
        };


        try {
            int a = (indbh - 12) / 2;
            int b = ftheight / 2 - 1;
            System.out.println("a b is : " + a + " " + b);
            System.out.println("[0][2]  imagine 40 : " + treeTableDoyle[0][2]);
            System.out.println("table : " + treeTableDoyle[a][b]);
            return treeTableDoyle[a][b];
        } catch (Exception e) {
            System.out.println("out of Index! return 0");
            return 0;
        }
        //Using Doyle rule.
    }


    //Statistics calculation and display.
    public void onstat(View view) {
        ImageButton imageButton2 = (ImageButton) view.findViewById(R.id.statisticalbtn);
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v = view;
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        getActivity(), R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(getContext()).inflate(
                        R.layout.layout_bottom_map_show, (LinearLayout) getView().findViewById(R.id.bottomSheetContainer));


                TextView mapbottomname = bottomSheetView.findViewById(R.id.mapbottomname);//지역
                TextView mapbottomlocation = bottomSheetView.findViewById(R.id.mapbottomlocation);//위도경도

                TextView mapbottomtreenum = bottomSheetView.findViewById(R.id.mapbottomtreenum);//몇

                TextView mapbottomtreespec1 = bottomSheetView.findViewById(R.id.mapbottomtreespec1);//1순위 퍼센트
                TextView mapbottomtreespec2 = bottomSheetView.findViewById(R.id.mapbottomtreespec2);//2순위 퍼센트
                TextView mapbottomtreespec3 = bottomSheetView.findViewById(R.id.mapbottomtreespec3);//3순위 퍼센트

                TextView mapbottomtreespecask1 = bottomSheetView.findViewById(R.id.mapbottomtreespecask1);//1순위 나무 이름
                TextView mapbottomtreespecask2 = bottomSheetView.findViewById(R.id.mapbottomtreespecask2);//2순위 나무 이름
                TextView mapbottomtreespecask3 = bottomSheetView.findViewById(R.id.mapbottomtreespecask3);//2순위 나무 이름

                TextView absorption = bottomSheetView.findViewById(R.id.absorption);
                TextView storage = bottomSheetView.findViewById(R.id.storage);

                LinearLayout firsttree = bottomSheetView.findViewById(R.id.firsttree);//1순위 리스트
                LinearLayout secondtree = bottomSheetView.findViewById(R.id.secondtree);//1순위 리스트
                LinearLayout thirdtree = bottomSheetView.findViewById(R.id.thirdtree);//1순위 리스트


                TextView boardFeet = bottomSheetView.findViewById(R.id.boardFeet);


                mapbottomtreenum.setText(tList.size() + "그루");
                if (tList.size() != 0) {

                    String[] items = new String[]{"은행", "이팝", "배롱", "무궁화", "느티", "벚", "단풍", "백합", "메타", "소나무"};
                    int[] values = new int[items.length];

                    for (int j = 0; j < tList.size(); j++) {
                        for (int i = 0; i < items.length; i++) {
                            if (tList.get(j).getTreeSpecies().equals(items[i])) {
                                values[i]++;
                                break;
                            }
                        }
                    }


                    //Count the top 3 trees
                    int maxIndex = 0;
                    for (int i = 0; i < items.length; i++) {
                        if (values[maxIndex] < values[i]) {
                            maxIndex = i;
                        }
                    }

                    int numberOfTree = values[maxIndex];
                    float per = values[maxIndex] / (float) tList.size();
                    per = per * 100;
                    mapbottomtreespec1.setText(numberOfTree + "그루" + "(" + (int) per + "%" + ")");
                    mapbottomtreespecask1.setText(items[maxIndex]);

                    if ((int) per == 0) {
                        firsttree.setVisibility(View.GONE);
                    }

                    int secondMaxIndex = 0;
                    for (int i = 0; i < items.length; i++) {

                        if (i != maxIndex) {
                            if (values[secondMaxIndex] < values[i]) {
                                secondMaxIndex = i;
                            }
                        }
                    }

                    numberOfTree = values[secondMaxIndex];
                    float secondper = values[secondMaxIndex] / (float) tList.size();
                    secondper = secondper * 100;
                    mapbottomtreespec2.setText(numberOfTree + "그루" + "(" + (int) secondper + "%" + ")");
                    mapbottomtreespecask2.setText(items[secondMaxIndex]);

                    if ((int) secondper == 0) {
                        secondtree.setVisibility(View.GONE);
                    }


                    int thirdMaxIndex = 0;
                    for (int i = 0; i < items.length; i++) {
                        if (i != maxIndex && i != secondMaxIndex) {
                            if (values[thirdMaxIndex] < values[i])
                                thirdMaxIndex = i;
                        }
                    }

                    numberOfTree = values[thirdMaxIndex];
                    float thirdper = values[thirdMaxIndex] / (float) tList.size();
                    thirdper = thirdper * 100;
                    mapbottomtreespecask3.setText(items[thirdMaxIndex]);
                    mapbottomtreespec3.setText(numberOfTree + "그루" + "(" + (int) thirdper + "%" + ")");

                    if ((int) thirdper == 0) {
                        thirdtree.setVisibility(View.GONE);
                    }

                } else {
                    mapbottomtreespec1.setText("");
                    mapbottomtreespec2.setText("");
                    mapbottomtreespec3.setText("");
                }

                //Calculation of small hardwood, medium hardwood, and large hardwood
                if (tList.size() != 0) {
                    int s = 0;
                    int m = 0;
                    int l = 0;
                    for (int i = 0; i < tList.size(); i++) {
                        if (Double.parseDouble(tList.get(i).getTreeDbh()) < 16) {
                            s++;
                        } else if (Double.parseDouble(tList.get(i).getTreeDbh()) < 29 && Double.parseDouble(tList.get(i).getTreeDbh()) > 16) {
                            m++;
                        } else {
                            l++;
                        }
                    }
                    TextView mapbottomtreedbh1 = bottomSheetView.findViewById(R.id.mapbottomtreedbh1);//소경목 6~16
                    TextView mapbottomtreedbh2 = bottomSheetView.findViewById(R.id.mapbottomtreedbh2);//중경목 16~29
                    TextView mapbottomtreedbh3 = bottomSheetView.findViewById(R.id.mapbottomtreedbh3);//대경목 29~

                    int tmp = (int) (((float) s / (float) tList.size()) * 100);
                    mapbottomtreedbh1.setText(s + "그루" + "(" + tmp + "%" + ")");
                    tmp = (int) (((float) m / (float) tList.size()) * 100);
                    mapbottomtreedbh2.setText(m + "그루" + "(" + tmp + "%" + ")");
                    tmp = (int) (((float) l / (float) tList.size()) * 100);
                    mapbottomtreedbh3.setText(l + "그루" + "(" + tmp + "%" + ")");
                }


                //Classification by tree height
                if (tList.size() != 0) {
                    int sh = 0;
                    int mh = 0;
                    int lh = 0;
                    for (int i = 0; i < tList.size(); i++) {
                        if (Double.parseDouble(tList.get(i).getTreeHeight()) < 5) {
                            sh++;
                        } else if (Double.parseDouble(tList.get(i).getTreeHeight()) < 10 && Double.parseDouble(tList.get(i).getTreeHeight()) > 5) {
                            mh++;
                        } else {
                            lh++;
                        }
                    }
                    TextView mapbottomtreeh1 = bottomSheetView.findViewById(R.id.mapbottomtreeh1);//10m 이상
                    TextView mapbottomtreeh2 = bottomSheetView.findViewById(R.id.mapbottomtreeh2);//5m 이상
                    TextView mapbottomtreeh3 = bottomSheetView.findViewById(R.id.mapbottomtreeh3);//~5m

                    int tmp = (int) (((float) lh / (float) tList.size()) * 100);
                    mapbottomtreeh1.setText(lh + "그루" + "(" + tmp + "%" + ")");
                    tmp = (int) (((float) mh / (float) tList.size()) * 100);
                    mapbottomtreeh2.setText(mh + "그루" + "(" + tmp + "%" + ")");
                    tmp = (int) (((float) sh / (float) tList.size()) * 100);
                    mapbottomtreeh3.setText(sh + "그루" + "(" + tmp + "%" + ")");


                    /***********CO2 계산 **************/
                    //String[] items = new String[]{"은행", "이팝", "배롱", "무궁화", "느티", "벚", "단풍", "백합", "메타", "소나무"};
                    //8m 이상 자라는 나무 교목

                    WoodForCo2[] wood = new WoodForCo2[4];
                    wood[0] = new WoodForCo2(true, true, 0, 0);//관목 침엽수
                    wood[1] = new WoodForCo2(true, false, 0, 0);//관목 활엽수 : 무궁화
                    wood[2] = new WoodForCo2(false, true, 0, 0);//교목 침엽수 : 은행, 메타, 소나무
                    wood[3] = new WoodForCo2(false, false, 0, 0);//교목 활엽수 : 이팝, 배롱, 느티, 벚, 단풍, 백합


                    //교목 침엽수 : 은행, 메타, 소나무
                    //교목 활엽수 : 이팝, 배롱, 느티, 느티, 벚, 단풍, 백합
                    //관목 침엽수
                    //관목 활엽수 : 무궁화


                    for (int i = 0; i < tList.size(); i++) {
                        String woodName = tList.get(i).getTreeSpecies();
                        System.out.println("나무 이름 : " + woodName);
                        switch (woodName) {
                            case "은행":
                            case "메타":
                            case "소나무":
                                wood[2].number++;
                                wood[2].averDBH = wood[2].averDBH + Float.parseFloat(tList.get(i).getTreeDbh());
                                break;
                            case "이팝":
                            case "배롱":
                            case "느티":
                            case "벚":
                            case "단풍":
                            case "백합":
                                wood[3].number++;
                                wood[3].averDBH = wood[3].averDBH + Float.parseFloat(tList.get(i).getTreeDbh());
                                break;
                            case "무궁화":
                                wood[1].number++;
                                wood[1].averDBH = wood[1].averDBH + Float.parseFloat(tList.get(i).getTreeDbh());
                            default:
                                System.out.println("그 외 나무");
                        }
                    }

                    for (int i = 0; i < 4; i++) {
                        if (wood[i].number != 0) {
                            wood[i].averDBH = wood[i].averDBH / (float) wood[i].number;
                        }
                    }

                    float S = 0;
                    float A = 0;


                    float storageShrubNeedle = 0;//저장량 관목침엽수
                    float storageShrubBroad = 0;//저장량 관목활엽수
                    float storageTreeNeedle = 0;//저장량 교목침엽수
                    float storageTreeBroad = 0;//저장량 교목활엽수

                    float absorbShrubNeedle = 0;//흡수량 관목침엽수
                    float absorbShrubBroad = 0;//흡수량 관목활엽수
                    float absorbTreeNeedle = 0;//흡수량 교목침엽수
                    float absorbTreeBroad = 0;//흡수량 교목활엽수

                    if (wood[0].averDBH != 0) {//저장량 관목침엽수
                        storageShrubNeedle = (float) (-1.8276 + 2.1892 * Math.log(wood[0].averDBH));
                        storageShrubNeedle = (float) Math.pow(2, storageShrubNeedle);
                        S = S + storageShrubNeedle * wood[0].number;
                    }

                    if (wood[1].averDBH != 0) {//저장량 관목활엽수
                        storageShrubBroad = (float) (-1.7148 + 1.9494 * Math.log(wood[1].averDBH));
                        storageShrubBroad = (float) Math.pow(2, storageShrubBroad);
                        S = S + storageShrubBroad * wood[1].number;
                    }

                    if (wood[2].averDBH != 0)//교목 침엽수 : 은행, 메타, 소나무
                    {
                        storageTreeNeedle = (float) (-1.047 + 2.1436 * Math.log(wood[2].averDBH));
                        storageTreeNeedle = (float) Math.pow(2, storageTreeNeedle);
                        S = S + storageTreeNeedle * wood[2].number;
                    }

                    if (wood[3].averDBH != 0)//교목 활엽수 : 이팝, 배롱, 느티, 벚, 단풍, 백합
                    {
                        storageTreeBroad = (float) (-1.3582 + 2.4595 * Math.log(wood[3].averDBH));
                        storageTreeBroad = (float) Math.pow(2, storageTreeBroad);
                        S = S + storageTreeBroad * wood[3].number;
                    }

                    if (S < 1000) {
                        storage.setText(String.format("%.0f", S) + " kgCO2");
                    } else if (S > 1000) {
                        S = S / 1000.0f;
                        storage.setText(String.format("%.1f", S) + " tCO2");
                    }


                    if (wood[0].averDBH != 0) {//흡수량 관목침엽수
                        absorbShrubNeedle = (float) (-2.8689 + 1.3350 * Math.log(wood[0].averDBH));
                        absorbShrubNeedle = (float) Math.pow(2, absorbShrubNeedle);
                        A = A + absorbShrubNeedle * wood[0].number;
                    }

                    if (wood[1].averDBH != 0) {//흡수량 관목활엽수
                        absorbShrubBroad = (float) (-3.4025 + 1.5823 * Math.log(wood[1].averDBH));
                        absorbShrubBroad = (float) Math.pow(2, absorbShrubBroad);
                        A = A + absorbShrubBroad * wood[1].number;
                    }

                    if (wood[2].averDBH != 0)//교목 침엽수 : 은행, 메타, 소나무
                    {
                        absorbTreeNeedle = (float) (-2.7714 + 0.9714 * wood[2].averDBH - 0.0225 * Math.pow(wood[2].averDBH, 2));
                        A = A + absorbTreeNeedle * wood[2].number;

                    }

                    if (wood[3].averDBH != 0)//교목 활엽수 : 이팝, 배롱, 느티, 벚, 단풍, 백합
                    {
                        absorbTreeBroad = (float) (-4.2136 + 1.9006 * wood[3].averDBH - 0.0068 * Math.pow(wood[3].averDBH, 2));
                        A = A + absorbTreeBroad * wood[3].number;
                    }


                    if (A < 1000)
                        absorption.setText(String.format("%.0f", A) + " kgCO2/y");
                    else if (A > 1000) {
                        A = A / 1000.0f;
                        absorption.setText(String.format("%.1f", A) + " tCO2/y");
                    }


                    int numberOfLumber = 0;
                    for (int i = 0; i < tList.size(); i++) {
                        numberOfLumber = numberOfLumber + boardFeetCalculator(Float.parseFloat(tList.get(i).treeDbh), Float.parseFloat(tList.get(i).treeHeight));
                    }

                    boardFeet.setText(numberOfLumber + " board feet");

                }


                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }

    //Implement a button to automatically find my location
    public boolean btnbool(View view, LatLng curPoint) {
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.mybtn);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                view1 = view;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 17));
                if (l == 1) {
                    Toast.makeText(getContext(), "지금부터 내 위치 자동 찾기 기능을 종료합니다", Toast.LENGTH_LONG).show();
                    l = 0;

                } else if (l == 0) {
                    Toast.makeText(getContext(), "지금부터 내 위치 자동 찾기를 시작합니다", Toast.LENGTH_LONG).show();
                    l = 1;
                }
            }
        });
        if (l == 1) {
            //System.out.print("트루다");
            return true;
        } else {
            //System.out.print("퍼스다");
            return false;
        }
    }

    //Go to my current location
    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);

        String markerTitle = "내위치";
        String markerSnippet = "위치정보가 확인되었습니다." + latitude + "\n" + longitude;

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(curPoint);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(false);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = map.addMarker(markerOptions);

        l = 0;
        if (btnbool(finalview, curPoint)) {
            //System.out.print("bool실행");
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPoint, 17);
            map.moveCamera(cameraUpdate);
        }
    }

    //Create a marker
    public void markeron2(LatLng point, String name, String sp, Double dbh, String time) {
        MarkerOptions mapoptions = new MarkerOptions();
        mapoptions.title("이름 : " + name);
        Double latitude2 = point.latitude;
        Double longitude2 = point.longitude;
        mapoptions.snippet(time);
        mapoptions.position(new LatLng(latitude2, longitude2));

        //지도 나무 아이콘
        if (sp.equals("은행")) {
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ginkgo2_icon_foreground));
        } else if (sp.equals("단풍")) {
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.maple2_icon_foreground));
        } else if (sp.equals("벚")) {
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.sakura4_icon_foreground));
        } else if (sp.equals("메타")) {
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.meta_icon_foreground));
        } else {
            mapoptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.tree_icon_foreground));
        }
        //System.out.println(latitude2);
        //map.animateCamera(CameraUpdateFactory.newLatLng(point));//마지막에만 카메라 이동하도록 고쳐야함

        Marker tr2mark = map.addMarker(mapoptions);
        //map.setOnMarkerClickListener(this);
        //onMarkerClick(tr2mark);
        //onMarkerClick(tr2mark);

        //map.setOnMarkerClickListener(this);

        mMarkerList.add(tr2mark);
        //Mlens=Mlens+1;
        //mPointList.add(point);
        //selectM(tr2mark);
    }


    //Mark on the map
    public void maketree() {
        System.out.println("들어가서 팅기나?");


        if (tList != null && tList.size() != 0) {
            for (int i = 0; i < tList.size(); i++) {
                Trees tree = tList.get(i);
                LatLng geoLatlng = new LatLng(tree.getTreeLocation().getLatitude(), tree.getTreeLocation().getLongitude());
                markeron2(geoLatlng, tree.getTreeName(), tree.getTreeSpecies(), Double.parseDouble(tree.getTreeDbh()), tree.getTime());
            }
            //ff=1;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        finalview = inflater.inflate(R.layout.fragment_fragment2_test, container, false);
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
        Context context = this.getContext();
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

            if (!checkForGpsProvider()) {
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