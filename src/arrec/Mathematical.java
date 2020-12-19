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

    public static Mat getPlaneCoefficients(double x1, double y1, double z1,
                                     double x2, double y2, double z2,
                                     double x3, double y3, double z3) {
        Mat planeMat = new Mat(3, 3, CvType.CV_64FC1);
        planeMat.put(0, 0, x1, y1, z1);
        planeMat.put(1, 0, x2, y2, z2);
        planeMat.put(2, 0, x3, y3, z3);

        Mat d = new Mat(3, 1, CvType.CV_64FC1);
        for (int i = 0; i < 3; ++i)
            d.put(i, 0, 1.);

        //System.out.println(planeMat.dump());
        //System.out.println(d.dump());

        Mat gemmMat = new Mat();
        Core.gemm(planeMat.inv(), d, 1, new Mat(), 0, gemmMat);
        //System.out.println("Result: " + gemmMat.dump());

        return gemmMat;
    }

    public static double getZ(double A, double B, double C, double xs, double ys, Mat camMatrix) {
        double fx = camMatrix.get(0, 0)[0];
        double fy = camMatrix.get(1, 1)[0];

        double cx = camMatrix.get(0, 2)[0];
        double cy = camMatrix.get(1, 2)[0];

        double x = (xs - cx) / fx;
        double y = (ys - cy) / fy;

        double zCam = 1. / (A * x + B * y + C);
        return zCam;
    }

}
