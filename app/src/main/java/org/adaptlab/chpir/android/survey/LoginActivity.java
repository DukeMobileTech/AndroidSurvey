package org.adaptlab.chpir.android.survey;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;

import androidx.fragment.app.Fragment;

public class LoginActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, InstrumentActivity.class);
//        i.putExtra(InstrumentActivity.EXTRA_AUTHORIZE_SURVEY, true);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            finishAfterTransition();
        } else {
            startActivity(i);
            finish();
        }
    }

}