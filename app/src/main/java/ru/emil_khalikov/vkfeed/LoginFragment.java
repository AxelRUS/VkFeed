package ru.emil_khalikov.vkfeed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKServiceActivity;
import com.vk.sdk.api.VKError;
import com.vk.sdk.util.VKUtil;

import java.util.ArrayList;

/**
 * Created by Axel on 26.07.2017.
 */

public class LoginFragment extends Fragment {

    public interface Callbacks {
        void onLogin();
    }

    private static final String TAG = "TAG";

    private Button mLogin;
    private Callbacks mCallbacks;

    public static LoginFragment newInstance() {
        Bundle args = new Bundle();
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mLogin = (Button) v.findViewById(R.id.vk_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogin.setEnabled(false);
                restoreSession();
                String[] fingerprints = VKUtil.getCertificateFingerprint(getActivity(), getActivity().getPackageName());
                for (String s: fingerprints) {
                    Log.d(TAG, "fingerprints: "+ s);
                }
            }
        });

        return v;
    }

    private void vkLogin() {
        Intent intent = new Intent(getActivity(), VKServiceActivity.class);
        intent.putExtra("arg1", "Authorization");
        ArrayList scopes = new ArrayList<>();
        scopes.add(VKScope.OFFLINE);
        scopes.add(VKScope.WALL);
        scopes.add(VKScope.FRIENDS);
        intent.putStringArrayListExtra("arg2", scopes);
        intent.putExtra("arg4", VKSdk.isCustomInitialize());
        startActivityForResult(intent, VKServiceActivity.VKServiceType.Authorization.getOuterCode());
    }


    private void restoreSession() {
        VKSdk.wakeUpSession(getActivity(), new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                switch (res) {
                    case LoggedIn:
                        Toast.makeText(getActivity(), "Успешная авторизация", Toast.LENGTH_SHORT).show();
                        mCallbacks.onLogin();
                        break;
                    case Pending:
                        mLogin.setText(R.string.authorization);
                        break;
                    case LoggedOut:
                    case Unknown:
                        vkLogin();
                        break;
                }
            }

            @Override
            public void onError(VKError error) {
                Log.d("xxx", "wake up error");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(getActivity(), "Успешная авторизация", Toast.LENGTH_SHORT).show();
                mCallbacks.onLogin();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getActivity(), ":(", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
