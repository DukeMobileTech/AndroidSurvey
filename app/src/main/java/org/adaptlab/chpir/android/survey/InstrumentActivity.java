package org.adaptlab.chpir.android.survey;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import org.adaptlab.chpir.android.survey.utils.LocaleManager;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ProjectViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.ProjectViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;

public class InstrumentActivity extends AppCompatActivity {
    private final static String TAG = "InstrumentActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.appInit(getApplication());
        setContentView(R.layout.activity_instrument);
        setSettings();
        requestNeededPermissions();
    }

    private void setSettings() {
        SettingsViewModel settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(this, new Observer<Settings>() {
            @Override
            public void onChanged(@Nullable Settings settings) {
                if (settings == null) return;
                if (settings.getProjectId() != null)
                    setProject(Long.valueOf(settings.getProjectId()));
                if (TextUtils.isEmpty(settings.getApiUrl()) || TextUtils.isEmpty(settings.getApiVersion()) ||
                        TextUtils.isEmpty(settings.getProjectId()) || TextUtils.isEmpty(settings.getApiKey())) {
                    Intent intent = new Intent(InstrumentActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
                setupViewPager();
            }
        });
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
        if (!hasPermission(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    private void setupViewPager() {
        InstrumentSurveyPagerAdapter instrumentSurveyPagerAdapter = new InstrumentSurveyPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(instrumentSurveyPagerAdapter);
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_instrument, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_item_submit_all).setEnabled(false).setVisible(false);
        if (getResources().getBoolean(R.bool.default_hide_admin_button)) {
            menu.findItem(R.id.menu_item_settings).setEnabled(false).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivity(i);
                }
                return true;
            case R.id.menu_item_refresh:
                AppUtil.downloadData(this.getApplication());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
