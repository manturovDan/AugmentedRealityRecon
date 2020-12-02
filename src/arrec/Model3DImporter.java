package arrec;

import java.io.*;
import java.util.ArrayList;

import javafx.util.Pair;
import org.json.*;
import org.opencv.core.Scalar;

public class Model3DImporter {
    private final File jsonModelFile;

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

    public Model3D build() {
        ArrayList<Polygon> Model3DPoly = new ArrayList<>();
        String jsonData = readModelFile();
        JSONObject jObj = new JSONObject(jsonData);

        JSONArray polygons = jObj.getJSONArray("polygons");
        for (int i = 0; i < polygons.length(); ++i) {
            JSONObject poly = polygons.getJSONObject(i);

            JSONArray points = poly.getJSONArray("points");
            JSONArray color = poly.getJSONArray("color");
            int face = poly.getInt("face");

            Scalar colorCV = new Scalar(color.getDouble(0), color.getDouble(1), color.getDouble(2));
            Polygon polyCV = new Polygon(colorCV, face);

            for (int p = 0; p < points.length(); ++p) {
                JSONArray onePoint = points.getJSONArray(p);
                polyCV.addPoint(new double[] { onePoint.getDouble(0), onePoint.getDouble(1), onePoint.getDouble(2), 1.} );
            }

            polyCV.build();

            if (!polyCV.isBuilt())
                throw new RuntimeException("You have an error in 3d model syntax. Check Polygon " + i );

            Model3DPoly.add(polyCV);
        }

        ArrayList<Pair<ArrayList<Integer>, ArrayList<Integer>>> renderCorrectionsCV = new ArrayList<>();
        JSONArray render_correction = jObj.getJSONArray("render_correction");
        for (int rc = 0; rc < render_correction.length(); ++rc) {
            JSONObject correct = render_correction.getJSONObject(rc);

            JSONArray if_visible = correct.getJSONArray("if_visible");
            JSONArray render_last = correct.getJSONArray("render_last");

            Pair<ArrayList<Integer>, ArrayList<Integer>> oneCorrection = new Pair<>(getAlFromJSONArr(if_visible), getAlFromJSONArr(render_last));
            renderCorrectionsCV.add(oneCorrection);
        }

        Model3D model = new Model3D(Model3DPoly, renderCorrectionsCV);
        //System.out.println(model);
        return model;
    }

    private ArrayList<Integer> getAlFromJSONArr(JSONArray jArr) {
        ArrayList<Integer> newAL = new ArrayList<>();
        for (int i = 0; i < jArr.length(); ++i) {
            newAL.add(jArr.getInt(i));
        }

        return newAL;
    }
}
