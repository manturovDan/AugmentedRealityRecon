package arrec;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class CamParamsReader {
    private File kFile;

    public CamParamsReader(File camConfYaml) {
        kFile = camConfYaml;
    }

    public void processKFile() throws FileNotFoundException {
        Yaml yaml = new Yaml();

        InputStream targetFile = new FileInputStream(kFile);
        Map<String, Object> obj = yaml.load(targetFile);
        System.out.println(obj);
    }
}
