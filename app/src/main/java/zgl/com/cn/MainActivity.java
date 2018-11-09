package zgl.com.cn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import zgl.com.cn.httputidemo.R;
import zgl.com.cn.bean.DataType;
import zgl.com.cn.bean.NetParams;
import zgl.com.cn.bean.WeatherInfo;
import zgl.com.cn.net.HttpNetHelper;
import zgl.com.cn.net.ICallBack;
import zgl.com.cn.net.okhttp.OkHttpRequest;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tv = findViewById(R.id.text);
        Button btn = findViewById(R.id.button);

        //初始化框架
        //OkHttpRequest  RetrofitHttpRequest
        HttpNetHelper.init(new OkHttpRequest());


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String, String> params = new HashMap<>();
                params.put("city", "北京");
                params.put("key", "13cb58f5884f9749287abbead9c658f2");

                NetParams netParams = new NetParams.Builder()
                        .baseUrl("http://restapi.amap.com")
                        .url("/v3/weather/weatherInfo")
                        .params(params)
                        .resDataType(DataType.JSON_OBJECT)
                        .clazz(WeatherInfo.class)
                        .build();

                //发送请求
                HttpNetHelper.getInstance().post(netParams, new ICallBack() {
                    @Override
                    public void onSuccess(Object result) {
                        tv.setText(((WeatherInfo)result).toString());
                    }

                    @Override
                    public void onError(int errorCode, String msg) {

                    }

                    @Override
                    public void onFailure(String msg) {

                    }

                });
            }
        });


    }
}
