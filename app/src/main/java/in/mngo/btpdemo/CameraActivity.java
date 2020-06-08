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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    ImageView imageView;

    Button captureBtn;

    LinearLayout continueBtnLayout;
    Button reCaptureBtn;
    Button continueBtn;

    LinearLayout calculationLayout;
    EditText densityInput;
    EditText gravityInput;
    Button calculateBtn;
    TextView resultView;

    BaseLoaderCallback baseLoaderCallback;
    boolean startCanny = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

    //defining views
        imageView = findViewById(R.id.imageView);

        captureBtn = findViewById(R.id.captureBtn);

        continueBtnLayout = findViewById(R.id.continueBtnLayout);
        reCaptureBtn = findViewById(R.id.reCaptureBtn);
        continueBtn = findViewById(R.id.continueBtn);

        imageView = findViewById(R.id.imageView);

        calculationLayout = findViewById(R.id.calculationLayout);
        densityInput = findViewById(R.id.densityInput);
        gravityInput = findViewById(R.id.gravityInput);
        calculateBtn = findViewById(R.id.calculateBtn);
        resultView = findViewById(R.id.resultView);

    // defining the camera preview view
        try {
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
        catch (IllegalStateException e) {
        }
        catch (NullPointerException e) {
        }
        catch (IllegalArgumentException e){
        }
        catch (Exception e){
        }
    }

    @Override
    public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        try {
            final Mat frame = inputFrame.rgba();
            if (startCanny == true) {
                Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2GRAY); //converting image to grayscale
                Imgproc.Canny(frame, frame, 100, 80); //converting image to canny or edge detection image
            }

        //fixing the image orientation prblm
            final Mat frameCopy = frame.clone();

            final Mat mRgbaT = frame.t();
            Core.flip(frame.t(), mRgbaT, 1);
            Imgproc.resize(mRgbaT, mRgbaT, frame.size());

        //on clicking on capture button
            captureBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v)
                {
                //playing capture sound
                    MediaActionSound sound = new MediaActionSound();
                    sound.play(MediaActionSound.SHUTTER_CLICK);

                //creating bitmap of the camera view image
                    Bitmap resultBitmap = Bitmap.createBitmap(mRgbaT.cols(), mRgbaT.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mRgbaT, resultBitmap);

                //showing and hiding stuffs
                    captureBtn.setVisibility(View.INVISIBLE);
                    continueBtnLayout.setVisibility(View.VISIBLE);

                    cameraBridgeViewBase.setVisibility(SurfaceView.GONE);

                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(resultBitmap);
                }
            });

        //on pressing reCapture btn
            reCaptureBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                //hiding and showing stuffs
                    captureBtn.setVisibility(View.VISIBLE);
                    continueBtnLayout.setVisibility(View.INVISIBLE);

                    cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                }
            });

        //on pressing continue btn
            continueBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                //hiding and showing stuffs
                    captureBtn.setVisibility(View.INVISIBLE);
                    continueBtnLayout.setVisibility(View.INVISIBLE);

                    cameraBridgeViewBase.setVisibility(SurfaceView.GONE);
                    imageView.setVisibility(View.INVISIBLE);

                    calculationLayout.setVisibility(View.VISIBLE);
                }
            });

        //on pressing calculate btn
            calculateBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    String densityStr = densityInput.getText().toString().trim();
                    String gravityStr =  gravityInput.getText().toString().trim();

                //if user has given all data in input fields
                    if( !densityStr.equals("") && !gravityStr.equals("") ) {
                        updateResult("please wait...");
//                    Toast.makeText(CameraActivity.this, "please wait...", Toast.LENGTH_SHORT).show();

                        double density = Double.parseDouble( densityStr );
                        double gravity =  Double.parseDouble( gravityStr);

                        resultView.setText( density + " " + gravity );

                    //creating bitmap of the camera view image
                        Bitmap resultBitmap = Bitmap.createBitmap(mRgbaT.cols(), mRgbaT.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mRgbaT, resultBitmap);

                    //saving the image
//                        storeImage(resultBitmap);

                    // getting points of the edge
                         getPoints(frameCopy);
                    } else {
                        updateResult( "please enter density & gravitational constant" );
                    }
                }
            });

        //showing image
            return mRgbaT;
        }
        catch (IllegalStateException e) {
        }
        catch (NullPointerException e) {
        }
        catch (IllegalArgumentException e){
        }
        catch (Exception e){
        }

        return inputFrame.rgba();
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

