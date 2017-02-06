package net.swallowsnest.captionthis;

/**
 * Created by marshas on 2/6/17.
 */
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface FileUploadService {
    String BASE_URL = "http://52.25.199.242/sagedom/";

    @Multipart
    @POST("index.php")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image,
                                 @Query("text") String caption);
}

