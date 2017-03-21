package com.example.masa.bizzarestrangeplayer.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.masa.bizzarestrangeplayer.R;
import com.google.gson.Gson;

public class OnSprintActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onsprint);
        //
        imageView = (ImageView) findViewById(R.id.iv_album_player);
        imageView.setImageResource(R.drawable.fever);
        //
        // mPlayer.playUri(null, "spotify:track:6ZSvhLZRJredt15aJiBQqv", 0, 0);
        //MainActivity.mPlayer.playUri(null, "spotify:track:7xAdO0zdl7lGf0ecg4IGhQ", 0, 0);

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        mPlayer.seekToPosition(null, 100000);
//        mPlayer.playUri(null, "spotify:track:7xAdO0zdl7lGf0ecg4IGhQ", 0, 0);

    }

    public void backTapped(View v) {
        System.out.println("戻る");
    }

    public void playPauseTapped(View v) {
        System.out.println("きてんだよ");

        Gson gson = new Gson();

//        JsonObject response = null;
//        MusicModel model = gson.fromJson(response.toString(),MusicModel.class);
    }

    public void forwardTapped(View v) {
        System.out.println("進む");
    }
}