package edu.skku.treearium.Utils;

public class MatrixUtil {
    public static float[] crossMatrix(float a1, float a2, float a3, float b1, float b2, float b3){

        // cross product
        float i = a2*b3 - a3*b2;
        float j = a3*b1 - a1*b3;
        float k = a1*b2 - a2*b1;

        // unit vector
        float unit = (float)Math.sqrt(i*i + j*j + k*k);
        i /= unit;
        j /= unit;
        k /= unit;

        return new float[]{i,j,k};
    }
}
