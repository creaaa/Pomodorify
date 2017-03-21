
package com.example.masa.bizzarestrangeplayer;

import android.os.AsyncTask;

import com.example.masa.bizzarestrangeplayer.Activity.SetListResultActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CreatePlaylistAsyncTask extends AsyncTask<String, String, String> {

    SetListResultActivity activity;

    String mAccessToken;
    String userID;

    public CreatePlaylistAsyncTask(SetListResultActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {

        this.mAccessToken = params[0];
        this.userID = params[1];

        URL url = null;
        HttpURLConnection con = null;

        OutputStream os = null;
        BufferedReader reader = null;
        PrintStream ps = null;

        try {

            url = new URL("https://api.spotify.com/v1/users/" +
                          userID +
                          "/playlists"
            );

            con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept",       "application/json; charset=utf-8");
            con.setRequestProperty("Authorization", "Bearer " + mAccessToken);
            con.setDoOutput(true);

            os = con.getOutputStream();   //POST用のOutputStreamを取得
            ps = new PrintStream(os);


            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            String playlistName = sdf.format(date);

            // プレイリスト名をjsonで付与
            ps.print("{\"name\":\"" + playlistName + "\"}");

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

            String playlistID = jsonObject.getString("id");

            // プレイリストIDを返す
            return playlistID;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            InputStream err = con.getErrorStream();
            System.out.println(err);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                reader.close();
                //ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    @Override
    protected void onPostExecute(String playlistID) {
        System.out.println("きてんの？ " + playlistID);
        activity.playlistID = playlistID;
        activity.putSongsToPlaylist(mAccessToken, userID, playlistID);
    }
}