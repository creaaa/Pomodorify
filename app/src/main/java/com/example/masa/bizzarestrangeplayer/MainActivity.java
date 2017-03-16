
package com.example.masa.bizzarestrangeplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.masa.bizzarestrangeplayer.Model.ArtistModel;
import com.example.masa.bizzarestrangeplayer.Model.TrackForPLModel;
import com.example.masa.bizzarestrangeplayer.Model.TrackModel;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "8482782774f44e5681ee617adcf6b3f6";

    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "spotify-player-sample-login://callback";

    private static final int REQUEST_CODE = 1;

    static public Player mPlayer;
    private String mAccessToken;


    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        //connectTrackJsonAndParse();
        //connectArtistJsonAndParse();

    }

    String[] musicIDs = {
            "3JIxjvbbDrA9ztYlNcp3yL",
            "01iyCAUm8EvOFqVWYJ3dVX",
            "5ztQHTm1YQqcTkQmgDEU4n",
    };


    public void times(View v) {

        for (int i = 0; i < 1; i++) {
            createPlaylists("hoge");
        }
    }


    public void createPlaylists(String seed) {

        try {

            // セトリのrecomendation
            URL url = new URL("https://api.spotify.com/v1/recommendations?seed_artists=4NHQUGzhtTLFvgF5SZesLK&seed_tracks=0c6xIDDpzE81m2q797ordA&min_energy=0.4&min_popularity=50&market=US&limit=1");

            //URL url = new URL("https://api.spotify.com/v1/search?q=passepied&type=artist");

            final Request request = new Request.Builder()
                    // URLを生成
                    .url(url.toString())
                    .get()
                    .addHeader("Authorization", "Bearer " + mAccessToken)
                    .build();

            // クライアントオブジェクトを作成する
            final OkHttpClient client = new OkHttpClient();
            // 新しいリクエストを行う
            client.newCall(request).enqueue(new Callback() {
                // 通信が成功した時
                @Override
                public void onResponse(Call call, Response response) throws IOException {

//                    // 通信結果をログに出力する
//                    final String responseBody = response.body().string();
//                    // パスピエのアーティストID: 115IWAVy4OTxhE0xdDef1c
//                    Log.d("OKHttp", "result: " + responseBody);
//                    final ArtistModel result = new Gson().fromJson(responseBody, ArtistModel.class);
//                    System.out.println(result.getArtists().getItems().get(0).getName());



                    ///// 動く /////

                    // 通信結果をログに出力する
                    //final String responseBody = response.body().string();
//                    // パスピエのアーティストID: 115IWAVy4OTxhE0xdDef1c
//                    Log.d("OKHttp", "result: " + responseBody);

                    //final ArtistModel result = new Gson().fromJson(responseBody, ArtistModel.class);
                    //System.out.println(result.getArtists().getItems().get(0).getName());

                    ///// 動く /////


                    // ↓動かない
                    final String responseBody = response.body().string();
//                    // パスピエのアーティストID: 115IWAVy4OTxhE0xdDef1c
                    Log.d("OKHttp", "" + responseBody);

                    final TrackForPLModel result = new Gson().fromJson(responseBody, TrackForPLModel.class);
                    System.out.println(result);





                }

                // 通信が失敗した時
                @Override
                public void onFailure(Call call, final IOException e) {
                    // new Handler().post って書いてたから、
                    // java.lang.RuntimeException: Can’t create handler inside thread that has not called Looper.prepare()
                    // で落ちてた？？？
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OKHttp", "エラー♪");
                        }
                    });
                }
            });
        } catch (Exception e) {

        }
    }


    public void connectMusicAnalyzeAndParse() {

        System.out.println("押されてる");

        try {
            // これはできる 3JIxjvbbDrA9ztYlNcp3yL
            // スーパーカー 3p4ELetqoTwFpsnUkEirzc
            // ダンシング・クイーン 01iyCAUm8EvOFqVWYJ3dVX
            String songID = musicIDs[new Random().nextInt(3)];

            URL url = new URL("https://api.spotify.com/v1/audio-analysis/" + songID);

            final Request request = new Request.Builder()
                    // URLを生成
                    .url(url.toString())
                    .get()
                    .addHeader("Authorization","Bearer " + mAccessToken)
                    .build();


            // クライアントオブジェクトを作成する
            final OkHttpClient client = new OkHttpClient();
            // 新しいリクエストを行う
            client.newCall(request).enqueue(new Callback() {
                // 通信が成功した時
                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    // 通信結果をログに出力する
                    final String responseBody = response.body().string();
                    //
                    Log.d("OKHttp", responseBody);
                }

                // 通信が失敗した時
                @Override
                public void onFailure(Call call, final IOException e) {
                    // new Handler().post って書いてたから、
                    // java.lang.RuntimeException: Can’t create handler inside thread that has not called Looper.prepare()
                    // で落ちてた？？？
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OKHttp", "エラー♪");
                        }
                    });
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


    public void connectTrackJsonAndParse() {

        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                // URLオブジェクト生成
                URL url = null;
                String serverUrl = "https://api.spotify.com/v1/artists/43ZHCT0cAZBISjO8DG9PnE/top-tracks?country=JP";

                try {
                    url = new URL(serverUrl);
                    // サーバーへのネットワーク接続
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    // UTF-8で読み込む
                    InputStreamReader is = new InputStreamReader(connection.getInputStream(), "UTF-8");
                    // GsonライブラリのJSONリーダー（パーサー）
                    JsonReader jsonReader = new JsonReader(is);
                    // Gson生成
                    Gson gson = new Gson();
                    // fromJsonメソッドでJSONからJavaオブジェクトへの変換
                    TrackModel data = gson.fromJson(jsonReader, TrackModel.class);

                    System.out.println(data.getTracks().get(0).getName());
                    System.out.println(data.getTracks().get(0).getPopularity());
                    System.out.println(data.getTracks().get(0).getAvailableMarkets());

                    List<TrackModel.Track> tracks = data.getTracks();


                    for (TrackModel.Track track: tracks) {
                        System.out.println(track.getName());
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    public void connectArtistJsonAndParse() {

        try {
            URL url = new URL("https://api.spotify.com/v1/search?q=passepied&type=artist");

            final Request request = new Request.Builder()
                    // URLを生成
                    .url(url.toString())
                    .get()
                    .addHeader("Authorization","Bearer " + mAccessToken)
                    .build();


            // クライアントオブジェクトを作成する
            final OkHttpClient client = new OkHttpClient();
            // 新しいリクエストを行う
            client.newCall(request).enqueue(new Callback() {
                // 通信が成功した時
                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    // 通信結果をログに出力する
                    final String responseBody = response.body().string();

                    // パスピエのアーティストID: 115IWAVy4OTxhE0xdDef1c
                    Log.d("OKHttp", "result: " + responseBody);

                    final ArtistModel result = new Gson().fromJson(responseBody, ArtistModel.class);

                    System.out.println(result.getArtists().getItems().get(0).getName());
                }

                // 通信が失敗した時
                @Override
                public void onFailure(Call call, final IOException e) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("エラー♪");
                        }
                    });
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                // 追加！！いいの？？
                mAccessToken = response.getAccessToken();

                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }


    // onPlaybackEventの直後に来ます
    @Override
    public void onLoggedIn() {

        Log.d("MainActivity", "User logged in");
        // ギミチョコ!!
        // mPlayer.playUri(null, "spotify:track:6ZSvhLZRJredt15aJiBQqv", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    // Event listener
    public void startSprint(View v) {
        Toast.makeText(this, "hoge", Toast.LENGTH_SHORT).show();
        //
        Intent i = new Intent(this, OnSprintActivity.class);
        startActivity(i);
    }
}