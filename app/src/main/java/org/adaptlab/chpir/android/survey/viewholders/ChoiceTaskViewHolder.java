package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.ChoiceDiagramAdapter;
import org.adaptlab.chpir.android.survey.relations.CollageRelation;
import org.adaptlab.chpir.android.survey.relations.OptionCollageRelation;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;

import java.util.ArrayList;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtmlWhitelist;

public class ChoiceTaskViewHolder extends QuestionViewHolder {
    private ArrayList<Integer> mResponseIndices;
    private ArrayList<MaterialCardView> mCardViews;

    ChoiceTaskViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mResponseIndices = new ArrayList<>();
        mCardViews = new ArrayList<>();

        List<OptionRelation> optionRelations = getOptionRelations();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout imageLayout = (LinearLayout) inflater.inflate(R.layout.choice_task, null);
        for (final OptionRelation optionRelation : optionRelations) {
            OptionSetOptionRelation relation = getOptionSetOptionRelation(optionRelation);
            int index = optionRelations.indexOf(optionRelation);
            LinearLayout linearLayout;
            TextView textView;
            MaterialCardView cardView;
            if (index == 0) {
                cardView = imageLayout.findViewById(R.id.leftCardView);
                linearLayout = imageLayout.findViewById(R.id.leftLayout);
                textView = imageLayout.findViewById(R.id.leftTitle);
            } else if (index == 1) {
                cardView = imageLayout.findViewById(R.id.middleCardView);
                linearLayout = imageLayout.findViewById(R.id.middleLayout);
                textView = imageLayout.findViewById(R.id.middleTitle);
            } else {
                cardView = imageLayout.findViewById(R.id.rightCardView);
                linearLayout = imageLayout.findViewById(R.id.rightLayout);
                textView = imageLayout.findViewById(R.id.rightTitle);
            }
            cardView.setId(index);
            cardView.setOnClickListener(v -> {
                int clickedIndex = v.getId();
                cardView.setSelected(!cardView.isSelected());
                cardView.setChecked(!cardView.isChecked());
                setResponseIndex(clickedIndex, cardView.isChecked());
            });
            mCardViews.add(cardView);
            String text = TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel());
            textView.setText(styleTextWithHtmlWhitelist(text));
            for (OptionCollageRelation optionCollageRelation : relation.optionCollages) {
                for (CollageRelation collageRelation : optionCollageRelation.collages) {
                    GridView gridView = (GridView) inflater.inflate(R.layout.list_item_option_grid_view, null);
                    gridView.setNumColumns(collageRelation.diagrams.size());
                    gridView.setAdapter(new ChoiceDiagramAdapter(getContext(), getQuestionRelation(),
                            collageRelation.diagrams, getSurveyViewModel(), optionRelations.size()));
                    LinearLayout gridViewLayout = new LinearLayout(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 0, 0, 5);
                    gridViewLayout.addView(gridView, layoutParams);
                    linearLayout.addView(gridViewLayout);
                }
            }
        }
        questionComponent.addView(imageLayout);
    }

    private void setResponseIndex(int index, boolean isChecked) {
        if (mResponseIndices.contains(index)) {
            mResponseIndices.remove((Integer) index);
        } else {
            mResponseIndices.add(index);
        }
        MaterialCardView cardView = mCardViews.get(index);
        if (!isChecked) {
            MaterialCardView materialCardView = new MaterialCardView(getContext());
            ColorStateList colorStateList = materialCardView.getCardForegroundColor();
            cardView.setCardForegroundColor(colorStateList);
            if (mResponseIndices.size() == 1) {
                setFirstSelection();
            }
        } else {
            if (mResponseIndices.size() == 1) {
                setFirstSelection();
            } else if (mResponseIndices.size() == 2) {
                cardView.setCardForegroundColor(getContext().getColorStateList(R.color.second));
            } else {
                mResponseIndices.clear();
                unCheckCards();
            }
        }
        saveResponse();
    }

    private void setFirstSelection() {
        MaterialCardView cardView = mCardViews.get(mResponseIndices.get(0));
        cardView.setCardForegroundColor(getContext().getColorStateList(R.color.first));
    }

    @Override
    protected String serialize() {
        return FormatUtils.arrayListToString(mResponseIndices);
    }

    private void unCheckCards() {
        MaterialCardView materialCardView = new MaterialCardView(getContext());
        ColorStateList colorStateList = materialCardView.getCardForegroundColor();
        for (MaterialCardView cardView : mCardViews) {
            if (cardView.isChecked()) {
                cardView.setChecked(false);
                cardView.setSelected(false);
                cardView.setCardForegroundColor(colorStateList);
            }
        }
    }

    @Override
    protected void deserialize(String responseText) {
        mResponseIndices = new ArrayList<>();
        if (responseText.trim().isEmpty()) {
            unCheckCards();
        } else {
            String[] listOfIndices = responseText.split(COMMA);
            for (String index : listOfIndices) {
                if (!index.isEmpty()) {
                    int indexInteger = Integer.parseInt(index);
                    MaterialCardView cardView = mCardViews.get(indexInteger);
                    cardView.setChecked(true);
                    cardView.setSelected(true);
                    mResponseIndices.add(indexInteger);
                }
            }
            if (!mResponseIndices.isEmpty()) {
                setFirstSelection();
            }
            if (mResponseIndices.size() == 2) {
                MaterialCardView cardView = mCardViews.get(mResponseIndices.get(1));
                cardView.setCardForegroundColor(getContext().getColorStateList(R.color.second));
            }
        }
    }

    @Override
    protected void unSetResponse() {
        mResponseIndices = new ArrayList<>();
        unCheckCards();
    }

    @Override
    protected void showOtherText(int position) {
    }

}
