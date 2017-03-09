package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import android.view.ViewGroup;
import android.widget.TimePicker;

import org.adaptlab.chpir.android.survey.FormatUtils;
import org.adaptlab.chpir.android.survey.QuestionFragment;

/**
 * Created by Harry on 3/3/17.
 */
public class TimeFragment extends RosterFragment {
    private int mHour;
    private int mMinute;
    private TimePicker mTimePicker;

    @Override
    protected void createResponseComponent(ViewGroup responseComponent) {
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
        responseComponent.addView(mTimePicker);
    }

    @Override
    protected String serialize() {
        return FormatUtils.formatTime(mHour, mMinute);
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