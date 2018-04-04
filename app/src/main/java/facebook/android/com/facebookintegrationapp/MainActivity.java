package facebook.android.com.facebookintegrationapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookSDKInitialize();
        setContentView(R.layout.activity_main);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","user_likes");
        getLoginDetails(loginButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
// Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
// Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    /*
Initialize the facebook sdk.
And then callback manager will handle the login responses.
*/
    protected void facebookSDKInitialize() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    /*
Register a callback function with LoginButton to respond to the login result.
*/
    protected void getLoginDetails(LoginButton login_button) {
// Callback registration
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult login_result) {
//                getLikedPageInfo(login_result);
                getPageFeed();
                Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_SHORT).show();

// code for cancellation
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(),exception.getMessage().toString(),Toast.LENGTH_SHORT).show();
// code to handle error
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.e("data", data.toString());
    }

    /* * To get the Facebook page which is liked by user's through creating a new request. * When the request is completed, a callback is called to handle the success condition. */
    protected void getLikedPageInfo(LoginResult login_result) {
        GraphRequest data_request = GraphRequest.newMeRequest(login_result.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject json_object, GraphResponse response) {
                try {
                    JSONArray posts = json_object.getJSONObject("likes").optJSONArray("data");
                    Log.e("data1", posts.toString());
                    for (int i = 0; i < posts.length(); i++) {
                        JSONObject post = posts.optJSONObject(i);
                        String id = post.optString("id");
                        String category = post.optString("category");
                        String name = post.optString("name");
                        int count = post.optInt("likes");
                        // print id, page name and number of like of facebook page
                        Log.e("id -", id + " name -" + name + " category-" + category + " likes count -" + count);
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_SHORT).show();

                }
            }
        });
        Bundle permission_param = new Bundle();
//        permission_param.putString("fields", "id,name,email,gender, birthday");
        permission_param.putString("fields", "likes{id,category,name,location,likes}");
        data_request.setParameters(permission_param);
        data_request.executeAsync();
    }

    protected void getPageFeed(){
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/208920989118113/feed",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.e("response: ",response.toString());
            /* handle the result */
                    }
                }
        ).executeAsync();
    }

    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }
}
