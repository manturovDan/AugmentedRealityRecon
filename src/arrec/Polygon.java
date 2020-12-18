package arrec;

import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Polygon {
    private MatOfPoint3f points;
    private final ArrayList<Point3> prevPoints;
    private final Scalar color;
    private final int face;

    public Polygon(Scalar _color, int _face) {
        color = _color;
        prevPoints = new ArrayList<>();
        face = _face;

    }

    public void addPoint(double[] pnt) {
        prevPoints.add(new Point3(new double[] {pnt[0], pnt[1], pnt[2], 1.}));
    }

    public int getFace() {
        return face;
    }

    public void build() {
        if (prevPoints.size() != 4)
            return;

        points = new MatOfPoint3f(prevPoints.toArray(new Point3[0]));
    }

    public boolean isBuilt() {
        return points != null && prevPoints.size() == 4;
    }

    public MatOfPoint3f getPoints() {
        return points;
    }

    public MatOfPoint3f getInternalPoints() {
        double step = 0.0003;
        ArrayList<Point3> allPolyPoints = new ArrayList<>();

        if (areVerticesXEq()) {
            double[] yBoundaries = getBoundaries(1);
            double[] zBoundaries = getBoundaries(2);
            double xVal = points.get(0, 0)[0];

            for (double currentY = yBoundaries[0];
                Double.compare(currentY, yBoundaries[1]) <= 0;
                currentY += step) {

                for (double currentZ = zBoundaries[0];
                    Double.compare(currentZ, zBoundaries[1]) <= 0;
                    currentZ += step) {

                    allPolyPoints.add(new Point3(xVal, currentY, currentZ));
                }
            }
        }
        else if (areVerticesYEq()) {
            double[] xBoundaries = getBoundaries(0);
            double[] zBoundaries = getBoundaries(2);
            double yVal = points.get(0, 0)[1];

            for (double currentX = xBoundaries[0];
                 Double.compare(currentX, xBoundaries[1]) <= 0;
                 currentX += step) {

                for (double currentZ = zBoundaries[0];
                     Double.compare(currentZ, zBoundaries[1]) <= 0;
                     currentZ += step) {

                    allPolyPoints.add(new Point3(currentX, yVal, currentZ));
                }
            }
        }
        else if (areVerticesZEq()) {
            double[] xBoundaries = getBoundaries(0);
            double[] yBoundaries = getBoundaries(1);
            double zVal = points.get(0, 0)[2];

            for (double currentX = xBoundaries[0];
                 Double.compare(currentX, xBoundaries[1]) <= 0;
                 currentX += step) {

                for (double currentY = yBoundaries[0];
                     Double.compare(currentY, yBoundaries[1]) <= 0;
                     currentY += step) {

                    allPolyPoints.add(new Point3(currentX, currentY, zVal));
                }
            }
        }
        else
            throw new RuntimeException("Error 3D model storing");

        MatOfPoint3f points3f = new MatOfPoint3f();
        points3f.fromList(allPolyPoints);
        return points3f;
    }

    private double[] getBoundaries(int coord) {
        double[] boundary = new double[2];
        boundary[0] = points.get(0, 0)[coord];
        boundary[1] = points.get(1, 0)[coord];
        boundary[1] = boundary[0] == boundary[1] ? points.get(2, 0)[coord] : boundary[1];

        if (Double.compare(boundary[0], boundary[1]) > 0) {
            double tmp = boundary[0];
            boundary[0] = boundary[1];
            boundary[1] = tmp;
        }

        return boundary;
    }

    public boolean areVerticesXEq() {
        return areVerticesEq(0);
    }

    public boolean areVerticesYEq() {
        return areVerticesEq(1);
    }

    public boolean areVerticesZEq() {
        return areVerticesEq(2);
    }

    public boolean areVerticesEq(int vert) {
        return Double.compare(points.get(0, 0)[vert], points.get(1, 0)[vert]) == 0 &&
                Double.compare(points.get(1, 0)[vert], points.get(2, 0)[vert]) == 0;
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
        retStr.append("color: ").append(color).append("\n");
        retStr.append("normal: ").append(getNormal().dump()).append("\nface: ").append(face).append("\n}\n");
        return retStr.toString();
    }


}
