package edu.skku.treearium.Utils;

import com.google.ar.core.PointCloud;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class PointCollector {

    Map<Integer, LinkedList<float[]>> allPoints;
    public FloatBuffer filterPoints;

    public PointCollector(){
        allPoints = new HashMap<>();
    }

    public void getPoints(PointCloud pointCloud){ //collect, accumulate

        IntBuffer intbuffer = pointCloud.getIds();
        FloatBuffer floatbuffer = pointCloud.getPoints();

        for (int i = 0; i < intbuffer.capacity(); i++) {
            float[] temp = {floatbuffer.get(i*4), floatbuffer.get(i*4 + 1), floatbuffer.get(i*4 + 2), floatbuffer.get(i*4 + 3)};

            //if hash map's IDth element doesn't exist, create array list
            int id = intbuffer.get(i);
            if (!allPoints.containsKey(id)) {
                LinkedList<float[]> list = new LinkedList<>();
                list.add(temp);
                allPoints.put(id, list);
            } else {
                allPoints.get(id).add(temp);
            }
        }
    }

    public FloatBuffer filterPoints() {

        int numPoints = 0;
        for (Map.Entry<Integer, LinkedList<float[]>> entry : allPoints.entrySet()) {
            if (entry.getValue().size() > 3) {
                numPoints++;
            }
        }
        filterPoints = ByteBuffer.allocateDirect(numPoints * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (Map.Entry<Integer, LinkedList<float[]>> entry : allPoints.entrySet()) {
            if (entry.getValue().size() > 3) {
                float mean_x = 0.0f, mean_y = 0.0f, mean_z = 0.0f;
                for (float[] p : entry.getValue()) {
                    mean_x += p[0]; // x
                    mean_y += p[1]; // y
                    mean_z += p[2]; // z
                }
                mean_z /= entry.getValue().size();
                mean_x /= entry.getValue().size();
                mean_y /= entry.getValue().size();

                if (entry.getValue().size() < 5) {
                    filterPoints.put(mean_x);
                    filterPoints.put(mean_y);
                    filterPoints.put(mean_z);
                    filterPoints.put(0);
                    continue; // no more calculation
                }

                // calculate variance
                float distance_mean = 0.f;
                float variance = 0.f;
                for (float[] tmp : entry.getValue()) {
                    float sqDistance = (float) (Math.pow((tmp[0] - mean_x), 2.0) + Math.pow((tmp[1] - mean_y), 2.0) + Math.pow((tmp[2] - mean_z), 2.0));
                    variance += sqDistance;
                    distance_mean += Math.sqrt(sqDistance);
                }
                distance_mean /= entry.getValue().size();
                variance = (variance / entry.getValue().size()) - distance_mean * distance_mean;

                // variance가 0일 때
                if (variance == 0) {
                    filterPoints.put(mean_x);
                    filterPoints.put(mean_y);
                    filterPoints.put(mean_z);
                    filterPoints.put(0);
                    continue; // no more calculation
                } else {
                    Iterator<float[]> iter = entry.getValue().iterator();
                    while (iter.hasNext()) {
                        float[] tmp = iter.next();
                        float sqDistance = (float) (Math.pow((tmp[0] - mean_x), 2) + Math.pow((tmp[1] - mean_y), 2) + Math.pow((tmp[2] - mean_z), 2));
                        float z_score = (float) (Math.abs(Math.sqrt(sqDistance) - distance_mean) / Math.sqrt(variance));
                        if (z_score >= 1.2f) {
                            iter.remove();
                        }
                    }

                    if (entry.getValue().size() == 0) continue;

                    mean_x = 0.f;
                    mean_y = 0.f;
                    mean_z = 0.f;
                    for (float[] tmp : entry.getValue()) {
                        mean_x += tmp[0];
                        mean_y += tmp[1];
                        mean_z += tmp[2];
                    }
                    mean_z /= entry.getValue().size();
                    mean_x /= entry.getValue().size();
                    mean_y /= entry.getValue().size();

                    filterPoints.put(mean_x);
                    filterPoints.put(mean_y);
                    filterPoints.put(mean_z);
                    filterPoints.put(0);
                }
            }
        }
        filterPoints.position(0);
        return filterPoints;
    }
}
