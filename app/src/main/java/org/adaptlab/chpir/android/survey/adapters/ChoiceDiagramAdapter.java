package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChoiceDiagramAdapter extends BaseAdapter {
    private final String TAG = "ChoiceDiagramAdapter";
    private final Context mContext;
    private final List<DiagramRelation> mDiagramRelations;
    private final QuestionRelation mQuestionRelation;
    private final SurveyViewModel mSurveyViewModel;
    private final int mCount;
    private final int mRow;
    private List<Bitmap> mBitmaps;
    private List<Integer> mWidths;
    private List<Integer> mHeights;
    private int mHeight;

    public ChoiceDiagramAdapter(Context context, QuestionRelation questionRelation,
                                List<DiagramRelation> diagramRelations,
                                SurveyViewModel surveyViewModel, int count, int row) {
        this.mContext = context;
        this.mQuestionRelation = questionRelation;
        this.mDiagramRelations = diagramRelations;
        this.mSurveyViewModel = surveyViewModel;
        this.mDiagramRelations.sort((dr1, dr2) -> dr1.diagram.getPosition().compareTo(dr2.diagram.getPosition()));
        this.mCount = count;
        this.mRow = row;
        setBitmaps();
    }

    private String getPath(DiagramRelation diagramRelation) {
        String path;
        String optionIdentifier = diagramRelation.options.get(0).option.getIdentifier();
        String translatedOptionIdentifier = "";
        if (!mSurveyViewModel.getInstrumentLanguage().equals(mSurveyViewModel.getDeviceLanguage())) {
            translatedOptionIdentifier = diagramRelation.options.get(0).option.getIdentifier() + "_" +
                    mSurveyViewModel.getDeviceLanguage().toUpperCase();
            if (mSurveyViewModel.getDeviceLanguage().contains("-")) {
                translatedOptionIdentifier = diagramRelation.options.get(0).option.getIdentifier() + "_" +
                        mSurveyViewModel.getDeviceLanguage().split("-")[0].toUpperCase();
            }
        }
        if (translatedOptionIdentifier.isEmpty()) {
            path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    optionIdentifier + ".png";
        } else {
            path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    translatedOptionIdentifier + ".png";
            File file = new File(path);
            if (!file.exists()) {
                path = mContext.getFilesDir().getAbsolutePath() + "/" +
                        mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                        optionIdentifier + ".png";
            }
        }
        return path;
    }

    private Bitmap getMissingBitmap() {
        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_image_not_supported_24);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void setBitmaps() {
        mWidths = new ArrayList<>();
        mHeights = new ArrayList<>();
        mBitmaps = new ArrayList<>();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        for (int k = 0; k < mDiagramRelations.size(); k++) {
            DiagramRelation diagramRelation = mDiagramRelations.get(k);
            Bitmap bitmap = BitmapFactory.decodeFile(getPath(diagramRelation), options);
            if (bitmap == null) {
                bitmap = getMissingBitmap();
            }
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            mBitmaps.add(bitmap);
            mHeights.add(height);
            mWidths.add(width);
        }
        setDimensions();
    }

    private void setDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int optionWidth = (displayMetrics.widthPixels - 32) / mCount;
        if (mRow != 2) {
            for (int i = 0; i < mWidths.size(); i++) {
                int width = mWidths.get(i);
                double newWidth;
                if (i == 0) {
                    newWidth = (optionWidth * 0.75);
                } else {
                    newWidth = (optionWidth * 0.25);
                }
                double scale = newWidth / width;
                int width1 = (int) Math.round(width * scale);
                int height1 = (int) Math.round(mHeights.get(i) * scale);
                mWidths.set(i, width1);
                mHeights.set(i, height1);
            }
        } else {
            double textHeight = displayMetrics.heightPixels * 0.08;
            double imageHeight = displayMetrics.heightPixels * 0.09;
            for (int i = 0; i < mHeights.size(); i++) {
                int height = mHeights.get(i);
                double scale;
                if (i == 0) {
                    scale = textHeight / height;
                } else {
                    scale = imageHeight / height;
                }
                int width1 = (int) Math.round(mWidths.get(i) * scale);
                int height1 = (int) Math.round(mHeights.get(i) * scale);
                mWidths.set(i, width1);
                mHeights.set(i, height1);
            }
        }

        if (mHeights.isEmpty()) {
            mHeight = 0;
        } else {
            mHeight = (int) (displayMetrics.heightPixels * 0.165);
        }
    }

    public int getMaxHeight() {
        return mHeight;
    }

    @Override
    public int getCount() {
        return mDiagramRelations.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout;
        if (convertView == null) {
            ImageView imageView = new ImageView(mContext);
            Bitmap bitmap = Bitmap.createScaledBitmap(mBitmaps.get(position), mWidths.get(position),
                    mHeights.get(position), true);
            imageView.setImageBitmap(bitmap);
            linearLayout = new LinearLayout(mContext);
            linearLayout.setMinimumHeight(mHeight);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.addView(imageView);
        } else {
            linearLayout = (LinearLayout) convertView;
        }
        return linearLayout;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
