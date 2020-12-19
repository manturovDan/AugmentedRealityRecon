import arrec.MainController;

import java.io.File;

public class Initializer {
    public static void main(String[] args) throws Exception {
        System.out.println("Start the project");
        boolean raster = false;
        if(args.length == 3 && args[2].equals("rasterization"))
            raster = true;

        MainController controller = new MainController(Integer.parseInt(args[0]), new File(args[1]), raster);
        controller.run();
    }
}
