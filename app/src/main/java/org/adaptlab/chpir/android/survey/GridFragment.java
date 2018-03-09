package org.adaptlab.chpir.android.survey;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.NextQuestion;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class GridFragment extends QuestionFragment {
    public final static String EXTRA_DISPLAY_ID =
            "org.adaptlab.chpir.android.survey.display_id";
    public final static String EXTRA_SURVEY_ID =
            "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_SKIPPED_QUESTION_ID_LIST =
            "org.adaptlab.chpir.android.survey.extra_skipped_id_list";
    public static final int MIN_HEIGHT = 80;
    public static final int MARGIN_10 = 10;
    public static final int MARGIN_0 = 0;

    protected void createQuestionComponent(ViewGroup questionComponent) {
    }

    private static final String TAG = "GridFragment";
    //	private Grid mGrid;
    private Display mDisplay;
    private Survey mSurvey;
    private List<Question> mQuestions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//		if (savedInstanceState != null) {
//        	mGrid = Grid.findByRemoteId(savedInstanceState.getLong(EXTRA_GRID_ID));
//        } else {
//            mGrid = Grid.findByRemoteId(getArguments().getLong(EXTRA_GRID_ID));
//        }
        if (savedInstanceState != null) {
            mDisplay = Display.findByRemoteId(savedInstanceState.getLong(EXTRA_DISPLAY_ID));
        } else {
            mDisplay = Display.findByRemoteId(getArguments().getLong(EXTRA_DISPLAY_ID));
        }
        init();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Allow both portrait and landscape orientations
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//        for (Question question : getSurveyFragment().getQuestions()) {
//            if (question.getGrid() == mGrid) {
//                mQuestions.add(question);
//            }
//        }
        mQuestions = mDisplay.questions();
    }

    @Override
    public void init() {
        long surveyId = getArguments().getLong(EXTRA_SURVEY_ID);
        if (surveyId != -1) {
            mSurvey = Model.load(Survey.class, surveyId);
            mQuestions = new ArrayList<>();
        }
    }

    @Override
    public void setSpecialResponse(String specialResponse) {
        for (Question question : mQuestions) {
            Response response = mSurvey.getResponseByQuestion(question);
            if (response != null) {
                response.setSpecialResponse(specialResponse);
                response.setDeviceUser(AuthUtils.getCurrentUser());
                response.setResponse("");
                response.save();
                deserialize(response.getText());
                if (AppUtil.DEBUG)
                    Log.i(TAG, "Saved special response: " + response.getSpecialResponse() +
                            " for question: " + question.getQuestionIdentifier());
            }
        }
    }

    @Override
    public String getSpecialResponse() {
        if (mDisplay == null && mSurvey == null) {
            return "";
        }

        for (int k = 0; k < mQuestions.size(); k++) {
            Response response = mSurvey.getResponseByQuestion(mQuestions.get(k));
            if (response != null && !response.getSpecialResponse().equals("")) {
                return response.getSpecialResponse();
            }
        }

        return "";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_DISPLAY_ID, mDisplay.getRemoteId());
        outState.putLong(EXTRA_SURVEY_ID, mSurvey.getId());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NestedScrollView nestedScrollView = (NestedScrollView) getActivity().findViewById(R
                .id.grid_scroll_view);
        nestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
                int tableHeaderHeight = getActivity().findViewById(R.id.table_header).getHeight();
                int tableBodyHeight = getActivity().findViewById(R.id.table_body_question_text)
                        .getHeight();
                NestedScrollView tableScrollView = (NestedScrollView) getActivity().findViewById
                        (R.id.grid_scroll_view);
                int scrollViewHeight = tableScrollView.getHeight();
                int activityVerticalMargin = (int) getActivity().getResources().getDimension(R
                        .dimen.activity_vertical_margin);
//                final int questionTextHeight = getActivity().findViewById(R.id
// .linear_layout_for_question_text_view).getHeight();
//                int questionIndexLabelHeight = getActivity().findViewById(R.id
// .participant_label_and_question_index).getHeight();
                int progressBarHeight = getActivity().findViewById(R.id.progress_bar).getHeight();
