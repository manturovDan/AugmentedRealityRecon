package arrec;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.File;

public class UserInterface extends Application implements Runnable {
    private static int height;
    private static int width;
    private static ImageView imgView;
    private static Group root;

    public void setHeight(int _height) {
        height = _height;
    }

    public void setWidth(int _width) {
        width = _width;
    }

    @Override
    public void start(Stage stage) {
        File splashScreen = new File("resources/gf_yoda.png");

        imgView = new ImageView(new Image(splashScreen.toURI().toString()));
        root = new Group(imgView);
        Scene scene = new Scene(root, width, height);

        stage.setScene(scene);
        stage.setTitle("Augmented Reality Recon");

        stage.show();
    }

    @Override
    public void run() {
        if (width <= 0 || height <= 0)
            throw new RuntimeException("Height or width are not set");

        Application.launch();
    }

    public synchronized void updateSnap(Image image) {
        if (imgView != null) {
            imgView.setImage(image);
        }
    }

    public static void main(String[] args) {
        Application.launch();
    }


}
