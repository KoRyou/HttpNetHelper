package zgl.com.cn.net;

import zgl.com.cn.bean.NetParams;

/**
 *  描述：请求的接口
 *
 * @author : jsj_android
 * @date : 2018/11/6
 */

public interface IHttpRequest {

    void get(NetParams params, ICallBack callBack);

    void post(NetParams params,ICallBack callBack);

    void cancel(String tag);

}
