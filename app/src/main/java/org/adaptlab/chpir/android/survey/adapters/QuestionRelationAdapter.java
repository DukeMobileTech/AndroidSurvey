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
import org.adaptlab.chpir.android.survey.viewmodels.DisplayViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

public class QuestionRelationAdapter extends ListAdapter<QuestionRelation, QuestionViewHolder> {
    public static final String TAG = QuestionRelationAdapter.class.getName();

    private static final DiffUtil.ItemCallback<QuestionRelation> DIFF_CALLBACK = new DiffUtil.ItemCallback<QuestionRelation>() {
        @Override
        public boolean areItemsTheSame(@NonNull QuestionRelation oldQuestionRelation, @NonNull QuestionRelation newQuestionRelation) {
            return oldQuestionRelation.question.getQuestionIdentifier().equals(newQuestionRelation.question.getQuestionIdentifier());
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuestionRelation oldQuestionRelation, @NonNull QuestionRelation newQuestionRelation) {
            return oldQuestionRelation.question.getQuestionIdentifier().equals(newQuestionRelation.question.getQuestionIdentifier());
        }
    };

    private QuestionViewHolder.OnResponseSelectedListener mListener;

    private SurveyViewModel mSurveyViewModel;

    private DisplayViewModel mDisplayViewModel;

    public QuestionRelationAdapter(QuestionViewHolder.OnResponseSelectedListener listener, SurveyViewModel viewModel) {
        super(DIFF_CALLBACK);
        mListener = listener;
        mSurveyViewModel = viewModel;
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
        viewHolder.setSurveyViewModel(mSurveyViewModel);
        QuestionRelation questionRelation = getItem(position);
        viewHolder.setRelations(questionRelation);
        viewHolder.setDisplayViewModel(mDisplayViewModel);
    }

    @Override
    public int getItemViewType(int position) {
        QuestionRelation questionRelation = getItem(position);
        String type = questionRelation.question.getQuestionType();
        return QuestionViewHolderFactory.getQuestionViewType(type);
    }

    SurveyViewModel getSurveyViewModel() {
        return mSurveyViewModel;
    }

    DisplayViewModel getDisplayViewModel() {
        return mDisplayViewModel;
    }

    void setDisplayViewModel(DisplayViewModel viewModel) {
        mDisplayViewModel = viewModel;
    }

    protected QuestionViewHolder.OnResponseSelectedListener getListener() {
        return mListener;
    }

}
