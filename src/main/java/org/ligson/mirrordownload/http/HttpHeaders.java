package org.ligson.mirrordownload.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpHeaders {
    private Map<String, String> headers = new ConcurrentHashMap<>();

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
