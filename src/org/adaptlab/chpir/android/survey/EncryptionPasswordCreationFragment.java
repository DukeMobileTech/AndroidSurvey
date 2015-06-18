package org.adaptlab.chpir.android.survey;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EncryptionPasswordCreationFragment extends Fragment {
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmationEditText;
    private Button mDoneButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_encryption_password_create, parent, false);
        v.findViewById(R.id.encryption_password_create_instructions);
        v.findViewById(R.id.encryption_password_create_password_label);
        mPasswordEditText = (EditText) v.findViewById(R.id.encryption_password_create_password_edit_text);
        v.findViewById(R.id.encryption_password_create_confirmation_label);
        mPasswordConfirmationEditText = (EditText) v.findViewById(R.id.encryption_password_create_confirmation_edit_text);
        mDoneButton = (Button) v.findViewById(R.id.encryption_password_create_done_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String encryptionPassword = mPasswordEditText.getText().toString();
                String encryptionPasswordConfirmation = mPasswordConfirmationEditText.getText().toString();
                if (encryptionPassword.isEmpty() || encryptionPasswordConfirmation.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.encryption_password_empty, Toast.LENGTH_LONG).show();
                } else if (!TextUtils.equals(encryptionPassword, encryptionPasswordConfirmation)) {
                	Toast.makeText(getActivity(), R.string.encryption_password_confirmation_mismatch, Toast.LENGTH_LONG).show();
                } else {
                	AppUtil.setDecryptionPassword(encryptionPassword);
                	if (AppUtil.encryptResponses()) {
                		getActivity().finish();
                	}
                }
            }
        });
        
        return v;       
    }

}
