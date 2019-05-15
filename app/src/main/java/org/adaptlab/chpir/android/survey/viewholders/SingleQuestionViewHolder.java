package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class SingleQuestionViewHolder extends QuestionViewHolder {
    protected RadioGroup mSpecialResponses;
    private Question mQuestion;
    private Instruction mQuestionInstruction;
    private Instruction mOptionSetInstruction;
    private SparseArray<List<Instruction>> mDisplayInstructions;
    private List<Option> mOptions;
    private List<Option> mSpecialOptions;
    private TextView mDisplayInstructionTextView;
    private TextView mSpannedTextView;
    private TextView mOptionSetInstructionTextView;
    private ViewGroup mQuestionComponent;
    private Button mClearButton;

    public SingleQuestionViewHolder(View itemView, Context context) {
        super(itemView, context);
        mDisplayInstructionTextView = itemView.findViewById(R.id.displayInstructions);
        mSpannedTextView = itemView.findViewById(R.id.spannedTextView);
        mOptionSetInstructionTextView = itemView.findViewById(R.id.optionSetInstructions);
        mQuestionComponent = itemView.findViewById(R.id.response_component);
        mSpecialResponses = itemView.findViewById(R.id.specialResponseButtons);
        mClearButton = itemView.findViewById(R.id.clearResponsesButton);
    }

    @Override
    public Question getQuestion() {
        return mQuestion;
    }

    public List<Option> getOptions() {
        return mOptions;
    }

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    /**
     * @param otherText An EditText injected from a subclass i.e a write other subclass
     */
    void addOtherResponseView(EditText otherText) {
        otherText.setHint(R.string.other_specify_edittext);
        otherText.setEnabled(false);
        otherText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        otherText.addTextChangedListener(new TextWatcher() {
            private Timer timer;

            // Required by interface
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timer != null) timer.cancel();
            }

            public void afterTextChanged(final Editable s) {
                timer = new Timer();
//                if (!deserialization) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Run on UI Thread
                        if (getContext() != null) {
                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                        setOtherResponse(s.toString());
                                }
                            });
                        }
                    }
                }, 1000); // 1 second delay before saving to db
//                }
            }
        });

