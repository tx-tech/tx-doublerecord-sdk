package com.txt.sl.http.interceptor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/6.
 * description：
 */
public class NetInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request().newBuilder()

                .addHeader("Connection","close").build();

        return chain.proceed(request);

    }
}

