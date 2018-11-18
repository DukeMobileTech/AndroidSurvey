package org.adaptlab.chpir.android.survey;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
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

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.tasks.ApkUpdateTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;
import org.apache.commons.codec.CharEncoding;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminFragment extends Fragment {
    private final String TAG = "AdminFragment";
    private final String CHANNEL = "RESTORATION_CHANNEL";
    private final int RESTORATION_ID = 12345;
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
    private ArrayList<EditText> mRequiredFields;
    private AlertDialog mDialog;
    private Spinner mSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.admin_settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_settings, parent, false);

        mApiDomainNameEditText = (EditText) v.findViewById(R.id.api_endpoint_text);
        mApiDomainNameEditText.setText(getAdminSettingsInstanceApiDomainName());
        mApiVersionEditText = (EditText) v.findViewById(R.id.api_version_text);
        mApiVersionEditText.setText(getAdminSettingsInstanceApiVersion());
        mProjectIdEditText = (EditText) v.findViewById(R.id.project_id_text);
        mProjectIdEditText.setText(getAdminSettingsInstanceProjectId());
        mApiKeyEditText = (EditText) v.findViewById(R.id.api_key_text);
        mApiKeyEditText.setText(getAdminSettingsInstanceApiKey());
        mRequiredFields = new ArrayList<>(Arrays.asList(mApiDomainNameEditText, mApiVersionEditText, mProjectIdEditText, mApiKeyEditText));

//        mRosterEndPointCheckBox = (CheckBox) v.findViewById(R.id.api2_endpoint);
//        mRosterEndPointCheckBox.setChecked(AppUtil.getAdminSettingsInstance().useEndpoint2());
//        mRosterEndPointCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                buttonView.setChecked(isChecked);
//                toggleRosterSettingsVisibility(isChecked);
//            }
//        });

        // TODO: 6/29/17 Add project id to end point
//        mRosterSyncSettingsLabel = (TextView) v.findViewById(R.id.roster_sync_settings_label);
//        mApi2DomainNameLabel = (TextView) v.findViewById(R.id.api2_endpoint_label);
//        mApi2VersionLabel = (TextView) v.findViewById(R.id.api2_version_label);
//        mApi2KeyLabel = (TextView) v.findViewById(R.id.api2_key_label);
//        mApi2DomainNameEditText = (EditText) v.findViewById(R.id.api2_endpoint_text);
//        mApi2DomainNameEditText.setText(getAdminSettingsInstanceApi2DomainName());
//        mApi2VersionEditText = (EditText) v.findViewById(R.id.api2_version_text);
//        mApi2VersionEditText.setText(getAdminSettingsInstanceApi2Version());
//        mApi2KeyEditText = (EditText) v.findViewById(R.id.api2_key_text);
//        mApi2KeyEditText.setText(getAdminSettingsInstanceApi2Key());
//        toggleRosterSettingsVisibility(AppUtil.getAdminSettingsInstance().useEndpoint2());

        mShowSurveysCheckBox = (CheckBox) v.findViewById(R.id.show_surveys_checkbox);
        mShowSurveysCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowSurveys());

//        mShowRostersCheckBox = (CheckBox) v.findViewById(R.id.show_rosters_checkbox);
//        mShowRostersCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowRosters());

//        mShowScoresCheckBox = (CheckBox) v.findViewById(R.id.show_scores_checkbox);
//        mShowScoresCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowScores());

