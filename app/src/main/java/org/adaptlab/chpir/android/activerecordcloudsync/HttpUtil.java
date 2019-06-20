package org.adaptlab.chpir.android.activerecordcloudsync;

import android.content.Context;
import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.models.Image;
import org.apache.commons.codec.CharEncoding;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class HttpUtil {
    private static final String TAG = "HttpUtil";
    private static final int TIMEOUT = 10000;

    public static void postData(SendModel element, String tableName) {
        if (!element.isSent() && element.readyToSend()) {
            HttpURLConnection connection = null;
            String endPoint = ActiveRecordCloudSync.getEndPoint() + tableName +
                    ActiveRecordCloudSync.getParams();
            try {
                connection = (HttpURLConnection) new URL(endPoint).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);
                connection.setDoOutput(true);

                byte[] outputInBytes = element.toJSON().toString().getBytes(CharEncoding.UTF_8);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(outputInBytes);
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    if (BuildConfig.DEBUG) Log.i(TAG, "Received OK HTTP code for " +
                            element.toJSON());
                    element.setAsSent(SurveyApp.getInstance());
                } else {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Received BAD HTTP code " + responseCode +
                            " for " + element.toJSON());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public static void getFile(Image image) {
        String[] imageUrl = image.getPhotoUrl().split("/");
        String url = ActiveRecordCloudSync.getEndPoint() + "images/" + imageUrl[2] + "/"
                + ActiveRecordCloudSync.getParams();
        if (BuildConfig.DEBUG) Log.i(TAG, "Image url: " + url);
        String filename = UUID.randomUUID().toString() + ".jpg";
        FileOutputStream fileWriter = null;
        try {
            byte[] imageBytes = getUrlBytes(url);
            if (imageBytes != null) {
                fileWriter = SurveyApp.getInstance().openFileOutput(filename, Context.MODE_PRIVATE);
                fileWriter.write(imageBytes);
                image.setBitmapPath(filename);
                image.save();
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

    private static byte[] getUrlBytes(String urlSpec) throws IOException {
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
