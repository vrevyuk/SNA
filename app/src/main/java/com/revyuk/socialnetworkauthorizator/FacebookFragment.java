package com.revyuk.socialnetworkauthorizator;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AppEventsLogger;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

public class FacebookFragment extends Fragment {
    OnFragmentSetResultListener listener;
    Session session;
    UiLifecycleHelper uiHelper;
    ProfilePictureView ppv;

    public FacebookFragment() { }

    public interface OnFragmentSetResultListener {
        public void setResult(String userName, String userUrl);
    }

    Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.d("XXX", "session state:"+state.isOpened());
            if(state.isOpened()) {
                new Request(session, "/me", null, HttpMethod.GET, new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                        ppv.setProfileId(response.getGraphObject().getProperty("id").toString());
                        if(listener!=null) listener.setResult((String) response.getGraphObject().getProperty("name")
                                , (String) response.getGraphObject().getProperty("link"));
                    }
                }).executeAsync();
            } else {
                if(listener!=null) listener.setResult("", "");
                ppv.setProfileId(null);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("XXX", "res act requestcode:"+requestCode+" result:"+resultCode);
        uiHelper.onActivityResult(requestCode, resultCode, data, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_facebook, container, false);
        ppv = (ProfilePictureView) rootView.findViewById(R.id.profilePhoto);
        final LoginButton authBtn = (LoginButton) rootView.findViewById(R.id.authButton);
        authBtn.setFragment(this);
        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authBtn.setReadPermissions("public_profile");
            }
        });
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        uiHelper.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getActivity());
        uiHelper.onResume();
        session = Session.getActiveSession();
        listener = (OnFragmentSetResultListener) getActivity();
        session.refreshPermissions();
    }

    @Override
    public void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(getActivity());
        uiHelper.onPause();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
