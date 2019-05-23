package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.repositories.DisplayResponseRepository;

import java.util.List;

public class DisplayResponseViewModel extends AndroidViewModel {
    private LiveData<List<Response>> mDisplayResponses;

    public DisplayResponseViewModel(@NonNull Application application, String surveyUUID, long instrumentId, long displayId) {
        super(application);
        DisplayResponseRepository responseRepository = new DisplayResponseRepository(application);
        mDisplayResponses = responseRepository.getDisplayResponseDao().displayResponses(surveyUUID, instrumentId, displayId);
    }

    public LiveData<List<Response>> getDisplayResponses() {
        return mDisplayResponses;
    }
}
