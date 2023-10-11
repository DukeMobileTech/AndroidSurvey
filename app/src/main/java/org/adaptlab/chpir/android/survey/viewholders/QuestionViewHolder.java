package org.adaptlab.chpir.android.survey.viewholders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.ChoiceDiagramAdapter;
import org.adaptlab.chpir.android.survey.adapters.QuestionDiagramAdapter;
import org.adaptlab.chpir.android.survey.adapters.QuestionRelationAdapter;
import org.adaptlab.chpir.android.survey.entities.ConditionSkip;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.CollageRelation;
import org.adaptlab.chpir.android.survey.relations.InstructionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionCollageRelation;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionCollageRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.ConstantUtils;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;
import org.adaptlab.chpir.android.survey.verhoeff.ParticipantIdValidator;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.ANY_NOT_SELECTED;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.EDIT_TEXT_DELAY;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.getStringArray;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtmlWhitelist;
import static org.adaptlab.chpir.android.survey.utils.SortUtils.sortedOptionSetOptionRelations;

public abstract class QuestionViewHolder extends RecyclerView.ViewHolder {
    public final String TAG = this.getClass().getName();

    private final Context mContext;
    private final ResponseRepository mResponseRepository;
    private QuestionRelationAdapter mAdapter;
    private OnResponseSelectedListener mListener;
    private SurveyViewModel mSurveyViewModel;
    private DisplayViewModel mDisplayViewModel;
    private QuestionRelation mQuestionRelation;
    private Survey mSurvey;
    private Response mResponse;
    private InstructionRelation mOptionSetInstruction;
    private List<OptionRelation> mOptionRelations;
    private List<OptionRelation> mSpecialOptionRelations;
    private List<OptionRelation> mCarryForwardOptionRelations;
    private HashMap<String, InstructionRelation> mOptionInstructions;
    private List<Long> mTextEntryOptionIds;
    private LongSparseArray<OptionSetOptionRelation> mOptionSetOptionRelations;

    private TextView mNumberTextView;
    private TextView mBeforeTextInstructionTextView;
    private TextView mSpannedTextView;
    private TextView mAfterTextInstructionTextView;
    private TextView mOptionSetInstructionTextView;
    private TextView mValidationTextView;
    private ViewGroup mResponseComponent;
    private ViewGroup mAudioComponent;
    private Button mClearButton;
    private RadioGroup mSpecialResponseRadioGroup;
    private LinearLayout mGridViewLayout;
    private ConstraintLayout mConstraintLayout;
    private boolean mDeserializing = false;

