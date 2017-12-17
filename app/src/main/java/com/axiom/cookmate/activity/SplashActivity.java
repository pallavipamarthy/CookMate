package com.axiom.cookmate.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.axiom.cookmate.R;
import com.axiom.cookmate.utilities.AccountUtils;

import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        ButterKnife.bind(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "Playball.ttf");
        TextView myTextView = (TextView) findViewById(R.id.splash_app_name_text);
        myTextView.setTypeface(myTypeface);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AccountUtils.getUserLogin(SplashActivity.this)) {
                    Intent i = new Intent(SplashActivity.this, SearchMainActivity.class);
                    startActivity(i);
                    finish();

                } else {
                    Intent i = new Intent(SplashActivity.this, MainAuthenticationActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, 1000);
    }
}
