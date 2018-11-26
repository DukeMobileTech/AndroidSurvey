package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.DisplayInstruction;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionSet;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ResponsePhoto;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.AuthUtils;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.utils.looper.ItemTouchHelperExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.isEmpty;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class SingleQuestionFragment extends QuestionFragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey" +
            ".instrument_id";
    public final static String EXTRA_RESPONSE_ID = "org.adaptlab.chpir.android.survey" +
            ".response_id";
    public final static String EXTRA_SURVEY_ID = "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_QUESTION_ID = "org.adaptlab.chpir.android.survey" +
            ".question_id";
    private final static String TAG = "SingleQuestionFragment";
    public TextView mValidationTextView;
    protected RadioGroup mSpecialResponses;
    private Response mResponse;
    private Question mQuestion;
    private Survey mSurvey;
    private Instrument mInstrument;
    private List<Option> mOptions;
    private List<Option> mSpecialOptions;
    private TextView mDisplayInstructionsText;
    private OptionsAdapter mOptionsAdapter;
    private View mFragmentView;
    private ViewGroup mRankLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question, parent, false);
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
        spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary_text)),
                0, numLen + idLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, numLen,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.ITALIC), numLen + idLen,
                numLen + idLen + insLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue)),
                numLen + idLen + insLen, numLen + idLen + insLen + textLen,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new RelativeSizeSpan(1.2f),
                numLen + idLen + insLen, numLen + idLen + insLen + textLen,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = v.findViewById(R.id.spannedTextView);
        textView.setText(spannableText);

        mValidationTextView = v.findViewById(R.id.validation_text);
        mDisplayInstructionsText = v.findViewById(R.id.displayInstructions);
        setDisplayInstructions();
        // Overridden by subclasses to place their graphical elements on the fragment.
        ViewGroup questionComponent = (LinearLayout) v.findViewById(R.id.question_component);
        setChoiceSelectionInstructions(v);
        createQuestionComponent(questionComponent);
        deserialize(mResponse.getText());
        setSkipPatterns();
        refreshFollowUpQuestion();
        setResponseRanking(v);
        setSpecialResponseUI(v);
        deserializeSpecialResponse();
        setLoopQuestions(mQuestion, mResponse);
        mFragmentView = v;
        return v;
    }

    private void setResponseRanking(View view) {
        if (getQuestion().rankResponses() && getSelectedOptions().size() > 1) {
            mRankLayout = view.findViewById(R.id.responseRankingLayout);
            mRankLayout.setVisibility(View.VISIBLE);
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            mOptionsAdapter = new OptionsAdapter(getSelectedOptions());
            recyclerView.setAdapter(mOptionsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                    recyclerView.getContext(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            ItemTouchHelperExtension.Callback callback = new ItemTouchHelperCallback();
            ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    protected void optionToggled(int index) {
        updateRankOrder(index);
        if (getSelectedOptions().size() < 2) {
            if (mRankLayout != null) {
                mRankLayout.setVisibility(View.GONE);
                if (mOptionsAdapter != null) mOptionsAdapter.updateOptions(getSelectedOptions());
            }
        } else {
            if (mOptionsAdapter == null) {
                setResponseRanking(mFragmentView);
            } else {
                if (mRankLayout != null) mRankLayout.setVisibility(View.VISIBLE);
                mOptionsAdapter.updateOptions(getSelectedOptions());
            }
        }
    }

    private void updateRankOrder(int index) {
        String indexInteger = String.valueOf(index);
        ArrayList<String> rankOrder = new ArrayList<>();
        if (mResponse.getRankOrder() != null) {
            rankOrder = new ArrayList<>(Arrays.asList(
                    mResponse.getRankOrder().split(Response.LIST_DELIMITER)));
        }
        if (rankOrder.contains(indexInteger)) {
            rankOrder.remove(indexInteger);
        } else {
            rankOrder.add(indexInteger);
        }
        mResponse.setRankOrder(FormatUtils.arrayListToString(rankOrder));
    }

    private List<Option> getSelectedOptions() {
        List<Option> options = new ArrayList<>();
        if (mResponse != null) {
            String[] order = new String[0];
            if (!TextUtils.isEmpty(mResponse.getRankOrder())) {
                order = mResponse.getRankOrder().split(Response.LIST_DELIMITER);
            } else if (!TextUtils.isEmpty(mResponse.getText())) {
                order = mResponse.getText().split(Response.LIST_DELIMITER);
            }
            for (String response : order) {
                if (!response.equals(Response.BLANK)) {
                    int index = Integer.parseInt(response);
                    if (index < getOptions().size()) {
                        options.add(getOptions().get(index));
                    }
                }
            }
        }
        return options;
    }

    private void setSpecialResponseUI(View v) {
        if (!mQuestion.getDisplay().getMode().equals(Display.DisplayMode.TABLE.toString())) {
            mSpecialResponses = v.findViewById(R.id.specialResponseButtons);
            List<String> responses = new ArrayList<>();
            if (mQuestion.hasSpecialOptions()) {
                for (Option option : mSpecialOptions) {
                    responses.add(option.getText(mQuestion.getInstrument()));
                }
            }

            for (String response : responses) {
                int responseId = responses.indexOf(response);
                final Button button = new RadioButton(getActivity());
                button.setText(response);
                button.setId(responseId);
                button.setTypeface(getInstrument().getTypeFace(v.getContext()));
                button.setTextColor(getResources().getColorStateList(R.color.states));

                mSpecialResponses.addView(button, responseId);
                final List<String> finalResponses = responses;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unSetResponse();
                        setResponse(finalResponses.get(v.getId()));
                    }
                });
            }
        }

        Button clearButton = v.findViewById(R.id.clearResponsesButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpecialResponses.clearCheck();
                unSetResponse();
                if (getQuestion().rankResponses()) {
                    if (mRankLayout != null && mOptionsAdapter != null) {
                        mOptionsAdapter.clear();
                        mRankLayout.setVisibility(View.GONE);
                    }
                }
                setResponse(Response.BLANK);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mInstrument != null) {
            outState.putLong(EXTRA_INSTRUMENT_ID, mInstrument.getId());
        }
        if (mSurvey != null) {
            outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
        }
        if (mQuestion != null) {
            outState.putLong(EXTRA_QUESTION_ID, mQuestion.getId());
        }
        if (mResponse != null) {
            outState.putLong(EXTRA_RESPONSE_ID, mResponse.getId());
        }
    }

    protected void setDisplayInstructions() {
        List<DisplayInstruction> displayInstructions = mSurveyFragment.getDisplayInstructions(mQuestion.getDisplay());
        if (displayInstructions != null && displayInstructions.size() > 0) {
            StringBuilder instructions = new StringBuilder();
            for (int k = 0; k < displayInstructions.size(); k++) {
                if (displayInstructions.get(k).getPosition() == mQuestion.getNumberInInstrument()) {
                    instructions.append(displayInstructions.get(k).getInstructions());
                }
            }
            if (instructions.length() > 0) {
                mDisplayInstructionsText.setVisibility(View.VISIBLE);
                mDisplayInstructionsText.setText(styleTextWithHtml(instructions.toString()));
            }
        }
    }

    protected void hideIndeterminateProgressBar() {
        List<Question> displayQuestions = mSurveyFragment.getQuestions(mQuestion.getDisplay());
        if (displayQuestions.get(displayQuestions.size() - 1).equals(mQuestion)) {
            mSurveyFragment.hideIndeterminateProgressBar();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mSurveyFragment == null) return;
        if (savedInstanceState == null) {
            init();
        } else {
            mInstrument = Instrument.load(Instrument.class, savedInstanceState.getLong(EXTRA_INSTRUMENT_ID));
            mSurvey = Survey.load(Survey.class, savedInstanceState.getLong(EXTRA_SURVEY_ID));
            mQuestion = Question.load(Question.class, savedInstanceState.getLong(EXTRA_QUESTION_ID));
            mResponse = Response.load(Response.class, savedInstanceState.getLong(EXTRA_RESPONSE_ID));
            mOptions = mSurveyFragment.getOptions().get(mQuestion);
        }
        if (mSurveyFragment.getSpecialOptions() != null && mQuestion != null) {
            mSpecialOptions = mSurveyFragment.getSpecialOptions().get(mQuestion.getRemoteSpecialOptionSetId());
        }
        if (AppUtil.PRODUCTION) {
            Fabric.with(getActivity(), new Crashlytics());
            Crashlytics.setString(getString(R.string.last_question),
                    String.valueOf(mQuestion.getNumberInInstrument()));
        }
    }

    public void init() {
        Bundle bundle = this.getArguments();
        String questionIdentifier = "";
        if (bundle != null) {
            questionIdentifier = bundle.getString("QuestionIdentifier");
        }
        mSurvey = mSurveyFragment.getSurvey();
        mQuestion = mSurveyFragment.getQuestion(questionIdentifier);
        if (mSurvey == null || mQuestion == null) return;
        mResponse = loadOrCreateResponse();
        mResponse.setQuestion(mQuestion);
        mResponse.setSurvey(mSurvey);
        mResponse.setTimeStarted(new Date());
        mInstrument = mSurvey.getInstrument();
        if (mSurveyFragment.getOptions() != null) {
            mOptions = mSurveyFragment.getOptions().get(mQuestion);
            if (mOptions == null) mOptions = new ArrayList<>();
        }
        refreshFollowUpQuestion();
    }

    private Response loadOrCreateResponse() {
        Response response = mSurveyFragment.getResponses().get(mQuestion);
        if (response == null) {
            response = new Response();
            response.setQuestion(mQuestion);
            response.setSurvey(mSurvey);
            response.save();
            mSurveyFragment.getResponses().put(mQuestion, response);
        }
        return response;
    }

    private void refreshFollowUpQuestion() {
        if (mQuestion.isToFollowUpOnQuestion()) {
            mDisplayFragment.reAnimateFollowUpFragment(mQuestion);
        }
    }

    private String getQuestionInstructions() {
        Spanned instructions = new SpannableString("");
        if (!TextUtils.isEmpty(mQuestion.getInstructions()) && !mQuestion.getInstructions().equals("null")) {
            instructions = styleTextWithHtml(mQuestion.getInstructions());
        }
        return instructions.toString();
    }

    /*
     * If this question is a follow up question, then attempt
     * to get the response to the question that is being followed up on.
     *
     * If the question being followed up on was skipped by the user,
     * then return false. This gives the calling function an opportunity
     * to handle this accordingly.  Likely this will involve skipping
     * the question that is a follow up question.
     *
     * If this question is not a following up question, then just
     * set the text as normal.
     */

    protected Spanned getQuestionText() {
        String text = "";
        if (mQuestion.isFollowUpQuestion()) {
            String followUpText = mQuestion.getFollowingUpText(mSurveyFragment.getResponses(), getActivity());
            if (followUpText != null) { text = followUpText; }
        } else if (mQuestion.hasRandomizedFactors()) {
            text = mQuestion.getRandomizedText(mSurveyFragment.getResponses().get(mQuestion));
        } else {
            text = mQuestion.getText();
        }
        return styleTextWithHtml(text);
    }

    private void setChoiceSelectionInstructions(View view) {
        OptionSet optionSet = OptionSet.findByRemoteId(getQuestion().getRemoteOptionSetId());
        if (optionSet != null && !isEmpty(optionSet.getInstructions())) {
            TextView instructionsView = view.findViewById(R.id.optionSetInstructions);
            if (instructionsView != null) {
                instructionsView.setText(styleTextWithHtml(optionSet.getInstructions()));
                instructionsView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setSkipPatterns() {
        Option selectedOption = null;
        String nextQuestion = null;
        String enteredValue = null;
        List<Option> selectedOptions = new ArrayList<>();
        if (!TextUtils.isEmpty(mResponse.getText())) {
            if (mQuestion.hasSingleResponse()) {
                selectedOption = getSelectedOption(mResponse.getText());
            } else if (mQuestion.getQuestionType().equals(Question.QuestionType.INTEGER)) {
                enteredValue = mResponse.getText();
            } else if (mQuestion.hasMultipleResponses()) {
                String[] responses = mResponse.getText().split(Response.LIST_DELIMITER);
                if (responses.length == 1) {
                    selectedOption = getSelectedOption(responses[0]);
                } else {
                    for (String response : responses) {
                        Option option = getSelectedOption(response);
                        if (option != null) selectedOptions.add(option);
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(mResponse.getSpecialResponse())) {
            selectedOption = Option.findByQuestionAndSpecialResponse(mQuestion,
                    mResponse.getSpecialResponse());
        }
        if (selectedOption == null && enteredValue == null && selectedOptions.isEmpty()) {
            showAllSubsequentQuestions();
        } else if (selectedOption != null && enteredValue == null && selectedOptions.isEmpty()) {
            nextQuestion = mQuestion.getNextQuestionIdentifier(selectedOption, mResponse);
        } else if (selectedOption == null && enteredValue != null && selectedOptions.isEmpty()) {
            nextQuestion = mQuestion.getNextQuestionIdentifier(enteredValue);
        } else if (!selectedOptions.isEmpty()) {
            nextQuestion = mQuestion.getNextQuestionIdentifier(selectedOptions);
        }
        if (nextQuestion == null) {
            showAllSubsequentQuestions();
        } else {
            if (nextQuestion.equals(Question.COMPLETE_SURVEY)) {
                mSurveyFragment.startSurveyCompletion(mQuestion);
            } else {
                mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(),
                        nextQuestion, mQuestion.getQuestionIdentifier());
            }
        }
        if ((selectedOption != null || enteredValue != null) && mQuestion.isMultipleSkipQuestion(mInstrument)) {
            mSurveyFragment.setMultipleSkipQuestions(selectedOption, enteredValue, mQuestion);
        } else if (!selectedOptions.isEmpty() && mQuestion.isMultipleSkipQuestion(mInstrument)) {
            mSurveyFragment.setMultipleSkipQuestions2(selectedOptions, mQuestion);
        }
    }

    private Option getSelectedOption(String responseText) {
        int responseIndex = Integer.parseInt(responseText);
        if (responseIndex < mQuestion.defaultOptions().size()) {
            return mQuestion.defaultOptions().get(responseIndex);
        } else {
            return null;
        }
    }

    private void showAllSubsequentQuestions() {
        mSurveyFragment.setNextQuestion(mQuestion.getQuestionIdentifier(),
                mQuestion.getQuestionIdentifier(), mQuestion.getQuestionIdentifier());
    }

    protected Instrument getInstrument() {
        return mInstrument;
    }

    private void deserializeSpecialResponse() {
        if (TextUtils.isEmpty(mResponse.getSpecialResponse())) return;
        for (int i = 0; i < mSpecialResponses.getChildCount(); i++) {
            if (((RadioButton) mSpecialResponses.getChildAt(i)).getText().equals(mResponse
                    .getSpecialResponse())) {
                mSpecialResponses.check(i);
            }
        }
    }

    public Question getQuestion() {
        return mQuestion;
    }

    /*
     * This will remove the focus of the input as the survey is
     * traversed.  If this is not called, then it will be possible
     * for someone to change the answer to a question that they are
     * not currently viewing.
     */
    private void removeTextFocus() {
        if (getActivity() == null) return;
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && getActivity().getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void animateValidationTextView(boolean valid) {
        Animation animation = new AlphaAnimation(0, 0);
        if (valid) {
            if (mValidationTextView.getVisibility() == TextView.VISIBLE)
                animation = new AlphaAnimation(1, 0);
            mValidationTextView.setVisibility(TextView.GONE);
        } else {
            animation = new AlphaAnimation(0, 1);
            mValidationTextView.setVisibility(TextView.VISIBLE);
            if (mQuestion.getValidation() != null) {
                mValidationTextView.setText(styleTextWithHtml(mQuestion.getValidation()
                        .getValidationMessage(mInstrument)));
            } else {
                mValidationTextView.setText(styleTextWithHtml(
                        getString(R.string.not_valid_response)));
            }
        }

        animation.setDuration(1000);
        if (mValidationTextView.getAnimation() == null ||
                mValidationTextView.getAnimation().hasEnded() ||
                !mValidationTextView.getAnimation().hasStarted()) {
            // Only animate if not currently animating
            mValidationTextView.setAnimation(animation);
        }
    }

    protected SurveyFragment getSurveyFragment() {
        return mSurveyFragment;
    }

    public List<Option> getOptions() {
        return mOptions;
    }

    protected Survey getSurvey() {
        return mSurvey;
    }

    public Response getResponse() {
        return mResponse;
    }

    protected void setResponse(String specialResponse) {
        if (mResponse == null) return;
        if (specialResponse == null) {
            // Set responseText
            mResponse.setResponse(serialize());
            mResponse.setSpecialResponse(Response.BLANK);
            validateResponse();
            if (!mQuestion.isTextEntryQuestionType()) {
                removeTextFocus();
            }
        } else {
            // Set specialResponse
            mResponse.setSpecialResponse(specialResponse);
            mResponse.setResponse(Response.BLANK);
            deserialize(mResponse.getText());
            removeTextFocus();
            animateValidationTextView(true);
        }
        mResponse.setTimeEnded(new Date());
        mResponse.setDeviceUser(AuthUtils.getCurrentUser());
        mResponse.setQuestionVersion(mQuestion.getQuestionVersion());
        mSurvey.setLastUpdated(new Date());
        saveResponseInBackground(mResponse);
        setSkipPatterns();
        refreshFollowUpQuestion();
        setLoopQuestions(mQuestion, mResponse);
    }

    protected ResponsePhoto getResponsePhoto() {
        return mResponse.getResponsePhoto();
    }

    /*
     * An otherText is injected from a subclass.  This gives
     * the majority of the control to the otherText to the subclass,
     * but the things that all other text fields have in common
     * can go here.
     */
    public void addOtherResponseView(EditText otherText) {
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
                        setOtherResponse(s.toString());
                    }
                }, 3000); // 3 second delay before saving to db
            }
        });

        if (mResponse.getOtherResponse() != null) {
            otherText.setText(mResponse.getOtherResponse());
        }
    }

    public void setOtherResponse(String response) {
        mResponse.setOtherResponse(response);
        setResponse(null); //Trigger a save
    }

    /*
     * Display warning to user if response does not match regular
     * expression in question.  Disable next button if not valid.
     */
    protected void validateResponse() {
        if (mResponse.isValid()) {
            animateValidationTextView(true);
        } else {
            animateValidationTextView(false);
        }
    }

    protected void setResponseTextBlank() {
        if (getResponse() != null) {
            getResponse().setResponse(Response.BLANK);
        }
    }

    private class ItemTouchHelperCallback extends ItemTouchHelperExtension.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            return mOptionsAdapter.onItemMove(viewHolder.getAdapterPosition(),
                    target.getAdapterPosition());
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    }

    private class OptionsAdapter extends RecyclerView.Adapter<OptionsViewHolder> {
        private List<Option> mOptions;

        OptionsAdapter(List<Option> options) {
            mOptions = options;
            saveRankOrder();
        }

        void updateOptions(List<Option> options) {
            final List<Option> oldOptions = new ArrayList<>(this.mOptions);
            this.mOptions.clear();
            if (options != null) {
                this.mOptions.addAll(options);
            }

            DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return oldOptions.size();
                }

                @Override
                public int getNewListSize() {
                    return mOptions.size();
                }

                @Override
                public boolean areItemsTheSame(int oldPosition, int newPosition) {
                    return oldOptions.get(oldPosition).equals(mOptions.get(newPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldPosition, int newPosition) {
                    Option oldOption = oldOptions.get(oldPosition);
                    Option newOption = mOptions.get(newPosition);
                    return oldOption.getIdentifier().equals(newOption.getIdentifier());
                }
            }).dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public OptionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View optionView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_option_view, parent, false);
            return new OptionsViewHolder(optionView);
        }

        @Override
        public void onBindViewHolder(@NonNull OptionsViewHolder holder, int position) {
            holder.setOption(mOptions.get(position));
        }

        @Override
        public int getItemCount() {
            return mOptions.size();
        }

        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mOptions, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mOptions, i, i - 1);
                }
            }
            this.notifyItemMoved(fromPosition, toPosition);
            saveRankOrder();
            return true;
        }

        private void saveRankOrder() {
            StringBuilder order = new StringBuilder();
            for (int i = 0; i < mOptions.size(); i++) {
                int index = getOptions().indexOf(mOptions.get(i));
                order.append(index);
                if (i < mOptions.size() - 1) order.append(Response.LIST_DELIMITER);
            }
            mResponse.setRankOrder(order.toString());
        }

        public void clear() {
            final int size = mOptions.size();
            mOptions.clear();
            notifyItemRangeRemoved(0, size);
        }

    }

    private class OptionsViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        private OptionsViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.optionTextListItem);
        }

        private void setOption(Option option) {
            mTextView.setText(option.getText(getInstrument()));
        }
    }

}