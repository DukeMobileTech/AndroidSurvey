package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.DisplayInstructionDao;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class DisplayInstructionRepository implements Downloadable {
    private DisplayInstructionDao mDisplayInstructionDao;

    public DisplayInstructionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mDisplayInstructionDao = db.displayInstructionDao();
    }

    @Override
    public void download() {
        new DownloadTask(mDisplayInstructionDao).execute();
    }

    private static class DownloadTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadTask";
        private DisplayInstructionDao displayInstructionDao;

        DownloadTask(DisplayInstructionDao dao) {
            displayInstructionDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            download();
            return null;
        }

        private void download() {
            String url = AppUtil.getFullApiUrl() + "display_instructions" + AppUtil.getParams();
            final Request request = new Request.Builder().url(url).build();

            AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "DisplayInstruction download exception: ", e);
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (BuildConfig.DEBUG) Log.i(TAG, "DisplayInstructions: " + responseString);
                            Gson gson = new Gson();
                            Type type = new TypeToken<ArrayList<DisplayInstruction>>() {
                            }.getType();
                            List<DisplayInstruction> displayInstructions = gson.fromJson(responseString, type);
                            displayInstructionDao.updateAll(displayInstructions);
                            displayInstructionDao.insertAll(displayInstructions);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
                        }
                        response.close();
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, "DisplayInstruction download not successful");
                        response.close();
                    }
                }
            });
        }
    }
}
