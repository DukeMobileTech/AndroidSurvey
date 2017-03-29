package org.adaptlab.chpir.android.survey;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.tasks.ApkUpdateTask;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, parent, false);

        getActivity().setTitle(getString(R.string.settings));

        TextView versionCode = (TextView) v.findViewById(R.id.settings_version_code_label);
        versionCode.setText(String.format(Locale.getDefault(), "%s%s%d",
                getString(R.string.version_code), " ", AppUtil.getVersionCode(getActivity())));

        TextView versionName = (TextView) v.findViewById(R.id.settings_version_name_label);
        versionName.setText(String.format(Locale.getDefault(), "%s%s%s",
                getString(R.string.version_name), " ", AppUtil.getVersionName(getActivity())));

        Button updatesCheck = (Button) v.findViewById(R.id.updates_check_button);
        updatesCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new ApkUpdateTask(getActivity()).execute();
            }
        });

        Calendar calendar = Calendar.getInstance();
        if (AdminSettings.getInstance().getLastSyncTime().equals("")) {
            calendar.setTimeInMillis(0);
        } else {
            calendar.setTimeInMillis(Long.parseLong(AdminSettings.getInstance().getLastSyncTime()));
        }
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        TextView lastSyncTime = (TextView) v.findViewById(R.id.settings_last_sync_time_label);
        lastSyncTime.setText(String.format(Locale.getDefault(), "%s%s%s",
                getString(R.string.last_sync_time), " ", dateFormat.format(calendar.getTime())));

        Button resetLastSyncTime = (Button) v.findViewById(R.id.reset_last_sync_time_button);
        resetLastSyncTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettings.getInstance().setLastSyncTime(null);
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            }
        });

        return v;
    }

}