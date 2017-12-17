package com.axiom.cookmate;


import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsUtils {

    private static final String EVENT_SEARCH = "search";
    private static final String EVENT_ADD_TO_FAV = "add_to_fav";
    private static final String EVENT_CREATE_RECIPE = "create_recipe";
    private static final String EVENT_WIDGET_LAUNCH = "widget_launch";
    private static final String EVENT_SHOP_ACTIVITY_LAUNCH = "shop_activity_launched";
    private static final String EVENT_SHOPLIST_SHARED = "shop_list_share";
    private static final String EVENT_ADD_TO_SHOP = "add_to_shop";
    private static final String EVENT_VOICE_INPUT_CLICKED = "voice_input_clicked";

    public static void reportSearchEvent(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_SEARCH, bundle);
    }

    public static void reportAddToFavorites(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_ADD_TO_FAV, bundle);
    }

    public static void reportCreateRecipeEvent(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_CREATE_RECIPE, bundle);
    }

    public static void reportWidgetLaunched(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_WIDGET_LAUNCH, bundle);
    }

    public static void reportShoppingActivityLaunched(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_SHOP_ACTIVITY_LAUNCH, bundle);
    }

    public static void reportShoppingListShared(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_SHOPLIST_SHARED, bundle);
    }

    public static void reportAddToShoppingList(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_ADD_TO_SHOP, bundle);
    }

    public static void reportVoiceInputClicked(Context context, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_VOICE_INPUT_CLICKED, bundle);
    }
}
