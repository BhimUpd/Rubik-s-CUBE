package np.nicolai.rubikscube;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity{
    View[] eachColorPerFace=new View[9];
    List<Scalar> colors;
    List<float[]> standardColors=Arrays.asList(
            new float[]{255, 0, 0},    // Red
            new float[]{0, 255, 0},    // Green
            new float[]{0, 0, 255},    // Blue
            new float[]{255, 255, 0},  // Yellow
            new float[]{255, 255, 255},// White
            new float[]{255, 165, 0}  // Orange
    );
    List<float[]> trainingData = Arrays.asList(
            new float[]{255, 0, 0}, new float[]{158, 41,42}, new float[]{115, 29, 30},
            new float[]{0, 255, 0}, new float[]{19, 173, 42}, new float[]{16, 144, 36},
            new float[]{0, 0, 255}, new float[]{19, 97, 162}, new float[]{20, 80, 128},
            new float[]{255, 255, 0}, new float[]{196, 216, 42}, new float[]{145, 173, 28},
            new float[]{255, 255, 255}, new float[]{195, 200, 180}, new float[]{152, 161, 141},
            new float[]{255, 165, 0}, new float[]{227, 96, 75}, new float[]{252, 123, 101}
    );

    List<Integer> labels = Arrays.asList(0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5);//"Red", "Green", "Blue", "Yellow", "White", "Orange"

    int CAMERA_REQUST_CODE=102;
    CameraBridgeViewBase cameraBridgeViewBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        if(!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "Error Loading OpenCV", Toast.LENGTH_SHORT).show();
            return;
        }
        int[] colorBoxIds = {
                R.id.colorBox1, R.id.colorBox2, R.id.colorBox3,
                R.id.colorBox4, R.id.colorBox5, R.id.colorBox6,
                R.id.colorBox7, R.id.colorBox8, R.id.colorBox9
        };
        for(int i=0; i<9; i++)
            eachColorPerFace[i] = findViewById(colorBoxIds[i]);

        Mat trainingDataMat=new Mat(trainingData.size(),3, CvType.CV_32F);
        Mat labelsMat=new Mat(trainingData.size(),1,CvType.CV_32S);//only 1 column for red
        for (int i = 0; i < trainingData.size(); i++) {
            trainingDataMat.put(i, 0, trainingData.get(i)[0]);  // Red channel
            trainingDataMat.put(i, 1, trainingData.get(i)[1]);  // Green channel
            trainingDataMat.put(i, 2, trainingData.get(i)[2]);  // Blue channel
            labelsMat.put(i,0,labels.get(i));//since there is only 1 column for labels
        }
        KNearest knn = KNearest.create();
        knn.train(trainingDataMat, Ml.ROW_SAMPLE, labelsMat);

        cameraBridgeViewBase=findViewById(R.id.camera);
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {            }
            @Override
            public void onCameraViewStopped() {            }
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat rgba = inputFrame.rgba();
                Mat original_rgba = rgba.clone();
                Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(rgba, rgba, new Size(5, 5), 1.5, 1.5);
                Imgproc.Canny(rgba, rgba, 100, 100);
                Imgproc.dilate(rgba, rgba, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(rgba, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                for(int i=0; i<contours.size();i++){
                    if(isSquareContour(contours.get(i))) {
                        Imgproc.drawContours(original_rgba, contours, i, new Scalar(50, 255, 90), 5);
                        colors = extractCubeColors(original_rgba, contours.get(i));
                        for(int k=0; k<9; k++){
                            Scalar color = colors.get(k);
                            int red = (int) color.val[2];
                            int green = (int) color.val[1];
                            int blue = (int) color.val[0];
                            float[] detectedColor = new float[]{red, green, blue};
                            Mat testSample = new Mat(1, 3, CvType.CV_32F);
                            testSample.put(0, 0, detectedColor);
                            Mat result = new Mat();//to store predicted labels only 1
                            Mat neighbors = new Mat();//labels of the K closest neighbors
                            Mat dist = new Mat();//stores the distances to the K nearest neighbors
                            knn.findNearest(testSample, 3, result, neighbors, dist);
                            int label_index=(int) result.get(0, 0)[0];
                            float[] predicted_color = getPredictedColor(label_index);
                            red= (int) predicted_color[0];
                            green= (int) predicted_color[1];
                            blue= (int) predicted_color[2];
                            int androidColor = Color.rgb(red, green, blue);
                            eachColorPerFace[k].setBackgroundColor(androidColor);
                        }
                    }
                }
                return original_rgba;
            }
        });
        cameraBridgeViewBase.enableView();


    }

private float[] getPredictedColor(int i){
        return standardColors.get(i);
}

    private List<Scalar> extractCubeColors(Mat inputImage, MatOfPoint squareContour) {
        Imgproc.cvtColor(inputImage,inputImage,Imgproc.COLOR_BGR2RGB);
        List<Scalar> colors = new ArrayList<>();
        Rect boundingRect = Imgproc.boundingRect(squareContour);
        int padding = 15;
        int squareWidth = (boundingRect.width - 2 * padding) / 3;
        int squareHeight = (boundingRect.height - 2 * padding) / 3;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int startX = boundingRect.x + padding + col * squareWidth;
                int startY = boundingRect.y + padding + row * squareHeight;
                Rect cellRect = new Rect(startX, startY, squareWidth, squareHeight);
                Mat cellROI = inputImage.submat(cellRect);
                Scalar avgColor = Core.mean(cellROI);
                colors.add(avgColor);
            }
        }
        return colors;
    }

    private boolean isSquareContour(MatOfPoint contour) {
        MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
        double epsilon = 0.02 * Imgproc.arcLength(contour2f, true);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);
        if (approxCurve.total() != 4) {
            return false;
        }
        if (!Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))) {
            return false;
        }
        Rect boundingRect = Imgproc.boundingRect(contour);
        double width = boundingRect.width;
        double height = boundingRect.height;
        double aspectRatio = height / width;
        double area = Imgproc.contourArea(contour);
        if (area < 200000 || area > 400000) {
            return false;
        }
        if (aspectRatio < 0.85 || aspectRatio > 1.15) {
            return false;
        }
        if (Math.abs(width - height) > 25) {
            return false;
        }
        return true;
    }

    private void getPermission(){
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){//checkSelfPermission() Checks whether the app currently has a specific permission granted by the user.
            requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_REQUST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CAMERA_REQUST_CODE && grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.enableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

}