package in.mngo.btpdemo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_WRITE_PERMISSION_CODE = 1000;

    private ImageView imageView;
    private Button photoButton;

    private String pictureImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView1);
        photoButton= findViewById(R.id.button1);

        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
            //checking permission
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(MainActivity.this, "Please grant permission to use camera", Toast.LENGTH_SHORT).show();
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) //checking permission to write in external storage
                    {
                        Toast.makeText(MainActivity.this, "Please grant permission to write in phone storage", Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_PERMISSION_CODE);
                    }
                    else // all permission are granted so starting camera
                    {
                        try {
                            Intent cameraActivity = new Intent(MainActivity.this, CameraActivity.class);
                            startActivity(cameraActivity);
                            //openBackCamera();
                        }
                        catch (Exception e) {
                        }
                    }
                }
            }
        });
    }

    //function to ask for permissons
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == STORAGE_WRITE_PERMISSION_CODE)
        {
            //restarting app
            finish();
            startActivity(getIntent());

            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Permission Granted! Please Restart the App.", Toast.LENGTH_SHORT);
            }
            else
            {
                Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT);
            }
        }
    }

//function to capture and store image
//    private void openBackCamera()
//    {
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//
//    //naming the image
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = timeStamp + ".jpg";
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
//
//    //storing the image in storage
//        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
//        File file = new File(pictureImagePath);
//        Uri outputFileUri = Uri.fromFile(file);
//
//    //capturing image
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
//        startActivityForResult(cameraIntent, 1);
//    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1) {
//            File imgFile = new File(pictureImagePath);
//            if (imgFile.exists()) {
//                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                Bitmap rotatedMyBitmap = rotateImage(myBitmap, 90);
//
//
//                imageView.setImageBitmap(rotatedMyBitmap);
//            }
//        }
//    }
//
////function to rotate image
//    public static Bitmap rotateImage(Bitmap source, float angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
//                matrix, true);
//    }
}
