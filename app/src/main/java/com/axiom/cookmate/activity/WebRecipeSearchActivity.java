package com.axiom.cookmate.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.axiom.cookmate.R;
import com.axiom.cookmate.data.RecipeBookmark;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.RecipeUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebRecipeSearchActivity extends AppCompatActivity {
    WebView webView;
    ProgressDialog mProgressDialog;
    @BindView(R.id.toolbar_webview)
    Toolbar toolbar;
    @BindView(R.id.bookmark_button)
    Button mBookmarkButton;
    String mRecipeName;
    DatabaseReference mBookmarksNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_recipe_search_layout);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        toolbar.setTitle(getResources().getString(R.string.app_name));

        mProgressDialog = new ProgressDialog(WebRecipeSearchActivity.this);
        mProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_text));
        mProgressDialog.show();
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebRecipeSearchActivity.MyWebViewClient());
        webView.loadUrl("https://www.google.com");

        mBookmarkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayAlertDialog();
            }
        });
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

    }

    private void displayAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.EditTextDialog);
        alertDialog.setTitle(getString(R.string.enter_recipe_name));

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        input.setTextColor(Color.BLACK);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mRecipeName = input.getText().toString().trim();
                        if (mRecipeName.isEmpty()) {
                            return;
                        } else {
                            String url = webView.getUrl();
                            RecipeBookmark recipeBookmark = new RecipeBookmark(mRecipeName, url);
                            RecipeUtils.addRecipeBookmark(WebRecipeSearchActivity.this, recipeBookmark);

                            if (AccountUtils.getUserLogin(WebRecipeSearchActivity.this)) {
                                FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
                                DatabaseReference mFirebaseDBRef = mFirebaseInstance.getReference("users");
                                String userId = AccountUtils.getFirebaseUserId(WebRecipeSearchActivity.this);
                                DatabaseReference firebaseUserNode = mFirebaseDBRef.child(userId);
                                mBookmarksNode = firebaseUserNode.child("bookmarks_list");
                                mBookmarksNode.child(mRecipeName).setValue(url);
                            }
                        }
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
}
