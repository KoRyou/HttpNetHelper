package zgl.com.cn.net.retrofit;

import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.protobuf.ProtoConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import zgl.com.cn.bean.DataType;
import zgl.com.cn.bean.NetParams;
import zgl.com.cn.net.ICallBack;
import zgl.com.cn.net.IHttpRequest;
import zgl.com.cn.net.utils.DataParseUtil;

/**
 * todo 描述：模块名_具体页面描述
 *
 * @author : jsj_android
 * @date : 2018/11/7
 */

public class RetrofitHttpRequest implements IHttpRequest {

    private Retrofit mRetrofit;
    private String mBaseUrl = "";
    private boolean mIsProtoType = false;//请求类型为ProtoBuffer
    private Call<ResponseBody> mCall;
    //缓存发出的请求，用于后期删除
    private static final Map<String, Call> REQ_CALL_MAP = new HashMap<>();
    private final Map mJsonMap, mProtoMap;

    public RetrofitHttpRequest() {

        mJsonMap = new HashMap(2);
        mJsonMap.put("Content-Type", "application/json");
        mJsonMap.put("Accept", "application/json");


        mProtoMap = new HashMap(2);
        mProtoMap.put("Content-Type", "application/x-protobuf");
        mProtoMap.put("Accept", "application/x-protobuf");


    }

    private void initRetrofit(String baseUrl, int resDataType) {
        if (TextUtils.isEmpty(baseUrl)) {
            throw new RuntimeException("<<<<<ERROR: BaseUrl is empty!!!>>>>>");
        }
        if (mIsProtoType == (resDataType == DataType.PROTO_BUFFER) || !mBaseUrl.equals(baseUrl) || mRetrofit == null) {
            mBaseUrl = baseUrl;
            if (resDataType == DataType.PROTO_BUFFER) {
                mIsProtoType = true;
                mRetrofit = new Retrofit.Builder().baseUrl(baseUrl)
                        .addConverterFactory(ScalarsConverterFactory.create()) //添加返回为字符串的支持
                        .addConverterFactory(ProtoConverterFactory.create())
                        .build();
            } else {
                mIsProtoType = false;
                mRetrofit = new Retrofit.Builder().baseUrl(baseUrl)
                        .addConverterFactory(ScalarsConverterFactory.create()) //添加返回为字符串的支持
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }

        }
    }

    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

    @Override
    public void get(final NetParams params, final ICallBack callBack) {

        // 1.把参数分解
        String url = appendGetUrl(params.getUrl(), params.getParams());
        NetParams netParams = new NetParams.Builder(params).url(url).build();
        // 2.发送请求
        initRetrofit(netParams.getBaseUrl(), netParams.getResDataType());
        Map headerMap = getHeaderMap(netParams.getResDataType());
        mCall = create(ApiService.class).executeGet(headerMap, url);
        putCall(netParams, mCall);//保存请求到本地，用于取消
        toRequest(netParams, callBack);

    }

    private Map getHeaderMap(int resDataType) {
        if (resDataType == DataType.PROTO_BUFFER) {
            return mProtoMap;
        } else {
            return mJsonMap;
        }
    }


    @Override
    public void post(NetParams params, ICallBack callBack) {
        initRetrofit(params.getBaseUrl(), params.getResDataType());
        Map headerMap = getHeaderMap(params.getResDataType());
        mCall = create(ApiService.class).executePost(headerMap, params.getUrl(), params.getParams());
        putCall(params, mCall);//保存请求到本地，用于取消
        toRequest(params, callBack);
    }

    @Override
    public void cancel(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        List<String> list = new ArrayList<>();
        synchronized (REQ_CALL_MAP) {
            for (String key : REQ_CALL_MAP.keySet()) {
                if (key.startsWith(tag)) {
                    REQ_CALL_MAP.get(key).cancel();
                    list.add(key);
                }
            }
        }
        for (String s : list) {
            removeCall(s);
        }
    }


    /**
     * 公共的请求方法
     *
     * @param netParams
     * @param callBack
     */
    private void toRequest(final NetParams netParams, final ICallBack callBack) {
        mCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (200 == response.code()) {
                    try {
                        //得到请求回来的数据，并转换成对应的实体
                        String result = response.body().string();
                        parseData(result, netParams.getClazz(), netParams.getResDataType(), callBack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (!response.isSuccessful() || 200 != response.code()) {
                    callBack.onError(response.code(), response.message());
                }
                if (netParams.getTag() != null) {
                    removeCall(netParams.getUrl());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callBack.onFailure(t.getMessage());
                if (netParams.getTag() != null) {
                    removeCall(netParams.getUrl());
                }
            }
        });
    }

/**************************************************************************/
/*************----------------------工具方法----------------------*********/
/**************************************************************************/


    /**
     * 响应的数据进行解析并返回给回调
     *
     * @param result
     * @param clazz
     * @param resDataType
     * @param callBack
     */
    private void parseData(String result, Class clazz, int resDataType, ICallBack callBack) {
        switch (resDataType) {
            case DataType.STRING:
                callBack.onSuccess(result);
                break;
            case DataType.JSON_OBJECT:
                callBack.onSuccess(DataParseUtil.parseObject(result, clazz));
                break;
            case DataType.JSON_ARRAY:
                callBack.onSuccess(DataParseUtil.parseToArrayList(result, clazz));
                break;
            case DataType.PROTO_BUFFER:
                callBack.onSuccess(DataParseUtil.parseObject(result, clazz));
                break;
            default:
                throw new RuntimeException("<<<<< ERROR : NetFrameWork response dataType error!!! >>>>>");
        }
    }

    /**
     * 具体删除请求的方法
     *
     * @param url
     */
    private void removeCall(String url) {

        synchronized (REQ_CALL_MAP) {
            for (String key : REQ_CALL_MAP.keySet()) {
                if (key.contains(url)) {
                    url = key;
                    break;
                }
            }
            REQ_CALL_MAP.remove(url);
        }

    }

    /**
     * 把请求保存到内存,用于取消
     *
     * @param params
     * @param call
     */
    private void putCall(NetParams params, Call call) {
        if (TextUtils.isEmpty(params.getTag())) {
            return;
        }
        synchronized (REQ_CALL_MAP) {
            REQ_CALL_MAP.put(params.getTag() + params.getUrl(), call);
        }
    }


    /**
     * 把参数拼接到请求URL中
     *
     * @param url
     * @param params
     * @return
     */
    private String appendGetUrl(String url, Map<String, String> params) {
        StringBuffer sb = new StringBuffer("?");
        //拼接get请求url
        if (null != params) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
                sb.append("&");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        url += sb.toString();
        return url;
    }

}
