package org.adaptlab.chpir.android.survey;

import androidx.fragment.app.Fragment;

public class SurveyNoteActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SurveyNoteFragment();
    }

}
