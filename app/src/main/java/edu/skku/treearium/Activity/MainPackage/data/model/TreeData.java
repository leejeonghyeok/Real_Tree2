package edu.skku.treearium.Activity.MainPackage.data.model;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeData {

    public String name;
    public float DBH;
    public LatLng point;
    public Time time;
    public float height;

    List<TreeData> Ltr=new ArrayList<>();
    Map<String, Float> Dtreemap= new HashMap<String, Float>();
    Map<String, Float> Htreemap= new HashMap<String, Float>();
    /*public TreeData(String name,) {
    }*/

    public void selectTree(TreeData treeData)
    {
        Ltr.add(treeData);
    }

    public void CDBH(TreeData tr){

        if(Dtreemap.containsKey(tr.name))
        {
            float prevalue= Dtreemap.get(tr.name);
            Dtreemap.put(tr.name,(prevalue+tr.DBH)/2);
        }
        else
            Dtreemap.put(tr.name,tr.DBH);
    }

    public void CHEI(TreeData tr)
    {
        if(Htreemap.containsKey(tr.name))
        {
            float preheight=Htreemap.get(tr.name);
            Htreemap.put(tr.name,preheight+(tr.height)/2);
        }
        else
            Htreemap.put(tr.name,tr.height);

    }
}
