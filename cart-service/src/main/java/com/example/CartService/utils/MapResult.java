package com.example.CartService.utils;

import com.example.CartService.ApiResult.ApiResult;
import org.springframework.stereotype.Component;

@Component
public class MapResult<T> {
    public ApiResult<T> map(T data, String message){
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setData(data);
        apiResult.setMessage(message);
        return apiResult;
    }
}
