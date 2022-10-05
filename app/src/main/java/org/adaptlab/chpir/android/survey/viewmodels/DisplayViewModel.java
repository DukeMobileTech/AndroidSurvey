package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;

import java.util.HashMap;

public class DisplayViewModel extends AndroidViewModel {
    public final String TAG = this.getClass().getName();
    private final HashMap<String, Response> mResponses;
    private final HashMap<String, QuestionRelation> mQuestions;

    public DisplayViewModel(@NonNull Application application) {
        super(application);
        mResponses = new HashMap<>();
        mQuestions = new HashMap<>();
    }

    public HashMap<String, Response> getResponses() {
        return mResponses;
    }

    public HashMap<String, QuestionRelation> getQuestions() {
        return mQuestions;
    }

    public Response getResponse(String questionIdentifier) {
        return mResponses.get(questionIdentifier);
    }

    public void setResponse(String questionIdentifier, Response response) {
        mResponses.put(questionIdentifier, response);
    }

    public QuestionRelation getQuestion(String questionIdentifier) {
        return mQuestions.get(questionIdentifier);
    }

    public void setQuestion(String questionIdentifier, QuestionRelation questionRelation) {
        mQuestions.put(questionIdentifier, questionRelation);
    }
}
