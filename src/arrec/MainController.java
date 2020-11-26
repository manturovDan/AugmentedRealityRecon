package arrec;

import org.opencv.core.Core;

import java.io.File;

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

    public void run() {
        Vision vision = new Vision(camIdx, calibrationFile);

        UserInterface gui = new UserInterface();
        gui.setHeight(500);
        gui.setWidth(700);
        gui.run();
    }
}
