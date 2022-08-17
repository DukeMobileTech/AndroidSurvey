package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetRelation;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;

import java.util.ArrayList;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SelectMultipleImageViewHolder extends QuestionViewHolder {
    private ArrayList<Integer> mResponseIndices;
    private ArrayList<MaterialCardView> mCardViews;
    private ArrayList<ImageView> mImageViews;
    private int minHeight;
    private int minWidth;
    private int maxSelection = 2;

    SelectMultipleImageViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    public void setImageDimensions() {
        for (ImageView imageView : mImageViews) {
            imageView.setMinimumHeight(minHeight * 2);
            imageView.setMinimumWidth(minWidth * 2);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mCardViews = new ArrayList<>();
        mResponseIndices = new ArrayList<>();
        mImageViews = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LongSparseArray<OptionSetOptionRelation> longSparseArray = getOptionSetOptionRelations();
        OptionSetRelation optionSetRelation = getQuestionRelation().optionSets.get(0);

        if (optionSetRelation.optionSet.isAlignImageVertical()) {
            for (int k = 0; k < longSparseArray.size(); k++) {
                OptionSetOptionRelation relation = longSparseArray.valueAt(k);
                View view = inflater.inflate(R.layout.list_item_image, null);
                final MaterialCardView cardView = view.findViewById(R.id.material_card_view);
                cardView.setId(k);
                ImageView imageView = cardView.findViewById(R.id.item_image);
                String path = getContext().getFileStreamPath(relation.optionSetOption.getBitmapPath()).getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(bitmap);
                mImageViews.add(imageView);
                minHeight = bitmap.getHeight();
                minWidth = bitmap.getWidth();

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
            view.setWeightSum(longSparseArray.size());
            for (int k = 0; k < longSparseArray.size(); k++) {
                OptionSetOptionRelation relation = longSparseArray.valueAt(k);
                View layout = inflater.inflate(R.layout.list_item_card, null);
                MaterialCardView cardView = layout.findViewById(R.id.material_card_view);
                cardView.setId(k);
                ImageView imageView = cardView.findViewById(R.id.item_image);
                String path = getContext().getFileStreamPath(relation.optionSetOption.getBitmapPath()).getAbsolutePath();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
//                cardView.setMinimumHeight(bitmap.getHeight());
                imageView.setImageBitmap(bitmap);

                cardView.setOnClickListener(v -> {
                    int index = v.getId();
                    if (mResponseIndices.size() == maxSelection) {
                        for (MaterialCardView materialCardView : mCardViews) {
                            if (materialCardView.isChecked()) {
                                materialCardView.setChecked(false);
                                materialCardView.setSelected(false);
                            }
                        }
                        mResponseIndices.clear();
                    } else {
                        if (mResponseIndices.size() == 1) {
                            cardView.setCheckedIcon(getContext().getDrawable(R.drawable.ic_baseline_cancel_24));
                        }

                        cardView.setSelected(!cardView.isSelected());
                        cardView.setChecked(!cardView.isChecked());
                        setResponseIndex(index, cardView.isChecked());
                    }
                });
                mCardViews.add(cardView);
                view.addView(layout);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                if (params != null) {
                    params.weight = 1;
                    layout.setLayoutParams(params);
                }
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

                    if (mResponseIndices.size() == 1) {
                        cardView.setCheckedIcon(getContext().getDrawable(R.drawable.ic_baseline_cancel_24));
                    }

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
