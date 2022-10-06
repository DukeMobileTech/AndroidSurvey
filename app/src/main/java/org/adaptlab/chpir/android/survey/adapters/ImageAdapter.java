package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.relations.CollageRelation;
import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private final String TAG = "ImageAdapter";
    private final Context mContext;
    private final List<DiagramRelation> mDiagramRelations;
    private final QuestionRelation mQuestionRelation;
    private List<Bitmap> mBitmaps;
    private List<Integer> mWidths;
    private List<Integer> mHeights;
    private int mHeight;

    public ImageAdapter(Context context, QuestionRelation questionRelation, CollageRelation collageRelation) {
        this.mContext = context;
        this.mQuestionRelation = questionRelation;
        this.mDiagramRelations = collageRelation.diagrams;
        this.mDiagramRelations.sort((dr1, dr2) -> dr1.diagram.getPosition().compareTo(dr2.diagram.getPosition()));
        setBitmaps();
    }

    private void setBitmaps() {
        mWidths = new ArrayList<>();
        mHeights = new ArrayList<>();
        mBitmaps = new ArrayList<>();
        for (int k = 0; k < mDiagramRelations.size(); k++) {
            DiagramRelation diagramRelation = mDiagramRelations.get(k);
            String path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    diagramRelation.options.get(0).option.getIdentifier() + ".png";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = true;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels - 32;
            int height = bitmap.getHeight();
            double imageViewWidth = (width - (width * 0.1)) / mDiagramRelations.size();
            if (imageViewWidth > bitmap.getWidth()) {
                double scale = imageViewWidth / bitmap.getWidth();
                width = (int) Math.round(bitmap.getWidth() * scale);
                height = (int) Math.round(bitmap.getHeight() * scale);
            }
            mBitmaps.add(bitmap);
            mHeights.add(height);
            mWidths.add(width);
        }
        mHeight = Collections.max(mHeights);
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
            linearLayout = new LinearLayout(mContext);
            ImageView imageView;
            imageView = new ImageView(mContext);
            Bitmap bitmap = Bitmap.createScaledBitmap(mBitmaps.get(position), mWidths.get(position),
                    mHeights.get(position), true);
            imageView.setImageBitmap(bitmap);
            linearLayout.addView(imageView);
            linearLayout.setMinimumHeight(mHeight);
        } else {
            linearLayout = (LinearLayout) convertView;
        }
        return linearLayout;
    }
}
