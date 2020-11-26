package arrec;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.*;
import org.opencv.videoio.*;

import java.io.File;
import java.io.FileNotFoundException;

public class Vision implements Runnable {
    private final int camId;
    private final File calibrationFile;
    private Mat camMatrix;
    private Mat dstMatrix;
    private VideoCapture inputVideo;
    private Dictionary dict;
    private boolean isHandling;

    private Mat image;
    private Mat imageToOut;

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
        imageToOut = new Mat();
    }

    void handleVideo() {
        isHandling = true;
        while (isHandling && inputVideo.grab()) {
            inputVideo.retrieve(image);
            setImageToOut();
        }
    }

    @Override
    public void run() {
        handleVideo();
    }

    synchronized void setImageToOut() {
        image.copyTo(imageToOut);
    }

    synchronized Mat getImageToOut() {
        return imageToOut;
    }

    public int getVideoWidth() {
        return image.width();
    }

    public int getVideoHeight() {
        return image.height();
    }
}
