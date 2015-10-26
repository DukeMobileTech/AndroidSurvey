package org.adaptlab.chpir.android.survey;

import android.os.Bundle;

public abstract class AuthorizedActivity extends SingleFragmentActivity implements Foreground.Listener {
    private Foreground.Binding mListenerBinder;

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
        authorize();
    }

    @Override
    public void onBecameBackground() {
        AuthUtils.signOut();
    }

    private void authorize() {
        AppUtil.authorize();
    }
}