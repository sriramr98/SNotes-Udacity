package in.snotes.snotes.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import in.snotes.snotes.R;
import in.snotes.snotes.utils.SharedPrefsUtils;
import timber.log.Timber;

public class PreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(getString(R.string.isPinSet))) {

            boolean pref = (boolean) newValue;

            Timber.i(String.valueOf(pref));

            if (pref) {
                new MaterialDialog.Builder(getActivity())
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input(null, null, (dialog, input) -> {
                            // preference is changed from false to true
                            Timber.i("Changing false to true");
                            SharedPrefsUtils.setPin(input.toString());

                        }).show();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference);

        Preference isPinSetPref = findPreference(getString(R.string.isPinSet));
        isPinSetPref.setOnPreferenceChangeListener(this);
    }
}
