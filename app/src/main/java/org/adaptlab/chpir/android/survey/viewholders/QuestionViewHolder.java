package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.QuestionRelationAdapter;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayViewModel;
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
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtmlWhitelist;

public abstract class QuestionViewHolder extends RecyclerView.ViewHolder {
    public final String TAG = this.getClass().getName();
    private RadioGroup mSpecialResponseRadioGroup;
    private Context mContext;
    private OnResponseSelectedListener mListener;
    private SurveyViewModel mSurveyViewModel;
    private DisplayViewModel mDisplayViewModel;
    private ResponseRepository mResponseRepository;

    private QuestionRelation mQuestionRelation;
    private Survey mSurvey;
    private Response mResponse;
    private Instruction mOptionSetInstruction;
    private List<OptionRelation> mOptionRelations;
    private List<OptionRelation> mSpecialOptionRelations;
    private List<OptionRelation> mCarryForwardOptionRelations;
    private HashMap<String, Instruction> mOptionInstructions;
    private QuestionRelationAdapter mAdapter;

    private TextView mNumberTextView;
    private TextView mBeforeTextInstructionTextView;
    private TextView mSpannedTextView;
    private ImageButton mPopUpButton;
    private TextView mAfterTextInstructionTextView;

    private TextView mOptionSetInstructionTextView;
    private ViewGroup mQuestionComponent;
    private Button mClearButton;
    private boolean mDeserializing = false;

