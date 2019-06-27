package com.example.mstudorFinal;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            switch(key){
                case("AIR"):
                    boolean val = ((SwitchPreference)pref).isChecked();
                    sharedPreferences.edit().putBoolean(key, val).commit();
                    break;

                case("ITEM"):
                    ListPreference list = (ListPreference)pref;
                    String shape = list.getValue();
                    sharedPreferences.edit().putString(key, shape).commit();
                    break;

                case("PROXIMITY"):
                    EditTextPreference text = (EditTextPreference)pref;
                    int prox = Integer.parseInt(text.getText());
                    if(prox<1)
                        prox = 1;
                    else if(prox>5)
                        prox = 5;
                    sharedPreferences.edit().putString(key, Integer.toString(prox)).commit();
                    pref.setSummary(Integer.toString(prox));
                    break;
                default:
                    return;
            }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();
        onSharedPreferenceChanged(sharedPrefs, "PROXIMITY");
        onSharedPreferenceChanged(sharedPrefs, "ITEM");
        onSharedPreferenceChanged(sharedPrefs, "AIR");
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().
                registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(this);
    }



}
