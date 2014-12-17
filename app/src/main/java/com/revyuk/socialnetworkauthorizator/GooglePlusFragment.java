package com.revyuk.socialnetworkauthorizator;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

public class GooglePlusFragment extends Fragment{
    final static int INTENT_SIGNIN_CODE = 1001;
    OnFragmentSetResultListener listener;
    GoogleApiClient google;
    boolean procBtn = false;
    ImageView avatar;

    public GooglePlusFragment() { }

    public interface OnFragmentSetResultListener {
        public void setResult(String userName, String userUrl);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getActivity(), new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                final Person person = Plus.PeopleApi.getCurrentPerson(google);
                Log.d("XXX", "Curr person "+person.getDisplayName());
                listener.setResult(person.getDisplayName(), person.getUrl());
                avatar.setImageBitmap(MainActivity.getImageFromUrl(person.getImage().getUrl()));
                Plus.PeopleApi.loadVisible(google, null).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(People.LoadPeopleResult loadPeopleResult) {
                        Log.d("XXX", "Peoples onResult:" + loadPeopleResult.getStatus());
                        if (loadPeopleResult.getStatus().isSuccess()) {
                            PersonBuffer persons = loadPeopleResult.getPersonBuffer();
                            for (int i = 0; i < persons.getCount(); i++) {
                                //Log.d("XXX", ">>>"+persons.get(i).getDisplayName());
                            }
                            persons.close();
                        }
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d("XXX", "onConnectionSuspended");
                google.connect();
            }
        }, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Log.d("XXX", "onConnectionFailed");
                if(procBtn) {
                    if(connectionResult.hasResolution()) {
                        try {
                            getActivity().startIntentSenderForResult(connectionResult.getResolution().getIntentSender(),
                                    INTENT_SIGNIN_CODE, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Log.d("XXX", "Exception for request intent");
                        }
                    }
                    procBtn = false;
                }
            }
        });
        google = builder
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("XXX", "OnActivityResult "+resultCode+" request "+requestCode);
        if(requestCode == INTENT_SIGNIN_CODE && resultCode == Activity.RESULT_OK) {
            google.connect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        google.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(google.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google);
            google.disconnect();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_google_plus, container, false);
        rootView.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("XXX", "google connected:"+google.isConnected()+" connecting:"+google.isConnecting());
                if(!google.isConnecting()) {
                    procBtn = true;
                    google.connect();
                }
            }
        });
        rootView.findViewById(R.id.logoffButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(google.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(google);
                    google.disconnect();
                    listener.setResult("", "");
                    avatar.setImageBitmap(null);
                }
            }
        });
        avatar = (ImageView) rootView.findViewById(R.id.avatarGooglePlus);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnFragmentSetResultListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener.setResult("", "");
        listener = null;
    }
}
