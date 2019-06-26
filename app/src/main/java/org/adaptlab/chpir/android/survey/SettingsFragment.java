package org.adaptlab.chpir.android.survey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.tasks.ApkUpdateTask;
import org.adaptlab.chpir.android.survey.tasks.RemoteAuthenticationTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    private EditText mDeviceIdentifierEditText;
    private EditText mDeviceLabelEditText;
    private EditText mApiDomainNameEditText;
    private EditText mApiVersionEditText;
    private EditText mProjectIdEditText;
    private EditText mApiKeyEditText;
    private EditText mCustomLocaleEditText;
    private CheckBox mShowSurveysCheckBox;
    private CheckBox mRequirePasswordCheckBox;
    private CheckBox mRecordSurveyLocationCheckBox;
    private TextView mLastUpdateTextView;
    private ArrayList<EditText> mRequiredFields;
    private AlertDialog mDialog;
    private Spinner mSpinner;
    private Settings mSettings;
    private SettingsViewModel mSettingsViewModel;
    private List<String> mLanguages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() == null) return;
        getActivity().setTitle(R.string.admin_settings);
        mSettingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
        mSettingsViewModel.getSettings().observe(this, new Observer<Settings>() {
            @Override
            public void onChanged(@Nullable Settings settings) {
                mSettings = settings;
                mApiDomainNameEditText.setText(mSettings.getApiUrl());
                mApiVersionEditText.setText(mSettings.getApiVersion());
                mProjectIdEditText.setText(mSettings.getProjectId());
                mApiKeyEditText.setText(mSettings.getApiKey());
                mShowSurveysCheckBox.setChecked(mSettings.isShowSurveys());
                mRequirePasswordCheckBox.setChecked(mSettings.isRequirePassword());
                mRecordSurveyLocationCheckBox.setChecked(mSettings.isRecordSurveyLocation());
                mDeviceIdentifierEditText.setText(mSettings.getDeviceIdentifier());
                mDeviceIdentifierEditText.setSelection(mDeviceIdentifierEditText.getText().length());
                mDeviceLabelEditText.setText(mSettings.getDeviceLabel());
                mCustomLocaleEditText.setText(mSettings.getCustomLocaleCode());
                mLastUpdateTextView.setText(String.format(Locale.getDefault(), "%s%s%s",
                        getString(R.string.last_update), " ", getLastUpdateTime()));
                if (mLanguages != null) setSpinnerAdapter();
            }
        });

        mSettingsViewModel.getLanguages().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> languages) {
                mLanguages = languages;
                if (mSettings != null) setSpinnerAdapter();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, parent, false);

        mApiDomainNameEditText = v.findViewById(R.id.api_endpoint_text);
        mApiVersionEditText = v.findViewById(R.id.api_version_text);
        mProjectIdEditText = v.findViewById(R.id.project_id_text);
        mApiKeyEditText = v.findViewById(R.id.api_key_text);
        mRequiredFields = new ArrayList<>(Arrays.asList(mApiDomainNameEditText, mApiVersionEditText, mProjectIdEditText, mApiKeyEditText));
        mShowSurveysCheckBox = v.findViewById(R.id.show_surveys_checkbox);
        mRequirePasswordCheckBox = v.findViewById(R.id.require_password);
        mRecordSurveyLocationCheckBox = v.findViewById(R.id.record_survey_location_checkbox);
        mDeviceIdentifierEditText = v.findViewById(R.id.device_identifier_edit_text);
        mDeviceLabelEditText = v.findViewById(R.id.device_label_edit_text);
        mCustomLocaleEditText = v.findViewById(R.id.custom_locale_edit_text);
        mSpinner = v.findViewById(R.id.device_language_spinner);

        // Disable edits if using default settings
        if (getResources().getBoolean(R.bool.default_admin_settings)) {
            mDeviceIdentifierEditText.setEnabled(false);
            mDeviceLabelEditText.setEnabled(false);
            mCustomLocaleEditText.setEnabled(false);
            mRecordSurveyLocationCheckBox.setEnabled(false);
            mRequirePasswordCheckBox.setEnabled(false);
            mShowSurveysCheckBox.setEnabled(false);
        }

        mLastUpdateTextView = v.findViewById(R.id.last_update_label);
