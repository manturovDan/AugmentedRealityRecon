package arrec;

import org.opencv.core.Core;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainController {
    static {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
    }

    private final File calibrationFile;
    private int camIdx;

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

        Model3DImporter mImporter = new Model3DImporter("resources/grogu_v.json");
        Model3D showingModel = mImporter.build();

        //System.out.print(showingModel);

        VisionResult snapshot;

        while (!UserInterface.getClosed()) {
            snapshot = vision.getResult();
            if (snapshot == null) { //is not loaded yet
                TimeUnit.MILLISECONDS.sleep(100);
                continue;
            }

            if (snapshot.getRvecs() != null && snapshot.getTvecs() != null) {
                //renderer.drawAxis(snapshot.getImage(), vision.getCamMatrix(), vision.getDstMatrix(), snapshot.getRvecs().row(0), snapshot.getTvecs().row(0));
                renderer.drawModel(showingModel, snapshot.getImage(), vision.getCamMatrix(), vision.getDstMatrix(), snapshot.getRvecs().row(0), snapshot.getTvecs().row(0));
            }

            gui.updateSnap(renderer.MatToImg(snapshot.getImage()));

            TimeUnit.MILLISECONDS.sleep(30);
        }

        guiThread.interrupt();
        vision.noHandle();
        visThread.join();

    }
}
