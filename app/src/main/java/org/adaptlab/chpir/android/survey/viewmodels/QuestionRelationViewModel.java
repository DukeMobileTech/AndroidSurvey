package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.QuestionRelationRepository;

import java.util.List;

public class QuestionRelationViewModel extends AndroidViewModel {
    public final String TAG = this.getClass().getName();

    private LiveData<List<QuestionRelation>> questionRelations;

    public QuestionRelationViewModel(@NonNull Application application, Long instrumentId, Long displayId) {
        super(application);
        QuestionRelationRepository repository = new QuestionRelationRepository(application, instrumentId, displayId);
        questionRelations = repository.getQuestions();
    }

    public LiveData<List<QuestionRelation>> getQuestionRelations() {
        return questionRelations;
    }

}