//        if (mResponse.getOtherResponse() != null) {
//            otherText.setText(mResponse.getOtherResponse());
//        }
    }

    @Override
    public void setQuestionData(Question displayQuestion, Instruction qInstruction,
                                SparseArray<List<Instruction>> displayInstructions,
                                Instruction osInstruction, List<Option> options, List<Option> specialOptions) {
        mQuestion = displayQuestion;
        mQuestionInstruction = qInstruction;
        mDisplayInstructions = displayInstructions;
        mOptionSetInstruction = osInstruction;
        mOptions = options;
        mSpecialOptions = specialOptions;
        setSpannedText();
        setDisplayInstructions();
        setOptionSetInstructions();
        // Overridden by subclasses to place their graphical elements on the fragment.
        createQuestionComponent(mQuestionComponent);
        setSpecialResponseView();
    }

    private void setSpannedText() {
        String number = mQuestion.getNumberInInstrument() + ": ";
        int numLen = number.length();
        String identifier = mQuestion.getQuestionIdentifier() + "\n";
        int idLen = identifier.length();
        String instructions = getQuestionInstructions();
        if (instructions.length() != 0) {
            instructions = instructions + "\n";
        }
        int insLen = instructions.length();
        Spanned text = getQuestionText();
        int textLen = text.length();
        SpannableString spannableText = new SpannableString(number + identifier + instructions + text);
        spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                0, numLen + idLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, numLen,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.ITALIC), numLen + idLen,
                numLen + idLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue)),
                numLen + idLen + insLen, numLen + idLen + insLen + textLen,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new RelativeSizeSpan(1.2f),
                numLen + idLen + insLen, numLen + idLen + insLen + textLen,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpannedTextView.setText(spannableText);
    }

    private String getQuestionInstructions() {
        String instructions = "";
        if (mQuestionInstruction != null) instructions = mQuestionInstruction.getText();
        if (!TextUtils.isEmpty(instructions) && !instructions.equals("null")) {
            instructions = styleTextWithHtml(instructions).toString();
        }
        return instructions;
    }

    private Spanned getQuestionText() {
        String text = mQuestion.getText();

//            if (question.isFollowUpQuestion()) {
//                String followUpText = question.getFollowingUpText(mSurveyFragment.getResponses(), getActivity());
//                if (followUpText != null) {
//                    text = followUpText;
//                }
//            } else if (question.hasRandomizedFactors()) {
//                text = question.getRandomizedText(mSurveyFragment.getResponses().get(question.getQuestionIdentifier()));
//            } else if (!TextUtils.isEmpty(question.getLoopSource())) {
//                String causeId = question.getQuestionIdentifier().split("_")[0];
//                Response response = mSurveyFragment.getResponses().get(causeId);
//                if (response == null || TextUtils.isEmpty(response.getText())) {
//                    text = question.getText();
//                } else {
//                    String responseText = "";
//                    String[] responses = response.getText().split(Response.LIST_DELIMITER, -1);
//                    Question causeQuestion = mSurveyFragment.getQuestions().get(causeId);
//                    if (causeQuestion.isSingleSelect()) {
//                        int index = Integer.parseInt(responses[question.getLoopNumber()]);
//                        responseText = mSurveyFragment.getOptions().get(causeQuestion).get(index).getText(mSurveyFragment.getInstrument());
//                    } else if (causeQuestion.hasMultipleResponses()) {
//                        if (Arrays.asList(responses).contains(Integer.toString(question.getLoopNumber()))) {
//                            responseText = mSurveyFragment.getOptions().get(causeQuestion).get(question.getLoopNumber()).getText(mSurveyFragment.getInstrument());
//                        }
//                    } else {
//                        if (question.getLoopNumber() < responses.length) {
//                            responseText = responses[question.getLoopNumber()]; //Keep empty values
//                        }
//                    }
//                    if (TextUtils.isEmpty(responseText)) {
//                        text = question.getText();
//                    } else {
//                        text = question.getText();
//                        int begin = text.indexOf("[");
//                        int last = text.indexOf("]");
//                        if (begin != -1 && last != -1 && begin < last) {
//                            text = text.replace(text.substring(begin, last + 1), responseText);
//                        } else {
//                            if (question.getTextToReplace() == null) {
//                                text = question.getText();
//                            } else {
//                                text = question.getText().replace(question.getTextToReplace(), responseText);
//                            }
//                        }
//                    }
//                }
//            } else {
//                text = question.getText();
//            }

        return styleTextWithHtml(text);
    }

    private void setDisplayInstructions() {
        if (mDisplayInstructions != null) {
            StringBuilder stringBuilder = new StringBuilder();
            List<Instruction> instructions = mDisplayInstructions.get(mQuestion.getNumberInInstrument());
            if (instructions != null) {
                for (Instruction instruction : instructions) {
                    stringBuilder.append(instruction.getText());
                }
            }
            if (stringBuilder.length() > 0) {
                mDisplayInstructionTextView.setVisibility(View.VISIBLE);
                mDisplayInstructionTextView.setText(styleTextWithHtml(stringBuilder.toString()));
            }
        }
    }

    private void setOptionSetInstructions() {
        if (mOptionSetInstruction != null && mOptionSetInstructionTextView != null) {
            mOptionSetInstructionTextView.setText(styleTextWithHtml(mOptionSetInstruction.getText()));
            mOptionSetInstructionTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setSpecialResponseView() {
        mSpecialResponses.removeAllViews();
        List<String> responses = new ArrayList<>();
        for (Option option : mSpecialOptions) {
            responses.add(option.getText());
        }

        for (String response : responses) {
            int responseId = responses.indexOf(response);
            final RadioButton button = new RadioButton(getContext());
            button.setText(response);
            button.setId(responseId);
//            button.setTypeface(mInstrument.getTypeFace(mContext));
            button.setTextColor(getContext().getResources().getColorStateList(R.color.states));

            mSpecialResponses.addView(button, responseId);
            final List<String> finalResponses = responses;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Special Response: " + finalResponses.get(v.getId()));
                    saveSpecialResponse(finalResponses.get(v.getId()));
                }
            });
        }

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSpecialResponses.clearCheck();
//                unSetResponse();
//                if (mQuestion.rankResponses()) {
//                    if (mRankLayout != null && mOptionsAdapter != null) {
//                        mOptionsAdapter.clear();
//                        mRankLayout.setVisibility(View.GONE);
//                    }
//                }
//                setResponse(Response.BLANK);
            }
        });
    }

    @Override
    protected void deserializeSpecialResponse() {
        if (getResponse() == null || TextUtils.isEmpty(getResponse().getSpecialResponse())) return;
        for (int i = 0; i < mSpecialResponses.getChildCount(); i++) {
            if (((RadioButton) mSpecialResponses.getChildAt(i)).getText().equals(getResponse().getSpecialResponse())) {
                mSpecialResponses.check(i);
            }
        }
    }

}