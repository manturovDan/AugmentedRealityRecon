package arrec;

import javafx.scene.image.Image;
import javafx.util.Pair;
import org.opencv.aruco.Aruco;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class Renderer {
    private Model3D model3d;

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

    public void drawModel(Model3D model, Mat image, Mat camMatrix, Mat dstMatrix, Mat rvec, Mat tvec) {
        model3d = model;
        ArrayList<Pair<Polygon, MatOfPoint2f>> renderQueue = new ArrayList<>();

        for (Polygon poly : model.getPolygons()) {
            MatOfPoint2f points2f = new MatOfPoint2f();
            MatOfPoint3f pointsToProject = poly.getPoints();

            Calib3d.projectPoints(pointsToProject, rvec, tvec, camMatrix, new MatOfDouble(dstMatrix), points2f, new Mat());

            if (!isVisible(points2f))
                continue;

            renderQueue.add(new Pair<>(poly, points2f));
        }

        renderQueue = correctRenderOrder(renderQueue);

        drawQueue(image, renderQueue);
    }

    private void drawQueue(Mat image, ArrayList<Pair<Polygon, MatOfPoint2f>> renderQueue) {
        //System.out.println("painting");
        for (Pair<Polygon, MatOfPoint2f> polyWithProj : renderQueue) {
            MatOfPoint2f points2f = polyWithProj.getValue();
            ArrayList<MatOfPoint> pointsList = new ArrayList<>();
            pointsList.add(
                new MatOfPoint (
                        points2f.toArray()[0],
                        points2f.toArray()[1],
                        points2f.toArray()[2],
                        points2f.toArray()[3]
                )
            );

            Imgproc.fillPoly (
                image,
                pointsList,
                polyWithProj.getKey().getColor()
            );

            //System.out.println("painted: " + polyWithProj.getKey().getFace());
        }
    }

    private boolean isVisible(MatOfPoint2f points2f) {
        Mat vec20 = new Mat(1, 3, CvType.CV_64FC1);
        Mat vec31 = new Mat(1, 3, CvType.CV_64FC1);

        vec20.put(0, 0, points2f.toArray()[2].x - points2f.toArray()[0].x,
                points2f.toArray()[2].y - points2f.toArray()[0].y, 0);

        vec31.put(0, 0, points2f.toArray()[3].x - points2f.toArray()[1].x,
                points2f.toArray()[3].y - points2f.toArray()[1].y, 0);

        Mat cross = vec20.cross(vec31);


        return cross.get(0, 2)[0] > 0;
    }

    private ArrayList<Pair<Polygon, MatOfPoint2f>> correctRenderOrder(ArrayList<Pair<Polygon, MatOfPoint2f>> renderQueue) {
        //System.out.println("before: ");
        //renderQueue.forEach(e -> System.out.print(e.getKey().getFace() + " "));
        //System.out.println();

        for (Pair<ArrayList<Integer>, ArrayList<Integer>> correction : model3d.getRenderCorrections()) {
            ArrayList<Integer> idxesOfBacks = new ArrayList<>();
            int countOfAppeared = 0;

            int polyIdx = 0;
            for (Pair<Polygon, MatOfPoint2f> renderPoly : renderQueue) {
                Polygon poly = renderPoly.getKey();

                if (correction.getKey().contains(poly.getFace()) || correction.getKey().contains(-poly.getFace())) {
                    countOfAppeared++;
                }else if (correction.getValue().contains(poly.getFace())) {
                    idxesOfBacks.add(polyIdx);
                }

                polyIdx++;
            }

            if (countOfAppeared == correction.getKey().size() && correction.getKey().get(0) > 0 ||
                    countOfAppeared == 0 && correction.getKey().get(0) < 0) {
                ArrayList<Pair<Polygon, MatOfPoint2f>> newRenderQueue = new ArrayList<>(renderQueue.size());

                for (Integer i : idxesOfBacks) {
                    newRenderQueue.add(renderQueue.get(i));
                }

                for (int itl = 0; itl < renderQueue.size(); ++itl) {
                    if (!idxesOfBacks.contains(itl)) {
                        newRenderQueue.add(renderQueue.get(itl));
                    }
                }

                renderQueue = newRenderQueue;
            }
        }

        //renderQueue.forEach(e -> System.out.print(e.getKey().getFace() + " "));
        //System.out.println();

        return renderQueue;
    }

    @DebugAnno
    public void drawAxis(Mat image, Mat camMatrix, Mat dstMatrix, Mat rvec, Mat tvec) {
        Mat curTvec = new Mat();
        tvec.copyTo(curTvec);

        double[] curTvecVal = curTvec.get(0, 0);
        //curTvecVal[0] += 0.1;

        Aruco.drawAxis(image, camMatrix, dstMatrix, rvec, curTvec, 0.1f);
    }
}
