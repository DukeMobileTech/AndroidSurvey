package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.repositories.DisplayRepository;

import java.util.HashMap;

public class DisplayViewModel extends AndroidViewModel {
    public final String TAG = this.getClass().getName();
    private DisplayRepository mDisplayRepository;
    private HashMap<String, Response> mResponses;

    public DisplayViewModel(@NonNull Application application, Long displayId) {
        super(application);
        mDisplayRepository = new DisplayRepository(application);
        mResponses = new HashMap<>();
    }

    public HashMap<String, Response> getResponses() {
        return mResponses;
    }

    public Response getResponse(String questionIdentifier) {
        return mResponses.get(questionIdentifier);
    }

    public void setResponse(String questionIdentifier, Response response) {
        mResponses.put(questionIdentifier, response);
    }
}
