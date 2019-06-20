package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.adaptlab.chpir.android.survey.models.Project;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;

import static org.adaptlab.chpir.android.survey.utils.AppUtil.getProjectId;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayProjectName();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    public void displayProjectName() {
        Project project = Project.findByRemoteId(getProjectId());
        TextView textView = findViewById(R.id.projectName);
        if (project != null && textView != null) {
            textView.setVisibility(View.GONE);
            textView.setText(project.getName());
        }
    }

}