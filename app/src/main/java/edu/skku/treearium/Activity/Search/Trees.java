package edu.skku.treearium.Activity.Search;

import com.google.firebase.firestore.GeoPoint;

public class Trees {
    public String treeName, treeSpecies, time, treePerson, treeNearLandMark, treeDbh, treeHeight;;
    public GeoPoint treeLocation;

    public Trees(){

    }

    public Trees(String treeName, String treeSpecies, String time, String treePerson, String treeNearLandMark, String treeDbh, String treeHeight, String treeMillis, GeoPoint treeLocation) {
        this.treeName = treeName;
        this.treeSpecies = treeSpecies;
        this.time = time;
        this.treePerson = treePerson;
        this.treeNearLandMark = treeNearLandMark;
        this.treeDbh = treeDbh;
        this.treeHeight = treeHeight;
        this.treeLocation = treeLocation;
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

    public String getTreePerson() {
        return treePerson;
    }

    public void setTreePerson(String treePerson) {
        this.treePerson = treePerson;
    }

    public String getTreeNearLandMark() {
        return treeNearLandMark;
    }

    public void setTreeNearLandMark(String treeNearLandMark) {
        this.treeNearLandMark = treeNearLandMark;
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
}
