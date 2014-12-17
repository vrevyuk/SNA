package com.revyuk.socialnetworkauthorizator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity
        implements mainFragment.OnSelectNetworkListener,
        GooglePlusFragment.OnFragmentSetResultListener,
        FacebookFragment.OnFragmentSetResultListener,
        TwitterFragment.OnFragmentSetResultListener,
        InstagramFragment.OnFragmentSetResultListener {

    TextView username, email;
    ImageView avatar;
    FrameLayout container;
    ActionBar actionBar;
    mainFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = (TextView) findViewById(R.id.userNameTextView);
        avatar = (ImageView) findViewById(R.id.userAvatar);
        actionBar = getSupportActionBar();

        fragment = new mainFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frameContainer, fragment, "fragment")
                .commit();
    }

    @Override
    public void setResult(String userName, final String userUrl) {
        Log.d("XXX", "Name:"+userName+" email:"+userUrl);
        if(userName  != null) {
            username.setText(userName);
            if(userUrl!=null) {
                username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Toast(getApplicationContext())
                                .makeText(getApplicationContext(), "URL:"+userUrl, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    @Override
    public void onSelectNet(String s) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        ArrayList nets = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.socialNetworkList)));
        switch (nets.indexOf(s)) {
            case 0: // Google+
                username.setText(""); avatar.setImageResource(R.drawable.google_plus);
                GooglePlusFragment googlePlusFragment = new GooglePlusFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameContainer, googlePlusFragment, "fragment")
                        .commit();
                break;
            case 1: // Facebook
                username.setText(""); avatar.setImageResource(R.drawable.facebook);
                FacebookFragment facebookFragment = new FacebookFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameContainer, facebookFragment, "fragment")
                        .commit();
                break;
            case 2: // Twitter
                username.setText(""); avatar.setImageResource(R.drawable.twitter);
                TwitterFragment twitterFragment = new TwitterFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameContainer, twitterFragment, "fragment")
                        .commit();
                break;
            case 3: // Instagram
                username.setText(""); avatar.setImageResource(R.drawable.twitter);
                InstagramFragment instagramFragment = new InstagramFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameContainer, instagramFragment, "fragment")
                        .commit();
                username.setText(""); avatar.setImageResource(R.drawable.instagram);
                break;
            case 4: // Odnoklassniki
                username.setText(""); avatar.setImageResource(R.drawable.odnoklassniki);
                break;
            case 5: // VKontakte
                username.setText(""); avatar.setImageResource(R.drawable.vk);
                break;
            case 6: // LinkedIn
                username.setText(""); avatar.setImageResource(R.drawable.linkedin);
                break;
            case 7: // LiveJournal
                username.setText(""); avatar.setImageResource(R.drawable.livejournal);
                break;
            default:
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fr = getSupportFragmentManager().findFragmentByTag("fragment");
        fr.onActivityResult(requestCode, resultCode, data);
        Log.d("XXX", "1OnActivityResult " + resultCode + " request " + requestCode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                break;
            default:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameContainer, fragment)
                        .commit();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                username.setText("");
                username.setOnClickListener(null);
                avatar.setImageResource(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static Bitmap getImageFromUrl(final String urlString) {
        final Bitmap[] bitmap = new Bitmap[1];
        Log.d("XXX", "Load image from: "+urlString);
        if(urlString!=null)  {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = null;
                    try {
                        URL url = new URL(urlString);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.setDoInput(true);
                        con.setRequestProperty("Content-type", "image/jpeg");
                        con.setUseCaches(false);
                        if(con.getResponseCode()==200) {
                            is = con.getInputStream();
                            if(is!=null) {
                                bitmap[0] = BitmapFactory.decodeStream(is);
                                is.close();
                                is=null;
                            }
                            con.disconnect();
                            con=null;
                        }
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
        }
        return bitmap[0];
    }
}
