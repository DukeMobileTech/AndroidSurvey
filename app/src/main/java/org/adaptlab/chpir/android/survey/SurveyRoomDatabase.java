package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import org.adaptlab.chpir.android.survey.converters.Converters;
import org.adaptlab.chpir.android.survey.daos.ConditionSkipDao;
import org.adaptlab.chpir.android.survey.daos.CriticalResponseDao;
import org.adaptlab.chpir.android.survey.daos.DeviceUserDao;
import org.adaptlab.chpir.android.survey.daos.DisplayDao;
import org.adaptlab.chpir.android.survey.daos.DisplayInstructionDao;
import org.adaptlab.chpir.android.survey.daos.DisplayTranslationDao;
import org.adaptlab.chpir.android.survey.daos.DomainDao;
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
import org.adaptlab.chpir.android.survey.daos.ProjectDao;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.daos.QuestionTranslationDao;
import org.adaptlab.chpir.android.survey.daos.ResponseDao;
import org.adaptlab.chpir.android.survey.daos.ScoreSchemeDao;
import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.daos.SectionTranslationDao;
import org.adaptlab.chpir.android.survey.daos.SettingsDao;
import org.adaptlab.chpir.android.survey.daos.SubdomainDao;
import org.adaptlab.chpir.android.survey.daos.SurveyDao;
import org.adaptlab.chpir.android.survey.daos.SurveyNoteDao;
import org.adaptlab.chpir.android.survey.daos.SurveyScoreDao;
import org.adaptlab.chpir.android.survey.entities.ConditionSkip;
import org.adaptlab.chpir.android.survey.entities.CriticalResponse;
import org.adaptlab.chpir.android.survey.entities.DeviceUser;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.entities.DisplayTranslation;
import org.adaptlab.chpir.android.survey.entities.Domain;
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
import org.adaptlab.chpir.android.survey.entities.Project;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.QuestionTranslation;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.ScoreScheme;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.SectionTranslation;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.entities.Subdomain;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.SurveyNote;
import org.adaptlab.chpir.android.survey.entities.SurveyScore;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.Locale;
import java.util.UUID;

@Database(entities = {Instrument.class, InstrumentTranslation.class, Question.class, Settings.class,
        QuestionTranslation.class, LoopQuestion.class, CriticalResponse.class, Display.class, Project.class,
        DisplayTranslation.class, DisplayInstruction.class, Instruction.class, InstructionTranslation.class,
        Section.class, SectionTranslation.class, Option.class, OptionSet.class, OptionSetOption.class,
        OptionSetTranslation.class, OptionTranslation.class, ConditionSkip.class, DeviceUser.class,
        FollowUpQuestion.class, MultipleSkip.class, NextQuestion.class, Survey.class, Response.class,
        SurveyNote.class, ScoreScheme.class, Domain.class, Subdomain.class, SurveyScore.class},
        version = SurveyRoomDatabase.DATABASE_VERSION, exportSchema = true)
@TypeConverters({Converters.class})
public abstract class SurveyRoomDatabase extends RoomDatabase {
    static final int DATABASE_VERSION = 8;
    private static final String TAG = SurveyRoomDatabase.class.getName();
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Settings ADD COLUMN Latitude TEXT");
            database.execSQL("ALTER TABLE Settings ADD COLUMN Longitude TEXT");
        }
    };
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE OptionSetOptions ADD COLUMN InstructionId INTEGER");
            database.execSQL("ALTER TABLE OptionSetOptions ADD COLUMN AllowTextEntry INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE Responses ADD COLUMN OtherText TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN PopUpInstruction INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE Questions ADD COLUMN InstructionAfterText INTEGER DEFAULT 0 NOT NULL");
        }
    };
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Questions ADD COLUMN CarryForwardIdentifier TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN DefaultResponse TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN CarryForwardOptionSetId INTEGER");
            database.execSQL("CREATE TABLE IF NOT EXISTS SurveyNotes (`UUID` TEXT NOT NULL, `SurveyUUID` TEXT NOT NULL, " +
                    "`Text` TEXT, `Reference` TEXT, `SentToRemote` INTEGER NOT NULL, `DeviceUserId` INTEGER, PRIMARY KEY(`UUID`))");
            database.execSQL("CREATE  INDEX `index_SurveyNotes_UUID` ON SurveyNotes (`UUID`)");
            database.execSQL("CREATE  INDEX `index_SurveyNotes_SurveyUUID` ON SurveyNotes (`SurveyUUID`)");
        }
    };
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE OptionSetOptions ADD COLUMN ExclusionIds TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN SkipOperation TEXT");
        }
    };
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE NextQuestions ADD COLUMN ValueOperator TEXT");
        }
    };
    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE MultipleSkips ADD COLUMN ValueOperator TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN NextQuestionOperator TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN MultipleSkipOperator TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN NextQuestionNeutralIds TEXT");
            database.execSQL("ALTER TABLE Questions ADD COLUMN MultipleSkipNeutralIds TEXT");
        }
    };
    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ConditionSkips ADD COLUMN QuestionIdentifiers TEXT");
            database.execSQL("ALTER TABLE ConditionSkips ADD COLUMN OptionIds TEXT");
            database.execSQL("ALTER TABLE ConditionSkips ADD COLUMN Entries TEXT");
            database.execSQL("ALTER TABLE ConditionSkips ADD COLUMN ValueOperators TEXT");
        }
    };
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
        String password = AppUtil.getDatabaseKey();
        final byte[] passphrase = SQLiteDatabase.getBytes(password.toCharArray());
        final SupportFactory factory = new SupportFactory(passphrase);
        if (INSTANCE == null) {
            synchronized (SurveyRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SurveyRoomDatabase.class, "SurveyDatabase")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                            .openHelperFactory(factory)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
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

    public abstract SurveyDao surveyDao();

    public abstract ResponseDao responseDao();

    public abstract ProjectDao projectDao();

    public abstract SurveyNoteDao surveyNoteDao();

    public abstract ScoreSchemeDao scoreSchemeDao();

    public abstract DomainDao domainDao();

    public abstract SubdomainDao subdomainDao();

    public abstract SurveyScoreDao surveyScoreDao();

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
                settings.setId(1L);
                settings.setDeviceIdentifier(UUID.randomUUID().toString());
                settings.setDeviceLabel(Build.MODEL);
                settings.setLanguage(Locale.getDefault().getLanguage());
                mSettingsDao.insert(settings);
                if (BuildConfig.DEBUG) Log.i(TAG, "Language: " + settings.getLanguage());
            }
            return null;
        }
    }

}
