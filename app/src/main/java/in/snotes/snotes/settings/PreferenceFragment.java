package in.snotes.snotes.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import in.snotes.snotes.R;

public class PreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference);
    }
}
