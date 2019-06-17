package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.relations.ResponseRelation;

import java.util.ArrayList;
import java.util.List;

public abstract class TableQuestionViewHolder extends QuestionViewHolder {
    private TextView mQuestionTextView;
    private ViewGroup mQuestionComponent;
    private RadioGroup mSpecialResponseRadioGroup;
    private Button mClearButton;

    TableQuestionViewHolder(@NonNull View itemView, Context context) {
        super(itemView, context);
        mQuestionTextView = itemView.findViewById(R.id.questionColumn);
        mQuestionComponent = itemView.findViewById(R.id.optionsColumn);
        mSpecialResponseRadioGroup = itemView.findViewById(R.id.specialResponseButtons);
        mClearButton = itemView.findViewById(R.id.clearResponsesButton);
    }

    @Override
    public void setQuestionRelation(ResponseRelation responseRelation, QuestionRelation questionRelation) {
        setQuestionRelation(questionRelation);
        setOptionSetItems(questionRelation);
        setSpecialOptions(questionRelation);
        setQuestion(questionRelation.question);
        setSurvey(responseRelation.surveys.get(0));
        setResponse(responseRelation.response);

        setQuestionText();
        createQuestionComponent(getQuestionComponent());
        deserializeResponse();
    }

    int getOptionWidth() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float margin = getContext().getResources().getDimension(R.dimen.activity_horizontal_margin);
        float totalWidth = (displayMetrics.widthPixels - (margin * 2)) / 2;
        return (int) totalWidth / getOptions().size();
    }

    private void setQuestionText() {
        String number = getQuestion().getNumberInInstrument() + ": ";
        int numLen = number.length();
        String identifier = getQuestion().getQuestionIdentifier() + "\n";
        int idLen = identifier.length();

        Spanned text = getQuestionText();
        int textLen = text.length();
        SpannableString spannableText = new SpannableString(number + identifier + text);
        spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                0, numLen + idLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, numLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.blue)),
                numLen + idLen, numLen + idLen + textLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new RelativeSizeSpan(1.2f),
                numLen + idLen, numLen + idLen + textLen,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mQuestionTextView.setText(spannableText);
    }

    void setTableInstructions() {
        int number = getQuestion().getNumberInInstrument();
        String range = number + " - " + (number + (getAdapter().getItemCount() - 2)) + "\n";
        SpannableString spannableText = new SpannableString(range + getQuestionInstructions());
        spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                0, spannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, range.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.ITALIC), range.length(), spannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mQuestionTextView.setText(spannableText);
    }

    void setSpecialResponseView() {
        mSpecialResponseRadioGroup.removeAllViews();
        List<String> responses = new ArrayList<>();
        for (Option option : getSpecialOptions()) {
            responses.add(option.getText());
        }
        final List<String> finalResponses = responses;

        for (String response : responses) {
            int responseId = responses.indexOf(response);
            final RadioButton button = new RadioButton(getContext());
            button.setText(response);
            button.setId(responseId);
            button.setTextColor(getContext().getResources().getColorStateList(R.color.states));

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSpecialResponse(finalResponses.get(v.getId()));
                }
            });
            mSpecialResponseRadioGroup.addView(button, responseId);
        }

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearResponse();
                updateResponse();
            }
        });
    }

    @Override
    protected void deserializeSpecialResponse() {
        if (getResponse() == null || TextUtils.isEmpty(getResponse().getSpecialResponse())) return;
        for (int i = 0; i < mSpecialResponseRadioGroup.getChildCount(); i++) {
            if (((RadioButton) mSpecialResponseRadioGroup.getChildAt(i)).getText().equals(getResponse().getSpecialResponse())) {
                mSpecialResponseRadioGroup.check(i);
            }
        }
    }

    ViewGroup getQuestionComponent() {
        return mQuestionComponent;
    }

    RadioGroup getSpecialResponseRadioGroup() {
        return mSpecialResponseRadioGroup;
    }

    Button getClearButton() {
        return mClearButton;
    }

}
