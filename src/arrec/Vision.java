package arrec;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.*;
import org.opencv.videoio.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Vision implements Runnable {
    private final int camId;
    private final File calibrationFile;
    private Mat camMatrix;
    private Mat dstMatrix;
    private VideoCapture inputVideo;
    private Dictionary dict;
    private boolean isHandling;
    private Mat image;

    private ArrayList<Mat> corners;
    private Mat ids;

    private Mat rvecs;
    private Mat tvecs;

    private VisionResult curResult;

    public void setCamCalibrationFromFile() throws FileNotFoundException {
        CamParamsReader readKfs = new CamParamsReader(calibrationFile);
        readKfs.processKFile();

        camMatrix = readKfs.getCameraMatrix();
        dstMatrix = readKfs.getDstMatrix();
    }

    public Vision(int _camId, File _calibration) {
        camId = _camId;
        calibrationFile = _calibration;
        try {
            setCamCalibrationFromFile();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No Such file");
        }
    }

    public void initForRecognition() {
        inputVideo = new VideoCapture();
        inputVideo.open(camId);
        dict = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250);
        image = new Mat();
    }

    void handleVideo() throws Exception {
        isHandling = true;

        while (isHandling && inputVideo.grab()) {
            inputVideo.retrieve(image);

            corners = new ArrayList<>();
            ids = new Mat();
            Aruco.detectMarkers(image, dict, corners, ids);

            if (ids.size(0) > 0) {
                rvecs = new Mat();
                tvecs = new Mat();

                Aruco.estimatePoseSingleMarkers(corners, 0.13f, camMatrix, dstMatrix, rvecs, tvecs);
            }

            setCalculationResults();

            TimeUnit.MILLISECONDS.sleep(27);
        }
    }

    @Override
    public void run() {
        try {
            handleVideo();
        } catch (Exception ignored) {}
    }

    synchronized void setCalculationResults() {
        curResult = new VisionResult.Builder().image(image).corners(corners).ids(ids).rvecs(rvecs).tvecs(tvecs).build();
    }

    public int getVideoWidth() {
        return image.width();
    }

    public int getVideoHeight() {
        return image.height();
    }

    synchronized VisionResult getResult() {
        return curResult;
    }

    public Mat getCamMatrix() {
        return camMatrix;
    }

    public Mat getDstMatrix() {
        return dstMatrix;
    }
}
