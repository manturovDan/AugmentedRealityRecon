package arrec;

import javafx.geometry.Point3D;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Polygon {
    private MatOfPoint3f points;
    private ArrayList<Point3> prevPoints;
    private Scalar color;

    public Polygon(Scalar _color) {
        color = _color;
        prevPoints = new ArrayList<>();
    }

    public void addPoint(double[] pnt) {
        prevPoints.add(new Point3(new double[] {pnt[0], pnt[1], pnt[2], 1.}));
    }

    public void build() {
        if (prevPoints.size() < 3)
            return;

        points = new MatOfPoint3f(prevPoints.toArray(new Point3[0]));
        System.out.print("Color " +color );
    }

    public boolean isBuilt() {
        return points != null && prevPoints.size() >= 3;
    }

    public MatOfPoint3f getPoints() {
        return points;
    }

    public Mat getNormal() {
        Mat vec1 = new Mat(1, 3, CvType.CV_64FC1);
        Mat vec2 = new Mat(1, 3, CvType.CV_64FC1);

        vec1.put(0, 0, prevPoints.get(1).x - prevPoints.get(0).x,
                prevPoints.get(1).y - prevPoints.get(0).y,
                prevPoints.get(1).z - prevPoints.get(0).z);

        vec2.put(0, 0, prevPoints.get(2).x - prevPoints.get(0).x,
                prevPoints.get(2).y - prevPoints.get(0).y,
                prevPoints.get(2).z - prevPoints.get(0).z);

        String v1 = vec1.dump();
        String v2 = vec2.dump();

        Mat cross =  vec1.cross(vec2);

        String norm1 = cross.dump();
        make1Size(cross);

        String norm = cross.dump();
        return cross;
    }

    public static void make1Size(Mat vector) {
        double vecLength = Math.sqrt(Math.pow(vector.get(0, 0)[0], 2) + Math.pow(vector.get(0, 1)[0], 2) + Math.pow(vector.get(0, 2)[0], 2));
        vector.put(0, 0, vector.get(0, 0)[0] / vecLength);
        vector.put(0, 1, vector.get(0, 1)[0] / vecLength);
        vector.put(0, 2, vector.get(0, 2)[0] / vecLength);
    }

    public Scalar getColor() {
        return color;
    }

    @Override
    public String toString() {
        StringBuilder retStr = new StringBuilder("{\n");

        if (isBuilt()) {
            retStr.append("points: ").append(points.dump()).append("\n");
        }
        retStr.append("color: ").append(color).append("\n}");
        retStr.append("normal: ").append(getNormal().dump() + "\n" +
                "(" + Math.sqrt(Math.pow(getNormal().get(0, 0)[0], 2) + Math.pow(getNormal().get(0, 1)[0], 2) + Math.pow(getNormal().get(0, 2)[0], 2)) + ")\n");
        return retStr.toString();
    }
}
