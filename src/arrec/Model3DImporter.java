package arrec;

import java.io.*;
import java.util.ArrayList;

import org.json.*;
import org.opencv.core.Scalar;

public class Model3DImporter {
    private File jsonModelFile;

    public Model3DImporter(File modelFile) {
        jsonModelFile = modelFile;
    }

    public Model3DImporter(String pathToModel) {
        jsonModelFile = new File(pathToModel);
    }

    private String readModelFile() {
        StringBuilder resModelStr = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(jsonModelFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                resModelStr.append(line).append("\n");
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading model file");
        }

        return resModelStr.toString();
    }

    public ArrayList<Polygon> build() {
        ArrayList<Polygon> Model3D = new ArrayList<>();
        String jsonData = readModelFile();
        JSONObject jObj = new JSONObject(jsonData);

        JSONArray polygons = jObj.getJSONArray("polygons");
        for (int i = 0; i < polygons.length(); ++i) {
            JSONObject poly = polygons.getJSONObject(i);

            JSONArray points = poly.getJSONArray("points");
            JSONArray color = poly.getJSONArray("color");

            Scalar colorCV = new Scalar(color.getDouble(0), color.getDouble(1), color.getDouble(2));
            Polygon polyCV = new Polygon(colorCV);

            for (int p = 0; p < points.length(); ++p) {
                JSONArray onePoint = points.getJSONArray(p);
                polyCV.addPoint(new double[] { onePoint.getDouble(0), onePoint.getDouble(1), onePoint.getDouble(2), 1.} );
            }

            polyCV.setOdd(poly.getBoolean("odd"));
            polyCV.build();

            if (!polyCV.isBuilt())
                throw new RuntimeException("You have an error in 3d model syntax. Check Polygon " + i );

            Model3D.add(polyCV);
        }

        return Model3D;
    }
}
