
package com.example.masa.bizzarestrangeplayer.Activity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.masa.bizzarestrangeplayer.Fragment.MyConfigFragment;

public class MyConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new MyConfigFragment());
        fragmentTransaction.commit();
    }
}
