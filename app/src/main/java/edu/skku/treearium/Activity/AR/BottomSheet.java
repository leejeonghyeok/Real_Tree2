package edu.skku.treearium.Activity.AR;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

import edu.skku.treearium.R;

public class BottomSheet {
  /*================= Buttom Sheet Stuffs =================*/
  private View view;
  private EditText teamName;
  private EditText dbhSize;
  private EditText treeLandMark;
  private EditText treeHeight;
  private Spinner treeType;
  private BottomSheetDialog dialog;
  private Button dbhMeasureButton;
  private Button heightMeasureButton;
  private Button confirmButton;
  /*=======================================================*/


  void setTeamName(String teamName) {
//		if (!TextUtils.isEmpty(this.teamName.getText())) return;
    this.teamName.setText(teamName);
  }

  public void setDbhSize(String dbh) {
    dbhSize.setText(dbh);
  }

  public void setTreeHeight(String height) {
    treeHeight.setText(height);
  }

  public void setTreeLandMark(String position) {
    treeLandMark.setText(position);
  }

  public void init(ArActivity activity, BottomSheetDialog dialog) {
    this.dialog = dialog;

    LayoutInflater inflater = LayoutInflater.from(activity.getApplicationContext());
    view = inflater.inflate(
            R.layout.layout_bottom_sheet,
            (LinearLayout) activity.findViewById(R.id.bottomSheetContainer)
    );

    teamName = view.findViewById(R.id.bottomname);
    dbhSize = view.findViewById(R.id.bottomdbh);
    treeLandMark = view.findViewById(R.id.bottomnearbylm);
    treeHeight = view.findViewById(R.id.bottomheight);
    treeType = view.findViewById(R.id.bottomspecies);
    String[] items = new String[]{"은행", "이팝", "배롱", "무궁화", "느티", "벚", "단풍", "백합", "메타", "기타"};
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, items);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    treeType.setAdapter(adapter);

    dbhMeasureButton = view.findViewById(R.id.bottomdbhmeasure);
    setDbhMeasureButton(activity);

    heightMeasureButton = view.findViewById(R.id.bottomheightmeasure);
    setHeightMeasureButton(activity);
  }

  public void setConfirmButton(FirebaseFirestore fstore, GeoPoint locationA) {
    view.findViewById(R.id.confirmBtn).setOnClickListener(v1 -> {
      Map<String, Object> tree = new HashMap<>();
      Long tsLong = System.currentTimeMillis() / 1000;
      String ts = (tsLong).toString();

//			aractivity에서 username 선언만 되고 한번도 안쓰길래 그냥 일단 주석해둠
//			tree.put("treePerson", username);

//			aractivity 보니 이게 teamname treename 같이 쓰길래 ??
      tree.put("treeName", teamName.getText().toString());
      tree.put("treeSpecies", treeType.getSelectedItem().toString());
//            if(mappedRecognitions.get(0).getDetectedClass() != -1)
//              tree.put("treeSpecies", "Ginkgo");
      tree.put("treeDBH", dbhSize.getText().toString());
      tree.put("treeHeight", treeHeight.getText().toString());
      tree.put("treeLocation", locationA);
      tree.put("treeNearLandMark", treeLandMark.getText().toString());
      tree.put("treeMillis", ts);
      DocumentReference treearray = fstore.collection("Team").document(teamName.getText().toString()).collection("Tree").document(ts);
      treearray.set(tree).addOnSuccessListener(aVoid -> {
      });
      dialog.dismiss();
    });
  }

  private void setDbhMeasureButton(ArActivity activity) {
    dbhMeasureButton.setOnClickListener(l -> {
      dialog.dismiss();
      activity.currentMode = Mode.isFindingCylinder;
      activity.resetArActivity(true);
    });
  }

  private void setHeightMeasureButton(ArActivity activity) {
    heightMeasureButton.setOnClickListener(l -> {
      dialog.dismiss();
      activity.currentMode = Mode.isFindingHeight;
      activity.resetArActivity(false);
    });
  }

  public void show() {
    dialog.setContentView(view);
    dialog.show();
  }
}
