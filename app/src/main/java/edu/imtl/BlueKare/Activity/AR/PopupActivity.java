package edu.imtl.BlueKare.Activity.AR;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import edu.imtl.BlueKare.R;

// ARActivity 시작 시 팝업창이 뜨게 함.
class PopupActivity {
  Activity activity;
  Button dialogButton;

  PopupActivity(Activity myactivity) {
    activity = myactivity;
  }

  void startDialog(int resource) {
    LayoutInflater factory = LayoutInflater.from(activity);
    final View deleteDialogView = factory.inflate(resource, null);
    final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setView(deleteDialogView);
    deleteDialogView.findViewById(R.id.okbtn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        alertDialog.dismiss();
      }
    });
    alertDialog.show();
  }
}