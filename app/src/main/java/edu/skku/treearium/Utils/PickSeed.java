package com.example.Real_Tree.Utils;

import android.util.Log;

import java.nio.FloatBuffer;

public class PickSeed {

    private float[] seedPoint;
    public int seedPointID;

    public void pickPoint(FloatBuffer filterPoints, float[] camera, float[] ray){ // camera: 위치(x,y,z), ray : ray의 방향벡터
        float thresholdDistance = 0.01f; // 10cm = 0.1m * 0.1m = 0.01f
        seedPoint = new float[]{0, 0, 0, Float.MAX_VALUE};

        for(int i = 0; i<filterPoints.remaining(); i += 4){
            float[] product = new float[]{filterPoints.get(i) - camera[0], filterPoints.get(i+1) - camera[1], filterPoints.get(i+2) - camera[2], 1.0f};

            // length between camera and point
            // pow보다 곱하는 게 더 빠름
            float distanceSq = product[0] * product[0] + product[1] * product[1] + product[2] * product[2]; // dot( product, product )
            float innerProduct = ray[0] * product[0] + ray[1] * product[1] + ray[2] * product[2]; // dot( ray, product )
            distanceSq = distanceSq - (innerProduct * innerProduct);  //c^2 - a^2 = b^2

            // determine candidate points
            if(distanceSq < thresholdDistance && distanceSq < seedPoint[3]){
                seedPoint[0] = filterPoints.get(i);
                seedPoint[1] = filterPoints.get(i+1);
                seedPoint[2] = filterPoints.get(i+2);
                seedPoint[3] = distanceSq;
                seedPointID = i/4;
            }
        }
        Log.d("pickSeed", String.format("%.2f %.2f %.2f : %d", seedPoint[0], seedPoint[1],seedPoint[2],seedPointID));
    }
    public float[] getSeedArr(){
        return new float[]{seedPoint[0], seedPoint[1], seedPoint[2], 1.0f};
    }
}
