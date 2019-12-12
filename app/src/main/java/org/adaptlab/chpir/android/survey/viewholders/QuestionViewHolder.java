package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.ResponseRelationAdapter;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.DisplayInstructionRelation;
import org.adaptlab.chpir.android.survey.relations.DisplayRelation;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.EDIT_TEXT_DELAY;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class QuestionViewHolder extends RecyclerView.ViewHolder {
    public final String TAG = this.getClass().getName();
    private RadioGroup mSpecialResponses;
    private Context mContext;
    private OnResponseSelectedListener mListener;
    private SurveyViewModel mSurveyViewModel;
    private ResponseRepository mResponseRepository;

    private QuestionRelation mQuestionRelation;
    private Question mQuestion;
    private Survey mSurvey;
    private Response mResponse;
    private Instruction mOptionSetInstruction;
    private List<OptionRelation> mOptionRelations;
    private List<OptionRelation> mSpecialOptionRelations;
    private List<OptionRelation> mCarryForwardOptionRelations;
    private List<DisplayInstructionRelation> mDisplayInstructions;
    private HashMap<String, Instruction> mOptionInstructions;
    private ResponseRelationAdapter mAdapter;

    private TextView mDisplayInstructionTextView;
    private TextView mSpannedTextView;
    private TextView mOptionSetInstructionTextView;
    private ViewGroup mQuestionComponent;
    private Button mClearButton;
    private ImageButton mPopUpButton;

    private boolean mDeserialization = false;

    public QuestionViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView);
        mContext = context;
        mListener = listener;
        mResponseRepository = new ResponseRepository((Application) context.getApplicationContext());
        mDisplayInstructionTextView = itemView.findViewById(R.id.displayInstructions);
        mSpannedTextView = itemView.findViewById(R.id.spannedTextView);
        mOptionSetInstructionTextView = itemView.findViewById(R.id.optionSetInstructions);
        mQuestionComponent = itemView.findViewById(R.id.response_component);
        mSpecialResponses = itemView.findViewById(R.id.specialResponseButtons);
        mClearButton = itemView.findViewById(R.id.clearResponsesButton);
        mPopUpButton = itemView.findViewById(R.id.popupInstructions);
    }

    public QuestionViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;
        mResponseRepository = new ResponseRepository((Application) context.getApplicationContext());
    }

    public void setRelations(QuestionRelation questionRelation) {
//        Log.i(TAG, "setRelations: " + questionRelation.question.toString());
        mQuestionRelation = questionRelation;
        mQuestion = questionRelation.question;
        mResponse = questionRelation.response;
        setOptionSetItems(questionRelation);
        setSpecialOptions(questionRelation);
        setCarryForwardOptions(questionRelation);
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

    SurveyViewModel getSurveyViewModel() {
        return mSurveyViewModel;
    }

    public void setSurveyViewModel(SurveyViewModel model) {
        mSurveyViewModel = model;
        mSurvey = mSurveyViewModel.getSurvey();
    }

    ResponseRelationAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(ResponseRelationAdapter adapter) {
        mAdapter = adapter;
    }

    void setQuestionRelation(QuestionRelation relation) {
        mQuestionRelation = relation;
    }

    Question getQuestion() {
        return mQuestion;
    }

    void setQuestion(Question mQuestion) {
        this.mQuestion = mQuestion;
    }

    List<OptionRelation> getOptionRelations() {
        if (mQuestion.isCarryForward()) {
            return mCarryForwardOptionRelations;
        } else {
            return mOptionRelations;
        }
    }

    List<OptionRelation> getSpecialOptionRelations() {
        return mSpecialOptionRelations;
    }

    private Response getCarryForwardResponse() {
        return getSurveyViewModel().getResponses().get(mQuestion.getCarryForwardIdentifier());
    }

    void toggleCarryForward(View view, int optionId) {
        if (getQuestion().isCarryForward()) {
            ArrayList<Integer> responseIndices = new ArrayList<>();
            String[] listOfIndices = getCarryForwardResponse().getText().split(COMMA);
            for (String index : listOfIndices) {
                if (!index.equals("")) {
                    responseIndices.add(Integer.parseInt(index));
                }
            }
            if (!responseIndices.contains(optionId)) {
                view.setEnabled(false);
                if (view instanceof EditText) {
                    ((EditText) view).setHint(null);
                }
                if (view instanceof TextView) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ((TextView) view).setTextColor(getContext().getColor(R.color.secondary_text));
                    }
                }
            }
        }
    }

    void setOptionTextColor(CompoundButton button) {
        button.setTextColor(getContext().getResources().getColorStateList(R.color.states));
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

    void setOptionSetItems(QuestionRelation questionRelation) {
        mOptionRelations = new ArrayList<>();
        mOptionInstructions = new HashMap<>();
        if (questionRelation.optionSets.size() > 0) {
            OptionSetRelation optionSetRelation = questionRelation.optionSets.get(0);
            if (optionSetRelation.instructions.size() > 0) {
                mOptionSetInstruction = optionSetRelation.instructions.get(0);
            }
            for (OptionSetOptionRelation relation : optionSetRelation.optionSetOptions) {
                if (relation.options.size() > 0) {
                    mOptionRelations.add(relation.options.get(0));
                    if (relation.instructions.size() > 0) {
                        mOptionInstructions.put(relation.options.get(0).option.getIdentifier(), relation.instructions.get(0));
                    }
                }
            }
        }
    }

    private void setDisplayInstructions(QuestionRelation questionRelation) {
        mDisplayInstructions = new ArrayList<>();
        if (questionRelation.displays != null) {
            DisplayRelation displayRelation = questionRelation.displays.get(0);
            if (displayRelation != null && displayRelation.displayInstructions != null) {
                mDisplayInstructions.addAll(displayRelation.displayInstructions);
            }
        }
    }

    void deserializeResponse() {
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

    void updateResponse() {
        updateSkipData();
        mResponse.setTimeEnded(new Date());
        mResponse.setIdentifiesSurvey(mQuestion.isIdentifiesSurvey());
        mResponseRepository.update(mResponse);
    }

    void updateSkipData() {
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
            for (OptionRelation optionRelation : mSpecialOptionRelations) {
                if (optionRelation.option.getText().equals(mResponse.getSpecialResponse())) {
                    selectedOption = optionRelation.option;
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

        getListener().onResponseSelected(mQuestionRelation, selectedOption, selectedOptions, enteredValue, nextQuestion, mResponse.getText());
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
        if (responseIndex < mOptionRelations.size()) {
            return mOptionRelations.get(responseIndex).option;
        } else {
            return null;
        }
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

    void setResponse(Response mResponse) {
        this.mResponse = mResponse;
    }

    public Survey getSurvey() {
        return mSurvey;
    }

    void setSurvey(Survey mSurvey) {
        this.mSurvey = mSurvey;
    }

    private void setSpannedText() {
        if (mSpannedTextView == null) return;
        String number = mQuestion.getNumberInInstrument() + ": ";
        int numLen = number.length();
        String identifier = mQuestion.getQuestionIdentifier() + "\n";
        int idLen = identifier.length();
        String instructions = getQuestionInstructions();
        int insLen = instructions.length();
        Spanned text = getQuestionText();
        int textLen = text.length();
        SpannableString spannableText;
        if (mQuestion.isPopUpInstruction()) {
            spannableText = new SpannableString(number + identifier + text);
            spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                    0, numLen + idLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue)),
                    numLen + idLen, numLen + idLen + textLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (mQuestion.isInstructionAfterText()) {
            spannableText = new SpannableString(number + identifier + text + instructions);
            spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                    0, numLen + idLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                    numLen + idLen + textLen, numLen + idLen + textLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new StyleSpan(Typeface.ITALIC), numLen + idLen + textLen,
                    numLen + idLen + textLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue)),
                    numLen + idLen, numLen + idLen + textLen,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableText = new SpannableString(number + identifier + instructions + text);
            spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                    0, numLen + idLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new StyleSpan(Typeface.ITALIC), numLen + idLen,
                    numLen + idLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue)),
                    numLen + idLen + insLen, numLen + idLen + insLen + textLen,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, numLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mQuestion.isPopUpInstruction()) {
            spannableText.setSpan(new RelativeSizeSpan(1.2f), numLen + idLen,
                    numLen + idLen + textLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mPopUpButton.setVisibility(View.VISIBLE);
            mPopUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopUpInstruction();
                }
            });
        } else {
            spannableText.setSpan(new RelativeSizeSpan(1.2f), numLen + idLen + insLen,
                    numLen + idLen + insLen + textLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mSpannedTextView.setText(spannableText);
    }

    private void showPopUpInstruction() {
        String instructions = getQuestionInstructions();
        new AlertDialog.Builder(getContext())
                .setMessage(instructions)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();

    }

    String getQuestionInstructions() {
        String instructions = "";
        if (mQuestionRelation.instructions.size() > 0)
            instructions = mQuestionRelation.instructions.get(0).getText();
        return styleTextWithHtml(instructions).toString();
    }

    Spanned getQuestionText() {
        String text = TranslationUtil.getText(mQuestion, mQuestionRelation.translations, mSurveyViewModel);
        if (!TextUtils.isEmpty(mQuestion.getLoopSource())) {
            String causeId = mQuestion.getQuestionIdentifier().split("_")[0];
            Response response = getSurveyViewModel().getResponses().get(causeId);
            if (response != null && !TextUtils.isEmpty(response.getText())) {
                String responseText = "";
                String[] responses = response.getText().split(COMMA, -1);
                Question causeQuestion = getSurveyViewModel().getQuestionsMap().get(causeId);
                if (causeQuestion.isSingleResponse()) {
                    int index = Integer.parseInt(responses[mQuestion.getLoopNumber()]);
                    responseText = mOptionRelations.get(index).option.getText();
                } else if (causeQuestion.isMultipleResponse()) {
                    if (Arrays.asList(responses).contains(Integer.toString(mQuestion.getLoopNumber()))) {
                        if (mQuestion.getLoopNumber() < mOptionRelations.size())
                            responseText = mOptionRelations.get(mQuestion.getLoopNumber()).option.getText();
                    }
                } else {
                    if (mQuestion.getLoopNumber() < responses.length) {
                        responseText = responses[mQuestion.getLoopNumber()]; //Keep empty values
                    }
                }
                if (!TextUtils.isEmpty(responseText)) {
                    int begin = text.indexOf("[");
                    int last = text.indexOf("]");
                    if (begin != -1 && last != -1 && begin < last) {
                        text = text.replace(text.substring(begin, last + 1), responseText);
                    } else {
                        if (!TextUtils.isEmpty(mQuestion.getTextToReplace())) {
                            text = mQuestion.getText().replace(mQuestion.getTextToReplace(), responseText);
                        }
                    }
                }
            }
        }
        return styleTextWithHtml(text);
    }

    private void setDisplayInstructionsText() {
        if (mDisplayInstructions != null) {
            StringBuilder stringBuilder = new StringBuilder();
            List<Instruction> instructions = new ArrayList<>();
            for (DisplayInstructionRelation relation : mDisplayInstructions) {
                if (relation.instructions.size() > 0) {
                    if (relation.displayInstruction.getPosition() == mQuestion.getNumberInInstrument()) {
                        instructions.add(relation.instructions.get(0));
                    }
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
        if (mSpecialOptionRelations != null) {
            for (OptionRelation optionRelation : mSpecialOptionRelations) {
                responses.add(optionRelation.option.getText());
            }
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

        if (mQuestion.getQuestionType().equals(Question.INSTRUCTIONS)) {
            mClearButton.setVisibility(View.GONE);
        } else {
            mClearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearResponse();
                    updateResponse();
                }
            });
        }
    }

    void clearResponse() {
        mResponse.setText(BLANK);
        mResponse.setOtherResponse(BLANK);
        mResponse.setSpecialResponse(BLANK);
    }

    protected void deserializeSpecialResponse() {
        if (getResponse() == null || TextUtils.isEmpty(getResponse().getSpecialResponse())) return;
        for (int i = 0; i < mSpecialResponses.getChildCount(); i++) {
            if (((RadioButton) mSpecialResponses.getChildAt(i)).getText().equals(getResponse().getSpecialResponse())) {
                mSpecialResponses.check(i);
            }
        }
    }

    public Context getContext() {
        return mContext;
    }

    protected abstract void deserialize(String responseText);

    protected void deserializeOtherResponse(String otherResponse) {
    }

    protected abstract String serialize();

    public OnResponseSelectedListener getListener() {
        return mListener;
    }

    void setOnResponseSelectedListener(OnResponseSelectedListener listener) {
        mListener = listener;
    }

    void setSpecialOptions(QuestionRelation questionRelation) {
        mSpecialOptionRelations = new ArrayList<>();
        if (questionRelation.specialOptionSets.size() > 0) {
            OptionSetRelation optionSetRelation = questionRelation.specialOptionSets.get(0);
            if (optionSetRelation != null && optionSetRelation.optionSetOptions != null) {
                for (OptionSetOptionRelation relation : optionSetRelation.optionSetOptions) {
                    if (relation.options != null) {
                        mSpecialOptionRelations.add(relation.options.get(0));
                    }
                }
            }
        }
    }

    void setCarryForwardOptions(QuestionRelation questionRelation) {
        mCarryForwardOptionRelations = new ArrayList<>();
        if (questionRelation.carryForwardOptionSets.size() > 0) {
            OptionSetRelation optionSetRelation = questionRelation.carryForwardOptionSets.get(0);
            if (optionSetRelation != null && optionSetRelation.optionSetOptions != null) {
                for (OptionSetOptionRelation relation : optionSetRelation.optionSetOptions) {
                    if (relation.options.size() > 0) {
                        mCarryForwardOptionRelations.add(relation.options.get(0));
                    }
                }
            }
        }
    }

    public interface OnResponseSelectedListener {
        void onResponseSelected(QuestionRelation questionRelation, Option selectedOption,
                                List<Option> selectedOptions, String enteredValue,
                                String nextQuestion, String response);
    }

}