    public QuestionViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView);
        mContext = context;
        mListener = listener;
        mResponseRepository = new ResponseRepository((Application) context.getApplicationContext());

        mNumberTextView = itemView.findViewById(R.id.numberTextView);
        mBeforeTextInstructionTextView = itemView.findViewById(R.id.beforeTextInstructions);
        mSpannedTextView = itemView.findViewById(R.id.spannedTextView);
        mPopUpButton = itemView.findViewById(R.id.popupInstructions);
        mAfterTextInstructionTextView = itemView.findViewById(R.id.afterTextInstructions);

        mOptionSetInstructionTextView = itemView.findViewById(R.id.optionSetInstructions);
        mQuestionComponent = itemView.findViewById(R.id.response_component);
        mSpecialResponseRadioGroup = itemView.findViewById(R.id.specialResponseButtons);
        mClearButton = itemView.findViewById(R.id.clearResponsesButton);
    }

    public QuestionViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;
        mResponseRepository = new ResponseRepository((Application) context.getApplicationContext());
    }

    public void setRelations(QuestionRelation questionRelation) {
        mQuestionRelation = questionRelation;
        setOptionSetItems(questionRelation);
        setSpecialOptions(questionRelation);
        setCarryForwardOptions(questionRelation);
        setQuestionTextComponents();
        setOptionSetInstructionsText();
        // Overridden by subclasses to place their graphical elements on the fragment.
        createQuestionComponent(mQuestionComponent);
        setSpecialResponseView();
    }

    SurveyViewModel getSurveyViewModel() {
        return mSurveyViewModel;
    }

    public void setSurveyViewModel(SurveyViewModel model) {
        mSurveyViewModel = model;
        mSurvey = mSurveyViewModel.getSurvey();
    }

    public void setDisplayViewModel(DisplayViewModel viewModel) {
        mDisplayViewModel = viewModel;
        mResponse = mDisplayViewModel.getResponse(mQuestionRelation.question.getQuestionIdentifier());
        mDeserializing = true;
        deserializeResponse();
        mDeserializing = false;
    }

    QuestionRelationAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(QuestionRelationAdapter adapter) {
        mAdapter = adapter;
    }

    void setQuestionRelation(QuestionRelation relation) {
        mQuestionRelation = relation;
    }

    Question getQuestion() {
        return mQuestionRelation.question;
    }

    List<OptionRelation> getOptionRelations() {
        if (mQuestionRelation.question.isCarryForward()) {
            return mCarryForwardOptionRelations;
        } else {
            return mOptionRelations;
        }
    }

    List<OptionRelation> getSpecialOptionRelations() {
        return mSpecialOptionRelations;
    }

    private Response getCarryForwardResponse() {
        return getSurveyViewModel().getResponses().get(mQuestionRelation.question.getCarryForwardIdentifier());
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

    void setOptionText(String text, CompoundButton button) {
        button.setText(styleTextWithHtmlWhitelist(text));
        button.setTextColor(getContext().getResources().getColorStateList(R.color.states));
    }

    void setOptionPopUpInstruction(ViewGroup questionComponent, View view, int viewId, OptionRelation optionRelation) {
        final Instruction optionInstruction = getOptionInstruction(optionRelation.option.getIdentifier());
        if (optionInstruction != null) {
            LinearLayout optionLayout = new LinearLayout(getContext());
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            optionLayout.setLayoutParams(layoutParams);
            optionLayout.setOrientation(LinearLayout.HORIZONTAL);
            optionLayout.addView(view);
            ImageButton imageButton = new ImageButton(getContext());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                imageButton.setBackgroundColor(getContext().getColor(R.color.white));
                imageButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_info_outline_blue_24dp));
            }
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopUpInstruction(styleTextWithHtml(optionInstruction.getText()).toString());
                }
            });
            optionLayout.addView(imageButton);
            questionComponent.addView(optionLayout, viewId);
        } else {
            questionComponent.addView(view, viewId);
        }
    }

    boolean isDeserializing() {
        return mDeserializing;
    }

    RadioGroup getSpecialResponses() {
        return mSpecialResponseRadioGroup;
    }

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    protected abstract void unSetResponse();

    /**
     * @param otherText An EditText injected from a subclass i.e a write other subclass
     */
    void addOtherResponseView(final EditText otherText) {
        otherText.setHint(R.string.other_specify_edittext);
        otherText.setEnabled(false);
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
                if (!mDeserializing) {
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
        mResponse.setOtherResponse(response);
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
                if (!relation.optionSetOption.isDeleted() && relation.options.size() > 0) {
                    mOptionRelations.add(relation.options.get(0));
                    if (relation.instructions.size() > 0) {
                        mOptionInstructions.put(relation.options.get(0).option.getIdentifier(), relation.instructions.get(0));
                    }
                }
            }
        }
    }

    private Instruction getOptionInstruction(String optionIdentifier) {
        return mOptionInstructions.get(optionIdentifier);
    }

    private void deserializeResponse() {
        deserialize(mResponse.getText());
        deserializeSpecialResponse();
        deserializeOtherResponse(mResponse.getOtherResponse());
    }

    void saveResponse() {
        mResponse.setText(serialize());
        clearSpecialResponse();
        updateResponse();
    }

    void updateResponse() {
        updateSkipData();
        mResponse.setTimeEnded(new Date());
        mResponse.setIdentifiesSurvey(mQuestionRelation.question.isIdentifiesSurvey());
        mResponseRepository.update(mResponse);
    }

    private void updateSkipData() {
        Option selectedOption = null;
        String enteredValue = null;
        List<Option> selectedOptions = new ArrayList<>();
        if (!TextUtils.isEmpty(mResponse.getText())) {
            if (mQuestionRelation.question.isSingleResponse()) {
                selectedOption = getSelectedOption(mResponse.getText());
            } else if (mQuestionRelation.question.getQuestionType().equals(Question.INTEGER)) {
                enteredValue = mResponse.getText();
            } else if (mQuestionRelation.question.isMultipleResponse()) {
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

    private void setQuestionTextComponents() {
        if (mQuestionRelation.question.getQuestionType().equals(Question.INSTRUCTIONS)) {
            mNumberTextView.setVisibility(View.GONE);
            mBeforeTextInstructionTextView.setVisibility(View.GONE);
            mAfterTextInstructionTextView.setVisibility(View.GONE);
        } else {
            setQuestionNumberView();
            setBeforeTextInstructionView();
            setAfterTextInstructionView();
        }
        setQuestionText();
    }

    private void setQuestionNumberView() {
        if (mNumberTextView == null) return;
        String text = mQuestionRelation.question.getPosition() + ") " + mQuestionRelation.question.getQuestionIdentifier();
        mNumberTextView.setText(text);
    }

    private void setBeforeTextInstructionView() {
        if (mBeforeTextInstructionTextView == null) return;
        if (mQuestionRelation.instructions.size() == 0) {
            mBeforeTextInstructionTextView.setVisibility(View.GONE);
        } else {
            String instructions = getQuestionInstructions();
            mBeforeTextInstructionTextView.setText(instructions);
        }
    }

    private void setQuestionText() {
        if (mSpannedTextView == null) return;
        mSpannedTextView.setText(getQuestionText());
        if (mQuestionRelation.question.getPopUpInstructionId() != null && mPopUpButton != null) {
            mPopUpButton.setVisibility(View.VISIBLE);
            mPopUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopUpInstruction(getPopUpInstructions());
                }
            });
        }
    }

    private void setAfterTextInstructionView() {
        if (mAfterTextInstructionTextView == null) return;
        if (mQuestionRelation.afterTextInstructions.size() == 0) {
            mAfterTextInstructionTextView.setVisibility(View.GONE);
        } else {
            mAfterTextInstructionTextView.setText(getAfterTextInstructions());
        }
    }

    private String getAfterTextInstructions() {
        String instructions = "";
        if (mQuestionRelation.afterTextInstructions.size() > 0) {
            instructions = TranslationUtil.getText(mQuestionRelation.afterTextInstructions.get(0).instruction,
                    mQuestionRelation.afterTextInstructions.get(0).translations, mSurveyViewModel);
        }
        return styleTextWithHtmlWhitelist(instructions).toString();
    }

    private void showPopUpInstruction(String instructions) {
        new AlertDialog.Builder(getContext())
                .setMessage(instructions)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

    }

    private String getPopUpInstructions() {
        String instructions = "";
        if (mQuestionRelation.popUpInstructions.size() > 0) {
            instructions = TranslationUtil.getText(mQuestionRelation.popUpInstructions.get(0).instruction,
                    mQuestionRelation.popUpInstructions.get(0).translations, mSurveyViewModel);
        }
        return styleTextWithHtml(instructions).toString();
    }

    String getQuestionInstructions() {
        String instructions = "";
        if (mQuestionRelation.instructions.size() > 0) {
            instructions = TranslationUtil.getText(mQuestionRelation.instructions.get(0).instruction,
                    mQuestionRelation.instructions.get(0).translations, mSurveyViewModel);
        }
        return styleTextWithHtmlWhitelist(instructions).toString();
    }

    Spanned getQuestionText() {
        String text = TranslationUtil.getText(mQuestionRelation.question, mQuestionRelation.translations, mSurveyViewModel);
        if (!TextUtils.isEmpty(mQuestionRelation.question.getLoopSource())) {
            String causeId = mQuestionRelation.question.getQuestionIdentifier().split("_")[0];
            Response response = getSurveyViewModel().getResponses().get(causeId);
            if (response != null && !TextUtils.isEmpty(response.getText())) {
                String responseText = "";
                String[] responses = response.getText().split(COMMA, -1);
                Question causeQuestion = getSurveyViewModel().getQuestionsMap().get(causeId);
                if (causeQuestion.isSingleResponse()) {
                    int index = Integer.parseInt(responses[mQuestionRelation.question.getLoopNumber()]);
                    responseText = mOptionRelations.get(index).option.getText();
                } else if (causeQuestion.isMultipleResponse()) {
                    if (Arrays.asList(responses).contains(Integer.toString(mQuestionRelation.question.getLoopNumber()))) {
                        if (mQuestionRelation.question.getLoopNumber() < mOptionRelations.size())
                            responseText = mOptionRelations.get(mQuestionRelation.question.getLoopNumber()).option.getText();
                    }
                } else {
                    if (mQuestionRelation.question.getLoopNumber() < responses.length) {
                        responseText = responses[mQuestionRelation.question.getLoopNumber()]; //Keep empty values
                    }
                }
                if (!TextUtils.isEmpty(responseText)) {
                    int begin = text.indexOf("[");
                    int last = text.indexOf("]");
                    if (begin != -1 && last != -1 && begin < last) {
                        text = text.replace(text.substring(begin, last + 1), responseText);
                    } else {
                        if (!TextUtils.isEmpty(mQuestionRelation.question.getTextToReplace())) {
                            text = mQuestionRelation.question.getText().replace(mQuestionRelation.question.getTextToReplace(), responseText);
                        }
                    }
                }
            }
        }
        return styleTextWithHtml(text);
    }

    private void setOptionSetInstructionsText() {
        if (mOptionSetInstruction != null && mOptionSetInstructionTextView != null) {
            mOptionSetInstructionTextView.setText(styleTextWithHtml(mOptionSetInstruction.getText()));
            mOptionSetInstructionTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setSpecialResponseView() {
        mSpecialResponseRadioGroup.removeAllViews();
        if (mSpecialOptionRelations != null) {
            for (OptionRelation optionRelation : mSpecialOptionRelations) {
                String text = TranslationUtil.getText(optionRelation.option, optionRelation.translations, mSurveyViewModel);
                int responseId = mSpecialOptionRelations.indexOf(optionRelation);
                final RadioButton button = new RadioButton(getContext());
                button.setId(responseId);
                setOptionText(text, button);

                mSpecialResponseRadioGroup.addView(button, responseId);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveSpecialResponse(v.getId());
                    }
                });
            }
        }

        if (mQuestionRelation.question.getQuestionType().equals(Question.INSTRUCTIONS)) {
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

    void saveSpecialResponse(int index) {
        OptionRelation optionRelation = mSpecialOptionRelations.get(index);
        mResponse.setSpecialResponse(optionRelation.option.getText());
        clearNonSpecialResponse();
        unSetResponse();
        updateResponse();
    }

    void clearResponse() {
        clearNonSpecialResponse();
        clearSpecialResponse();
        unSetResponse();
    }

    private void clearSpecialResponse() {
        mResponse.setSpecialResponse(BLANK);
        mSpecialResponseRadioGroup.clearCheck();
    }

    private void clearNonSpecialResponse() {
        mResponse.setText(BLANK);
        mResponse.setOtherResponse(BLANK);
        mResponse.setOtherText(BLANK);
    }

    protected void deserializeSpecialResponse() {
        if (mResponse == null || TextUtils.isEmpty(mResponse.getSpecialResponse())) return;
        for (int k = 0; k < mSpecialOptionRelations.size(); k++) {
            if (mSpecialOptionRelations.get(k).option.getText().equals(mResponse.getSpecialResponse())) {
                mSpecialResponseRadioGroup.check(k);
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
                    if (relation.options.size() > 0) {
                        mSpecialOptionRelations.add(relation.options.get(0));
                    }
                }
            }
        }
    }

    private void setCarryForwardOptions(QuestionRelation questionRelation) {
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