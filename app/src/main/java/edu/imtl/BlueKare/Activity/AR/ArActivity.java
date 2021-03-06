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

package edu.imtl.BlueKare.Activity.AR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.hluhovskyi.camerabutton.CameraButton;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.imtl.BlueKare.Activity.MainActivity;
import edu.imtl.BlueKare.R;
import edu.imtl.BlueKare.Renderer.BackgroundRenderer;
import edu.imtl.BlueKare.Renderer.ObjectRenderer;
import edu.imtl.BlueKare.Renderer.PointCloudRenderer;
import edu.imtl.BlueKare.Renderer.TempRendererSet.GLSupport;
import edu.imtl.BlueKare.Renderer.TempRendererSet.RendererForDebug;
import edu.imtl.BlueKare.Utils.MatrixUtil;
import edu.imtl.BlueKare.Utils.PointCollector;
import edu.imtl.BlueKare.Utils.PointUtil;
import edu.imtl.BlueKare.Utils.VectorCal;
import edu.imtl.BlueKare.helpers.CameraPermissionHelper;
import edu.imtl.BlueKare.helpers.DisplayRotationHelper;
import edu.imtl.BlueKare.helpers.FullScreenHelper;
import edu.imtl.BlueKare.tensorflow.Classifier;
import edu.imtl.BlueKare.tensorflow.YoloV4Classifier;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class ArActivity extends AppCompatActivity implements GLSurfaceView.Renderer, SensorEventListener {
  /*********************** 건들 일 없는 것 ***********************/
  private static final String TAG = ArActivity.class.getSimpleName();

  private GLSurfaceView surfaceView;

  private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.CAMERA};
  private final int PERMISSION_REQUEST_CODE = 0; // PROTECTION_NORMAL
  private DisplayRotationHelper displayRotationHelper;
  private boolean installRequested;

  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final ObjectRenderer virtualObject = new ObjectRenderer();
  /*************************************************************/


  /************************* AR Core 용 *************************/
  private Session session;
  private Frame frame;
  /*************************************************************/


  /************************* 인터페이스용 ************************/
  private View arLayout;
  private Button popup = null;  /* <-- 이 버튼은 머임 ?*/
  private Button exit = null;
  private CameraButton recBtn = null;
  private BottomSheet bottomSheet = null;
  MaterialButtonToggleGroup toggle = null;
  private Button dbhButton = null;
  private Button heightButton = null;
  private Button typeButton = null;
  TextView dhbText = null;
  TextView heightText = null;
  TextView typeText = null;
  PopupActivity popupActivity = null;
  /*************************************************************/


  /************************* point 처리 *************************/
  private PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
  private PointCollector collector = null;
  /*************************************************************/


  /************************* findSurf **************************/
  private Thread httpTh;
  private Thread treeRec;
  private static final String REQUEST_URL = "https://developers.curvsurf.com/FindSurface/cylinder";
  private static final String REQUEST_URL_Plane = "https://developers.curvsurf.com/FindSurface/plane"; // Plane searching server address
  /*************************************************************/


  /************************** 탐지된 물체 ************************/
  private CylinderVars cylinderVars = null;
  private Plane plane;
  float curHeight = 0.0f;
  float treeHeight = 0.0f;
  /*************************************************************/


  /************************** 최종결과 **************************/
  String teamname = "teamname", username;
  String landmark = "landmark";
  String height = "";
  String dbh = "";
  String treeType = "";
  /*************************************************************/


  /************************* 보여주기용 **************************/
  private int angle = -3; // treearium 글씨 돌아감
  private float[] modelMatrix = new float[16];
  RendererForDebug renderer = new RendererForDebug();
  float[] treeBottom = null;
  float[] treeTanTop = new float[4];
  /*************************************************************/


  /************************* bool 값 ***************************/
  boolean isPlaneFound;    /* findSurf 로 땅을 찾았음 */
  //  boolean isProcessDone;   /* 모든게 끝나서 넘어갈 준비가 됨 */
  boolean isCylinderDone = false;
  boolean isHeightDone = false;
  Mode currentMode = Mode.isFindingCylinder;
  private boolean isRecording = false;
  private boolean isStaticView = false;
  private boolean drawSeedState = false;
  public boolean heightForTheFirstTime = true;
  /*************************************************************/


  /************************* firebase **************************/
  FirebaseAuth mFirebaseAuth;
  FirebaseFirestore fstore;
  String userID;
  LocationManager locationManager;
  String latitude, longitude;
  public static GeoPoint locationA;
  /*************************************************************/


  /************************** 수종인식 ***************************/
  private boolean treeRecog = false;
  private boolean surfToBitmap = false;
  private Classifier detector;
  private Bitmap croppedBitmap = null;
  public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

  private enum DetectorMode {
    TF_OD_API;
  }

  private void initBox() {
    try {
      detector =
              YoloV4Classifier.create(
                      getAssets(),
                      TF_OD_API_MODEL_FILE,
                      TF_OD_API_LABELS_FILE,
                      TF_OD_API_IS_QUANTIZED);
    } catch (final IOException e) {
      e.printStackTrace();
      Toast.makeText(getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private static final int TF_OD_API_INPUT_SIZE = 416;
  private static final boolean TF_OD_API_IS_QUANTIZED = false;
  private static final String TF_OD_API_MODEL_FILE = "yolov4-tiny-416-treearium.tflite";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/name.txt";

  private static final DetectorMode MODE = DetectorMode.TF_OD_API;

  /*************************************************************/

  /*************************** GYRO ****************************/
  private static SensorManager mSensorManager;
  private Sensor mGravity, mGeomagnetic, mLinearAcceleration;
  float[] fGravity, fGeo, fLinear;

  /*************************************************************/

  // 1 : dbh, 2 : height, 3 : type
  public void checkToggleType(int num) {
    dbhButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(num == 1 ? R.color.filters_buttons : R.color.colorWhite)));
    heightButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(num == 2 ? R.color.filters_buttons : R.color.colorWhite)));
    typeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(num == 3 ? R.color.filters_buttons : R.color.colorWhite)));
  }

  private static final int REQUEST_LOCATION = 1;

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  private final float[] anchorMatrix = new float[16];


  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
      fGravity = event.values;
    }

    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      fGeo = event.values;
    }

    if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
      fLinear = event.values;
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  // when touch after point_cloud gathering finished.
  View.OnTouchListener surfaceViewTouchListener = new View.OnTouchListener() {
    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      {
        if (collector != null && collector.filterPoints != null) {

          float tx = event.getX();
          float ty = event.getY();
          // ray 생성
          float[] ray = screenPointToWorldRay(tx, ty, frame);
          float[] rayOrigin = new float[]{ray[3], ray[4], ray[5]};

          Camera camera = frame.getCamera();

          float[] projmtx = new float[16];
          camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
          final float unitRadius = (float) (0.8 / Math.max(projmtx[0], projmtx[5]));

          drawSeedState = true;

          FloatBuffer targetPoints = collector.filterPoints;
          targetPoints.rewind();

          // pick the point that is closest to the ray
          int pickIndex = PointUtil.pickPoint(targetPoints, ray, rayOrigin);
          float[] seedPoint = PointUtil.getSeedPoint();

          float seedZLength = ray[0] * (seedPoint[0] - rayOrigin[0]) + ray[1] * (seedPoint[1] - rayOrigin[1]) + ray[2] * (seedPoint[2] - rayOrigin[2]);
          seedZLength = Math.abs(seedZLength);

          Log.d("UnitRadius__", Float.toString(unitRadius));
          Log.d("seedZLength__", Float.toString(seedZLength));
          float roiRadius = unitRadius * seedZLength / 2;
          Log.d("UnitRadius", roiRadius + " " + /*RMS*/roiRadius * 0.2f + " " + roiRadius * 0.4f);

          if (pickIndex >= 0 && !Thread.currentThread().isInterrupted()) {
            switch (currentMode) {
              // if user is trying to measure dbh
              case isFindingCylinder:
                // make a thread to find a cylinder
                httpTh = new Thread(() -> {
                  isCylinderDone = false;
                  RequestForm rf = new RequestForm();

                  rf.setPointBufferDescription(targetPoints.capacity() / 4, 16, 0); //pointcount, pointstride, pointoffset
                  rf.setPointDataDescription(roiRadius * 0.2f, roiRadius * 0.4f); //accuracy, meanDistance
                  rf.setTargetROI(pickIndex, roiRadius);//seedIndex,touchRadius
                  rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
                  FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);
                  // Request Find Surface
                  try {
                    Log.d("CylinderFinder", "request");
                    targetPoints.rewind();
                    ResponseForm resp = fsr.request(rf, targetPoints);
                    if (resp != null && resp.isSuccess()) {

                      ResponseForm.CylinderParam param = resp.getParamAsCylider();

                      // Normal Vector should be [0, 1, 0]
                      float[] tmp = new float[]{param.b[0] - param.t[0], param.b[1] - param.t[1], param.b[2] - param.t[2]};
                      float dist = (float) Math.sqrt(tmp[0] * tmp[0] + tmp[1] * tmp[1] + tmp[2] * tmp[2]);
                      tmp[0] /= dist;
                      tmp[1] /= dist;
                      tmp[2] /= dist;
                      if (tmp[1] < 0) {
                        tmp[0] = -tmp[0];
                        tmp[1] = -tmp[1];
                        tmp[2] = -tmp[2];
                        Log.d("tmp", "바뀜");
                      }

                      // making a model_matrix based on cylinder's geometric information
                      modelMatrix[4] = tmp[0];
                      modelMatrix[5] = tmp[1];
                      modelMatrix[6] = tmp[2];

                      float[] centerPose = new float[]{(param.b[0] + param.t[0]) / 2, (param.b[1] + param.t[1]) / 2, (param.b[2] + param.t[2]) / 2};
                      modelMatrix[12] = centerPose[0];
                      modelMatrix[13] = centerPose[1];
                      modelMatrix[14] = centerPose[2];

                      float[] x = MatrixUtil.crossMatrix(modelMatrix[4], modelMatrix[5], modelMatrix[6],
                              rayOrigin[0], rayOrigin[1], rayOrigin[2]);
                      modelMatrix[0] = x[0];
                      modelMatrix[1] = x[1];
                      modelMatrix[2] = x[2];
                      float[] z = MatrixUtil.crossMatrix(modelMatrix[0], modelMatrix[1], modelMatrix[2],
                              modelMatrix[4], modelMatrix[5], modelMatrix[6]);
                      modelMatrix[8] = z[0];
                      modelMatrix[9] = z[1];
                      modelMatrix[10] = z[2];

                      Log.d("modelMatrix: ", modelMatrix[0] + " " + modelMatrix[4] + " " + modelMatrix[8] + " " + modelMatrix[12]);
                      Log.d("modelMatrix: ", modelMatrix[1] + " " + modelMatrix[5] + " " + modelMatrix[9] + " " + modelMatrix[13]);
                      Log.d("modelMatrix: ", modelMatrix[2] + " " + modelMatrix[6] + " " + modelMatrix[10] + " " + modelMatrix[14]);
                      Log.d("modelMatrix: ", modelMatrix[3] + " " + modelMatrix[7] + " " + modelMatrix[11] + " " + modelMatrix[15]);

                      cylinderVars = new CylinderVars(param.r, tmp, centerPose, param.b, param.t);
                      Log.d("CylinderFinder", "request success code: " + parseInt(valueOf(resp.getResultCode())) +
                              ", Radius: " + param.r + ", Normal Vector: " + Arrays.toString(tmp) +
                              ", RMS: " + resp.getRMS());
                      Log.d("Cylinder", valueOf(cylinderVars.getDbh()));

                      if (cylinderVars.getDbh() > 0.0f) {
                        isCylinderDone = true;
                      }

                      // if cylinder is built without any errors, bottom_sheet pops out
                      if (isCylinderDone) {
                        runOnUiThread(() -> {
                          Snackbar.make(arLayout, "Cylinder Found", Snackbar.LENGTH_LONG).show();
                          landmark = "일월저수지";
                          dbh = String.format("%.2f", cylinderVars.getDbh() * 200);
                          buildTextView();

                          currentMode = Mode.isFindingHeight;
                          if (heightForTheFirstTime) {
                            popupActivity.startDialog(R.layout.activity_popup2);
                            heightForTheFirstTime = false;
                          }
                          toggle.check(R.id.heightButton);
                          checkToggleType(2);
                          resetArActivity(false);

                          bottomSheet.setAlertText(1);
                          bottomSheet.setTeamName(teamname);
                          bottomSheet.setDbhSize(dbh);
                          bottomSheet.setTreeHeight(height);
                          bottomSheet.setTreeLandMark(landmark);
                          bottomSheet.setConfirmButton(fstore, locationA);
                          bottomSheet.setTreeType(ArActivity.this, treeType);
                          bottomSheet.show();
                        });
                      }

                      drawSeedState = false;
                      collector = null;
                    } else {
                      Log.d("CylinderFinder", "request fail");
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                });
                httpTh.start();
                break;

              // if user is trying to find height, and plane has found
              case isFindingHeight:
                // make thread like one above
                httpTh = new Thread(() -> {
                  RequestForm rf = new RequestForm();

                  rf.setPointBufferDescription(targetPoints.capacity() / 4, 16, 0); //pointcount, pointstride, pointoffset
                  rf.setPointDataDescription(0.05f, 0.01f); //accuracy, meanDistance
                  rf.setTargetROI(pickIndex, 0.05f);//seedIndex,touchRadius
                  rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
                  FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL_Plane, true);
                  // Request Find Surface
                  try {
                    Log.d("PlaneFinder", "request");
                    targetPoints.rewind();
                    ResponseForm resp = fsr.request(rf, targetPoints);
                    if (resp != null && resp.isSuccess()) {

                      ResponseForm.PlaneParam param = resp.getParamAsPlane();

                      plane = new Plane(
                              param.ll, param.lr, param.ur, param.ul, camera.getPose().getZAxis()
                      );

                      plane.normal[0] = 0.0f;
                      plane.normal[1] = 1.0f;
                      plane.normal[2] = 0.0f;

                      // using orthogonal formula
                      treeBottom = new float[4];
                      if (cylinderVars != null) {
                        float[] p1_to_p3 = new float[]{
                                plane.ll[0] - cylinderVars.top[0],
                                plane.ll[1] - cylinderVars.top[1],
                                plane.ll[2] - cylinderVars.top[2]
                        };

                        float[] p1_to_p2 = new float[]{
                                cylinderVars.bottom[0] - cylinderVars.top[0],
                                cylinderVars.bottom[1] - cylinderVars.top[1],
                                cylinderVars.bottom[2] - cylinderVars.top[2]
                        };

                        float n_p1_p3 = VectorCal.inner(plane.normal, p1_to_p3);
                        float n_p1_p2 = VectorCal.inner(plane.normal, p1_to_p2);
                        float const_u = n_p1_p3 / n_p1_p2;

                        float[] p = new float[]{
                                cylinderVars.top[0] + const_u * p1_to_p2[0],
                                cylinderVars.top[1] + const_u * p1_to_p2[1],
                                cylinderVars.top[2] + const_u * p1_to_p2[2],
                        };

                        treeBottom[0] = p[0];
                        treeBottom[1] = p[1];
                        treeBottom[2] = p[2];
                        treeBottom[3] = 1.0f;

                        // our new normal
                        float[] bot_to_top = new float[]{
                                cylinderVars.top[0] - cylinderVars.bottom[0],
                                cylinderVars.top[1] - cylinderVars.bottom[1],
                                cylinderVars.top[2] - cylinderVars.bottom[2]
                        };

                        plane.normal[0] = bot_to_top[0];
                        plane.normal[1] = bot_to_top[1];
                        plane.normal[2] = bot_to_top[2];

                        isPlaneFound = true;
                      } else {
                        // if user is trying to find height, and plane hasn't found
                        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                            Snackbar.make(arLayout, "나무의 밑동을 터치", Snackbar.LENGTH_SHORT).show();
                          }
                        });

                        // doing same thing that has been done when finding cylinder
                        surfaceView.setOnTouchListener(new View.OnTouchListener() {
                          @Override
                          public boolean onTouch(View v, MotionEvent motionEvent) {

                            float tx = motionEvent.getX();
                            float ty = motionEvent.getY();

                            float[] ray = screenPointToWorldRay(tx, ty, frame);

                            float a = plane.normal[0], b = plane.normal[1], c = plane.normal[2];
                            float d = -a * plane.ll[0] - b * plane.ll[1] - c * plane.ll[2];
                            float a1 = (a * a) + (b * b) + (c * c);
                            double planeConstant = java.lang.Math.sqrt(a1);
                            double distance = Math.abs((a * ray[0]) + (b * ray[1]) + (c * ray[2]) + d) / planeConstant;

                            float cosTheta = (float) Math.abs(
                                    (a * ray[3] + b * ray[4] + c * ray[5])
                                            /
                                            (Math.sqrt(a1) * Math.sqrt(ray[3] * ray[3] + ray[4] * ray[4] + ray[5] * ray[5]))
                            );

                            double vecSize = distance / cosTheta;


                            treeBottom[0] = (float) (ray[0] + ray[3] * vecSize);
                            treeBottom[1] = (float) (ray[1] + ray[4] * vecSize);
                            treeBottom[2] = (float) (ray[2] + ray[5] * vecSize);
                            treeBottom[3] = 1.0f;

                            isPlaneFound = true;

                            return false;
                          }
                        });
                      }
                    } else {
                      Log.d("PlaneFinder", "request fail");
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                });
                httpTh.start();
                break;
            }
          }


          // when failed to find plane
          ArActivity.this.runOnUiThread(() -> {
            Snackbar.make(arLayout, "Please Wait...", Snackbar.LENGTH_LONG).show();
            try {
              httpTh.join();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            if (currentMode == Mode.isFindingHeight) {
              Snackbar.make(arLayout, "Plane " + (isPlaneFound ? "Found" : "Not Found"), Snackbar.LENGTH_SHORT).show();

            } else {
              Snackbar.make(arLayout, "PickSeed Again", Snackbar.LENGTH_LONG).show();
            }
          });
        }
      }
      return false;
    }
  };

  // reset this activity
  @SuppressLint("ClickableViewAccessibility")
  public void resetArActivity(boolean needToDeleteCylinder) {
    isPlaneFound = false;
    isHeightDone = false;
    if (needToDeleteCylinder) {
      isCylinderDone = false;
      cylinderVars = null;
    }
    collector = null;
    surfaceView.setOnTouchListener(surfaceViewTouchListener);
  }


  // when create, initiate things that has to be initiated.
  @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);

    Intent intent = getIntent();
    ArrayList<String> recieve = intent.getStringArrayListExtra("treeInfo");
