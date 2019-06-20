package org.adaptlab.chpir.android.survey;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.adaptlab.chpir.android.survey.models.DeviceUser;
import org.adaptlab.chpir.android.survey.utils.AuthUtils;

public class LoginFragment extends Fragment {
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, parent,
                false);

        mUsernameEditText = (EditText) v.findViewById(R.id.login_username_edit_text);
        mPasswordEditText = (EditText) v.findViewById(R.id.login_password_edit_text);
        Button mLoginButton = (Button) v.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String userName = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                DeviceUser deviceUser = DeviceUser.findByUserName(userName);
                if (deviceUser != null && deviceUser.checkPassword(password)) {
                    AuthUtils.signIn(deviceUser);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().finishAfterTransition();
                    } else {
                        getActivity().finish();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.invalid_username_or_password), Toast.LENGTH_LONG).show();
                }
            }
        });

        return v;
    }
}