//function to show text in result view in UI thread
    private void updateResult(final String text)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultView.setText( text );
            }
        });
    }

//function to get points coordinates of the edge of the image
    private void getPoints(final Mat frameCopy) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try  {
                    int colSize = frameCopy.cols();
                    int rowSize = frameCopy.rows();

                // coordinates of apex
                    int minYCord = -0+rowSize;
                    int xForMinYCord =  -0+rowSize;

                // looping through points in th matrix
                    ArrayList<int[]> coords = new ArrayList<>();
                    for (int row=0; row<rowSize; row++)
                    {
                        for (int col=0; col<colSize; col++ )
                        {
                            double dataMat[] = frameCopy.get(row, col);
                            if(dataMat[0] != 0.0) //if not black
                            {
                                int X = -row+rowSize;
                                int Y = -col+colSize;

                                //setting min Y coordinate and its corresponding X coordinate
                                if( Y <  minYCord ) {
                                    minYCord = Y;
                                    xForMinYCord = X;
                                }
                                //storing coordinates
                                int coord[] = { X, Y }; // image was coming mirror about x and y axis so doing this (-row+rowSize)
                                coords.add(coord);
                            }
                        }
                    }

                //getting max x coordinates
                    int maxXCord = 0;

                    int indexOfMinCord = 0;

                //making origin at apex
                    for( int i = 0 ; i<coords.size(); i++ ) {
                        int coord[] = coords.get(i);
                        int newX = coord[0] - xForMinYCord;
                        int newY = coord[1] - minYCord;

                    //getting index in the array whose coordinate is apex i.e (0, 0)
                        if( newX == 0 && newY == 0 ) {
                            indexOfMinCord = i;
                        }

                    //setting max X coord
                        if( newX > maxXCord ) {
                            maxXCord = newX;
                        }

                    //storing new coordinates
                        int newCoord [] = { newX, newY };
                        coords.set(i, newCoord);
                    }

                //calculating radius of curvature at apex
                    try {
                    //here y = z and x = x
                        int z1 = coords.get( indexOfMinCord )[1];
                        int x1 = coords.get( indexOfMinCord )[0];

                        int z2 = coords.get( indexOfMinCord + 1 )[1];
                        int x2 = coords.get( indexOfMinCord + 1 )[0];

                        int z3 = coords.get( indexOfMinCord + 2 )[1];
                        int x3 = coords.get( indexOfMinCord + 2 )[0];

                        int z4 = coords.get( indexOfMinCord + 3 )[1];
                        int x4 = coords.get( indexOfMinCord + 3 )[0];

                        double dz_dx = normalize( z2, z1 )/normalize( x2, x1 );

                        double dz_dx_2_3 = normalize( z3, z2 )/normalize( x3, x2 );

                        double d2z_dx2 = normalize( dz_dx_2_3, dz_dx )/normalize( x3, x1 );

                        double radiusAtApex = ( Math.pow(  Math.abs( 1 + dz_dx*dz_dx ), 3/2 ) / Math.abs( d2z_dx2 )  );

                        updateResult( Double.toString( radiusAtApex ) );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                //storing coordinates in excel sheet
//                    createExcel(coords);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
// function to make subtract val 1 if it is 0
    private double normalize( double a, double b ) {
        double diff = a-b;
        if( diff == 0.0 ) {
            diff = 1.0;
        }

        return diff;
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
            int coord[] = coords.get(i);

            Row tempRow = sheet.createRow(i+1);
            Cell tempCell = null;
            tempCell = tempRow.createCell(0);
            tempCell.setCellValue(coord[0]);

            tempCell = tempRow.createCell(1);
            tempCell.setCellValue(coord[1]);
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

//function to save the generated excel file
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
        String timeStamp = new SimpleDateFormat("dd_MM_yy_HH_mm_ss").format(new Date());
        File mediaFile;
        String mImageName="BTP_coords_"+ timeStamp +".xlsx";
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

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();

            Toast.makeText(CameraActivity.this, "image saved in phone storage", Toast.LENGTH_SHORT).show();

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
