package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.models.Display;
import org.adaptlab.chpir.android.survey.models.FollowUpQuestion;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.questionfragments.MultipleSelectMultipleQuestionsFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SingleSelectMultipleQuestionsFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.isEmpty;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayFragment extends Fragment {
    private static final String TAG = "DisplayFragment";
    private static final int OFFSET = 1000000;

    private Survey mSurvey;
    private Display mDisplay;
    private LinearLayout mDisplayLayout;

    private OnFragmentInteractionListener mListener;

    private SurveyFragment mSurveyFragment;
    private List<QuestionFragment> mQuestionFragments;

    public DisplayFragment() {
        // Required empty public constructor
    }

    public static DisplayFragment newInstance() {
        return new DisplayFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mSurveyFragment = mListener.getSurveyFragment();
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mSurveyFragment == null) return;
        mSurvey = mSurveyFragment.getSurvey();
        mDisplay = mSurveyFragment.getDisplay();
        mQuestionFragments = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display, container, false);
        mDisplayLayout = view.findViewById(R.id.displayFragmentsContainer);
        createQuestionFragments();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Scroll to the top question
        mSurveyFragment.getScrollView().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSurveyFragment.getScrollView().scrollTo(0, 0);
            }
        }, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSurveyFragment.persistSkippedQuestions();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private List<Question> displayTableQuestions(String tableIdentifier) {
        List<Question> questions = new ArrayList<>();
        for (Question question : mSurveyFragment.getQuestions(mDisplay)) {
            if (question.getTableIdentifier().equals(tableIdentifier)) {
                questions.add(question);
            }
        }
        return questions;
    }

    private void createQuestionFragments() {
        if (mSurveyFragment == null || getActivity() == null) return;
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        for (Question question : mSurveyFragment.getQuestions(mDisplay)) {
            int frameLayoutId;
            String qfTag;
            if (isEmpty(question.getTableIdentifier())) {
                // Add large offset to avoid id conflicts
                frameLayoutId = new BigDecimal(question.getRemoteId()).intValueExact() + OFFSET;
                qfTag = mSurvey.getId().toString() + "-" + question.getId().toString();
            } else {
                long sumId = 0;
                for (Question q : displayTableQuestions(question.getTableIdentifier())) {
                    sumId += q.getRemoteId();
                }
                frameLayoutId = new BigDecimal(sumId).intValueExact() + OFFSET;
                qfTag = mSurvey.getId().toString() + "-" + question.getTableIdentifier();
            }
            FrameLayout frameLayout = getActivity().findViewById(frameLayoutId);
            if (frameLayout == null) {
                frameLayout = new FrameLayout(getActivity());
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup
                        .LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT));
                frameLayout.setId(frameLayoutId);
                mDisplayLayout.addView(frameLayout);
            }
            QuestionFragment questionFragment = (QuestionFragment) getChildFragmentManager().
                    findFragmentByTag(qfTag);
            if (questionFragment == null) {
                if (isEmpty(question.getTableIdentifier())) {
                    Bundle bundle = new Bundle();
                    bundle.putString("QuestionIdentifier", question.getQuestionIdentifier());

                    questionFragment = (QuestionFragment) QuestionFragmentFactory.createQuestionFragment(question);
                    questionFragment.setArguments(bundle);
                    fragmentTransaction.add(frameLayout.getId(), questionFragment, qfTag);
                } else {
                    if (question.getQuestionType() == Question.QuestionType.SELECT_ONE) {
                        questionFragment = new SingleSelectMultipleQuestionsFragment();
                    } else if (question.getQuestionType() == Question.QuestionType
                            .SELECT_MULTIPLE) {
                        questionFragment = new MultipleSelectMultipleQuestionsFragment();
                    }
                    Bundle bundle = new Bundle();
                    ArrayList<String> questionsToSkip = new ArrayList<>();
                    for (String curSkip : mSurveyFragment.getQuestionsToSkipSet()) {
                        if (curSkip != null)
                            questionsToSkip.add(curSkip);
                    }
                    bundle.putStringArrayList(MultipleQuestionsFragment
                            .EXTRA_SKIPPED_QUESTION_ID_LIST, questionsToSkip);
                    bundle.putLong(MultipleQuestionsFragment.EXTRA_DISPLAY_ID, mDisplay.getRemoteId());
                    bundle.putLong(MultipleQuestionsFragment.EXTRA_SURVEY_ID, mSurvey.getId());
                    bundle.putString(MultipleQuestionsFragment.EXTRA_TABLE_ID, question.getTableIdentifier());
                    questionFragment.setArguments(bundle);
                    fragmentTransaction.add(frameLayout.getId(), questionFragment, qfTag);
                }
            } else {
                fragmentTransaction.show(questionFragment);
            }
            mQuestionFragments.add(questionFragment);
        }
        fragmentTransaction.commit();
        mSurveyFragment.getScrollView().fullScroll(View.FOCUS_UP);
    }

    public void hideQuestions() {
        if (!isAdded()) return;
        if (mDisplay != null && !mDisplay.getMode().equals(Display.DisplayMode.TABLE.toString())) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            HashSet<Integer> hideSet = new HashSet<>();
            List<String> ids = new ArrayList<>();
            for (Question q : getSurveyFragment().getQuestions(mDisplay)) {
                ids.add(q.getQuestionIdentifier());
            }

            for (String curSkip : mSurveyFragment.getQuestionsToSkipSet()) {
                int index = ids.indexOf(curSkip);
                if (index != -1) {
                    hideSet.add(index);
                    ft.hide(mQuestionFragments.get(index));
                }
            }
            for (int i = 0; i < mQuestionFragments.size(); i++) {
                if (!hideSet.contains(i)) {
                    ft.show(mQuestionFragments.get(i));
                }
            }
            ft.commit();
        }
    }

    protected SurveyFragment getSurveyFragment() {
        return mSurveyFragment;
    }

    protected String checkForEmptyResponses() {
        if (mSurveyFragment == null || mDisplay == null) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (Question question : mSurveyFragment.getQuestions(mDisplay)) {
            if (!mSurveyFragment.getQuestionsToSkipSet().contains(question.getQuestionIdentifier()) &&
                    mSurveyFragment.getResponses().get(question).isResponseEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (stringBuilder.length() > 0) stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(getResources().getString(R.string.question))
                            .append(" # ").append(question.getNumberInInstrument());
                }
            }
        }
        return stringBuilder.toString();
    }

    protected void reAnimateFollowUpFragment(Question currentQuestion) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (FollowUpQuestion question : currentQuestion.toFollowUpOnQuestions()) {
            int index = mDisplay.questions().indexOf(question.getFollowUpQuestion());
            if (index > -1 && index <= mDisplay.questions().size() - 1) {
                QuestionFragment qf = mQuestionFragments.get(index);
                ft.detach(qf);
                ft.attach(qf);
                ft.commit();
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        SurveyFragment getSurveyFragment();
    }

}
