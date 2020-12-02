package arrec;

import javafx.util.Pair;

import java.util.ArrayList;

public class Model3D {
    private ArrayList<Polygon> polygons;
    private ArrayList<Pair<ArrayList<Integer>, ArrayList<Integer>>> renderCorrections;

    public Model3D(ArrayList<Polygon> _poly, ArrayList<Pair<ArrayList<Integer>, ArrayList<Integer>>> _renderCorrections) {
        polygons = _poly;
        renderCorrections = _renderCorrections;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public ArrayList<Pair<ArrayList<Integer>, ArrayList<Integer>>> getRenderCorrections() {
        return renderCorrections;
    }

    @Override
    public String toString() {
        StringBuilder retStr = new StringBuilder();
        retStr.append("polygons: \n");
        polygons.forEach(retStr::append);
        retStr.append("corrections ([if_visible]=[render_last]): \n");
        renderCorrections.forEach(retStr::append);

        return retStr.toString();
    }
}
