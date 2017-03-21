
package com.example.masa.bizzarestrangeplayer.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
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

import com.example.masa.bizzarestrangeplayer.CreatePlaylistAsyncTask;
import com.example.masa.bizzarestrangeplayer.Model.Track;
import com.example.masa.bizzarestrangeplayer.PutSongsToPlaylistAsyncTask;
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


public class SetListResultActivity extends AppCompatActivity {

    ListView listView;
    MyAdapter adapter;


    // FIXME: ちゃんとしろ
    private String mAccessToken = null;
    private String userID       = null;
    public static String playlistID   = null;



    //public ArrayList<Song> songs;
    public Boolean[] isCheckedArray = new Boolean[]{};

    TextView remainingBreakTimeTextView;


    private CountDown countDown;
    Long breakTime;
    SharedPreferences pref;

    // ログインユーザーのID
    //String userID;


    // 前画面から送られてくるプレイリスト
    ArrayList<Track> currentSetPlaylist = new ArrayList<>();

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

        // getMyselfInfo();
        // TODO: ここだぜ
        //createPlaylistContainer();


        /* Timer Setting */
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        breakTime = Long.valueOf(pref.getString("break_time", "9000"));

        if (breakTime != null) {
            System.out.println("残り休憩時間: " + breakTime);
            countDown = new CountDown(breakTime, 1000);
            countDown.start();
        }


        isCheckedArray = new Boolean[currentSetPlaylist.size()];

        // TODO: ここが0になってる。ふつう6とかになる
        System.out.println("サイズ: " + currentSetPlaylist.size());

        Arrays.fill(isCheckedArray, false);

        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (isCheckedArray[position] == false) {
                    isCheckedArray[position] = true;
                    view.setBackgroundColor(getColor(R.color.colorPrimary));
                } else {
                    isCheckedArray[position] = false;
                    view.setBackgroundColor(getColor(R.color.text_secondary_light));
                }
            }
        });



        ((Button)findViewById(R.id.addPlaylistButton))
                .setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //STEP 0. ここに書くのはOK??
                getMyselfInfo();

                //STEP 1. ここに書くのはOK??
                SystemClock.sleep(3000);
                createPlaylistContainer();

                finish();
            }
        });


        ((Button)findViewById(R.id.selectAllButton))
                .setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
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


    // Step 0: 自身のアカウントのIDをゲット
    // Step 1: /v1/users/{user_id}/playlists  プレイリスト(箱そのもの)を生成
    // Step 2: /v1/users/{user_id}/playlists/{playlist_id}/tracks その中に曲を次々ぶち込んでいく


    // Step 0
    private void getMyselfInfo() {

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
    private void createPlaylistContainer() {
        // この中野onPost〜で、playlistIDが初期化されます
        new CreatePlaylistAsyncTask(this).execute(mAccessToken, userID);
    }


    // Step 2
    public void putSongsToPlaylist(String mAccessToken, String userID, String playlistID) {
        new PutSongsToPlaylistAsyncTask().execute(mAccessToken, userID, playlistID);
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

            Picasso.with(getApplicationContext())
                    .load(currentSetPlaylist.get(position).getAlbum().getImages().get(0).getUrl())
                    .into((ImageView) view.findViewById(R.id.jacketImageView));

            ((TextView)view.findViewById(R.id.songNameTextView))
                    .setText(currentSetPlaylist.get(position).getName());

            ((TextView)view.findViewById(R.id.artistNameTextView))
                    .setText(currentSetPlaylist.get(position).getArtists().get(0).getName());

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


    private class Song {

        private String imageURL;
        private String name;
        private String artist;

        public Song(String imageURL, String name, String artist) {
            this.imageURL = imageURL;
            this.name = name;
            this.artist = artist;
        }

        public String getImageURL() {
            return imageURL;
        }
        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public String getArtist() {
            return artist;
        }
        public void setArtist(String artist) {
            this.artist = artist;
        }
    }
}