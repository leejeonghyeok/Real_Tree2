package edu.skku.treearium.Activity.Search;

import com.google.firebase.firestore.GeoPoint;

public class Trees {
    public String treeName, treeSpecies, time;
    public String treeDbh, treeHeight;
    public GeoPoint treeLocation;
    public boolean isSelect;


    public Trees(){

    }

    public Trees(String treeName, String treeSpecies, String time, String treeDbh, String treeHeight, GeoPoint treeLocation, boolean isSelect) {
        this.treeName = treeName;
        this.treeSpecies = treeSpecies;
        this.time = time;
        this.treeDbh = treeDbh;
        this.treeHeight = treeHeight;
        this.treeLocation = treeLocation;
        this.isSelect = isSelect;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public String getTreeSpecies() {
        return treeSpecies;
    }

    public void setTreeSpecies(String treeSpecies) {
        this.treeSpecies = treeSpecies;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTreeDbh() {
        return treeDbh;
    }

    public void setTreeDbh(String treeDbh) {
        this.treeDbh = treeDbh;
    }

    public String getTreeHeight() {
        return treeHeight;
    }

    public void setTreeHeight(String treeHeight) {
        this.treeHeight = treeHeight;
    }

    public GeoPoint getTreeLocation() {
        return treeLocation;
    }

    public void setTreeLocation(GeoPoint treeLocation) {
        this.treeLocation = treeLocation;
    }

    public boolean getSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }
}
