package edu.imtl.BlueKare.Activity;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import edu.imtl.BlueKare.R;

import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.tData;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment3_fragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment3_fragment2 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ArrayList<PieEntry> visitor=new ArrayList<>();

    public fragment3_fragment2() {
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
    public static fragment3_fragment2 newInstance(String param1, String param2) {
        fragment3_fragment2 fragment = new fragment3_fragment2();
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
        View v=inflater.inflate(R.layout.fragment_fragment3_fragment2, container, false);
        PieChart pieChart = v.findViewById(R.id.pieChart);
        int a1=0;
        int a2=0;
        int a3=0;
        int a4=0;
        int a5=0;
        int a6=0;
        int a7=0;
        int a8=0;
        int a9=0;
        int a0=0;
        //세중 splist -> tList
        //System.out.print("이름들:"+splist);
        for (int i = 0; i</*datasize*/tData.getAllTrees().size(); i++)
        {
            String string= tData.getAllTrees().get(i).getTreeSpecies();
            //String string = splist.get(i);
            if(string.equals("은행"))
            {
                a1=a1+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("이팝"))
            {
                a2=a2+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("배롱"))
            {
                a3=a3+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("무궁화"))
            {
                a4=a4+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("느티"))
            {
                a5=a5+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("벚"))
            {
                a6=a6+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("단풍"))
            {
                a7=a7+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("백합"))
            {
                a8=a8+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("메타"))
            {
                a9=a9+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
            else if(string.equals("기타"))
            {
                a0=a0+1;
                visitor.clear();
                makech(a1,a2,a3,a4,a5,a6,a7,a8,a9,a0);
            }
        }
        PieDataSet pieDataSet=new PieDataSet(visitor,"TREE");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(18f);
        pieDataSet.setValueFormatter(new MyValueFormatter());
        PieData pieData=new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.animate();
        pieChart.setCenterText("수종");
        pieChart.setCenterTextSize(20f);

        return v;
    }
    private void makech(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8, int a9, int a0) {
        if(a1!=0)visitor.add(new PieEntry(a1,"은행"));
        if(a2!=0)visitor.add(new PieEntry(a2,"이팝"));
        if(a3!=0)visitor.add(new PieEntry(a3,"배롱"));
        if(a4!=0)visitor.add(new PieEntry(a4,"무궁화"));
        if(a5!=0)visitor.add(new PieEntry(a5,"느티"));
        if(a6!=0)visitor.add(new PieEntry(a6,"벚"));
        if(a7!=0)visitor.add(new PieEntry(a7,"단풍"));
        if(a8!=0)visitor.add(new PieEntry(a8,"백합"));
        if(a9!=0)visitor.add(new PieEntry(a9,"메타"));
        if(a0!=0)visitor.add(new PieEntry(a0,"기타"));
    }
}