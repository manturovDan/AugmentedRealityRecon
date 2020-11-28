package arrec;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class VisionResult {
    private Mat image;
    private ArrayList<Mat> corners;
    private Mat ids;
    private Mat rvecs;
    private Mat tvecs;

    private VisionResult(Mat _image, ArrayList<Mat> _corners, Mat _ids, Mat _rvecs, Mat _tvecs) {
        image = _image;
        corners = _corners;
        ids = _ids;
        rvecs = _rvecs;
        tvecs = _tvecs;
    }

    public Mat getImage() {
        return image;
    }

    public ArrayList<Mat> getCorners() {
        return corners;
    }

    public Mat getIds() {
        return ids;
    }

    public Mat getRvecs() {
        return rvecs;
    }

    public Mat getTvecs() {
        return tvecs;
    }

    public static class Builder {
        private Mat image;
        private ArrayList<Mat> corners;
        private Mat ids;
        private Mat rvecs;
        private Mat tvecs;

        public Builder image(Mat _image) {
            image = new Mat();
            _image.copyTo(image);
            return this;
        }

        public Builder corners(ArrayList<Mat> _corners) {
            corners = new ArrayList<>();
            for (Mat c : _corners) {
                corners.add(new Mat());
                c.copyTo(corners.get(corners.size() - 1));
            }
            return this;
        }

        public Builder ids(Mat _ids) {
            ids = new Mat();
            _ids.copyTo(ids);
            return this;
        }

        public Builder rvecs(Mat _rvecs) {
            if (_rvecs != null) {
                rvecs = new Mat();
                _rvecs.copyTo(rvecs);
            }
            return this;
        }

        public Builder tvecs(Mat _tvecs) {
            if (_tvecs != null) {
                tvecs = new Mat();
                _tvecs.copyTo(tvecs);
            }
            return this;
        }

        public VisionResult build() {
            return new VisionResult(image, corners, ids, rvecs, tvecs);
        }
    }
}
