package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.AppUtil;
import org.adaptlab.chpir.android.survey.R;

@Table(name = "DefaultAdminSettings")
public class DefaultAdminSettings extends AdminSettings {
    private static DefaultAdminSettings adminSettings;

    public DefaultAdminSettings() {
        super();
    }

    public static DefaultAdminSettings getInstance() {
        adminSettings = new Select().from(DefaultAdminSettings.class).orderBy("Id asc").executeSingle();
        if (adminSettings == null) {
            adminSettings = new DefaultAdminSettings();
            adminSettings.save();
        }
        return adminSettings;
    }

    @Override
    public boolean getShowSurveys() {
        return AppUtil.getContext().getResources().getBoolean(R.bool.default_show_surveys);
    }

    @Override
    public boolean getShowSkip() {
        return AppUtil.getContext().getResources().getBoolean(R.bool.default_show_sk);
    }

    @Override
    public boolean getRequirePassword() {
        return false;
    }

    @Override
    public int getSyncInterval() {
        return getSyncIntervalInMinutes() * 1000 * 60;
    }

    @Override
    public int getSyncIntervalInMinutes() {
        return Integer.parseInt(AppUtil.getContext().getResources().getString(R.string.default_sync_interval_minutes));
    }

    @Override
    public String getApiDomainName() {
        return AppUtil.getContext().getResources().getString(R.string.default_api_domain_name);
    }

    @Override
    public String getApiVersion() {
        return AppUtil.getContext().getResources().getString(R.string.default_api_version);
    }

    @Override
    public String getProjectId() {
        return AppUtil.getContext().getResources().getString(R.string.default_project_id);
    }

    @Override
    public boolean getShowDK() {
        return AppUtil.getContext().getResources().getBoolean(R.bool.default_show_dk);
    }

    @Override
    public boolean getShowNA() {
        return AppUtil.getContext().getResources().getBoolean(R.bool.default_show_na);
    }

    @Override
    public boolean getShowRF() {
        return AppUtil.getContext().getResources().getBoolean(R.bool.default_show_rf);
    }

    @Override
    public String getApiKey() {
        return AppUtil.getContext().getResources().getString(R.string.default_backend_api_key);
    }

    @Override
    public String getApiUrl() {
        return getApiDomainName() + "api/" + getApiVersion() + "/" + "projects/" + getProjectId() + "/";
    }

    @Override
    public String getCustomLocaleCode() {
        String customLocaleCode = AppUtil.getContext().getResources().getString(R.string.default_custom_locale);
        if (customLocaleCode == null) {
            customLocaleCode = "";
        }
        return customLocaleCode;
    }

    @Override
    public boolean getRecordSurveyLocation() {
        return AppUtil.getContext().getResources().getBoolean(R.bool.default_record_survey_location);
    }

}