//        mShowSkipCheckBox = (CheckBox) v.findViewById(R.id.show_skip_checkbox);
//        mShowSkipCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowSkip());
//
//        mShowNACheckBox = (CheckBox) v.findViewById(R.id.show_na_checkbox);
//        mShowNACheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowNA());
//
//        mShowRFCheckBox = (CheckBox) v.findViewById(R.id.show_rf_checkbox);
//        mShowRFCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowRF());
//
//        mShowDKCheckBox = (CheckBox) v.findViewById(R.id.show_dk_checkbox);
//        mShowDKCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getShowDK());

        mRequirePasswordCheckBox = (CheckBox) v.findViewById(R.id.require_password);
        mRequirePasswordCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getRequirePassword());

        mRecordSurveyLocationCheckBox = (CheckBox) v.findViewById(R.id.record_survey_location_checkbox);
        mRecordSurveyLocationCheckBox.setChecked(AppUtil.getAdminSettingsInstance().getRecordSurveyLocation());

        mDeviceIdentifierEditText = (EditText) v.findViewById(R.id.device_identifier_edit_text);
        mDeviceIdentifierEditText.setText(getAdminSettingsInstanceDeviceId());
        mDeviceIdentifierEditText.setSelection(mDeviceIdentifierEditText.getText().length());

        mDeviceLabelEditText = (EditText) v.findViewById(R.id.device_label_edit_text);
        mDeviceLabelEditText.setText(AppUtil.getAdminSettingsInstance().getDeviceLabel());

        mCustomLocaleEditText = (EditText) v.findViewById(R.id.custom_locale_edit_text);
        mCustomLocaleEditText.setText(getAdminSettingsInstanceCustomLocaleCode());

        mSpinner = v.findViewById(R.id.device_language_spinner);
        setSpinnerAdapter();

        // Disable edits if using default settings
        if (getActivity().getResources().getBoolean(R.bool.default_admin_settings)) {
            mDeviceIdentifierEditText.setEnabled(false);
            mDeviceLabelEditText.setEnabled(false);
            mCustomLocaleEditText.setEnabled(false);
            mRecordSurveyLocationCheckBox.setEnabled(false);
            mRequirePasswordCheckBox.setEnabled(false);
//            mShowDKCheckBox.setEnabled(false);
//            mShowRFCheckBox.setEnabled(false);
//            mShowNACheckBox.setEnabled(false);
//            mShowSkipCheckBox.setEnabled(false);
//            mShowScoresCheckBox.setEnabled(false);
//            mShowRostersCheckBox.setEnabled(false);
            mShowSurveysCheckBox.setEnabled(false);
//            mRosterEndPointCheckBox.setEnabled(false);
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

        Button settingsDownload = v.findViewById(R.id.fetch_endpoint_settings);
        settingsDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfigurationsDialog();
            }
        });

        Button restoreData = v.findViewById(R.id.restoreResponsesButton);
        restoreData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreDeletedSurveys();
            }
        });

        return v;
    }

    private void restoreDeletedSurveys() {
        if (getActivity() == null) return;
        Set<String> surveyIds = new HashSet<>();
        List<Survey> surveys = Survey.getAll();
        List<Response> responses = Response.getAll();
        int MAX = surveys.size() + responses.size();
        NotificationManager notificationManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notificationManager = getActivity().getSystemService(NotificationManager.class);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL)
                .setSmallIcon(R.drawable.ic_restore_black_24dp)
                .setContentTitle("Restoring Data")
                .setContentText("Please wait a moment as data is restored")
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Restoring previously deleted surveys that still have their data on the device";
            NotificationChannel channel = new NotificationChannel(CHANNEL, CHANNEL, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        builder.setProgress(MAX, 0, false);
        if (notificationManager != null) {
            notificationManager.notify(RESTORATION_ID, builder.build());
        }

        int counter = 0;
        for (Survey survey : surveys) {
            surveyIds.add(survey.getUUID());
            counter +=1;
            builder.setProgress(counter, MAX, false);
        }
        for (Response response : responses) {
            counter +=1;
            builder.setProgress(counter, MAX, false);
            if (!surveyIds.contains(response.getSurveyUUID())) {
                Survey survey = new Survey();
                survey.setUuid(response.getSurveyUUID());
                survey.setInstrumentRemoteId(response.getQuestion().getInstrument().getRemoteId());
                survey.setProjectId(response.getQuestion().getInstrument().getProjectId());
                survey.save();
                surveyIds.add(response.getSurveyUUID());
            }
        }
        builder.setContentText("Restoration complete").setProgress(0,0,false);
        if (notificationManager != null) {
            notificationManager.notify(RESTORATION_ID, builder.build());
            notificationManager.cancel(RESTORATION_ID);
        }
        getActivity().finish();
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
        // TODO: 10/6/17 fix
        menu.findItem(R.id.delete_data_button).setEnabled(false).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_admin_settings_button:
                saveAdminSettings();
                finishActivity();
                return true;
            case R.id.delete_data_button:
                deleteData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public String getAdminSettingsInstanceApiDomainName() {
        return AppUtil.getAdminSettingsInstance().getApiDomainName();
    }

//    private void toggleRosterSettingsVisibility(boolean isChecked) {
//        if (isChecked) {
//            mRosterSyncSettingsLabel.setVisibility(View.VISIBLE);
//            mApi2DomainNameLabel.setVisibility(View.VISIBLE);
//            mApi2VersionLabel.setVisibility(View.VISIBLE);
//            mApi2KeyLabel.setVisibility(View.VISIBLE);
//            mApi2DomainNameEditText.setVisibility(View.VISIBLE);
//            mApi2VersionEditText.setVisibility(View.VISIBLE);
//            mApi2KeyEditText.setVisibility(View.VISIBLE);
//        } else {
//            mRosterSyncSettingsLabel.setVisibility(View.GONE);
//            mApi2DomainNameEditText.setVisibility(View.GONE);
//            mApi2VersionEditText.setVisibility(View.GONE);
//            mApi2KeyEditText.setVisibility(View.GONE);
//            mApi2DomainNameLabel.setVisibility(View.GONE);
//            mApi2VersionLabel.setVisibility(View.GONE);
//            mApi2KeyLabel.setVisibility(View.GONE);
//        }
//    }

    public String getAdminSettingsInstanceApiVersion() {
        return AppUtil.getAdminSettingsInstance().getApiVersion();
    }

    public String getAdminSettingsInstanceProjectId() {
        return AppUtil.getAdminSettingsInstance().getProjectId();
    }

    public String getAdminSettingsInstanceApiKey() {
        return AppUtil.getAdminSettingsInstance().getApiKey();
    }

    public String getAdminSettingsInstanceDeviceId() {
        return AppUtil.getAdminSettingsInstance().getDeviceIdentifier();
    }

    public String getAdminSettingsInstanceCustomLocaleCode() {
        return AppUtil.getAdminSettingsInstance().getCustomLocaleCode();
    }

    private void setSpinnerAdapter() {
        final List<String> languageCodes = Instrument.getLanguages();
        ArrayList<String> displayLanguages = new ArrayList<>();
        for (String languageCode : languageCodes) {
            displayLanguages.add(new Locale(languageCode).getDisplayLanguage());
        }
        final ArrayAdapter<String> mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, displayLanguages);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != languageCodes.indexOf(AppUtil.getAdminSettingsInstance().getLanguage())) {
                    updateLocale(languageCodes.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinner.setSelection(languageCodes.indexOf(AppUtil.getAdminSettingsInstance().getLanguage()));
    }

    public String getLastUpdateTime() {
        String last = AppUtil.getAdminSettingsInstance().getLastSyncTime();
        if (last.isEmpty()) return last;
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
            final AdminSettings adminSettings = AdminSettings.getInstance();
            if (TextUtils.isEmpty(adminSettings.getApiDomainName())) {
                endpointEditText.setText(getString(R.string.default_api_domain_name));
            } else {
                endpointEditText.setText(adminSettings.getApiDomainName());
            }
            if (TextUtils.isEmpty(adminSettings.getApiVersion())) {
                versionEditText.setText(getString(R.string.default_api_version));
            } else {
                versionEditText.setText(adminSettings.getApiVersion());
            }
            if (TextUtils.isEmpty(adminSettings.getProjectId())) {
                projectEditText.setText(getString(R.string.default_project_id));
            } else {
                projectEditText.setText(adminSettings.getProjectId());
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
                        adminSettings.setApiDomainName(endpoint);
                        adminSettings.setApiVersion(version);
                        adminSettings.setProjectId(project);
                        adminSettings.setShowSurveys(surveysCheckBox.isChecked());
                        adminSettings.setRecordSurveyLocation(recordSurveyLocation.isChecked());
                        mApiDomainNameEditText.setText(endpoint);
                        mApiVersionEditText.setText(version);
                        mProjectIdEditText.setText(project);
                        mShowSurveysCheckBox.setChecked(surveysCheckBox.isChecked());
                        mRecordSurveyLocationCheckBox.setChecked(recordSurveyLocation.isChecked());
                        dialog.dismiss();
                        deviceUserLogin(adminSettings.getApiUrl());
                    } else {
                        endpointEditText.setError(getString(R.string.invalid_url));
                    }
                }
            });
        }
    }

    private void updateLocale(String languageCode) {
        AppUtil.getAdminSettingsInstance().setLanguage(languageCode);
        LocaleManager.setNewLocale(getActivity(), languageCode);
        Intent i = new Intent(getActivity(), AdminActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        } else {
            startActivity(i);
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
                            if (param == null || param.equals(HttpURLConnection.HTTP_UNAUTHORIZED
                                    + "")) {
                                Toast.makeText(getActivity(), R.string.invalid_user_credentials,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                AdminSettings.getInstance().setApiKey(param);
                                mApiKeyEditText.setText(param);
                                if (mDialog != null) mDialog.dismiss();
                                saveAdminSettings();
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

            AppUtil.getAdminSettingsInstance().setShowSurveys(mShowSurveysCheckBox.isChecked());
//            AppUtil.getAdminSettingsInstance().setShowRosters(mShowRostersCheckBox.isChecked());
//            AppUtil.getAdminSettingsInstance().setShowScores(mShowScoresCheckBox.isChecked());
//            AppUtil.getAdminSettingsInstance().setShowSkip(mShowSkipCheckBox.isChecked());
//            AppUtil.getAdminSettingsInstance().setShowNA(mShowNACheckBox.isChecked());
//            AppUtil.getAdminSettingsInstance().setShowRF(mShowRFCheckBox.isChecked());
//            AppUtil.getAdminSettingsInstance().setShowDK(mShowDKCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setRequirePassword(mRequirePasswordCheckBox.isChecked());
            AppUtil.getAdminSettingsInstance().setRecordSurveyLocation(mRecordSurveyLocationCheckBox.isChecked());

            //Roster settings
//            AppUtil.getAdminSettingsInstance().setUseEndpoint2(mRosterEndPointCheckBox.isChecked());
//            AppUtil.getAdminSettingsInstance().setApi2DomainName(mApi2DomainNameEditText.getText().toString());
//            AppUtil.getAdminSettingsInstance().setApi2Version(mApi2VersionEditText.getText().toString());
//            AppUtil.getAdminSettingsInstance().setApi2Key(mApi2KeyEditText.getText().toString());

        }
    }

    private void finishActivity() {
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

    public String getAdminSettingsInstanceApiUrl() {
        // Append forward slash to domain name if does not exist
        String domainName = AppUtil.getAdminSettingsInstance().getApiDomainName();
        char lastChar = domainName.charAt(domainName.length() - 1);
        if (lastChar != '/') domainName = domainName + "/";

        return domainName + "api/" + AppUtil.getAdminSettingsInstance().getApiVersion() + "/" +
                "projects/" + AppUtil.getAdminSettingsInstance().getProjectId() + "/";
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

    private static class RemoteAuthenticationTask extends AsyncTask<String, Void, String> {
        private final String TAG = "RemoteAuthTask";
        private String uri;
        private String userName;
        private String password;

        private AsyncTaskListener mListener;

        void setListener(AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            uri = params[0];
            userName = params[1];
            password = params[2];

            final String urlString;
            if (uri.contains("device_users/")) {
                urlString = uri;
            } else {
                urlString = uri + "device_users/";
            }
            JSONObject json = new JSONObject();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", userName);
                jsonObject.put("password", password);
                json.put("device_user", jsonObject);
            } catch (JSONException je) {
                if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
            }

            HttpURLConnection urlConnection = null;
            String apiKey = null;

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(30000);
                urlConnection.setDoOutput(true);

                byte[] outputInBytes = json.toString().getBytes(CharEncoding.UTF_8);
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(outputInBytes);
                outputStream.close();

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    InputStream in = urlConnection.getInputStream();

                    int bytesRead = 0;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = in.read(buffer)) > 0) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.close();
                    String jsonString = new String(output.toByteArray());
                    JSONObject jsonObject = new JSONObject(jsonString);
                    apiKey = jsonObject.optString("access_token", null);
                } else if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    apiKey = HttpURLConnection.HTTP_UNAUTHORIZED + "";
                }

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Exception: " + e);
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return apiKey;
        }

        public interface AsyncTaskListener {
            void onAsyncTaskFinished(String param);
        }

        @Override
        protected void onPostExecute(String param) {
            super.onPostExecute(param);
            mListener.onAsyncTaskFinished(param);
        }

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
            finishActivity();
        }
    }
}
