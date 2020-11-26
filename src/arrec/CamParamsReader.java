package arrec;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class CamParamsReader {
    private File kFile;
    private Mat cameraMatrix;
    private Mat dstMatrix;

    public CamParamsReader(File camConfYaml) {
        kFile = camConfYaml;
    }

    public Mat getCameraMatrix() {
        return cameraMatrix;
    }

    public Mat getDstMatrix() {
        return dstMatrix;
    }

    public void processKFile() throws FileNotFoundException {
        Yaml yaml = new Yaml();

        InputStream targetFile = new FileInputStream(kFile);
        Map<String, List<List<Double>>> readCoefs = yaml.load(targetFile);

        double[] camMatrix = new double[9];
        fillArray(camMatrix, readCoefs.get("camera_matrix"));
        cameraMatrix = new Mat(3, 3, CvType.CV_64FC1);
        cameraMatrix.put(0, 0, camMatrix);

        double[] distMatrix = new double[5];
        fillArray(distMatrix, readCoefs.get("dist_coeff"));
        dstMatrix = new Mat(1, 5, CvType.CV_64FC1);
        dstMatrix.put(0, 0, distMatrix);
    }

    private void fillArray(double[] destination, List<List<Double>> source) {
        int i = 0;
        for (List<Double> row : source) {
            for (Double val : row) {
                destination[i++] = val;
            }
        }
    }
}