//        mLastUpdateTextView.setText(String.format(Locale.getDefault(), "%s%s%s",
//                getString(R.string.last_update), " ", getLastUpdateTime()));

        Button resetLastSyncTime = v.findViewById(R.id.reset_last_sync_time_button);
        resetLastSyncTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettings.setLastSyncTime(null);
                mLastUpdateTextView.setText(String.format(Locale.getDefault(), "%s%s%s",
                        getString(R.string.last_update), " ", getLastUpdateTime()));
            }
        });

        TextView versionCodeTextView = v.findViewById(R.id.version_code_label);
        versionCodeTextView.setText(String.format(Locale.getDefault(), "%s%s%d",
                getString(R.string.version_code), " ", AppUtil.getVersionCode(getActivity())));

        TextView versionNameTextView = v.findViewById(R.id.version_name_label);
        versionNameTextView.setText(String.format(Locale.getDefault(), "%s%s%s",
                getString(R.string.version_name), " ", AppUtil.getVersionName()));

        Button updatesCheck = v.findViewById(R.id.updates_check_button);
        updatesCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new ApkUpdateTask(getActivity()).execute();
            }
        });

        Button settingsDownload = v.findViewById(R.id.fetch_endpoint_settings);
        settingsDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfigurationsDialog();
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_setting, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getResources().getBoolean(R.bool.default_admin_settings)) {
            menu.findItem(R.id.save_settings_button).setEnabled(false).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_settings_button) {
            saveSettings();
            finishActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSpinnerAdapter() {
        if (getActivity() == null) return;
        final List<String> languageCodes = new ArrayList<>();
        languageCodes.add(getString(R.string.english_iso_code));
        languageCodes.addAll(mLanguages);
        ArrayList<String> displayLanguages = new ArrayList<>();
        for (String languageCode : languageCodes) {
            displayLanguages.add(new Locale(languageCode).getDisplayLanguage());
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, displayLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != languageCodes.indexOf(mSettings.getLanguage())) {
                    mSettings.setLanguage(languageCodes.get(position));
                    LocaleManager.setNewLocale(getActivity(), languageCodes.get(position));
                    mSettingsViewModel.updateSettings(mSettings);
                    getActivity().recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinner.setSelection(languageCodes.indexOf(mSettings.getLanguage()));
    }

    private String getLastUpdateTime() {
        String last = mSettings.getLastSyncTime();
        if (TextUtils.isEmpty(last)) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(last));
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        return dateFormat.format(calendar.getTime());
    }

    private void showConfigurationsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setTitle(R.string.api_endpoint_settings)
                    .setView(R.layout.fragment_api_settings)
                    .setPositiveButton(R.string.upper_case_OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();

            final EditText endpointEditText = dialog.findViewById(R.id.apiEndpointEditText);
            final EditText versionEditText = dialog.findViewById(R.id.apiVersionEditText);
            final EditText projectEditText = dialog.findViewById(R.id.projectIdEditText);
            final CheckBox surveysCheckBox = dialog.findViewById(R.id.showSurveys);
            final CheckBox recordSurveyLocation = dialog.findViewById(R.id.recordLocation);

            if (TextUtils.isEmpty(mSettings.getApiUrl())) {
                endpointEditText.setText(getString(R.string.default_api_domain_name));
            } else {
                endpointEditText.setText(mSettings.getApiUrl());
            }
            if (TextUtils.isEmpty(mSettings.getApiVersion())) {
                versionEditText.setText(getString(R.string.default_api_version));
            } else {
                versionEditText.setText(mSettings.getApiVersion());
            }
            if (TextUtils.isEmpty(mSettings.getProjectId())) {
                projectEditText.setText(getString(R.string.default_project_id));
            } else {
                projectEditText.setText(mSettings.getProjectId());
            }
            surveysCheckBox.setChecked(true);
            recordSurveyLocation.setChecked(true);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String endpoint = endpointEditText.getText().toString();
                    String version = versionEditText.getText().toString();
                    String project = projectEditText.getText().toString();
                    if (URLUtil.isValidUrl(endpoint)) {
                        mSettings.setApiUrl(endpoint);
                        AppUtil.setDomainName(endpoint);
                        mSettings.setApiVersion(version);
                        AppUtil.setApiVersion(version);
                        mSettings.setProjectId(project);
                        AppUtil.setProjectId(Long.valueOf(project));
                        mSettings.setShowSurveys(surveysCheckBox.isChecked());
                        mSettings.setRecordSurveyLocation(recordSurveyLocation.isChecked());
                        mSettingsViewModel.updateSettings(mSettings);
                        mApiDomainNameEditText.setText(endpoint);
                        mApiVersionEditText.setText(version);
                        mProjectIdEditText.setText(project);
                        mShowSurveysCheckBox.setChecked(surveysCheckBox.isChecked());
                        mRecordSurveyLocationCheckBox.setChecked(recordSurveyLocation.isChecked());
                        dialog.dismiss();
                        deviceUserLogin(mSettings.getFullApiUrl());
                    } else {
                        endpointEditText.setError(getString(R.string.invalid_url));
                    }
                }
            });
        }
    }

    private void deviceUserLogin(final String endpoint) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setTitle(R.string.device_user_login)
                    .setView(R.layout.fragment_login);
            mDialog = builder.create();
            mDialog.show();

            final EditText username = mDialog.findViewById(R.id.login_username_edit_text);
            final EditText password = mDialog.findViewById(R.id.login_password_edit_text);
            final Button login = mDialog.findViewById(R.id.login_button);
            login.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    RemoteAuthenticationTask task = new RemoteAuthenticationTask();
                    task.setListener(new RemoteAuthenticationTask.AsyncTaskListener() {
                        @Override
                        public void onAsyncTaskFinished(String param) {
                            if (param == null || param.equals(HttpURLConnection.HTTP_UNAUTHORIZED + "")) {
                                Toast.makeText(getActivity(), R.string.invalid_user_credentials, Toast.LENGTH_LONG).show();
                            } else {
                                mSettings.setApiKey(param);
                                mSettings.setDeviceUserName(username.getText().toString());
                                AppUtil.setAccessToken(param);
                                mApiKeyEditText.setText(param);
                                if (mDialog != null) mDialog.dismiss();
                                saveSettings();
                                finishActivity();
                            }
                        }
                    });
                    task.execute(endpoint, username.getText().toString(),
                            password.getText().toString());
                }
            });
        }
    }

    private void saveSettings() {
        if (!requiredFieldIsEmpty()) {
            mSettings.setDeviceIdentifier(mDeviceIdentifierEditText.getText().toString());
            mSettings.setDeviceLabel(mDeviceLabelEditText.getText().toString());
            mSettings.setApiUrl(mApiDomainNameEditText.getText().toString());
            mSettings.setApiVersion(mApiVersionEditText.getText().toString());
            mSettings.setProjectId(mProjectIdEditText.getText().toString());
            mSettings.setApiKey(mApiKeyEditText.getText().toString());
            // If this code is set, it will override the language selection on the device for all instrument translations.
            mSettings.setCustomLocaleCode(mCustomLocaleEditText.getText().toString());
            mSettings.setShowSurveys(mShowSurveysCheckBox.isChecked());
            mSettings.setRequirePassword(mRequirePasswordCheckBox.isChecked());
            mSettings.setRecordSurveyLocation(mRecordSurveyLocationCheckBox.isChecked());

            mSettingsViewModel.updateSettings(mSettings);
        }
    }

    private void finishActivity() {
        if (getActivity() == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().finishAfterTransition();
        } else {
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

}
