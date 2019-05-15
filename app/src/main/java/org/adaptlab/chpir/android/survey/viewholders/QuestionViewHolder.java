package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;

import java.util.Date;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;

public abstract class QuestionViewHolder extends RecyclerView.ViewHolder {
    public final String TAG = this.getClass().getName();
    private Context mContext;
    private Survey mSurvey;
    private LongSparseArray<Response> mResponses;

    QuestionViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
    }

    public void setSurveyData(Survey survey, List<Response> responses) {
        mSurvey = survey;
        mResponses = new LongSparseArray<>();
        for (Response response : responses) {
            mResponses.put(response.getQuestionRemoteId(), response);
        }
        if (getResponse() != null) {
            deserialize(getResponse().getText());
            deserializeSpecialResponse();
        }
    }

    void saveResponse() {
        Response response = getResponse();
        response.setText(serialize());
        response.setSpecialResponse(BLANK);
        updateResponse(response);
    }

    private void updateResponse(Response response) {
        response.setTimeEnded(new Date());
        ResponseRepository repository = new ResponseRepository(SurveyApp.getInstance());
        repository.update(response);
    }

    void saveSpecialResponse(String specialResponse) {
        Response response = getResponse();
        response.setSpecialResponse(specialResponse);
        response.setText(BLANK);
        updateResponse(response);
    }

    Response getResponse() {
        return getResponses().get(getQuestion().getRemoteId());
    }

    public Context getContext() {
        return mContext;
    }

    public Survey getSurvey() {
        return mSurvey;
    }

    public LongSparseArray<Response> getResponses() {
        return mResponses;
    }

    protected abstract Question getQuestion();

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    public abstract void setQuestionData(Question displayQuestion, Instruction qInstruction,
                                         SparseArray<List<Instruction>> displayInstructions,
                                         Instruction osInstruction, List<Option> options,
                                         List<Option> specialOptions);

    protected abstract void deserialize(String responseText);

    protected abstract void deserializeSpecialResponse();

    protected abstract String serialize();

}
