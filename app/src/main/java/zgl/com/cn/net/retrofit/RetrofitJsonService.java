package zgl.com.cn.net.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

/**
 * todo 描述：模块名_具体页面描述
 *
 * @author : jsj_android
 * @date : 2018/11/7
 */

public interface RetrofitJsonService {

    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @GET
    Call<ResponseBody> req_weather (@Url String url);

}
