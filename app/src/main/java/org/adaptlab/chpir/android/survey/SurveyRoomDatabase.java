package org.adaptlab.chpir.android.survey;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.converters.Converters;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentTranslationDao;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.daos.SettingsDao;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Settings;

import java.util.UUID;

@Database(entities = {Instrument.class, InstrumentTranslation.class, Question.class, Settings.class},
        version = 1)
@TypeConverters({Converters.class})
public abstract class SurveyRoomDatabase extends RoomDatabase {
    public abstract InstrumentDao instrumentDao();

    public abstract InstrumentTranslationDao instrumentTranslationDao();

    public abstract QuestionDao questionDao();

    public abstract SettingsDao settingsDao();

    private static volatile SurveyRoomDatabase INSTANCE;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new CreateSettingsInstanceTask(INSTANCE).execute();
                }
            };

    public static SurveyRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SurveyRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SurveyRoomDatabase.class, "SurveyDatabase")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class CreateSettingsInstanceTask extends AsyncTask<Void, Void, Void> {
        private SettingsDao mSettingsDao;

        CreateSettingsInstanceTask(SurveyRoomDatabase database) {
            mSettingsDao = database.settingsDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Settings settings = mSettingsDao.getInstanceSync();
            if (settings == null) {
                settings = new Settings();
                settings.setDeviceIdentifier(UUID.randomUUID().toString());
                settings.setDeviceLabel(Build.MODEL);
                mSettingsDao.insert(settings);
            }
            return null;
        }
    }

}
