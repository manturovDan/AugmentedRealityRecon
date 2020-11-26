package arrec;

import org.opencv.core.*;

import java.io.File;
import java.io.FileNotFoundException;

public class Vision {
    private int camId;
    private File calibrationFile;
    private Mat camMatrix;
    private Mat dstMatrix;

    public void setCamCalibrationFromFile() throws FileNotFoundException {
        CamParamsReader readKfs = new CamParamsReader(calibrationFile);
        readKfs.processKFile();
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
}
