package com.ucan.sso.server.base.response;

/**
 * @Description: 响应类
 * @author liming.cen
 * @date 2023年1月3日 下午3:11:30
 */
public class Response<T, E> {
    /**
     * 响应代码
     */
    private int code;
    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;
    /**
     * 额外的响应数据
     */
    private E extraData;

    public Response() {
    }

    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Response(int code, String msg, T data, E extraData) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.extraData = extraData;
    }

    /**
     * 返回消息和数据
     * 
     * @param <T,E>
     * @param msg
     * @param data
     * @return
     */
    public static <T, E> Response<T, E> respose(MsgEnum msg, T data) {
        return new Response<T, E>(msg.getCode(), msg.getMsg(), data);
    }

    /**
     * 返回消息和数据+额外数据
     * 
     * @param <T>
     * @param <E>
     * @param msg       包含响应码（code）和消息（msg）
     * @param data
     * @param extraData
     * @return
     */
    public static <T, E> Response<T, E> respose(MsgEnum msg, T data, E extraData) {
        return new Response<T, E>(msg.getCode(), msg.getMsg(), data, extraData);
    }

    /**
     * 返回自定义消息和数据+额外数据
     * 
     * @param <T>
     * @param <E>
     * @param code      (FAIL(-1, "Fail!"), SUCCESS(0, "Success!"), SERVER_ERROR(1,
     *                  "服务器内部错误！")),TOKEN_VERIFICATION_FAILED(2, "token校验失败！");
     * @param customMsg
     * @param data
     * @param extraData
     * @return
     */
    public static <T, E> Response<T, E> respose(int code, String msg, T data, E extraData) {
        return new Response<T, E>(code, msg, data, extraData);
    }

    /**
     * 自定义成功响应内容：code=0，msg="Success!"以及自定义返回数据
     * 
     * @param <T>
     * @param <E>
     * @param data 自定义返回数据
     * @return
     */
    public static <T, E> Response<T, E> success(T data) {
        return new Response<T, E>(MsgEnum.SUCCESS.getCode(), MsgEnum.SUCCESS.getMsg(), data);
    }

    /**
     * 自定义失败响应内容：code=-1，msg="Fail!"以及自定义返回数据
     * 
     * @param <T>
     * @param <E>
     * @param data 自定义响应数据
     * @return
     */
    public static <T, E> Response<T, E> fail(T data) {
        return new Response<T, E>(MsgEnum.FAIL.getCode(), MsgEnum.FAIL.getMsg(), data);
    }

    /**
     * 响应成功消息，响应码 code=0
     * 
     * @param <T,E>
     * @return
     */
    public static <T, E> Response<T, E> success() {
        return respose(MsgEnum.SUCCESS);
    }

    /**
     * 自定义成功响应信息
     * 
     * @param <T>
     * @param <E>
     * @param msg
     * @return
     */
    public static <T, E> Response<T, E> success(String msg) {
        return new Response<T, E>(MsgEnum.SUCCESS.getCode(), msg);
    }

    /**
     * 自定义失败响应信息
     * 
     * @param <T>
     * @param <E>
     * @param msg
     * @return
     */
    public static <T, E> Response<T, E> fail(String msg) {
        return new Response<T, E>(MsgEnum.FAIL.getCode(), msg);
    }

    /**
     * 自定义失败响应码和响应信息
     * 
     * @param <T>
     * @param <E>
     * @param code
     * @param msg
     * @return
     */
    public static <T, E> Response<T, E> fail(int code, String msg) {
        return new Response<T, E>(code, msg);
    }

    /**
     * 响应失败消息
     * 
     * @param <T,E>
     * @return
     */
    public static <T, E> Response<T, E> fail() {
        return respose(MsgEnum.FAIL);
    }

    /**
     * 响应服务器内部错误消息
     * 
     * @param <T,E>
     * @return
     */
    public static <T, E> Response<T, E> serverError() {
        return respose(MsgEnum.SERVER_ERROR);
    }

    /**
     * 响应消息
     * 
     * @param <T,E>
     * @param msg
     * @return
     */
    private static <T, E> Response<T, E> respose(MsgEnum msg) {
        return new Response<T, E>(msg.getCode(), msg.getMsg());

    }

    /**
     * 响应自定义消息和数据<br>
     * 通过 MsgEnum 获取code，例如：MsgEnum.SUCCESS.getCode() ，消息可自定义
     * 
     * @param <T,E>
     * @param code
     * @param msg
     * @param data
     * @return
     */
    public static <T, E> Response<T, E> respCustomMsgWithData(int code, String msg, T data) {
        return new Response<T, E>(code, msg, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public E getExtraData() {
        return extraData;
    }

    public void setExtraData(E extraData) {
        this.extraData = extraData;
    }

}
