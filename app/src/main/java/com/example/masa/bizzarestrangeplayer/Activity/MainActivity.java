
package com.example.masa.bizzarestrangeplayer.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.masa.bizzarestrangeplayer.Model.ArtistModel;
import com.example.masa.bizzarestrangeplayer.Model.Track;
import com.example.masa.bizzarestrangeplayer.Model.TrackForPLModel;
import com.example.masa.bizzarestrangeplayer.Model.TrackModel;
import com.example.masa.bizzarestrangeplayer.R;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
        Workout, Break, Prepare, Pause, Standby;
    }


    String[] musicIDs = {
            "3JIxjvbbDrA9ztYlNcp3yL",
            "01iyCAUm8EvOFqVWYJ3dVX",
            "5ztQHTm1YQqcTkQmgDEU4n",
    };


    SharedPreferences pref;

    TimerState state = TimerState.Standby;

    // TrackForPLModelのうち、workout minutesぶんに調整された、実際にかけるプレイリスト
    ArrayList<Track> currentSetPlaylist = new ArrayList<>();



    /* UI */

    ImageView jacketImageView;

    TextView loginStateTextView, setStateTextView, timerStateTextView;
    TextView nowMusicTextView;
    TextView timerTextView;
    Button cancelButton;
    ToggleButton playerToggleButton;


    /* Timer setting class */
    private CountDown countDown;
    private int currentSet = 1; // current set
    private int MAX_TIMES;


    // Time interval(主に内部処理用)
    // private Long ti;

    // set by user(Milli Seconds)
    private Long workoutTime;
    private Long breakTime;
    private Long prepareTime;


    /* Login Info */
    private static final String CLIENT_ID = "8482782774f44e5681ee617adcf6b3f6";
    private static final String REDIRECT_URI = "spotify-player-sample-login://callback";
    private static final int REQUEST_CODE = 1;
    private String mAccessToken;


    static public Player mPlayer;

    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* Auth Process */
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

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


        float brightness = -125;

        ColorMatrix cmx = new ColorMatrix(new float[] {

                  1, 0, 0, 0, brightness
                , 0, 1, 0, 0, brightness
                , 0, 0, 1, 0, brightness
                , 0, 0, 0, 1, 0 });

        jacketImageView.setColorFilter(new ColorMatrixColorFilter(cmx));
        jacketImageView.setAlpha(0.8f);

        Picasso.with(getApplicationContext()).load(R.drawable.fever).into(jacketImageView);

        jacketImageView.invalidate();


        // shared prefから、ポモドーロの間隔をロード

        workoutTime = Long.valueOf(pref.getString("workout_time", "4000"));
        breakTime = Long.valueOf(pref.getString("break_time", "8000"));
        prepareTime = Long.valueOf(pref.getString("prepare_time", "12000"));

        MAX_TIMES = Integer.parseInt(pref.getString("set", "4"));

        // ワークアウト時間を最初に画面に表示しておく(実際はスタートしたら、直後にprepareに移行するのだけど)
        renewTimer(workoutTime);

        renewSetInfo();
        renewTimerStateInfo(TimerState.Standby);


        playerToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {  // 一時停止中のとき、再開する

                    if (state == TimerState.Standby) {

                        // この中で曲も再生
                        //createPlaylists("");

                        // まずは、prepareからstateをスタート
                        state = TimerState.Prepare;
                        renewTimer(prepareTime);
                        renewTimerStateInfo(TimerState.Prepare);

                        countDown = new CountDown(prepareTime, 1000);
                        countDown.start();

                        invalidateOptionsMenu();

                        return;
                    }


                    String[] tmp = (timerTextView.getText().toString()).split(":", 0);

                    int minute = Integer.parseInt(tmp[0]) * 1000 * 60;
                    int second = Integer.parseInt(tmp[1]) * 1000;

                    countDown = new CountDown(minute + second, 1000);

                    state = TimerState.Workout;
                    renewTimerStateInfo(TimerState.Workout);

                    // onCreateOptionsMenu, onPrepareOptionsMenuを再度走らせる
                    invalidateOptionsMenu();

                    countDown.start();


                } else {  // 再生中のとき、一時停止する

                    if (state == TimerState.Standby) { return; }

                    state = TimerState.Pause;
                    renewTimerStateInfo(TimerState.Pause);
                    countDown.cancel();
                }
            }
        });


        // TODO: 最初はcancelButtonは非表示に。でないとnullぽで落ちる
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                countDown.cancel();

                state = TimerState.Standby;
                renewTimerStateInfo(TimerState.Standby);

                playerToggleButton.setVisibility(View.VISIBLE);
                playerToggleButton.setChecked(false);  // まさかここで、onCheckedChangeが呼ばれてる？→合ってた

                renewTimer(workoutTime);

                invalidateOptionsMenu();

            }
        });
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (state == TimerState.Break) {

        } else {
            // workoutTime = Long.valueOf(pref.getString("workout_time", "7000"));
            // renewTimer(workoutTime);
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



    /* アクションバーに設定画面へのボタン追加 */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (state != TimerState.Standby) {
            return false;
        }

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pref:
                Intent i = new Intent(this, MyConfigActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




    /* Inner Class */

    private class CountDown extends CountDownTimer {

        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {

            switch (state) {

                case Workout:

                    state = TimerState.Break;
                    countDown = new CountDown(breakTime, 1000);
                    // これ、なぜかここだとタイマーがスタートしない
                    // countDown.start();


                    //playerToggleButton.setChecked(false); // toggleボタンをオフにする
                    playerToggleButton.setVisibility(View.GONE);
                    renewTimer(breakTime);
                    renewTimerStateInfo(TimerState.Break);

                    launchSetListActivity();

                    // から、ここに書かないとだめ。
                    countDown.start();

                    break;

                case Break:

                    state = TimerState.Prepare;
                    countDown = new CountDown(prepareTime, 1000);
                    // これ、なぜかここだとタイマーがスタートしない
                    //countDown.start();


                    renewTimer(prepareTime);
                    renewTimerStateInfo(TimerState.Prepare);

                    countDown.start();

                    break;

                case Prepare:

                    state = TimerState.Workout;
                    countDown = new CountDown(workoutTime, 1000);
                    countDown.start();


                    //playerToggleButton.setChecked(true); // toggleボタンをオンにする
                    playerToggleButton.setVisibility(View.VISIBLE);
                    renewTimer(workoutTime);
                    renewTimerStateInfo(TimerState.Workout);

                    // たぶんここ！！セトリを生成し、再生を開始する絶好のタイミングは。
                    createPlaylists("");

                    break;
            };
        }


        // Timerのカウント周期で呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            renewTimer(millisUntilFinished);
        }
    }


    /* Helper Method */

    private void launchSetListActivity() {
        Intent i = new Intent(this, SetListResultActivity.class);
        startActivity(i);
    }

    private void playMusic() {
//         mPlayer.playUri(null, "spotify:track:6ZSvhLZRJredt15aJiBQqv", 0, 0);

        enqueueMusicToPlayer();

        mPlayer.playUri(null, "spotify:track:" + currentSetPlaylist.get(0).getId(), 0, 0);

        System.out.println("1曲め: " + currentSetPlaylist.get(0).getName() + "即スキップ");


        // ここはダメ。メインスレッドで呼ばないとダメ。
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                renewMusicInfo();
            }
        });

        mPlayer.skipToNext(null);
    }

    private void enqueueMusicToPlayer() {
        for (Track eachTrack: currentSetPlaylist) {
            mPlayer.queue(null, eachTrack.getId());
            System.out.println(eachTrack.getName() + "がenqueueされました");
        }
    }

    private void createPlaylists(String seed) {

        try {
            //  "j-idol", "j-pop", "j-rock", industrial, chill, techno
            URL url = new URL("https://api.spotify.com/v1/recommendations?" +
                    //"seed_genres=techno&" +  // なんかこのジャンル指定がやばいっぽいな
                    "seed_artists=115IWAVy4OTxhE0xdDef1c&" +  // パスピエ
                    //"seed_tracks=3p4ELetqoTwFpsnUkEirzc&" +   // スーパーカー
                    //"min_instrumentalness=0.8&" +
                    //"market=JP&" +
                    "limit=15");

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

                    // ここ、最後は toStringじゃないぞ、まじで気をつけろ
                    String responseBody = response.body().string();

                    TrackForPLModel result = new Gson().fromJson(responseBody, TrackForPLModel.class);


                    long currentTotalDuration = 0;

                    // 前スプリントでたまっていた曲をリセット(nullだとだめよ。)
                    // currentSetPlaylist = new ArrayList<Track>();

                    // 25分 = 1500000 15分 = 900000
                    for (Track eachTrack: result.getTracks()) {

                        currentSetPlaylist.add(eachTrack);
                        System.out.println(eachTrack.getName() + " が今回のプレイリストに選出！");

                        currentTotalDuration += eachTrack.getDurationMs();
                        System.out.println("現在の合計時間: " + currentTotalDuration);

                        if (currentTotalDuration > 1500000) {
                            break;
                        }
                    }

                    if (mAccessToken != null && !currentSetPlaylist.isEmpty()) {
                        playMusic();
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    /* renew UI */
    private void renewLoginStateTextView() {
        if (mAccessToken != null) {
            loginStateTextView.setText("Login: OK");
        } else {
            loginStateTextView.setText("Login: NG");
        }
    }

    private void renewSetInfo() {
        // setStateTextView.setText(currentSet + " / " + MAX_TIMES);
         setStateTextView.setText(String.format("Set: %1$02d / %2$02d", currentSet, MAX_TIMES));
    }

    private void renewTimerStateInfo(TimerState s) {
        timerStateTextView.setText("State: " + s);
    }

    private void renewMusicInfo() {

        Track currentSong = currentSetPlaylist.get(0);

        Picasso.with(getApplicationContext())
                .load(currentSong.getAlbum().getImages().get(0).getUrl()).into(jacketImageView);

        String info = currentSong.getName() + " - " + currentSong.getArtists().get(0).getName();

        nowMusicTextView.setText(info);
    }

    private void renewTimer(Long kind_of_timer) {
        Long time_interval = Long.valueOf(kind_of_timer);
        long m = time_interval / 1000 / 60;
        long s = time_interval / 1000 % 60;
        timerTextView.setText(String.format("%1$02d:%2$02d", m, s));
    }
}