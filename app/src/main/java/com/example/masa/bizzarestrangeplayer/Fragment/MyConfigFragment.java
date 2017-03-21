
package com.example.masa.bizzarestrangeplayer.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.example.masa.bizzarestrangeplayer.R;


public class MyConfigFragment extends PreferenceFragment {

    ListPreference workoutTimePref;
    ListPreference breakTimePref;
    ListPreference prepareTimePref;
    ListPreference setPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        workoutTimePref = (ListPreference) findPreference("workout_time");
        breakTimePref = (ListPreference) findPreference("break_time");
        prepareTimePref = (ListPreference) findPreference("prepare_time");
        setPref = (ListPreference) findPreference("set");
    }


    @Override
    public void onResume() {

        super.onResume();

        System.out.println("くるのね");

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        workoutTimePref.setSummary(workoutTimePref.getEntry());
        breakTimePref.setSummary(breakTimePref.getEntry());
        prepareTimePref.setSummary(prepareTimePref.getEntry());
        setPref.setSummary(setPref.getEntry());
    }


    private SharedPreferences.OnSharedPreferenceChangeListener listener =

            new SharedPreferences.OnSharedPreferenceChangeListener() {

                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    switch (key) {

                        case "workout_time":
                            workoutTimePref.setSummary(workoutTimePref.getEntry());
                            break;

                        case "break_time":
                            breakTimePref.setSummary(breakTimePref.getEntry());
                            break;

                        case "prepare_time":
                            prepareTimePref.setSummary(prepareTimePref.getEntry());
                            break;

                        case "set":
                            setPref.setSummary(setPref.getEntry());
                            break;
                    }
                }
            };

}