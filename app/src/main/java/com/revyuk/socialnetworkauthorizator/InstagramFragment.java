package com.revyuk.socialnetworkauthorizator;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class InstagramFragment extends Fragment {
    OnFragmentSetResultListener listener;
    final private static String CLIENT_ID = "85b82d40c59c46c8a015af5d6ade36b4";
    final private static String CLIENT_SECRET = "69922360aa844c9cb6b1f8f2fb24f9cf";
    final static int INSTAGRAM_INTENT = 1011;
    final static int INSTAGRAM_INTENT_STEP2 = 1012;
    private String access_token, userID;
    private ImageView avatar;

    public InstagramFragment() {    }

    public interface OnFragmentSetResultListener {
        public void setResult(String userName, String userUrl);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (OnFragmentSetResultListener) getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("XXX", "activityResult " + resultCode + " request " + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== INSTAGRAM_INTENT) {
            if(resultCode== Activity.RESULT_OK) {
                String code = data.getStringExtra("code");
                if(code!=null) {
                    String oauth_token = new Oauth2insta(CLIENT_ID, CLIENT_SECRET, code).getAccessToken();
                    resolveOauthToken(oauth_token);
                }
            }
        }
    }

    private void resolveOauthToken(String token) {
        try {
            JSONObject json = (JSONObject) new JSONTokener(token).nextValue();
            if(json!=null) {
                if(!json.isNull("access_token")) access_token = json.getString("access_token");
                if(!json.isNull("user")) {
                    JSONObject jsonUser = json.getJSONObject("user");
                    String userName="", userUrl="", userAvatarUrl="";
                    if(!jsonUser.isNull("full_name")) userName = jsonUser.getString("full_name");
                    if(!jsonUser.isNull("profile_picture")) userAvatarUrl = jsonUser.getString("profile_picture");
                    if(!jsonUser.isNull("id")) userID = jsonUser.getString("id");
                    if(!jsonUser.isNull("username")) userUrl = "http://instagram.com/"+jsonUser.getString("username");
                    if(listener!=null) {
                        listener.setResult(userName, userUrl);
                        if(avatar!=null) avatar.setImageBitmap(MainActivity.getImageFromUrl(userAvatarUrl));
                    }
                }
            } else {
                Log.d("XXX", "Cannot find json object");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_instagram, container, false);
        rootView.findViewById(R.id.instagramSignInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(access_token==null) {
                    Intent intent = new Intent(getActivity(), OAuthWebActivity.class);
                    intent.putExtra("requestToken"
                            , "https://api.instagram.com/oauth/authorize/?client_id="+CLIENT_ID+"&redirect_uri="+URLUtil.guessUrl("http://www.revyuk.com/")+"&response_type=code");
                    getActivity().startActivityForResult(intent, INSTAGRAM_INTENT);
                }
            }
        });
        avatar = (ImageView) rootView.findViewById(R.id.instagramAvatarImageView);
        return rootView;
    }

    class Oauth2insta {
        final String[] accessToken = new String[1];
        final String mKey;
        final String mSecret;
        final String mCode;

        public Oauth2insta(String key, String secret, String code) {
            mKey = key;
            mSecret = secret;
            mCode = code;
        }

        String getAccessToken() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String httpData;
                    try {
                        StringBuilder sb = new StringBuilder("");
                        sb.append("client_id="+mKey);
                        sb.append("&client_secret="+mSecret);
                        sb.append("&grant_type=authorization_code");
                        sb.append("&redirect_uri="+URLUtil.guessUrl("http://www.revyuk.com/"));
                        sb.append("&code="+mCode);
                        Log.d("XXX", sb.toString());
                        URL url = new URL("https://api.instagram.com/oauth/access_token/");
                        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                        con.setRequestProperty("Accept-Charset", "UTF-8");
                        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        con.setRequestMethod("POST");
                        con.setDoInput(true);
                        con.setDoOutput(true);
                        con.setUseCaches(false);
                        con.setRequestProperty("Content-Length", ""+sb.toString().length());
                        DataOutputStream os = new DataOutputStream(con.getOutputStream());
                        os.writeBytes(sb.toString());
                        os.flush();
                        os.close();
                        Log.d("XXX", "response code: " + con.getResponseCode());
                        if(con.getResponseCode() == 200) {
                            httpData = convertStreamToString(con.getInputStream());
                            Log.d("XXX", "JSON:"+httpData);
                            accessToken[0] = httpData;
                        }
                        con.disconnect();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return accessToken[0];
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
