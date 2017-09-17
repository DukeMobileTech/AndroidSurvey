package org.adaptlab.chpir.android.survey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class SurveyActivity extends AuthorizedActivity {

    @Override
    protected Fragment createFragment() {
        return new SurveyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            ((SurveyFragment) fragment).onResumeFragments();
        }
    }

}