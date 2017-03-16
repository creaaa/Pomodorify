
package com.example.masa.bizzarestrangeplayer.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

//     @SerializedName("")には、jsonのキー名を入れる。変数名は自分で決めてOK


public class TrackForPLModel implements Serializable {

    @SerializedName("tracks")
    @Expose
    private List<Track> tracks;

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    @SerializedName("seeds")
    @Expose
    private List<Seed> seeds;

    public List<Seed> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<Seed> seeds) {
        this.seeds = seeds;
    }

}