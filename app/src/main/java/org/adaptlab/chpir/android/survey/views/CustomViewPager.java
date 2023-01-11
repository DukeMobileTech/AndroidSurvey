package org.adaptlab.chpir.android.survey.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    private final int minimumDistance = 100;
    private SwipeListener mSwipeListener;
    private float x1, x2;
    private boolean swipe;

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomViewPager(@NonNull Context context) {
        super(context);
    }

    public void setSwipeListener(SwipeListener listener) {
        mSwipeListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
//        Log.i("CustomViewPager", "onInterceptTouchEvent");
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                x1 = event.getX();
//                Log.i("CustomViewPager", "x1 = " + x1);
//                break;
//            case MotionEvent.ACTION_UP:
//                x2 = event.getX();
//                Log.i("CustomViewPager", "x2 = " + x2);
//                swipe = Math.abs(x2 - x1) > minimumDistance;
//                break;
//        }
//        Log.i("CustomViewPager", " swipe = " + swipe);
//        boolean status = mSwipeListener.onSwipe();
//        return swipe && mSwipeListener.onSwipe() && super.onInterceptTouchEvent(event);
//        return super.onInterceptTouchEvent(event);
        return false;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        Log.i("CustomViewPager", "onTouchEvent");
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                x1 = event.getX();
//                Log.i("CustomViewPager", "x1 = " + x1);
//                break;
//            case MotionEvent.ACTION_UP:
//                x2 = event.getX();
//                Log.i("CustomViewPager", "x2 = " + x2);
//                swipe = Math.abs(x2 - x1) > minimumDistance;
//                break;
//        }
//        boolean status = mSwipeListener.onSwipe();
//        return status && super.onTouchEvent(event);
//        return super.onTouchEvent(event);
//        return swipe && mSwipeListener.onSwipe() && super.onTouchEvent(event);
//        return super.onTouchEvent(event);
//    }

}
