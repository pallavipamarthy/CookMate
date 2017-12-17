package com.axiom.cookmate.widget;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.axiom.cookmate.FirebaseAnalyticsUtils;
import com.axiom.cookmate.R;
import com.axiom.cookmate.utilities.RecipeUtils;
import java.util.ArrayList;

public class ShopListRemoteService extends RemoteViewsService {
    private static final String INTENT_EXTRA_RECIPE_NAME = "recipeName";
    private static final String EVENT_WIDGET_LAUNCHED = "widget_launched";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private ArrayList<String> mRecipeNameList;
            private  ArrayList<String> mIngredientList;

            @Override
            public void onCreate() {
                // Nothing to do
                Bundle b = new Bundle();
                b.putString(EVENT_WIDGET_LAUNCHED,getString(R.string.event_widget_launched));
                FirebaseAnalyticsUtils.reportWidgetLaunched(getApplicationContext(),b);
            }
            @Override
            public void onDataSetChanged() {
                if (mRecipeNameList != null) {
                }
                final long identityToken = Binder.clearCallingIdentity();
                SharedPreferences sharedPref = getApplicationContext()
                        .getSharedPreferences("SHOPPING_LIST_PREFERENCE",0);
                mRecipeNameList = RecipeUtils.getShoppingRecipeList(getApplicationContext());

                Binder.restoreCallingIdentity(identityToken);
            }
            @Override
            public void onDestroy() {
                if (mRecipeNameList != null) {
                    mRecipeNameList = null;
                }
            }

           @Override
            public int getCount() {
               return mRecipeNameList == null ? 0 : mRecipeNameList.size();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || mRecipeNameList == null) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),R.layout.widget_list_item);
                views.setTextViewText(R.id.itemText, mRecipeNameList.get(position));

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(INTENT_EXTRA_RECIPE_NAME,mRecipeNameList.get(position));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
