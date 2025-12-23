package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;//响应码: 1 代表成功，0 代表失败
    private String msg;
    private Object data;

//=========成功响应============

    //增删改 成功响应
    public static Result success(){
        return new Result(200,"success",null);
    }
    //查询 成功响应
    public static Result success(Object data){
        return new Result(200,"success",data);
    }

    //查询 批量成功响应
    public static Result success(String msg, Object data){
        return new Result(200, msg,data);
    }


//========失败响应============

    //失败响应
    public static Result error(Integer code,String msg){
        return new Result(code,msg,null);
    }

    public static Result error(Integer code,String msg,Object data){
        return new Result(code,msg,data);
    }

    public static Result error(String message) {
        return error(500, message);
    }

    public static Result error(String message, Object data) {
        return error(500, message, data);
    }
}
