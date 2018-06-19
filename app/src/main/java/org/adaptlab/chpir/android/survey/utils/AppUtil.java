package org.adaptlab.chpir.android.survey.utils;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.crashlytics.android.Crashlytics;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.PollService;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.models.DefaultAdminSettings;
import org.adaptlab.chpir.android.survey.models.DeviceSyncEntry;
import org.adaptlab.chpir.android.survey.models.DeviceUser;
import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.DisplayInstruction;
import org.adaptlab.chpir.android.survey.models.EventLog;
import org.adaptlab.chpir.android.survey.models.FollowUpQuestion;
import org.adaptlab.chpir.android.survey.models.Grid;
import org.adaptlab.chpir.android.survey.models.GridLabel;
import org.adaptlab.chpir.android.survey.models.GridLabelTranslation;
import org.adaptlab.chpir.android.survey.models.GridTranslation;
import org.adaptlab.chpir.android.survey.models.Image;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.models.MultipleSkip;
import org.adaptlab.chpir.android.survey.models.NextQuestion;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionInOptionSet;
import org.adaptlab.chpir.android.survey.models.OptionScore;
import org.adaptlab.chpir.android.survey.models.OptionSet;
import org.adaptlab.chpir.android.survey.models.OptionTranslation;
import org.adaptlab.chpir.android.survey.models.Project;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.QuestionRandomizedFactor;
import org.adaptlab.chpir.android.survey.models.QuestionTranslation;
import org.adaptlab.chpir.android.survey.models.RandomizedFactor;
import org.adaptlab.chpir.android.survey.models.RandomizedOption;
import org.adaptlab.chpir.android.survey.models.RawScore;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ResponsePhoto;
import org.adaptlab.chpir.android.survey.models.Roster;
import org.adaptlab.chpir.android.survey.models.Rule;
import org.adaptlab.chpir.android.survey.models.Score;
import org.adaptlab.chpir.android.survey.models.ScoreScheme;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.ScoreUnitQuestion;
import org.adaptlab.chpir.android.survey.models.Section;
import org.adaptlab.chpir.android.survey.models.SectionTranslation;
import org.adaptlab.chpir.android.survey.models.Skip;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.vendor.BCrypt;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;

public class AppUtil {
    private final static String TAG = "AppUtil";
    public final static boolean PRODUCTION = !BuildConfig.DEBUG;
    public final static boolean REQUIRE_SECURITY_CHECKS = PRODUCTION;
    public static boolean DEBUG = !PRODUCTION;

    public static String ADMIN_PASSWORD_HASH;
    public static String ACCESS_TOKEN;
    private static Context mContext;
    private static AdminSettings adminSettingsInstance;

    /*
     * Get the version code from the AndroidManifest
     */
    public static int getVersionCode(Context context) {
        try {
            if (context != null) {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return pInfo.versionCode;
            }
        } catch (NameNotFoundException nnfe) {
            Log.e(TAG, "Error finding version code: " + nnfe);
        }
        return -1;
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (NameNotFoundException nnfe) {
            Log.e(TAG, "Error finding version code: " + nnfe);
        }
        return "";
    }

