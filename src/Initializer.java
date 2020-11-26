import arrec.MainController;

import java.io.File;

public class Initializer {
    public static void main(String[] args) {
        System.out.println("Start the project");
        MainController controller = new MainController(2, new File("resources/calibration.yaml"));
        controller.run();
    }
}
