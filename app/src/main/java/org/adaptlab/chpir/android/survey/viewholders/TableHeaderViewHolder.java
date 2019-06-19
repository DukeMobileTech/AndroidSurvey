package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.relations.ResponseRelation;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;

public class TableHeaderViewHolder extends TableQuestionViewHolder {

    TableHeaderViewHolder(@NonNull View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void setRelations(ResponseRelation responseRelation, QuestionRelation questionRelation) {
        setQuestion(questionRelation.question);
        setQuestionRelation(questionRelation);
        setOptionSetItems(questionRelation);
        setTableInstructions();
        createQuestionComponent(getQuestionComponent());
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        for (int k = 0; k < getOptionRelations().size(); k++) {
            TextView textView = new TextView(getContext());
            OptionRelation optionRelation = getOptionRelations().get(k);
            textView.setText(TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel()));
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
            textView.setWidth(getOptionWidth());
            textView.setPadding(1, 1, 1, 1);
            questionComponent.addView(textView);
        }
    }

    @Override
    protected void deserialize(String responseText) {
    }

    @Override
    protected String serialize() {
        return null;
    }

    private void setTableInstructions() {
        int number = getQuestion().getNumberInInstrument();
        String range = number + " - " + (number + (getAdapter().getItemCount() - 2)) + "\n";
        SpannableString spannableText = new SpannableString(range + getQuestionInstructions());
        spannableText.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.secondary_text)),
                0, spannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, range.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.ITALIC), range.length(), spannableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getQuestionTextView().setText(spannableText);
    }

}
