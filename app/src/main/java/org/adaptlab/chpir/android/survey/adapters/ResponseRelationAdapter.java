package org.adaptlab.chpir.android.survey.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolderFactory;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

public class ResponseRelationAdapter extends ListAdapter<QuestionRelation, QuestionViewHolder> {
    public static final String TAG = ResponseRelationAdapter.class.getName();
    private static final DiffUtil.ItemCallback<QuestionRelation> DIFF_CALLBACK = new DiffUtil.ItemCallback<QuestionRelation>() {
        @Override
        public boolean areItemsTheSame(@NonNull QuestionRelation oldQuestionRelation, @NonNull QuestionRelation newQuestionRelation) {
            return oldQuestionRelation.response.getUUID().equals(newQuestionRelation.responses.get(0).getUUID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuestionRelation oldQuestionRelation, @NonNull QuestionRelation newQuestionRelation) {
            return oldQuestionRelation.response.getText().equals(newQuestionRelation.response.getText()) &&
                    oldQuestionRelation.response.getSpecialResponse().equals(newQuestionRelation.response.getSpecialResponse()) &&
                    oldQuestionRelation.response.getOtherResponse().equals(newQuestionRelation.response.getOtherResponse());
        }
    };
    private QuestionViewHolder.OnResponseSelectedListener mListener;
    private SurveyViewModel mSurveyViewModel;

    public ResponseRelationAdapter(QuestionViewHolder.OnResponseSelectedListener listener) {
        super(DIFF_CALLBACK);
        mListener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_question, parent, false);
        return QuestionViewHolderFactory.createViewHolder(view, parent.getContext(), viewType, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder viewHolder, int position) {
        viewHolder.setAdapter(this);
        if (mSurveyViewModel != null) viewHolder.setSurveyViewModel(mSurveyViewModel);
        QuestionRelation questionRelation = getItem(position);
        if (questionRelation == null) return;
        viewHolder.setRelations(questionRelation);
    }

    @Override
    public int getItemViewType(int position) {
        QuestionRelation questionRelation = getItem(position);
        if (questionRelation == null) return -1;
        String type = questionRelation.question.getQuestionType();
        return QuestionViewHolderFactory.getQuestionViewType(type);
    }

    SurveyViewModel getSurveyViewModel() {
        return mSurveyViewModel;
    }

    void setSurveyViewModel(SurveyViewModel viewModel) {
        mSurveyViewModel = viewModel;
    }

    protected QuestionViewHolder.OnResponseSelectedListener getListener() {
        return mListener;
    }

}
