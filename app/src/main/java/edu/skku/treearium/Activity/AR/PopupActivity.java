package edu.skku.treearium.Activity.AR;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import edu.skku.treearium.R;

public class PopupActivity extends AppCompatActivity {

    Button okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_popup);

//        LottieAnimationView lottie = (LottieAnimationView)findViewById(R.id.lottie);
//        lottie.setAnimation("successful.json");
//        lottie.loop(true);
//        lottie.playAnimation();

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