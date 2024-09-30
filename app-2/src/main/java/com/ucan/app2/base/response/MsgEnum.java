package com.ucan.app2.base.response;

/**
 * @Description: 响应消息枚举类
 * @author liming.cen
 * @date 2023年1月3日 下午2:44:36
 */
public enum MsgEnum {
    /**
     * 失败响应<br>
     * code: -1 , msg: 'Fail!'
     */
    FAIL(-1, "Fail!"),
    /**
     * 成功响应<br>
     * code: 0 , msg: 'Success!'
     */
    SUCCESS(0, "Success!"),
    /**
     * 
     * code: 1, msg: '服务器内部错误！'
     */
    SERVER_ERROR(1, "服务器内部错误！"),
    /**
     * code: 2, msg: 'token校验失败！'
     */
    TOKEN_VERIFICATION_FAILED(2, "token校验失败！");

    /**
     * 响应码
     */
    private int code;
    /**
     * 响应消息
     */
    private String msg;

    private MsgEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
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

}
