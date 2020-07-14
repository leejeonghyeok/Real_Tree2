package Renderer;

/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Camera;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import Utils.ShaderUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/** Renders a point cloud. */
public class PointCloudRenderer {
    private static final String TAG = PointCloud.class.getSimpleName();

    // Shader names.
    private static final String VERTEX_SHADER_NAME = "point_cloud.vert";
    private static final String FRAGMENT_SHADER_NAME = "point_cloud.frag";

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;  // 32 / 8 = 4
    private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
    private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT; // 16
    private static final int INITIAL_BUFFER_POINTS = 1000;

    private int vbo;
    private int vboSize;

    private int programName;
    private int positionAttribute;
    private int modelViewProjectionUniform;
    private int colorUniform;
    private int pointSizeUniform;
    private int colorAttribute;
    private int bUseSolidColor;

    private int numPoints = 0;

    // Keep track of the last point cloud rendered to avoid updating the VBO if point cloud
    // was not changed.  Do this using the timestamp since we can't compare PointCloud objects.
    private long lastTimestamp = 0;

    public HashMap<Integer, ArrayList<float[]>> allPoints;
    public HashMap<Integer, float[]> filteredPoints;

    public FloatBuffer finalPointBuffer;
    public FloatBuffer pointBuffer;

    private float[] seedPoint;
    public int seedPointID;
    private FloatBuffer seedBuffer;
    public PointCloudRenderer() {}

    public void createOnGlThread(Context context) throws IOException {
        ShaderUtil.checkGLError(TAG, "before create");

        int[] buffers = new int[1];
        GLES20.glGenBuffers(1, buffers, 0);
        vbo = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        vboSize = INITIAL_BUFFER_POINTS * BYTES_PER_POINT;
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        ShaderUtil.checkGLError(TAG, "buffer alloc");

        int vertexShader =
                ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        int passthroughShader =
                ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);

        programName = GLES20.glCreateProgram();
        GLES20.glAttachShader(programName, vertexShader);
        GLES20.glAttachShader(programName, passthroughShader);
        GLES20.glLinkProgram(programName);
        GLES20.glUseProgram(programName);

        ShaderUtil.checkGLError(TAG, "program");

        positionAttribute = GLES20.glGetAttribLocation(programName, "a_Position");
        colorUniform = GLES20.glGetUniformLocation(programName, "u_Color");
        modelViewProjectionUniform = GLES20.glGetUniformLocation(programName, "u_ModelViewProjection");
        pointSizeUniform = GLES20.glGetUniformLocation(programName, "u_PointSize");
        colorAttribute = GLES20.glGetAttribLocation(programName, "a_Color");
        bUseSolidColor = GLES20.glGetUniformLocation(programName, "bUseSolidColor");

        ShaderUtil.checkGLError(TAG, "program  params");

