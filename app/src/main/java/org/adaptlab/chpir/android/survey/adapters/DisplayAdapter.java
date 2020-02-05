package org.adaptlab.chpir.android.survey.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayViewModel;

import java.util.List;

public class DisplayAdapter extends ListAdapter<List<QuestionRelation>, DisplayAdapter.DisplayViewHolder> {
    public static final String TAG = DisplayAdapter.class.getName();

    private static final DiffUtil.ItemCallback<List<QuestionRelation>> DIFF_CALLBACK = new DiffUtil.ItemCallback<List<QuestionRelation>>() {
        @Override
        public boolean areItemsTheSame(@NonNull List<QuestionRelation> oldQuestionRelations, @NonNull List<QuestionRelation> newQuestionRelations) {
            if (oldQuestionRelations.size() != newQuestionRelations.size()) return false;
            boolean same = true;
            for (int k = 0; k < oldQuestionRelations.size(); k++) {
                QuestionRelation oldQuestionRelation = oldQuestionRelations.get(k);
                QuestionRelation newQuestionRelation = newQuestionRelations.get(k);
                same = oldQuestionRelation.question.getQuestionIdentifier().equals(newQuestionRelation.question.getQuestionIdentifier());
                if (!same) break;
            }
            return same;
        }

        @Override
        public boolean areContentsTheSame(@NonNull List<QuestionRelation> oldQuestionRelations, @NonNull List<QuestionRelation> newQuestionRelations) {
            if (oldQuestionRelations.size() != newQuestionRelations.size()) return false;
            for (int k = 0; k < oldQuestionRelations.size(); k++) {
                QuestionRelation oldQuestionRelation = oldQuestionRelations.get(k);
                QuestionRelation newQuestionRelation = newQuestionRelations.get(k);
                boolean same = oldQuestionRelation.question.getQuestionIdentifier().equals(newQuestionRelation.question.getQuestionIdentifier());
                if (!same) return false;
            }
            return true;
        }
    };

    private static final int DEFAULT = 0;

    private static final int TABLE = 1;

    private List<QuestionRelationAdapter> mQuestionRelationAdapters;

    private Context mContext;

    private DisplayViewModel mDisplayViewModel;

    public DisplayAdapter(Context context) {
        super(DIFF_CALLBACK);
        mContext = context;
    }

    public void setResponseRelationAdapters(List<QuestionRelationAdapter> adapters) {
        mQuestionRelationAdapters = adapters;
    }

    public void setDisplayViewModel(DisplayViewModel viewModel) {
        mDisplayViewModel = viewModel;
    }

    @NonNull
    @Override
    public DisplayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_display, parent, false);
        return new DisplayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DisplayViewHolder viewHolder, int position) {
        QuestionRelationAdapter adapter = mQuestionRelationAdapters.get(position);
        List<QuestionRelation> questionRelations = getItem(position);
        if (viewHolder.recyclerView.getAdapter() == null) {
            viewHolder.recyclerView.setAdapter(adapter);
        }
        adapter.setDisplayViewModel(mDisplayViewModel);
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

        @SuppressLint("ClickableViewAccessibility")
        DisplayViewHolder(final View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.questionsRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return false;
                }
            });
        }
    }

}
