package org.adaptlab.chpir.android.survey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.DisplayInstruction;
import org.adaptlab.chpir.android.survey.models.Instruction;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.NextQuestion;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionInOptionSet;
import org.adaptlab.chpir.android.survey.models.OptionSet;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.utils.AuthUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.isEmpty;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class MultipleQuestionsFragment extends QuestionFragment {
    public final static String EXTRA_DISPLAY_ID =
            "org.adaptlab.chpir.android.survey.display_id";
    public final static String EXTRA_SURVEY_ID =
            "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_SKIPPED_QUESTION_ID_LIST =
            "org.adaptlab.chpir.android.survey.extra_skipped_id_list";
    public final static String EXTRA_TABLE_ID =
            "org.adaptlab.chpir.android.survey.table_id";
    private static final String TAG = "MultipleQuestionsFragment";
    private Display mDisplay;
    private Survey mSurvey;
    private List<Question> mQuestions;
    private String mTableIdentifier;
    private TextView mDisplayInstructionsText;
    private int mOptionWidth;

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    protected abstract void clearRegularResponseUI(int position);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mSurveyFragment == null) return;
        if (savedInstanceState != null) {
            mTableIdentifier = savedInstanceState.getString(EXTRA_TABLE_ID);
        } else if (getArguments() != null) {
            mTableIdentifier = getArguments().getString(EXTRA_TABLE_ID);
        }
        mDisplay = mSurveyFragment.getDisplay();
        mSurvey = mSurveyFragment.getSurvey();
        mQuestions = getTableQuestions();
        for (Question curQuestion : mQuestions) {
            setSpecialResponseSkips(curQuestion);
        }
    }

    protected List<Question> getTableQuestions() {
        List<Question> tableQuestions = new ArrayList<>();
        for (Question question : mSurveyFragment.getDisplayQuestions(mDisplay)) {
            if (question.getTableIdentifier().equals(mTableIdentifier))
                tableQuestions.add(question);
        }
        return tableQuestions;
    }

    protected List<Option> getTableOptions() {
        return mSurveyFragment.getOptions().get(mQuestions.get(0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question, parent, false);
        mDisplayInstructionsText = v.findViewById(R.id.displayInstructions);
        setDisplayInstructions();
        setOptionWidth();
        String questionRange = getQuestionRange() + "\n";
        String questionInstructions = getInstructions();
        SpannableString spannableText = new SpannableString(questionRange + questionInstructions);
        spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary_text)),
                0, spannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, questionRange.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.ITALIC), questionRange.length(),
                spannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView textView = v.findViewById(R.id.spannedTextView);
        textView.setText(spannableText);

        v.findViewById(R.id.optionSetInstructions).setVisibility(View.GONE);
        v.findViewById(R.id.response_component).setVisibility(View.GONE);

        ViewGroup questionComponent2 = (LinearLayout) v.findViewById(R.id.responseLayout2);
        questionComponent2.setVisibility(View.VISIBLE);
        createQuestionComponent(questionComponent2);

        // Hide special responses UI
        v.findViewById(R.id.specialResponseButtons).setVisibility(View.GONE);
        v.findViewById(R.id.clearResponsesButton).setVisibility(View.GONE);
        return v;
    }

    protected void setDisplayInstructions() {
        List<DisplayInstruction> displayInstructions = mSurveyFragment.getDisplayInstructions();
        if (displayInstructions != null && displayInstructions.size() > 0) {
            StringBuilder instructions = new StringBuilder();
            int index = 0;
            for (DisplayInstruction displayInstruction : displayInstructions) {
                index += 1;
                if (displayInstruction.getPosition() == mQuestions.get(0).getNumberInInstrument()) {
                    Instruction instruction = mSurveyFragment.getInstruction(displayInstruction.getInstructionId());
                    if (instruction != null) {
                        instructions.append(instruction.getText(getInstrument()));
                        if (index != displayInstructions.size()) {
                            instructions.append("\n");
                        }
                    }
                }
            }
            if (instructions.length() > 0) {
                mDisplayInstructionsText.setVisibility(View.VISIBLE);
                mDisplayInstructionsText.setText(styleTextWithHtml(instructions.toString()));
            }
        }
    }

    private String getQuestionRange() {
        return mQuestions.get(0).getPosition() + " - " + mQuestions.get(mQuestions.size
                () - 1).getPosition();
    }

    private String getInstructions() {
        StringBuilder instructions = new StringBuilder();
        String instructionsText = getQuestionInstructions(mQuestions.get(0));
        if (!isEmpty(instructionsText)) {
            instructions.append(styleTextWithHtml(instructionsText));
        }
        OptionSet optionSet = mSurveyFragment.getOptionSet(mQuestions.get(0).getRemoteOptionSetId());
        if (optionSet != null && !isEmpty(optionSet.getInstructions())) {
            instructions.append("\n").append(styleTextWithHtml(optionSet.getInstructions()));
        }
        return instructions.toString();
    }

    public void setSpecialResponse(Question question, String specialResponse) {
        Response response = mSurvey.getResponseByQuestion(question);
        if (response != null) {
            response.setSpecialResponse(specialResponse);
            response.setResponse("");
            response.setDeviceUser(AuthUtils.getCurrentUser());
            response.setTimeEnded(new Date());
            mSurvey.setLastUpdated(new Date());
            saveResponseInBackground(response);
            setSpecialResponseSkips(question);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_DISPLAY_ID, mDisplay.getRemoteId());
        outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
        outState.putString(EXTRA_TABLE_ID, mTableIdentifier);
    }

    protected void toggleLoadingStatus() {
        List<Question> displayQuestions = mSurveyFragment.getDisplayQuestions(mDisplay);
        if (displayQuestions.get(displayQuestions.size() - 1).equals(mQuestions.get(mQuestions.size() - 1))) {
            mSurveyFragment.toggleLoadingStatus();
        }
    }

    private void setOptionWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float margin = getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
        float totalWidth = (displayMetrics.widthPixels - (margin * 2)) / 2;
        int headerCount = getTableOptions().size();
        for (Question question : mQuestions) {
            if (question.hasSpecialOptions()) {
                headerCount += 1;
                break;
            }
        }
        mOptionWidth = (int) totalWidth / headerCount;
    }

    protected String[] getTableHeaders() {
        List<Option> options = getTableOptions();
        String[] headers = new String[options.size()];
        for (int k = 0; k < options.size(); k++) {
            headers[k] = getOptionText(options.get(k));
        }
        return headers;
    }

    protected int getOptionWidth() {
        return mOptionWidth;
    }

    protected void addSpecialResponseUI(final int k, final Question q, LinearLayout linearLayout, final Button specialResponseButton) {
        if (q.hasSpecialOptions()) {
            specialResponseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog(specialResponseButton, q, k);
                }
            });
            linearLayout.addView(specialResponseButton);
            deserializeSpecialResponse(q, specialResponseButton);
        }
    }

    private void deserializeSpecialResponse(Question question, Button button) {
        Response response = getSurvey().getResponseByQuestion(question);
        if (response == null || TextUtils.isEmpty(response.getSpecialResponse())) return;
        button.setText(response.getSpecialResponse());
    }

    private void createDialog(final Button button, final Question q, final int pos) {
        final String[] optionsArray = new String[q.specialOptions().size() + 1];
        for (int j = 0; j < q.specialOptions().size(); j++) {
            optionsArray[j] = q.specialOptions().get(j).getText(getInstrument());
        }
        optionsArray[q.specialOptions().size()] = "";

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_special_response)
                .setItems(optionsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        button.setText(optionsArray[which]);
                        setSpecialResponse(q, optionsArray[which]);
                        clearRegularResponseUI(pos);
                    }
                })
                .create().show();
    }

    protected List<Question> getQuestions() {
        return mQuestions;
    }

    private NextQuestion getNextQuestion(Question question, Option selectedOption) {
        return new Select().from(NextQuestion.class).where("OptionIdentifier = ? AND " +
                "QuestionIdentifier = ? AND " + "RemoteInstrumentId = ?", selectedOption
                .getIdentifier(), question.getQuestionIdentifier(), question.getInstrument()
                .getRemoteId())
                .executeSingle();
    }

    private void setResponseSkips(Question question, int responseIndex) {
        if (question.hasSingleResponse() && question.hasRegularOptionSkips(
                question.getInstrument()) && responseIndex != -1) {
            if ((question.isOtherQuestionType() || question.isDropDownQuestionType()) &&
                    responseIndex == question.options().size()) {
                mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), question
                        .getQuestionIdentifier(), question.getQuestionIdentifier());
                mSurveyFragment.setMultipleSkipQuestions(null, null, question);

            } else if (responseIndex < question.options().size()) {
                Option selectedOption = question.options().get(responseIndex);
                NextQuestion skipOption = getNextQuestion(question, selectedOption);
                if (skipOption != null) {
                    mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), skipOption
                            .getNextQuestionIdentifier(), question.getQuestionIdentifier());
                } else if (question.hasRegularOptionSkips(question.getInstrument())) {
                    mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), question
                            .getQuestionIdentifier(), question.getQuestionIdentifier());
                }
                if (question.isMultipleSkipQuestion(question.getInstrument())) {
                    mSurveyFragment.setMultipleSkipQuestions(selectedOption, null, question);
                }
            }
        } else if (!TextUtils.isEmpty(getSurvey().getResponseByQuestion(question).getText())) {
            mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), question
                    .getQuestionIdentifier(), question.getQuestionIdentifier());
        }
    }

    private void setSpecialResponseSkips(Question question) {
        Response curResponse = mSurvey.getResponseByQuestion(question);
        if (curResponse != null) {
            if (!TextUtils.isEmpty(curResponse.getSpecialResponse()) && question.hasSpecialOptions()) {
                Option specialOption = new Select("Options.*").distinct().from(Option.class)
                        .innerJoin(OptionInOptionSet.class)
                        .on("OptionInOptionSets.RemoteOptionSetId = ?", question.getRemoteSpecialOptionSetId())
                        .where("Options.Text = ? AND OptionInOptionSets.RemoteOptionId = Options" +
                                ".RemoteId", curResponse.getSpecialResponse())
                        .executeSingle();
                if (specialOption != null) {
                    NextQuestion specialSkipOption = getNextQuestion(question, specialOption);
                    if (specialSkipOption != null) {
                        mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(),
                                specialSkipOption.getNextQuestionIdentifier(), question.getQuestionIdentifier());
                    } else if (question.hasSpecialOptionSkips(question.getInstrument())) {
                        mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), question
                                .getQuestionIdentifier(), question.getQuestionIdentifier());
                    }
                }
                if (question.isMultipleSkipQuestion(question.getInstrument()) && !TextUtils.isEmpty(curResponse.getSpecialResponse())) {
                    mSurveyFragment.setMultipleSkipQuestions(specialOption, null, question);
                }
            }
        }
    }

    protected Display getDisplay() {
        return mDisplay;
    }

    protected Survey getSurvey() {
        return mSurvey;
    }

    protected void setResponseIndex(Question q, int checkedId) {
        setResponseSkips(q, checkedId);
        saveResponse(q, checkedId, false);
    }

    protected Instrument getInstrument() {
        return mSurveyFragment.getInstrument();
    }

    protected void createResponse(Question question) {
        Response response = mSurveyFragment.getResponses().get(question.getQuestionIdentifier());
        if (response == null) {
            response = getResponse(question);
            getSurvey().setLastUpdated(new Date());
            saveResponseInBackground(response);
            mSurveyFragment.getResponses().put(question.getQuestionIdentifier(), response);
        }
    }

    @NonNull
    private Response getResponse(Question question) {
        Response response = new Response();
        response.setQuestion(question);
        response.setSurvey(getSurvey());
        response.setTimeStarted(new Date());
        return response;
    }

    protected void saveResponse(Question question, int checkedId, boolean isChecked) {
        Response response = mSurveyFragment.getResponses().get(question.getQuestionIdentifier());
        if (response == null) {
            response = getResponse(question);
        }
        response.setResponse(String.valueOf(checkedId));
        response.setTimeEnded(new Date());
        mSurvey.setLastUpdated(new Date());
        saveResponseInBackground(response);
        checkForCriticalResponses(question, response);
    }

    protected void setResponseHeader(HeaderHolder holder, String text, Context context) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        textView.setWidth(getOptionWidth());
        textView.setPadding(1, 1, 1, 1);
        holder.optionsPart.addView(textView);
    }

    protected class HeaderHolder extends RecyclerView.ViewHolder {
        public TextView questionText;
        public LinearLayout optionsPart;

        public HeaderHolder(View view) {
            super(view);
            questionText = view.findViewById(R.id.questionColumn);
            optionsPart = view.findViewById(R.id.optionsPart);
        }
    }

}