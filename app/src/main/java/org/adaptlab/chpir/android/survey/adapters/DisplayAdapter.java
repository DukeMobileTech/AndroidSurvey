package org.adaptlab.chpir.android.survey.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;

import java.util.List;

public class DisplayAdapter extends ListAdapter<List<QuestionRelation>, DisplayAdapter.DisplayViewHolder> {
    private static final DiffUtil.ItemCallback<List<QuestionRelation>> DIFF_CALLBACK = new DiffUtil.ItemCallback<List<QuestionRelation>>() {
        @Override
        public boolean areItemsTheSame(@NonNull List<QuestionRelation> oldQuestionRelations, @NonNull List<QuestionRelation> newQuestionRelations) {
            if (oldQuestionRelations.size() != newQuestionRelations.size()) return false;
            boolean same = true;
            for (int k = 0; k < oldQuestionRelations.size(); k++) {
                QuestionRelation oldQuestionRelation = oldQuestionRelations.get(k);
                QuestionRelation newQuestionRelation = newQuestionRelations.get(k);
                same = oldQuestionRelation.response.getUUID().equals(newQuestionRelation.response.getUUID());
                if (!same) break;
            }
            return same;
        }

        @Override
        public boolean areContentsTheSame(@NonNull List<QuestionRelation> oldQuestionRelations, @NonNull List<QuestionRelation> newQuestionRelations) {
            if (oldQuestionRelations.size() != newQuestionRelations.size()) return false;
            boolean same = true;
            for (int k = 0; k < oldQuestionRelations.size(); k++) {
                QuestionRelation oldQuestionRelation = oldQuestionRelations.get(k);
                QuestionRelation newQuestionRelation = newQuestionRelations.get(k);
                same = oldQuestionRelation.response.getText().equals(newQuestionRelation.response.getText()) &&
                        oldQuestionRelation.response.getSpecialResponse().equals(newQuestionRelation.response.getSpecialResponse()) &&
                        oldQuestionRelation.response.getOtherResponse().equals(newQuestionRelation.response.getOtherResponse());
                if (!same) break;
            }
            return same;
        }
    };
    private static final int DEFAULT = 0;
    private static final int TABLE = 1;

    private List<ResponseRelationAdapter> mResponseRelationAdapters;
    private Context mContext;

    public DisplayAdapter(Context context) {
        super(DIFF_CALLBACK);
        mContext = context;
    }

    public void setResponseRelationAdapters(List<ResponseRelationAdapter> adapters) {
        mResponseRelationAdapters = adapters;
    }

    @NonNull
    @Override
    public DisplayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_display, parent, false);
        return new DisplayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DisplayViewHolder viewHolder, int position) {
        ResponseRelationAdapter adapter = mResponseRelationAdapters.get(position);
        List<QuestionRelation> questionRelations = getItem(position);
        if (viewHolder.recyclerView.getAdapter() == null) {
            viewHolder.recyclerView.setAdapter(adapter);
        }
        adapter.submitList(questionRelations);
    }

    @Override
    public int getItemViewType(int position) {
        List<QuestionRelation> group = getItem(position);
        if (group.size() == 0 || TextUtils.isEmpty(group.get(0).question.getTableIdentifier())) {
            return DEFAULT;
        } else {
            return TABLE;
        }
    }

    class DisplayViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        DisplayViewHolder(final View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.questionsRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        }
    }

}
