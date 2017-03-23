
package com.example.masa.bizzarestrangeplayer.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.masa.bizzarestrangeplayer.Async.CreatePlaylistAsyncTask;
import com.example.masa.bizzarestrangeplayer.Model.Track;
import com.example.masa.bizzarestrangeplayer.Async.PutSongsToPlaylistAsyncTask;
import com.example.masa.bizzarestrangeplayer.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.os.Build.VERSION_CODES.M;


public class SetListResultActivity extends AppCompatActivity {


    ListView listView;
    MyAdapter adapter;


    private String mAccessToken = null;
    private String userID       = null;
    public  String playlistID   = null;


    public Boolean[] isCheckedArray = new Boolean[]{};

    TextView remainingBreakTimeTextView;


    private CountDown countDown;
    Long breakTime;
    SharedPreferences pref;


    // 前画面から送られてくるプレイリスト
    public ArrayList<Track> currentSetPlaylist = new ArrayList<>();

    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list_result);


        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);


        listView = (ListView) findViewById(R.id.setListListView);
        adapter = new MyAdapter(this, R.layout.song_cell);

        remainingBreakTimeTextView = (TextView) findViewById(R.id.remainingBreakTimeTextView);

        currentSetPlaylist = (ArrayList<Track>) getIntent().getSerializableExtra("playlist");
        mAccessToken = getIntent().getStringExtra("token");


        for (Track eachTrack: currentSetPlaylist) {
            System.out.println("うけとれ！" + eachTrack.getName());
        }


        /* Timer Setting */

        Intent i = getIntent();
        int currentSet = i.getIntExtra("current_set", 99);
        int max_set    = i.getIntExtra("max_set", 99);


        pref = PreferenceManager.getDefaultSharedPreferences(this);


        // もし最終セットでなければ、タイマーをセット
        if (currentSet != max_set) {

            breakTime = Long.valueOf(pref.getString("break_time", "9000"));

            System.out.println("残り休憩時間: " + breakTime);
            countDown = new CountDown(breakTime, 1000);
            countDown.start();

        }


        isCheckedArray = new Boolean[currentSetPlaylist.size()];

        System.out.println("サイズ: " + currentSetPlaylist.size());

        Arrays.fill(isCheckedArray, false);


        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = M)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (isCheckedArray[position] == false) {
                    isCheckedArray[position] = true;
                    view.setBackgroundColor(getColor(R.color.colorPrimary));
                } else {
                    isCheckedArray[position] = false;
                    view.setBackgroundColor(getColor(R.color.text_secondary_light));
                }


                // タイマーを停止。と同時に、前画面のタイマーも停止
                if (countDown != null) {
                    countDown.cancel();
                }

                MainActivity.timerRemoteStopHandler.sendEmptyMessage(100);

            }
        });



        ((Button)findViewById(R.id.addPlaylistButton)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //STEP 0. ここに書くのはOK??
                getMyselfInfo(new CreatePlaylistAsyncTask(SetListResultActivity.this));

                finish();
            }
        });


        ((Button)findViewById(R.id.selectAllButton)).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = M)
                    @Override
                    public void onClick(View v) {

                        if (countDown != null) {
                            countDown.cancel();
                        }


                        MainActivity.timerRemoteStopHandler.sendEmptyMessage(100);

                        Arrays.fill(isCheckedArray, true);
                        for (int i = 0; i < listView.getChildCount(); i++) {
                            listView.getChildAt(i).setBackgroundColor(getColor(R.color.colorPrimary));
                        }
                    }
                });
    }


    @Override
    protected void onStop() {
        countDown = null;
        System.out.println("タイマーは あとかたもなく ぶっこわれた");
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /* プレイリスト作成 + 曲の追加は以下3ステップで実施される */

    // Step 0: 自身のアカウントのIDをゲット
    // Step 1: /v1/users/{user_id}/playlists  プレイリスト(箱そのもの)を生成(Step0のコールバックとして渡している)
    // Step 2: /v1/users/{user_id}/playlists/{playlist_id}/tracks その中に曲を次々ぶち込んでいく
    // (step1の onPostの中で実施している)


    // Step 0
    private void getMyselfInfo(final CreatePlaylistAsyncTask cb) {

        try {

            // 前画面からアクセストークンが渡ってきていないのであれば早期リターン
            if (mAccessToken == null) {
                return;
            }

            URL url = new URL("https://api.spotify.com/v1/me");

            final Request request = new Request.Builder()
                    // URLを生成
                    .url(url.toString())
                    .get()
                    .addHeader("Authorization", "Bearer " + mAccessToken)
                    .build();

            final OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OKHttp", "エラー♪");
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    // ここ、最後は toStringじゃないぞ、まじで気をつけろ
                    String responseBody = response.body().string();

                    try {

                        JSONObject obj = new JSONObject(responseBody);
                        System.out.println(obj);

                        String uID = obj.getString("id");

                        System.out.println("到達♪ " + uID);

                        userID = uID;

                        // Step 1 : Create Playlist Container
                        cb.execute(mAccessToken, userID);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }




    // Step 1
//    private void createPlaylistContainer() {
//        // この中のonPost〜で、playlistIDが初期化されます
//        new CreatePlaylistAsyncTask(this).execute(mAccessToken, userID);
//    }



    public ArrayList<String> intendedAddedSongIDs;

    // Step 2
    public void putSongsToPlaylist(String mAccessToken, String userID, String playlistID) {
        new PutSongsToPlaylistAsyncTask(this).execute(mAccessToken, userID, playlistID);
    }




    private class MyAdapter extends BaseAdapter {


        private LayoutInflater layoutInflater;
        SetListResultActivity activity;


        private int resource = 0;


        public MyAdapter(Context context, int resource) {

            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            activity = (SetListResultActivity) context;

            this.resource = resource;
        }


        @Override
        public int getCount() {
            return currentSetPlaylist.size();
        }

        @Override
        public Object getItem(int position) {
            return currentSetPlaylist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0; // itemList.get(position).getId();
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                view = layoutInflater.inflate(R.layout.song_cell, null);
            }

            if (isCheckedArray[position] == true) {
                view.setBackgroundColor(getColor(R.color.colorPrimary));
            } else {
                view.setBackgroundColor(getColor(R.color.text_secondary_light));
            }

            // render each view conponent
            Picasso.with(getApplicationContext())
                    .load(currentSetPlaylist.get(position).getAlbum().getImages().get(0).getUrl())
                    .into((ImageView) view.findViewById(R.id.jacketImageView));

            ((TextView)view.findViewById(R.id.songNameTextView))
                    .setText(currentSetPlaylist.get(position).getName());

            ((TextView)view.findViewById(R.id.artistNameTextView))
                    .setText(currentSetPlaylist.get(position).getArtists().get(0).getName());


            (view.findViewById(R.id.previewButton)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (countDown != null) {
                        countDown.cancel();
                    }

                    MainActivity.timerRemoteStopHandler.sendEmptyMessage(100);
                    MainActivity.mPlayer.pause(null);


                    System.out.println("プレビューURI: " + currentSetPlaylist.get(position).getPreviewUrl());


                    try {
                        String url = currentSetPlaylist.get(position).getPreviewUrl();
                        MediaPlayer mp = new MediaPlayer();
                        mp.setDataSource(url);
                        mp.prepare();
                        mp.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


//                    final Request request = new Request.Builder()
//                    // URLを生成
//                    .url(url.toString())
//                    //.addHeader("Authorization","Bearer " + mAccessToken)
//                    .build();
//
//
//                    // クライアントオブジェクトを作成する
//                    final OkHttpClient client = new OkHttpClient();
//                    // 新しいリクエストを行う
//                    final URL finalUrl = url;
//                    client.newCall(request).enqueue(new Callback() {
//                // 通信が成功した時
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//
//                    // 通信結果をログに出力する
//                    final String responseBody = response.body().string();
//                    //
//                    Log.d("OKHttp", responseBody);
//
//
////                    MediaPlayer mp = new MediaPlayer();
////                    mp.setDataSource(String.valueOf(finalUrl));
////                    mp.prepare();
////                    mp.start();
//
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
        }});

            return view;
        }
    }


    private class CountDown extends CountDownTimer {


        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }


        @Override
        public void onFinish() {
            finish();
        }


        // Timerのカウント周期で呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            // 残り時間を分、秒、ミリ秒に分割
            long mm = millisUntilFinished / 1000 / 60;
            long ss = millisUntilFinished / 1000 % 60;
            // long ms = millisUntilFinished - ss * 1000 - mm * 1000 * 60;

            //timerText.setText(String.format("%1$02d:%2$02d.%3$03d", mm, ss, ms));
            remainingBreakTimeTextView.setText(String.format("Remaining: %1$02d:%2$02d", mm, ss));

            breakTime = millisUntilFinished;
        }
    }


//    private class Song {
//
//        private String imageURL;
//        private String name;
//        private String artist;
//
//        public Song(String imageURL, String name, String artist) {
//            this.imageURL = imageURL;
//            this.name = name;
//            this.artist = artist;
//        }
//
//        public String getImageURL() {
//            return imageURL;
//        }
//        public void setImageURL(String imageURL) {
//            this.imageURL = imageURL;
//        }
//
//        public String getName() {
//            return name;
//        }
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getArtist() {
//            return artist;
//        }
//        public void setArtist(String artist) {
//            this.artist = artist;
//        }
//    }
}