package net.swallowsnest.captionthis;

/**
 * Created by marshas on 2/6/17.
 */


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewImageActivity extends AppCompatActivity {

    public static final String TAG = ViewImageActivity.class.getSimpleName();
    private FileUploadService fileUploadService;

    private EditText captionView;
    private Uri imageUri;
    private File imageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        ButterKnife.bind(this);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        captionView = (EditText) findViewById(R.id.captionView);

        Intent intent = getIntent();
        imageUri = intent.getData();

        Picasso.with(this).load(imageUri).into(imageView);
    }


    @OnClick(R.id.postButton)
    void upload() {

        String caption = captionView.getText().toString();
        OkHttpClient client = new OkHttpClient.Builder().build();
        fileUploadService = new Retrofit.Builder().baseUrl(FileUploadService.BASE_URL).client(client).build().create(FileUploadService.class);

        File file = getImageFromUri();
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), file);
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        Call<ResponseBody> call = fileUploadService.postImage(image, caption);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // {"status":0,"details":[{"message":"File upload SUCCESSFUL"}]}
                // {"status":-1,"details":[{"message":"File size must be less than 2 MB"}]}
                // {"status":-1,"details":[{"message":"extension not allowed, please choose a JPEG or PNG file"}]}

                if (response.isSuccessful()) {
                    Log.e(TAG, "Response was successful");
                } else {
                    Log.e(TAG, "Response was failure");
                }

                int status = -1;
                String message = "This didn\'t work. Try again.";

                try {
                    String jsonString = response.body().string();
                    JSONObject jsonResponse = new JSONObject(jsonString);
                    status = jsonResponse.getInt("status");
                    message = jsonResponse.getJSONArray("details").getJSONObject(0).getString("message");

                } catch (Exception e) {
                    Log.e(TAG, "Exception caught parsing JSON: ", e);
                }

                if (status == 0) {
                    makeToast("Success: " + message);
                } else {
                    makeToast("Failure: " + message);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Exception caught making request: ", t);
                makeToast("Failure: " + t.getMessage());
            }
        });
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public File getImageFromUri() {
        String fileName = "";
        String fileType = ".jpg";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        fileName = "IMG_" + timeStamp;

        try {
            InputStream in = getContentResolver().openInputStream(imageUri);
            imageFile = File.createTempFile(fileName, fileType, getCacheDir());
            copyInputStreamToFile(in, imageFile);


            // copy from inputstream to output stream
            // save result in a temp file
            // close both streams
            // upload the file
            // delete the file after upload
        } catch (Exception e) {
            Log.e(TAG, "Error creating file: ", e);
        }
        return imageFile;
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
