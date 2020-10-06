package edu.skku.treearium.Utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.skku.treearium.Activity.Search.Trees;


public class TreesContent {



    public static ArrayList<Trees> getTrees()
    {

        ArrayList<Trees> trees = new ArrayList<>();

        //firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        String userID = mFirebaseAuth.getCurrentUser().getUid();

        db.collection("tree").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> listS = new ArrayList<>();
                        List<Object> listO = new ArrayList<>();
                        Map<String, Object> map = document.getData();
                        List<String> timeS = new ArrayList<>();

                        for(String mapkey : map.keySet())
                        {
                            timeS.add(mapkey);
                        }

                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                String key=entry.getKey();
                                Object object=entry.getValue();
                                listS.add(key);
                                listO.add(object);
                            }

                            int i=0;

                            for (Object s : listO) {
                                Map<String, Object> map2 = (Map) s;
                                Trees model = new Trees();
                                for (Map.Entry<String, Object> entry : map2.entrySet()) {
                                    String keyname = entry.getKey();
                                    Object objname = entry.getValue();
                                    if(keyname !=null && objname !=null) {
                                        if (keyname.equals("treeLocation")) {
                                            model.setTreeLocation((GeoPoint) objname);
                                        } else if (keyname.equals("treeName")) {
                                            model.setTreeName((String)objname);
                                        } else if (keyname.equals("treeDBH")) {
                                            model.setTreeDbh((String) objname);
                                        } else if (keyname.equals("treeSpecies")) {
                                            model.setTreeSpecies((String) objname);
                                        } else if (keyname.equals("treeHeight")) {
                                            model.setTreeHeight((String) objname);
                                        }
                                    }
                                }
                                model.setTime(timeS.get(i++));
                                trees.add(model);
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //called when there is any error while retireving

            }
        });

        return trees;
    }
}
