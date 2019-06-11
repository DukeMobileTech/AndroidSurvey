package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.relations.ResponseRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRelationRepository;

import java.util.List;

public class ResponseRelationViewModel extends AndroidViewModel {
    private LiveData<List<ResponseRelation>> responseRelations;

    public ResponseRelationViewModel(@NonNull Application application, long instrumentId, long displayId, String surveyUUID) {
        super(application);
        ResponseRelationRepository responseRepository = new ResponseRelationRepository(application, instrumentId, displayId, surveyUUID);
        responseRelations = responseRepository.getResponses();
    }

    public LiveData<List<ResponseRelation>> getResponseRelations() {
        return responseRelations;
    }
}
