package org.adaptlab.chpir.android.survey.questionfragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.TimePicker;

import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.SingleQuestionFragment;

public class TimeQuestionFragment extends SingleQuestionFragment {
    private int mHour;
    private int mMinute;
    private TimePicker mTimePicker;

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
                setResponseText();
            }
        });
        questionComponent.addView(mTimePicker);
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mSpecialResponses.clearCheck();
            }
        });
    }

    @Override
    protected String serialize() {
        return FormatUtils.formatTime(mHour, mMinute);
    }

    @Override
    protected void deserialize(String responseText) {
        int[] timeComponents = FormatUtils.unformatTime(responseText);
        if (timeComponents != null) {
            mTimePicker.setCurrentHour(timeComponents[0]);
            mTimePicker.setCurrentMinute(timeComponents[1]);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void unSetResponse() {
        if (mResponse != null) {
            mResponse.setResponse("");
        }
    }
}
