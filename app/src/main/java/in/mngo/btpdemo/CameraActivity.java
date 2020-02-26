package in.mngo.btpdemo;

import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2
{
    myJavaCameraView cameraBridgeViewBase;

    Button captureBtn;
    ImageView imageView;

    BaseLoaderCallback baseLoaderCallback;
    boolean startCanny = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        captureBtn = findViewById(R.id.captureBtn);
        imageView = findViewById(R.id.imageView);

    // defining the camera preview view
        cameraBridgeViewBase = findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch(status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat frame = inputFrame.rgba();
        if (startCanny == true)
        {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2GRAY); //converting image to grayscale
            Imgproc.Canny(frame, frame, 100, 80); //converting image to canny or edge detection image
        }

    //fixing the image orientation prblm
        final Mat mRgbaT = frame.t();
        Core.flip(frame.t(), mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, frame.size());

    //on clicking on capture button
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v)
            {
                Toast.makeText(CameraActivity.this, "please wait...", Toast.LENGTH_SHORT).show();

//            //playing capture sound
//                MediaActionSound sound = new MediaActionSound();
//                sound.play(MediaActionSound.SHUTTER_CLICK);
//
//            //crating bitmap of the camera view image
//                Bitmap resultBitmap = Bitmap.createBitmap(mRgbaT.cols(), mRgbaT.rows(),Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(mRgbaT, resultBitmap);
//
//            //saving the image
//                storeImage(resultBitmap);
//
////                imageView.setImageBitmap(resultBitmap);

            //trying to get the points of contour
                Thread t = new Thread(new Runnable() {
                    public void run()
                    {
                        try
                        {
                            ArrayList<int[]> coords = new ArrayList<>();

                            int count = 0;
                            for (int row=0; row<mRgbaT.rows(); row++)
                            {
                                for (int col=0; col<mRgbaT.cols(); col++ )
                                {
                                    double dataMat[] = mRgbaT.get(row,col);

                                    if(dataMat[0] != 0.0) //if not black
                                    {
                                        count++;

                                        int coord[] = {row, col};
                                        coords.add(coord);
                                    }
                                }
                            }

                        //storing coordinates in excel sheet
                            createExcel(coords);

                        }
                        catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }
        });

    //showing image
        return mRgbaT;
    }

//function to show toast in the UI thread
    private void updatefeed(final String text)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                Toast.makeText(CameraActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

//function to create and store coordinates in excel
    private void createExcel(ArrayList<int[]> coords)
    {
        Workbook wb = new HSSFWorkbook();

    //Now we are creating sheet
        Sheet sheet=null;
        sheet = wb.createSheet("Coordinates");
        sheet.setColumnWidth(0,(10*200));
        sheet.setColumnWidth(1,(10*200));

    //Now title column and row
        CellStyle titleCellStyle = wb.createCellStyle();
        titleCellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        titleCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        Row row = sheet.createRow(0);

        Cell cell = null;
        cell=row.createCell(0);
        cell.setCellValue("X");
        cell.setCellStyle(titleCellStyle);

        cell=row.createCell(1);
        cell.setCellValue("Y");
        cell.setCellStyle(titleCellStyle);

    //Now points coordinates
        int coordSize = coords.size();
        for(int i=0; i< coordSize; i++)
        {
            Row tempRow = sheet.createRow(i+1);

            Cell tempCell = null;
            tempCell = tempRow.createCell(0);
            tempCell.setCellValue(coords.get(i)[0]);

            tempCell = tempRow.createCell(1);
            tempCell.setCellValue(coords.get(i)[1]);
        }

        File excelFile = getOutputExcelFile();
        if (excelFile == null) {
            updatefeed("Error exporting excel file, check storage permissions");
            return;
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(excelFile);
            wb.write(fos);
            fos.close();

            updatefeed("Excel File exported");
        } catch (FileNotFoundException e) {
            updatefeed("Failed to export excel");
        } catch (java.io.IOException e) {
            updatefeed("Failed to export excel");
        }
    }

    private File getOutputExcelFile(){
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="BTP_coords_"+ timeStamp +".xls";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

//saving the image in phone
    private void storeImage(Bitmap image)
    {
        File pictureFile = getOutputMediaFile();

        if (pictureFile == null) {
            Log.d(String.valueOf(CameraActivity.this),
                    "Error creating media file, check storage permissions");// e.getMessage());
            return;
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(String.valueOf(CameraActivity.this), "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(String.valueOf(CameraActivity.this), "Error accessing file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile(){
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

         // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="BTP_images_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

//pause stop and other functions
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem", Toast.LENGTH_SHORT).show();
        }
        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }
}
