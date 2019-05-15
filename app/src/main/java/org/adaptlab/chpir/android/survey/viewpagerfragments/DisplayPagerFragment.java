package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.QuestionAdapter;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.OptionSet;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.DisplayQuestion;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.viewmodelfactories.DisplayInstructionViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.DisplayQuestionViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.DisplayResponseViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayInstructionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayQuestionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayResponseViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.InstructionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.OptionSetOptionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.OptionSetViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.OptionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;

public class DisplayPagerFragment extends Fragment {
    public final static String EXTRA_DISPLAY_POSITION = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_POSITION";
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_DISPLAY_ID = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";

    private static final String TAG = "DisplayPagerFragment";

    private QuestionAdapter mQuestionAdapter;
    private Survey mSurvey;
    private List<Question> mQuestions;
    private LongSparseArray<Response> mResponses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        long instrumentId = getArguments().getLong(EXTRA_INSTRUMENT_ID, -1);
        if (instrumentId == -1) return;
        long displayId = getArguments().getLong(EXTRA_DISPLAY_ID, -1);
        if (displayId == -1) return;
        int displayPosition = getArguments().getInt(EXTRA_DISPLAY_POSITION, 1);
        String surveyUUID = getArguments().getString(EXTRA_SURVEY_UUID, null);
        if (surveyUUID == null) return;

        mQuestionAdapter = new QuestionAdapter(this.getContext());

