package com.txt.sl.http;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jayden on 2016/7/21.
 */
public class ResponseData<T> {

    private int code;
    @SerializedName("msg")
    private String message;
    private T response;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    @NonNull
    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("ResponseData{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", response=").append(response);
        sb.append('}');
        return sb.toString();
    }
}
