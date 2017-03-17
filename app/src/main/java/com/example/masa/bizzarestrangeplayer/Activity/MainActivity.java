
package com.example.masa.bizzarestrangeplayer.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.masa.bizzarestrangeplayer.Model.ArtistModel;
import com.example.masa.bizzarestrangeplayer.Model.TrackForPLModel;
import com.example.masa.bizzarestrangeplayer.Model.TrackModel;
import com.example.masa.bizzarestrangeplayer.R;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

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


    private enum TimerState {
        Workout, Break, Prepare, none;
    }


    SharedPreferences pref;

    TimerState state = TimerState.none;


    /* UI */

    ImageView jacketImageView;

    TextView loginStateTextView, setStateTextView, timerStateTextView;
    TextView nowMusicTextView;
    TextView timerTextView;
    Button cancelButton;
    ToggleButton playerToggleButton;

    /* Timer setting class */
    private CountDown countDown;

    int currentSet = 1; // current set


    // Time interval(主に内部処理用)
    private Long ti;

    // set by user(Milli Seconds)
    private Long workoutTime;
    private Long breakTime;
    private Long prepareTime;



    private static final String CLIENT_ID = "8482782774f44e5681ee617adcf6b3f6";
    private static final String REDIRECT_URI = "spotify-player-sample-login://callback";
    private static final int REQUEST_CODE = 1;


    static public Player mPlayer;
    private String mAccessToken;


    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
//                AuthenticationResponse.Type.TOKEN,
//                REDIRECT_URI);
//        builder.setScopes(new String[]{"user-read-private", "streaming"});
//        AuthenticationRequest request = builder.build();
//
//        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        pref = PreferenceManager.getDefaultSharedPreferences(this);


        /* UI componet initialize */

        jacketImageView = (ImageView) findViewById(R.id.jacketImageView);

        loginStateTextView = (TextView) findViewById(R.id.loginStateTextView);
        setStateTextView   = (TextView) findViewById(R.id.setStateTextView);
        timerStateTextView = (TextView) findViewById(R.id.timerStateTextView);
        nowMusicTextView   = (TextView) findViewById(R.id.nowMusicTextView);
        timerTextView      = (TextView) findViewById(R.id.timerTextView);

        cancelButton       = (Button) findViewById(R.id.cancelButton);
        playerToggleButton = (ToggleButton) findViewById(R.id.playerToggleButton);


        float br = -125;

        ColorMatrix cmx = new ColorMatrix(new float[] {

                  1, 0, 0, 0, br // brightness
                , 0, 1, 0, 0, br // brightness
                , 0, 0, 1, 0, br // brightness
                , 0, 0, 0, 1, 0 });

        jacketImageView.setColorFilter(new ColorMatrixColorFilter(cmx));
        jacketImageView.setAlpha(0.8f);

        Picasso.with(getApplicationContext()).load(R.drawable.fever).into(jacketImageView);

        jacketImageView.invalidate();




        // shared prefから、ポモドーロの間隔をロード
