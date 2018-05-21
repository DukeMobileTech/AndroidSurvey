package org.adaptlab.chpir.android.survey.questionfragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.MultipleQuestionsFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.FormatUtils.styleTextWithHtml;

public class SingleSelectMultipleQuestionsFragment extends MultipleQuestionsFragment {
    private static final String TAG = "SingleSelectMultipleQuestionsFragment";
    private int mIndex;
    private List<RadioGroup> mRadioGroups;
    private List<Question> mQuestionList;
    private LinearLayout questionTextLayout;
    private LinearLayout optionsListLinearLayout;
    private View mView;
    private HashSet<Integer> mViewToHideSet;
    private Integer[] rowHeights;
    private int mOptionWidth;

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            int checked = mRadioGroups.get(mIndex).getCheckedRadioButtonId();
            if (checked > -1) {
                ((RadioButton) mRadioGroups.get(mIndex).getChildAt(checked)).setChecked(false);
            }
        } else {
            RadioButton button = (RadioButton) mRadioGroups.get(mIndex).getChildAt(Integer
                    .parseInt(responseText));
            if (button != null) {
                button.setChecked(true);
            }
        }
    }

    private void setTableBodyContent(View v) {
        questionTextLayout = v.findViewById(R.id.table_body_question_text);
        optionsListLinearLayout = v.findViewById(R.id.table_body_options_choice);
        mRadioGroups = new ArrayList<>();
        mQuestionList = getQuestions();
        rowHeights = new Integer[mQuestionList.size()];
        for (int k = 0; k < mQuestionList.size(); k++) {
            final Question q = mQuestionList.get(k);
            setQuestionText(questionTextLayout, k, q);
            setRadioButtons(optionsListLinearLayout, k, q);
            createResponse(q);
            mIndex = k;
            if (getSurvey().getResponseByQuestion(q) != null) {
                deserialize(getSurvey().getResponseByQuestion(q).getText());
            }
        }
    }

    private void updateViewToHideSet() {
        mViewToHideSet = new HashSet<>();
        for (int i = 0; i < mQuestionList.size(); i++) {
            if (mSurveyFragment.getQuestionsToSkipSet().contains(mQuestionList.get(i))) {
                mViewToHideSet.add(i);
            }
        }
    }

    private void updateLayout() {
        updateViewToHideSet();
        mView.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < questionTextLayout.getChildCount(); i++) {
                    View curQuestionTextView = questionTextLayout.getChildAt(i);
                    View curOptionListView = optionsListLinearLayout.getChildAt(i);
                    if (mViewToHideSet.contains(i)) {
                        setCurrentRowHeight(curQuestionTextView, 0);
                        setCurrentRowHeight(curOptionListView, 0);
                    } else {
                        if (rowHeights[i] != null) {
                            setCurrentRowHeight(curQuestionTextView, rowHeights[i]);
                            setCurrentRowHeight(curOptionListView, rowHeights[i]);
                        }
                    }
                }
            }
        });
    }

    private void setRadioButtons(LinearLayout optionsListLinearLayout, final int k, final
    Question q) {
        LinearLayout choiceRow = new LinearLayout(getActivity());
        choiceRow.setOrientation(LinearLayout.HORIZONTAL);
        final RadioGroup radioButtons = new RadioGroup(getActivity());
        radioButtons.setOrientation(RadioGroup.HORIZONTAL);
        RadioGroup.LayoutParams buttonParams = new RadioGroup.LayoutParams(RadioGroup
                .LayoutParams.MATCH_PARENT, MIN_HEIGHT);
        buttonParams.gravity = Gravity.CENTER;
        radioButtons.setLayoutParams(buttonParams);
        adjustRowHeight(radioButtons, k);
        final int normalOptionsSize = getDisplay().tableOptions(getTableIdentifier()).size();
        final Button specialResponseButton = new Button(getActivity());
        for (int i = 0; i < normalOptionsSize; i++) {
            RadioButton button = new RadioButton(getActivity());
            button.setSaveEnabled(false);
            button.setId(i);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(mOptionWidth, MIN_HEIGHT);
            button.setLayoutParams(params);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateLayout();
                    specialResponseButton.setText("");
                }
            });
            radioButtons.addView(button, i);
        }

        LinearLayout radioGroupLayout = new LinearLayout(getActivity());
        radioGroupLayout.setLayoutParams(new LinearLayout.LayoutParams(normalOptionsSize *
                mOptionWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        radioGroupLayout.addView(radioButtons);
        choiceRow.addView(radioGroupLayout);

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

        optionsListLinearLayout.addView(choiceRow, k);
        radioButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setResponseIndex(q, checkedId);
            }
        });
        mRadioGroups.add(radioButtons);
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
                        mRadioGroups.get(pos).clearCheck();
                        Response response = getSurvey().getResponseByQuestion(q);
                        if (response != null) {
                            response.setResponse("");
                            response.save();
                        }
                    }
                })
                .create().show();
    }

    private void setQuestionText(LinearLayout questionTextLayout, int k, Question q) {
        LinearLayout questionRow = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_10);
        questionRow.setLayoutParams(params);
        TextView questionNumber = new TextView(getActivity());
        questionNumber.setText(String.valueOf(q.getNumberInInstrument() + "."));
        questionNumber.setMinHeight(MIN_HEIGHT);
        questionNumber.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams questionNumberParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        questionNumberParams.setMargins(MARGIN_0, MARGIN_0, MARGIN_10, MARGIN_0);
        questionNumber.setLayoutParams(questionNumberParams);
        questionRow.addView(questionNumber);
        TextView questionText = new TextView(getActivity());
        questionText.setText(styleTextWithHtml(q.getText()));
        questionText.setMinHeight(MIN_HEIGHT);
        questionRow.addView(questionText);
        questionTextLayout.addView(questionRow, k);
        setRowHeight(questionRow, k);
    }

    private void setTableHeaderOptions(View v) {
        TextView questionTextHeader = v.findViewById(R.id.table_header_question_text);
        questionTextHeader.setMinHeight(MIN_HEIGHT);
        questionTextHeader.setPadding(10, 10, 10, 10);

        LinearLayout headerTableLayout = v.findViewById(R.id.table_options_header);
        List<Option> headerLabels = getDisplay().tableOptions(getTableIdentifier());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float margin = getActivity().getResources().getDimension(R.dimen
                .activity_horizontal_margin);
        float totalWidth = (displayMetrics.widthPixels - margin * 2) / 2;

        int headerCount = headerLabels.size();
        for (Question question : getQuestions()) {
            if (question.hasSpecialOptions()) {
                headerCount += 1;
                break;
            }
        }
        mOptionWidth = (int) totalWidth / headerCount;

        for (int k = 0; k < headerLabels.size(); k++) {
            TextView textView = getHeaderTextView(headerLabels.get(k).getText(getInstrument()));
            textView.setWidth(mOptionWidth);
            headerTableLayout.addView(textView);
        }

        if (headerCount != headerLabels.size()) {
            TextView textView = getHeaderTextView(getString(R.string.special_response_abbrv));
            textView.setWidth(mOptionWidth);
            headerTableLayout.addView(textView);
        }

    }

    private void setCurrentRowHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    private void setRowHeight(final LinearLayout view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view
                        .getLayoutParams();
                rowHeights[position] = view.getHeight() + params.topMargin + params.bottomMargin;
            }
        });
    }

    private void adjustRowHeight(final RadioGroup view, final int pos) {
        view.post(new Runnable() {
            @Override
            public void run() {
                int diff = rowHeights[pos] - view.getHeight();
                RadioGroup.LayoutParams params = (RadioGroup.LayoutParams) view.getLayoutParams();
                params.setMargins(MARGIN_0, diff / 2, MARGIN_0, diff / 2);
                view.setLayoutParams(params);
            }
        });
    }

    @Override
    protected String serialize() {
        return null;
    }

    @Override
    protected void unSetResponse() {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        mView = getLayoutInflater().inflate(R.layout.fragment_table_question, null);
        setTableHeaderOptions(mView);
        setTableBodyContent(mView);
        updateLayout();
        questionComponent.addView(mView);
    }
}