//                int remainingScreenHeight = screenHeight - activityVerticalMargin -
// questionTextHeight - questionIndexLabelHeight - progressBarHeight;
                int remainingScreenHeight = screenHeight - activityVerticalMargin -
                        progressBarHeight;
                int viewHeight = tableScrollView.getHeight();
                if (scrollViewHeight < tableBodyHeight && remainingScreenHeight > tableBodyHeight) {
                    viewHeight = tableBodyHeight;
                } else if (scrollViewHeight < tableBodyHeight && remainingScreenHeight >
                        scrollViewHeight) {
                    viewHeight = remainingScreenHeight;
                } else if (remainingScreenHeight < 0 && scrollViewHeight < tableBodyHeight &&
                        tableBodyHeight < screenHeight) {
                    viewHeight = tableBodyHeight;
                } else if (remainingScreenHeight < 0 && scrollViewHeight < tableBodyHeight &&
                        tableBodyHeight > screenHeight) {
//                    viewHeight = screenHeight - activityVerticalMargin -
// questionIndexLabelHeight - progressBarHeight - tableHeaderHeight;
                    viewHeight = screenHeight - activityVerticalMargin - progressBarHeight -
                            tableHeaderHeight;
                }
                ViewGroup.LayoutParams params = tableScrollView.getLayoutParams();
                params.height = viewHeight;
                tableScrollView.setLayoutParams(params);
            }
        });
    }

    protected GradientDrawable getBorder() {
        GradientDrawable border = new GradientDrawable();
        border.setStroke(1, 0xFF000000);
        return border;
    }

    protected TextView getHeaderTextView(String text) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setMinHeight(MIN_HEIGHT);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(MARGIN_10, MARGIN_0, MARGIN_10, MARGIN_0);
        return textView;
    }

    protected List<Question> getQuestions() {
        return mQuestions;
    }

//	protected Grid getGrid() {
//		return mGrid;
//	}

    protected List<Question> getQuestionExcludingSkip(){
        List<Question> questionLst = new ArrayList<>();
        for(Question curQuestion: mQuestions){
            if(!mSurveyFragment.getQuestionsToSkipSet().contains(curQuestion)){
                questionLst.add(curQuestion);
            }
        }
        return questionLst;
    }

    private NextQuestion getNextQuestion(Question question, Option selectedOption) {
        return new Select().from(NextQuestion.class).where("OptionIdentifier = ? AND " +
                "QuestionIdentifier = ? AND " + "RemoteInstrumentId = ?", selectedOption
                .getIdentifier(), question.getQuestionIdentifier(), question.getInstrument().getRemoteId())
                .executeSingle();
    }

    private void setResponseSkips(Question question, int responseIndex) {
        if (question.isSkipQuestionType() && responseIndex!=-1) {
            if ((question.isOtherQuestionType()||question.isDropDownQuestionType()) && responseIndex == question.options().size()) {
                Log.i("isOtherOrDropDown","isOtherOrDropDownQuestionType");
                mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), question
                        .getQuestionIdentifier(), question.getQuestionIdentifier());
                mSurveyFragment.setMultipleSkipQuestions(null, question);

            } else if (responseIndex < question.options().size()){
                Option selectedOption = question.options().get(responseIndex);
                NextQuestion skipOption = getNextQuestion(question,selectedOption);
                Log.i("selectedOption",selectedOption.toString()+" ");
                if (skipOption != null) {
                    Log.i("skipOption",skipOption.toString()+" ");
                    mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), skipOption
                            .getNextQuestionIdentifier(), question.getQuestionIdentifier());
                } else if (question.hasSkips(question.getInstrument())) {
                    mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), question
                            .getQuestionIdentifier(), question.getQuestionIdentifier());
                }
                if (question.isMultipleSkipQuestion(question.getInstrument())){
                    mSurveyFragment.setMultipleSkipQuestions(selectedOption, question);
                }
            }
        } else if(!TextUtils.isEmpty(mResponse.getText())){
            mSurveyFragment.setNextQuestion(question.getQuestionIdentifier(), question
                    .getQuestionIdentifier(), question.getQuestionIdentifier());
        }
    }

    protected Display getDisplay() {
        return mDisplay;
    }

    protected Survey getSurvey() {
        return mSurvey;
    }

    protected void setResponseIndex(Question q, int checkedId) {
        setResponseSkips(q,checkedId);
        new SaveResponseTask(mSurvey, q, checkedId).execute();
    }

    @Override
    protected Instrument getInstrument() {
        return mSurvey.getInstrument();
    }

    private class SaveResponseTask extends AsyncTask<Void, Void, Void> {
        private int id;
        private Question question;
        private Survey survey;

        private SaveResponseTask(Survey s, Question q, int checkedId) {
            question = q;
            id = checkedId;
            survey = s;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Response response = survey.getResponseByQuestion(question);
            if (response == null) {
                response = new Response();
                response.setQuestion(question);
                response.setSurvey(survey);
            }
            response.setResponse(String.valueOf(id));
            response.save();
            survey.save();
            return null;
        }

    }
}