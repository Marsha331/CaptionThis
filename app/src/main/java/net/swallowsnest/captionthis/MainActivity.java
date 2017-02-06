package net.swallowsnest.captionthis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 0;
    public static final int REQUEST_PICK_PHOTO = 1;

    private Uri mMediaUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    mMediaUri = data.getData();
                }

                Intent intent = new Intent(this, ViewImageActivity.class);
                intent.setData(mMediaUri);
                startActivity(intent);
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.takePhoto)
    void takePhoto() {
        mMediaUri = getOutputMediaFileUri();
        if (mMediaUri == null) {
            Toast.makeText(this,
                    "There was a problem accessing your device's external storage.",
                    Toast.LENGTH_LONG).show();
        } else {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @OnClick(R.id.pickPhoto)
    void pickPhoto() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_PHOTO);
    }

    private Uri getOutputMediaFileUri() {
        // check for external storage
        if (!isExternalStorageAvailable()) {
            return null;
        }

        // 1. Get the external storage directory
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // 2. Create a unique file name
        String fileName = "";
        String fileType = "";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        fileName = "IMG_" + timeStamp;
        fileType = ".jpg";

        // 3. Create the file
        File mediaFile;
        Uri uri = null;

        try {
            mediaFile = File.createTempFile(fileName, fileType, mediaStorageDir);
            Log.i(TAG, "File: " + Uri.fromFile(mediaFile));

            // 4. Return the file's URI
            uri = Uri.fromFile(mediaFile);
        } catch (IOException e) {
            Log.e(TAG, "Error creating file: " +
                    mediaStorageDir.getAbsolutePath() + fileName + fileType);
        }

        return uri;
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }
}