        allPoints = new HashMap<>();
        filteredPoints = new HashMap<>();
    }

    /**
     * Updates the OpenGL buffer contents to the provided point. Repeated calls with the same point
     * cloud will be ignored.
     */
    public void update(PointCloud cloud, boolean recording) {
        ShaderUtil.checkGLError(TAG, "before update");

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        // If the VBO is not large enough to fit the new point cloud, resize it.
        numPoints = cloud.getPoints().remaining() / FLOATS_PER_POINT;
        if (numPoints * BYTES_PER_POINT > vboSize) {
            while (numPoints * BYTES_PER_POINT > vboSize) {
                vboSize *= 2;
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, GLES20.GL_DYNAMIC_DRAW);
        }
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, 0, numPoints * BYTES_PER_POINT, cloud.getPoints());
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        if(recording){
            FloatBuffer pointCloud = cloud.getPoints();
            IntBuffer pointCloudId = cloud.getIds();

            for (int i = 0; i < pointCloud.capacity() / 4; i++) {
                float[] temp = {pointCloud.get(i*4),pointCloud.get(i*4+1),pointCloud.get(i*4+2),pointCloud.get(i*4+3)};

                //if hash map's IDth element doesn't exist, create array list
                int id = pointCloudId.get(i);
                if (!allPoints.containsKey(id)) {
                    ArrayList<float[]> list = new ArrayList<>();
                    list.add(temp);
                    allPoints.put(id, list);
                } else {
                    allPoints.get(id).add(temp);
                }
            }
        }
        pointBuffer = cloud.getPoints();
        ShaderUtil.checkGLError(TAG, "after update");
    }

    /**
     * Renders the point cloud. ARCore point cloud is given in world space.
     *
     * @param cameraView the camera view matrix for this frame, typically from {@link
     *     com.google.ar.core.Camera#getViewMatrix(float[], int)}.
     * @param cameraPerspective the camera projection matrix for this frame, typically from {@link
     *     com.google.ar.core.Camera#getProjectionMatrix(float[], int, float, float)}.
     */
    public void draw(float[] cameraView, float[] cameraPerspective) {
        float[] modelViewProjection = new float[16];
        Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, cameraView, 0);

        ShaderUtil.checkGLError(TAG, "Before draw");

        GLES20.glUseProgram(programName);
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glVertexAttribPointer(positionAttribute, 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
        GLES20.glUniform1i(bUseSolidColor,1);
        GLES20.glUniform4f(colorUniform, 31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, modelViewProjection, 0);
        GLES20.glUniform1f(pointSizeUniform, 7.0f);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, numPoints);
        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        ShaderUtil.checkGLError(TAG, "Draw");
    }
    public void draw_conf(float[] cameraView, float[] cameraPerspective){
        float[] modelViewProjection = new float[16];
        Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, cameraView, 0);
        GLES20.glUseProgram(programName);

        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        //change color by vertexes' confidence
        float[] color = new float[pointBuffer.remaining()];
        for (int i = 3; i < pointBuffer.remaining(); i = i + 4) {
            float conf = pointBuffer.get(i);

            if (conf <= 0.33f) {
                color[i - 3] = 1.0f - conf;
                color[i - 2] = 0.0f;
                color[i - 1] = 0.0f;
                color[i] = 1.0f;
            } else if (conf <= 0.66f) {
                color[i - 3] = 0.0f;
                color[i - 2] = 1.0f - conf;
                color[i - 1] = 0.0f;
                color[i] = 1.0f;
            } else {
                color[i - 3] = 0.0f;
                color[i - 2] = 0.0f;
                color[i - 1] = conf;
                color[i] = 1.0f;
            }
        }
        //create color buffer and send to shader
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * color.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer colorBuffer = bb.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        GLES20.glEnableVertexAttribArray(colorAttribute);

        GLES20.glVertexAttribPointer(positionAttribute, 4, GLES20.GL_FLOAT, false, 16, 0);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, modelViewProjection, 0);
        GLES20.glUniform1f(pointSizeUniform, 7.0f);
        GLES20.glUniform1i(bUseSolidColor,0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glVertexAttribPointer(colorAttribute, 4, GLES20.GL_FLOAT, false, 16, colorBuffer);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, numPoints);

        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(colorAttribute);
    }
    public void draw_final(float[] cameraView, float[] cameraPerspective){

        float[] modelViewProjection = new float[16];
        Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, cameraView, 0);

        ShaderUtil.checkGLError(TAG, "Before draw");

        GLES20.glUseProgram(programName);
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, 4, GLES20.GL_FLOAT, false, 16, finalPointBuffer);
        GLES20.glUniform1i(bUseSolidColor,1);
        GLES20.glUniform4f(colorUniform, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, modelViewProjection, 0);
        GLES20.glUniform1f(pointSizeUniform, 7.0f);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, finalPointBuffer.remaining()/4);
        GLES20.glDisableVertexAttribArray(positionAttribute);

        ShaderUtil.checkGLError(TAG, "Draw");
    }
    public void draw_seedPoint(float[] vpMatrix){
        GLES20.glUseProgram(programName);
        GLES20.glEnableVertexAttribArray(positionAttribute);

        ByteBuffer bb = ByteBuffer.allocateDirect(4 * 4);
        bb.order(ByteOrder.nativeOrder());
        seedBuffer = bb.asFloatBuffer();
        seedBuffer.put(seedPoint);
        seedBuffer.position(0);

        GLES20.glVertexAttribPointer(positionAttribute, 4, GLES20.GL_FLOAT, false, 16, seedBuffer);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, vpMatrix, 0);
        GLES20.glUniform1f(pointSizeUniform, 30.0f);
        GLES20.glUniform1i(bUseSolidColor,1);

        GLES20.glUniform4f(colorUniform, 1.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, seedBuffer.remaining()/4);
        GLES20.glDisableVertexAttribArray(positionAttribute);
    }
    public void filterPoints(){
        for (int id : allPoints.keySet()) {
            ArrayList<float[]> list = allPoints.get(id);
            float mean_x = 0.0f, mean_y = 0.0f, mean_z = 0.0f;
            for (float[] p : list) {
                mean_x += p[0]; //  x
                mean_y += p[1]; //  y
                mean_z += p[2]; //  z
            }
            mean_z /= list.size();
            mean_x /= list.size();
            mean_y /= list.size();

            if (list.size() < 5) {
                float[] finalPoint = new float[]{mean_x, mean_y, mean_z};
                filteredPoints.put(id, finalPoint);
                continue;   // no more calculation
            }

            // calculate variance
            float distance_mean = 0.f;
            float variance = 0.f;
            for (float[] tmp : list) {
                float sqDistance = (float)(Math.pow((tmp[0] - mean_x), 2.0) + Math.pow((tmp[1] - mean_y), 2.0) + Math.pow((tmp[2] - mean_z), 2.0));
                variance += sqDistance;
                distance_mean += Math.sqrt(sqDistance);
            }
            distance_mean /= list.size();
            variance = (variance / list.size()) - distance_mean*distance_mean;

            // variance가 0일 때
            if(variance == 0){
                float[] finalPoint = new float[]{mean_x, mean_y, mean_z};
                filteredPoints.put(id, finalPoint);
                continue; // no more calculation
            }

            else {
                Iterator<float[]> iter = list.iterator();
                while(iter.hasNext()){
                    float[] tmp = iter.next();
                    float sqDistance = (float)(Math.pow((tmp[0] - mean_x), 2) + Math.pow((tmp[1] - mean_y), 2) + Math.pow((tmp[2] - mean_z), 2));
                    float z_score = (float)(Math.abs(Math.sqrt(sqDistance) - distance_mean) / Math.sqrt(variance));
                    if (z_score >= 1.2f) {
                        iter.remove();
                    }
                }

                if(list.size() == 0) continue;

                mean_x = 0.f;
                mean_y = 0.f;
                mean_z = 0.f;
                for (float[] tmp : list) {
                    mean_x += tmp[0];
                    mean_y += tmp[1];
                    mean_z += tmp[2];
                }
                mean_z /= list.size();
                mean_x /= list.size();
                mean_y /= list.size();

                filteredPoints.put(id, new float[]{mean_x, mean_y, mean_z});
            }
        }

        // make Floatbuffer with filtered points
        ByteBuffer fbb = ByteBuffer.allocateDirect(filteredPoints.size()*16);
        fbb.order(ByteOrder.nativeOrder());
        finalPointBuffer = fbb.asFloatBuffer();

        // convert List to array(primitive)
        float[] tempArray = new float[filteredPoints.size() *4];
        int point_num = 0;
        for(float[] p : filteredPoints.values()){
            tempArray[point_num] = p[0];
            tempArray[point_num+1] = p[1];
            tempArray[point_num+2] = p[2];
            tempArray[point_num+3] = 1.0f;

            point_num += 4;
        }

        finalPointBuffer.put(tempArray);
        finalPointBuffer.position(0);
    }
}