    public static String getDeviceLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (AppUtil.getAdminSettingsInstance() == null) return language;
        if (!TextUtils.isEmpty(AppUtil.getAdminSettingsInstance().getCustomLocaleCode())) {
            language = AppUtil.getAdminSettingsInstance().getCustomLocaleCode();
        } else if (!TextUtils.isEmpty(AppUtil.getAdminSettingsInstance().getLanguage())) {
            language = AppUtil.getAdminSettingsInstance().getLanguage();
        }
        return language;
    }

    public static void appInit(Context context) {
        mContext = context;
        if (AppUtil.REQUIRE_SECURITY_CHECKS) {
            if (!AppUtil.runDeviceSecurityChecks(context)) {
                // Device has failed security checks
                return;
            }
        }

        setAdminSettingsInstance();

        ADMIN_PASSWORD_HASH = context.getResources().getString(R.string.admin_password_hash);
        ACCESS_TOKEN = adminSettingsInstance.getApiKey();

        if (PRODUCTION) {
            Fabric.with(context, new Crashlytics());
            Crashlytics.setUserIdentifier(adminSettingsInstance.getDeviceIdentifier());
            Crashlytics.setString(getContext().getString(R.string.crashlytics_device_label), adminSettingsInstance.getDeviceLabel());
        }

        DatabaseSeed.seed(context);

        if (TextUtils.isEmpty(adminSettingsInstance.getDeviceIdentifier())) {
            adminSettingsInstance.setDeviceIdentifier(UUID.randomUUID().toString());
        }

        if (TextUtils.isEmpty(adminSettingsInstance.getDeviceLabel())) {
            adminSettingsInstance.setDeviceLabel(getBuildName());
        }

        ActiveRecordCloudSync.setAccessToken(ACCESS_TOKEN);
        ActiveRecordCloudSync.setVersionCode(AppUtil.getVersionCode(context));
        ActiveRecordCloudSync.setEndPoint(adminSettingsInstance.getApiUrl());
        ActiveRecordCloudSync.addReceiveTable("projects", Project.class);
        ActiveRecordCloudSync.addReceiveTable("instruments", Instrument.class);
        ActiveRecordCloudSync.addReceiveTable("sections", Section.class);
//        ActiveRecordCloudSync.addReceiveTable("grids", Grid.class);
        ActiveRecordCloudSync.addReceiveTable("questions", Question.class);
        ActiveRecordCloudSync.addReceiveTable("options", Option.class);
        ActiveRecordCloudSync.addReceiveTable("randomized_factors", RandomizedFactor.class);
        ActiveRecordCloudSync.addReceiveTable("randomized_options", RandomizedOption.class);
        ActiveRecordCloudSync.addReceiveTable("question_randomized_factors", QuestionRandomizedFactor.class);
//        ActiveRecordCloudSync.addReceiveTable("grid_labels", GridLabel.class);
        ActiveRecordCloudSync.addReceiveTable("images", Image.class);
        ActiveRecordCloudSync.addReceiveTable("device_users", DeviceUser.class);
//        ActiveRecordCloudSync.addReceiveTable("skips", Skip.class);
        ActiveRecordCloudSync.addReceiveTable("rules", Rule.class);
        ActiveRecordCloudSync.addReceiveTable("score_schemes", ScoreScheme.class);
        ActiveRecordCloudSync.addReceiveTable("score_units", ScoreUnit.class);
        ActiveRecordCloudSync.addReceiveTable("option_scores", OptionScore.class);
        ActiveRecordCloudSync.addReceiveTable("score_unit_questions", ScoreUnitQuestion.class);
        ActiveRecordCloudSync.addReceiveTable("displays", Display.class);
        ActiveRecordCloudSync.addReceiveTable("display_instructions", DisplayInstruction.class);
        ActiveRecordCloudSync.addReceiveTable("next_questions", NextQuestion.class);
        ActiveRecordCloudSync.addReceiveTable("multiple_skips", MultipleSkip.class);
        ActiveRecordCloudSync.addReceiveTable("follow_up_questions", FollowUpQuestion.class);
        ActiveRecordCloudSync.addReceiveTable("option_in_option_sets", OptionInOptionSet.class);
        ActiveRecordCloudSync.addReceiveTable("option_sets", OptionSet.class);
        ActiveRecordCloudSync.addSendTable("surveys", Survey.class);
        ActiveRecordCloudSync.addSendTable("responses", Response.class);
        ActiveRecordCloudSync.addSendTable("response_images", ResponsePhoto.class);
        ActiveRecordCloudSync.addSendTable("device_sync_entries", DeviceSyncEntry.class);
        ActiveRecordCloudSync.addSendTable("rosters", Roster.class);
        ActiveRecordCloudSync.addSendTable("scores", Score.class);
        ActiveRecordCloudSync.addSendTable("raw_scores", RawScore.class);

        PollService.setServiceAlarm(context.getApplicationContext(), true);
    }

    private static void setAdminSettingsInstance() {
        if (getContext() != null && getContext().getResources() != null && getContext().getResources().getBoolean(R.bool.default_admin_settings)) {
            adminSettingsInstance = DefaultAdminSettings.getInstance();
        } else {
            adminSettingsInstance = AdminSettings.getInstance();
        }
    }

    public static Long getProjectId() {
        AdminSettings adminSettings = getAdminSettingsInstance();
        if (adminSettings.getProjectId() != null)
            return Long.parseLong(adminSettings.getProjectId());
        return Long.MAX_VALUE;
    }

    /*
     * Security checks that must pass for the application to start.
     * 
     * If the application fails any security checks, display
     * AlertDialog indicating why and immediately stop execution
     * of the application.
     * 
     * Current security checks: require encryption
     */
    public static final boolean runDeviceSecurityChecks(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.encryption_required_title)
                    .setMessage(R.string.encryption_required_text)
                    .setCancelable(false)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Kill app on OK
                            int pid = android.os.Process.myPid();
                            android.os.Process.killProcess(pid);
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }


    /*
     * Hash the entered password and compare it with admin password hash
     */
    public static boolean checkAdminPassword(String password) {
        return BCrypt.checkpw(password, ADMIN_PASSWORD_HASH);
    }

    public static Context getContext() {
        if (mContext == null) {
            mContext = SurveyApp.getInstance().getApplicationContext();
        }
        return mContext;
    }

    public static void setContext(Context context) {
        mContext = context;
    }

    public static AdminSettings getAdminSettingsInstance() {
        if (adminSettingsInstance == null) {
            setAdminSettingsInstance();
        }
        return adminSettingsInstance;
    }

    public static String getOsBuildNumber() {
        return Build.DISPLAY;
    }

    public static String getBuildName() {
        return Build.MODEL;
    }

    public static void deleteApplicationData() {
        ActiveAndroid.beginTransaction();
        try {
            AppUtil.getAdminSettingsInstance().resetLastSyncTime();
            new Delete().from(ResponsePhoto.class).execute();
            new Delete().from(Response.class).execute();
            new Delete().from(Survey.class).execute();
            new Delete().from(Roster.class).execute();
            new Delete().from(Rule.class).execute();
            new Delete().from(Skip.class).execute();
            new Delete().from(DeviceUser.class).execute();
            new Delete().from(Image.class).execute();
            new Delete().from(GridLabelTranslation.class).execute();
            new Delete().from(GridLabel.class).execute();
            new Delete().from(GridTranslation.class).execute();
            new Delete().from(Grid.class).execute();
            new Delete().from(InstrumentTranslation.class).execute();
            new Delete().from(OptionTranslation.class).execute();
            new Delete().from(QuestionTranslation.class).execute();
            new Delete().from(SectionTranslation.class).execute();
            new Delete().from(Option.class).execute();
            new Delete().from(Question.class).execute();
            new Delete().from(Section.class).execute();
            new Delete().from(EventLog.class).execute();
            new Delete().from(Instrument.class).execute();
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public static void orderInstrumentsSections() {
        new OrderInstrumentSectionsTask().execute();
    }

    private static class OrderInstrumentSectionsTask extends AsyncTask<Void, Void, Void> {

        public OrderInstrumentSectionsTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Instrument> instruments = Instrument.getAllProjectInstruments(
                    Long.valueOf(adminSettingsInstance.getProjectId()));
            for (Instrument instrument : instruments) {
                instrument.orderSections();
            }
            return null;
        }
    }

}