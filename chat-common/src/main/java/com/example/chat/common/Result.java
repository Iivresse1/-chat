package com.example.chat.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;

    private String msg;

    private Object data;

    /*直接返回成功结果*/

    public static Result success(Object data) {
        return success(200,"操作成功",data);
    }

    /*自定义返回的成功结果*/

    public static Result success(int code, String msg, Object data) {
        Result res=new Result();
        res.setCode(code);
        res.setMsg(msg);
        res.setData(data);
        return res;
    }

    /*不带结果的成功返回*/

    public static Result success(){
        Result res=new Result();
        res.setCode(200);
        res.setMsg("操作成功");
        return res;
    }

    /*直接返回失败结果*/

    public static Result error(Object data) {
        return error(400,"操作失败",data);
    }

    /*带参数返回失败结果*/

    public static Result error(int code, String msg, Object data) {
        Result res=new Result();
        res.setCode(code);
        res.setMsg(msg);
        res.setData(data);
        return res;
    }

    /*不带结果的失败返回*/

    public static Result error(){
        Result res=new Result();
        res.setCode(400);
        res.setMsg("操作失败");
        return res;
    }


}

