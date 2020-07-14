package com.example.Real_Tree.Activity.AR;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.Real_Tree.R;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import Renderer.BackgroundRenderer;
import Renderer.PointCloudRenderer;

public class ARActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private boolean mUserRequestedInstall = false;
    private boolean mViewportChanged = false;
    private int mViewportWidth = -1;
    private int mViewportHeight = -1;

    //카메라 권한 관련
    private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.CAMERA};
    private final int PERMISSION_REQUEST_CODE = 0; // PROTECTION_NORMAL

    private GLSurfaceView glView;

    BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    private Session session;
    private Frame frame;

    private Button btn_record;
    private boolean recording = false;
    private int renderingMode = 0; // 0:start, 1:recording, 2:recorded 3:pickPoint

    private float[] viewMatrix = new float[16];
    private float[] projMatrix = new float[16];
    private float[] vpMatrix = new float[16];

    //private float[] ray = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        glView = findViewById(R.id.glView);
        glView.setPreserveEGLContextOnPause(true);
        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(8,8,8,8,16,0);
        glView.setRenderer(this);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        glView.queueEvent(new Runnable() {
            @Override
            public void run() {

            }
        });

        btn_record = findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = !recording;
                if(recording) {
                    renderingMode = 1;
                    Toast.makeText(getApplicationContext(), "collecting points....", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(renderingMode == 1) pointCloudRenderer.filterPoints();
                    renderingMode = 2;
                    Toast.makeText(getApplicationContext(), "collecting finished!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        for(String permission : REQUIRED_PERMISSSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSSIONS, PERMISSION_REQUEST_CODE);
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();

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
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        try{
            backgroundRenderer.createOnGlThread(this);
            pointCloudRenderer.createOnGlThread(this);
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
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
        }
        try{
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            frame = session.update();
            Camera camera = frame.getCamera();
            backgroundRenderer.draw(frame);

            if(camera.getTrackingState() == TrackingState.TRACKING) {
                camera.getViewMatrix(viewMatrix, 0);
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0);
                pointCloudRenderer.update(frame.acquirePointCloud(), recording);
                Log.d("RMode", String.format("%b %d", recording, renderingMode));
                switch (renderingMode) {
                    case 0:
                        pointCloudRenderer.draw(viewMatrix, projMatrix);
                        break;
                    case 1:
                        pointCloudRenderer.draw_conf(viewMatrix, projMatrix);
                        break;
                    case 2:
                        pointCloudRenderer.draw_final(viewMatrix, projMatrix);
                        Log.d("numPoints", String.valueOf(pointCloudRenderer.finalPointBuffer.remaining()));
                        break;
                    case 3:
                        pointCloudRenderer.draw_seedPoint(vpMatrix);
                }
            }
        }catch (CameraNotAvailableException e){
            finish();
        }
    }
}
