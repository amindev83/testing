package com.nooshindroid.yastashir;

import android.app.Application;
import android.content.Context;
import android.content.Intent;


public class Apps extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Ad.InitializePandora(this);
        Ad.InitializeAds(this);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "Vazir-Medium-FD-WOL.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "Vazir-Medium-FD-WOL.ttf");

    }
}