//    이런식으로 intentd에 스트링배열루다가 넘겨서 하나하나 그냥 스트링에 입력하면 댐
//    if (recieve != null) {
//      teamname = recieve.get(0);
//    }

    // initiating stuffs...
    arLayout = findViewById(R.id.arLayout);
    popup = (Button) findViewById(R.id.popup);
    exit = (Button) findViewById(R.id.delete);
    recBtn = (CameraButton) findViewById(R.id.recBtn);
    surfaceView = (GLSurfaceView) findViewById(R.id.surfaceview);
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    toggle = (MaterialButtonToggleGroup) findViewById(R.id.toggleGroup);
    dbhButton = (Button) findViewById(R.id.dbhButton);
    heightButton = (Button) findViewById(R.id.heightButton);
    typeButton = (Button) findViewById(R.id.typeButton);

    dhbText = (TextView) findViewById(R.id.dbhText);
    heightText = (TextView) findViewById(R.id.heightText);
    typeText = (TextView) findViewById(R.id.typeText);
    dhbText.setWidth(dbhButton.getWidth());
    heightText.setWidth(heightButton.getWidth());
    typeText.setWidth(typeButton.getWidth());
    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    mGeomagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

    modelMatrix[3] = 0.0f;
    modelMatrix[7] = 0.0f;
    modelMatrix[11] = 0.0f;
    modelMatrix[15] = 1.0f;

    //firebase
    mFirebaseAuth = FirebaseAuth.getInstance();
    fstore = FirebaseFirestore.getInstance();
    userID = mFirebaseAuth.getCurrentUser().getUid();
    fstore.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
          DocumentSnapshot document = task.getResult();
          teamname = (String) document.getData().get("Team");
          //teamname = (String) document.getData().get("fName");
          //document.getString("Team")
          if (document.exists()) {
            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
          } else {
            Log.d(TAG, "No such document");
          }
        } else {
          Log.d(TAG, "get failed with ", task.getException());
        }
      }
    });
    //Intent intent = new Intent(ArActivity.this, PopupActivity.class);
    //startActivityForResult(intent, 1);
    popupActivity = new PopupActivity(ArActivity.this);
    popupActivity.startDialog(R.layout.activity_popup);

    installRequested = false;

    LocationManager nManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    if (ActivityCompat.checkSelfPermission(
            ArActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            ArActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    } else {
      Location locationGPS = nManager.getLastKnownLocation(GPS_PROVIDER);
      if (locationGPS != null) {
        double lat = locationGPS.getLatitude();
        double longi = locationGPS.getLongitude();
        //location = new GeoPoint(lat, longi);
      } else {
        Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
      }

    }
    GPSSListener gpsListener = new GPSSListener();

    long minTime = 1000;
    float minDistance = 0;

    nManager.requestLocationUpdates(NETWORK_PROVIDER, minTime, minDistance, gpsListener);
    nManager.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance, gpsListener);

    buildTextView();
    checkToggleType(1);

    // initiating the 3 buttons that is on top of this activity
    toggle.check(R.id.dbhButton);
    toggle.addOnButtonCheckedListener((group, checkedID, isChecked) -> {
      if (isChecked) {
        switch (checkedID) {
          // change current mode and reset
          case R.id.dbhButton:
            currentMode = Mode.isFindingCylinder;
            resetArActivity(true);
            checkToggleType(1);
            break;
          case R.id.heightButton:
            currentMode = Mode.isFindingHeight;
            if (heightForTheFirstTime) {
              popupActivity.startDialog(R.layout.activity_popup2);
              heightForTheFirstTime = false;
            }
            resetArActivity(false);
            checkToggleType(2);
            break;
          case R.id.typeButton:
            checkToggleType(3);
            break;
        }
      } else {
        if (group.getCheckedButtonId() == -1) {
          group.check(checkedID);
        }
      }
    });

    popup.setOnClickListener(v -> {
      popupActivity.startDialog(currentMode == Mode.isFindingCylinder ? R.layout.activity_popup : R.layout.activity_popup2);
    });

    exit.setOnClickListener(v -> {
      startActivity(new Intent(ArActivity.this, MainActivity.class));
      finish();
    });

    bottomSheet = new BottomSheet();
    bottomSheet.init(ArActivity.this, new BottomSheetDialog(
            ArActivity.this, R.style.BottomSheetDialogTheme
    ));

    // initiating record button
    recBtn.setOnClickListener(v -> {
      double distance = 0.1f;

      // to give bottom_sheet information
      if (isPlaneFound) {
        treeHeight = curHeight;
        isHeightDone = true;

        runOnUiThread(() -> {
          Snackbar.make(arLayout, "Height Found", Snackbar.LENGTH_LONG).show();
          landmark = "일월저수지";
          height = String.format("%.2f", treeHeight);
          buildTextView();
          toggle.check(R.id.typeButton);
        });

        // 수종 인식: GLSurfaceView to Bitmap
        surfToBitmap = true;
        // test.setImageBitmap(croppedBitmap); // 디버깅용

        // 인식 시작
        treeRec = new Thread(() -> {
          while (surfToBitmap) {
            try {
              Thread.sleep(300);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
          final List<Classifier.Recognition> mappedRecognitions = new LinkedList<>();

          for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
              result.setLocation(location);
              mappedRecognitions.add(result);
            }
          }
          if (mappedRecognitions.size() > 0) {
            Log.d("treeRecognization", "success");
            Log.d("treeRecognization", mappedRecognitions.get(0).getTitle());
            treeType = mappedRecognitions.get(0).getTitle();
            switch (treeType) {
              case "Maple":
                treeType = "단풍";
                break;
              case "Ginkgo":
                treeType = "은행";
                break;
              default:
                treeType = "기타";
                break;
            }
            buildTextView();
          } else {
            Log.d("treeRecognization", "fail");
          }

          treeRecog = false;
        });
        treeRec.start();
        ////////////////////////////////

        if (isHeightDone) {
          // setting bottom_sheet information, and make it pop
          runOnUiThread(() -> {
            try {
              treeRec.join();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            bottomSheet.setAlertText(2);
            bottomSheet.setTeamName(teamname);
            bottomSheet.setDbhSize(dbh);
            bottomSheet.setTreeHeight(height);
            bottomSheet.setTreeLandMark(landmark);
            bottomSheet.setConfirmButton(fstore, locationA);
            bottomSheet.setTreeType(this, treeType);
            bottomSheet.show();
          });
        }

      } else {
        // when pressed again, turn of recording mode
        isRecording = !isRecording;
        if (isRecording) {
          collector = new PointCollector();
          Snackbar.make(arLayout, "Collecting", Snackbar.LENGTH_LONG).show();
          isStaticView = false;
        } else {
          (new Thread(() -> {
            if (ArActivity.this.collector != null) {
              ArActivity.this.collector.filterPoints = ArActivity.this.collector.doFilter();
              ArActivity.this.surfaceView.queueEvent(() -> {
                pointCloudRenderer.update(ArActivity.this.collector.filterPoints);
                isStaticView = true;
              });
              ArActivity.this.runOnUiThread(() -> {
                Snackbar.make(arLayout, "Collecting Finished", Snackbar.LENGTH_LONG).show();
              });
            }
          })).start();
        }
      }
    });

    resetArActivity(true);
    surfaceView.setOnTouchListener(surfaceViewTouchListener);

    // require permissions
    for (String permission : REQUIRED_PERMISSSIONS) {
      if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSSIONS, PERMISSION_REQUEST_CODE);
      }
    }

    initBox();
  }

  void buildTextView() {
    dhbText.setText(dbh);
    heightText.setText(height);
    typeText.setText(treeType);
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onResume() {
    // (nothing special.. only things that needs to be done)

    super.onResume();
    mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_UI);
    mSensorManager.registerListener(this, mGeomagnetic, SensorManager.SENSOR_DELAY_UI);
    mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_UI);

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
        session = new Session(this);

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
    mSensorManager.unregisterListener(this);
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
      virtualObject.createOnGlThread(/*context=*/ this, "models/cylinder_r.obj", "models/treearium.png");
      virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);
      virtualObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);

      renderer.createOnGlThread(this);
    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
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

    try {// Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      session.setCameraTextureName(backgroundRenderer.getTextureId());
      frame = session.update();
      Camera camera = frame.getCamera();

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);

      if (pointCloudRenderer == null) return;
      // Get projection matrix.
      // Get camera matrix and draw.
      // Get multiple of proj matrix and view matrix
      float[] projmtx = new float[16];
      float[] viewmtx = new float[16];
      float[] vpMatrix = new float[16];
      camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
      camera.getViewMatrix(viewmtx, 0);

      Matrix.multiplyMM(vpMatrix, 0, projmtx, 0, viewmtx, 0);

      // Compute lighting from average intensity of the image.
      // The first three components are color scaling factors.
      // The last one is the average pixel intensity in gamma space.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // draw gathered point_clouds while recording
      if (!isStaticView) {
        PointUtil.resetSeedPoint();
        try (PointCloud pointCloud = frame.acquirePointCloud()) {
          if (isRecording && collector != null) {
            collector.doCollect(pointCloud);
          }
          pointCloudRenderer.update(pointCloud);
          pointCloudRenderer.draw(viewmtx, projmtx);
        }
      } else {
        pointCloudRenderer.draw(viewmtx, projmtx);

        if (drawSeedState && PointUtil.getSeedPoint() != null) {
          float[] seedPoint = PointUtil.getSeedPoint();

          pointCloudRenderer.draw_seedPoint(vpMatrix, seedPoint);
        }
      }

      // when cylinder is found, draw small cylinder
      if (isCylinderDone) {
        if (cylinderVars != null) {
          GLES20.glEnable(GLES20.GL_CULL_FACE);
          //Matrix.setIdentityM(modelMatrix, 0);
          Matrix.rotateM(modelMatrix, 0, angle, 0, 1, 0);
          //angle++;
          virtualObject.updateModelMatrix(modelMatrix, cylinderVars.getDbh(), 0.05f, cylinderVars.getDbh());
          virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba);

          GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
      }

      // when finding height, draw a line based on phone's angle
      if (currentMode == Mode.isFindingHeight) {
        if (isPlaneFound) {
          if (treeBottom != null && !isHeightDone) {
            float width = this.surfaceView.getMeasuredWidth();
            float height = this.surfaceView.getMeasuredHeight();
            float[] ray = screenPointToWorldRay(width / 2.0f, height / 2.0f, frame);
            float[] rayVec = new float[]{ray[3], ray[4], ray[5]};
            float[] bigPlaneVec = VectorCal.outer(rayVec, VectorCal.outer(plane.normal, rayVec));

            // applying orthogonal formula
            float u = ((bigPlaneVec[0] * (ray[0] - treeBottom[0])) + (bigPlaneVec[1] * (ray[1] - treeBottom[1])) + (bigPlaneVec[2] * (ray[2] - treeBottom[2])))
                    /
                    ((bigPlaneVec[0] * plane.normal[0]) + (bigPlaneVec[1] * plane.normal[1]) + (bigPlaneVec[2] * plane.normal[2]));
            curHeight = (float) java.lang.Math.sqrt(
                    u * plane.normal[0] * u * plane.normal[0]
                            + u * plane.normal[1] * u * plane.normal[1]
                            + u * plane.normal[2] * u * plane.normal[2]
            );

            treeTanTop = new float[]{
                    treeBottom[0] + u * plane.normal[0], treeBottom[1] + u * plane.normal[1], treeBottom[2] + u * plane.normal[2], 1.0f
            };
          }

          float[] tmp = new float[]{
                  treeBottom[0], treeBottom[1], treeBottom[2], 1.0f,
                  treeTanTop[0], treeTanTop[1], treeTanTop[2], 1.0f
          };
          renderer.pointDraw(GLSupport.makeFloatBuffer(treeBottom), vpMatrix, Color.valueOf(Color.CYAN), 30.0f);
          renderer.lineDraw(GLSupport.makeFloatBuffer(tmp), vpMatrix, Color.valueOf(Color.RED), 30.0f);
        }
      }

    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }

    // 수종 인식: GLSurfaceView to croppedBitmap
    if (surfToBitmap) {
      int width = surfaceView.getWidth();
      int height = surfaceView.getHeight();
      int screenshotSize = width * height;
      ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
      bb.order(ByteOrder.nativeOrder());
      gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA,
              GL10.GL_UNSIGNED_BYTE, bb);
      int pixelsBuffer[] = new int[screenshotSize];
      bb.asIntBuffer().get(pixelsBuffer);
      bb = null;
      croppedBitmap = Bitmap.createBitmap(width, height,
              Bitmap.Config.RGB_565);
      croppedBitmap.setPixels(pixelsBuffer, screenshotSize - width, -width, 0,
              0, width, height);
      pixelsBuffer = null;

      short sBuffer[] = new short[screenshotSize];
      ShortBuffer sb = ShortBuffer.wrap(sBuffer);
      croppedBitmap.copyPixelsToBuffer(sb);

      // Making created bitmap (from OpenGL points) compatible with
      // Android bitmap
      for (int i = 0; i < screenshotSize; ++i) {
        short v = sBuffer[i];
        sBuffer[i] = (short) (((v & 0x1f) << 11) | (v & 0x7e0) | ((v & 0xf800) >> 11));
      }
      sb.rewind();
      croppedBitmap.copyPixelsFromBuffer(sb);

      // resize
      croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap,
              TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true);

      surfToBitmap = false;
      treeRecog = true;
    }
  }

  // not used.
  public static double haversine(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6372.8; // In kilometers
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    lat1 = Math.toRadians(lat1);
    lat2 = Math.toRadians(lat2);

    double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
    double c = 2 * Math.asin(Math.sqrt(a));
    return R * c;
  }

  float[] screenPointToWorldRay(float xPx, float yPx, Frame frame) {
    // ray[0~2] : camera pose
    // ray[3~5] : Unit vector of ray
    float[] ray_clip = new float[4];
    ray_clip[0] = 2.0f * xPx / surfaceView.getMeasuredWidth() - 1.0f;
    // +y is up (android UI Y is down):
    ray_clip[1] = 1.0f - 2.0f * yPx / surfaceView.getMeasuredHeight();
    ray_clip[2] = -1.0f; // +z is forwards (remember clip, not camera)
    ray_clip[3] = 1.0f; // w (homogenous coordinates)

    // multiply inverse proj, inverse view, to get world coordinate

    float[] ProMatrices = new float[32];  // {proj, inverse proj}
    frame.getCamera().getProjectionMatrix(ProMatrices, 0, 0.1f, 100.0f);
    Matrix.invertM(ProMatrices, 16, ProMatrices, 0);
    float[] ray_eye = new float[4];
    Matrix.multiplyMV(ray_eye, 0, ProMatrices, 16, ray_clip, 0);

    ray_eye[2] = -1.0f;
    ray_eye[3] = 0.0f;

    float[] out = new float[6];
    float[] ray_wor = new float[4];
    float[] ViewMatrices = new float[32];

    frame.getCamera().getViewMatrix(ViewMatrices, 0);
    Matrix.invertM(ViewMatrices, 16, ViewMatrices, 0);
    Matrix.multiplyMV(ray_wor, 0, ViewMatrices, 16, ray_eye, 0);

    float size = (float) Math.sqrt(ray_wor[0] * ray_wor[0] + ray_wor[1] * ray_wor[1] + ray_wor[2] * ray_wor[2]);

    out[3] = ray_wor[0] / size;
    out[4] = ray_wor[1] / size;
    out[5] = ray_wor[2] / size;

    out[0] = frame.getCamera().getPose().tx();
    out[1] = frame.getCamera().getPose().ty();
    out[2] = frame.getCamera().getPose().tz();

    return out;
  }

  private class GPSSListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location2) {
      Double latitude = location2.getLatitude();
      Double longitude = location2.getLongitude();
      //LatLng onLop=new LatLng(latitude,longitude);
      locationA = new GeoPoint(latitude, longitude);

      String message = "내 위치 -> Latitude : " + latitude + "\nLongitude:" + longitude;
      Log.d("Map", message);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
  }
}

class Plane {
  public float[] ll, lr, ul, ur;
  public float[] normal = null;

  public Plane(float[] ll, float[] lr, float[] ur, float[] ul, float[] z_dir) {
    this.ll = ll;
    this.lr = lr;
    this.ul = ul;
    this.ur = ur;
    normal = new float[3];
    this.calNormal();
    this.checkNormal(z_dir);
  }

  protected void calNormal() {
    // Calculate normal vector
    float[] vec1 = {lr[0] - ll[0], lr[1] - ll[1], lr[2] - ll[2]};
    float[] vec2 = {ul[0] - ll[0], ul[1] - ll[1], ul[2] - ll[2]};

    this.normal = VectorCal.outer(vec1, vec2);
    VectorCal.normalize(this.normal);
  }

  public void checkNormal(float[] z_dir) {
    if (z_dir[0] * normal[0] + z_dir[1] * normal[1] + z_dir[2] * normal[2] >= 0) return;

    normal[0] = -normal[0];
    normal[1] = -normal[1];
    normal[2] = -normal[2];
  }
}

enum Mode {
  isFindingCylinder,
  isFindingHeight
}