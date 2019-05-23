package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.app.Application;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.DisplayInstructionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.DisplayRelation;
import org.adaptlab.chpir.android.survey.entities.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.OptionSetRelation;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.EDIT_TEXT_DELAY;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class SingleQuestionViewHolder extends QuestionViewHolder {
    protected RadioGroup mSpecialResponses;
    private ResponseRepository mResponseRepository;
    private SurveyRepository mSurveyRepository;
    private Question mQuestion;
    private Survey mSurvey;
    private Response mResponse;
    private Instruction mQuestionInstruction;
    private Instruction mOptionSetInstruction;
    private List<Option> mOptions;
    private List<Option> mSpecialOptions;
    private List<DisplayInstructionRelation> mDisplayInstructions;
    private TextView mDisplayInstructionTextView;
    private TextView mSpannedTextView;
    private TextView mOptionSetInstructionTextView;
    private ViewGroup mQuestionComponent;
    private Button mClearButton;

    public SingleQuestionViewHolder(View itemView, Context context) {
        super(itemView, context);
        mResponseRepository = new ResponseRepository((Application) context.getApplicationContext());
        mSurveyRepository = new SurveyRepository((Application) context.getApplicationContext());
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
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Run on UI Thread
                        if (getContext() != null) {
                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setOtherResponse(s.toString());
                                }
                            });
                        }
                    }
                }, EDIT_TEXT_DELAY); // delay before saving to db
            }
        });

    }

    private void setOtherResponse(String response) {
        getResponse().setOtherResponse(response);
        saveResponse();
    }

    @Override
    public void setQuestionRelation(QuestionRelation questionRelation) {
        mQuestion = questionRelation.question;
        mSurvey = questionRelation.question.getSurvey();
        mResponse = questionRelation.question.getResponse();
        setQuestionInstruction(questionRelation);
        setOptionSetItems(questionRelation);
        setSpecialOptions(questionRelation);
        setDisplayInstructions(questionRelation);

        setSpannedText();
        setDisplayInstructionsText();
        setOptionSetInstructionsText();
        // Overridden by subclasses to place their graphical elements on the fragment.
        createQuestionComponent(mQuestionComponent);
        setSpecialResponseView();

        deserializeResponse();
    }

    private void setQuestionInstruction(QuestionRelation questionRelation) {
        if (questionRelation.instructions != null) {
            mQuestionInstruction = questionRelation.instructions.get(0);
        }
    }

    private void setOptionSetItems(QuestionRelation questionRelation) {
        if (questionRelation.optionSets != null) {
            mOptions = new ArrayList<>();
            OptionSetRelation optionSetRelation = questionRelation.optionSets.get(0);
            if (optionSetRelation != null) {
                if (optionSetRelation.instructions != null) {
                    mOptionSetInstruction = optionSetRelation.instructions.get(0);
                }
                if (optionSetRelation.optionSetOptions != null) {
                    for (OptionSetOptionRelation relation : optionSetRelation.optionSetOptions) {
                        if (relation.options != null) {
                            mOptions.add(relation.options.get(0));
                        }
                    }
                }
            }
        }
    }

    private void setSpecialOptions(QuestionRelation questionRelation) {
        if (questionRelation.specialOptionSets != null) {
            mSpecialOptions = new ArrayList<>();
            OptionSetRelation optionSetRelation = questionRelation.specialOptionSets.get(0);
            if (optionSetRelation != null && optionSetRelation.optionSetOptions != null) {
                for (OptionSetOptionRelation relation : optionSetRelation.optionSetOptions) {
                    if (relation.options != null) {
                        mSpecialOptions.add(relation.options.get(0));
                    }
                }
            }
        }
    }

    private void setDisplayInstructions(QuestionRelation questionRelation) {
        if (questionRelation.displays != null) {
            mDisplayInstructions = new ArrayList<>();
            DisplayRelation displayRelation = questionRelation.displays.get(0);
            if (displayRelation != null && displayRelation.displayInstructions != null) {
                mDisplayInstructions.addAll(displayRelation.displayInstructions);
            }
        }
    }

    private void deserializeResponse() {
        if (mResponse != null) {
            deserialize(mResponse.getText());
            deserializeSpecialResponse();
            deserializeOtherResponse(mResponse.getOtherResponse());
        }
    }

    void saveResponse() {
        mResponse.setText(serialize());
        mResponse.setSpecialResponse(BLANK);
        updateResponse();
    }

    private void updateResponse() {
        mResponse.setTimeEnded(new Date());
        mResponseRepository.update(mResponse);
//        mQuestion.setResponse(mResponse);
        mSurvey.setLastUpdated(new Date());
        mSurveyRepository.update(mSurvey);
        Log.i(TAG, "updateResponse");
    }

    void saveSpecialResponse(String specialResponse) {
        mResponse.setSpecialResponse(specialResponse);
        mResponse.setText(BLANK);
        mResponse.setOtherResponse(BLANK);
        updateResponse();
    }

    Response getResponse() {
        return mResponse;
    }

    public Survey getSurvey() {
        return mSurvey;
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

    private void setDisplayInstructionsText() {
        if (mDisplayInstructions != null) {
            StringBuilder stringBuilder = new StringBuilder();
            List<Instruction> instructions = new ArrayList<>();
            for (DisplayInstructionRelation relation : mDisplayInstructions) {
                if (relation.displayInstruction.getPosition() == mQuestion.getNumberInInstrument()) {
                    instructions.add(relation.instructions.get(0));
                }
            }
            for (Instruction instruction : instructions) {
                stringBuilder.append(instruction.getText());
            }
            if (stringBuilder.length() > 0 && mDisplayInstructionTextView != null) {
                mDisplayInstructionTextView.setVisibility(View.VISIBLE);
                mDisplayInstructionTextView.setText(styleTextWithHtml(stringBuilder.toString()));
            }
        }
    }

    private void setOptionSetInstructionsText() {
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
            button.setTextColor(getContext().getResources().getColorStateList(R.color.states));

            mSpecialResponses.addView(button, responseId);
            final List<String> finalResponses = responses;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

    @Override
    protected void deserializeOtherResponse(String otherResponse) {
        // Implemented by WriteOther subclass
    }

}