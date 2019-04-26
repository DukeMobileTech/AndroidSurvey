package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.InstructionDao;
import org.adaptlab.chpir.android.survey.daos.InstructionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.InstructionTranslation;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class InstructionRepository implements Downloadable {
    private InstructionDao mInstructionDao;
    private InstructionTranslationDao mInstructionTranslationDao;

    public InstructionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mInstructionDao = db.instructionDao();
        mInstructionTranslationDao = db.instructionTranslationDao();
    }

    @Override
    public void download() {
        new DownloadTask(mInstructionDao, mInstructionTranslationDao).execute();
    }

    private static class DownloadTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadTask";
        private InstructionDao mInstructionDao;
        private InstructionTranslationDao mInstructionTranslationDao;

        DownloadTask(InstructionDao dao, InstructionTranslationDao translationDao) {
            mInstructionDao = dao;
            mInstructionTranslationDao = translationDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            download();
            return null;
        }

        private void download() {
            String url = AppUtil.getFullApiUrl() + "instructions" + AppUtil.getParams();
            final Request request = new Request.Builder().url(url).build();

            AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Instruction download exception: ", e);
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (BuildConfig.DEBUG) Log.i(TAG, "Instructions: " + responseString);
                            Gson gson = new Gson();
                            Type type = new TypeToken<ArrayList<Instruction>>() {
                            }.getType();
                            List<Instruction> instructions = gson.fromJson(responseString, type);
                            mInstructionDao.updateAll(instructions);
                            mInstructionDao.insertAll(instructions);
                            List<InstructionTranslation> translations = new ArrayList<>();
                            for (Instruction instruction : instructions) {
                                translations.addAll(instruction.getInstructionTranslations());
                            }
                            mInstructionTranslationDao.updateAll(translations);
                            mInstructionTranslationDao.insertAll(translations);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
                        }
                        response.close();
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Instruction download not successful");
                        response.close();
                    }
                }
            });
        }
    }
}
