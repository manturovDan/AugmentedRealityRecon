import arrec.MainController;

public class Initializer {
    public static void main(String[] args) {
        System.out.println("Start the project");
        MainController controller = new MainController(2, null);
        controller.run();
    }
}
