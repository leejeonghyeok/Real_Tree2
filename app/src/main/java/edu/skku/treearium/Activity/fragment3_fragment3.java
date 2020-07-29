package edu.skku.treearium.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import edu.skku.treearium.Activity.MainPackage.data.model.TreeData;
import edu.skku.treearium.R;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment3_fragment3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment3_fragment2.
     */
    // TODO: Rename and change types and number of parameters
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

        PieChart pieChart = v.findViewById(R.id.pieChart);
        ArrayList<PieEntry> visitor=new ArrayList<>();
        ArrayList<TreeData> treeData=new ArrayList<>();

        for (int i = 0; i<treeData.size(); i++)
        {

        }
        visitor.add(new PieEntry(420,"2019"));
        visitor.add(new PieEntry(450,"2018"));
        visitor.add(new PieEntry(501,"2017"));
        visitor.add(new PieEntry(301,"2016"));
        visitor.add(new PieEntry(218,"2015"));

        PieDataSet pieDataSet=new PieDataSet(visitor,"TREE");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(16f);
        PieData pieData=new PieData(pieDataSet);


        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.animate();
        pieChart.setCenterText("Visitor");
        return v;
    }
}