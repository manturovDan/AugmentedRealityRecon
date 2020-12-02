import arrec.MainController;

import java.io.File;

public class Initializer {
    public static void main(String[] args) throws Exception {
        System.out.println("Start the project");
        MainController controller = new MainController(Integer.parseInt(args[0]), new File(args[1]));
        controller.run();
    }
}
