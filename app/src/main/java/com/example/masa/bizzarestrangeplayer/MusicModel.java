
package com.example.masa.bizzarestrangeplayer;

import com.google.gson.annotations.SerializedName;

public class MusicModel {

    // 変数宣言

    @SerializedName("data")
    private String data;

    @SerializedName("origin")
    private String origin;

    @SerializedName("url")
    private String url;

    @SerializedName("headers")
    private Header headers;

    // getter/setter

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Header getHeaders() {
        return headers;
    }

    public void setHeaders(Header headers) {
        this.headers = headers;
    }

    /**
     * headerクラス
     */

    class Header {

        // 変数宣言

        @SerializedName("Accept-Encoding")
        private String acceptEncoding;

        @SerializedName("Content-Length")
        private Integer contentLength;

        @SerializedName("Content-Type")
        private String contentType;

        @SerializedName("Host")
        private String host;

        @SerializedName("User-Agent")
        private String userAgent;

        // getter/setter

        public String getAcceptEncoding() {
            return acceptEncoding;
        }

        public void setAcceptEncoding(String acceptEncoding) {
            this.acceptEncoding = acceptEncoding;
        }

        public Integer getContentLength() {
            return contentLength;
        }

        public void setContentLength(Integer contentLength) {
            this.contentLength = contentLength;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
    }

}