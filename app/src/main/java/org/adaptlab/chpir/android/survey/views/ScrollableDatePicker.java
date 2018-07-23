package org.adaptlab.chpir.android.survey.views;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.DatePicker;

public class ScrollableDatePicker extends DatePicker {
    public ScrollableDatePicker(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent viewParent = getParent();
            if (viewParent != null) {
                viewParent.requestDisallowInterceptTouchEvent(true);
            }
        }
        return false;
    }

}
