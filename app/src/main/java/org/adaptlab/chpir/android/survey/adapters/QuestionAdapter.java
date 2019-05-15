package org.adaptlab.chpir.android.survey.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.OptionSet;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyResponse;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolderFactory;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionViewHolder> {
    public final String TAG = this.getClass().getName();
    private final LayoutInflater mInflater;
    private Context mContext;
    private Survey mSurvey;
    private List<Question> mQuestions;
    private List<DisplayInstruction> mDisplayInstructions;
    private List<Response> mResponses;
    private LongSparseArray<Instruction> mInstructions;
    private LongSparseArray<OptionSet> mOptionSets;
    private LongSparseArray<Option> mOptions; // The key is Option's RemoteId
    private LongSparseArray<List<OptionSetOption>> mOptionSetOptions; // The key is OptionSet's RemoteId

    public QuestionAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setSurvey(Survey survey) {
        mSurvey = survey;
        notifyDataSetChanged();
    }

    public void setResponses(List<Response> responses) {
        Log.i(TAG, "Set Responses... ");
        if (mResponses == null) {
            this.mResponses = responses;
        } else {
            if (responses == null) return;
            final List<Response> oldResponses = new ArrayList<>(this.mResponses);
            this.mResponses.clear();
            this.mResponses.addAll(responses);

            DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return oldResponses.size();
                }

                @Override
                public int getNewListSize() {
                    return mResponses.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return oldResponses.get(oldItemPosition).getUUID().equals(mResponses.get(newItemPosition).getUUID());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Response oldResponse = oldResponses.get(oldItemPosition);
                    Response newResponse = mResponses.get(newItemPosition);
                    boolean same = oldResponse.getText().equals(newResponse.getText()) &&
                            oldResponse.getSpecialResponse().equals(newResponse.getSpecialResponse()) &&
                            oldResponse.getOtherResponse().equals(newResponse.getOtherResponse());
                    Log.i(TAG, "SAME? " + same + " @position: " + newItemPosition);
                    return same;
                }
            }).dispatchUpdatesTo(this);
        }

    }

    public void setQuestions(List<Question> questions) {
        mQuestions = questions;
        notifyDataSetChanged();
    }

    public void setInstructions(LongSparseArray<Instruction> instructions) {
        mInstructions = instructions;
        notifyDataSetChanged();
    }

    public void setDisplayInstructions(List<DisplayInstruction> displayInstructions) {
        mDisplayInstructions = displayInstructions;
        notifyDataSetChanged();
    }

    public void setOptionSets(LongSparseArray<OptionSet> optionSets) {
        mOptionSets = optionSets;
        notifyDataSetChanged();
    }

    public void setOptions(LongSparseArray<Option> options) {
        mOptions = options;
        notifyDataSetChanged();
    }

    public void setOptionSetOptions(LongSparseArray<List<OptionSetOption>> optionSetOptions) {
        mOptionSetOptions = optionSetOptions;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_question, parent, false);
        return QuestionViewHolderFactory.createViewHolder(view, mContext, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder viewHolder, int position) {
        Question question = mQuestions.get(position);
        Log.i(TAG, "Binding view for q#: " + question.getNumberInInstrument());
        viewHolder.setQuestionData(question, getQuestionInstruction(question), getDisplayInstructions(question),
                getQuestionOptionSetInstruction(question), getOptions(question), getSpecialOptions(question));
        if (mSurvey != null && mResponses != null)
            viewHolder.setSurveyData(mSurvey, mResponses);
    }

    @Override
    public int getItemCount() {
        return mQuestions == null ? 0 : mQuestions.size();
    }

    @Override
    public int getItemViewType(int position) {
        String type = mQuestions.get(position).getQuestionType();
        return QuestionViewHolderFactory.getQuestionViewType(type);
    }

    private Instruction getQuestionInstruction(Question question) {
        return (mInstructions == null || question.getInstructionId() == null) ? null : mInstructions.get(question.getInstructionId());
    }

    private Instruction getQuestionOptionSetInstruction(Question question) {
        Instruction instruction = null;
        if (mInstructions != null && mOptionSets != null && question.getRemoteOptionSetId() != null) {
            OptionSet optionSet = mOptionSets.get(question.getRemoteOptionSetId());
            if (optionSet != null && optionSet.getInstructionRemoteId() != null) {
                instruction = mInstructions.get(optionSet.getInstructionRemoteId());
            }
        }
        return instruction;
    }

    private List<Option> getOptions(Question question) {
        List<Option> options = new ArrayList<>();
        if (question.getRemoteOptionSetId() != null) {
            options = questionOptions(question.getRemoteOptionSetId());
        }
        return options;
    }

    private List<Option> getSpecialOptions(Question question) {
        List<Option> options = new ArrayList<>();
        if (question.getRemoteSpecialOptionSetId() != null) {
            options = questionOptions(question.getRemoteSpecialOptionSetId());
        }
        return options;
    }

    private List<Option> questionOptions(Long remoteOptionSetId) {
        List<Option> options = new ArrayList<>();
        if (mOptions != null && mOptionSetOptions != null) {
            List<OptionSetOption> optionSetOptions = mOptionSetOptions.get(remoteOptionSetId);
            if (optionSetOptions != null) {
                for (OptionSetOption optionSetOption : optionSetOptions) {
                    options.add(mOptions.get(optionSetOption.getOptionRemoteId()));
                }
            }
        }
        return options;
    }

    /**
     * @param question This is the question to be displayed at a position
     * @return A SparseArray containing Instructions to be show right before question.
     * The SparseArray is generated from the List of DisplayInstructions belonging to the given display
     */
    private SparseArray<List<Instruction>> getDisplayInstructions(Question question) {
        SparseArray<List<Instruction>> sparseArray = new SparseArray<>();
        if (mDisplayInstructions == null) return sparseArray;
        for (DisplayInstruction displayInstruction : mDisplayInstructions) {
            if (displayInstruction.getPosition() == question.getNumberInInstrument()) {
                List<Instruction> instructionList = sparseArray.get(question.getNumberInInstrument());
                if (instructionList == null) {
                    instructionList = new ArrayList<>();
                }
                instructionList.add(mInstructions.get(displayInstruction.getInstructionRemoteId()));
                sparseArray.put(displayInstruction.getPosition(), instructionList);
            }
        }
        return sparseArray;
    }

}
