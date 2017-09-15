package org.adaptlab.chpir.android.survey.questionfragments;

import android.view.ViewGroup;
import android.widget.TimePicker;

import org.adaptlab.chpir.android.survey.FormatUtils;
import org.adaptlab.chpir.android.survey.QuestionFragment;

public class TimeQuestionFragment extends QuestionFragment {
    private int mHour;
    private int mMinute;
    private TimePicker mTimePicker;
    private boolean mHasBeenReset = false;

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mTimePicker = new TimePicker(getActivity());
        mHour = mTimePicker.getCurrentHour();
        mMinute = mTimePicker.getCurrentMinute();
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
                if (!mHasBeenReset) {
                    clearSpecialResponseSelection();
                }
                setResponseText();
            }
        });
        questionComponent.addView(mTimePicker);
    }

    @Override
    protected String serialize() {
        if (mHasBeenReset) {
            mHasBeenReset = false;
            return "";
        }
        return FormatUtils.formatTime(mHour, mMinute);
    }

    @Override
    protected void unSetResponse() {
        mHour = 0;
        mMinute = 0;
        mHasBeenReset = true;
    }

    @Override
    protected void deserialize(String responseText) {
        int[] timeComponents = FormatUtils.unformatTime(responseText);
        if(timeComponents != null) {
            mTimePicker.setCurrentHour(timeComponents[0]);
            mTimePicker.setCurrentMinute(timeComponents[1]);
        }
    }
   
}
