package edu.skku.treearium.Activity.AR;

//원기둥 class
public class CylinderVars {
    private float dbh = -1.0f;
    private float[] nVec;
    private float[] Pose;

    public CylinderVars(float dbh, float[] nVec, float[] pose) {
        this.dbh = dbh;
        this.nVec = nVec;
        Pose = pose;
    }

    public float getDbh() {
        return dbh;
    }

    public float[] getnVec() {
        return nVec;
    }

    public float[] getPose() {
        return Pose;
    }

}
