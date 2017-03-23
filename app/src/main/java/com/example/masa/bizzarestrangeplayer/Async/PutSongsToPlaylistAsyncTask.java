
package com.example.masa.bizzarestrangeplayer.Async;

import android.os.AsyncTask;

import com.example.masa.bizzarestrangeplayer.Activity.SetListResultActivity;
import com.example.masa.bizzarestrangeplayer.Model.Track;

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
import java.util.ArrayList;


public class PutSongsToPlaylistAsyncTask extends AsyncTask<String, String, Void> {


    SetListResultActivity activity;


    String mAccessToken;
    String userID;
    String playlistID;


    public PutSongsToPlaylistAsyncTask(SetListResultActivity activity) {
        super();
        this.activity = activity;
    }


    @Override
    protected Void doInBackground(String... params) {


        this.mAccessToken = params[0];
        this.userID = params[1];
        this.playlistID = params[2];


        // これ意味あんのか自問自答で〜 → 有効だった。
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


            ArrayList<Track> currentSetPlaylist = activity.currentSetPlaylist;

            ArrayList<String> songIDs = new ArrayList<>();

//            for (Track id: currentSetPlaylist) {
//                songIDs.add(id.getId());
//            }


            for (int i = 0; i < currentSetPlaylist.size(); i++) {
                if (activity.isCheckedArray[i]) {
                    songIDs.add(currentSetPlaylist.get(i).getId());
                }
            }

            //songIDs.add("1301WleyT98MSxVHPZCA6M");
            //songIDs.add("4iV5W9uYEdYUVa79Axb7Rh");

            StringBuilder sb = new StringBuilder();


            sb.append("{\"uris\": [");

            for (String id: songIDs) {
                sb.append("\"spotify:track:" + id + "\",");
            }

            // 最後の文字　"," をトリム
            sb.deleteCharAt(sb.toString().length()-1);

            sb.append("]}");

            ps.print(sb.toString());

            System.out.println("きつい: " + sb.toString());


            //ps.print("{\"uris\": [\"spotify:track:4iV5W9uYEdYUVa79Axb7Rh\"," +
            //                     "\"spotify:track:1301WleyT98MSxVHPZCA6M\"" +
            //        "]}");

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