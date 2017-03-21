
package com.example.masa.bizzarestrangeplayer;


import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class PutSongsToPlaylistAsyncTask extends AsyncTask<String, String, Void> {

    String mAccessToken;
    String userID;
    String playlistID;


    public PutSongsToPlaylistAsyncTask() {
        super();
    }


    @Override
    protected Void doInBackground(String... params) {

        this.mAccessToken = params[0];
        this.userID = params[1];
        this.playlistID = params[2];


        // これ意味あんのか自問自答で〜
        if (params[2] == null) {
            System.out.println("早期リターン in Step2");
            return null;
        }

        System.out.println("step2まできた");


        URL url = null;
        HttpURLConnection con = null;

        OutputStream os = null;
        BufferedReader reader = null;
        PrintStream ps = null;

        try {

//            https://api.spotify.com/v1/users/{user_id}/playlists/{playlist_id}/tracks

            url = new URL("https://api.spotify.com/v1/users/" +
                    userID +
                    "/playlists/" +
                    playlistID +
                    "/tracks"
            );

            System.out.println("URL: " + url);


            con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            //con.setRequestProperty("Accept", "application/json; charset=utf-8");
            con.setRequestProperty("Authorization", "Bearer " + mAccessToken);
            con.setDoOutput(true);


            os = con.getOutputStream();   //POST用のOutputStreamを取得
            ps = new PrintStream(os);


//            {"uris": ["spotify:track:4iV5W9uYEdYUVa79Axb7Rh",
//                    "spotify:track:1301WleyT98MSxVHPZCA6M"]}

            ps.print("{\"uris\": [\"spotify:track:4iV5W9uYEdYUVa79Axb7Rh\"]}");
            ps.close();


            reader = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), "UTF-8"));

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            String s = builder.toString();

            //System.out.println("おら！結果だ！ " + s);

            JSONObject jsonObject = new JSONObject(s);

            //System.out.println("おら！jsonだ！ " + jsonObject);

            String snapshot_id = jsonObject.getString("snapshot_id");

            System.out.println("スナップショットID: " + snapshot_id);


            return null;

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}