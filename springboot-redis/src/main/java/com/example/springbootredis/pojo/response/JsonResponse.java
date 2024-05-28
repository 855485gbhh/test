package com.example.springbootredis.pojo.response;
import com.example.springbootredis.pojo.enums.ErrorCodeEnums;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class JsonResponse<T> {

private int code;

private T data;

private String errorMsg;

public JsonResponse(int code,T data,String errorMsg){
        this.code=code;
        this.data=data;
        this.errorMsg=errorMsg;
        }

public JsonResponse(int code,T data){
        this(code,data,"");
        }

public JsonResponse(ErrorCodeEnums errorCode,String errorMsg){
        this(errorCode.getCode(),null,errorMsg);
        }

public JsonResponse(ErrorCodeEnums errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage());
        }

public JsonResponse(int errorCode,String errorMsg){
        this(errorCode,null,errorMsg);
        }
}

