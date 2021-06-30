package edu.imtl.BlueKare.Activity.AR;

// cylinder의 정보를 담고 있는 class. DBH, 위치 등으로 렌더링에 활용됨.
public class CylinderVars {
    private float dbh = -1.0f;
    private float[] nVec;
    private float[] Pose;
    final public float[] bottom;
    final public float[] top;

    public CylinderVars(float dbh, float[] nVec, float[] pose, float[] bottom, float[] top) {
        this.dbh = dbh;
        this.nVec = nVec;
        this.Pose = pose;
        this.bottom = bottom;
        this.top = top;
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
