package com.nooshindroid.yastashir;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appbrain.AdId;
import com.appbrain.AppBrain;
import com.appbrain.AppBrainBanner;
import com.appbrain.InterstitialBuilder;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.pandora.Banner.ad;
import com.pandora.Pandora;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Ad {

    //Shared Preferences Keys
    private static String SHARED_PREF_KEY = ".ADMOB_ID_PREF_FILE_KEY";
    private static final String SHARED_PREF_ADMOB_ID = "admob_appid" ;
    private static final String SHARED_PREF_ADMOB_BANNER = "admob_banner";
    private static final String SHARED_PREF_ADMOB_FULLSCREEN = "admob_fullscreen_banner";
    private static final String SHARED_PREF_ADMOB_NATIVE = "admob_nativeads";

    //Log Tag
    private static final String AD_TAG = "AwfynAdTag";

    //Shared Preferences Default Value
    private static String DefAppId;
    private static String DefBanner;
    private static String DefInterstitial;
    private static String DefNative;

    //Interstitial
    private static InterstitialAd mInterstitialAd;

    //Must be called in Application Class
    public static void InitializeAds(Context context) {
        //Initialize AppBrain
        AppBrain.init(context);
        //Initialize IDs
        SHARED_PREF_KEY = context.getApplicationContext().getPackageName() + ".ADMOB_ID_PREF_FILE_KEY";
        DefAppId = context.getString(R.string.admob_appid);
        DefBanner = context.getString(R.string.admob_banner);
        DefInterstitial = context.getString(R.string.admob_fullscreen_banner);
        DefNative = context.getString(R.string.admob_nativeads);

        //Initialize Interstitial
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        String admob_fullscreen_banner = sharedPreferences.getString(SHARED_PREF_ADMOB_FULLSCREEN, DefInterstitial);
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(admob_fullscreen_banner);

        //Calls Method to Get Admob IDs
        String Url = context.getString(R.string.getIdUrl) + "?packageName=" + context.getApplicationContext().getPackageName();
        getIDs(context, Url);

        //Initialize Interstitial
        LoadInterstitial(context);
    }

    //Must be called in Application Class or Splash Activity
    public static void InitializePandora(Context context) {
        //Initialize Pandora
        Pandora.create(context, "identity")
                .active_end_splash()
                .active_middle_splash()
                .active_app_list()
                .active_banner();
    }

    //Load Ad in mInterstitialAd
    private static void LoadInterstitial(final Context context) {
//        mInterstitialAd.loadAd(new AdRequest.Builder().setMaxAdContentRating(context.getResources().getString(R.string.content_rating)).build());
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
//                mInterstitialAd.loadAd(new AdRequest.Builder().setMaxAdContentRating(context.getResources().getString(R.string.content_rating)).build());
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    //Shows Interstitial Ad
    public static void ShowInterstitial(Context context) {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            InterstitialBuilder interstitialBuilder = InterstitialBuilder.create().setAdId(AdId.HOME_SCREEN).preload(context);
            interstitialBuilder.show(context);
        }
    }

    //Shows Pandora.AppWall
    public static void ShowAppList () {
        Pandora.get().displayAppList();
    }

    //Shows Banner //Should be Called in every Activity that needs to show Banner
    public static void ShowBanner(final Context context, View viewAds) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
        String admob_banner = sharedPreferences.getString(SHARED_PREF_ADMOB_BANNER, DefBanner);


        RelativeLayout rlAds = (RelativeLayout) viewAds;

        rlAds.removeAllViews();

        //Initialize Pandora Banner
        final ad PandoraBanner = new ad(context);
        PandoraBanner.setVisibility(View.GONE);
        rlAds.addView(PandoraBanner, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        Pandora.get().active_banner();

        //Initialize AppBrain Banner
        final AppBrainBanner appBrainBanner = new AppBrainBanner(context);
        appBrainBanner.setVisibility(View.GONE);
        rlAds.addView(appBrainBanner, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);



        final ImageView ads_logo = new ImageView(context);
        ads_logo.setImageResource(R.drawable.ads_logo);
        ads_logo.setVisibility(View.GONE);
        rlAds.addView(ads_logo, 20, 20);
        //Initialize AppBrain Banner ImageView



        //Initialize AdMob Banner
        final AdView mAdView = new AdView(context);
        mAdView.setAdUnitId(admob_banner);
        mAdView.setVisibility(View.GONE);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        rlAds.addView(mAdView);

//        AdRequest request = new AdRequest.Builder()
//                .setMaxAdContentRating(context.getResources().getString(R.string.content_rating)).build();
//        mAdView.loadAd(request);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        //Show Banner
        if (Pandora.get().get_Banner_Code() > 0 || Pandora.get().showExchangeAds()) {
           
            if (Pandora.get().is_Banner_Code_Ready()) {
              
                if (Pandora.get().get_Banner_Code() == 1) {
                 
                    PandoraBanner.setVisibility(View.VISIBLE);
                    appBrainBanner.setVisibility(View.GONE);
                    ads_logo.setVisibility(View.GONE);
                    mAdView.setVisibility(View.GONE);
                } else if (Pandora.get().get_Banner_Code() == 2) {
                 
                    mAdView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            // Code to be executed when an ad finishes loading.
                            mAdView.setVisibility(View.VISIBLE);
                            appBrainBanner.setVisibility(View.GONE);
                            ads_logo.setVisibility(View.GONE);
                            PandoraBanner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // Code to be executed when an ad request fails.
                            PandoraBanner.setVisibility(View.VISIBLE);
                            appBrainBanner.setVisibility(View.GONE);
                            ads_logo.setVisibility(View.GONE);
                            mAdView.setVisibility(View.GONE);
                        }
                    });
                }
            } else {

                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        mAdView.setVisibility(View.VISIBLE);
                        appBrainBanner.setVisibility(View.GONE);
                        ads_logo.setVisibility(View.GONE);
                        PandoraBanner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Code to be executed when an ad request fails.
                        appBrainBanner.setVisibility(View.VISIBLE);
                        ads_logo.setVisibility(View.VISIBLE);
                        mAdView.setVisibility(View.GONE);
                        PandoraBanner.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    //shows Pandora.EndSplash
    public static void ShowEndSplash() {
        Pandora.get().displayEndSplash();
    }

    //Gets Admob IDs from Given link //Save them in SharedPreference
    private static void getIDs(final Context context, String Link) {

        JsonObjectRequest Req = new JsonObjectRequest(Request.Method.GET, Link, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ApplicationInfo applicationInfo;
                try {
                    JSONArray Rows = response.getJSONArray("Rows");
                    JSONObject Ids = Rows.getJSONObject(0);

                    String admob_appid = Ids.getString("admob_appid");
                    String admob_banner = Ids.getString("admob_banner");
                    String admob_fullscreen_banner = Ids.getString("admob_fullscreen_banner");
                    String admob_nativeads = Ids.getString("admob_nativeads");

                    applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    applicationInfo.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", admob_appid);
                    MobileAds.initialize(context, admob_appid);

                    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SHARED_PREF_ADMOB_BANNER, admob_banner);
                    editor.putString(SHARED_PREF_ADMOB_FULLSCREEN, admob_fullscreen_banner);
                    editor.putString(SHARED_PREF_ADMOB_NATIVE, admob_nativeads);
                    editor.apply();

                } catch (JSONException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(AD_TAG, "JsonObjReq/Error: " + error);
                ApplicationInfo applicationInfo;
                try {
                    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
                    String admob_appid = sharedPreferences.getString(SHARED_PREF_ADMOB_ID, DefAppId);

                    applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    applicationInfo.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", admob_appid);
                    MobileAds.initialize(context, admob_appid);

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(Req);
    }

}
