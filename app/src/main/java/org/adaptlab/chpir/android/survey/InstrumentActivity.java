package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.models.Project;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;

import static org.adaptlab.chpir.android.survey.utils.AppUtil.getProjectId;

public class InstrumentActivity extends AppCompatActivity {
    public final static String EXTRA_AUTHORIZE_SURVEY =
            "org.adaptlab.chpir.android.survey.authorize_survey_bool";
    private final static String TAG = "InstrumentActivity";
    private boolean mAuthorizeSurvey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.appInit(this);
        setContentView(R.layout.activity_instrument2);
        if (getIntent() != null) {
            mAuthorizeSurvey = getIntent().getBooleanExtra(EXTRA_AUTHORIZE_SURVEY, false);
        }
        requestNeededPermissions();
        checkApiEndpointSettings();
        setupViewPager();
    }

    private void requestNeededPermissions() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (!hasPermission(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    private void checkApiEndpointSettings() {
        AdminSettings adminSettings = AdminSettings.getInstance();
        if (TextUtils.isEmpty(adminSettings.getApiDomainName()) || TextUtils.isEmpty
                (adminSettings.getApiVersion()) || TextUtils.isEmpty(adminSettings.getProjectId()
        ) || TextUtils.isEmpty(adminSettings.getApiKey())) {
            startActivity(new Intent(this, AdminActivity.class));
        }
    }

    private void setupViewPager() {
        FragmentPagerAdapter mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(mFragmentPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.slidingTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private boolean hasPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViewPager();
        displayProjectName();
    }

    public void displayProjectName() {
        Project project = Project.findByRemoteId(getProjectId());
        TextView textView = findViewById(R.id.projectName);
        if (project != null && textView != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(project.getName());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    public boolean isAuthorizeSurvey() {
        return mAuthorizeSurvey;
    }

}
