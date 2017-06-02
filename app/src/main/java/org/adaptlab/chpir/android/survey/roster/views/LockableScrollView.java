package org.adaptlab.chpir.android.survey.roster.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class LockableScrollView extends ScrollView {

    private boolean scrollingEnabled = true;

    public LockableScrollView(Context context) {
        super(context);
    }

    public LockableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isScrollingEnabled() {
        return scrollingEnabled;
    }

    public void setScrollingEnabled(boolean status) {
        scrollingEnabled = status;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScrollingEnabled() && super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isScrollingEnabled() && super.onTouchEvent(ev);
    }

}
