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
import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.daos.SectionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.InstructionTranslation;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.SectionTranslation;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class SectionRepository implements Downloadable {
    private SectionDao mSectionDao;
    private SectionTranslationDao mSectionTranslationDao;

    public SectionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSectionDao = db.sectionDao();
        mSectionTranslationDao = db.sectionTranslationDao();
    }

    @Override
    public void download() {
        new DownloadTask(mSectionDao, mSectionTranslationDao).execute();
    }

    private static class DownloadTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadTask";
        private SectionDao mSectionDao;
        private SectionTranslationDao mSectionTranslationDao;

        DownloadTask(SectionDao dao, SectionTranslationDao translationDao) {
            mSectionDao = dao;
            mSectionTranslationDao = translationDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            download();
            return null;
        }

        private void download() {
            String url = AppUtil.getFullApiUrl() + "sections" + AppUtil.getParams();
            final Request request = new Request.Builder().url(url).build();

            AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Section download exception: ", e);
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (BuildConfig.DEBUG) Log.i(TAG, "Section: " + responseString);
                            Gson gson = new Gson();
                            Type type = new TypeToken<ArrayList<Section>>() {
                            }.getType();
                            List<Section> sections = gson.fromJson(responseString, type);
                            mSectionDao.updateAll(sections);
                            mSectionDao.insertAll(sections);
                            List<SectionTranslation> translations = new ArrayList<>();
                            for (Section section : sections) {
                                translations.addAll(section.getSectionTranslations());
                            }
                            mSectionTranslationDao.updateAll(translations);
                            mSectionTranslationDao.insertAll(translations);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
                        }
                        response.close();
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Section download not successful");
                        response.close();
                    }
                }
            });
        }
    }
}
