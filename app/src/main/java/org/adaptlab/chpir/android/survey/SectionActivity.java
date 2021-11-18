package org.adaptlab.chpir.android.survey;

import androidx.fragment.app.Fragment;

public class SectionActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new SectionFragment();
    }
}