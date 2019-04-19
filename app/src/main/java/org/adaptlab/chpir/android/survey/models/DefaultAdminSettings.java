package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.R;

@Table(name = "DefaultAdminSettings")
public class DefaultAdminSettings extends AdminSettings {

    public DefaultAdminSettings() {
        super();
    }

    public static DefaultAdminSettings getInstance() {
        DefaultAdminSettings adminSettings = new Select().from(DefaultAdminSettings.class)
                .orderBy("Id asc").executeSingle();
        if (adminSettings == null) {
            adminSettings = new DefaultAdminSettings();
            adminSettings.save();
        }
        return adminSettings;
    }

    @Override
    public String getCustomLocaleCode() {
        return SurveyApp.getInstance().getResources().getString(R.string.default_custom_locale);
    }

    @Override
    public boolean getShowSurveys() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_show_surveys);
    }

    @Override
    public String getApiKey() {
        return SurveyApp.getInstance().getResources().getString(R.string.default_backend_api_key);
    }

    @Override
    public boolean getRequirePassword() {
        return false;
    }

    @Override
    public boolean getRecordSurveyLocation() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_record_survey_location);
    }

    @Override
    public String getApiUrl() {
        return getApiDomainName() + "api/" + getApiVersion() + "/" + "projects/" + getProjectId()
                + "/";
    }

    @Override
    public String getApiDomainName() {
        return SurveyApp.getInstance().getResources().getString(R.string.default_api_domain_name);
    }

    @Override
    public String getApiVersion() {
        return SurveyApp.getInstance().getResources().getString(R.string.default_api_version);
    }

    @Override
    public String getProjectId() {
        return SurveyApp.getInstance().getResources().getString(R.string.default_project_id);
    }

    @Override
    public boolean getShowDK() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_show_dk);
    }

    @Override
    public boolean getShowNA() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_show_na);
    }

    @Override
    public boolean getShowRF() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_show_rf);
    }

    @Override
    public boolean getShowSkip() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_show_sk);
    }

    @Override
    public boolean getShowRosters() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_show_rosters);
    }

    @Override
    public boolean getShowScores() {
        return SurveyApp.getInstance().getResources().getBoolean(R.bool.default_show_scores);
    }

}