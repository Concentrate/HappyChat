package com.nettysocket.pratise.protocal;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by liudeyu on 2019/6/6.
 */
public class CommonMessage<T> {

    @JSONField(name = "code")
    private int code;
    @JSONField(name = "data")
    private T data;

    @JSONField(name="extra")
    private Extra extra;


    public CommonMessage() {
    }

    public CommonMessage(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public String buildJsonMessage(){
        return JSONObject.toJSONString(this);
    }
}
