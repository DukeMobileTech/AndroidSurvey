package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;

public class SetInstrumentLabelTask extends AsyncTask<InstrumentListLabel, Void, InstrumentListLabel> {
    private AsyncTaskListener mListener;

    public void setListener(AsyncTaskListener listener) {
        this.mListener = listener;
    }

    @Override
    protected InstrumentListLabel doInBackground(InstrumentListLabel... params) {
        InstrumentListLabel instrumentListLabel = params[0];
        Instrument instrument = instrumentListLabel.getInstrument();
        instrumentListLabel.setLoaded(instrument.isLoaded());
        return instrumentListLabel;
    }

    @Override
    protected void onPostExecute(InstrumentListLabel instrumentListLabel) {
        super.onPostExecute(instrumentListLabel);
        mListener.onAsyncTaskFinished(instrumentListLabel);
    }

    public interface AsyncTaskListener {
        void onAsyncTaskFinished(InstrumentListLabel instrumentListLabel);
    }

}