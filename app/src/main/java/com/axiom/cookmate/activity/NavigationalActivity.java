package com.axiom.cookmate.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.axiom.cookmate.R;
import com.axiom.cookmate.data.RecipeContract;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.RecipeUtils;

public class NavigationalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_home:
                if (!(this instanceof SearchMainActivity)) {
                    startActivity(new Intent(getApplicationContext(), SearchMainActivity.class));
                }
                break;
            case R.id.nav_create_recipe:
                startActivity(new Intent(getApplicationContext(), CreateRecipeActivity.class));
                break;
            case R.id.nav_my_recipes:
                if (!(this instanceof MyRecipesActivity)) {
                    startActivity(new Intent(getApplicationContext(), MyRecipesActivity.class));
                }
                break;
            case R.id.nav_fav_recipes:
                if (!(this instanceof FavouriteRecipesActivity)) {
                    startActivity(new Intent(getApplicationContext(), FavouriteRecipesActivity.class));
                }
                break;
            case R.id.nav_shop_list:
                startActivity(new Intent(getApplicationContext(), ShoppingListActivity.class));
                break;
            case R.id.nav_report_issue:
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setType("plain/text");
                sendIntent.setData(Uri.parse("mailto:customercare.cookmate@gmail.com"));
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Report Issue regarding:");
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
                break;
            case R.id.nav_feedback:
                Intent feedbackIntent = new Intent(Intent.ACTION_VIEW);
                feedbackIntent.setType("plain/text");
                feedbackIntent.setData(Uri.parse("mailto:customercare.cookmate@gmail.com"));
                feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about:");
                if (feedbackIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(feedbackIntent);
                }
                break;

            case R.id.nav_web_recipe:
                startActivity(new Intent(getApplicationContext(), WebRecipeSearchActivity.class));
                break;
            case R.id.nav_bookmarked_recipes:
                if (!(this instanceof BookmarkListActivity)) {
                    startActivity(new Intent(getApplicationContext(), BookmarkListActivity.class));
                }
                break;
            case R.id.nav_sign_out:
                AccountUtils.removeAccountEmail(this);
                AccountUtils.removeAccountName(this);
                AccountUtils.removeAccountPhoto(this);
                AccountUtils.userLoginCheck(this, false);

                getContentResolver().delete(RecipeContract.FavouriteRecipeEntry.CONTENT_URI,
                        null, null);
                getContentResolver().delete(RecipeContract.MyRecipeEntry.CONTENT_URI,
                        null, null);

                RecipeUtils.removeAllRecipesFromShopList(this);
                RecipeUtils.removeAllBookmarkFromList(this);
                startActivity(new Intent(getApplicationContext(), SplashActivity.class));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
