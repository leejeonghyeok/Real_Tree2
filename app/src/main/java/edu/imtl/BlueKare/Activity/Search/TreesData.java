package edu.imtl.BlueKare.Activity.Search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreesData {
    /******************* Map View의 필터 요소 관리 ***********************/


    private List<Trees> tList = new ArrayList<>();

    public TreesData(List<Trees> tList) {
        this.tList = tList;
    }

    public List<Trees> getAllTrees() {
        return tList;
    }

    public void settList(List<Trees> tList) {
        this.tList = tList;
    }

    public List<Trees> getDBHFilteredtrees(List<String> dbh, List<Trees> mList) {
        List<Trees> tempList = new ArrayList<>();
        for (Trees tree : mList) {
            for (String d : dbh) {//d : >10
                if(d.equals("목재"))
                    d = "> 24";//기준

                if (Float.parseFloat(tree.getTreeDbh()) >= Float.parseFloat(d.replace(">",""))) {
                    tempList.add(tree);
                }
            }
        }
        return tempList;
    }

    public List<Trees> getHeightFilteredtrees(List<String> height, List<Trees> mList) {
        List<Trees> tempList = new ArrayList<>();
        for (Trees tree : mList) {
            for (String h : height) {
                if (Float.parseFloat(tree.getTreeHeight()) >= Float.parseFloat(h.replace(">",""))) {
                    tempList.add(tree);
                }
            }
        }
        return tempList;
    }

    public List<Trees> getSpeciesFilteredtrees(List<String> species, List<Trees> mList) {
        List<Trees> tempList = new ArrayList<>();
        for (Trees tree : mList) {
            for (String s : species) {
                if (tree.getTreeSpecies().equalsIgnoreCase(s)) {
                    tempList.add(tree);
                }
            }

        }
        return tempList;
    }

    public List<Trees> getLandMarkFilteredtrees(List<String> landmark, List<Trees> mList) {
        List<Trees> tempList = new ArrayList<>();
        for (Trees tree : mList) {
            for (String s : landmark) {
                if (tree.getTreeNearLandMark().equalsIgnoreCase(s)) {
                    tempList.add(tree);
                }
            }

        }
        return tempList;
    }

    public List<String> getUniqueDBHKeys() {
        List<String> dbhs = new ArrayList<>();
        /*for (Trees tree : tList) {
            int dbh = (int) Math.floor(Float.parseFloat(tree.getTreeDbh()));
            String stringDbh = "> " + dbh;
            if (!dbhs.contains(stringDbh)) {
                dbhs.add(stringDbh);
            }
        }*/
        for( int i=10; i<=90; i+=10 )
        {
            String stringDbh ="> " + i;
            dbhs.add(stringDbh);
        }
        String wood = "목재";
        dbhs.add(wood);

        Collections.sort(dbhs);
        return dbhs;
    }

    public List<String> getUniqueHeightKeys() {
        List<String> heights = new ArrayList<>();
        /*for (Trees tree : tList) {
            int height = (int) Math.floor(Float.parseFloat(tree.getTreeHeight()));
            String stringHeight = "> " + height;
            if (!heights.contains(stringHeight)) {
                heights.add(stringHeight);
            }
        }*/

        List<Integer> tmpheights = new ArrayList<>();
        for( int i=1; i<=19; i+=2 )
        {
            int stringHeight = i;
            tmpheights.add(stringHeight);
        }
        Collections.sort(tmpheights);

        for(int i=0; i<tmpheights.size(); i++ )
        {
            String stringHeight ="> " + tmpheights.get(i);
            heights.add(stringHeight);
        }

        return heights;
    }


    public List<String> getUniqueSpeciesKeys() {
        List<String> species = new ArrayList<>();
        for (Trees tree : tList) {
            if (!species.contains(tree.getTreeSpecies())) {
                species.add(tree.getTreeSpecies());
            }
        }
        Collections.sort(species);
        return species;
    }



    public List<String> getUniqueLandmarkKeys() {
        List<String> landmark = new ArrayList<>();
        for (Trees tree : tList) {
            if (!landmark.contains(tree.getTreeNearLandMark())) {
                landmark.add(tree.getTreeNearLandMark());
            }
        }
        Collections.sort(landmark);
        return landmark;
    }




}