//        workoutTime = 3000l;
        workoutTime = Long.valueOf(pref.getString("workout_time", "4000"));

        System.out.println("おらワークアウト！" + workoutTime);

        breakTime = Long.valueOf(pref.getString("break_time", "8000"));

        prepareTime = 1000l;

        final Long MAX_TIMES = Long.valueOf(pref.getString("set", "4"));


        // まず、インターバル間隔が決まったら、その後で・・
        ti = Long.valueOf(workoutTime);

        // textViewを更新
        long mm = ti / 1000 / 60;
        long ss = ti / 1000 % 60;
        timerTextView.setText(String.format("%1$02d:%2$02d", mm, ss));

        setStateTextView.setText(String.format("Set: %1$02d / %2$02d", currentSet, MAX_TIMES));


        playerToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    //中止したtimeを取得
                    String   tmp1 = timerTextView.getText().toString();
                    //timeを分と秒に分ける
                    String[] tmp2 = tmp1.split(":", 0);

                    int mnt = Integer.parseInt(tmp2[0]) * 1000 * 60;
                    int scd = Integer.parseInt(tmp2[1]) * 1000;

                    if (ti != null) {
                        System.out.println("残り時間: " + ti);
                        countDown = new CountDown(ti, 1000);
                    }

                    state = TimerState.Workout;
                    timerStateTextView.setText("WORKOUT");

                    countDown.start();
                } else {
                    state = TimerState.none;
                    timerStateTextView.setText("NONE");

                    countDown.cancel();
                }
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 中止
                countDown.cancel();
                ti = Long.valueOf(breakTime);

                long mm = ti / 1000 / 60;
                long ss = ti / 1000 % 60;

                timerTextView.setText(String.format("%1$02d:%2$02d", mm, ss));

                playerToggleButton.setChecked(false);
            }
        });

        // アクセストークンの有無をラベルに表示→ここ、authがまさか非同期→ダメなのか？
        //renewLoginStateTextView();


        //connectTrackJsonAndParse();
        //connectArtistJsonAndParse();


    }


    @Override
    protected void onResume() {
        super.onResume();


        if (state == TimerState.Break) {

        } else {
            System.out.println("おらresume!" + pref.getString("workout_time", "ぼけ"));

            workoutTime = Long.valueOf(pref.getString("workout_time", "7000"));

            ti = Long.valueOf(workoutTime);

            long mm = ti / 1000 / 60;
            long ss = ti / 1000 % 60;
            timerTextView.setText(String.format("%1$02d:%2$02d", mm, ss));
        }

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
            URL url = new URL("https://api.spotify.com/v1/recommendations?" +
                    "seed_artists=4NHQUGzhtTLFvgF5SZesLK&" +
                    "seed_tracks=0c6xIDDpzE81m2q797ordA&" +
                    "min_energy=0.4&" +
                    "min_popularity=50&" +
                    "market=US&limit=10");

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
                    //Log.d("OKHttp", "" + responseBody);

                    final TrackForPLModel result = new Gson().fromJson(responseBody, TrackForPLModel.class);

                    for (int i = 0; i < result.getTracks().size(); i++) {
                        System.out.println(result.getTracks().get(i).getId());
                    }

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

                        renewLoginStateTextView();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());

                        renewLoginStateTextView();
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


    /* アクションバーに設定画面へのボタン追加 */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pref:

                System.out.println("ほげー");

                Intent i = new Intent(this, MyConfigActivity.class);
                startActivity(i);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private class CountDown extends CountDownTimer {

        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {

            switch (state) {

                case Workout:

                    playerToggleButton.setChecked(false); // toggleボタンをオフにする

                    ti = Long.valueOf(breakTime);
                    long mm = ti / 1000 / 60;
                    long ss = ti / 1000 % 60;
                    timerTextView.setText(String.format("%1$02d:%2$02d", mm, ss));

                    countDown = new CountDown(ti, 1000);
                    countDown.start();

                    state = TimerState.Break;
                    timerStateTextView.setText("BREAK");

                    playerToggleButton.setVisibility(View.GONE);


                    launchSetListActivity();

                    break;

                case Break:


                default:
                    break;
            };
        }

        // Timerのカウント周期で呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            // 残り時間を分、秒、ミリ秒に分割
            long mm = millisUntilFinished / 1000 / 60;
            long ss = millisUntilFinished / 1000 % 60;
            // long ms = millisUntilFinished - ss * 1000 - mm * 1000 * 60;

            //timerText.setText(String.format("%1$02d:%2$02d.%3$03d", mm, ss, ms));
            timerTextView.setText(String.format("%1$02d:%2$02d", mm, ss));

            ti = millisUntilFinished;
        }
    }


    /* Helper Method */

    public void renewLoginStateTextView() {
        if (mAccessToken != null) {
            loginStateTextView.setText("Login: OK");
        } else {
            loginStateTextView.setText("Login: NG");
        }
    }

    public void launchSetListActivity() {
        Intent i = new Intent(this, SetListResultActivity.class);
        startActivity(i);
    }
}