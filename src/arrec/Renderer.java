package arrec;

import javafx.scene.image.Image;
import javafx.util.Pair;
import org.opencv.aruco.Aruco;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.*;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

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

    private double[] getCamCoords(Mat rtMat, double[] pointGlobal) {
        Mat coordsMat = new Mat(4, 1, CvType.CV_64FC1);
        coordsMat.put(3, 0, 1);
        double[] camPoints = new double[3];

        Mat gemmMat = new Mat();
        coordsMat.put(0, 0, pointGlobal);
        Core.gemm(rtMat, coordsMat, 1, new Mat(), 0, gemmMat);
        for (int j = 0; j < 3; ++j) {
            camPoints[j] = gemmMat.get(j, 0)[0];
        }

        return camPoints;
    }

    public void rasterization(Model3D model, Mat image, Mat camMatrix, Mat dstMatrix, Mat rvec, Mat tvec) {
        model3d = model;
        Mat rtMat = Mathematical.getRTMat(tvec, rvec);
        double[][] depth = initDepth(image);

        for (Polygon poly : model.getPolygons()) {
            MatOfPoint2f vertices2f = new MatOfPoint2f();
            MatOfPoint3f verticesPointsToProject = poly.getPoints();
            Point[] projections = new Point[verticesPointsToProject.rows()];
            Point3[] camCoordinates = new Point3[verticesPointsToProject.rows()];
            for (int v = 0; v < verticesPointsToProject.rows(); ++v) {
                Point3 vertice = new Point3(verticesPointsToProject.get(v, 0));
                double[] vCamCoords = new double[3];
                int[] vProjCoords = new int[2];
                projectPointAndGetCameraCoordinates(rtMat, camMatrix, vertice, vCamCoords, vProjCoords);
                projections[v] = new Point(vProjCoords[0], vProjCoords[1]);
                camCoordinates[v] = new Point3(vCamCoords[0], vCamCoords[1], vCamCoords[2]);
            }

            vertices2f.fromArray(projections);

            if (!isVisible(vertices2f))
                continue;

            //for (int v = 0; v < 3; ++v) {
            //    Imgproc.circle(image, new Point(projections[v].x, projections[v].y),
            //            5, new Scalar(0, 0, 255), -1);
            //}


            //System.out.println("plane vertices: " + Arrays.toString(camCoordinates));
            Mat plane = Mathematical.getPlaneCoefficients(camCoordinates[0].x, camCoordinates[0].y, camCoordinates[0].z,
                                        camCoordinates[1].x, camCoordinates[1].y, camCoordinates[1].z,
                                        camCoordinates[2].x, camCoordinates[2].y, camCoordinates[2].z); //Ax+By+Cz=1

            double A = plane.get(0, 0)[0];
            double B = plane.get(1, 0)[0];
            double C = plane.get(2, 0)[0];



            for (int x = 0; x < image.cols(); ++x) {
                for (int y = 0; y < image.rows(); ++y) {
                    if (isPointInsideTriangle(new Point(x, y), projections[0], projections[1], projections[2]) ||
                            isPointInsideTriangle(new Point(x, y), projections[0], projections[3], projections[2])) {

                        //Imgproc.circle(image, new Point(x, y),
                         //       1, poly.getColor(),-1);

                        double currentZ = Mathematical.getZ(A, B, C, x, y, camMatrix);

                        if (depth[y][x] >= currentZ) {
                            depth[y][x] = currentZ;

                            Imgproc.circle(image, new Point(x, y),
                                    1, poly.getColor(),-1);

                            //image.put(y, x, poly.getColor().val[0], poly.getColor().val[1], poly.getColor().val[2]);
                        }

                    }
                }
            }

            /*MatOfPoint3f allPointsToProject = poly.getInternalPoints();
            for (Point3 point : allPointsToProject.toArray()) {
                double[] camCoords = new double[3];
                int[] projCoords = new int[2];
                projectPointAndGetCameraCoordinates(rtMat, camMatrix, point, camCoords, projCoords);

                if(projCoords[0] >= image.cols() || projCoords[1] >= image.rows() || projCoords[0] < 0 || projCoords[1] < 0)
                    continue;

                if (depth[projCoords[1]][projCoords[0]] >= camCoords[2]) {
                    depth[projCoords[1]][projCoords[0]] = camCoords[2];

                    Imgproc.circle(image, new Point(projCoords[0], projCoords[1]),
                            1, poly.getColor(),-1);
                }
            }*/

        }
    }

    private double triangleArea(Point p0, Point p1, Point p2) {
        return Math.abs((p0.x * (p1.y - p2.y) + p1.x * (p2.y - p0.y) + p2.x * (p0.y - p1.y)) / 2.0);
    }

    private boolean isPointInsideTriangle(Point p, Point p0, Point p1, Point p2) {
        if (Double.compare(p.x, Math.max(Math.max(p0.x, p1.x), p2.x)) > 0 ||
                Double.compare(p.y, Math.max(Math.max(p0.y, p1.y), p2.y)) > 0 ||
                Double.compare(p.x, Math.min(Math.min(p0.x, p1.x), p2.x)) < 0 ||
                Double.compare(p.y, Math.min(Math.min(p0.y, p1.y), p2.y)) < 0
        )
            return false;

        double pp0p1 = triangleArea(p, p0, p1);
        double pp0p2 = triangleArea(p, p0, p2);
        double pp1p2 = triangleArea(p, p1, p2);
        double p0p1p2 = triangleArea(p0, p1, p2);
        return Double.compare(pp0p1 + pp1p2 + pp0p2, p0p1p2) == 0;
    }

    private void projectPointAndGetCameraCoordinates(Mat rtMat, Mat camMatrix, Point3 pointGlobal, double[] camCoords, int[] projCoords) {
        double[] getCamCoords = getCamCoords(rtMat, new double[] {pointGlobal.x, pointGlobal.y, pointGlobal.z});
        camCoords[0] = getCamCoords[0];
        camCoords[1] = getCamCoords[1];
        camCoords[2] = getCamCoords[2];


        int xPx = (int) (camCoords[0] * camMatrix.get(0, 0)[0] / camCoords[2] + camMatrix.get(0, 2)[0]);
        int yPx = (int) (camCoords[1] * camMatrix.get(1, 1)[0] / camCoords[2] + camMatrix.get(1, 2)[0]);

        projCoords[0] = xPx;
        projCoords[1] = yPx;
    }


    private int minCoordinate(int[] point0, int[] point1, int[] point2, int axis) {
        return Math.min(Math.min(point0[axis], point1[axis]), point2[axis]);
    }

    private int maxCoordinate(int[] point0, int[] point1, int[] point2, int axis) {
        return Math.max(Math.max(point0[axis], point1[axis]), point2[axis]);
    }

    private double[][] initDepth(Mat image) {
        double[][] depth = new double[image.rows()][image.cols()];
        for (double[] row : depth) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }
        return depth;
    }

    public void drawModel(Model3D model, Mat image, Mat camMatrix, Mat dstMatrix, Mat rvec, Mat tvec) {
        model3d = model;
        ArrayList<Pair<Polygon, MatOfPoint2f>> renderQueue = new ArrayList<>();

        Mat rtMat = Mathematical.getRTMat(tvec, rvec);

        //System.out.println(rtMat.dump());

        ArrayList<Mat> planePoints = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("####0.0000000");

        for (Polygon poly : model.getPolygons()) {
            MatOfPoint2f points2f = new MatOfPoint2f();
            MatOfPoint3f pointsToProject = poly.getPoints();
            Calib3d.projectPoints(pointsToProject, rvec, tvec, camMatrix, new MatOfDouble(0, 0, 0, 0, 0), points2f, new Mat());

            if (!isVisible(points2f))
                continue;

            Mat coordsMat = new Mat(4, 1, CvType.CV_64FC1);
            coordsMat.put(3, 0, 1);

            double[][] points = new double[3][3];
            for (int i = 0; i < 3; ++i) {
                Mat gemmMat = new Mat();
                coordsMat.put(0, 0, pointsToProject.get(i, 0));
                Core.gemm(rtMat, coordsMat, 1, new Mat(), 0, gemmMat);
                for (int j = 0; j < 3; ++j) {
                    points[i][j] = gemmMat.get(j, 0)[0];
                }

                Imgproc.circle(image,
                        new Point(points2f.get(i, 0)[0], points2f.get(i, 0)[1]),
                        5,
                        new Scalar(255, 0, 0),
                        -1);

                Imgproc.putText(image,
                        "----> " + points[i][0] +
                                " " + points[i][1] +
                                " " + points[i][2],
                        new Point(points2f.get(i, 0)[0], points2f.get(i, 0)[1]),
                        5,
                        1.,
                        new Scalar(255, 0, 0));


            }
/*
            System.out.println("#");
            System.out.println(new Point(points[0].get(0,0)[0] / points[0].get(2,0)[0] ,
                    points[0].get(1,0)[0] / points[0].get(2,0)[0]));
            System.out.println(new Point(points[1].get(0,0)[0] / points[1].get(2,0)[0] ,
                    points[1].get(1,0)[0] / points[1].get(2,0)[0]));

            Imgproc.line(image, new Point(points[0].get(0,0)[0] / points[0].get(2,0)[0] ,
                            points[0].get(1,0)[0] / points[0].get(2,0)[0]),        //p1

                    new Point(points[1].get(0,0)[0] / points[1].get(2,0)[0] ,
                            points[1].get(1,0)[0] / points[1].get(2,0)[0]),       //p2
                    new Scalar(0, 0, 255),     //Scalar object for color
                    5 );
*/
            ArrayList<Double> planeCoefs = new ArrayList<>();
            //Mathematical.getNormalPlaneByPoints(planePoints, planeCoefs);

            System.out.println("Plane coefs:");
            System.out.println(planeCoefs);

            renderQueue.add(new Pair<>(poly, points2f));
            break;
        }

        //renderQueue = correctRenderOrder(renderQueue);

        //drawQueue(image, renderQueue);
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

            if (countOfAppeared == correction.getKey().size() && correction.getKey().get(0) >= 0 ||
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

        MatOfPoint2f points2f = new MatOfPoint2f();
        System.out.println(tvec.dump());
        System.out.println(new MatOfPoint3f(new Point3(tvec.get(0, 0))).dump());
        Calib3d.projectPoints(new MatOfPoint3f(new Point3(tvec.get(0,0))), rvec, tvec, camMatrix, new MatOfDouble(dstMatrix), points2f, new Mat());

        System.out.println(points2f.dump());

        Imgproc.circle(
                image,
                new Point(points2f.get(0, 0)[0] / 10, -points2f.get(0, 0)[1] / 10),
                2,
                new Scalar(255, 0, 0)
        );
    }
}
