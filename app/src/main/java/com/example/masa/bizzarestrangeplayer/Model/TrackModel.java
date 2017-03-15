
package com.example.masa.bizzarestrangeplayer.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrackModel implements Serializable {

    @SerializedName("tracks")
    @Expose
    private List<Track> tracks = new ArrayList<Track>();

    /**
     *
     * @return
     * The tracks
     */
    public List<Track> getTracks() {
        return tracks;
    }

    /**
     *
     * @param tracks
     * The tracks
     */
    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }



    public class Album {

        @SerializedName("album_type")
        @Expose
        private String albumType;
        @SerializedName("available_markets")
        @Expose
        private List<String> availableMarkets = new ArrayList<String>();
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("images")
        @Expose
        private List<Image> images = new ArrayList<Image>();
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("uri")
        @Expose
        private String uri;

        /**
         *
         * @return
         * The albumType
         */
        public String getAlbumType() {
            return albumType;
        }

        /**
         *
         * @param albumType
         * The album_type
         */
        public void setAlbumType(String albumType) {
            this.albumType = albumType;
        }

        /**
         *
         * @return
         * The availableMarkets
         */
        public List<String> getAvailableMarkets() {
            return availableMarkets;
        }

        /**
         *
         * @param availableMarkets
         * The available_markets
         */
        public void setAvailableMarkets(List<String> availableMarkets) {
            this.availableMarkets = availableMarkets;
        }


        /**
         *
         * @return
         * The href
         */
        public String getHref() {
            return href;
        }

        /**
         *
         * @param href
         * The href
         */
        public void setHref(String href) {
            this.href = href;
        }

        /**
         *
         * @return
         * The id
         */
        public String getId() {
            return id;
        }

        /**
         *
         * @param id
         * The id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         *
         * @return
         * The images
         */
        public List<Image> getImages() {
            return images;
        }

        /**
         *
         * @param images
         * The images
         */
        public void setImages(List<Image> images) {
            this.images = images;
        }

        /**
         *
         * @return
         * The name
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @param name
         * The name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         *
         * @return
         * The type
         */
        public String getType() {
            return type;
        }

        /**
         *
         * @param type
         * The type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         *
         * @return
         * The uri
         */
        public String getUri() {
            return uri;
        }

        /**
         *
         * @param uri
         * The uri
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

    }// Album


    public class Image {

        @SerializedName("height")
        @Expose
        private Integer height;
        @SerializedName("url")
        @Expose
        private String url;
        @SerializedName("width")
        @Expose
        private Integer width;

        /**
         *
         * @return
         * The height
         */
        public Integer getHeight() {
            return height;
        }

        /**
         *
         * @param height
         * The height
         */
        public void setHeight(Integer height) {
            this.height = height;
        }

        /**
         *
         * @return
         * The url
         */
        public String getUrl() {
            return url;
        }

        /**
         *
         * @param url
         * The url
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         *
         * @return
         * The width
         */
        public Integer getWidth() {
            return width;
        }

        /**
         *
         * @param width
         * The width
         */
        public void setWidth(Integer width) {
            this.width = width;
        }

    }// Image


    public class Track {

        @SerializedName("album")
        @Expose
        private Album album;

        @SerializedName("available_markets")
        @Expose
        private List<String> availableMarkets = new ArrayList<String>();
        @SerializedName("disc_number")
        @Expose
        private Integer discNumber;
        @SerializedName("duration_ms")
        @Expose
        private Integer durationMs;
        @SerializedName("explicit")
        @Expose
        private Boolean explicit;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("popularity")
        @Expose
        private Integer popularity;
        @SerializedName("preview_url")
        @Expose
        private String previewUrl;
        @SerializedName("track_number")
        @Expose
        private Integer trackNumber;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("uri")
        @Expose
        private String uri;

        /**
         *
         * @return
         * The album
         */
        public Album getAlbum() {
            return album;
        }

        /**
         *
         * @param album
         * The album
         */
        public void setAlbum(Album album) {
            this.album = album;
        }


        /**
         *
         * @return
         * The availableMarkets
         */
        public List<String> getAvailableMarkets() {
            return availableMarkets;
        }

        /**
         *
         * @param availableMarkets
         * The available_markets
         */
        public void setAvailableMarkets(List<String> availableMarkets) {
            this.availableMarkets = availableMarkets;
        }

        /**
         *
         * @return
         * The discNumber
         */
        public Integer getDiscNumber() {
            return discNumber;
        }

        /**
         *
         * @param discNumber
         * The disc_number
         */
        public void setDiscNumber(Integer discNumber) {
            this.discNumber = discNumber;
        }

        /**
         *
         * @return
         * The durationMs
         */
        public Integer getDurationMs() {
            return durationMs;
        }

        /**
         *
         * @param durationMs
         * The duration_ms
         */
        public void setDurationMs(Integer durationMs) {
            this.durationMs = durationMs;
        }

        /**
         *
         * @return
         * The explicit
         */
        public Boolean getExplicit() {
            return explicit;
        }

        /**
         *
         * @param explicit
         * The explicit
         */
        public void setExplicit(Boolean explicit) {
            this.explicit = explicit;
        }



        /**
         *
         * @return
         * The href
         */
        public String getHref() {
            return href;
        }

        /**
         *
         * @param href
         * The href
         */
        public void setHref(String href) {
            this.href = href;
        }

        /**
         *
         * @return
         * The id
         */
        public String getId() {
            return id;
        }

        /**
         *
         * @param id
         * The id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         *
         * @return
         * The name
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @param name
         * The name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         *
         * @return
         * The popularity
         */
        public Integer getPopularity() {
            return popularity;
        }

        /**
         *
         * @param popularity
         * The popularity
         */
        public void setPopularity(Integer popularity) {
            this.popularity = popularity;
        }

        /**
         *
         * @return
         * The previewUrl
         */
        public String getPreviewUrl() {
            return previewUrl;
        }

        /**
         *
         * @param previewUrl
         * The preview_url
         */
        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
        }

        /**
         *
         * @return
         * The trackNumber
         */
        public Integer getTrackNumber() {
            return trackNumber;
        }

        /**
         *
         * @param trackNumber
         * The track_number
         */
        public void setTrackNumber(Integer trackNumber) {
            this.trackNumber = trackNumber;
        }

        /**
         *
         * @return
         * The type
         */
        public String getType() {
            return type;
        }

        /**
         *
         * @param type
         * The type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         *
         * @return
         * The uri
         */
        public String getUri() {
            return uri;
        }

        /**
         *
         * @param uri
         * The uri
         */
        public void setUri(String uri) {
            this.uri = uri;
        }
    }
}