package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.HttpUtil;
import org.adaptlab.chpir.android.activerecordcloudsync.NotificationUtils;
import org.adaptlab.chpir.android.survey.adapters.FragmentPagerAdapter;
import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.models.Image;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Project;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;
import org.adaptlab.chpir.android.survey.tasks.SetScoreUnitOrderingQuestionTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;

import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.AppUtil.getProjectId;

public class InstrumentActivity extends AppCompatActivity {
    public final static String EXTRA_AUTHORIZE_SURVEY =
            "org.adaptlab.chpir.android.survey.authorize_survey_bool";
    private final static String TAG = "InstrumentActivity";
    private ProgressDialog mProgressDialog;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private boolean mAuthorizeSurvey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.appInit(getApplication());
        setContentView(R.layout.activity_instrument2);
        if (getIntent() != null) {
            mAuthorizeSurvey = getIntent().getBooleanExtra(EXTRA_AUTHORIZE_SURVEY, false);
        }
        setSettings();
        requestNeededPermissions();
        setupViewPager();
    }

    private void setSettings() {
        SettingsViewModel settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(this, new Observer<Settings>() {
            @Override
            public void onChanged(@Nullable Settings settings) {
                if (settings == null) {
                    startActivity(new Intent(InstrumentActivity.this, InstrumentActivity.class));
                    finish();
                } else {
                    if (TextUtils.isEmpty(settings.getApiUrl()) || TextUtils.isEmpty(settings.getApiVersion()) ||
                            TextUtils.isEmpty(settings.getProjectId()) || TextUtils.isEmpty(settings.getApiKey())) {
                        startActivity(new Intent(InstrumentActivity.this, SettingsActivity.class));
                    }
                }
            }
        });
    }

    private void requestNeededPermissions() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (!hasPermission(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    private void setupViewPager() {
        mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(mFragmentPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.slidingTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private boolean hasPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViewPager();
        displayProjectName();
    }

    public void displayProjectName() {
        Project project = Project.findByRemoteId(getProjectId());
        TextView textView = findViewById(R.id.projectName);
        if (project != null && textView != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(project.getName());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_instrument, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getResources().getBoolean(R.bool.default_hide_admin_button)) {
            menu.findItem(R.id.menu_item_settings).setEnabled(false).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(i);
                }
                return true;
            case R.id.menu_item_refresh:
//                downloadInstruments();
                AppUtil.downloadData(this.getApplication());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isAuthorizeSurvey() {
        return mAuthorizeSurvey;
    }

    private void downloadInstruments() {
        showProgressDialog();
        RefreshInstrumentsTask asyncTask = new RefreshInstrumentsTask();
        asyncTask.setListener(new RefreshInstrumentsTask.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished() {
                displayProjectName();
                new SetScoreUnitOrderingQuestionTask().execute();
                RefreshImagesTask refreshImagesTask = new RefreshImagesTask();
                refreshImagesTask.setListener(new RefreshImagesTask.AsyncTaskListener() {
                    @Override
                    public void onAsyncTaskFinished() {
                        List<Instrument> instruments = Instrument.getAllProjectInstruments(
                                getProjectId());
                        for (int k = 0; k < instruments.size(); k++) {
                            InstrumentSanitizerTask sanitizerTask = new InstrumentSanitizerTask();
                            sanitizerTask.setListener(new InstrumentSanitizerTask.AsyncTaskListener() {
                                @Override
                                public void onAsyncTaskFinished(Boolean last) {
                                    if (last) {
                                        AppUtil.getSettings().setLastSyncTime(
                                                ActiveRecordCloudSync.getLastSyncTime());
                                        finishProgressDialog();
                                    }
                                }
                            });
                            sanitizerTask.execute(instruments.get(k), (k == instruments.size() - 1));
                        }
                        if (instruments.size() == 0) {
                            finishProgressDialog();
                        }
                    }
                });
                refreshImagesTask.execute();
            }
        });
        asyncTask.execute();
    }

    private void finishProgressDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!isDestroyed()) dismissProgressDialog();
        } else {
            dismissProgressDialog();
        }
    }

//    private void refreshInstrumentsView() {
//        if (mFragmentPagerAdapter != null &&
//                mFragmentPagerAdapter.getInstrumentViewPagerFragment() != null) {
//            mFragmentPagerAdapter.getInstrumentViewPagerFragment().refreshRecyclerView();
//        } else {
//            startActivity(new Intent(this, InstrumentActivity.class));
//            finish();
//        }
//    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(
                R.string.instrument_loading_progress_header));
        mProgressDialog.setMessage(getResources().getString(
                R.string.background_process_progress_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && !this.isFinishing() && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
//        refreshInstrumentsView();
    }

    private static class RefreshInstrumentsTask extends AsyncTask<Void, Void, Integer> {
        private AsyncTaskListener mListener;

        public interface AsyncTaskListener {
            void onAsyncTaskFinished();
        }

        void setListener(AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (NotificationUtils.checkForNetworkErrors(SurveyApp.getInstance())) {
                List<Instrument> instruments = Instrument.getAllProjectInstruments(getProjectId());
                for (int k = 0; k < instruments.size(); k++) {
                    if (!instruments.get(k).loaded()) {
                        AdminSettings.getInstance().resetLastSyncTime();
                        break;
                    }
                }
                ActiveRecordCloudSync.syncReceiveTables(SurveyApp.getInstance());
                return 0;
            } else {
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            mListener.onAsyncTaskFinished();
        }
    }

    private static class RefreshImagesTask extends AsyncTask<Void, Void, Void> {
        private final static String TAG = "ImageDownloader";

        private AsyncTaskListener mListener;

        public interface AsyncTaskListener {
            void onAsyncTaskFinished();
        }

        void setListener(AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if (NotificationUtils.checkForNetworkErrors(SurveyApp.getInstance())) {
                ActiveRecordCloudSync.setAccessToken(AppUtil.getAccessToken());
                ActiveRecordCloudSync.setVersionCode(AppUtil.getVersionCode());
                ActiveRecordCloudSync.downloadNotification(SurveyApp.getInstance(),
                        android.R.drawable.stat_sys_download, R.string.sync_notification_text);
                for (Image image : Image.getAll()) {
                    HttpUtil.getFile(image);
                }
                ActiveRecordCloudSync.downloadNotification(SurveyApp.getInstance(),
                        android.R.drawable.stat_sys_download_done, R.string.sync_notification_complete_text);

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mListener.onAsyncTaskFinished();
        }
    }

    private static class InstrumentSanitizerTask extends AsyncTask<Object, Void, Boolean> {
        private AsyncTaskListener mListener;

        public interface AsyncTaskListener {
            void onAsyncTaskFinished(Boolean last);
        }

        void setListener(AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            ((Instrument) params[0]).setLoops();
            ((Instrument) params[0]).sanitize();
            return ((Boolean) params[1]);
        }

        @Override
        protected void onPostExecute(Boolean last) {
            super.onPostExecute(last);
            mListener.onAsyncTaskFinished(last);
        }
    }

}
