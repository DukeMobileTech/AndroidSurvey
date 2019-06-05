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
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.DisplayInstructionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.DisplayRelation;
import org.adaptlab.chpir.android.survey.entities.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.OptionSetRelation;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.ResponseRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.EDIT_TEXT_DELAY;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class SingleQuestionViewHolder extends QuestionViewHolder {
    protected RadioGroup mSpecialResponses;

    private ResponseRepository mResponseRepository;
    private SurveyRepository mSurveyRepository;

    private QuestionRelation mQuestionRelation;
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

    private boolean mDeserialization = false;

    public SingleQuestionViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
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
    public void setQuestionRelation(ResponseRelation responseRelation, QuestionRelation questionRelation) {
        mQuestionRelation = questionRelation;
        mQuestion = questionRelation.question;
        mSurvey = responseRelation.surveys.get(0);
        mResponse = responseRelation.response;
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
        mDeserialization = true;
        deserializeResponse();
        mDeserialization = false;
    }

    @Override
    public Question getQuestion() {
        return mQuestion;
    }

    public List<Option> getOptions() {
        return mOptions;
    }

    boolean isDeserialization() {
        return mDeserialization;
    }

    RadioGroup getSpecialResponses() {
        return mSpecialResponses;
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
                if (!mDeserialization) {
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
            }
        });
    }

    private void setOtherResponse(String response) {
        getResponse().setOtherResponse(response);
        saveResponse();
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
        updateSkipData();
        mSurvey.setLastUpdated(new Date());
        mSurveyRepository.update(mSurvey);
        mResponse.setTimeEnded(new Date());
        mResponseRepository.update(mResponse);
    }

    private void updateSkipData() {
        Option selectedOption = null;
        String enteredValue = null;
        List<Option> selectedOptions = new ArrayList<>();
        if (!TextUtils.isEmpty(mResponse.getText())) {
            if (mQuestion.isSingleResponse()) {
                selectedOption = getSelectedOption(mResponse.getText());
            } else if (mQuestion.getQuestionType().equals(Question.INTEGER)) {
                enteredValue = mResponse.getText();
            } else if (mQuestion.isMultipleResponse()) {
                String[] responses = mResponse.getText().split(COMMA);
                if (responses.length == 1) {
                    selectedOption = getSelectedOption(responses[0]);
                } else {
                    for (String str : responses) {
                        Option option = getSelectedOption(str);
                        if (option != null) selectedOptions.add(option);
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(mResponse.getSpecialResponse())) {
            for (Option option : mSpecialOptions) {
                if (option.getText().equals(mResponse.getSpecialResponse())) {
                    selectedOption = option;
                    break;
                }
            }
        }
        String nextQuestion = null;
        if (selectedOptions.isEmpty()) {
            if (selectedOption != null && enteredValue == null) {
                nextQuestion = getNextQuestionIdentifier(selectedOption);
            } else if (selectedOption == null && enteredValue != null) {
                nextQuestion = getNextQuestionIdentifier(enteredValue);
            }
        } else {
            nextQuestion = getNextQuestionIdentifier(selectedOptions);
        }

        getListener().onResponseSelected(mQuestionRelation, selectedOption, selectedOptions, enteredValue, nextQuestion);
    }

    private String getNextQuestionIdentifier(Option option) {
        if (!TextUtils.isEmpty(mResponse.getText())) {
            NextQuestion nextQuestion = getNextQuestionForOption(option);
            if (nextQuestion != null) {
                return nextQuestion.getNextQuestionString();
            }
        }
        if (!TextUtils.isEmpty(mResponse.getSpecialResponse())) {
            NextQuestion nextQuestion = getNextQuestionForOption(option);
            if (nextQuestion != null) {
                return nextQuestion.getNextQuestionString();
            }
        }
        return null;
    }

    private String getNextQuestionIdentifier(String value) {
        if (TextUtils.isEmpty(value)) return null;
        NextQuestion nextQuestion = getNextQuestionForValue(value);
        if (nextQuestion != null) {
            return nextQuestion.getNextQuestionString();
        }
        return null;
    }

    private String getNextQuestionIdentifier(List<Option> options) {
        HashSet<String> nextQuestions = new HashSet<>();
        for (Option option : options) {
            NextQuestion nextQuestion = getNextQuestionForOption(option);
            if (nextQuestion != null && !TextUtils.isEmpty(nextQuestion.getNextQuestionIdentifier())) {
                nextQuestions.add(nextQuestion.getNextQuestionIdentifier());
            }
        }
        if (nextQuestions.size() == 1) {
            return nextQuestions.iterator().next();
        } else {
            return null;
        }
    }

    private NextQuestion getNextQuestionForOption(Option option) {
        List<NextQuestion> nextQuestions = mQuestionRelation.nextQuestions;
        if (nextQuestions == null) return null;
        for (NextQuestion nextQuestion : nextQuestions) {
            if (nextQuestion.getOptionIdentifier().equals(option.getIdentifier())) {
                return nextQuestion;
            }
        }
        return null;
    }

    private NextQuestion getNextQuestionForValue(String value) {
        List<NextQuestion> nextQuestions = mQuestionRelation.nextQuestions;
        if (nextQuestions == null) return null;
        for (NextQuestion nextQuestion : nextQuestions) {
            if (nextQuestion.getValue().equals(value)) {
                return nextQuestion;
            }
        }
        return null;
    }

    private Option getSelectedOption(String responseText) {
        int responseIndex = Integer.parseInt(responseText);
        if (responseIndex < mOptions.size()) {
            return mOptions.get(responseIndex);
        } else {
            return null;
        }
    }

    private void saveSpecialResponse(String specialResponse) {
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
//                    } else if (causeQuestion.isMultipleResponse()) {
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
                mResponse.setText(BLANK);
                mResponse.setOtherResponse(BLANK);
                mResponse.setSpecialResponse(BLANK);
                updateResponse();
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