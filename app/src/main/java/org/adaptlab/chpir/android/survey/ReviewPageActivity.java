package org.adaptlab.chpir.android.survey;

import androidx.fragment.app.Fragment;

public class ReviewPageActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ReviewPageFragment();
    }

}
