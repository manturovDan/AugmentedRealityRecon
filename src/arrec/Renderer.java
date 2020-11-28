package arrec;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;

public class Renderer {

    public void printMarkersInfo(VisionResult result) {
        if (result == null) {
            System.out.println("null result");
            return;
        }

        for (int i = 0; i < result.getIds().size(0); ++i) {
            System.out.print("(" + i + ")\nCorners: ");
            result.getCorners().forEach(e -> System.out.println(e.dump()));

            System.out.print("Rvecs: ");
            if (result.getRvecs() != null)
                System.out.print(result.getRvecs().dump());

            System.out.print("Tvecs: ");
            if (result.getTvecs() != null)
                System.out.print(result.getTvecs().dump());

            System.out.println();
        }
    }

    public Image MatToImg(Mat image) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", image, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }
}
