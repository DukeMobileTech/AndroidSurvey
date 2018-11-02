package org.adaptlab.chpir.android.survey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.DisplayInstruction;
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
    public static final int MIN_HEIGHT = 80;
    public static final int MARGIN_10 = 10;
    public static final int MARGIN_0 = 0;

    protected abstract void createQuestionComponent(ViewGroup questionComponent);
    protected abstract void clearRegularResponseUI(int position);

    private static final String TAG = "MultipleQuestionsFragment";
    private Display mDisplay;
    private Survey mSurvey;
    private List<Question> mQuestions;
    private String mTableIdentifier;
    private TextView mDisplayInstructionsText;
    private int mOptionWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mDisplay = Display.findByRemoteId(savedInstanceState.getLong(EXTRA_DISPLAY_ID));
            mSurvey = Model.load(Survey.class, savedInstanceState.getLong(EXTRA_SURVEY_ID));
            mTableIdentifier = savedInstanceState.getString(EXTRA_TABLE_ID);
        } else {
            if (getArguments() != null) {
                mDisplay = Display.findByRemoteId(getArguments().getLong(EXTRA_DISPLAY_ID));
                mSurvey = Model.load(Survey.class, getArguments().getLong(EXTRA_SURVEY_ID));
                mTableIdentifier = getArguments().getString(EXTRA_TABLE_ID);
            }
        }

        // Allow both portrait and landscape orientations
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        mQuestions = mDisplay.tableQuestions(mTableIdentifier);
        for(Question curQuestion: mQuestions){
            setSpecialResponseSkips(curQuestion);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question, parent, false);
        mDisplayInstructionsText = (TextView) v.findViewById(R.id.displayInstructions);
        setDisplayInstructions();
        TextView questionNumber = (TextView) v.findViewById(R.id.questionNumber);
        questionNumber.setText(getQuestionRange());
        TextView questionInstructions = (TextView) v.findViewById(R.id.question_instructions);
        questionInstructions.setTypeface(getInstrument().getTypeFace(getActivity()));
        if (isEmpty(getInstructions())) {
            ((LinearLayout) questionInstructions.getParent()).setVisibility(View.GONE);
        } else {
            questionInstructions.append(styleTextWithHtml(getInstructions()));
            questionInstructions.setGravity(Gravity.END);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ViewGroup viewGroup = (ViewGroup) questionInstructions.getParent();
                viewGroup.setBackground(getResources().getDrawable(R.drawable.response_component_layout));
            }
        }
        TextView questionText = (TextView) v.findViewById(R.id.question_text);
        if (questionText != null) questionText.setVisibility(View.GONE);
        ViewGroup questionComponent1 = (LinearLayout) v.findViewById(R.id.responseLayout1);
        questionComponent1.setVisibility(View.GONE);
        ViewGroup questionComponent2 = (LinearLayout) v.findViewById(R.id.responseLayout2);
        questionComponent2.setVisibility(View.VISIBLE);
        createQuestionComponent(questionComponent2);

        // Hide special responses UI
        v.findViewById(R.id.special_responses_container).setVisibility(View.GONE);
        return v;
    }

    protected void setDisplayInstructions() {
        List<DisplayInstruction> displayInstructions = mSurveyFragment.getDisplayInstructions(mDisplay);
        if (displayInstructions != null && displayInstructions.size() > 0) {
            StringBuilder instructions = new StringBuilder();
            for (DisplayInstruction instruction : displayInstructions) {
                if (instruction.getPosition() == mQuestions.get(0).getNumberInInstrument()) {
                    instructions.append(instruction.getInstructions()).append("<br>");
                }
            }
            if (instructions.length() > 0) {
                ((LinearLayout) mDisplayInstructionsText.getParent()).setVisibility(View.VISIBLE);
                mDisplayInstructionsText.setText(styleTextWithHtml(instructions.toString()));
            }
        }
    }

    private String getQuestionRange() {
        return mQuestions.get(0).getNumberInInstrument() + " - " + mQuestions.get(mQuestions.size
                () - 1).getNumberInInstrument();
    }

    private String getInstructions() {
        StringBuilder instructions = new StringBuilder();
        Question question = mQuestions.get(0);
        if (!isEmpty(question.getInstructions())) {
            instructions.append(question.getInstructions()).append("<br>");
        }
        OptionSet optionSet = OptionSet.findByRemoteId(mQuestions.get(0).getRemoteOptionSetId());
        if (optionSet != null && !isEmpty(optionSet.getInstructions())) {
            instructions.append(optionSet.getInstructions());
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

    protected void hideIndeterminateProgressBar() {
        List<Question> displayQuestions = mSurveyFragment.getQuestions(mDisplay);
        if (displayQuestions.get(displayQuestions.size() - 1).equals(mQuestions.get(mQuestions.size() - 1))) {
            mSurveyFragment.hideIndeterminateProgressBar();
        }
    }

//    @Override
//    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        final NestedScrollView nestedScrollView = view.findViewById(R.id.grid_scroll_view);
//        nestedScrollView.post(new Runnable() {
//            @Override
//            public void run() {
//                final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
//                int tableHeaderHeight = view.findViewById(R.id.table_header).getHeight();
//
//                int tableBodyHeight = view.findViewById(R.id.table_body_question_text).getHeight();
//                NestedScrollView tableScrollView = view.findViewById(R.id.grid_scroll_view);
//                int scrollViewHeight = tableScrollView.getHeight();
//                int activityVerticalMargin = (int) getActivity().getResources().getDimension(R
//                        .dimen.activity_vertical_margin);
//                int progressBarHeight = getActivity().findViewById(R.id.progress_bar).getHeight();
//                int remainingScreenHeight = screenHeight - activityVerticalMargin -
//                        progressBarHeight;
//                int viewHeight = tableScrollView.getHeight();
//                if (scrollViewHeight < tableBodyHeight && remainingScreenHeight >
//                        tableBodyHeight) {
//                    viewHeight = tableBodyHeight;
//                } else if (scrollViewHeight < tableBodyHeight && remainingScreenHeight >
//                        scrollViewHeight) {
//                    viewHeight = remainingScreenHeight;
//                } else if (remainingScreenHeight < 0 && scrollViewHeight < tableBodyHeight &&
//                        tableBodyHeight < screenHeight) {
//                    viewHeight = tableBodyHeight;
//                } else if (remainingScreenHeight < 0 && scrollViewHeight < tableBodyHeight &&
//                        tableBodyHeight > screenHeight) {
//                    viewHeight = screenHeight - activityVerticalMargin - progressBarHeight -
//                            tableHeaderHeight;
//                }
//                ViewGroup.LayoutParams params = tableScrollView.getLayoutParams();
//                params.height = viewHeight;
//                tableScrollView.setLayoutParams(params);
//            }
//        });
//    }

//    protected void setTableHeaderOptions(View v) {
//        LinearLayout headerTableLayout = (LinearLayout) v.findViewById(R.id.table_options_header);
//        List<Option> headerLabels = getDisplay().tableOptions(getTableIdentifier());
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        float margin = getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
//        float totalWidth = (displayMetrics.widthPixels - margin * 2) / 2;
//
//        int headerCount = headerLabels.size();
//        for (Question question : getQuestions()) {
//            if (question.hasSpecialOptions()) {
//                headerCount += 1;
//                break;
//            }
//        }
//        mOptionWidth = (int) totalWidth / headerCount;
//
//        for (int k = 0; k < headerLabels.size(); k++) {
//            TextView textView = getHeaderTextView(headerLabels.get(k).getText(getInstrument()));
//            textView.setWidth(mOptionWidth);
//            headerTableLayout.addView(textView);
//        }
//
//        if (headerCount != headerLabels.size()) {
//            TextView textView = getHeaderTextView(getString(R.string.special_response_abbrv));
//            textView.setWidth(mOptionWidth);
//            headerTableLayout.addView(textView);
//        }
//
//    }

    protected TextView getHeaderTextView(String text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        return textView;
    }

    protected int getOptionWidth() {
        return mOptionWidth;
    }

    protected void addSpecialResponseUI(final int k, final Question q, LinearLayout choiceRow, final Button specialResponseButton) {
        if (q.hasSpecialOptions()) {
            LinearLayout specialResponseLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            specialResponseLayout.setLayoutParams(params);

            specialResponseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog(specialResponseButton, q, k);
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.TRANSPARENT);
                gd.setStroke(1, 0xFF000000);
                specialResponseButton.setBackground(gd);
            } else {
                specialResponseButton.setBackgroundColor(Color.TRANSPARENT);
            }
            specialResponseLayout.addView(specialResponseButton);
            choiceRow.addView(specialResponseLayout);
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

    protected List<Question> getQuestionExcludingSkip() {
        List<Question> questionLst = new ArrayList<>();
        for (Question curQuestion : mQuestions) {
            if (!mSurveyFragment.getQuestionsToSkipSet().contains(curQuestion)) {
                questionLst.add(curQuestion);
            }
        }
        return questionLst;
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
                mSurveyFragment.setMultipleSkipQuestions(null, question);

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
                    mSurveyFragment.setMultipleSkipQuestions(selectedOption, question);
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
                    mSurveyFragment.setMultipleSkipQuestions(specialOption, question);
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

    protected String getTableIdentifier() {
        return mTableIdentifier;
    }

    protected void setResponseIndex(Question q, int checkedId) {
        setResponseSkips(q, checkedId);
        saveResponse(q, checkedId, false);
    }

    protected Instrument getInstrument() {
        return mSurvey.getInstrument();
    }

    protected void createResponse(Question question) {
        Response response = getSurvey().getResponseByQuestion(question);
        if (response == null) {
            response = new Response();
            response.setQuestion(question);
            response.setSurvey(getSurvey());
            response.setTimeStarted(new Date());
            getSurvey().setLastUpdated(new Date());
            saveResponseInBackground(response);
        }
    }

    protected void saveResponse(Question question, int checkedId, boolean isChecked) {
        Response response = mSurvey.getResponseByQuestion(question);
        if (response == null) {
            response = new Response();
            response.setQuestion(question);
            response.setSurvey(mSurvey);
        }
        response.setResponse(String.valueOf(checkedId));
        response.setTimeEnded(new Date());
        mSurvey.setLastUpdated(new Date());
        saveResponseInBackground(response);
    }

}