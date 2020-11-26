package arrec;

import java.io.File;

public class MainController {
    public MainController(int camId, File camCalibration) {}

    public void run() {
        UserInterface gui = new UserInterface();
        gui.setHeight(500);
        gui.setWidth(700);
        gui.run();
    }
}
