package org.adaptlab.chpir.android.survey.listeners;

import org.adaptlab.chpir.android.survey.views.OHScrollView;

public interface ScrollViewListener {
    void onScrollChanged(OHScrollView scrollView, int x, int y, int oldx, int oldy);
}