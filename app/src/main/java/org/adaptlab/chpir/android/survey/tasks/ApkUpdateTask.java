package org.adaptlab.chpir.android.survey.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.adaptlab.chpir.android.survey.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ApkUpdateTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "ApkUpdateTask";
    private Context mContext;
    private int mApkId;
    private Integer mLatestVersion;
    private String mFileName;
    private File mFile;

    public ApkUpdateTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(mContext, R.string.background_process_progress_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetworkNotificationUtils.checkForNetworkErrors(mContext)) {
            checkLatestApk();
        }
        return null;
    }

    private void checkLatestApk() {
        ActiveRecordCloudSync.setAccessToken(AppUtil.getAdminSettingsInstance().getApiKey());
        ActiveRecordCloudSync.setVersionCode(AppUtil.getVersionCode(mContext));
        String url = AppUtil.getAdminSettingsInstance().getApiUrl() + "android_updates" + ActiveRecordCloudSync.getParams();
        try {
            String jsonString = getUrl(url);
            if (AppUtil.DEBUG) Log.i(TAG, "Got JSON String: " + jsonString);
            if (!jsonString.trim().equals("null")) {
                JSONObject obj = new JSONObject(jsonString);
                mLatestVersion = obj.getInt("version");
                mApkId = obj.getInt("id");
                mFileName = UUID.randomUUID().toString() + ".apk";
                if (AppUtil.DEBUG) Log.i(TAG, "Latest version is: " + mLatestVersion + ". Old version is: " + AppUtil.getVersionCode(mContext));
            }
        } catch (ConnectException cre) {
            if (AppUtil.DEBUG) Log.e(TAG, "Connection was refused", cre);
        } catch (IOException ioe) {
            if (AppUtil.DEBUG) Log.e(TAG, "Failed to fetch items", ioe);
        } catch (NullPointerException npe) {
            if (AppUtil.DEBUG) Log.e(TAG, "Url is null", npe);
        } catch (JSONException je) {
            if (AppUtil.DEBUG) Log.e(TAG, "Failed to parse items", je);
        }
    }

    @Override
    protected void onPostExecute(Void param) {
        if (mLatestVersion != null && mLatestVersion > AppUtil.getVersionCode(mContext)) {
            if (!((Activity) mContext).isFinishing()) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.new_apk_title)
                        .setMessage(R.string.new_apk_message)
                        .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int button) {
                                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    Toast.makeText(mContext, R.string.re_check_for_updates, Toast.LENGTH_LONG).show();
                                } else {
                                    new DownloadApkTask(mContext).execute();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        }).show();
            }
        } else {
            Toast.makeText(mContext, R.string.up_to_date, Toast.LENGTH_LONG).show();
        }
    }

    private String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

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


    private class DownloadApkTask extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog progressDialog;
        private Context mContext;
        private double contentLength = 0.0;
        private int bytesRead = 0;
        private double total = 0.0;

        private DownloadApkTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle(mContext.getString(R.string.instrument_loading_progress_header));
            progressDialog.setMessage(mContext.getString(R.string.background_process_progress_message));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (NetworkNotificationUtils.checkForNetworkErrors(mContext)) {
                downloadLatestApk();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(mFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }

        private void downloadLatestApk() {
            String url = AppUtil.getAdminSettingsInstance().getApiUrl() + "android_updates/" + mApkId + "/" + ActiveRecordCloudSync.getParams();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            mFile = new File(path, mFileName);
            FileOutputStream fileWriter = null;
            try {
                byte[] imageBytes = getBytes(url);
                fileWriter = new FileOutputStream(mFile);
                fileWriter.write(imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileWriter != null)
                        fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private byte[] getBytes(String urlSpec) throws IOException {
            URL url = new URL(urlSpec);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = connection.getInputStream();
                contentLength = connection.getContentLength();
                if (contentLength == -1) contentLength = 5000000.0; // File size unknown

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    total += bytesRead;
                    int progress = ((int) ((total / contentLength) * 100));
                    publishProgress(progress);
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            } finally {
                connection.disconnect();
            }
        }

    }


}
