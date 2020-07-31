package edu.skku.treearium.Activity.AR;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import edu.skku.treearium.R;
/*
public class PopupActivity extends AppCompatActivity {

    Button okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_popup);

        okBtn = (Button)findViewById(R.id.okbtn);
        okBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickOkBtn(v);
            }
        });
    }

    public void clickOkBtn(View v){
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        if(e.getAction() == MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
    @Override // 뒤로가기 방지
    public void onBackPressed(){
        return;
    }
}
 */
class PopupActivity {
    Activity activity;
    Button dialogButton;
    PopupActivity(Activity myactivity){
        activity = myactivity;
    }
    void startDialog(){
        LayoutInflater factory = LayoutInflater.from(activity);
        final View deleteDialogView = factory.inflate(R.layout.activity_popup, null);
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