package org.adaptlab.chpir.android.survey.questionfragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SingleQuestionFragment;
import org.adaptlab.chpir.android.survey.models.ResponsePhoto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class SignatureQuestionFragment extends SingleQuestionFragment {
    private final String TAG = "SignatureFragment";
    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    private ResponsePhoto mResponsePhoto;
    private Bitmap mSignatureBitmap;

    @Override
    protected void unSetResponse() {

    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        loadOrCreateResponsePhoto();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels / 5;
        View view = getLayoutInflater().inflate(R.layout.fragment_signature, null);
        questionComponent.addView(view);

        mSignaturePad = view.findViewById(R.id.signaturePad);
        mSignaturePad.getLayoutParams().height = height;
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
            }
        });

        mClearButton = view.findViewById(R.id.clearButton);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
                mResponsePhoto.setPicturePath(null);
                mResponsePhoto.save();
            }
        });

        mSaveButton = view.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignatureBitmap = mSignaturePad.getSignatureBitmap();
                if (saveJpgSignature(mSignatureBitmap)) {
                    Toast.makeText(getActivity(), R.string.signature_saved,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.signature_not_saved,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadOrCreateResponsePhoto() {
        if (getResponse() != null) {
            if (getResponse().getId() == null) getResponse().save();
            if (getResponse().getResponsePhoto() == null) {
                mResponsePhoto = new ResponsePhoto();
                mResponsePhoto.setResponse(getResponse());
                mResponsePhoto.save();
            } else {
                mResponsePhoto = getResponse().getResponsePhoto();
            }
        }
    }

    private boolean saveJpgSignature(Bitmap signature) {
        boolean result = false;
        try {
            String filename = UUID.randomUUID().toString() + ".jpg";
            File photo = new File(getActivity().getFilesDir(), filename);
            saveBitmapToJPG(signature, photo);
            result = true;
            mResponsePhoto.setPicturePath(filename);
            mResponsePhoto.save();
        } catch (IOException e) {
            Log.e(TAG, "IOException ", e);
        }
        return result;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    @Override
    protected void deserialize(String responseText) {
        String filename = mResponsePhoto.getPicturePath();
        if (filename != null && !filename.isEmpty()) {
            String path = getActivity().getFileStreamPath(filename).getAbsolutePath();
            mSignatureBitmap = BitmapFactory.decodeFile(path);
            if (mSignaturePad != null) {
                mSignaturePad.setSignatureBitmap(mSignatureBitmap);
            }
        }
    }

    @Override
    protected String serialize() {
        return null;
    }
}
