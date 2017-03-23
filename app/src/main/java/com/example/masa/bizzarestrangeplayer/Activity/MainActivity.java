
package com.example.masa.bizzarestrangeplayer.Activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import com.example.masa.bizzarestrangeplayer.Fragment.ResetDialogFragment;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {


    private enum TimerState {
        Standby, Workout, Pause, Break, Prepare
    }


    private Handler handler = new Handler();
    public static Handler timerRemoteStopHandler;  // setlist〜からタイマーを遠隔操作するためのハンドラ


    SharedPreferences pref;
    TimerState state = TimerState.Standby;
    private int currentSet = 1; // current set


    /* Music Player */
    public static Player mPlayer;
    private int playlistHead = 0;


    // TrackForPLModelのうち、workout minutesぶんに調整された、実際にかけるプレイリスト
    ArrayList<Track> currentSetPlaylist = new ArrayList<>();


    /* UI */

    ImageView jacketImageView;
    TextView loginStateTextView, setStateTextView, timerStateTextView;
    TextView nowMusicTextView;
    TextView timerTextView;
    Button cancelButton;
    ToggleButton playerToggleButton;
    Button nextSongButton;
    Button musicPauseButton;


    /* Timer setting class */
    private CountDown countDown;


    // set by user(Milli Seconds)
    private Long workoutTime;
    private Long breakTime;
    private Long prepareTime;
    private int MAX_TIMES;


    /* Login Info */
    private static final String CLIENT_ID = "8482782774f44e5681ee617adcf6b3f6";
    private static final String REDIRECT_URI = "spotify-player-sample-login://callback";
    private static final int REQUEST_CODE = 1;
    private String mAccessToken;



    @Override
    protected void onRestart() {

        super.onRestart();

        System.out.println("リスタートｗ");


        // ミッションコントロールから復帰の場合、タイマーを再点火
        if (countDown != null) {

//            System.out.println("うん");
//
//            // ターム終了直後は時間表示が空のためフォーマットできないため早期リターン
//            if (timerTextView.getText().toString().equals("")) {
//                return;
//            }
//
//            String[] tmp = (timerTextView.getText().toString()).split(":", 0);
//
//            int minute = Integer.parseInt(tmp[0]) * 1000 * 60;
//            int second = Integer.parseInt(tmp[1]) * 1000;
//            countDown = new CountDown(minute + second, 1000);
//            countDown.start();

            fromMissionControl();
        }


        // TODO: 設定画面からの復帰でもここが発動してしまう。遷移元に応じて場合分けが必要。
        // これでいいのか！？
        // TODO: だめ。2週目だと↑の条件が先にマッチしてしまう。

        if (currentSet == 1) {

//            System.out.println("設定フラグメントからの復帰。あってる？");
//
//            // タイマーを再セット。Standby状態だし支障ない、そうに違いない
//            workoutTime = Long.valueOf(pref.getString("workout_time", "5000"));
//            breakTime = Long.valueOf(pref.getString("break_time", "10000"));
//            prepareTime = Long.valueOf(pref.getString("prepare_time", "5000"));
//            MAX_TIMES = Integer.parseInt(pref.getString("set", "4"));
//
//            renewViews(workoutTime);

            fromPrefFragment();
            return;
        }


        // 最後のセットで、SetListResultActivityから復帰してここが通ると、
        // timer stringが空なので、formatができず、落ちる。
        // それを回避
        if (currentSet >= MAX_TIMES) {
            System.out.println("あぶないとこやで。");
            return;
        }


        // TODO: 「タイムアウトで」サブ画面から帰ってきた時は、ここに何の処理も書かなくていいのか？
        // とりま動いてるけど...。

        String[] tmp = (timerTextView.getText().toString()).split(":", 0);

        int minute = Integer.parseInt(tmp[0]) * 1000 * 60;
        int second = Integer.parseInt(tmp[1]) * 1000;

        countDown = new CountDown(minute + second, 1000);

        countDown.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* 0. prepare timerRemoteStopHandler */
        timerRemoteStopHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                        // タイマーストップ
                        System.out.println("タイマーストップ！！！");
                        if (countDown != null) {
                            countDown.cancel();
                        }
                        break;
                }
            }
        };


        /* 1. Auth Process */

        // doAuth();


        /* 2. prepare Preference and initialize user's interval setting */

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        workoutTime = Long.valueOf(pref.getString("workout_time", "5000"));
        breakTime = Long.valueOf(pref.getString("break_time", "10000"));
        prepareTime = Long.valueOf(pref.getString("prepare_time", "5000"));
        MAX_TIMES = Integer.parseInt(pref.getString("set", "4"));


        /* 3. UI componet initialize */

        jacketImageView = (ImageView) findViewById(R.id.jacketImageView);

        loginStateTextView = (TextView) findViewById(R.id.loginStateTextView);
        setStateTextView   = (TextView) findViewById(R.id.setStateTextView);
        timerStateTextView = (TextView) findViewById(R.id.timerStateTextView);
        nowMusicTextView   = (TextView) findViewById(R.id.nowMusicTextView);
        nowMusicTextView.setTypeface(Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf"));
        timerTextView      = (TextView) findViewById(R.id.timerTextView);

        cancelButton       = (Button) findViewById(R.id.cancelButton);
        playerToggleButton = (ToggleButton) findViewById(R.id.playerToggleButton);
        nextSongButton     = (Button) findViewById(R.id.nextSongButton);
        musicPauseButton   = (Button) findViewById(R.id.musicPauseButton);



        float brightness = -125;

        ColorMatrix cmx = new ColorMatrix(new float[] {

                  1, 0, 0, 0, brightness
                , 0, 1, 0, 0, brightness
                , 0, 0, 1, 0, brightness
                , 0, 0, 0, 1, 0 });

        jacketImageView.setColorFilter(new ColorMatrixColorFilter(cmx));
        jacketImageView.setAlpha(0.8f);

        // Picasso.with(getApplicationContext()).load(R.drawable.phantomjacket).into(jacketImageView);

        jacketImageView.invalidate();

//        renewViews(workoutTime);

        renewSetInfo();
        renewTimerStateInfo(state);



        /* 4. set event listener */

        playerToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    // 1セットめを開始
                    if (state == TimerState.Standby) {

                        // まずは、prepareからstateをスタート
                        state = TimerState.Prepare;
                        countDown = new CountDown(prepareTime, 1000);
                        countDown.start();

                        //renewTimerInfo(prepareTime);
                        //renewTimerStateInfo(TimerState.Prepare);
                        renewViews(prepareTime);
                        invalidateOptionsMenu();

                        playerToggleButton.setVisibility(View.INVISIBLE);

                        return;
                    }


                    // 一時停止中(state: Pause)のとき、タイマーを再開
                    String[] tmp = (timerTextView.getText().toString()).split(":", 0);

                    state = TimerState.Workout;
                    int minute = Integer.parseInt(tmp[0]) * 1000 * 60;
                    int second = Integer.parseInt(tmp[1]) * 1000;
                    countDown = new CountDown(minute + second, 1000);
                    countDown.start();


                    renewTimerStateInfo(TimerState.Workout);

                    // onCreateOptionsMenu, onPrepareOptionsMenuを再度走らせる
                    // ↓これ、いらないっしょ。だからコメントアウトした
                    // invalidateOptionsMenu();


                } else {  // 再生中のとき、一時停止する


                    System.out.println("くるよな！そりゃそうよな！");

                    // TODO: ここに回避処理
                    if (currentSet >= MAX_TIMES) {
                        System.out.println("よかよか、きとる");
                        System.out.println("状態: " + state);
                        return;
                    }


                    // ここ、実は必要。
                    // 1. なぜならキャンセルボタンを押したとき 2. state = Workout時にonStopしたとき
                    // トグルボタン=OFFになり、ここが走る。
                    // だから早期リターンさせてる。必要。
                    // まぁこの場合、2は書かなくていいかもしれないかもだけど...
                    if (state == TimerState.Standby || state == TimerState.Pause) { return; }

                    state = TimerState.Pause;
                    countDown.cancel();

                    renewTimerStateInfo(TimerState.Pause);
                }
            }
        });


        // TODO: 最初はcancelButtonは非表示に。でないとnullぽで落ちる
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                DialogFragment dialog = new ResetDialogFragment();
                dialog.show(getFragmentManager(), "dialog_basic");

                countDown.cancel();

                state = TimerState.Standby;
                currentSet = 1;

                playerToggleButton.setVisibility(View.VISIBLE);
                playerToggleButton.setChecked(false);  // まさかここで、onCheckedChange が呼ばれてる？→合ってた

                invalidateOptionsMenu();
                renewViews(workoutTime);

            }
        });



        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 俺はこれを実質使ってない(てゆうかわからない)
                // mPlayer.skipToNext(null);
                playlistHead += 1;

                // ここの第３引数はとりま0じゃないとだめっぽい、てかこのindexってなんなんだ...
                mPlayer.playUri(null, "spotify:track:" + currentSetPlaylist.get(playlistHead).getId(), 0, 0);
                renewMusicInfo();
            }
        });


        musicPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.pause(null);
            }
        });

    }


