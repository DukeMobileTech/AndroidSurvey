package org.adaptlab.chpir.android.survey.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.relations.ResponseRelation;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.List;

public class DisplayAdapter extends RecyclerView.Adapter<DisplayAdapter.DisplayViewHolder> {
    private static final int DEFAULT = 0;
    private static final int TABLE = 1;

    private List<List<QuestionRelation>> mQuestionRelationGroups;
    private List<List<ResponseRelation>> mResponseRelationGroups;
    private List<ResponseRelationAdapter> mResponseRelationAdapters;
    private SurveyViewModel mSurveyViewModel;
    private Context mContext;

    public DisplayAdapter(Context context) {
        mContext = context;
    }

    public void setQuestionRelationGroups(List<List<QuestionRelation>> groups) {
        mQuestionRelationGroups = groups;
        notifyDataSetChanged();
    }

    public void setResponseRelationGroups(List<List<ResponseRelation>> groups) {
        mResponseRelationGroups = groups;
        notifyDataSetChanged();
    }

    public void setResponseRelationAdapters(List<ResponseRelationAdapter> adapters) {
        mResponseRelationAdapters = adapters;
    }

    public void setSurveyViewModel(SurveyViewModel viewModel) {
        mSurveyViewModel = viewModel;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DisplayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_display, parent, false);
        return new DisplayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DisplayViewHolder viewHolder, int position) {
        if (mResponseRelationAdapters == null || mQuestionRelationGroups == null || mResponseRelationGroups == null)
            return;
        ResponseRelationAdapter adapter = mResponseRelationAdapters.get(position);
        List<QuestionRelation> questionRelations = mQuestionRelationGroups.get(position);
        List<ResponseRelation> responseRelations = mResponseRelationGroups.get(position);

        viewHolder.recyclerView.setAdapter(adapter);

        adapter.setQuestionRelations(questionRelations);
        adapter.setSurveyViewModel(mSurveyViewModel);
        adapter.submitList(responseRelations);
    }

    @Override
    public int getItemViewType(int position) {
        List<QuestionRelation> group = mQuestionRelationGroups.get(position);
        if (TextUtils.isEmpty(group.get(0).question.getTableIdentifier())) {
            return DEFAULT;
        } else {
            return TABLE;
        }
    }

    @Override
    public int getItemCount() {
        return mQuestionRelationGroups == null ? 0 : mQuestionRelationGroups.size();
    }

    class DisplayViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        DisplayViewHolder(final View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.questionsRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(mContext.getResources().getDrawable(R.drawable.border));
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

    }
}
