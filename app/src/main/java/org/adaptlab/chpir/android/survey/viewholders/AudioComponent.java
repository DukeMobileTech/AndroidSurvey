package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import org.adaptlab.chpir.android.survey.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioComponent {
    public final String TAG = this.getClass().getName();
    private final String mAudioFolder;
    private final Context mContext;
    private final ViewGroup mViewGroup;
    private final QuestionViewHolder mQuestionViewHolder;
    private final List<String> mAudioFiles;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean mRecord;
    private boolean mPlay;
    private AppCompatButton mRecordButton;
    private LinearLayout mAudioLayout;
    private String mAudioFile;

    AudioComponent(Context context, ViewGroup viewGroup, String audioFolder, QuestionViewHolder viewHolder) {
        mContext = context;
        mViewGroup = viewGroup;
        mAudioFolder = audioFolder;
        mQuestionViewHolder = viewHolder;
        mAudioFiles = new ArrayList<>();
        createAudioComponent();
    }

    private void createAudioComponent() {
        mViewGroup.removeAllViews();
        createFolder();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.audio, null);
        mRecordButton = view.findViewById(R.id.record);
        mRecord = true;
        mRecordButton.setOnClickListener(v -> prepareRecording());
        mPlay = true;
        mViewGroup.addView(view);
        showAudioFiles(view);
    }

    private void showAudioFiles(View view) {
        File folder = new File(mAudioFolder);
        File[] fileList = folder.listFiles();
        if (fileList == null) return;
        mAudioLayout = view.findViewById(R.id.audioLayout);
        for (File file : fileList) {
            if (file.isFile()) {
                createAudioButton(file);
            }
        }
    }

    private void createFolder() {
        File folder = new File(mAudioFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private void createAudioButton(File file) {
        String name = file.getName();
        AppCompatButton button = new AppCompatButton(mContext);
        button.setBackgroundColor(mContext.getColor(R.color.bg_gray));
        button.setText(name);
        button.setPadding(10, 10, 10, 10);
        Drawable img = AppCompatResources.getDrawable(mContext, R.drawable.ic_baseline_play_arrow_24);
        button.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 15);
        button.setOnClickListener(v -> preparePlaying(name, button));
        mAudioLayout.addView(button, params);
        mAudioFiles.add(name);
    }

    private void preparePlaying(String audioFile, AppCompatButton playButton) {
        File file = new File(mAudioFolder + "/" + audioFile);
        if (mPlay) {
            playButton.setBackgroundColor(mContext.getColor(R.color.blue));
            Drawable top = ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_pause_24);
            playButton.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(file.getAbsolutePath());
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(mp -> {
                    stopPlayer(playButton);
                    mPlay = true;
                });
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }
        } else {
            stopPlayer(playButton);
        }
        mPlay = !mPlay;
    }

    private void stopPlayer(AppCompatButton playButton) {
        if (mPlayer == null) return;
        mPlayer.release();
        mPlayer = null;
        playButton.setBackgroundColor(mContext.getColor(R.color.bg_gray));
        Drawable top = ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_play_arrow_24);
        playButton.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
    }

    private void prepareRecording() {
        if (mRecord) {
            mRecordButton.setBackgroundColor(mContext.getColor(R.color.blue));
            mRecordButton.setText(R.string.stop_recording);
            Drawable top = ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_mic_off_24);
            mRecordButton.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            mRecorder = new MediaRecorder();
            mAudioFile = mAudioFolder + "/" + System.currentTimeMillis() + ".3gp";
            try {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(mAudioFile);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.prepare();
                mRecorder.start();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }
        } else {
            createAudioButton(new File(mAudioFile));
            saveResponse();
            mRecordButton.setBackgroundColor(mContext.getColor(R.color.bg_gray));
            mRecordButton.setText(R.string.start_recording);
            Drawable top = ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_mic_24);
            mRecordButton.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            if (mRecorder == null) return;
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        mRecord = !mRecord;
    }

    private void saveResponse() {
        String response = String.join(",", mAudioFiles);
        mQuestionViewHolder.getResponse().setOtherText(response);
        mQuestionViewHolder.updateResponse();
    }

}
