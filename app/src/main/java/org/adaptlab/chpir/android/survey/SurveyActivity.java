package org.adaptlab.chpir.android.survey;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import java.util.List;

public class SurveyActivity extends AuthorizedActivity implements DisplayFragment.OnFragmentInteractionListener {
    private SurveyFragment surveyFragment;

    @Override
    protected Fragment createFragment() {
        surveyFragment = new SurveyFragment();
        return surveyFragment;
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public SurveyFragment getSurveyFragment() {
        return surveyFragment;
    }
}