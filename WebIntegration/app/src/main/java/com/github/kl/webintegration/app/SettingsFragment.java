package com.github.kl.webintegration.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;

public class SettingsFragment extends PreferenceFragment {

    public static int MIN_PORT_PRIVELEDGED = 1;
    public static int MIN_PORT = 1024;
    public static int MAX_PORT = 65535;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        setupValidation();
    }

    private void setupValidation() {
        addValidator(R.string.pref_key_http_server_ip,    new DottedDecimalIPValidator());
        addValidator(R.string.pref_key_http_server_port,  new PortValidator(MIN_PORT_PRIVELEDGED, MAX_PORT));
        addValidator(R.string.pref_key_https_server_ip,   new DottedDecimalIPValidator());
        addValidator(R.string.pref_key_https_server_port, new PortValidator(MIN_PORT_PRIVELEDGED, MAX_PORT));
        addValidator(R.string.pref_key_local_server_port, new PortValidator(MIN_PORT, MAX_PORT));
    }

    private void addValidator(int preferenceKey, Preference.OnPreferenceChangeListener listener) {
        findPreference(getString(preferenceKey)).setOnPreferenceChangeListener(listener);
    }

    private class PortValidator implements OnPreferenceChangeListener {

        private int maxPort;
        private int minPort;

        public PortValidator(int minPort, int maxPort) {
            this.minPort = minPort;
            this.maxPort = maxPort;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String port = (String)newValue;
            if (port.matches("\\d{1,5}") && isInRange(port)) {
                return true;
            } else {
                showValidationError("Port must be between " + minPort + " and " + maxPort);
                return false;
            }
        }

        private boolean isInRange(String portString) {
            int port = Integer.parseInt(portString);
            return (port >= minPort && port <= maxPort);
        }
    }

    private class DottedDecimalIPValidator implements OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String ip = (String)newValue;

            if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                String[] numbers = ip.split("\\.");
                if (doesNotContainLeadingZeroes(numbers) &&
                    eachNumberIsBetween0And255Inclusive(numbers)) return true;
            }

            showValidationError(getString(R.string.pref_validation_wrong_ip));
            return false;
        }

        private boolean doesNotContainLeadingZeroes(String[] numbers) {
            for (String num : numbers) {
                if (num.matches("(0\\d\\d)|(00\\d)|(000)|(0\\d)")) return false;
            }
            return true;
        }

        private boolean eachNumberIsBetween0And255Inclusive(String[] numbers) {
            for (String stringNum : numbers) {
                int num = Integer.parseInt(stringNum);
                if (num < 0 || num > 255) return false;
            }
            return true;
        }
    }

    private void showValidationError(final String message) {
        final Activity activity = getActivity();
        if (activity == null) throw new RuntimeException("SettingsFragment parent activity is null");

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity)
                        .setMessage(message)
                        .setPositiveButton(R.string.pref_validation_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface di, int i) {
                            }
                        })
                        .create()
                        .show();
            }
        });
    }
}















