package com.axiom.cookmate.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.axiom.cookmate.R;
import com.axiom.cookmate.data.RecipeBookmark;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.RecipeUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarkWebActivity extends AppCompatActivity {

    @BindView(R.id.webView)
    WebView webView;
    RecipeBookmark mRecipeBookmark;
    ProgressDialog mProgressDialog;
    private static final String BOOKMARK_OBJ = "bookmarkObj";
    DatabaseReference mBookmarksNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_web_layout);
        ButterKnife.bind(this);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        mRecipeBookmark = getIntent().getParcelableExtra(BOOKMARK_OBJ);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_text));
        mProgressDialog.show();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
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
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(mRecipeBookmark.getRecipeLink());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_myrecipe_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Implementation for user clicks on different menu items.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_item_share:
                //share intent to share trailer
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mRecipeBookmark.getRecipeLink());
                startActivity(Intent.createChooser(intent, getString(R.string.share_via_text)));
                return true;
            case R.id.action_delete:
                createAndShowAlertDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAndShowAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_bookmark_dialog_title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                RecipeUtils.deleteRecipeBookmark(BookmarkWebActivity.this, mRecipeBookmark);

                if (AccountUtils.getUserLogin(BookmarkWebActivity.this)) {
                    FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
                    DatabaseReference mFirebaseDBRef = mFirebaseInstance.getReference("users");
                    String userId = AccountUtils.getFirebaseUserId(BookmarkWebActivity.this);
                    DatabaseReference firebaseUserNode = mFirebaseDBRef.child(userId);
                    mBookmarksNode = firebaseUserNode.child("bookmarks_list");
                    mBookmarksNode.child(mRecipeBookmark.getRecipeName()).removeValue();
                }
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
