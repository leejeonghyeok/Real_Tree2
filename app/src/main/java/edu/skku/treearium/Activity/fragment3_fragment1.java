package edu.skku.treearium.Activity;

import android.content.Context;
import android.graphics.Color;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment3_fragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment3_fragment1 extends Fragment {

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

    public fragment3_fragment1() {
    }

    public static fragment3_fragment1 newInstance(String param1, String param2) {
        fragment3_fragment1 fragment = new fragment3_fragment1();
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
        View v=inflater.inflate(R.layout.fragment_fragment3_fragment1, container, false);

        BarChart barChart = v.findViewById(R.id.barChart);

        TreeData tr;


        visitor.add(new BarEntry(2014,420));
        visitor.add(new BarEntry(2015,475));
        visitor.add(new BarEntry(2016,508));
        visitor.add(new BarEntry(2017,601));
        visitor.add(new BarEntry(2018,340));

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
    void adden(TreeData tr)
    {
        int he= (int) ((tr.height-100)/200);
        switch(he){
            case(2):
            {
                fise=fise+1;
                visitor.add(new BarEntry(5,fise));
                break;
            }
            case(3):
            {
                seni=seni+1;
                visitor.add(new BarEntry(7,seni));
                break;
            }
            case(4):
            {
                niel=niel+1;
                visitor.add(new BarEntry(9,niel));
                break;
            }
            case(5):
            {
                elth=elth+1;
                visitor.add(new BarEntry(11,elth));
                break;
            }
            case(6):
            {
                thth=thth+1;
                visitor.add((new BarEntry(13,thth)));
                break;
            }
            default:
                break;
        }

    }
}