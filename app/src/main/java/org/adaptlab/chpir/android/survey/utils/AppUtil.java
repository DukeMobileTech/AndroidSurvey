package org.adaptlab.chpir.android.survey.utils;

import android.app.AlertDialog;
import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.daos.DeviceUserDao;
import org.adaptlab.chpir.android.survey.entities.DeviceUser;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.repositories.ConditionSkipRepository;
import org.adaptlab.chpir.android.survey.repositories.CriticalResponseRepository;
import org.adaptlab.chpir.android.survey.repositories.DeviceUserRepository;
import org.adaptlab.chpir.android.survey.repositories.DisplayInstructionRepository;
import org.adaptlab.chpir.android.survey.repositories.DisplayRepository;
import org.adaptlab.chpir.android.survey.repositories.FollowUpQuestionRepository;
import org.adaptlab.chpir.android.survey.repositories.InstructionRepository;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;
import org.adaptlab.chpir.android.survey.repositories.LoopQuestionRepository;
import org.adaptlab.chpir.android.survey.repositories.MultipleSkipRepository;
import org.adaptlab.chpir.android.survey.repositories.NextQuestionRepository;
import org.adaptlab.chpir.android.survey.repositories.OptionRepository;
import org.adaptlab.chpir.android.survey.repositories.OptionSetOptionRepository;
import org.adaptlab.chpir.android.survey.repositories.OptionSetRepository;
import org.adaptlab.chpir.android.survey.repositories.ProjectRepository;
import org.adaptlab.chpir.android.survey.repositories.QuestionRepository;
import org.adaptlab.chpir.android.survey.repositories.SectionRepository;
import org.adaptlab.chpir.android.survey.repositories.SettingsRepository;
import org.adaptlab.chpir.android.survey.tasks.GetDeviceUserTask;
import org.adaptlab.chpir.android.survey.tasks.GetSettingsTask;
import org.adaptlab.chpir.android.survey.tasks.SetLoopsTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;

public class AppUtil {
    public final static boolean PRODUCTION = !BuildConfig.DEBUG;
    private final static String TAG = "AppUtil";
    private final static boolean REQUIRE_SECURITY_CHECKS = PRODUCTION;
    private final static int REMOTE_TABLE_COUNT = 15;
    private static final int TIMEOUT = 10000;
    private static volatile int REMOTE_DOWNLOAD_COUNT = 0;
    private static String ACCESS_TOKEN;
    private static String LAST_SYNC_TIME;
    private static String DOMAIN_NAME;
    private static String API_VERSION;
    private static String DEVICE_LANGUAGE;
    private static String VERSION_NAME;
    private static Long DEVICE_USER_ID;
    private static int VERSION_CODE;
    private static Long PROJECT_ID;
    private static OkHttpClient okHttpClient;
    private static Settings mSettings;
    private static SettingsRepository mSettingsRepository;
    private static String mCurrentSyncTime;

    public static void appInit(Application application) {
        if (AppUtil.REQUIRE_SECURITY_CHECKS) {
            if (!AppUtil.runDeviceSecurityChecks(application)) {
                // Device has failed security checks
                return;
            }
        }
        setSettings(application);
        setVersionCode(application);
        setVersionName(application);
        getOkHttpClient();
        PollService.setServiceAlarm(application, true);
    }

    private static void setDeviceLanguage() {
        DEVICE_LANGUAGE = Locale.getDefault().getLanguage();
        if (!TextUtils.isEmpty(mSettings.getCustomLocaleCode())) {
            DEVICE_LANGUAGE = mSettings.getCustomLocaleCode();
        } else if (!TextUtils.isEmpty(mSettings.getLanguage())) {
            DEVICE_LANGUAGE = mSettings.getLanguage();
        }
    }

