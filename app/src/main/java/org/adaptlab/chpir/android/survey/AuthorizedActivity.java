package org.adaptlab.chpir.android.survey;

import android.os.Bundle;

import org.adaptlab.chpir.android.survey.utils.AuthUtils;
import org.adaptlab.chpir.android.survey.utils.Foreground;

public abstract class AuthorizedActivity extends SingleFragmentActivity implements Foreground.Listener {

    private static final String TAG = "AuthorizedActivity";
    private Foreground.Binding mListenerBinder;
    private boolean mAuthorize = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListenerBinder = Foreground.get(getApplication()).addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListenerBinder.unbind();
    }

    @Override
    public void onBecameForeground() {
        mAuthorize = true;
    }

    @Override
    public void onBecameBackground() {
        mAuthorize = true;
        AuthUtils.signOut();
    }

    public boolean getAuthorize() {
        return mAuthorize;
    }

    public void setAuthorize(boolean status) {
        mAuthorize = status;
    }

}