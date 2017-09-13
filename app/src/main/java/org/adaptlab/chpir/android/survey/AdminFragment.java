package org.adaptlab.chpir.android.survey;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.survey.tasks.ApkUpdateTask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class AdminFragment extends Fragment {
    private final String TAG = "AdminFragment";
    private EditText mDeviceIdentifierEditText;
    private EditText mDeviceLabelEditText;
    private EditText mApiDomainNameEditText;
    private EditText mApiVersionEditText;
    private EditText mProjectIdEditText;
    private EditText mApiKeyEditText;
    private EditText mCustomLocaleEditText;
    private EditText mApi2DomainNameEditText;
    private EditText mApi2VersionEditText;
    private EditText mApi2KeyEditText;
    private TextView mApi2DomainNameLabel;
    private TextView mApi2VersionLabel;
    private TextView mApi2KeyLabel;
    private TextView mRosterSyncSettingsLabel;
    private CheckBox mRosterEndPointCheckBox;
    private CheckBox mShowSurveysCheckBox;
    private CheckBox mShowRostersCheckBox;
    private CheckBox mShowScoresCheckBox;
    private CheckBox mShowSkipCheckBox;
    private CheckBox mShowNACheckBox;
    private CheckBox mShowRFCheckBox;
    private CheckBox mShowDKCheckBox;
    private CheckBox mRequirePasswordCheckBox;
    private CheckBox mRecordSurveyLocationCheckBox;
    private ArrayList<EditText> mTransformableFields;
    private ArrayList<EditText> mRequiredFields;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.admin_settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_settings, parent, false);
        mDeviceIdentifierEditText = (EditText) v.findViewById(R.id.device_identifier_edit_text);
        mDeviceIdentifierEditText.setText(getAdminSettingsInstanceDeviceId());

        mDeviceLabelEditText = (EditText) v.findViewById(R.id.device_label_edit_text);
        mDeviceLabelEditText.setText(AppUtil.getAdminSettingsInstance().getDeviceLabel());

        mCustomLocaleEditText = (EditText) v.findViewById(R.id.custom_locale_edit_text);
        mCustomLocaleEditText.setText(getAdminSettingsInstanceCustomLocaleCode());

        mApiDomainNameEditText = (EditText) v.findViewById(R.id.api_endpoint_text);
        mApiDomainNameEditText.setText(getAdminSettingsInstanceApiDomainName());
        if (!TextUtils.isEmpty(getAdminSettingsInstanceApiDomainName())) setOnClickListener(mApiDomainNameEditText);
        mApiVersionEditText = (EditText) v.findViewById(R.id.api_version_text);
        mApiVersionEditText.setText(getAdminSettingsInstanceApiVersion());
        if (!TextUtils.isEmpty(getAdminSettingsInstanceApiVersion())) setOnClickListener(mApiVersionEditText);
        mProjectIdEditText = (EditText) v.findViewById(R.id.project_id_text);
        mProjectIdEditText.setText(getAdminSettingsInstanceProjectId());
        if (!TextUtils.isEmpty(getAdminSettingsInstanceProjectId())) setOnClickListener(mProjectIdEditText);
        mApiKeyEditText = (EditText) v.findViewById(R.id.api_key_text);
        mApiKeyEditText.setText(getAdminSettingsInstanceApiKey());
        if (!TextUtils.isEmpty(getAdminSettingsInstanceApiKey())) setOnClickListener(mApiKeyEditText);
        mRequiredFields = new ArrayList<>(Arrays.asList(mApiDomainNameEditText, mApiVersionEditText, mProjectIdEditText, mApiKeyEditText));

        mRosterEndPointCheckBox = (CheckBox) v.findViewById(R.id.api2_endpoint);
        mRosterEndPointCheckBox.setChecked(AppUtil.getAdminSettingsInstance().useEndpoint2());
        mRosterEndPointCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(isChecked);
                toggleRosterSettingsVisibility(isChecked);
            }
        });

        // TODO: 6/29/17 Add project id to end point
        mRosterSyncSettingsLabel = (TextView) v.findViewById(R.id.roster_sync_settings_label);
        mApi2DomainNameLabel = (TextView) v.findViewById(R.id.api2_endpoint_label);
        mApi2VersionLabel = (TextView) v.findViewById(R.id.api2_version_label);
        mApi2KeyLabel = (TextView) v.findViewById(R.id.api2_key_label);
        mApi2DomainNameEditText = (EditText) v.findViewById(R.id.api2_endpoint_text);
        mApi2DomainNameEditText.setText(getAdminSettingsInstanceApi2DomainName());
        if (!TextUtils.isEmpty(getAdminSettingsInstanceApi2DomainName())) setOnClickListener(mApi2DomainNameEditText);
        mApi2VersionEditText = (EditText) v.findViewById(R.id.api2_version_text);
        mApi2VersionEditText.setText(getAdminSettingsInstanceApi2Version());
        if (!TextUtils.isEmpty(getAdminSettingsInstanceApi2Version())) setOnClickListener(mApi2VersionEditText);
        mApi2KeyEditText = (EditText) v.findViewById(R.id.api2_key_text);
        mApi2KeyEditText.setText(getAdminSettingsInstanceApi2Key());
        if (!TextUtils.isEmpty(getAdminSettingsInstanceApi2Key())) setOnClickListener(mApi2KeyEditText);
        toggleRosterSettingsVisibility(AppUtil.getAdminSettingsInstance().useEndpoint2());
        mTransformableFields = new ArrayList<>(Arrays.asList(mApiDomainNameEditText, mApiVersionEditText, mProjectIdEditText, mApiKeyEditText, mApi2VersionEditText, mApi2KeyEditText, mApi2DomainNameEditText));

        mShowSurveysCheckBox = (CheckBox) v.findViewById(R.id.show_surveys_checkbox);
        mShowSurveysCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowSurveys());

        mShowRostersCheckBox = (CheckBox) v.findViewById(R.id.show_rosters_checkbox);
        mShowRostersCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowRosters());

        mShowScoresCheckBox = (CheckBox) v.findViewById(R.id.show_scores_checkbox);
        mShowScoresCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowScores());

        mShowSkipCheckBox = (CheckBox) v.findViewById(R.id.show_skip_checkbox);
        mShowSkipCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowSkip());

        mShowNACheckBox = (CheckBox) v.findViewById(R.id.show_na_checkbox);
        mShowNACheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowNA());

        mShowRFCheckBox = (CheckBox) v.findViewById(R.id.show_rf_checkbox);
        mShowRFCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowRF());

        mShowDKCheckBox = (CheckBox) v.findViewById(R.id.show_dk_checkbox);
        mShowDKCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowDK());

        mRequirePasswordCheckBox = (CheckBox) v.findViewById(R.id.require_password);
        mRequirePasswordCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getRequirePassword());

        mRecordSurveyLocationCheckBox = (CheckBox) v.findViewById(R.id
                .record_survey_location_checkbox);
        mRecordSurveyLocationCheckBox.setChecked(AppUtil.getAdminSettingsInstance()
                .getRecordSurveyLocation());

        // Disable edits if using default settings
        if (getActivity().getResources().getBoolean(R.bool.default_admin_settings)) {
            mDeviceIdentifierEditText.setEnabled(false);
            mDeviceLabelEditText.setEnabled(false);
            mCustomLocaleEditText.setEnabled(false);
            mRecordSurveyLocationCheckBox.setEnabled(false);
            mRequirePasswordCheckBox.setEnabled(false);
            mShowDKCheckBox.setEnabled(false);
            mShowRFCheckBox.setEnabled(false);
            mShowNACheckBox.setEnabled(false);
            mShowSkipCheckBox.setEnabled(false);
            mShowScoresCheckBox.setEnabled(false);
            mShowRostersCheckBox.setEnabled(false);
            mShowSurveysCheckBox.setEnabled(false);
            mRosterEndPointCheckBox.setEnabled(false);
            for (EditText editText : mTransformableFields) {
                editText.setEnabled(false);
            }
        }

        final TextView lastUpdateTextView = (TextView) v.findViewById(R.id.last_update_label);
        lastUpdateTextView.setText(String.format(Locale.getDefault(), "%s%s%s",
                getString(R.string.last_update), " ", getLastUpdateTime()));

        Button resetLastSyncTime = (Button) v.findViewById(R.id.reset_last_sync_time_button);
        resetLastSyncTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtil.getAdminSettingsInstance().setLastSyncTime(null);
                lastUpdateTextView.setText(String.format(Locale.getDefault(), "%s%s%s",
                        getString(R.string.last_update), " ", getLastUpdateTime()));
            }
        });

        TextView versionCodeTextView = (TextView) v.findViewById(R.id.version_code_label);
        versionCodeTextView.setText(String.format(Locale.getDefault(), "%s%s%d",
                getString(R.string.version_code), " ", AppUtil.getVersionCode(getActivity())));

        TextView versionNameTextView = (TextView) v.findViewById(R.id.version_name_label);
        versionNameTextView.setText(String.format(Locale.getDefault(), "%s%s%s",
                getString(R.string.version_name), " ", AppUtil.getVersionName(getActivity())));

        Button updatesCheck = (Button) v.findViewById(R.id.updates_check_button);
        updatesCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new ApkUpdateTask(getActivity()).execute();
            }
        });

        return v;
    }

    private void toggleRosterSettingsVisibility(boolean isChecked) {
        if (isChecked) {
            mRosterSyncSettingsLabel.setVisibility(View.VISIBLE);
            mApi2DomainNameLabel.setVisibility(View.VISIBLE);
            mApi2VersionLabel.setVisibility(View.VISIBLE);
            mApi2KeyLabel.setVisibility(View.VISIBLE);
            mApi2DomainNameEditText.setVisibility(View.VISIBLE);
            mApi2VersionEditText.setVisibility(View.VISIBLE);
            mApi2KeyEditText.setVisibility(View.VISIBLE);
        } else {
            mRosterSyncSettingsLabel.setVisibility(View.GONE);
            mApi2DomainNameEditText.setVisibility(View.GONE);
            mApi2VersionEditText.setVisibility(View.GONE);
            mApi2KeyEditText.setVisibility(View.GONE);
            mApi2DomainNameLabel.setVisibility(View.GONE);
            mApi2VersionLabel.setVisibility(View.GONE);
            mApi2KeyLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.admin_setting_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getActivity().getResources().getBoolean(R.bool.default_admin_settings)) {
            menu.findItem(R.id.save_admin_settings_button).setEnabled(false).setVisible(false);
            menu.findItem(R.id.delete_data_button).setEnabled(false).setVisible(false);
        }
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
        if (!requiredFieldIsEmpty()) {
            AppUtil.getAdminSettingsInstance().setDeviceIdentifier(mDeviceIdentifierEditText.getText().toString());

            AppUtil.getAdminSettingsInstance().setDeviceLabel(mDeviceLabelEditText.getText().toString());
            AppUtil.getAdminSettingsInstance().setApiDomainName(mApiDomainNameEditText.getText().toString());
            AppUtil.getAdminSettingsInstance().setApiVersion(mApiVersionEditText.getText().toString());
            AppUtil.getAdminSettingsInstance().setProjectId(mProjectIdEditText.getText().toString
                    ());
            AppUtil.getAdminSettingsInstance().setApiKey(mApiKeyEditText.getText().toString());
            // If this code is set, it will override the language selection on the device
            // for all instrument translations.
            AppUtil.getAdminSettingsInstance().setCustomLocaleCode(mCustomLocaleEditText.getText().toString());

            ActiveRecordCloudSync.setAccessToken(getAdminSettingsInstanceApiKey());
            ActiveRecordCloudSync.setEndPoint(getAdminSettingsInstanceApiUrl());
            AppUtil.appInit(getActivity());

            AppUtil.getAdminSettingsInstance().setShowSurveys(mShowSurveysCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setShowRosters(mShowRostersCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setShowScores(mShowScoresCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setShowSkip(mShowSkipCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setShowNA(mShowNACheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setShowRF(mShowRFCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setShowDK(mShowDKCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setRequirePassword(mRequirePasswordCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setRecordSurveyLocation(mRecordSurveyLocationCheckBox
                    .isChecked());

            //Roster settings
            AppUtil.getAdminSettingsInstance().setUseEndpoint2(mRosterEndPointCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setApi2DomainName(mApi2DomainNameEditText.getText().toString());
            AppUtil.getAdminSettingsInstance().setApi2Version(mApi2VersionEditText.getText().toString());
            AppUtil.getAdminSettingsInstance().setApi2Key(mApi2KeyEditText.getText().toString());

            getActivity().finish();
        }
    }

    private boolean requiredFieldIsEmpty() {
        for (EditText editText : mRequiredFields) {
            if (TextUtils.isEmpty(editText.getText())) {
                editText.setError(getString(R.string.required_field));
                return true;
            }
        }
        return false;
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
        String domainName = AppUtil.getAdminSettingsInstance().getApiDomainName();
        char lastChar = domainName.charAt(domainName.length() - 1);
        if (lastChar != '/') domainName = domainName + "/";

        return domainName + "api/" + AppUtil.getAdminSettingsInstance().getApiVersion() + "/" +
                "projects/" + AppUtil.getAdminSettingsInstance().getProjectId() + "/";
    }

    public String getAdminSettingsInstanceDeviceId() {
        return AppUtil.getAdminSettingsInstance().getDeviceIdentifier();
    }

    public String getAdminSettingsInstanceApiDomainName() {
        return AppUtil.getAdminSettingsInstance().getApiDomainName();
    }

    public String getAdminSettingsInstanceApiVersion() {
        return AppUtil.getAdminSettingsInstance().getApiVersion();
    }

    public String getAdminSettingsInstanceProjectId() {
        return AppUtil.getAdminSettingsInstance().getProjectId();
    }

    public String getAdminSettingsInstanceApiKey() {
        return AppUtil.getAdminSettingsInstance().getApiKey();
    }

    public String getAdminSettingsInstanceCustomLocaleCode() {
        return AppUtil.getAdminSettingsInstance().getCustomLocaleCode();
    }

    public String getAdminSettingsInstanceApi2DomainName() {
        return AppUtil.getAdminSettingsInstance().getApi2DomainName();
    }

    public String getAdminSettingsInstanceApi2Version() {
        return AppUtil.getAdminSettingsInstance().getApi2Version();
    }

    public String getAdminSettingsInstanceApi2Key() {
        return AppUtil.getAdminSettingsInstance().getApi2Key();
    }

    public String getLastUpdateTime() {
        String last = AppUtil.getAdminSettingsInstance().getLastSyncTime();
        if (last.isEmpty()) return last;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(last));
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(calendar.getTime());
    }

    private void setOnClickListener(final EditText editText) {
        editText.setTransformationMethod(new PasswordTransformationMethod());
        editText.setFocusable(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPasswordPrompt();
            }
        });
    }

    private void displayPasswordPrompt() {
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.password_title)
                .setMessage(R.string.password_message)
                .setView(input)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        if (AppUtil.checkAdminPassword(input.getText().toString())) {
                            for (EditText editText : mTransformableFields) {
                                editText.setTransformationMethod(null);
                                editText.setFocusableInTouchMode(true);
                                editText.setClickable(false);
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.incorrect_password, Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
            }
        }).show();
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