//    public void connectMusicAnalyzeAndParse() {
//
//        System.out.println("押されてる");
//
//        try {
//            // これはできる 3JIxjvbbDrA9ztYlNcp3yL
//            // スーパーカー 3p4ELetqoTwFpsnUkEirzc
//            // ダンシング・クイーン 01iyCAUm8EvOFqVWYJ3dVX
//            String songID = musicIDs[new Random().nextInt(3)];
//
//            URL url = new URL("https://api.spotify.com/v1/audio-analysis/" + songID);
//
//            final Request request = new Request.Builder()
//                    // URLを生成
//                    .url(url.toString())
//                    .get()
//                    .addHeader("Authorization","Bearer " + mAccessToken)
//                    .build();
//
//
//            // クライアントオブジェクトを作成する
//            final OkHttpClient client = new OkHttpClient();
//            // 新しいリクエストを行う
//            client.newCall(request).enqueue(new Callback() {
//                // 通信が成功した時
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//
//                    // 通信結果をログに出力する
//                    final String responseBody = response.body().string();
//                    //
//                    Log.d("OKHttp", responseBody);
//                }
//
//                // 通信が失敗した時
//                @Override
//                public void onFailure(Call call, final IOException e) {
//                    // new Handler().post って書いてたから、
//                    // java.lang.RuntimeException: Can’t create handler inside thread that has not called Looper.prepare()
//                    // で落ちてた？？？
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d("OKHttp", "エラー♪");
//                        }
//                    });
//                }
//            });
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//    }

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



    /* callback method */


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

        if (requestCode == 777) {

            System.out.println("まじか。くるんか。");

            // TODO: ここに各種タイマーインターバルの再設定、ビューの更新処理を書く

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


        // 曲が始まった時:
        // kSpPlaybackNotifyBecameActive → kSpPlaybackNotifyNext → kSpPlaybackNotifyMetadataChanged →
        // kSpPlaybackNotifyContextChanged → kSpPlaybackNotifyTrackChanged → kSpPlaybackEventAudioFlush →
        // kSpPlaybackNotifyPlay


        // 曲再生中:


        // 曲が終わった時:
        // kSpPlaybackNotifyTrackDelivered → kSpPlaybackNotifyAudioDeliveryDone → kSpPlaybackNotifyPause →
        // kSpPlaybackNotifyMetadataChanged → kSpPlaybackNotifyTrackChanged



        switch (playerEvent) {

            case kSpPlaybackNotifyBecameActive:
                System.out.println("kSpPlaybackNotifyBecameActive ようわからん");
                break;

            case kSpPlaybackNotifyTrackChanged:
                System.out.println("kSpPlaybackNotifyTrackChangedだよ↑");
                break;

            case kSpPlaybackNotifyNext:
                System.out.println("kSpPlaybackNotifyNextだっちゃ☆");
                break;

            case kSpPlaybackNotifyMetadataChanged:
                System.out.println("kSpPlaybackNotifyMetadataChanged だいぶ謎だわ");
                break;

            case kSpPlaybackNotifyContextChanged:
                System.out.println("kSpPlaybackNotifyContextChanged　最大の謎だわ");
                break;

            case kSpPlaybackNotifyPlay:
                System.out.println("kSpPlaybackNotifyPlay　ふーん");
                break;

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
    protected void onStop() {

        if (countDown != null) {
            System.out.println("やった♪消滅");
            countDown.cancel();
        }


        System.out.println("ここでくんのかよ...");

        // タイマーの種類に応じて退避行動の挙動を変化
        switch (state) {

            case Standby:
                break;

            case Workout:

                //これだと、startActivityしたときも発動してしまう。他の手を考えないと。。。

                // state = TimerState.Pause;
                // playerToggleButton.setChecked(false);

                // renewTimerStateInfo(TimerState.Pause);

                break;

            case Pause:
                break;

            case Break:
                break;

            case Prepare:
                break;
        }

        super.onStop();
    }


    @Override
    protected void onDestroy() {


        System.out.println("きとるな");

        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);

        //FIXME: これ書かないとどうなるか？
        // たぶん、タイマーは生きたままになって回り続けてる。、
        // そのため、一見、アプリ立ち上げてなくてもonFinishが勝手に起動してしまう。
        // し、ビューに反映されてないから、おかしく感じる。



        // ここやりすぎか、なぜなら再起動した時に、タイマーも消えてしまうから
//        if (countDown != null) {
//            System.out.println("やった♪消滅");
//            countDown.cancel();
//        }

        // とりあえず、stopで判断を留保しよう
        // てかこれそもそもondestroyにかいちゃだめや、これ
        //countDown.cancel();

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

                // 戻るボタンを押すとfinish()が内部的に呼ばれてgit remote -vいるため、
                // onActivityResultで処理を加えられる
                startActivityForResult(i, 777);

                return true;

            case R.id.login:

                doAuth();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /* Inner Class */

    private class CountDown extends CountDownTimer {


        Boolean canGoPrepareMode = false;


        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }


        @Override
        public void onFinish() {

            switch (state) {

                case Workout:

                    if (currentSet >= MAX_TIMES) {


                        state = TimerState.Standby;
                        System.out.println("状態: " + state);
                        countDown.cancel();


                        // ここだと、putExtraで1が渡るからおかしいっぽいな？
                        //currentSet = 1;


                        timerTextView.setText("");
                        // ここで、onSetCheckedが走ってしまうので、onSetCheckedに回避処理を書いている
                        playerToggleButton.setChecked(false);


                        playerToggleButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.INVISIBLE);

                        renewTimerStateInfo(state);
//                        renewSetInfo();

                        launchSetListActivity();

                        // よって、launchSetListActivity してから、currentSetをリセットすればよろし。
                        currentSet = 1;
                        renewSetInfo();

                        return;
                    }


                    state = TimerState.Break;
                    countDown = new CountDown(breakTime, 1000);
                    countDown.start();

                    playerToggleButton.setVisibility(View.INVISIBLE);
                    renewViews(breakTime);

                    launchSetListActivity();

                    break;


                case Break:

                    state = TimerState.Prepare;

                    countDown = new CountDown(prepareTime, 1000);
                    countDown.start();

                    increaseCurrentSet();

                    renewViews(prepareTime);

                    break;

                case Prepare:

                    state = TimerState.Workout;
                    countDown = new CountDown(workoutTime, 1000);
                    countDown.start();

                    playerToggleButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);

                    renewViews(workoutTime);

                    // たぶんここ！！セトリを生成し、再生を開始する絶好のタイミングは。
                    createThisSetPlaylist();

                    // ここ、非同期だからだめよ
                   // mPlayer.playUri(null, "spotify:track:" + currentSetPlaylist.get(playlistHead).getId(), 0, 0);

                    break;
            };
        }


        // Timerのカウント周期で呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            renewTimerInfo(millisUntilFinished);
        }
    }


    /* Helper Method */

    private void doAuth() {

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        //builder.setScopes(new String[]{"user-read-private", "streaming"});
        builder.setScopes(new String[]{
                "user-read-private",
                "playlist-modify-private",
                "playlist-modify-public",
                "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);


    }

    private void increaseCurrentSet() { currentSet += 1; }


    private void launchSetListActivity() {

        Intent i = new Intent(this, SetListResultActivity.class);

        i.putExtra("playlist", currentSetPlaylist);
        if (mAccessToken != null) {
            i.putExtra("token", mAccessToken);
        }
        i.putExtra("current_set", currentSet);
        i.putExtra("max_set", MAX_TIMES);

        startActivity(i);
    }



    private void createThisSetPlaylist() {

        // TODO: 自分のアカウント（日本アカウント）じゃきけない曲まで選出されてるっぽい。
        // だから、そのとき、なにもきこえてこないんじゃないの。

        try {
            //  "j-idol", "j-pop", "j-rock", industrial, chill, techno
            URL url = new URL("https://api.spotify.com/v1/recommendations?" +
                    "seed_genres=techno&" +  // なんかこのジャンル指定がやばいっぽいな
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

                    if (result.getTracks() == null) {
                        System.out.println("早期リターン！");
                        return;
                    }

                    // 前の設定を空に
                    currentSetPlaylist = new ArrayList<>();


                    long currentTotalDuration = 0;

                    // 25分 = 1500000milli 15分 = 900000milli
                    for (Track eachTrack: result.getTracks()) {

                        currentSetPlaylist.add(eachTrack);
                        currentTotalDuration += eachTrack.getDurationMs();

                        System.out.println(eachTrack.getName() + " が今回のプレイリストに選出！");
                        System.out.println("現在の合計時間: " + currentTotalDuration);

                        // workoutTime / 1500000
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


    private void playMusic() {

        //enqueueMusicToPlayer();

        // ここは、こうやってわざわざメインスレッドで呼ばないとダメ。
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                renewMusicInfo();
            }
        });

        //mPlayer.skipToNext(null);
        // ここの第３引数はとりま0じゃないとだめっぽい、てかこのindexってなんなんだ...
        mPlayer.playUri(null, "spotify:track:" + currentSetPlaylist.get(playlistHead).getId(), 0, 0);

    }


//    private void enqueueMusicToPlayer() {
//        for (Track eachTrack: currentSetPlaylist) {
//            mPlayer.queue(null, eachTrack.getId());
//            System.out.println(eachTrack.getName() + "がenqueueされました");
//        }
//    }



    /* renew UI */

    private void renewViews(Long kind_of_timer) {
        renewSetInfo();
        renewTimerStateInfo(state);
        renewTimerInfo(kind_of_timer);
    }

    private void renewSetInfo() {
        // setStateTextView.setText(currentSet + " / " + MAX_TIMES);
         setStateTextView.setText(String.format("Set: %1$02d / %2$02d", currentSet, MAX_TIMES));
    }

    private void renewTimerStateInfo(TimerState s) {
        timerStateTextView.setText("State: " + s);
    }

    private void renewTimerInfo(Long kind_of_timer) {
        Long time_interval = Long.valueOf(kind_of_timer);
        long m = time_interval / 1000 / 60;
        long s = time_interval / 1000 % 60;
        timerTextView.setText(String.format("%1$02d:%2$02d", m, s));
    }


    private void renewLoginStateTextView() {
        if (mAccessToken != null) {
            loginStateTextView.setText("Login: OK");
        } else {
            loginStateTextView.setText("Login: NG");
        }
    }


    private void renewMusicInfo() {

        Track currentSong = currentSetPlaylist.get(playlistHead);

        Picasso.with(getApplicationContext())
                .load(currentSong.getAlbum().getImages().get(0).getUrl()).into(jacketImageView);

        String info = currentSong.getName() + " - " + currentSong.getArtists().get(0).getName();

        nowMusicTextView.setText(info);
    }


    /* onRestart時の制御 */

    private void fromMissionControl() {

        System.out.println("Mission Controlからの復帰。");

        if (currentSet==1) {
            System.out.println("はーいうまく回避。");
            return;
        }


        System.out.println("うん");

        // ターム終了直後は時間表示が空のためフォーマットできないため早期リターン
        if (timerTextView.getText().toString().equals("")) {
            return;
        }

        String[] tmp = (timerTextView.getText().toString()).split(":", 0);

        int minute = Integer.parseInt(tmp[0]) * 1000 * 60;
        int second = Integer.parseInt(tmp[1]) * 1000;
        countDown = new CountDown(minute + second, 1000);
        countDown.start();
    }


    private void fromPrefFragment() {

        System.out.println("設定フラグメントからの復帰。あってる？");

        // タイマーを再セット。Standby状態だし支障ない、そうに違いない
        workoutTime = Long.valueOf(pref.getString("workout_time", "5000"));
        breakTime = Long.valueOf(pref.getString("break_time", "10000"));
        prepareTime = Long.valueOf(pref.getString("prepare_time", "5000"));
        MAX_TIMES = Integer.parseInt(pref.getString("set", "4"));

        renewViews(workoutTime);
    }




}