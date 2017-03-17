
package com.example.masa.bizzarestrangeplayer.Fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.masa.bizzarestrangeplayer.R;


public class MyConfigFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
