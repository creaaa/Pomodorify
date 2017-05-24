
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
import android.widget.SeekBar;
import android.widget.TabHost;
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


// TODO: isTimerActiveは一部しか書いてないよ！！いま！　だからそれに依存した実装を書くな

public class MainActivity extends AppCompatActivity
        implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {


//


//    public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
//
//        // running activity count
//        private int running = 0;
//
//        @Override
//        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//
//        }
//
//        @Override
//        public void onActivityStarted(Activity activity) {
//
//        }
//
//        @Override
//        public void onActivityResumed(Activity activity) {
//
//        }
//
//        @Override
//        public void onActivityPaused(Activity activity) {
//
//        }
//
//        @Override
//        public void onActivityStopped(Activity activity) {
//
//        }
//
//        @Override
//        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//
//        }
//
//        @Override
//        public void onActivityDestroyed(Activity activity) {
//
//        }
//    }









    private final String TAG = "深刻なバグ";

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


    /* UI Tab1 */

    ImageView jacketImageView;
    TextView loginStateTextView, setStateTextView, timerStateTextView;
    TextView nowMusicTextView;
    TextView timerTextView;
    Button cancelButton;
    ToggleButton timerStateToggleButton;


    /* UI Tab2 */

    SeekBar musicSeekBar;
    Button previousSongButton;
    Button nextSongButton;
    ToggleButton musicPauseButton;


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
    private static final int REQUEST_CODE = 3;
    private String mAccessToken;

    /* Launch Activity */
    private final int LAUNCH_SETLIST_RESULT = 1;
    private final int LAUNCH_PREF = 2;


    Handler _handler = new Handler();


    @Override
    protected void onRestart() {

        super.onRestart();

        System.out.println("リスタートｗ");

//        // TODO: 「タイムアウトで」サブ画面から帰ってきた時は、ここに何の処理も書かなくていいのか？
//        // とりま動いてるけど...。
//
//
//        // 設定画面から復帰したときは、↓のタイマーを走らせる処理をしたくないため、早期リターン
//        if (state == TimerState.Standby) {
//            return;
//        }
//
//        // 3/22 23:00 これかかないとだめ！！
//        if (state == TimerState.Break) {
//            renewTimerStateInfo(state);
//            return;
//        }
//
//
//        // FIXME: これないと落ちるが汚い。なんとかしろ。
//        if (timerTextView.getText().toString().equals("")) {
//            return;
//        }



        // TODO: タイマー再開処理


    }
    
    
    private void timerReset() {

        Log.d(TAG, "timerReset:");
        
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


        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // Toast.makeText(getApplicationContext(), "hoge", Toast.LENGTH_SHORT).show();

                if (mPlayer == null) {
                    return;
                }

                // ここでもnullチェックいる

                double t;
                double c;

                if (mPlayer.getMetadata() != null) {

                    t = mPlayer.getMetadata().currentTrack.durationMs;
                    c = mPlayer.getPlaybackState().positionMs;

                    double result = c / t * 100.0;

                    System.out.println(result);

                    musicSeekBar.setProgress((int)result);

                    _handler.postDelayed(this, 1000);  // 何秒ごとに実行するか
                }



            }
        }, 20000); // 最初に何秒遅延させるか


        // キャンセルしたいときはこーする
        // _handler.removeCallbacksAndMessages(null);


        /* 00. Tabhost Initialize */

        // Tabhostを有効化
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();


        // タブ:Tab1を作成＆追加
        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1");
        tab1.setIndicator(getResources().getString(R.string.tab1));  // indicator = ラベル(ツマミ)
        tab1.setContent(R.id.tab1);

        tabHost.addTab(tab1);


        // タブ:Tab2を作成＆追加
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2");
        tab2.setIndicator(getResources().getString(R.string.tab2));
        tab2.setContent(R.id.tab2);

        tabHost.addTab(tab2);

        tabHost.setCurrentTab(0);



        /* 0. prepare timerRemoteStopHandler */

        timerRemoteStopHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {

                    case 100:
                        System.out.println("タイマーストップ！！！");

                        if (countDown != null) {
                            countDown.cancel();
                            isTimerActive = false;
                        }

                        break;
                }
            }
        };


        /* 1. Auth Process */

         doAuth();


        /* 2. prepare Preference and initialize user's interval setting */

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        setAllTimerSetting();


        /* 3. UI componet initialize */

        initializeViewComponents();

        // addFilterToImageView();

        renewViews(workoutTime);



        /* 4. set event listener */

        timerStateToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                // OFF→ONになった瞬間
                if (isChecked) {

                    // 1セットめを開始
                    if (state == TimerState.Standby) {

                        // まずは、prepareからstateをスタート
                        state = TimerState.Prepare;
                        countDown = new CountDown(prepareTime, 1000);
                        countDown.start();


                        currentSet = 1;


                        renewViews(prepareTime);

                        timerStateToggleButton.setVisibility(View.INVISIBLE);

                        // 3/22 18:00 追加
                        invalidateOptionsMenu();

                        return;
                    }


                    // 一時停止中(state: Pause)のとき、タイマーを再開
                    timerReset();

                    // renewTimerStateInfo(TimerState.Workout);

                    renewViews(workoutTime);



                } else { // ON→OFFになった瞬間 (再生中のとき、一時停止する)

                    if (currentSet >= MAX_TIMES) {
                        return;
                    }

                    // ここ、実は必要。
                    // 1. なぜならキャンセルボタンを押したとき 2. state = Workout時にonStopしたとき
                    // トグルボタン=OFFになり、ここが走る。
                    // だから早期リターンさせてる。必要。
                    // まぁこの場合、2は書かなくていいかもしれないかもだけど...
                    if (state == TimerState.Standby || state == TimerState.Pause) { return; }

                    state = TimerState.Pause;

//                    if(countDown != null) {
                        Log.d(TAG, "はいとおったー2");
                        countDown.cancel();
//                    }

                    renewTimerStateInfo(TimerState.Pause);
                }
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {


                DialogFragment dialog = new ResetDialogFragment();
                dialog.show(getFragmentManager(), "dialog_basic");

                Log.d(TAG, "はいとおったー3");
                    countDown.cancel();
//              }

                state = TimerState.Standby;
                currentSet = 1;

                timerStateToggleButton.setVisibility(View.VISIBLE);
                timerStateToggleButton.setChecked(false);  // まさかここで、onCheckedChange が呼ばれてる？→合ってた

                invalidateOptionsMenu();
                renewViews(workoutTime);

            }
        });



        /* Music Player Tab Event Listener */


        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                System.out.println("バー: " + progress);


                if (fromUser == false) {
                    return;
                }


                if (mPlayer == null) {
                    return;
                }

                int time = currentSetPlaylist.get(playlistHead).getDurationMs();
                time = time * progress / 100;
                mPlayer.seekToPosition(null, time);


                long ms = mPlayer.getPlaybackState().positionMs;
                System.out.println(ms);
                System.out.println(mPlayer.getMetadata().currentTrack.name);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        previousSongButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                System.out.println("現在のHEAD: " + playlistHead);

                if (playlistHead == 0) {
                    return;
                }

                playlistHead -= 1;

                // mPlayer.playUri(null, "spotify:track:" + currentSetPlaylist.get(playlistHead).getId(), 0, 0);
                play();

                renewMusicInfo();
            }
        });


        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("現在のHEAD: " + playlistHead);

                if (playlistHead >= currentSetPlaylist.size() - 1) {
                    return;
                }

                // 俺はこれを実質使ってない(てゆうかわからない)
                // mPlayer.skipToNext(null);

                playlistHead += 1;

                play();

                renewMusicInfo();
            }
        });



        musicPauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    pause();
                } else {
                    resume();
                }

            }
        });
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


        else if (requestCode == LAUNCH_SETLIST_RESULT) {

            System.out.println("セトリ画面からの復帰");

            // ターム終了直後は時間表示が空のためフォーマットできないため早期リターン
            if (timerTextView.getText().toString().equals("")) {
                return;
            }

            // これ、いれるか、いれないか、わかれる
            // いれると自然。だが、ノータッチで勝手にfinishしたときにバグっていく。
            // 制御できないと思ったら、ここをコメントアウトしろ
            // TODO: timerReset

            if (isTimerActive == false) {
                timerReset();
            } else {
                System.out.println("華麗に回避");
            }
        }


        // 設定フラグメント画面から復帰時の処理
        else if (requestCode == LAUNCH_PREF) {

            System.out.println("設定画面からの復帰");
            setAllTimerSetting();
            renewViews(workoutTime);
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

        if (state == TimerState.Standby || state == TimerState.Break) {

        } else {
            Log.d(TAG, "はいとおったー4");
            System.out.println("遷移直前の状態: " + state);
            countDown.cancel();
        }


        // タイマーの種類に応じて退避行動の挙動を変化
//        switch (state) {
//
//            case Standby:
//                break;
//
//            case Workout:
//                break;
//
//            case Pause:
//                break;
//
//            case Break:
//                break;
//
//            case Prepare:
//                break;
//        }

        super.onStop();
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

                // 戻るボタンを押すとfinish()が内部的に呼ばれているため、
                // onActivityResultで処理を加えられる
                startActivityForResult(i, LAUNCH_PREF);

                return true;

            case R.id.login:

                doAuth();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /* Inner Class */


    Boolean isTimerActive = false;


    private class CountDown extends CountDownTimer {


        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }


        @Override
        public void onFinish() {

            switch (state) {

                case Workout:

                    if (currentSet >= MAX_TIMES) {

                        invalidateOptionsMenu();

                        state = TimerState.Standby;

                        countDown.cancel();

                        timerTextView.setText("");
                        // ここで、onSetChecked が走ってしまうので、onSetCheckedに回避処理を書いている
                        timerStateToggleButton.setChecked(false);


                        timerStateToggleButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.INVISIBLE);

                        renewTimerStateInfo(state);

                        launchSetListActivity();

                        return;
                    }


                    Log.d(TAG, "ふつーの workout onFinish");

                    state = TimerState.Break;
                    countDown = new CountDown(breakTime, 1000);
                    countDown.start();

                    timerStateToggleButton.setVisibility(View.INVISIBLE);
                    renewViews(breakTime);

                    launchSetListActivity();

                    break;


                case Break:

                    state = TimerState.Prepare;

                    countDown = new CountDown(prepareTime, 1000);

                    Log.d(TAG, "セットされたprepareTime: " + prepareTime);

                    countDown.start();

                    increaseCurrentSet();

                    renewViews(prepareTime);

                    // mPlayer.pause(null);


                    // タイマーサイクル中、音楽を止めているところはここ！
                    pause();


                    break;

                case Prepare:

                    state = TimerState.Workout;
                    countDown = new CountDown(workoutTime, 1000);
                    countDown.start();

                    timerStateToggleButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);

                    renewViews(workoutTime);

                    // たぶんここ！！セトリを生成し、再生を開始する絶好のタイミングは。
                    createThisSetPlaylist();

                    break;
            };
        }


        // Timerのカウント周期で呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {

            isTimerActive = true;
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


    private void setAllTimerSetting() {

        workoutTime = Long.valueOf(pref.getString("workout_time", "9000"));
        breakTime   = Long.valueOf(pref.getString("break_time", "9000"));
        prepareTime = Long.valueOf(pref.getString("prepare_time", "9000"));
        MAX_TIMES   = Integer.parseInt(pref.getString("set", "4"));

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

        startActivityForResult(i, LAUNCH_SETLIST_RESULT);
    }



    private void createThisSetPlaylist() {

        // TODO: 自分のアカウント（日本アカウント）じゃきけない曲まで選出されてるっぽい。
        // だから、そのとき、なにもきこえてこないんじゃないの。

        try {
            //  "j-idol", "j-pop", "j-rock", industrial, chill, techno
            URL url = new URL("https://api.spotify.com/v1/recommendations?" +
                    //"seed_genres=techno&" +  // なんかこのジャンル指定がやばいっぽいな
                    "seed_artists=115IWAVy4OTxhE0xdDef1c&" +  // パスピエ
                    //"seed_tracks=3p4ELetqoTwFpsnUkEirzc&" +   // スーパーカー
                    //"min_instrumentalness=0.8&" +
                    "market=JP&" +
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
                        return;
                    }

                    // 前の設定を空に
                    currentSetPlaylist = new ArrayList<>();
                    playlistHead       = 0;


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

        play();

    }


    private void play() {
        if (mPlayer != null) {
            // ここの第３引数はとりま0じゃないとだめっぽい、てかこのindexってなんなんだ...
            mPlayer.playUri(null, "spotify:track:" + currentSetPlaylist.get(playlistHead).getId(), 0, 0);
        }
    }


    private void pause() {

        if (mPlayer == null) {
            return;
        }
        mPlayer.pause(null);
    }


    private void resume() {

        if (mPlayer == null) {
            return;
        }
        mPlayer.resume(null);
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



    /* renew UI */


    private void initializeViewComponents() {


        jacketImageView = (ImageView) findViewById(R.id.jacketImageView);

        loginStateTextView = (TextView) findViewById(R.id.loginStateTextView);
        loginStateTextView.setTypeface(Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf"));

        setStateTextView   = (TextView) findViewById(R.id.setStateTextView);

        timerStateTextView = (TextView) findViewById(R.id.timerStateTextView);
        timerStateTextView.setTypeface(Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf"));

        nowMusicTextView   = (TextView) findViewById(R.id.nowMusicTextView);
        nowMusicTextView.setTypeface(Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf"));

        timerTextView      = (TextView) findViewById(R.id.timerTextView);
        timerTextView.setTypeface(Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf"));


        cancelButton       = (Button) findViewById(R.id.cancelButton);
        timerStateToggleButton = (ToggleButton) findViewById(R.id.timerStateToggleButton);


        musicSeekBar       = (SeekBar) findViewById(R.id.musicSeekBar);
        previousSongButton = (Button) findViewById(R.id.previousSongButton);
        nextSongButton     = (Button) findViewById(R.id.nextSongButton);
        musicPauseButton   = (ToggleButton) findViewById(R.id.musicPauseButton);

    }


    private void addFilterToImageView() {

        float brightness = -125;

        ColorMatrix cmx = new ColorMatrix(new float[] {

                1, 0, 0, 0, brightness
                , 0, 1, 0, 0, brightness
                , 0, 0, 1, 0, brightness
                , 0, 0, 0, 1, 0 });

        jacketImageView.setColorFilter(new ColorMatrixColorFilter(cmx));
        jacketImageView.setAlpha(0.8f);

    }


    private void renewViews(Long kind_of_timer) {
        renewSetInfo();
        renewTimerStateInfo(state);
        renewTimerInfo(kind_of_timer);
    }

    private void renewSetInfo() {
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

}