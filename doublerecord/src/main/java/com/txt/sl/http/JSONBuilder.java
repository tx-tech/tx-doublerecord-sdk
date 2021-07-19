package com.txt.sl.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

public class JSONBuilder {

    @NonNull
    private JSONObject jsonObject = new JSONObject();

    private JSONBuilder() {
    }

    @NonNull
    public static JSONBuilder newBuilder() {
        return new JSONBuilder();
    }

    @NonNull
    public static JSONBuilder wrap(@Nullable Map<String, ?> map) {
        JSONBuilder delegate = new JSONBuilder();
        if (null == map) return delegate;
        JSONObject json = new JSONObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        delegate.jsonObject = json;
        return delegate;
    }

    @NonNull
    public static JSONBuilder wrap(String s) {
        JSONBuilder delegate = new JSONBuilder();
        try {
            delegate.jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            delegate.jsonObject = new JSONObject();
        }
        return delegate;
    }

    public int length() {
        return jsonObject.length();
    }

    @NonNull
    public JSONBuilder putAlways(String name, Object value) {
        try {
            jsonObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    @NonNull
    public JSONBuilder putIfAvailable(String name, @Nullable Object value) {
        try {
            if (value == null) return this;
            jsonObject.put(name, value);
        } catch (JSONException ignored) {
        }
        return this;
    }

    @NonNull
    public JSONBuilder putIfNotZero(String name, int value) {
        if (value == 0) return this;
        try {
            jsonObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    @NonNull
    public JSONBuilder putIfNotNull(String name, @Nullable Object value) {
        if (value == null) return this;
        try {
            jsonObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    @NonNull
    public JSONBuilder putIfNotEmpty(String name, @Nullable Map<?, ?> value) {
        if (value == null || value.isEmpty()) return this;
        try {
            jsonObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    @NonNull
    public JSONBuilder putIfNotEmpty(String name, @Nullable Collection<?> value) {
        if (value == null || value.isEmpty()) return this;
        try {
            jsonObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    @Nullable
    public String asString() {
        if (jsonObject != null) {
            return jsonObject.toString();
        }
        return null;
    }

    private interface Catchable {
        void done() throws JSONException;
    }

    private void tryCatch(@NonNull Catchable catchable) {
        try {
            catchable.done();
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }

    @NonNull
    public JSONObject build() {
        return jsonObject;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
