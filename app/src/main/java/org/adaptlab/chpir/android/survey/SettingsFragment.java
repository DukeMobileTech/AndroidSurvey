package org.adaptlab.chpir.android.survey;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.adaptlab.chpir.android.survey.tasks.ApkUpdateTask;

public class SettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, parent, false);
        Button updatesCheck = (Button) v.findViewById(R.id.updates_check_button);
        updatesCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new ApkUpdateTask(getActivity()).execute();
            }
        });

        return v;
    }

}