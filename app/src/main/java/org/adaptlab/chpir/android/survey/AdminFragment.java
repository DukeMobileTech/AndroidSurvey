package org.adaptlab.chpir.android.survey;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.PollService;
import org.adaptlab.chpir.android.survey.models.AdminSettings;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminFragment extends Fragment {
    private final String TAG = "AdminFragment";
    private EditText mDeviceIdentifierEditText;
    private EditText mDeviceLabelEditText;
    private EditText mSyncIntervalEditText;
    private EditText mApiDomainNameEditText;
    private EditText mApiVersionEditText;
    private EditText mProjectIdEditText;
    private EditText mApiKeyEditText;
    private EditText mCustomLocaleEditText;
    private EditText mApi2DomainNameEditText;
    private EditText mApi2VersionEditText;
    private EditText mApi2KeyEditText;
    private CheckBox mUseSecondEndpoint;
    private CheckBox mShowSurveysCheckBox;
    private CheckBox mShowRostersCheckBox;
    private CheckBox mShowSkipCheckBox;
    private CheckBox mShowNACheckBox;
    private CheckBox mShowRFCheckBox;
    private CheckBox mShowDKCheckBox;
    private CheckBox mRequirePasswordCheckBox;
    private CheckBox mRecordSurveyLocationCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_settings, parent, false);
        mDeviceIdentifierEditText = (EditText) v.findViewById(R.id.device_identifier_edit_text);
        mDeviceIdentifierEditText.setText(getAdminSettingsInstanceDeviceId());

        mDeviceLabelEditText = (EditText) v.findViewById(R.id.device_label_edit_text);
        mDeviceLabelEditText.setText(AdminSettings.getInstance().getDeviceLabel());

        mSyncIntervalEditText = (EditText) v.findViewById(R.id.sync_interval_edit_text);
        mSyncIntervalEditText.setText(getAdminSettingsInstanceSyncInterval());

        mApiDomainNameEditText = (EditText) v.findViewById(R.id.api_endpoint_text);
        mApiDomainNameEditText.setText(getAdminSettingsInstanceApiDomainName());

        mApiVersionEditText = (EditText) v.findViewById(R.id.api_version_text);
        mApiVersionEditText.setText(getAdminSettingsInstanceApiVersion());

        mProjectIdEditText = (EditText) v.findViewById(R.id.project_id_text);
        mProjectIdEditText.setText(getAdminSettingsInstanceProjectId());

        mApiKeyEditText = (EditText) v.findViewById(R.id.api_key_text);
        mApiKeyEditText.setText(getAdminSettingsInstanceApiKey());

        mCustomLocaleEditText = (EditText) v.findViewById(R.id.custom_locale_edit_text);
        mCustomLocaleEditText.setText(getAdminSettingsInstanceCustomLocaleCode());

        mUseSecondEndpoint = (CheckBox) v.findViewById(R.id.api2_endpoint);
        mUseSecondEndpoint.setChecked(AdminSettings.getInstance().useEndpoint2());

        mApi2DomainNameEditText = (EditText) v.findViewById(R.id.api2_endpoint_text);
        mApi2DomainNameEditText.setText(getAdminSettingsInstanceApi2DomainName());

        mApi2VersionEditText = (EditText) v.findViewById(R.id.api2_version_text);
        mApi2VersionEditText.setText(getAdminSettingsInstanceApi2Version());

        mApi2KeyEditText = (EditText) v.findViewById(R.id.api2_key_text);
        mApi2KeyEditText.setText(getAdminSettingsInstanceApi2Key());

        mShowSurveysCheckBox = (CheckBox) v.findViewById(R.id.show_surveys_checkbox);
        mShowSurveysCheckBox.setChecked(AdminSettings.getInstance().getShowSurveys());

        mShowRostersCheckBox = (CheckBox) v.findViewById(R.id.show_rosters_checkbox);
        mShowRostersCheckBox.setChecked(AdminSettings.getInstance().getShowRosters());

        mShowSkipCheckBox = (CheckBox) v.findViewById(R.id.show_skip_checkbox);
        mShowSkipCheckBox.setChecked(AdminSettings.getInstance().getShowSkip());

        mShowNACheckBox = (CheckBox) v.findViewById(R.id.show_na_checkbox);
        mShowNACheckBox.setChecked(AdminSettings.getInstance().getShowNA());

        mShowRFCheckBox = (CheckBox) v.findViewById(R.id.show_rf_checkbox);
        mShowRFCheckBox.setChecked(AdminSettings.getInstance().getShowRF());

        mShowDKCheckBox = (CheckBox) v.findViewById(R.id.show_dk_checkbox);
        mShowDKCheckBox.setChecked(AdminSettings.getInstance().getShowDK());

        mRequirePasswordCheckBox = (CheckBox) v.findViewById(R.id.require_password);
        mRequirePasswordCheckBox.setChecked(AdminSettings.getInstance().getRequirePassword());

        mRecordSurveyLocationCheckBox = (CheckBox) v.findViewById(R.id
                .record_survey_location_checkbox);
        mRecordSurveyLocationCheckBox.setChecked(AdminSettings.getInstance()
                .getRecordSurveyLocation());

        TextView mLastUpdateTextView = (TextView) v.findViewById(R.id.last_update_label);
        mLastUpdateTextView.setText(String.format(Locale.getDefault(), "%s%s",
                mLastUpdateTextView.getText().toString(), getLastUpdateTime()));

        TextView mVersionCodeTextView = (TextView) v.findViewById(R.id.version_code_label);
        mVersionCodeTextView.setText(String.format(Locale.getDefault(), "%s%d",
                getString(R.string.version_code), AppUtil.getVersionCode(getActivity())));

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.admin_setting_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_admin_settings_button:
                saveAdminSettings();
                return true;
            case R.id.delete_data_button:
                deleteData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveAdminSettings() {
        AdminSettings.getInstance().setDeviceIdentifier(mDeviceIdentifierEditText.getText()
                .toString());
        AdminSettings.getInstance().setDeviceLabel(mDeviceLabelEditText.getText().toString());
        AdminSettings.getInstance().setSyncInterval(Integer.parseInt(mSyncIntervalEditText
                .getText().toString()));
        AdminSettings.getInstance().setApiDomainName(mApiDomainNameEditText.getText().toString());
        AdminSettings.getInstance().setApiVersion(mApiVersionEditText.getText().toString());
        AdminSettings.getInstance().setProjectId(mProjectIdEditText.getText().toString());
        AdminSettings.getInstance().setApiKey(mApiKeyEditText.getText().toString());
        // If this code is set, it will override the language selection on the device
        // for all instrument translations.
        AdminSettings.getInstance().setCustomLocaleCode(mCustomLocaleEditText.getText().toString());

        PollService.setPollInterval(AdminSettings.getInstance().getSyncInterval());
        ActiveRecordCloudSync.setAccessToken(getAdminSettingsInstanceApiKey());
        ActiveRecordCloudSync.setEndPoint(getAdminSettingsInstanceApiUrl());
        AppUtil.appInit(getActivity());

        AdminSettings.getInstance().setShowSurveys(mShowSurveysCheckBox.isChecked());
        AdminSettings.getInstance().setShowRosters(mShowRostersCheckBox.isChecked());
        AdminSettings.getInstance().setShowSkip(mShowSkipCheckBox.isChecked());
        AdminSettings.getInstance().setShowNA(mShowNACheckBox.isChecked());
        AdminSettings.getInstance().setShowRF(mShowRFCheckBox.isChecked());
        AdminSettings.getInstance().setShowDK(mShowDKCheckBox.isChecked());
        AdminSettings.getInstance().setRequirePassword(mRequirePasswordCheckBox.isChecked());
        AdminSettings.getInstance().setRecordSurveyLocation(mRecordSurveyLocationCheckBox
                .isChecked());

        //Roster settings
        AdminSettings.getInstance().setUseEndpoint2(mUseSecondEndpoint.isChecked());
        AdminSettings.getInstance().setApi2DomainName(mApi2DomainNameEditText.getText().toString());
        AdminSettings.getInstance().setApi2Version(mApi2VersionEditText.getText().toString());
        AdminSettings.getInstance().setApi2Key(mApi2KeyEditText.getText().toString());

        getActivity().finish();
    }

    private void deleteData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_application_data)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        new WipeDataTask().execute();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create().show();
    }

    public String getAdminSettingsInstanceApiUrl() {
        // Append forward slash to domain name if does not exist
        String domainName = AdminSettings.getInstance().getApiDomainName();
        char lastChar = domainName.charAt(domainName.length() - 1);
        if (lastChar != '/') domainName = domainName + "/";

        return domainName + "api/" + AdminSettings.getInstance().getApiVersion() + "/" +
                "projects/" + AdminSettings.getInstance().getProjectId() + "/";
    }

    public String getAdminSettingsInstanceDeviceId() {
        return AdminSettings.getInstance().getDeviceIdentifier();
    }

    public String getAdminSettingsInstanceSyncInterval() {
        return String.valueOf(AdminSettings.getInstance().getSyncIntervalInMinutes());
    }

    public String getAdminSettingsInstanceApiDomainName() {
        return AdminSettings.getInstance().getApiDomainName();
    }

    public String getAdminSettingsInstanceApiVersion() {
        return AdminSettings.getInstance().getApiVersion();
    }

    public String getAdminSettingsInstanceProjectId() {
        return AdminSettings.getInstance().getProjectId();
    }

    public String getAdminSettingsInstanceApiKey() {
        return AdminSettings.getInstance().getApiKey();
    }

    public String getAdminSettingsInstanceCustomLocaleCode() {
        return AdminSettings.getInstance().getCustomLocaleCode();
    }

    public String getAdminSettingsInstanceApi2DomainName() {
        return AdminSettings.getInstance().getApi2DomainName();
    }

    public String getAdminSettingsInstanceApi2Version() {
        return AdminSettings.getInstance().getApi2Version();
    }

    public String getAdminSettingsInstanceApi2Key() {
        return AdminSettings.getInstance().getApi2Key();
    }

    public String getLastUpdateTime() {
        String last = AdminSettings.getInstance().getLastSyncTime();
        if (last.isEmpty()) return last;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(last));
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(calendar.getTime());
    }

    private class WipeDataTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... params) {
            AppUtil.deleteApplicationData();
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(
                    getActivity(),
                    getString(R.string.wiping_data_header),
                    getString(R.string.background_process_progress_message)
            );
        }

        @Override
        protected void onPostExecute(Void params) {
            progressDialog.dismiss();
            getActivity().finish();
        }
    }
}