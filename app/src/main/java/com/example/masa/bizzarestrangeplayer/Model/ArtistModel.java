
package com.example.masa.bizzarestrangeplayer.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ArtistModel {

    @SerializedName("artists")
    @Expose
    private Artists artists;

    /**
     * @return The artists
     */
    public Artists getArtists() {
        return artists;
    }

    /**
     * @param artists The artists
     */
    public void setArtists(Artists artists) {
        this.artists = artists;
    }


    public class Artists {

        @SerializedName("href")
        @Expose
        private String href;

        @SerializedName("items")
        @Expose
        private List<Item> items = new ArrayList<Item>();

        @SerializedName("limit")
        @Expose
        private Integer limit;

        @SerializedName("next")
        @Expose
        private Object next;

        @SerializedName("offset")
        @Expose
        private Integer offset;

        @SerializedName("previous")
        @Expose
        private Object previous;

        @SerializedName("total")
        @Expose
        private Integer total;

        /**
         * @return The href
         */
        public String getHref() {
            return href;
        }

        /**
         * @param href The href
         */
        public void setHref(String href) {
            this.href = href;
        }

        /**
         * @return The items
         */
        public List<Item> getItems() {
            return items;
        }

        /**
         * @param items The items
         */
        public void setItems(List<Item> items) {
            this.items = items;
        }

        /**
         * @return The limit
         */
        public Integer getLimit() {
            return limit;
        }

        /**
         * @param limit The limit
         */
        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        /**
         * @return The next
         */
        public Object getNext() {
            return next;
        }

        /**
         * @param next The next
         */
        public void setNext(Object next) {
            this.next = next;
        }

        /**
         * @return The offset
         */
        public Integer getOffset() {
            return offset;
        }

        /**
         * @param offset The offset
         */
        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        /**
         * @return The previous
         */
        public Object getPrevious() {
            return previous;
        }

        /**
         * @param previous The previous
         */
        public void setPrevious(Object previous) {
            this.previous = previous;
        }

        /**
         * @return The total
         */
        public Integer getTotal() {
            return total;
        }

        /**
         * @param total The total
         */
        public void setTotal(Integer total) {
            this.total = total;
        }

    }


    public class Item {

        @SerializedName("genres")
        @Expose
        private List<Object> genres = new ArrayList<Object>();

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

        @SerializedName("popularity")
        @Expose
        private Integer popularity;

        @SerializedName("type")
        @Expose
        private String type;

        @SerializedName("uri")
        @Expose
        private String uri;


        /**
         * @return The genres
         */
        public List<Object> getGenres() {
            return genres;
        }

        /**
         * @param genres The genres
         */
        public void setGenres(List<Object> genres) {
            this.genres = genres;
        }

        /**
         * @return The href
         */
        public String getHref() {
            return href;
        }

        /**
         * @param href The href
         */
        public void setHref(String href) {
            this.href = href;
        }

        /**
         * @return The id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id The id
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
         * @return The name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name The name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return The popularity
         */
        public Integer getPopularity() {
            return popularity;
        }

        /**
         * @param popularity The popularity
         */
        public void setPopularity(Integer popularity) {
            this.popularity = popularity;
        }

        /**
         * @return The type
         */
        public String getType() {
            return type;
        }

        /**
         * @param type The type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return The uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * @param uri The uri
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

    }


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

}// End of Class ArtistsData