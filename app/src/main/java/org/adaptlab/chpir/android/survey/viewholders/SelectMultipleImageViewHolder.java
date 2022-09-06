package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetRelation;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SelectMultipleImageViewHolder extends QuestionViewHolder {
    private ArrayList<Integer> mResponseIndices;
    private ArrayList<MaterialCardView> mCardViews;
//    private int minHeight;
//    private int minWidth;
//    private int maxSelection = 2;

    SelectMultipleImageViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mCardViews = new ArrayList<>();
        mResponseIndices = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        OptionSetRelation optionSetRelation = getQuestionRelation().optionSets.get(0);
        List<OptionRelation> optionRelations = getOptionRelations();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        double targetWidth = deviceWidth - (0.25 * deviceWidth);

        if (optionSetRelation.optionSet.isAlignImageVertical()) {
            for (final OptionRelation optionRelation : optionRelations) {
                View view = inflater.inflate(R.layout.list_item_image, null);
                final MaterialCardView cardView = view.findViewById(R.id.material_card_view);
                cardView.setId(optionRelations.indexOf(optionRelation));
                ImageView imageView = cardView.findViewById(R.id.item_image);
//                String path = getContext().getFileStreamPath(relation.optionSetOption.getBitmapPath()).getAbsolutePath();
                String path = getContext().getFilesDir().getAbsolutePath() + "/" +
                        getQuestionRelation().question.getInstrumentRemoteId() + "/" +
                        optionRelation.option.getIdentifier() + ".png";
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width < targetWidth) {
                    double scale = targetWidth / width;
                    width = (int) Math.round(width * scale);
                    height = (int) Math.round(height * scale);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                imageView.setImageBitmap(bitmap);

                cardView.setOnClickListener(v -> {
                    int index = v.getId();
                    cardView.setSelected(!cardView.isSelected());
                    cardView.setChecked(!cardView.isChecked());
                    setResponseIndex(index, cardView.isChecked());
                });

                mCardViews.add(cardView);
                questionComponent.addView(view);
            }
        } else {
            LinearLayout view = (LinearLayout) inflater.inflate(R.layout.card_images, null);
            view.setWeightSum(optionRelations.size());
            for (final OptionRelation optionRelation : optionRelations) {
                View layout = inflater.inflate(R.layout.list_item_card, null);
                MaterialCardView cardView = layout.findViewById(R.id.material_card_view);
                cardView.setId(optionRelations.indexOf(optionRelation));
                ImageView imageView = cardView.findViewById(R.id.item_image);
                String path = getContext().getFilesDir().getAbsolutePath() + "/" +
                        getQuestionRelation().question.getInstrumentRemoteId() + "/" +
                        optionRelation.option.getIdentifier() + ".png";
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                imageView.setImageBitmap(bitmap);

                cardView.setOnClickListener(v -> {
//                    int index = v.getId();
//                    if (mResponseIndices.size() == maxSelection) {
//                        for (MaterialCardView materialCardView : mCardViews) {
//                            if (materialCardView.isChecked()) {
//                                materialCardView.setChecked(false);
//                                materialCardView.setSelected(false);
//                            }
//                        }
//                        mResponseIndices.clear();
//                    } else {
//                        if (mResponseIndices.size() == 1) {
//                            cardView.setCheckedIcon(getContext().getDrawable(R.drawable.ic_baseline_cancel_24));
//                        }
//
//                        cardView.setSelected(!cardView.isSelected());
//                        cardView.setChecked(!cardView.isChecked());
//                        setResponseIndex(index, cardView.isChecked());
//                    }
                    int index = v.getId();
                    cardView.setSelected(!cardView.isSelected());
                    cardView.setChecked(!cardView.isChecked());
                    setResponseIndex(index, cardView.isChecked());
                });
                mCardViews.add(cardView);
                view.addView(layout);

//                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
//                if (params != null) {
//                    params.weight = 1;
//                    layout.setLayoutParams(params);
//                }
            }
            questionComponent.addView(view);
        }
    }

    private void setResponseIndex(int index, boolean status) {
        if (mResponseIndices.contains(index)) {
            mResponseIndices.remove((Integer) index);
        } else {
            mResponseIndices.add(index);
        }
        saveResponse();
    }

    @Override
    protected String serialize() {
        return FormatUtils.arrayListToString(mResponseIndices);
    }

    @Override
    protected void deserialize(String responseText) {
        mResponseIndices = new ArrayList<>();
        if (responseText.trim().isEmpty()) {
            for (MaterialCardView cardView : mCardViews) {
                if (cardView.isChecked()) {
                    cardView.setChecked(false);
                    cardView.setSelected(false);
                }
            }
        } else {
            String[] listOfIndices = responseText.split(COMMA);
            for (String index : listOfIndices) {
                if (!index.isEmpty()) {
                    int indexInteger = Integer.parseInt(index);
                    MaterialCardView cardView = mCardViews.get(indexInteger);
                    cardView.setChecked(true);
                    cardView.setSelected(true);

//                    if (mResponseIndices.size() == 1) {
//                        cardView.setCheckedIcon(getContext().getDrawable(R.drawable.ic_baseline_cancel_24));
//                    }

                    mResponseIndices.add(indexInteger);
                }
            }
        }
    }

    @Override
    protected void unSetResponse() {
        mResponseIndices = new ArrayList<>();
        for (MaterialCardView cardView : mCardViews) {
            if (cardView.isChecked()) {
                cardView.setChecked(false);
                cardView.setSelected(false);
            }
        }
    }

    @Override
    protected void showOtherText(int position) {
    }

}