    public QuestionViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView);
        mContext = context;
        mListener = listener;
        mResponseRepository = new ResponseRepository((Application) context.getApplicationContext());

        mConstraintLayout = itemView.findViewById(R.id.constraintLayout);
        mNumberTextView = itemView.findViewById(R.id.numberTextView);
        mBeforeTextInstructionTextView = itemView.findViewById(R.id.beforeTextInstructions);
        mSpannedTextView = itemView.findViewById(R.id.spannedTextView);
        mGridViewLayout = itemView.findViewById(R.id.gridViewLayout);
        mAfterTextInstructionTextView = itemView.findViewById(R.id.afterTextInstructions);

        mOptionSetInstructionTextView = itemView.findViewById(R.id.optionSetInstructions);
        mResponseComponent = itemView.findViewById(R.id.responseComponent);
        mAudioComponent = itemView.findViewById(R.id.audioComponent);
        mSpecialResponseRadioGroup = itemView.findViewById(R.id.specialResponseButtons);
        mClearButton = itemView.findViewById(R.id.clearResponsesButton);
        mValidationTextView = itemView.findViewById(R.id.validation_text);
    }

    public QuestionViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;
        mResponseRepository = new ResponseRepository((Application) context.getApplicationContext());
    }

    private void setCrashlytics() {
        if (AppUtil.PRODUCTION) {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey(mContext.getString(R.string.last_question), String.valueOf(
                    mQuestionRelation.question.getNumberInInstrument()));
        }
    }

    public void setRelations(QuestionRelation questionRelation, DisplayViewModel viewModel) {
        mQuestionRelation = questionRelation;
        mDisplayViewModel = viewModel;
        mResponse = mDisplayViewModel.getResponse(getQuestion().getQuestionIdentifier());
        setCrashlytics();
        setOptionSetItems(questionRelation);
        setSpecialOptions(questionRelation);
        setCarryForwardOptions(questionRelation);
        setQuestionTextComponents();
        setOptionSetInstructionsText();
        createAudioComponent();
        // Overridden by subclasses to place their graphical elements on the fragment.
        createQuestionComponent(mResponseComponent);
        setSpecialResponseView();

        mDeserializing = true;
        deserializeResponse();
        mDeserializing = false;
    }

    protected ViewGroup getAudioComponent() {
        return mAudioComponent;
    }

    protected String getAudioFolder() {
        return getContext().getExternalCacheDir().getAbsolutePath() +
                "/" + getSurvey().getUUID() + "/" + getQuestion().getQuestionIdentifier();
    }

    private void createAudioComponent() {
        if (getQuestion().getRecordAudio()) {
            new AudioComponent(mContext, mAudioComponent, getAudioFolder(), this);
        } else {
            mAudioComponent.setVisibility(View.GONE);
        }
    }

    SurveyViewModel getSurveyViewModel() {
        return mSurveyViewModel;
    }

    public void setSurveyViewModel(SurveyViewModel model) {
        mSurveyViewModel = model;
        if (mSurveyViewModel != null) mSurvey = mSurveyViewModel.getSurvey();
    }

    QuestionRelationAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(QuestionRelationAdapter adapter) {
        mAdapter = adapter;
    }

    QuestionRelation getQuestionRelation() {
        return mQuestionRelation;
    }

    void setQuestionRelation(QuestionRelation relation) {
        mQuestionRelation = relation;
    }

    Question getQuestion() {
        return mQuestionRelation.question;
    }

    List<OptionRelation> getOptionRelations() {
        if (getQuestion().isCarryForward() &&
                !(getCarryForwardQuestion().getQuestionType().equals(Question.CHOICE_TASK))) {
            return mCarryForwardOptionRelations;
        } else {
            return mOptionRelations;
        }
    }

    List<OptionRelation> getSpecialOptionRelations() {
        return mSpecialOptionRelations;
    }

    List<Long> getTextEntryOptionIds() {
        return mTextEntryOptionIds;
    }

    private Response getCarryForwardResponse() {
        return getSurveyViewModel().getResponses().get(getQuestion().getCarryForwardIdentifier());
    }

    private Question getCarryForwardQuestion() {
        return getSurveyViewModel().getQuestionsMap().get(getQuestion().getCarryForwardIdentifier());
    }

    void toggleCarryForward(View view, int optionId) {
        if (getQuestion().isCarryForward() &&
                !(getCarryForwardQuestion().getQuestionType().equals(Question.CHOICE_TASK))) {
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
                    ((TextView) view).setTextColor(getContext().getColor(R.color.secondary_text));
                }
            }
        }
    }

    void setOptionText(String text, CompoundButton button) {
        button.setText(styleTextWithHtmlWhitelist(text));
        button.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.states));
    }

    void setOptionPopUpInstruction(ViewGroup questionComponent, View view, int viewId, OptionRelation optionRelation) {
        final InstructionRelation optionInstruction = getOptionInstruction(optionRelation.option.getIdentifier());
        if (optionInstruction != null) {
            LinearLayout optionLayout = new LinearLayout(getContext());
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            optionLayout.setLayoutParams(layoutParams);
            optionLayout.setOrientation(LinearLayout.HORIZONTAL);
            optionLayout.addView(view);
            ImageButton imageButton = new ImageButton(getContext());
            imageButton.setBackgroundColor(getContext().getColor(R.color.white));
            imageButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_info_outline_blue_24dp));
            imageButton.setOnClickListener(view1 -> showPopUpInstruction(getOptionPopUpInstructions(optionInstruction)));
            optionLayout.addView(imageButton);
            questionComponent.addView(optionLayout, viewId);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);
            questionComponent.addView(view, viewId, params);
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

    protected abstract void showOtherText(int position);

    /**
     * @param otherText An EditText injected from a subclass i.e a write other subclass
     */
    void addOtherResponseView(final EditText otherText) {
        otherText.setHint(R.string.other_specify_edit_text);
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

    void setTextEntry(final int position) {
        if (position < getOptionRelations().size()) {
            OptionRelation optionRelation = getOptionRelations().get(position);
            if (getTextEntryOptionIds().contains(optionRelation.option.getRemoteId())) {
                final EditText otherText = new EditText(getContext());
                otherText.setSingleLine(false);
                otherText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                otherText.setHint(R.string.free_response_edit_text);
                if (!TextUtils.isEmpty(getResponse().getOtherText()))
                    otherText.setText(getResponse().getOtherText());

                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getContext().getString(R.string.extra_text_header))
                        .setView(otherText)
                        .setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setOtherText(otherText.getText().toString());
                                showOtherText(position);
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                setOtherText(BLANK);
                showOtherText(position);
            }
        }
    }

    private void setOtherText(String otherText) {
        mResponse.setOtherText(otherText);
        saveResponse();
    }

    void setOptionSetItems(QuestionRelation questionRelation) {
        mOptionRelations = new ArrayList<>();
        mOptionInstructions = new HashMap<>();
        mTextEntryOptionIds = new ArrayList<>();
        mOptionSetOptionRelations = new LongSparseArray<>();
        if (questionRelation.optionSets.size() > 0) {
            OptionSetRelation optionSetRelation = questionRelation.optionSets.get(0);
            if (optionSetRelation.instructions.size() > 0) {
                mOptionSetInstruction = optionSetRelation.instructions.get(0);
            }
            for (OptionSetOptionRelation relation : sortedOptionSetOptionRelations(optionSetRelation.optionSetOptions)) {
                if (!relation.optionSetOption.isDeleted() && relation.options.size() > 0) {
                    mOptionRelations.add(relation.options.get(0));
                    mOptionSetOptionRelations.put(relation.options.get(0).option.getRemoteId(), relation);
                    if (relation.instructions.size() > 0) {
                        mOptionInstructions.put(relation.options.get(0).option.getIdentifier(), relation.instructions.get(0));
                    }
                    if (relation.optionSetOption.isAllowTextEntry()) {
                        mTextEntryOptionIds.add(relation.optionSetOption.getOptionRemoteId());
                    }
                }
            }
        }
    }

    OptionSetOptionRelation getOptionSetOptionRelation(OptionRelation optionRelation) {
        return mOptionSetOptionRelations.get(optionRelation.option.getRemoteId());
    }

    LongSparseArray<OptionSetOptionRelation> getOptionSetOptionRelations() {
        return mOptionSetOptionRelations;
    }

    InstructionRelation getOptionInstruction(String optionIdentifier) {
        return mOptionInstructions.get(optionIdentifier);
    }

    private void deserializeResponse() {
        if (mResponse == null) return;
        deserialize(mResponse.getText());
        deserializeSpecialResponse();
        deserializeOtherResponse(mResponse.getOtherResponse());
    }

    void saveResponse() {
        mResponse.setText(serialize());
        clearSpecialResponse();
        validateResponse();
        updateResponse();
    }

    private void validateResponse() {
        if (getQuestion().getQuestionIdentifier().equals("ParticipantID")) {
            boolean valid = ParticipantIdValidator.validate(mResponse.getText());
            animateValidationTextView(valid);
        } else if (getQuestion().getQuestionIdentifier().equals("Gender")) {
            mSurveyViewModel.setParticipantGender(mResponse.getText());
        }
    }

    private void animateValidationTextView(boolean valid) {
        Animation animation = new AlphaAnimation(0, 0);

        if (valid) {
            if (mValidationTextView.getVisibility() == TextView.VISIBLE)
                animation = new AlphaAnimation(1, 0);
            mValidationTextView.setVisibility(TextView.INVISIBLE);
            mSurveyViewModel.setParticipantID(mResponse.getText());
        } else {
            animation = new AlphaAnimation(0, 1);
            mValidationTextView.setVisibility(TextView.VISIBLE);
            mValidationTextView.setText(R.string.not_valid_response);
        }

        animation.setDuration(1000);
        if (mValidationTextView.getAnimation() == null ||
                mValidationTextView.getAnimation().hasEnded() ||
                !mValidationTextView.getAnimation().hasStarted()) {
            // Only animate if not currently animating
            mValidationTextView.setAnimation(animation);
        }
    }

    void updateResponse() {
        updateSkipData();
        mResponse.setTimeEnded(new Date());
        mResponse.setIdentifiesSurvey(getQuestion().isIdentifiesSurvey());
        mSurveyViewModel.setResponse(getQuestion().getQuestionIdentifier(), mResponse);
        mDisplayViewModel.setResponse(getQuestion().getQuestionIdentifier(), mResponse);
        mResponseRepository.update(mResponse);
    }

    private void updateSkipData() {
        Option selectedOption = null;
        String enteredValue = null;
        List<Option> selectedOptions = new ArrayList<>();
        if (!TextUtils.isEmpty(mResponse.getText())) {
            if (getQuestion().isSingleResponse()) {
                selectedOption = getSelectedOption(mResponse.getText());
            } else if (getQuestion().getQuestionType().equals(Question.INTEGER)) {
                enteredValue = mResponse.getText();
            } else if (getQuestion().isMultipleResponse()) {
                String[] responses = mResponse.getText().split(COMMA);
                if (responses.length == 1) {
                    selectedOption = getSelectedOption(responses[0]);
                } else {
                    for (String str : responses) {
                        Option option = getSelectedOption(str);
                        if (option != null) selectedOptions.add(option);
                    }
                }
            } else if (getQuestion().getQuestionType().equals(Question.LIST_OF_INTEGER_BOXES)) {
                String[] responses = getStringArray(mResponse.getText());
                for (int k = 0; k < responses.length; k++) {
                    if (!TextUtils.isEmpty(responses[k])) {
                        Option option = getSelectedOption(k + "");
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
        if (mQuestionRelation.conditionSkips.size() > 0) {
            if (getQuestion().getQuestionType().equals(Question.INTEGER)) {
                nextQuestion = integerConditionNextQuestionIdentifier();
            } else if (getQuestion().getQuestionType().equals(Question.SELECT_ONE)) {
                nextQuestion = selectOneConditionNextQuestionIdentifier();
            }
        } else if (!TextUtils.isEmpty(getQuestion().getNextQuestionOperator()) &&
                getQuestion().getNextQuestionOperator().equals(ANY_NOT_SELECTED)) {
            nextQuestion = getNextQuestionIdentifierForNot();
        } else if (selectedOptions.isEmpty()) {
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

    private String selectOneConditionNextQuestionIdentifier() {
        HashSet<String> nextQuestions = new HashSet<>();
        for (ConditionSkip conditionSkip : mQuestionRelation.conditionSkips) {
            String[] optionIds = conditionSkip.getOptionIds().split(COMMA);
            int index = 0;
            List<String> responses = new ArrayList<>();
            List<String> options = new ArrayList<>();
            for (String identifier : conditionSkip.getQuestionIdentifiers().split(COMMA)) {
                responses.add(mDisplayViewModel.getResponse(identifier).getText());
                List<OptionSetOptionRelation> optionRelations = new ArrayList<>();
                for (OptionSetOptionRelation optionSetOptionRelation : sortedOptionSetOptionRelations(
                        mDisplayViewModel.getQuestion(identifier).optionSets.get(0).optionSetOptions)) {
                    if (optionSetOptionRelation.optionSetOption.isDeleted()) continue;
                    optionRelations.add(optionSetOptionRelation);
                }
                for (int k = 0; k < optionRelations.size(); k++) {
                    String optionId = String.valueOf(optionRelations.get(k).options.get(0).option.getRemoteId());
                    if (optionId.equals(optionIds[index])) {
                        options.add(String.valueOf(k));
                        break;
                    }
                }
                index++;
            }
            if (responses.equals(options)) {
                nextQuestions.add(conditionSkip.getNextQuestionIdentifier());
            }
        }
        return nextQuestions.size() == 1 ? nextQuestions.iterator().next() : null;
    }

    private String integerConditionNextQuestionIdentifier() {
        HashSet<String> nextQuestions = new HashSet<>();
        for (ConditionSkip conditionSkip : mQuestionRelation.conditionSkips) {
            List<String> responses = new ArrayList<>();
            for (String identifier : conditionSkip.getQuestionIdentifiers().split(COMMA)) {
                responses.add(mDisplayViewModel.getResponse(identifier).getText());
            }
            if (responses.equals(Arrays.asList(conditionSkip.getValues().split(COMMA)))) {
                nextQuestions.add(conditionSkip.getNextQuestionIdentifier());
            }
        }
        return nextQuestions.size() == 1 ? nextQuestions.iterator().next() : null;
    }

    private String getNextQuestionIdentifierForNot() {
        List<String> selectedIndices = Arrays.asList(mResponse.getText().split(COMMA));
        for (String string : getSkipOptionIndices()) {
            if (selectedIndices.contains(string)) return null;
        }

        HashSet<String> nextQuestions = new HashSet<>();
        for (NextQuestion nextQuestion : mQuestionRelation.nextQuestions) {
            if (!nextQuestion.isDeleted())
                nextQuestions.add(nextQuestion.getNextQuestionIdentifier());
        }
        return nextQuestions.size() == 1 ? nextQuestions.iterator().next() : null;
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

    private List<String> neutralOptionIndices() {
        List<String> list = new ArrayList<>();
        if (TextUtils.isEmpty(getQuestion().getNextQuestionNeutralIds())) return list;
        for (String id : getQuestion().getNextQuestionNeutralIds().split(COMMA)) {
            for (int k = 0; k < mOptionRelations.size(); k++) {
                if (mOptionRelations.get(k).option.getRemoteId().equals(Long.valueOf(id))) {
                    list.add(k + BLANK);
                }
            }
        }
        return list;
    }

    private String getNextQuestionIdentifier(List<Option> options) {
        if (!TextUtils.isEmpty(getQuestion().getNextQuestionOperator()) &&
                getQuestion().getNextQuestionOperator().equals(ConstantUtils.ANY_AND_NO_OTHER) &&
                getQuestion().isMultipleResponse()) {
            List<String> skipOptions = getSkipOptionIndices();
            for (String string : mResponse.getText().split(COMMA)) {
                if (!skipOptions.contains(string) && !neutralOptionIndices().contains(string))
                    return null;
            }
        }

        HashSet<String> nextQuestions = new HashSet<>();

        for (Option option : options) {
            NextQuestion nextQuestion = getNextQuestionForOption(option);
            if (nextQuestion != null && !TextUtils.isEmpty(nextQuestion.getNextQuestionIdentifier())) {
                if (getQuestion().getQuestionType().equals(Question.LIST_OF_INTEGER_BOXES) &&
                        !TextUtils.isEmpty(getQuestion().getNextQuestionOperator()) &&
                        getQuestion().getNextQuestionOperator().equals(ConstantUtils.ANY_AND_NO_OTHER) &&
                        !TextUtils.isEmpty(nextQuestion.getValueOperator())) {
                    String response = null;
                    for (int k = 0; k < mOptionRelations.size(); k++) {
                        if (mOptionRelations.get(k).option.getIdentifier().equals(option.getIdentifier())) {
                            response = getStringArray(mResponse.getText())[k];
                            break;
                        }
                    }
                    if (nextQuestion.getValueOperator().equals(ConstantUtils.EQUALS_TO) && response != null) {
                        if (response.equals(nextQuestion.getValue())) {
                            nextQuestions.add(nextQuestion.getNextQuestionIdentifier());
                        } else {
                            return null;
                        }
                    }
                } else if (getQuestion().getQuestionType().equals(Question.LIST_OF_INTEGER_BOXES) &&
                        !TextUtils.isEmpty(getQuestion().getNextQuestionOperator()) &&
                        getQuestion().getNextQuestionOperator().equals(ConstantUtils.ALL) &&
                        !TextUtils.isEmpty(nextQuestion.getValueOperator())) {
                    for (int k = 0; k < mOptionRelations.size(); k++) {
                        if (mOptionRelations.get(k).option.getIdentifier().equals(option.getIdentifier())) {
                            String response = getStringArray(mResponse.getText())[k];
                            if (!TextUtils.isEmpty(response) && nextQuestion.getValueOperator().equals(ConstantUtils.EQUALS_TO)) {
                                if (response.equals(nextQuestion.getValue())) {
                                    nextQuestions.add(nextQuestion.getNextQuestionIdentifier());
                                } else {
                                    return null;
                                }
                            }
                        }
                    }
                } else {
                    nextQuestions.add(nextQuestion.getNextQuestionIdentifier());
                }
            }
        }
        return nextQuestions.size() == 1 ? nextQuestions.iterator().next() : null;
    }

    private List<String> getSkipOptionIndices() {
        List<String> skipOptions = new ArrayList<>();
        for (NextQuestion nextQuestion : mQuestionRelation.nextQuestions) {
            for (int k = 0; k < mOptionRelations.size(); k++) {
                if (!nextQuestion.isDeleted() &&
                        nextQuestion.getInstrumentRemoteId().equals(mQuestionRelation.question.getInstrumentRemoteId()) &&
                        mOptionRelations.get(k).option.getIdentifier().equals(nextQuestion.getOptionIdentifier())) {
                    skipOptions.add(String.valueOf(k));
                }
            }
        }
        return skipOptions;
    }

    private NextQuestion getNextQuestionForOption(Option option) {
        List<NextQuestion> nextQuestions = mQuestionRelation.nextQuestions;
        if (nextQuestions == null) return null;
        for (NextQuestion nextQuestion : nextQuestions) {
            if (!nextQuestion.isDeleted() &&
                    nextQuestion.getInstrumentRemoteId().equals(mQuestionRelation.question.getInstrumentRemoteId()) &&
                    nextQuestion.getOptionIdentifier().equals(option.getIdentifier())) {
                return nextQuestion;
            }
        }
        return null;
    }

    private NextQuestion getNextQuestionForValue(String value) {
        List<NextQuestion> nextQuestions = mQuestionRelation.nextQuestions;
        if (nextQuestions == null) return null;
        for (NextQuestion nextQuestion : nextQuestions) {
            if (!nextQuestion.isDeleted() && nextQuestion.getValue().equals(value) &&
                    nextQuestion.getInstrumentRemoteId().equals(mQuestionRelation.question.getInstrumentRemoteId())) {
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

    public Survey getSurvey() {
        return mSurvey;
    }

    private void setQuestionTextComponents() {
        setQuestionNumberView();
        if (getQuestion().getQuestionType().equals(Question.INSTRUCTIONS)) {
            mBeforeTextInstructionTextView.setVisibility(View.GONE);
            mAfterTextInstructionTextView.setVisibility(View.GONE);
        } else {
            setBeforeTextInstructionView();
            setAfterTextInstructionView();
        }
        setQuestionText();
    }

    private void setQuestionNumberView() {
        if (mNumberTextView == null) return;
        if (getQuestion().getShowNumber()) {
            String text = getQuestion().getPosition() + ") " + getQuestion().getQuestionIdentifier();
            mNumberTextView.setText(text);
        } else {
            mNumberTextView.setVisibility(View.GONE);
        }
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
        if (getQuestion().getPopUpInstructionId() == null) {
            mSpannedTextView.setCompoundDrawables(null, null, null, null);
        } else {
            setCompoundDrawableRight(mSpannedTextView,
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_info_outline_blue_24dp),
                    getQuestionPopUpInstructions());
        }
        mSpannedTextView.setText(getQuestionText());
        if (mQuestionRelation.question.getQuestionType().equals(Question.PAIRWISE_COMPARISON)) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mConstraintLayout);
            constraintSet.center(R.id.spannedTextView, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0,
                    ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0, 0.5f);
            constraintSet.applyTo(mConstraintLayout);
        }
        setQuestionDiagrams();
    }

    private void setQuestionDiagrams() {
        List<QuestionCollageRelation> questionCollageRelations = mQuestionRelation.questionCollages.stream()
                .filter(questionCollageRelation -> !questionCollageRelation.questionCollage.isDeleted())
                .collect(Collectors.toList());
        if (questionCollageRelations.size() > 0) {
            mGridViewLayout.setVisibility(View.VISIBLE);
            for (int k = 0; k < questionCollageRelations.size(); k++) {
                LinearLayout layout = (LinearLayout) LayoutInflater.from(mContext).inflate(
                        R.layout.list_item_question_grid_view, null, false);
                GridView gridView = layout.findViewById(R.id.gridView);
                CollageRelation collageRelation = questionCollageRelations.get(k).collages.get(0);
                gridView.setNumColumns(collageRelation.diagrams.size());
                QuestionDiagramAdapter adapter = new QuestionDiagramAdapter(mContext, mQuestionRelation,
                        collageRelation, mSurveyViewModel);
                gridView.setAdapter(adapter);
                ViewGroup.LayoutParams layoutParams = mGridViewLayout.getLayoutParams();
                layoutParams.width = adapter.getViewWidth();
                mGridViewLayout.setLayoutParams(layoutParams);
                mGridViewLayout.addView(layout);
            }
        }

        // Choice task carry forward
        setCarryForwardDiagrams();
    }

    private void setCarryForwardDiagrams() {
        if (mQuestionRelation.question.isCarryForward()) {
            if (getCarryForwardQuestion().getQuestionType().equals(Question.CHOICE_TASK)) {
                Response carryForwardResponse = getCarryForwardResponse();
                if (carryForwardResponse != null && !carryForwardResponse.getText().isEmpty()) {
                    String[] listOfIndices = carryForwardResponse.getText().split(COMMA);
                    int best = Integer.parseInt(listOfIndices[0]);
                    mGridViewLayout.removeAllViews();
                    mGridViewLayout.setVisibility(View.VISIBLE);
                    ArrayList<ArrayList<LinearLayout>> layouts = new ArrayList<>();
                    ArrayList<ArrayList<Integer>> heights = new ArrayList<>();
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout imageLayout = (LinearLayout) inflater.inflate(R.layout.choice_task, null);

                    for (final OptionRelation optionRelation : mCarryForwardOptionRelations) {
                        ArrayList<Integer> collageHeights = new ArrayList<>();
                        ArrayList<LinearLayout> collageLayouts = new ArrayList<>();
                        OptionSetOptionRelation relation = null;
                        for (OptionSetOptionRelation optionSetOptionRelation :
                                getQuestionRelation().carryForwardOptionSets.get(0).optionSetOptions) {
                            if (optionSetOptionRelation.optionSetOption.getOptionRemoteId()
                                    .equals(optionRelation.option.getRemoteId())) {
                                relation = optionSetOptionRelation;
                            }
                        }
                        if (relation == null) return;

                        int index = mCarryForwardOptionRelations.indexOf(optionRelation);
                        LinearLayout linearLayout;
                        TextView textView;
                        MaterialCardView cardView;
                        if (index == 0) {
                            cardView = imageLayout.findViewById(R.id.leftCardView);
                            linearLayout = imageLayout.findViewById(R.id.leftLayout);
                            textView = imageLayout.findViewById(R.id.leftTitle);
                            textView.setText(getContext().getResources().getString(R.string.option, 'A'));
                        } else if (index == 1) {
                            cardView = imageLayout.findViewById(R.id.middleCardView);
                            linearLayout = imageLayout.findViewById(R.id.middleLayout);
                            textView = imageLayout.findViewById(R.id.middleTitle);
                            textView.setText(getContext().getResources().getString(R.string.option, 'B'));
                        } else {
                            cardView = imageLayout.findViewById(R.id.rightCardView);
                            linearLayout = imageLayout.findViewById(R.id.rightLayout);
                            textView = imageLayout.findViewById(R.id.rightTitle);
                            textView.setText(getContext().getResources().getString(R.string.option, 'C'));
                        }
                        cardView.setId(index);

                        for (OptionCollageRelation optionCollageRelation : relation.optionCollages) {
                            for (CollageRelation collageRelation : optionCollageRelation.collages) {
                                GridView gridView = (GridView) inflater.inflate(R.layout.list_item_option_grid_view, null);
                                gridView.setNumColumns(collageRelation.diagrams.size());
                                ChoiceDiagramAdapter adapter = new ChoiceDiagramAdapter(getContext(), getQuestionRelation(),
                                        collageRelation.diagrams, getSurveyViewModel(), mCarryForwardOptionRelations.size());
                                gridView.setAdapter(adapter);
                                LinearLayout gridViewLayout = new LinearLayout(getContext());
                                gridViewLayout.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.choice_option_border));
                                gridViewLayout.setPadding(5, 0, 5, 0);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0, 5, 0, 5);
                                gridViewLayout.addView(gridView, layoutParams);
                                linearLayout.addView(gridViewLayout);
                                collageHeights.add(adapter.getMaxHeight());
                                collageLayouts.add(gridViewLayout);
                            }
                        }
                        heights.add(collageHeights);
                        layouts.add(collageLayouts);
                        if (best == index) {
                            cardView.setCheckable(true);
                            cardView.setChecked(true);
                            cardView.setCardForegroundColor(getContext().getColorStateList(R.color.first));
                        } else {
                            cardView.setCheckable(false);
                            cardView.setChecked(false);
                            cardView.setCardForegroundColor(getContext().getColorStateList(R.color.third));
                        }
                    }
                    // Set layout heights to the maximum height of the layouts in the row
                    for (int j = 0; j < layouts.size(); j++) {
                        ArrayList<Integer> rowHeights = new ArrayList<>();
                        for (int k = 0; k < heights.size(); k++) {
                            rowHeights.add(heights.get(k).get(j));
                        }
                        int max = Collections.max(rowHeights);
                        for (int k = 0; k < heights.size(); k++) {
                            ViewGroup.LayoutParams layoutParams = layouts.get(k).get(j).getLayoutParams();
                            layoutParams.height = max;
                        }
                    }
                    mGridViewLayout.addView(imageLayout);
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    void setCompoundDrawableRight(final TextView textView, Drawable right, final String instructions) {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, right, null);
        textView.setCompoundDrawablePadding(2);
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable[] drawables = ((TextView) v).getCompoundDrawables();
                if (drawables.length == 4 && drawables[2] != null) {
                    Drawable drawable = drawables[2];
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (event.getRawX() >= (textView.getRight() - drawable.getBounds().width())) {
                            showPopUpInstruction(instructions);
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    private void setAfterTextInstructionView() {
        if (mAfterTextInstructionTextView == null) return;
        if (mQuestionRelation.afterTextInstructions.size() == 0) {
            mAfterTextInstructionTextView.setVisibility(View.GONE);
        } else {
            mAfterTextInstructionTextView.setText(getAfterTextInstructions());
            if (mQuestionRelation.question.isCarryForward() ||
                    mQuestionRelation.question.getQuestionType().equals(Question.CHOICE_TASK)) {
                mAfterTextInstructionTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
            }
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

    void showPopUpInstruction(String instructions) {
        new AlertDialog.Builder(getContext())
                .setMessage(instructions)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

    }

    private String getQuestionPopUpInstructions() {
        String instructions = "";
        if (mQuestionRelation.popUpInstructions.size() > 0) {
            instructions = TranslationUtil.getText(mQuestionRelation.popUpInstructions.get(0).instruction,
                    mQuestionRelation.popUpInstructions.get(0).translations, mSurveyViewModel);
        }
        return styleTextWithHtml(instructions).toString();
    }

    String getOptionPopUpInstructions(InstructionRelation instructionRelation) {
        String instructions = instructionRelation.instruction.getText();
        if (instructionRelation.translations.size() > 0) {
            instructions = TranslationUtil.getText(instructionRelation.instruction,
                    instructionRelation.translations, mSurveyViewModel);
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
        String text = TranslationUtil.getText(getQuestion(), mQuestionRelation.translations, mSurveyViewModel);
        if (!TextUtils.isEmpty(getQuestion().getLoopSource())) {
            String causeId = getQuestion().getQuestionIdentifier().split("_")[0];
            Response response = getSurveyViewModel().getResponses().get(causeId);
            if (response != null && !TextUtils.isEmpty(response.getText())) {
                String responseText = "";
                String[] responses = response.getText().split(COMMA, -1);
                Question causeQuestion = getSurveyViewModel().getQuestionsMap().get(causeId);
                if (causeQuestion.isSingleResponse()) {
                    int index = Integer.parseInt(responses[getQuestion().getLoopNumber()]);
                    responseText = mOptionRelations.get(index).option.getText();
                } else if (causeQuestion.isMultipleResponse()) {
                    if (Arrays.asList(responses).contains(Integer.toString(getQuestion().getLoopNumber()))) {
                        if (getQuestion().getLoopNumber() < mOptionRelations.size())
                            responseText = mOptionRelations.get(getQuestion().getLoopNumber()).option.getText();
                    }
                } else {
                    if (getQuestion().getLoopNumber() < responses.length) {
                        responseText = responses[getQuestion().getLoopNumber()]; //Keep empty values
                    }
                }
                if (!TextUtils.isEmpty(responseText)) {
                    int begin = text.indexOf("[");
                    int last = text.indexOf("]");
                    if (begin != -1 && last != -1 && begin < last) {
                        text = text.replace(text.substring(begin, last + 1), responseText);
                    } else {
                        if (!TextUtils.isEmpty(getQuestion().getTextToReplace())) {
                            text = getQuestion().getText().replace(getQuestion().getTextToReplace(), responseText);
                        }
                    }
                }
            }
        }
        if (mQuestionRelation.question.isCarryForward()) {
            Question cfq = getCarryForwardQuestion();
            if (cfq.getQuestionType().equals(Question.CHOICE_TASK)) {
                Response resp = getCarryForwardResponse();
                if (resp != null && !resp.getText().isEmpty()) {
                    text = TranslationUtil.getText(getQuestion(), mQuestionRelation.translations, mSurveyViewModel);
                    String[] listOfIndices = resp.getText().split(COMMA);
                    int best = Integer.parseInt(listOfIndices[0]);
                    if (resp.getRandomizedData() == null || resp.getRandomizedData().isEmpty()) {
                        OptionRelation optionRelation = mCarryForwardOptionRelations.get(best);
                        String oText = TranslationUtil.getText(optionRelation.option, optionRelation.translations, mSurveyViewModel);
                        text = text.replaceFirst("\\[followup\\]", Html.fromHtml(oText).toString().trim());
                    } else {
                        String replacement;
                        if (best == 0) {
                            replacement = "A";
                        } else if (best == 1) {
                            replacement = "B";
                        } else {
                            replacement = "C";
                        }
                        text = text.replaceFirst("\\[followup\\]",
                                getContext().getResources().getString(R.string.option, replacement));
                    }
                }
            }
        }
        return styleTextWithHtml(subNewLine(text));
    }

    private String subNewLine(String text) {
        if (text.contains("\n")) {
            String[] paras = text.split("\n");
            StringBuilder builder = new StringBuilder();
            for (String para : paras) {
                builder.append("<p>").append(para).append("</p>");
            }
            text = builder.toString();
        }
        text = text.replaceFirst("<p>","").replaceFirst("</p>", "");
        return text;
    }

    private void setOptionSetInstructionsText() {
        if (mOptionSetInstruction != null && mOptionSetInstructionTextView != null &&
                !mQuestionRelation.question.getQuestionType().equals(Question.PAIRWISE_COMPARISON) &&
                !mQuestionRelation.question.getQuestionType().equals(Question.CHOICE_TASK)
        ) {
            mOptionSetInstructionTextView.setText(getOptionSetInstructions());
            mOptionSetInstructionTextView.setVisibility(View.VISIBLE);
        }
    }

    public String getOptionSetInstructions() {
        String instructions = "";
        if (mOptionSetInstruction != null) {
            instructions = TranslationUtil.getText(mOptionSetInstruction.instruction,
                    mOptionSetInstruction.translations, getSurveyViewModel());
        }
        return styleTextWithHtml(instructions).toString();
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

        if (getQuestion().getQuestionType().equals(Question.INSTRUCTIONS) ||
                getQuestion().getQuestionType().equals(Question.PAIRWISE_COMPARISON) ||
                getQuestion().getQuestionType().equals(Question.AUDIO)) {
            mClearButton.setVisibility(View.GONE);
        } else {
            mClearButton.setOnClickListener(view -> {
                clearResponse();
                updateResponse();
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
        if (mSpecialResponseRadioGroup != null) mSpecialResponseRadioGroup.clearCheck();
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

    void setCarryForwardOptions(QuestionRelation questionRelation) {
        mCarryForwardOptionRelations = new ArrayList<>();
        if (questionRelation.carryForwardOptionSets.size() > 0) {
            OptionSetRelation optionSetRelation = questionRelation.carryForwardOptionSets.get(0);
            if (optionSetRelation != null && optionSetRelation.optionSetOptions != null) {
                List<OptionSetOptionRelation> optionSetOptionRelations = optionSetRelation.optionSetOptions;
                optionSetOptionRelations.sort((o1, o2) -> o1.optionSetOption.getPosition().compareTo(o2.optionSetOption.getPosition()));
                for (OptionSetOptionRelation relation : optionSetOptionRelations) {
                    if (relation.options.size() > 0) {
                        mCarryForwardOptionRelations.add(relation.options.get(0));
                    }
                }
            }

            if (getCarryForwardQuestion().getQuestionType().equals(Question.CHOICE_TASK)) {
                Response carryForwardResponse = getCarryForwardResponse();
                List<OptionRelation> carryForwardOptionRelations = new ArrayList<>();
                if (carryForwardResponse != null && carryForwardResponse.getRandomizedData() != null
                        && !carryForwardResponse.getRandomizedData().isEmpty()) {
                    String[] orderList = carryForwardResponse.getRandomizedData().split(COMMA);
                    List<Integer> order = new ArrayList<>();
                    for (String index : orderList) {
                        if (!index.isEmpty()) {
                            int indexInteger = Integer.parseInt(index);
                            order.add(indexInteger);
                        }
                    }
                    for (Integer integer : order) {
                        carryForwardOptionRelations.add(mCarryForwardOptionRelations.get(integer));
                    }
                    mCarryForwardOptionRelations = carryForwardOptionRelations;
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