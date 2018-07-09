package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;
import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.models.Image;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Project;
import org.adaptlab.chpir.android.survey.tasks.SendResponsesTask;
import org.adaptlab.chpir.android.survey.tasks.SetScoreUnitOrderingQuestionTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static org.adaptlab.chpir.android.survey.utils.AppUtil.getProjectId;

public class Instrument2Activity extends AppCompatActivity {
    public final static String EXTRA_AUTHORIZE_SURVEY =
            "org.adaptlab.chpir.android.survey.authorize_survey_bool";
    private final static int DEFAULT_SETTINGS_CODE = 101;
    private ProgressDialog mProgressDialog;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private boolean mAuthorizeSurvey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.appInit(this);
        setContentView(R.layout.activity_instrument2);
        if (getIntent() != null) {
            mAuthorizeSurvey = getIntent().getBooleanExtra(EXTRA_AUTHORIZE_SURVEY, false);
        }
        requestNeededPermissions();
        checkApiEndpointSettings();
        setupViewPager();
    }

    private void requestNeededPermissions() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (!hasPermission(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    private void checkApiEndpointSettings() {
        AdminSettings adminSettings = AdminSettings.getInstance();
        if (TextUtils.isEmpty(adminSettings.getApiDomainName()) || TextUtils.isEmpty
                (adminSettings.getApiVersion()) || TextUtils.isEmpty(adminSettings.getProjectId()
        ) || TextUtils.isEmpty(adminSettings.getApiKey())) {
            Intent i = new Intent(this, AdminActivity.class);
            startActivityForResult(i, DEFAULT_SETTINGS_CODE);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == DEFAULT_SETTINGS_CODE) {
            downloadInstruments();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViewPager();
        displayProjectName();
    }

    public void displayProjectName() {
        Project project = Project.findByRemoteId(getProjectId());
        TextView textView = findViewById(R.id.project_name);
        if (project != null && textView != null) {
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
                Intent i = new Intent(this, AdminActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(i);
                }
                return true;
            case R.id.menu_item_refresh:
                downloadInstruments();
                new SendResponsesTask(this).execute();
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
                                        AppUtil.getAdminSettingsInstance().setLastSyncTime(
                                                ActiveRecordCloudSync.getLastSyncTime());
                                        AppUtil.orderInstrumentsSections();
                                        mFragmentPagerAdapter.getInstrumentViewPagerFragment()
                                                .refreshRecyclerView();
                                        dismissProgressDialog();
                                    }
                                }
                            });
                            sanitizerTask.execute(instruments.get(k), (k == instruments.size() - 1));
                        }
                        if (instruments.size() == 0) {
                            dismissProgressDialog();
                        }
                    }
                });
                refreshImagesTask.execute();
            }
        });
        asyncTask.execute();
    }

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
        if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
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
            if (NetworkNotificationUtils.checkForNetworkErrors(AppUtil.getContext())) {
                List<Instrument> instruments = Instrument.getAllProjectInstruments(getProjectId());
                for (int k = 0; k < instruments.size(); k++) {
                    if (!instruments.get(k).loaded()) {
                        AdminSettings.getInstance().resetLastSyncTime();
                        break;
                    }
                }
                ActiveRecordCloudSync.syncReceiveTables(AppUtil.getContext());
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
            if (NetworkNotificationUtils.checkForNetworkErrors(AppUtil.getContext())) {
                downloadImages();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mListener.onAsyncTaskFinished();
        }

        private void downloadImages() {
            ActiveRecordCloudSync.setAccessToken(AppUtil.getAdminSettingsInstance().getApiKey());
            ActiveRecordCloudSync.setVersionCode(AppUtil.getVersionCode(AppUtil.getContext()));

            for (Image img : Image.getAll()) {
                String[] imageUrl = img.getPhotoUrl().split("/");
                String url = ActiveRecordCloudSync.getEndPoint() + "images/" + imageUrl[2] + "/"
                        + ActiveRecordCloudSync.getParams();
                if (BuildConfig.DEBUG) Log.i(TAG, "Image url: " + url);
                String filename = UUID.randomUUID().toString() + ".jpg";
                FileOutputStream fileWriter = null;
                try {
                    byte[] imageBytes = getUrlBytes(url);
                    if (imageBytes != null) {
                        fileWriter = AppUtil.getContext().openFileOutput(filename,
                                Context.MODE_PRIVATE);
                        fileWriter.write(imageBytes);
                        img.setBitmapPath(filename);
                        img.save();
                    }
                    if (BuildConfig.DEBUG) Log.i(TAG, "Image saved in " + filename);
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "IOException ", e);
                } finally {
                    try {
                        if (fileWriter != null) {
                            fileWriter.close();
                        }
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) Log.e(TAG, "Exception ", e);
                    }
                }
            }
        }

        private byte[] getUrlBytes(String urlSpec) throws IOException {
            URL url = new URL(urlSpec);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
                InputStream in = connection.getInputStream();
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            } finally {
                connection.disconnect();
            }
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
