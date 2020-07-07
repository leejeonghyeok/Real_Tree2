package com.example.Real_Tree.Activity.AR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.Real_Tree.R;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;
//import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ARActivity extends AppCompatActivity implements GLSurfaceView.Renderer{

    private boolean mUserRequestedInstall = false;
    private boolean mViewportChanged = false;
    private int mViewportWidth = -1;
    private int mViewportHeight = -1;

    //카메라 권한
    private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.CAMERA};
    private final int PERMISSION_REQUEST_CODE = 0; // PROTECTION_NORMAL

    private GLSurfaceView glView;
    private BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private Session session;
    private Frame frame;

    private BlockingQueue<MotionEvent> tapQueue = new ArrayBlockingQueue<>(16);
    private long LastTapTime = 0;
    //private ArrayList<float[]> cubePoses = new ArrayList<>(20);

    private float[] viewMatrix = new float[16];
    private float[] projMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private float[] vpMatrix = new float[16];

    private int angle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glView = findViewById(R.id.glView);
        glView.setPreserveEGLContextOnPause(true);
        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(8,8,8,8,16,0);
        glView.setRenderer(this);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        for(String permission : REQUIRED_PERMISSSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSSIONS, PERMISSION_REQUEST_CODE);
            }
        }

        glView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Toast.makeText(getApplicationContext(), "Touched : " + cubePoses.size(), Toast.LENGTH_SHORT).show();
                tapQueue.offer(event);
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(session != null){
            glView.onPause();
            session.pause();
        }
    }
    @Override // annotation: 상위 클래스에서 정의됨
    protected void onResume() {
        super.onResume(); //

        if(session == null){
            try{
                switch(ArCoreApk.getInstance().requestInstall(this,!mUserRequestedInstall)){
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = true;
                        return;
                    case INSTALLED:
                        break;
                }
                session = new Session(this);

                Config config = new Config(session);
                config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
                config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
                config.setFocusMode(Config.FocusMode.AUTO);
                session.configure(config);

            }catch (Exception e){
                Log.d("ULTRA", e.getMessage());
                return;
            }
        }

        try{
            session.resume();
        }catch (CameraNotAvailableException e){
            e.printStackTrace();
            session = null;
            finish();
        }

        glView.onResume();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        try{
            backgroundRenderer.createOnGlThread(this);
            //cubeRenderer = new CubeRenderer();
        }catch (IOException e){
            e.getMessage();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        if(session == null){
            return;
        }
        if(mViewportChanged){
            int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
            ///////
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewportChanged = false;
        }

        try{
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            frame = session.update();
            backgroundRenderer.draw(frame);

//            Camera camera = frame.getCamera();
//            if(camera.getTrackingState() == TrackingState.TRACKING){
//                camera.getViewMatrix(viewMatrix, 0); //카메라 위치 추측
//                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f); //Projection
//                Matrix.multiplyMM(vpMatrix, 0, projMatrix,0,viewMatrix,0);
//
//                Matrix.setIdentityM(modelMatrix, 0);
//                Matrix.rotateM(modelMatrix, 0, angle,1,1,1);
//                angle++;
//
//                for(float[] t : cubePoses){
//                    float[] temp = new float[16];
//                    Matrix.setIdentityM(temp, 0);
//                    Matrix.translateM(temp, 0, t[0], t[1], t[2]);
//
//                    Matrix.multiplyMM(temp,0, temp,0, modelMatrix, 0);
//
//                    Matrix.multiplyMM(temp,0,vpMatrix,0, temp,0);
//                    //cubeRenderer.draw(temp);
//                }
//            }
//            handleTap();
        }catch (CameraNotAvailableException e){
            finish();
        }
    }

    public void handleTap(){
        MotionEvent tap = tapQueue.poll();
        if(tap == null || LastTapTime == tap.getEventTime()) return;
        /*
        if(cubePoses.size() == 20){
            cubePoses.remove(0);
        }

        cubePoses.add(new float[]{
                (float) (frame.getCamera().getPose().tx() + (float)frame.getCamera().getPose().getZAxis()[0] * -0.3),
                (float) (frame.getCamera().getPose().ty() + (float)frame.getCamera().getPose().getZAxis()[1] * -0.3),
                (float) (frame.getCamera().getPose().tz() + (float)frame.getCamera().getPose().getZAxis()[2] * -0.3),
        });
        LastTapTime = tap.getEventTime();
        */
    }
}
