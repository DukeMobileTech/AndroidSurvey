package org.adaptlab.chpir.android.survey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.adaptlab.chpir.android.survey.models.CriticalResponse;
import org.adaptlab.chpir.android.survey.models.Instruction;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtml;

public abstract class QuestionFragment extends Fragment {
    protected SurveyFragment mSurveyFragment;
    protected DisplayFragment mDisplayFragment;

    protected abstract void unSetResponse();

    protected abstract void createQuestionComponent(ViewGroup questionComponent);

    protected abstract void deserialize(String responseText);

    protected abstract String serialize();

    protected abstract void setDisplayInstructions();

    protected abstract void toggleLoadingStatus();

    private final String TAG = "QuestionFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDisplayFragment = (DisplayFragment) getParentFragment();
        if (mDisplayFragment != null) {
            mSurveyFragment = mDisplayFragment.getSurveyFragment();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        toggleLoadingStatus();
    }

    /*
    This is needed to hide the indeterminate progress bar when showing fragments that have previously been
    added and hidden. The onStart lifecycle event is not called when using the method FragmentTransaction.show(fragment)
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            toggleLoadingStatus();
        }
    }

    protected void setLoopQuestions(Question question, Response response) {
        if (question == null || response == null) return;
        if (question.getQuestionType().equals(Question.QuestionType.INTEGER) &&
                question.getLoopQuestionCount() > 0) {
            mSurveyFragment.setIntegerLoopQuestions(question, response.getText());
        } else if (question.isMultipleResponseLoop() && question.getLoopQuestionCount() > 0) {
            mSurveyFragment.setMultipleResponseLoopQuestions(question, response.getText());
        }
    }

    protected void saveResponseInBackground(final Response response) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                response.save();
                mSurveyFragment.getSurvey().save();
            }
        });
    }

    protected String getQuestionInstructions(Question question) {
        String qInstructions;
        if (question.getInstructionId() == null) {
            qInstructions = null;
        } else {
            Instruction instruction = mSurveyFragment.getInstruction(question.getInstructionId());
            if (instruction == null) {
                qInstructions = null;
            } else {
                qInstructions = instruction.getText(mSurveyFragment.getInstrument());
            }
        }
        return qInstructions;
    }

    protected void checkForCriticalResponses(Question question, Response response) {
        if (question.hasSingleResponse() || question.hasMultipleResponses() || question.hasListResponses()) {
            if (TextUtils.isEmpty(response.getText())) return;
            List<CriticalResponse> criticalResponses = mSurveyFragment.getCriticalResponses(question.getQuestionIdentifier());
            if (criticalResponses == null || criticalResponses.size() == 0) return;
            List<Option> options = mSurveyFragment.getOptions().get(question);
            if (options == null || options.size() == 0) return;
            String[] indices = response.getText().split(Response.LIST_DELIMITER);
            List<Option> selectedOptions = new ArrayList<>();
            for (int k = 0; k < indices.length; k++) {
                int index = Integer.parseInt(indices[k]);
                selectedOptions.add(options.get(index));
            }
            List<CriticalResponse> activatedResponses = new ArrayList<>();

            for (Option option : selectedOptions) {
                for (CriticalResponse criticalResponse : criticalResponses) {
                    if (criticalResponse.getOptionIdentifier().equals(option.getIdentifier())) {
                        activatedResponses.add(criticalResponse);
                    }
                }
            }

            if (activatedResponses.size() > 0) {
                String[] warnings = new String[activatedResponses.size()];
                for (int k = 0; k < activatedResponses.size(); k++) {
                    Instruction instruction = mSurveyFragment.getInstruction(activatedResponses.get(k).getInstructionId());
                    warnings[k] = activatedResponses.get(k).getOptionIdentifier() + ": " +
                            styleTextWithHtml(instruction.getText(mSurveyFragment.getInstrument()));
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View content = LayoutInflater.from(getActivity()).inflate(R.layout
                        .critical_responses_dialog, null);
                ListView listView = content.findViewById(R.id.critical_list);
                listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout
                        .simple_selectable_list_item, warnings));

                builder.setTitle(R.string.critical_message_title)
                        .setView(content)
                        .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                            }
                        });
                final AlertDialog criticalDialog = builder.create();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        criticalDialog.dismiss();
                    }
                });
                criticalDialog.show();
            }
        }
    }

    protected Spanned getQuestionText(Question question) {
        String text = "";
        if (question.isFollowUpQuestion()) { // TODO: 12/4/18 Remove db call
            String followUpText = question.getFollowingUpText(mSurveyFragment.getResponses(), getActivity());
            if (followUpText != null) {
                text = followUpText;
            }
        } else if (question.hasRandomizedFactors()) {
            text = question.getRandomizedText(mSurveyFragment.getResponses().get(question.getQuestionIdentifier()));
        } else if (!TextUtils.isEmpty(question.getLoopSource())) {
            String causeId = question.getQuestionIdentifier().split("_")[0];
            Response response = mSurveyFragment.getResponses().get(causeId);
            if (response == null || TextUtils.isEmpty(response.getText())) {
                text = question.getText();
            } else {
                String responseText = "";
                String[] responses = response.getText().split(Response.LIST_DELIMITER, -1);
                Question causeQuestion = mSurveyFragment.getQuestions().get(causeId);
                if (causeQuestion.isSingleSelect()) {
                    int index = Integer.parseInt(responses[question.getLoopNumber()]);
                    responseText = mSurveyFragment.getOptions().get(causeQuestion).get(index).getText(mSurveyFragment.getInstrument());
                } else if (causeQuestion.hasMultipleResponses()) {
                    if (Arrays.asList(responses).contains(Integer.toString(question.getLoopNumber()))) {
                        responseText = mSurveyFragment.getOptions().get(causeQuestion).get(question.getLoopNumber()).getText(mSurveyFragment.getInstrument());
                    }
                } else {
                    if (question.getLoopNumber() < responses.length) {
                        responseText = responses[question.getLoopNumber()]; //Keep empty values
                    }
                }
                if (TextUtils.isEmpty(responseText)) {
                    text = question.getText();
                } else {
                    text = question.getText();
                    int begin = text.indexOf("[");
                    int last = text.indexOf("]");
                    if (begin != -1 && last != -1 && begin < last) {
                        text = text.replace(text.substring(begin, last + 1), responseText);
                    } else {
                        text = question.getText().replace(question.getTextToReplace(), responseText);
                    }
                }
            }
        } else {
            text = question.getText();
        }
        return styleTextWithHtml(text);
    }

}
