package in.mngo.btpdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class myJavaCameraView extends JavaCameraView implements android.hardware.Camera.PictureCallback
{
    private static final String TAG = "OpenCV";
    private String mPictureFileName;

    public myJavaCameraView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void takePicture(final String fileName)
    {
        try {
            Log.i(TAG, "Taking Picture");
            this.mPictureFileName = fileName;
            mCamera.setPreviewCallback(null);

            mCamera.takePicture(null, null, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, android.hardware.Camera camera)
    {
        Log.i(TAG, "Saving a bitmap to file");

        try {
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);

            FileOutputStream fos = new FileOutputStream(mPictureFileName);

            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "exception in photo call back");
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }
}
