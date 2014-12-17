package com.revyuk.socialnetworkauthorizator;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class TwitterFragment extends Fragment {
    protected final String CONSUMER_KEY = "UbWn5xn3XTqca7LFQW0xAVazT";
    protected final String CONSUMER_SECRET = "vkZ6N69nyKwAopEVDEDYCFB6K9deVS418l7bfdKyzYs6ULbezN";
    protected String token_key;
    protected String token_secret;
    //protected String oauth_token;
    protected String oauth_verifier;
    protected final static int REQUEST_TOKEN_INTENT = 1010;

    OnFragmentSetResultListener listener;
    ImageView avatar;
    Twitter twitter;
    RequestToken requestToken;
    AccessToken accessToken;

    EditText text, login, password;


    public TwitterFragment() { }

    public interface OnFragmentSetResultListener {
        public void setResult(String userName, String userUrl);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (OnFragmentSetResultListener) getActivity();
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(CONSUMER_KEY,CONSUMER_SECRET);
        Log.d("XXX", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener=null;
        Log.d("XXX", "onDestroy");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_TOKEN_INTENT) {
                if(data!=null) {
                    //oauth_token = data.getStringExtra("oauth_token");
                    oauth_verifier = data.getStringExtra("oauth_verifier");
                    //final String f_oauth_token = oauth_token;
                    final String f_oauth_verifier = oauth_verifier;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                accessToken = twitter.getOAuthAccessToken(requestToken, f_oauth_verifier);
                                token_key = accessToken.getToken();
                                token_secret = accessToken.getTokenSecret();
                            } catch (TwitterException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();
                    try {
                        t.join();
                        getUserInfo();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d("XXX", "request:" + requestCode + " result:" + resultCode);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_twitter, container, false);
        Button signin = (Button) rootView.findViewById(R.id.signInTwitterButton);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestToken = twitter.getOAuthRequestToken();
                            Intent intent = new Intent(getActivity(), OAuthWebActivity.class);
                            intent.putExtra("requestToken", requestToken.getAuthenticationURL());
                            getActivity().startActivityForResult(intent, REQUEST_TOKEN_INTENT);
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
        Button signout = (Button) rootView.findViewById(R.id.signOutTwitterButton);
        Button messageTwitterBtn = (Button) rootView.findViewById(R.id.messageTwitterButton);
        text = (EditText) rootView.findViewById(R.id.messageTwitter);
        messageTwitterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Status status;
                        try {
                            status = twitter.updateStatus(text.getText().toString());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    text.setText("");
                                    new Toast(getActivity()).makeText(getActivity(), "Message is posted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        avatar = (ImageView) rootView.findViewById(R.id.avatarTwitter);
        return rootView;
    }

    void getUserInfo() {
        final String[] profile = new String[3];
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading ...");
        pd.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = twitter.users().showUser(twitter.getId());
                    //User user = accessToken.getScreenName()
                    profile[0] = user.getName();
                    profile[1] = user.getProfileBannerURL();
                    profile[2] = user.getBiggerProfileImageURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
        try {
            thread.join();
            listener.setResult(profile[0], profile[1]);
            avatar.setImageBitmap(MainActivity.getImageFromUrl(profile[2]));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pd.hide();
    }
}
