package edu.skku.treearium.Utils;

import android.util.Log;

import java.nio.FloatBuffer;

// 이름 PointUtil 로 바꾸기
public class PickSeed {

    public static float p2 = 0.0f;
    public static int pickPoint(FloatBuffer filterPoints, float[] camera, float[] ray){ // camera: 위치(x,y,z), ray : ray의 방향벡터

        //float thresholdDistance = 0.01f; // 10cm = 0.1m * 0.1m = 0.01f
        int seedPointID = -1;
        float minDistanceSq = Float.MAX_VALUE;

        for(int i = 0; i<filterPoints.remaining(); i += 4){
            float[] product = new float[]{filterPoints.get(i) - camera[0], filterPoints.get(i+1) - camera[1], filterPoints.get(i+2) - camera[2], 1.0f};

            // length between camera and point
            // faster than pow
            float distanceSq = product[0] * product[0] + product[1] * product[1] + product[2] * product[2]; // dot( product, product )
            float innerProduct = ray[0] * product[0] + ray[1] * product[1] + ray[2] * product[2]; // dot( ray, product )
            distanceSq = distanceSq - (innerProduct * innerProduct);  //c^2 - a^2 = b^2

            // determine candidate points
            if(distanceSq < minDistanceSq){ // distanceSq < thresholdDistance &&
                p2 = filterPoints.get(i+2);
                minDistanceSq = distanceSq;
                seedPointID = i/4;
            }
        }
        Log.d("pickSeed", String.format("%d", seedPointID));
        return seedPointID;
    }
}
