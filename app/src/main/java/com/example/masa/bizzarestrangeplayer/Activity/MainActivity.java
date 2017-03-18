
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
        Workout, Break, Prepare, Suspend, none;
    }

    SharedPreferences pref;

    TimerState state = TimerState.none;


    ArrayList<String> musicIDArray = new ArrayList<>();

    // レスポンスjsonからGSON化したプレイリストデータ
    // TrackForPLModel result;

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

        workoutTime = Long.valueOf(pref.getString("workout_time", "4000"));
        breakTime = Long.valueOf(pref.getString("break_time", "8000"));
        prepareTime = Long.valueOf(pref.getString("prepare_time", "12000"));

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

                if (isChecked) {  // 一時停止中のとき、再開する

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

                    // TODO:
                    createPlaylists("");


                } else {  // 再生中のとき、一時停止する

                    // TODO: ここの条件式　まじでわかりにくい　応急処置だから　なおせ
                    if (state != TimerState.none) {
                        state = TimerState.Suspend;
                        timerStateTextView.setText("SUSPEND");
                        countDown.cancel();
                    }
                }
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 中止
                countDown.cancel();

                state = TimerState.none;
                timerStateTextView.setText("NONE");

                playerToggleButton.setVisibility(View.VISIBLE);
                playerToggleButton.setChecked(false);  // まさかここで、onCheckedChangeが呼ばれてる？→合ってた




                ti = Long.valueOf(workoutTime);

                long mm = ti / 1000 / 60;
                long ss = ti / 1000 % 60;

                timerTextView.setText(String.format("%1$02d:%2$02d", mm, ss));

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

                    ti = Long.valueOf(prepareTime);

                    long m = ti / 1000 / 60;
                    long s = ti / 1000 % 60;
                    timerTextView.setText(String.format("%1$02d:%2$02d", m, s));

                    countDown = new CountDown(ti, 1000);
                    countDown.start();

                    state = TimerState.Prepare;
                    timerStateTextView.setText("PREPARE");

                    break;

                case Prepare:

                    ti = Long.valueOf(workoutTime);

                    long mmm = ti / 1000 / 60;
                    long sss = ti / 1000 % 60;
                    timerTextView.setText(String.format("%1$02d:%2$02d", mmm, sss));

                    countDown = new CountDown(ti, 1000);
                    countDown.start();

                    state = TimerState.Workout;
                    timerStateTextView.setText("WORKOUT");

                    playerToggleButton.setChecked(true); // toggleボタンをオンにする
                    playerToggleButton.setVisibility(View.VISIBLE);

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

    private void renewLoginStateTextView() {
        if (mAccessToken != null) {
            loginStateTextView.setText("Login: OK");
        } else {
            loginStateTextView.setText("Login: NG");
        }
    }

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


    private void renewMusicInfo() {

        Track currentSong = currentSetPlaylist.get(0);

        Picasso.with(getApplicationContext())
                .load(currentSong.getAlbum().getImages().get(0).getUrl()).into(jacketImageView);

        String info = currentSong.getName() + " - " + currentSong.getArtists().get(0).getName();

        nowMusicTextView.setText(info);

    }


    private void createPlaylists(String seed) {

        System.out.println("とおる");

        try {

            //  "j-idol", "j-pop", "j-rock", industrial, chill, techno

            // セトリのrecomendation
            URL url = new URL("https://api.spotify.com/v1/recommendations?" +
                    "seed_genres=j-pop" +  // なんかこのジャンル指定がやばいっぽいな
                    //"seed_artists=115IWAVy4OTxhE0xdDef1c&" +  // パスピエ
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

                    //System.out.println("onresponseしとる");
                    //System.out.println(responseBody);

                    long currentTotalDuration = 0;

                    // 前スプリントでたまっていた曲をリセット(nullだとだめよ。)
                    // currentSetPlaylist = new ArrayList<Track>();

                    // 25分 = 1500000 15分 = 900000
                    for (Track eachTrack: result.getTracks()) {

                        //musicIDArray.add(eachTrack.getId());

                        currentSetPlaylist.add(eachTrack);
                        System.out.println(eachTrack.getName() + " が今回のプレイリストに選出！");

                        currentTotalDuration += eachTrack.getDurationMs();
                        System.out.println("現在の合計時間: " + currentTotalDuration);


                        if (currentTotalDuration > 1500000) {
                            break;
                        }
                    }


                    if (mAccessToken != null && !currentSetPlaylist.isEmpty()) {
                        System.out.println("きとるね！曲再生いったれ。");
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
}