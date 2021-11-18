package org.adaptlab.chpir.android.survey;


import androidx.fragment.app.Fragment;

public class AdminActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new AdminFragment();
    }

}
