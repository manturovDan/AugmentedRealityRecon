package arrec;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Mathematical {
    public static Mat getRTMat(Mat tvec, Mat rvec) {
        Mat rtMat = new Mat(4, 4, CvType.CV_64FC1);

        Mat rotationMat = new Mat();
        Calib3d.Rodrigues(rvec, rotationMat);

        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 3; ++c) {
                rtMat.put(r, c, rotationMat.get(r, c));
            }
        }

        //System.out.println(tvec.dump());
        for (int i = 0; i < 3; ++i) {
            rtMat.put(i, 3, tvec.get(0, 0)[i]);
            rtMat.put(3, i, 0);
        }
        rtMat.put(3, 3, 1);

        return rtMat;
    }

    public static void getNormalPlaneByPoints(Mat p1, Mat p2, Mat p3, ArrayList<Double> planeCoefs) {
        System.out.println(p1.dump());
        System.out.println(p2.dump());
        System.out.println(p3.dump());

        double a1 = p2.get(0,0)[0] - p1.get(0,0)[0]; //x2 - x1
        double b1 = p2.get(1,0)[0] - p1.get(1,0)[0]; //y2 - y1
        double c1 = p2.get(2,0)[0] - p1.get(2,0)[0]; //z2 - z1
        double a2 = p3.get(0,0)[0] - p1.get(0,0)[0]; //x3 - x1
        double b2 = p3.get(1,0)[0] - p1.get(1,0)[0]; //y3 - y1
        double c2 = p3.get(2,0)[0] - p1.get(2,0)[0]; //z3 - z1

        double a = b1 * c2 - b2 * c1;
        double b = a2 * c1 - a1 * c2;
        double c = a1 * b2 - b1 * a2;
        double d = (- a * p1.get(0,0)[0] - b * p1.get( 1,0)[0] - c * p1.get(2,0)[0]); //(- a * x1 - b * y1 - c * z1)

        planeCoefs.add(0, -a / d);
        planeCoefs.add(1, -b / d);
        planeCoefs.add(2, -c / d);
    }

    static void equation_plane(double x1, double y1, double z1,
                               double x2, double y2, double z2,
                               double x3, double y3, double z3)
    {

        System.out.println(x1 + " " + y1 + " " + z1);
        System.out.println(x2 + " " + y2 + " " + z2);
        System.out.println(x3 + " " + y3 + " " + z3);

        double a1 = x2 - x1;
        double b1 = y2 - y1;
        double c1 = z2 - z1;
        double a2 = x3 - x1;
        double b2 = y3 - y1;
        double c2 = z3 - z1;
        double a = b1 * c2 - b2 * c1;
        double b = a2 * c1 - a1 * c2;
        double c = a1 * b2 - b1 * a2;
        double d = (- a * x1 - b * y1 - c * z1);
        System.out.println("equation of plane is " + a +
                " x + " + b + " y + " + c +
                " z + " + d + " = 0.");
    }

    public static void getPlaneCoefficients(double x1, double y1, double z1,
                                     double x2, double y2, double z2,
                                     double x3, double y3, double z3) {
        Mat planeMat = new Mat(3, 3, CvType.CV_64FC1);
        planeMat.put(0, 0, x1, y1, z1);
        planeMat.put(1, 0, x2, y2, z2);
        planeMat.put(2, 0, x3, y3, z3);

        Mat d = new Mat(3, 1, CvType.CV_64FC1);
        for (int i = 0; i < 3; ++i)
            d.put(i, 0, 1.);

        System.out.println(planeMat.dump());
        System.out.println(d.dump());

        Mat gemmMat = new Mat();
        Core.gemm(planeMat.inv(), d, 1, new Mat(), 0, gemmMat);
        System.out.println("Result: " + gemmMat.dump());
    }

    public static void getNormalPlaneByPoints(ArrayList<Mat> pointsList, ArrayList<Double> planeCoefs) {
        getNormalPlaneByPoints(pointsList.get(0), pointsList.get(1), pointsList.get(2), planeCoefs);
    }
}
