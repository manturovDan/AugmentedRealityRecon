package arrec;

import org.opencv.core.Core;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainController {
    static {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
    }

    private File calibrationFile;
    private int camIdx = 0;

    public MainController(int camId, File camCalibration) {
        calibrationFile = camCalibration;
        camIdx = camId;
    }

    public void run() throws Exception {
        Vision vision = new Vision(camIdx, calibrationFile);
        vision.initForRecognition();
        Thread visThread = new Thread(vision);
        visThread.start();

        UserInterface gui = new UserInterface();

        while (vision.getVideoWidth() <= 0 || vision.getVideoHeight() <= 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        gui.setHeight(vision.getVideoHeight());
        gui.setWidth(vision.getVideoWidth());
        Thread guiThread = new Thread(gui);
        guiThread.start();

        Renderer renderer = new Renderer();

        VisionResult snapshot;
        while (true) {
            snapshot = vision.getResult();
            if (snapshot == null) { //is not loaded yet
                TimeUnit.MILLISECONDS.sleep(100);
                continue;
            }

            gui.updateSnap(renderer.MatToImg(snapshot.getImage()));

            renderer.printMarkersInfo(snapshot);
        }
    }
}