    private static void setCrashLogs(Context context) {
        if (PRODUCTION) {
            Fabric.with(context, new Crashlytics());
            Crashlytics.setUserIdentifier(mSettings.getDeviceIdentifier());
            Crashlytics.setString(context.getString(R.string.crashlytics_device_label), mSettings.getDeviceLabel());
        }
    }

    private static void checkDatabaseVersionChange(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            int databaseVersion = 1;
//            int databaseVersion = (int) ai.metaData.get("AA_DB_VERSION"); // TODO: 2019-04-25 FIX
            if (databaseVersion != mSettings.getDatabaseVersion()) {
                mSettings.resetLastSyncTime();
                mSettings.setDatabaseVersion(databaseVersion);
                mSettingsRepository.update(mSettings);
            }
        } catch (NameNotFoundException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
        }
    }

    public static int getVersionCode(Context context) {
        try {
            if (context != null) {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return pInfo.versionCode;
            }
        } catch (NameNotFoundException nnfe) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error finding version code: " + nnfe);
        }
        return -1;
    }

    public static String getVersionName() {
        return VERSION_NAME;
    }

    private static void setVersionName(Context context) {
        VERSION_NAME = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            VERSION_NAME = pInfo.versionName;
        } catch (NameNotFoundException nnfe) {
            Log.e(TAG, "Error finding version code: " + nnfe);
        }
    }

    public static String getDeviceLanguage() {
        return DEVICE_LANGUAGE;
    }

    public static Long getProjectId() {
        return PROJECT_ID;
    }

    public static void setProjectId(Long projectId) {
        PROJECT_ID = projectId;
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
    private static boolean runDeviceSecurityChecks(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager == null || devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE) {
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

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient;
    }

    public static String getOsBuildNumber() {
        return Build.DISPLAY;
    }

    public static String getBuildName() {
        return Build.MODEL;
    }

    public static void downloadData(Application application) {
        Date currentTime = new Date();
        mCurrentSyncTime = Long.toString(currentTime.getTime());
        new ProjectRepository(application).download();
        new InstrumentRepository(application).download();
        new OptionRepository(application).download();
        new OptionSetRepository(application).download();
        new InstructionRepository(application).download();
        new DisplayRepository(application).download();
        new QuestionRepository(application).download();
        new SectionRepository(application).download();
        new DisplayInstructionRepository(application).download();
        new OptionSetOptionRepository(application).download();
        new ConditionSkipRepository(application).download();
        new DeviceUserRepository(application).download();
        new CriticalResponseRepository(application).download();
        new LoopQuestionRepository(application).download();
        new FollowUpQuestionRepository(application).download();
        new MultipleSkipRepository(application).download();
        new NextQuestionRepository(application).download();
    }

    public static void incrementRemoteDownloadCount() {
        synchronized (AppUtil.class) {
            REMOTE_DOWNLOAD_COUNT++;
        }
    }

    public static void resetRemoteDownloadCount() {
        synchronized (AppUtil.class) {
            REMOTE_DOWNLOAD_COUNT = 0;
        }
    }

    private static int getRemoteDownloadCount() {
        synchronized (AppUtil.class) {
            return REMOTE_DOWNLOAD_COUNT;
        }
    }

    private static int getRemoteTableCount() {
        return REMOTE_TABLE_COUNT;
    }

    public static void setLoopsTask() {
        if (AppUtil.getRemoteDownloadCount() == AppUtil.getRemoteTableCount()) {
            new SetLoopsTask().execute();
        }
    }

    private static String getAccessToken() {
        return ACCESS_TOKEN;
    }

    public static void setAccessToken(String accessToken) {
        ACCESS_TOKEN = accessToken;
    }

    public static int getVersionCode() {
        return VERSION_CODE;
    }

    private static void setVersionCode(Context context) {
        if (context == null) {
            VERSION_CODE = -1;
            return;
        }
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            VERSION_CODE = pInfo.versionCode;
        } catch (NameNotFoundException nnfe) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error finding version code: " + nnfe);
        }
    }

    public static String getParams() {
        return "?access_token=" + getAccessToken() + "&version_code=" + getVersionCode() + "&last_sync_time=" + getLastSyncTime();
    }

    private static String getLastSyncTime() {
        return LAST_SYNC_TIME;
    }

    public static void setLastSyncTime(String lastSyncTime) {
        LAST_SYNC_TIME = lastSyncTime;
        mSettings.setLastSyncTime(LAST_SYNC_TIME);
        mSettingsRepository.update(mSettings);
    }

    public static String getFullApiUrl() {
        return getBaseUrl() + "api/" + getApiVersion() + "/" + "projects/" + getProjectId() + "/";
    }

    private static String getBaseUrl() {
        String apiUrl = getDomainName();
        if (!TextUtils.isEmpty(apiUrl)) {
            char lastChar = apiUrl.charAt(apiUrl.length() - 1);
            if (lastChar != '/') apiUrl = apiUrl + "/";
        }
        return apiUrl;
    }

    public static String getProjectsEndPoint() {
        return getBaseUrl() + "api/" + getApiVersion() + "/projects/";
    }

    private static String getDomainName() {
        return DOMAIN_NAME;
    }

    public static void setDomainName(String domainName) {
        DOMAIN_NAME = domainName;
    }

    private static String getApiVersion() {
        return API_VERSION;
    }

    public static void setApiVersion(String apiVersion) {
        API_VERSION = apiVersion;
    }

    public static Settings getSettings() {
        return mSettings;
    }

    private static void setSettings(final Application application) {
        mSettingsRepository = new SettingsRepository(application);
        GetSettingsTask getSettingsTask = new GetSettingsTask();
        getSettingsTask.setListener(new GetSettingsTask.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(Settings settings) {
                mSettings = settings;
                ACCESS_TOKEN = mSettings.getApiKey();
                LAST_SYNC_TIME = mSettings.getLastSyncTime();
                DOMAIN_NAME = mSettings.getApiUrl();
                API_VERSION = mSettings.getApiVersion();
                if (mSettings.getProjectId() != null)
                    PROJECT_ID = Long.valueOf(mSettings.getProjectId());
                checkDatabaseVersionChange(application);
                setCrashLogs(application);
                setDeviceLanguage();
                if (mSettings.getDeviceUserName() != null) setDeviceUserId(application);
            }
        });
        getSettingsTask.execute(mSettingsRepository.getSettingsDao());
    }

    public static Long getDeviceUserId() {
        return DEVICE_USER_ID;
    }

    private static void setDeviceUserId(Application application) {
        DeviceUserRepository repository = new DeviceUserRepository(application);
        GetDeviceUserTask deviceUserTask = new GetDeviceUserTask(mSettings.getDeviceUserName());
        deviceUserTask.setListener(new GetDeviceUserTask.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(DeviceUser deviceUser) {
                DEVICE_USER_ID = deviceUser.getRemoteId();
            }
        });
        deviceUserTask.execute((DeviceUserDao) repository.getDao());
    }

    static boolean isApiAvailable() {
        int responseCode = ping(getPingAddress());
        return responseCode == 426 || (200 <= responseCode && responseCode < 300);
    }

    static boolean isVersionAcceptable() {
        int responseCode = ping(getPingAddress());
        return responseCode != 426;  // Http Status Code 426 = upgrade required
    }

    private static String getPingAddress() {
        return getProjectsEndPoint();
    }


    private static int ping(String url) {
        if (url == null) return -1;
        url = url + getParams();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (BuildConfig.DEBUG)
                Log.i(TAG, "Received response code " + responseCode + " for api endpoint");
            return responseCode;
        } catch (IOException exception) {
            return -1;
        }
    }

    public static String getCurrentSyncTime() {
        return mCurrentSyncTime;
    }

    public static void resetCurrentSyncTime() {
        mCurrentSyncTime = null;
    }

}
