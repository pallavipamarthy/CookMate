package com.axiom.cookmate.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.axiom.cookmate.R;
import com.axiom.cookmate.adapter.ViewPagerAdapter;
import com.axiom.cookmate.data.AccountInfo;
import com.axiom.cookmate.data.User;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.NetworkUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixelcan.inkpageindicator.InkPageIndicator;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;

public class MainAuthenticationActivity extends AppCompatActivity {

    private static final String TAG = MainAuthenticationActivity.class.getSimpleName();

    GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1;
    private DatabaseReference mFirebaseDBRef;
    private FirebaseDatabase mFirebaseInstance;
    private String userId;
    private String displayName;
    private String emailId;
    Timer timer;
    int count = 0;
    ViewPager viewPager;
    private Snackbar mSnackbar;
    private NetworkInfoReceiver mNetworkInfoReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_auth_layout);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "Playball.ttf");
        TextView myTextView = (TextView) findViewById(R.id.splash_app_name_text);
        myTextView.setTypeface(myTypeface);

        FrameLayout mainLayout = (FrameLayout) findViewById(R.id.frame_layout);
        mSnackbar = Snackbar.make(mainLayout, R.string.no_internet_connection_snackbar, Snackbar.LENGTH_LONG);
        View sbView = mSnackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);

        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        InkPageIndicator inkPageIndicator = (InkPageIndicator) findViewById(R.id.indicator);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), this));
        inkPageIndicator.setViewPager(viewPager);
        viewPager.setCurrentItem(0);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (count <= 5) {
                            viewPager.setCurrentItem(count, true);
                            count++;
                        } else {
                            count = 0;
                            viewPager.setCurrentItem(count, false);
                        }
                    }
                });
            }
        }, 300, 4000);

        TextView mSkipButtonView = (TextView) findViewById(R.id.skip_button);
        mSkipButtonView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchSearchActivity();
            }
        });
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("704166480207-mi963pk18m91mfnj4ma2j2r7ij2ic04r.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(MainAuthenticationActivity.this)) {
                    signIn();
                } else {
                    mSnackbar.show();
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.e("Google sign in error", result.getStatus().toString());
            }

        }
    }

    private void handleSignInResult(FirebaseUser firebaseUser) {
        // Store the account info in shared pref
        AccountUtils.saveAccountName(MainAuthenticationActivity.this, firebaseUser.getDisplayName());
        AccountUtils.saveAccountPhoto(MainAuthenticationActivity.this, firebaseUser.getPhotoUrl().toString());
        AccountUtils.saveAccountEmail(MainAuthenticationActivity.this, firebaseUser.getEmail());
        AccountUtils.userLoginCheck(MainAuthenticationActivity.this, true);
        // Signed in successfully, show authenticated UI.
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDBRef = mFirebaseInstance.getReference("users");
        userId = firebaseUser.getUid();
        displayName = firebaseUser.getDisplayName();
        emailId = firebaseUser.getEmail();
        AccountUtils.saveFirebaseUserId(MainAuthenticationActivity.this, userId);
        mFirebaseDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(userId)) {
                    AccountInfo accountInfo = new AccountInfo(displayName, emailId);
                    User user = new User(accountInfo);
                    mFirebaseDBRef.child(userId).setValue(user);
                }
                launchSyncActivity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUserData:onCancelled", databaseError.toException());
            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            handleSignInResult(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainAuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void launchSyncActivity() {
        Intent intent = new Intent(this, SyncActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        mNetworkInfoReceiver = new NetworkInfoReceiver();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkInfoReceiver, networkFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNetworkInfoReceiver != null) {
            unregisterReceiver(mNetworkInfoReceiver);
        }
    }

    private void launchSearchActivity() {
        Intent intent = new Intent(this, SearchMainActivity.class);
        startActivity(intent);
        finish();
    }

    private class NetworkInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.isNetworkConnected(context)) {
                mSnackbar.dismiss();
            }
        }
    }
}

