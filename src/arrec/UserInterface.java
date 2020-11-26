package arrec;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

public class UserInterface extends Application implements Runnable {
    private static int height;
    private static int width;
    private static ImageView imgView;

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
        Group root = new Group(imgView);
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

    public synchronized void updateSnap(Mat image) {
        if (imgView != null) {
            imgView.setImage(MatToImg(image));
        }
    }

    public static void main(String[] args) {
        Application.launch();
    }

    private Image MatToImg(Mat image) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", image, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }
}
