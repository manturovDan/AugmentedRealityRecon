package arrec;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.shape.Sphere;
import org.opencv.aruco.Aruco;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class Renderer {

    public void printMarkersInfo(VisionResult result) {
        if (result == null) {
            System.out.println("null result");
            return;
        }

        for (int i = 0; i < result.getIds().size(0); ++i) {
            System.out.print("(" + i + ")\nCorners: ");
            result.getCorners().forEach(e -> System.out.println(e.dump()));

            System.out.print("Rvecs: ");
            if (result.getRvecs() != null)
                System.out.print(result.getRvecs().dump());

            System.out.print("Tvecs: ");
            if (result.getTvecs() != null)
                System.out.print(result.getTvecs().dump());

            System.out.println();
        }
    }

    public Image MatToImg(Mat image) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", image, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    public void drawAxis(Mat image, Mat camMatrix, Mat dstMatrix, Mat rvec, Mat tvec) {
        Mat curTvec = new Mat();
        tvec.copyTo(curTvec);

        double[] curTvecVal = curTvec.get(0, 0);
        //curTvecVal[0] += 0.1;

        Imgproc.circle (
                image,                 //Matrix obj of the image
                new Point(230, 160),    //Center of the circle
                0,                    //Radius
                new Scalar(0, 0, 255),  //Scalar object for color
                10                      //Thickness of the circle
        );

        MatOfPoint3f pointsToProject = new MatOfPoint3f(new Point3(new double[] { 0., 0., .15, 1}));

        MatOfPoint2f points2f = new MatOfPoint2f();
        Mat jacobian = new Mat();

        Calib3d.projectPoints(pointsToProject, rvec, tvec, camMatrix, new MatOfDouble(dstMatrix), points2f, jacobian);

        Imgproc.circle (
                image,                 //Matrix obj of the image
                points2f.toArray()[0],    //Center of the circle
                0,                    //Radius
                new Scalar(0, 255, 255),  //Scalar object for color
                10                      //Thickness of the circle
        );

        System.out.println(points2f.dump());
        curTvec.put(0, 0, curTvecVal);

        //System.out.println(tvec.dump());

        Aruco.drawAxis(image, camMatrix, dstMatrix, rvec, curTvec, 0.1f);
    }
}
