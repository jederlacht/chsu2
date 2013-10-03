package com.jederlacht.chsu2;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class chsu_preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    SharedPreferences.Editor SettingsEditor;
    String stringCurrentGroup;
    String stringCurrentTerm;
    ArrayList<String> arrayStringCurrentGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Читаем настройки запомненные
        SharedPreferences SavedSettings = PreferenceManager.getDefaultSharedPreferences(this);
        stringCurrentGroup = SavedSettings.getString("sCurrentGroup", "1СПО-21");
        stringCurrentTerm = SavedSettings.getString("sCurrentTerm", "1");

        Bundle extra = getIntent().getExtras();
        arrayStringCurrentGroups = extra.getStringArrayList("CURRENT_GROUPS");

        ListPreference termPref = (ListPreference) findPreference("sCurrentTerm");
        termPref.setSummary(stringCurrentTerm + " семестр");
        ListPreference groupPref = (ListPreference) findPreference("sCurrentGroup");
        final CharSequence[] cs = arrayStringCurrentGroups.toArray(new CharSequence[arrayStringCurrentGroups.size()]);
        groupPref.setEntries(cs);
        groupPref.setEntryValues(cs);
        groupPref.setSummary(stringCurrentGroup);

    }


    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("sCurrentTerm") || key.equals("sCurrentGroup")) {
            Preference connectionPref = findPreference(key);
            // Обновляем вывыод текущего значения
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
        	Intent intent = new Intent(this, chsu_main.class);
        	startActivity(intent);
        	finish();
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
	*/

}
