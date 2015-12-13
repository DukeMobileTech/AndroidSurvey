package org.adaptlab.chpir.android.survey;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, parent, false);
        Button wipeDatabase = (Button) v.findViewById(R.id.wipe_database_button);
        wipeDatabase.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new WipeDataTask().execute();
            }
        });

        Button resetLastSyncTime = (Button) v.findViewById(R.id.reset_last_sync_time_button);
        resetLastSyncTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AppUtil.getAdminSettingsInstance().resetLastSyncTime();
                finishActivity();
            }
        });

        return v;
    }

    private void finishActivity() {
        Intent intent = new Intent(getActivity(), InstrumentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private class WipeDataTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.wiping_data_header),
                    getString(R.string.background_process_progress_message)
            );
        }
        @Override
        protected Void doInBackground(Void... params) {
            AppUtil.wipeOutData();
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            mProgressDialog.dismiss();
            finishActivity();
        }
    }

}