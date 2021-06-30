package edu.imtl.BlueKare.Activity.AR;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

import edu.imtl.BlueKare.R;

public class BottomSheet {
  /*================= Buttom Sheet Stuffs =================*/
  private View view;
  private TextView alertText;
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


  /*==================== Inner Variable ===================*/
  String[] items = new String[]{"벚", "이팝", "배롱", "은행", "무궁화", "백합", "단풍", "메타", "소", "느티"};
  /*=======================================================*/


  // setting team name
  void setTeamName(String teamName) {
//		if (!TextUtils.isEmpty(this.teamName.getText())) return;
    this.teamName.setText(teamName);
  }

  // setting dbh
  public void setDbhSize(String dbh) {
    dbhSize.setText(dbh);
    // handling exceptions
    if (!TextUtils.isEmpty(dbhSize.getText())) {
      dbhMeasureButton.setText("다시");
    }
  }

  // set tree type
  public void setTreeType(ArActivity activity, String treeName) {
    boolean found = false;
    int parameterIDX = -1;

    // to certain tree name to go on the top of the list
    for (int i = 0; i < items.length; i++) {
      if (treeName.equals(items[i])) {
        found = true;
        parameterIDX = i;
      }
    }
    if (found) {
      String tmp = items[parameterIDX];
      items[parameterIDX] = items[0];
      items[0] = tmp;
    } else {
      int guitarIDX = items.length - 1;
      String tmp = items[guitarIDX];
      items[guitarIDX] = items[0];
      items[0] = tmp;
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, items);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    treeType.setAdapter(adapter);
  }

  // 1 : dbh, 2 : height
  // set alert text
  public void setAlertText(int doneType) {
    if (doneType == 1) {
      if (TextUtils.isEmpty(treeHeight.getText())) {
        alertText.setText("흉고직경을 측정했어요!");
      } else {
        alertText.setText("측정이 완료되었어요!");
      }
    } else if (doneType == 2) {
      if (TextUtils.isEmpty(dbhSize.getText())) {
        alertText.setText("높이를 측정했어요!");
      } else {
        alertText.setText("측정이 완료되었어요!");
      }
    }
  }

  // set height
  public void setTreeHeight(String height) {
    treeHeight.setText(height);
    // handling exceptions
    if (!TextUtils.isEmpty(treeHeight.getText())) {
      heightMeasureButton.setText("다시");
    }
  }

  // set landmark
  public void setTreeLandMark(String position) {
    treeLandMark.setText(position);
  }

  // initialiating stuffs
  public void init(ArActivity activity, BottomSheetDialog dialog) {
    this.dialog = dialog;

    LayoutInflater inflater = LayoutInflater.from(activity.getApplicationContext());
    view = inflater.inflate(
            R.layout.layout_bottom_sheet,
            (LinearLayout) activity.findViewById(R.id.bottomSheetContainer)
    );

    alertText = view.findViewById(R.id.treeAlertText);
    teamName = view.findViewById(R.id.bottomname);
    dbhSize = view.findViewById(R.id.bottomdbh);
    treeLandMark = view.findViewById(R.id.bottomnearbylm);
    treeHeight = view.findViewById(R.id.bottomheight);
    treeType = view.findViewById(R.id.bottomspecies);
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
      if (locationA == null) {
        System.out.println("in bottomsheet confirmbutton, locationA empty!");
        return;
      }
      if (TextUtils.isEmpty(dbhSize.getText()) || TextUtils.isEmpty(treeHeight.getText())) {
        System.out.println("in bottomsheet confirmbutton, dbhsize or treeheight empty!");
        return;
      }

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

      //세중 잠시 주석
      //v1.getContext().startActivity(new Intent(v1.getContext(), MainActivity.class));
    });
  }

  // set buttons with reset

  private void setDbhMeasureButton(ArActivity activity) {
    dbhMeasureButton.setOnClickListener(l -> {
      dialog.dismiss();
      activity.currentMode = Mode.isFindingCylinder;
      activity.toggle.check(R.id.dbhButton);
      activity.checkToggleType(1);
      activity.resetArActivity(true);
    });
  }

  private void setHeightMeasureButton(ArActivity activity) {
    heightMeasureButton.setOnClickListener(l -> {
      dialog.dismiss();
      activity.currentMode = Mode.isFindingHeight;
      if (activity.heightForTheFirstTime) {
        activity.popupActivity.startDialog(R.layout.activity_popup2);
        activity.heightForTheFirstTime = false;
      }
      activity.toggle.check(R.id.heightButton);
      activity.checkToggleType(2);
      activity.resetArActivity(false);
    });
  }

  public void show() {
    dialog.setContentView(view);
    dialog.show();
  }
}
