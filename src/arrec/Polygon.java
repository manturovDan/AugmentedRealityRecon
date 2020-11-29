package arrec;

import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Scalar;

public class Polygon {
    private MatOfPoint3f points;
    private Scalar color;

    public Polygon(MatOfPoint3f pointsArr, Scalar _color) {
        points = pointsArr;
        color = _color;
    }
}
