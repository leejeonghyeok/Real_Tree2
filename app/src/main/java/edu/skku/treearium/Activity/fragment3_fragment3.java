package edu.skku.treearium.Activity;

import android.content.Context;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.core.utilities.Tree;

import java.util.ArrayList;

import edu.skku.treearium.Activity.MainPackage.data.model.TreeData;
import edu.skku.treearium.R;

import static edu.skku.treearium.Activity.MainPackage.fragment2_test.datasize;
import static edu.skku.treearium.Activity.MainPackage.fragment2_test.dbhlist;
import static edu.skku.treearium.Activity.MainPackage.fragment2_test.helist;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment3_fragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment3_fragment3 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    ArrayList<BarEntry> visitor=new ArrayList<>();
    int fise=0;
    int seni=0;
    int niel=0;
    int elth=0;
    int thth=0;

    public fragment3_fragment3() {
    }

    public static fragment3_fragment3 newInstance(String param1, String param2) {
        fragment3_fragment3 fragment = new fragment3_fragment3();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_fragment3_fragment3, container, false);

        BarChart barChart = v.findViewById(R.id.barChart2);

        int fo=0;
        int fi=0;
        int si=0;
        int se=0;
        int ei=0;

        for(int i=0;i<datasize;i++)
        {
            double geti=helist.get(i);

            if(geti<6)
            {
                fo=fo+1;
                visitor.clear();
                makech(fo,fi,si,se,ei);
            }
            else if(geti>=6&&geti<7)
            {
                fi=fi+1;
                visitor.clear();
                makech(fo,fi,si,se,ei);
            }
            else if(geti>=7&&geti<8)
            {
                si=si+1;
                visitor.clear();
                makech(fo,fi,si,se,ei);
            }
            else if(geti>=8&&geti<9)
            {
                se=se+1;
                visitor.clear();
                makech(fo,fi,si,se,ei);
            }
            else if(geti>=9)
            {
                ei=ei+1;
                visitor.clear();
                makech(fo,fi,si,se,ei);
            }
        }

        BarDataSet barDataSet=new BarDataSet(visitor,"Height");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);
        BarData barData=new BarData(barDataSet);

        barChart.setFitBars(true);
        barChart.setData(barData);
        barChart.getDescription().setText("TREE HEIGHT");
        barChart.animateY(2000);
        return v;
    }
    public void makech(int a,int b, int c, int d,int e)
    {
        visitor.add(new BarEntry(5,a));
        visitor.add(new BarEntry(6,b));
        visitor.add(new BarEntry(7,c));
        visitor.add(new BarEntry(8,d));
        visitor.add(new BarEntry(9,e));
    }

}