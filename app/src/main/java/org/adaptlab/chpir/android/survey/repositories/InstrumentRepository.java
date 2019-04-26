package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentTranslationDao;
import org.adaptlab.chpir.android.survey.daos.SettingsDao;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class InstrumentRepository implements Downloadable {
    private InstrumentDao mInstrumentDao;
    private InstrumentTranslationDao mInstrumentTranslationDao;
    private LiveData<List<Instrument>> mAllInstruments;
    private LiveData<List<InstrumentTranslation>> mAllInstrumentTranslations;

    public InstrumentRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mInstrumentDao = db.instrumentDao();
        mInstrumentTranslationDao = db.instrumentTranslationDao();
        mAllInstruments = mInstrumentDao.getAllInstruments();
        mAllInstrumentTranslations = mInstrumentTranslationDao.getAllInstrumentTranslations();
    }

    public LiveData<List<Instrument>> getAllInstruments() {
        return mAllInstruments;
    }

    public LiveData<List<InstrumentTranslation>> getAllInstrumentTranslations() {
        return mAllInstrumentTranslations;
    }

    public InstrumentDao getInstrumentDao() {
        return mInstrumentDao;
    }

    public void download() {
        DownloadInstrumentsTask downloadInstrumentsTask = new DownloadInstrumentsTask(
                mInstrumentDao, mInstrumentTranslationDao);
        downloadInstrumentsTask.setListener(new DownloadInstrumentsTask.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished() {
                mAllInstruments = mInstrumentDao.getAllInstruments();
                mAllInstrumentTranslations = mInstrumentTranslationDao.getAllInstrumentTranslations();
            }
        });
        downloadInstrumentsTask.execute();
    }

    private static class DownloadInstrumentsTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadQuestionsTask";
        private InstrumentDao mInstrumentDao;
        private InstrumentTranslationDao mInstrumentTranslationDao;
        private AsyncTaskListener mListener;

        DownloadInstrumentsTask(InstrumentDao instrumentDao, InstrumentTranslationDao instrumentTranslationDao) {
            mInstrumentDao = instrumentDao;
            mInstrumentTranslationDao = instrumentTranslationDao;
        }

        void setListener(AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            downloadInstruments();
            return null;
        }

        private void downloadInstruments() {
            String url = AppUtil.getFullApiUrl() + "instruments" + AppUtil.getParams();
            final Request request = new okhttp3.Request.Builder().url(url).build();

            AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Instrument download exception: ", e);
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (BuildConfig.DEBUG) Log.i(TAG, "Instruments: " + responseString);
                            Gson gson = new Gson();
                            Type instrumentType = new TypeToken<ArrayList<Instrument>>() {
                            }.getType();
                            List<Instrument> instruments = gson.fromJson(responseString, instrumentType);
                            mInstrumentDao.updateAll(instruments);
                            mInstrumentDao.insertAll(instruments);
                            List<InstrumentTranslation> instrumentTranslations = new ArrayList<>();
                            for (Instrument instrument : instruments) {
                                instrumentTranslations.addAll(instrument.getInstrumentTranslations());
                            }
                            mInstrumentTranslationDao.updateAll(instrumentTranslations);
                            mInstrumentTranslationDao.insertAll(instrumentTranslations);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
                        }
                        response.close();
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Instrument download not successful");
                        response.close();
                    }
                }
            });
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mListener.onAsyncTaskFinished();
        }

        public interface AsyncTaskListener {
            void onAsyncTaskFinished();
        }

    }

}
