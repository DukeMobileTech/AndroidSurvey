package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.adaptlab.chpir.android.survey.adapters.InstrumentSurveyPagerAdapter;
import org.adaptlab.chpir.android.survey.entities.Project;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.EncryptUtil;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;
import org.adaptlab.chpir.android.survey.utils.LocationManager;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ProjectViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.ProjectViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class InstrumentActivity extends AppCompatActivity {
    private final static String TAG = "InstrumentActivity";
    private static final int ACCESS_PERMISSION_CODE = 1;
    private LocationManager mLocationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument);
        if (AppUtil.getDatabaseKey() == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                String password = EncryptUtil.getPassword();
                if (password == null) {
                    generatePass();
                } else {
                    AppUtil.setDatabaseKey(password);
                    init();
                }
            } else {
                generatePass();
            }
        } else {
            init();
        }
    }

    private void init() {
        AppUtil.appInit(this);
        requestNeededPermissions();
    }

    private void generatePass() {
        String passPhrase = RandomStringUtils.randomAlphanumeric(32);
        AppUtil.setDatabaseKey(passPhrase);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                EncryptUtil.encrypt(passPhrase);
            } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException |
                    InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
                Log.e(TAG, "Exception: " + e);
            }
        }
        init();
    }

    private void setSettings() {
        final SettingsViewModel settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(this, new Observer<Settings>() {
            @Override
            public void onChanged(@Nullable Settings settings) {
                if (settings == null) return;
                if (settings.getProjectId() != null)
                    setProject(Long.valueOf(settings.getProjectId()));
                if (TextUtils.isEmpty(settings.getApiUrl()) || TextUtils.isEmpty(settings.getApiVersion()) ||
                        TextUtils.isEmpty(settings.getProjectId()) || TextUtils.isEmpty(settings.getApiKey())) {
                    if (!settingsViewModel.isSetting()) {
                        settingsViewModel.setSetting(true);
                        Intent intent = new Intent(InstrumentActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                }
                setupViewPager();
                startLocationUpdates();
            }
        });
    }

    private void setupViewPager() {
        InstrumentSurveyPagerAdapter instrumentSurveyPagerAdapter = new InstrumentSurveyPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(instrumentSurveyPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.slidingTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setProject(Long projectId) {
        ProjectViewModelFactory factory = new ProjectViewModelFactory(getApplication(), projectId);
        ProjectViewModel viewModel = ViewModelProviders.of(this, factory).get(ProjectViewModel.class);
        viewModel.getProject().observe(this, new Observer<Project>() {
            @Override
            public void onChanged(Project project) {
                TextView textView = findViewById(R.id.projectName);
                if (project != null && textView != null) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(project.getName());
                }
            }
        });
    }

    private void requestNeededPermissions() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (hasPermission(permissions)) {
            setSettings();
        } else {
            ActivityCompat.requestPermissions(this, permissions, ACCESS_PERMISSION_CODE);
        }
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLocationManager != null) mLocationManager.stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
        setSettings();
    }

    private void startLocationUpdates() {
        if (mLocationManager == null) {
            mLocationManager = new LocationManager(this);
            mLocationManager.startLocationUpdates();
        }
    }

}
