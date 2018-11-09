package zgl.com.cn.bean;

import java.util.HashMap;
import java.util.Map;

/**
 *  描述：网络请求的入参实体（构建者模式）
 *
 * @author : jsj_android
 * @date : 2018/11/8
 */

public class NetParams {

    private String baseUrl;
    private String url;
    private String tag;//请求标签
    private Map<String, String> params;//请求参数
    private @DataType.Type int resDataType = DataType.STRING; //响应数据的类型
    private Class clazz; //被解析成的类

    private NetParams (){
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getTag() {
        return tag;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public int getResDataType() {
        return resDataType;
    }

    public Class getClazz() {
        return clazz;
    }










    public NetParams (Builder builder){
        this.baseUrl = builder.baseUrl;
        this.url = builder.url;
        this.tag = builder.tag;
        this.params = builder.params;
        this.resDataType = builder.resDataType;
        this.clazz = builder.clazz;
    }

    public static class Builder{
        private String baseUrl;
        private String url;
        private String tag;
        private Map<String, String> params = new HashMap<>();//请求参数
        private @DataType.Type int resDataType = DataType.STRING; //响应数据的类型
        private Class clazz; //被解析成的类

        public Builder(){}

        public Builder(NetParams params){
            this.baseUrl = params.getBaseUrl();
            this.url = params.getUrl();
            this.tag = params.getTag();
            this.params = params.getParams();
            this.resDataType = params.getResDataType();
            this.clazz = params.getClazz();
        }

        public Builder baseUrl(String val){
            baseUrl = val;
            return this;
        }
        public Builder url(String val){
            url = val;
            return this;
        }
        public Builder tag(String val){
            tag = val;
            return this;
        }
        public Builder params(Map<String, String> val){
            params = val;
            return this;
        }
        public Builder resDataType(@DataType.Type int val){
            resDataType = val;
            return this;
        }
        public Builder clazz(Class val){
            clazz = val;
            return this;
        }

        public NetParams build(){
            return new NetParams(this);
        }
    }

}
