/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.skku.treearium.Activity.AR;

import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.skku.treearium.R;
import edu.skku.treearium.Renderer.BackgroundRenderer;
import edu.skku.treearium.Renderer.PointCloudRenderer;
import edu.skku.treearium.Utils.PointCollector;
import edu.skku.treearium.helpers.CameraPermissionHelper;
import edu.skku.treearium.helpers.DisplayRotationHelper;
import edu.skku.treearium.helpers.FullScreenHelper;
import edu.skku.treearium.helpers.TrackingStateHelper;
import edu.skku.treearium.Utils.PickSeed;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Integer.parseInt;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class ArActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
  private static final String TAG = ArActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;

  private boolean installRequested;

  //private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;
  private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);

  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
  private Session session;
  private Frame frame;

  private PointCollector collector = null;
  private boolean isRecording = false;
  private Button recButton = null;
  private Button popup = null;
  //private float dbh = 10;

  private boolean isStaticView = false;
  private float[] ray = null;
  private static final String REQUEST_URL = "https://developers.curvsurf.com/FindSurface/plane";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);
    recButton = (Button)findViewById(R.id.recButton);
    popup = (Button)findViewById(R.id.popup);
    surfaceView = (GLSurfaceView)findViewById(R.id.surfaceview);
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    Intent intent = new Intent(ArActivity.this, PopupActivity.class);
    startActivityForResult(intent, 1);

    installRequested = false;

    recButton.setOnClickListener(v -> {
      isRecording = !isRecording;
      if(isRecording){
        collector = new PointCollector();
        recButton.setText("Stop");
        isStaticView = false;
      } else {
        (new Thread(new Runnable() {
          @Override
          public void run() {
            if (ArActivity.this.collector != null) {
              final FloatBuffer points = ArActivity.this.collector.filterPoints();
              ArActivity.this.surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                  pointCloudRenderer.update(points);
                  isStaticView = true;
                }
              });
              ArActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  recButton.setText("Recording");
                  recButton.setClickable(true);
                }
              });
            }
          }
        })).start();
      }
    });

    popup.setOnClickListener(v -> {
      Intent intent12 = new Intent(ArActivity.this, PopupActivity.class);
      startActivityForResult(intent12, 1);
    });

    surfaceView.setOnTouchListener((v, event) ->{
      if(collector != null && collector.filterPoints != null) {
        // ray 생성
        Camera camera = frame.getCamera();
        ray = camera.getPose().getZAxis(); // by unit
        ray[0] = -ray[0];
        ray[1] = -ray[1];
        ray[2] = -ray[2];

        // camera location
        float[] rayOrigin = camera.getPose().getTranslation();

        int pickIndex = PickSeed.pickPoint(collector.filterPoints, ray, rayOrigin);
        if(pickIndex >= 0 && !Thread.currentThread().isInterrupted()) {
          (new Thread(() -> {
            RequestForm rf = new RequestForm();

            rf.setPointBufferDescription(collector.filterPoints.capacity() / 4, 16, 0); //pointcount, pointstride, pointoffset
            rf.setPointDataDescription(0.05f, 0.01f); //accuracy, meanDistance
            rf.setTargetROI(pickIndex, 0.1f);//seedIndex,touchRadius //PickSeed.p2 * 0.25f
            rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
            FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);
            // Request Find Surface
            try {
              Log.d("PlaneFinder", "request");
              ResponseForm resp = fsr.request(rf, collector.filterPoints);
              if (resp != null && resp.isSuccess()) {
                ResponseForm.PlaneParam param = resp.getParamAsPlane();
                Log.d("PlaneFinder", "request success code: "+parseInt(String.valueOf(resp.getResultCode()))+
                        ", Normal Vector: "+Arrays.toString(resp.getParamAsPlane().n));
              } else {
                Log.d("PlaneFinder", "request fail");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          })).start();
          return false;
        }
      }
      return true;
    });
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        // Create the session.
        session = new Session(/* context= */ this);

        // ARCore 세부 설정
        Config config = new Config(session);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        config.setFocusMode(Config.FocusMode.AUTO);

        session.configure(config); // Update Configuration

      } catch (UnavailableArcoreNotInstalledException
              | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
      }

      if (message != null) {
        //messageSnackbarHelper.showError(this, message);
        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      session.resume();
    } catch (CameraNotAvailableException e) {
      //messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
              .show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(/*context=*/ this);
      pointCloudRenderer.createOnGlThread(/*context=*/ this);

    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }
    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      frame = session.update();
      Camera camera = frame.getCamera();

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);

      // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
      trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

      // If not tracking, don't draw 3D objects, show tracking failure reason instead.
      if (camera.getTrackingState() == TrackingState.PAUSED) {
        //messageSnackbarHelper.showMessage(
        //    this, TrackingStateHelper.getTrackingFailureReasonString(camera));
        return;
      }

      // Get projection matrix.
      float[] projmtx = new float[16];
      camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

      // Get camera matrix and draw.
      float[] viewmtx = new float[16];
      camera.getViewMatrix(viewmtx, 0);

      // Compute lighting from average intensity of the image.
      // The first three components are color scaling factors.
      // The last one is the average pixel intensity in gamma space.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // Visualize tracked points.
      // Use try-with-resources to automatically release the point cloud.
      if(!isStaticView) {
        try (PointCloud pointCloud = frame.acquirePointCloud()) {
          if (isRecording && collector != null) {
            collector.getPoints(pointCloud);
          }
          pointCloudRenderer.update(pointCloud);
          pointCloudRenderer.draw(viewmtx, projmtx);
        }
      } else {
        pointCloudRenderer.draw(viewmtx, projmtx);
      }

    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }
  }
}
