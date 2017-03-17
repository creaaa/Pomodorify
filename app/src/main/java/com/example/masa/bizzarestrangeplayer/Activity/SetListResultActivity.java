
package com.example.masa.bizzarestrangeplayer.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.ListView;
import android.widget.TextView;

import com.example.masa.bizzarestrangeplayer.R;

import java.util.ArrayList;
import java.util.Arrays;


public class SetListResultActivity extends AppCompatActivity {

    ListView listView;
    MyAdapter adapter;

    public ArrayList<Song> songs;
    public Boolean[] isCheckedArray = new Boolean[]{};

    TextView remainingBreakTimeTextView;


    private CountDown countDown;
    Long breakTime;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list_result);

        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.setListListView);
        adapter = new MyAdapter(this, R.layout.song_cell);

        remainingBreakTimeTextView = (TextView) findViewById(R.id.remainingBreakTimeTextView);


        /* Timer Setting */
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        breakTime = Long.valueOf(pref.getString("break_time", "9000"));

        if (breakTime != null) {
            System.out.println("残り休憩時間: " + breakTime);
            countDown = new CountDown(breakTime, 1000);
            countDown.start();
        }


        songs = new ArrayList<>();

        songs.add(new Song("ff", "the one", "BABYMETAL"));
        songs.add(new Song("ff", "グッとくるSUMMER", "大森靖子"));
        songs.add(new Song("ff", "Sugar!!", "フジファブリック"));
        songs.add(new Song("ff", "ぜんぜん", "寺嶋由芙"));
        songs.add(new Song("ff", "メーデー", "パスピエ"));
        //
        songs.add(new Song("ff", "the one", "BABYMETAL"));
        songs.add(new Song("ff", "グッとくるSUMMER", "大森靖子"));
        songs.add(new Song("ff", "Sugar!!", "フジファブリック"));
        songs.add(new Song("ff", "ぜんぜん", "寺嶋由芙"));
        songs.add(new Song("ff", "メーデー", "パスピエ"));
        songs.add(new Song("ff", "the one", "BABYMETAL"));
        songs.add(new Song("ff", "グッとくるSUMMER", "大森靖子"));
        songs.add(new Song("ff", "Sugar!!", "フジファブリック"));
        songs.add(new Song("ff", "ぜんぜん", "寺嶋由芙"));
        songs.add(new Song("ff", "メーデー", "パスピエ"));

        isCheckedArray = new Boolean[songs.size()];
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

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < isCheckedArray.length; i++) {
                    if (isCheckedArray[i] == true) {
                        sb.append(songs.get(i).getName() + ",");
                    }
                }

                // 通知
                if (sb.toString() != String.valueOf("")) {
                    Log.d("SETLIST", sb.substring(0, sb.length() - 1));
                }
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




    private class MyAdapter extends BaseAdapter {

        private LayoutInflater layoutInflater;
        SetListResultActivity activity;

        private int resource = 0;

        //        public ArrayList<Song> songs;


        public MyAdapter(Context context, int resource) {

            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            activity = (SetListResultActivity) context;

            this.resource = resource;
        }


        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int position) {
            return songs.get(position);
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

            ((TextView)view.findViewById(R.id.songNameTextView))
                    .setText(songs.get(position).getName());

            ((TextView)view.findViewById(R.id.artistNameTextView))
                    .setText(songs.get(position).getArtist());

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
