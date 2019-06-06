package com.nettysocket.pratise.protocal;

/**
 * Created by liudeyu on 2019/6/6.
 */

/**
 * format{
 * <p>
 * {
 * code:xxx,
 * data:{
 * <p>
 * },
 * extra:{
 * <p>
 * }
 * }
 * <p>
 * }
 */
public class NMessageProto {

    public final static int PING = 41 >> 5;
    public final static int PONG = 42 >> 5;
    public final static int SYS = 43 >> 5;
    public final static int MESSAGE = 44 >> 5;
    public final static int AUTHEN = 45 >> 5;
    public final static int ERROR = 46 >> 5;


    private int code;
    private String body;

    public NMessageProto(int code, String body) {
        this.code = code;
        this.body = body;
    }


    public static CommonMessage<String> buildPingMessage() {
        return new CommonMessage<String>(PING, "");
    }

    public static CommonMessage<String> buildPongMessage() {
        return new CommonMessage<String>(PONG, "");
    }

    public static CommonMessage<String> buildTextMessage(int code, String text) {
        return new CommonMessage<String>(code, text);
    }

    public static CommonMessage<AuthenObject> buildAuthenMessage(int code,AuthenObject object){
        return new CommonMessage<>(code,object);
    }


}