        setSurvey(surveyUUID);
        setDisplayQuestions(instrumentId, displayPosition);
        setDisplayResponses(surveyUUID, instrumentId, displayId);
        setInstructions();
        setDisplayInstruction(instrumentId, displayId);
        setOptionSets();
        setOptionSetOptions();
        setOptions();
    }

    private void setDisplayResponses(String surveyUUID, long instrumentId, long displayId) {
        DisplayResponseViewModelFactory factory = new DisplayResponseViewModelFactory(getActivity().getApplication(), surveyUUID, instrumentId, displayId);
        DisplayResponseViewModel viewModel = ViewModelProviders.of(this, factory).get(DisplayResponseViewModel.class);
        viewModel.getDisplayResponses().observe(this, new Observer<List<Response>>() {
            @Override
            public void onChanged(@Nullable List<Response> responses) {
                mQuestionAdapter.setResponses(responses);
                mResponses = new LongSparseArray<>();
                for (Response response : responses) {
                    mResponses.put(response.getQuestionRemoteId(), response);
                }
                initializeResponses();
            }
        });
    }

    private void setSurvey(String surveyUUID) {
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getActivity().getApplication(), surveyUUID);
        SurveyViewModel surveyViewModel = ViewModelProviders.of(getActivity(), factory).get(SurveyViewModel.class);
        surveyViewModel.getSurvey().observe(this, new Observer<Survey>() {
            @Override
            public void onChanged(@Nullable Survey survey) {
                mSurvey = survey;
                mQuestionAdapter.setSurvey(mSurvey);
            }
        });
    }

    private void initializeResponses() {
        if (mSurvey != null && mResponses != null && mQuestions != null) {
            if (mResponses.size() == mQuestions.size()) return;
            ResponseRepository responseRepository = new ResponseRepository(getActivity().getApplication());
            List<Response> responses = new ArrayList<>();
            for (Question question : mQuestions) {
                Response response = mResponses.get(question.getRemoteId());
                if (response == null) {
                    response = new Response();
                    response.setSurveyUUID(mSurvey.getUUID());
                    response.setQuestionIdentifier(question.getQuestionIdentifier());
                    response.setQuestionRemoteId(question.getRemoteId());
                    response.setQuestionVersion(question.getQuestionVersion());
                    response.setText(BLANK);
                    response.setSpecialResponse(BLANK);
                    response.setOtherResponse(BLANK);
                    response.setTimeStarted(new Date());
                    responses.add(response);
                }
            }
            if (responses.size() > 0) {
                responseRepository.insertAll(responses);
            }
        }
    }

    private void setOptions() {
        OptionViewModel viewModel = ViewModelProviders.of(getActivity()).get(OptionViewModel.class);
        viewModel.getAllOptions().observe(this, new Observer<List<Option>>() {
            @Override
            public void onChanged(@Nullable List<Option> options) {
                LongSparseArray<Option> array = new LongSparseArray<>();
                for (Option option : options) {
                    array.put(option.getRemoteId(), option);
                }
                mQuestionAdapter.setOptions(array);
            }
        });
    }

    private void setOptionSetOptions() {
        OptionSetOptionViewModel viewModel = ViewModelProviders.of(getActivity()).get(OptionSetOptionViewModel.class);
        viewModel.getAllOptionSetOptions().observe(this, new Observer<List<OptionSetOption>>() {
            @Override
            public void onChanged(@Nullable List<OptionSetOption> optionSetOptions) {
                LongSparseArray<List<OptionSetOption>> array = new LongSparseArray<>();
                for (OptionSetOption optionSetOption : optionSetOptions) {
                    List<OptionSetOption> list = array.get(optionSetOption.getOptionSetRemoteId());
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(optionSetOption);
                    array.put(optionSetOption.getOptionSetRemoteId(), list);
                }
                mQuestionAdapter.setOptionSetOptions(array);
            }
        });
    }

    private void setOptionSets() {
        OptionSetViewModel optionSetViewModel = ViewModelProviders.of(getActivity()).get(OptionSetViewModel.class);
        optionSetViewModel.getAllOptionSets().observe(this, new Observer<List<OptionSet>>() {
            @Override
            public void onChanged(@Nullable List<OptionSet> optionSets) {
                LongSparseArray<OptionSet> array = new LongSparseArray<>();
                for (OptionSet optionSet : optionSets) {
                    array.put(optionSet.getRemoteId(), optionSet);
                }
                mQuestionAdapter.setOptionSets(array);
            }
        });
    }

    /**
     * @param instrumentId Instrument ID contained in the fragment's arguments
     * @param displayId    Display ID contained in the fragment's arguments
     *                     'this' (fragment) is passed to ViewModelProviders so that each DisplayFragment gets its own list of DisplayInstructions.
     *                     Passing the activity would result in each DisplayFragment having the same DisplayInstructions
     */
    private void setDisplayInstruction(long instrumentId, long displayId) {
        DisplayInstructionViewModelFactory factory = new DisplayInstructionViewModelFactory(getActivity().getApplication(), instrumentId, displayId);
        DisplayInstructionViewModel displayInstructionViewModel = ViewModelProviders.of(this, factory).get(DisplayInstructionViewModel.class);
        displayInstructionViewModel.getDisplayInstructions().observe(this, new Observer<List<DisplayInstruction>>() {
            @Override
            public void onChanged(@Nullable List<DisplayInstruction> displayInstructions) {
                mQuestionAdapter.setDisplayInstructions(displayInstructions);
            }
        });
    }

    /**
     * The activity (Survey2Activity is set as the owner of the view model.
     * This results in all the instructions being shared across all the instances of DisplayFragment
     */
    private void setInstructions() {
        InstructionViewModel instructionViewModel = ViewModelProviders.of(getActivity()).get(InstructionViewModel.class);
        instructionViewModel.getAllInstructions().observe(this, new Observer<List<Instruction>>() {
            @Override
            public void onChanged(@Nullable List<Instruction> instructions) {
                LongSparseArray<Instruction> array = new LongSparseArray<>();
                for (Instruction instruction : instructions) {
                    array.put(instruction.getRemoteId(), instruction);
                }
                mQuestionAdapter.setInstructions(array);
            }
        });
    }

    /**
     * @param instrumentId    Instrument ID contained in the fragment's arguments
     * @param displayPosition Display Position contained in the fragment's arguments
     *                        'this' (fragment) is passed to ViewModelProviders so that each DisplayFragment gets its own list of questions.
     *                        Passing the activity would result in each DisplayFragment having the same questions
     */
    private void setDisplayQuestions(long instrumentId, int displayPosition) {
        DisplayQuestionViewModelFactory factory = new DisplayQuestionViewModelFactory(getActivity().getApplication(), instrumentId, displayPosition);
        DisplayQuestionViewModel displayQuestionViewModel = ViewModelProviders.of(this, factory).get(DisplayQuestionViewModel.class);
        displayQuestionViewModel.getDisplayQuestion().observe(this, new Observer<DisplayQuestion>() {
            @Override
            public void onChanged(@Nullable DisplayQuestion displayQuestion) {
                mQuestionAdapter.setQuestions(displayQuestion.questions);
                mQuestions = displayQuestion.questions;
                initializeResponses();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_display, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.displayRecyclerView);
        recyclerView.setAdapter(mQuestionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManagerWrapper(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    /**
     * Disable predictive animation in layout manager
     * Source: https://stackoverflow.com/questions/46563485/diffresult-dispatching-lead-to-inconsistency-detected-invalid-view-holder-adap
     */
    private class LinearLayoutManagerWrapper extends LinearLayoutManager {

        LinearLayoutManagerWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

}
