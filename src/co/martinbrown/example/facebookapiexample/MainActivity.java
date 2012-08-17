package co.martinbrown.example.facebookapiexample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class MainActivity extends Activity {

    public Facebook facebook = new Facebook("386944074698248");

    EditText mEditEmail;
    EditText mEditPassword;

    Button mButtonLogin;
    Button mButtonLoginViaFacebook;

    private SharedPreferences mPrefs;
    Intent myIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myIntent = new Intent(this, SecondActivity.class);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);

        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }

        mEditEmail = (EditText) findViewById(R.id.editEmail);
        mEditPassword = (EditText) findViewById(R.id.editPassword);

        mButtonLogin = (Button) findViewById(R.id.buttonLogin);
        mButtonLoginViaFacebook = (Button) findViewById(R.id.buttonLoginViaFacebook);

        if(facebook.isSessionValid()) {
            startActivity(myIntent);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        facebook.extendAccessTokenIfNeeded(this, null);
    }

    public void loginViaFacebook(View v) {

        if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] { "email", "publish_checkins", "user_birthday" }, new DialogListener() {

                @Override
                public void onComplete(Bundle values) {

                    String type = "";
                    String message = "";

                    if(values != null) {
                        Bundle error = values.getBundle("error");
                        if(error != null) {
                            type = error.getString("type");
                            message = error.getString("OAuthException");

                        }
                    }

                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();

                    startActivity(myIntent);
                }

                @Override
                public void onFacebookError(FacebookError error) {}

                @Override
                public void onError(DialogError e) {}

                @Override
                public void onCancel() {}
            });
        }
        else {
            Intent myIntent = new Intent(this, SecondActivity.class);
            startActivity(myIntent);
        }
        String kablooey = "";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
}
