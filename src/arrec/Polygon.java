package arrec;

import javafx.geometry.Point3D;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;

public class Polygon {
    private MatOfPoint3f points;
    private ArrayList<Point3> prevPoints;
    private Scalar color;

    public Polygon(MatOfPoint3f pointsArr, Scalar _color) {
        points = pointsArr;
        color = _color;
    }

    public Polygon(Scalar _color) {
        color = _color;
        prevPoints = new ArrayList<>();
    }

    public void addPoint(double[] pnt) {
        double[] newPoint = Arrays.copyOf(pnt, 3);
        prevPoints.add(new Point3(newPoint));
    }

    public void build() {
        if (prevPoints.size() < 3)
            return;

        points = new MatOfPoint3f(prevPoints.toArray(new Point3[0]));
    }

    public boolean isBuilt() {
        return points != null;
    }
}
