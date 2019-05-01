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
import org.adaptlab.chpir.android.survey.daos.ConditionSkipDao;
import org.adaptlab.chpir.android.survey.daos.CriticalResponseDao;
import org.adaptlab.chpir.android.survey.daos.DeviceUserDao;
import org.adaptlab.chpir.android.survey.daos.DisplayDao;
import org.adaptlab.chpir.android.survey.daos.DisplayInstructionDao;
import org.adaptlab.chpir.android.survey.daos.DisplayTranslationDao;
import org.adaptlab.chpir.android.survey.daos.FollowUpQuestionDao;
import org.adaptlab.chpir.android.survey.daos.InstructionDao;
import org.adaptlab.chpir.android.survey.daos.InstructionTranslationDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentTranslationDao;
import org.adaptlab.chpir.android.survey.daos.LoopQuestionDao;
import org.adaptlab.chpir.android.survey.daos.MultipleSkipDao;
import org.adaptlab.chpir.android.survey.daos.NextQuestionDao;
import org.adaptlab.chpir.android.survey.daos.OptionDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetOptionDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetTranslationDao;
import org.adaptlab.chpir.android.survey.daos.OptionTranslationDao;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.daos.QuestionTranslationDao;
import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.daos.SectionTranslationDao;
import org.adaptlab.chpir.android.survey.daos.SettingsDao;
import org.adaptlab.chpir.android.survey.entities.ConditionSkip;
import org.adaptlab.chpir.android.survey.entities.CriticalResponse;
import org.adaptlab.chpir.android.survey.entities.DeviceUser;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.entities.DisplayTranslation;
import org.adaptlab.chpir.android.survey.entities.FollowUpQuestion;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.InstructionTranslation;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.entities.LoopQuestion;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.OptionSet;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;
import org.adaptlab.chpir.android.survey.entities.OptionSetTranslation;
import org.adaptlab.chpir.android.survey.entities.OptionTranslation;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.QuestionTranslation;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.SectionTranslation;
import org.adaptlab.chpir.android.survey.entities.Settings;

import java.util.UUID;

@Database(entities = {Instrument.class, InstrumentTranslation.class, Question.class, Settings.class,
        QuestionTranslation.class, LoopQuestion.class, CriticalResponse.class, Display.class,
        DisplayTranslation.class, DisplayInstruction.class, Instruction.class, InstructionTranslation.class,
        Section.class, SectionTranslation.class, Option.class, OptionSet.class, OptionSetOption.class,
        OptionSetTranslation.class, OptionTranslation.class, ConditionSkip.class, DeviceUser.class,
        FollowUpQuestion.class, MultipleSkip.class, NextQuestion.class},
        version = 1)
@TypeConverters({Converters.class})
public abstract class SurveyRoomDatabase extends RoomDatabase {
    private static volatile SurveyRoomDatabase INSTANCE;
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
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

    public abstract InstrumentDao instrumentDao();

    public abstract InstrumentTranslationDao instrumentTranslationDao();

    public abstract QuestionDao questionDao();

    public abstract SettingsDao settingsDao();

    public abstract QuestionTranslationDao questionTranslationDao();

    public abstract LoopQuestionDao loopQuestionDao();

    public abstract CriticalResponseDao criticalResponseDao();

    public abstract DisplayDao displayDao();

    public abstract DisplayTranslationDao displayTranslationDao();

    public abstract DisplayInstructionDao displayInstructionDao();

    public abstract InstructionDao instructionDao();

    public abstract InstructionTranslationDao instructionTranslationDao();

    public abstract SectionDao sectionDao();

    public abstract SectionTranslationDao sectionTranslationDao();

    public abstract OptionDao optionDao();

    public abstract OptionSetDao optionSetDao();

    public abstract OptionSetOptionDao optionSetOptionDao();

    public abstract OptionSetTranslationDao optionSetTranslationDao();

    public abstract OptionTranslationDao optionTranslationDao();

    public abstract ConditionSkipDao conditionSkipDao();

    public abstract DeviceUserDao deviceUserDao();

    public abstract FollowUpQuestionDao followUpQuestionDao();

    public abstract MultipleSkipDao multipleSkipDao();

    public abstract NextQuestionDao nextQuestionDao();

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
