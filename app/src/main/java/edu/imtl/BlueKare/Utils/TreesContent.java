package edu.imtl.BlueKare.Utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.imtl.BlueKare.Activity.Search.Trees;
import edu.imtl.BlueKare.Activity.Search.TreesData;


/******************* Firebase에서 데이터를 받아오기 위한 class ***********************/

public class TreesContent {

    public static TreesData getTrees()
    {

        List<Trees> tList = new ArrayList<>();

        //firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        String userID = mFirebaseAuth.getCurrentUser().getUid();

        //옛날꺼
        /*db.collection("tree").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
        });*/

        //return trees;


        db.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //document.getString("Team") -> document.getString("fName")
                        db.collection("Team").document(document.getString("Team")).collection("Tree").orderBy("treeMillis", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                QuerySnapshot query = task.getResult();
                                List<DocumentSnapshot> documentSnapshotList = query.getDocuments();
                                for(DocumentSnapshot object : documentSnapshotList) {
                                    Trees model = new Trees();
                                    model.setTreePerson(document.getString("fName"));
                                    for(String key : object.getData().keySet()){
                                        if (key.equals("treeLocation")) {
                                            model.setTreeLocation((GeoPoint) object.getData().get(key));
                                        } else if (key.equals("treeName")) {
                                            model.setTreeName((String)object.getData().get(key));
                                        } else if (key.equals("treeDBH")) {
                                            model.setTreeDbh((String) object.getData().get(key));
                                        } else if (key.equals("treeSpecies")) {
                                            if(((String)object.getData().get(key)).equals("기타"))
                                            {
                                                model.setTreeSpecies("소나무");
                                            }else {
                                                model.setTreeSpecies((String) object.getData().get(key));
                                            }
                                        } else if (key.equals("treeHeight")) {
                                            model.setTreeHeight((String) object.getData().get(key));
                                        } else if (key.equals("treeMillis")) {
                                            model.setTime((String) object.getData().get(key));
                                        } else if (key.equals("treeNearLandMark")) {
                                            model.setTreeNearLandMark((String) object.getData().get(key));
                                        } else if (key.equals("treePerson")) {
                                            model.setTreePerson((String) object.getData().get(key));
                                        }
                                    }tList.add(model);

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //called when there is any error while retireving
                            }
                        });
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //called when there is any error while retireving
            }
        });

        Collections.sort(tList);

        return new TreesData(tList);
    }

    public static void updateFirebase(String memo, String treeID){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        String userID = mFirebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        db.collection("Team")
                                .document(document.getString("Team"))
                                .collection("Tree")
                                .document(treeID)
                                .update("treeName", memo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //called when updated successfully
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //called when there is an error
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //called when there is an error
            }
        });
    }
}
