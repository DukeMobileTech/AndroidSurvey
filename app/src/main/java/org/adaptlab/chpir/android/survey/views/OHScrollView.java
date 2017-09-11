package org.adaptlab.chpir.android.survey.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import org.adaptlab.chpir.android.survey.listeners.ScrollViewListener;

public class OHScrollView extends HorizontalScrollView {

    private ScrollViewListener listener;

    public OHScrollView(Context context) {
        super(context);
    }

    public OHScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OHScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.listener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (listener != null) {
            listener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    @Override
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

}