package co.martinbrown.example.facebookapiexample;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class SecondActivity extends Activity {

    TextView mTextHello;
    Button buttonLogout;
    Button buttonPost;
    AsyncFacebookRunner mAsyncRunner;

    Intent logoutIntent;
    private SharedPreferences mPrefs;

    public final Facebook facebook = new Facebook("386944074698248");

    @Override
    public void onResume() {
        super.onResume();
        facebook.extendAccessTokenIfNeeded(this, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        logoutIntent = new Intent(this, MainActivity.class);

        mTextHello = (TextView) findViewById(R.id.textHello);
        mAsyncRunner = new AsyncFacebookRunner(facebook);

        mPrefs = mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);

        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }

        listenForRequests();

        buttonPost = (Button) findViewById(R.id.buttonPost);

        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String method = "DELETE";
                Bundle params = new Bundle();
                /*
                 * this will revoke 'publish_stream' permission
                 * Note: If you don't specify a permission then this will de-authorize the application completely.
                 */
                params.putString("permission", "email");
                params.putString("permission", "publish_checkins");
                params.putString("permission", "user_birthday");

                mAsyncRunner.request("/me/permissions", params, method, new RequestListener() {

                    @Override
                    public void onMalformedURLException(MalformedURLException e, Object state) {
                    }

                    @Override
                    public void onIOException(IOException e, Object state) {
                    }

                    @Override
                    public void onFileNotFoundException(FileNotFoundException e, Object state) {
                    }

                    @Override
                    public void onFacebookError(FacebookError e, Object state) {
                    }

                    @Override
                    public void onComplete(String response, Object state) {
                        response.charAt(0);
                    }
                }, null);


                mAsyncRunner.logout(getApplicationContext(), new RequestListener() {

                    @Override
                    public void onMalformedURLException(MalformedURLException e, Object state) {
                        Log.i("logout","malformed");
                    }

                    @Override
                    public void onIOException(IOException e, Object state) {
                        Log.i("logout","IO");
                    }

                    @Override
                    public void onFileNotFoundException(FileNotFoundException e, Object state) {
                        Log.i("logout","file not found");
                    }

                    @Override
                    public void onFacebookError(FacebookError e, Object state) {
                        Log.i("logout","error");
                    }

                    @Override
                    public void onComplete(String response, Object state) {


                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.remove("access_token");
                        editor.remove("access_expires");
                        editor.commit();

                        startActivity(logoutIntent);
                    }
                });
            }

        });
    }

    private void listenForRequests() {
        // get information about the currently logged in user
        mAsyncRunner.request("me", new RequestListener() {

            @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }

            @Override
            public void onComplete(String responseString, Object state) {

                try {
                    JSONObject response = new JSONObject(responseString);
                    final String name = response.getString("name");
                    final String email = response.getString("email");
                    final String birthday = response.getString("birthday");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mTextHello.setText("Hello " + name + ", if today is " + birthday + ", then Happy Birthday! " +
                                    "If not, then I'll sign " + email + " up for spam");
                        }
                    });
                }
                catch(JSONException e) {
                    return;
                }


            }
        });

        // get the logged-in user's friends
        mAsyncRunner.request("me/friends", new RequestListener() {

            @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }

            @Override
            public void onComplete(String response, Object state) {
                response.charAt(0);
            }
        });
    }

    public void postToWall(View v) {

        facebook.dialog(getApplicationContext(), "feed", new DialogListener() {

            @Override
            public void onFacebookError(FacebookError e) {
            }

            @Override
            public void onError(DialogError e) {
            }

            @Override
            public void onComplete(Bundle values) {

            }

            @Override
            public void onCancel() {
            }
        });
    }
}
