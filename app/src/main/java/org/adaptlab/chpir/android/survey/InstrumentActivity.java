package org.adaptlab.chpir.android.survey;

import android.support.v4.app.Fragment;

public class InstrumentActivity extends AuthorizedActivity {

    @Override
    protected Fragment createFragment() {
        return new InstrumentFragment();
    }
    
}