package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.fragment.app.Fragment;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;

public class SetInstrumentLabelTask extends AsyncTask<InstrumentListLabel, Void,
        InstrumentListLabel> {
    private Fragment mFragment;

    public SetInstrumentLabelTask(Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected InstrumentListLabel doInBackground(InstrumentListLabel... params) {
        InstrumentListLabel instrumentListLabel = params[0];
        Instrument instrument = instrumentListLabel.getInstrument();
        instrumentListLabel.setLoaded(instrument.loaded());
        return instrumentListLabel;
    }

    @Override
    protected void onPostExecute(InstrumentListLabel instrumentListLabel) {
        if (mFragment.isAdded()) {
            if (!instrumentListLabel.isLoaded()) {
                CharSequence text = instrumentListLabel.getTextView().getText();
                Spannable spannable = new SpannableString(text);
                spannable.setSpan(new ForegroundColorSpan(mFragment.getResources().getColor(
                        R.color.red)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                instrumentListLabel.getTextView().setText(spannable);
            }
        }
    }
}