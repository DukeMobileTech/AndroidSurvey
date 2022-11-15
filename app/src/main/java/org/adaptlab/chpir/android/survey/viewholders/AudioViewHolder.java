package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatButton;

import org.adaptlab.chpir.android.survey.R;

import java.io.IOException;

public class AudioViewHolder extends QuestionViewHolder {
    private static String fileName;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private AppCompatButton recordBtn;
    private AppCompatButton playBtn;

    AudioViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        fileName = getContext().getExternalCacheDir().getAbsolutePath();
        fileName += "/" + getQuestion().getQuestionIdentifier() + ".3gp";
        Log.i(TAG, "FILE NAME " + fileName);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.audio_controls, null);
        recordBtn = view.findViewById(R.id.record);
        recordBtn.setOnClickListener(v -> startRecording());
        AppCompatButton stopRecording = view.findViewById(R.id.stopRecord);
        stopRecording.setOnClickListener(v -> stopRecording());
        playBtn = view.findViewById(R.id.play);
        playBtn.setOnClickListener(v -> startPlaying());
        AppCompatButton stopPlaying = view.findViewById(R.id.stopPlay);
        stopPlaying.setOnClickListener(v -> stopPlaying());
        questionComponent.addView(view);
    }

    private void startPlaying() {
        playBtn.setBackgroundColor(getContext().getColor(R.color.blue));
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        if (player == null) return;
        player.release();
        player = null;
        playBtn.setBackgroundColor(getContext().getColor(R.color.bg_gray));
    }

    private void startRecording() {
        recordBtn.setBackgroundColor(getContext().getColor(R.color.blue));

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        if (recorder == null) return;
        recorder.stop();
        recorder.release();
        recorder = null;
        recordBtn.setBackgroundColor(getContext().getColor(R.color.bg_gray));
    }

    @Override
    protected void unSetResponse() {

    }

    @Override
    protected void showOtherText(int position) {

    }

    @Override
    protected void deserialize(String responseText) {

    }

    @Override
    protected String serialize() {
        return null;
    }

}
