package co.realinventor.forblind;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import co.realinventor.forblind.Admin.AdminHomeActivity;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername, mPhotoUrl;
    private final String TAG = "MainActivity";
    private String ANONYMOUS = "anonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        //Manage the signed in users
//        SharedPreferences sharedPref = getSharedPreferences("USER", Context.MODE_PRIVATE);
//        String user = sharedPref.getString("currentUser", ANONYMOUS);
//        Log.d(TAG, "Current User : "+user);
//
//        if(user.equals("admin")){
//            startActivity(new Intent(this, AdminHomeActivity.class));
//            finish();
//        }


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
    }

    public void onChatClicked(View view) {
        Log.d(TAG, "onChatClicked");
        startActivity(new Intent(this, MessageActivity.class));
        finish();

    }

    public void onSignOutClicked(View view) {
        Log.d(TAG, "onSignOutClicked");
        mFirebaseAuth.signOut();
        mUsername = ANONYMOUS;
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
