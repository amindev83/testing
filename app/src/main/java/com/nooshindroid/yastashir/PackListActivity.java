/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.nooshindroid.yastashir;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.pandora.Pandora;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PackListActivity extends AddStickerPackAct {
    private AdView rateBanner;
    private boolean doubleBackToExitPressedOnce = false;

    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private GridLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;
    private PackListAdapter allStickerPacksListAdapter;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ArrayList<StickerPack> stickerPackList;
    private Toolbar toolbar;
    private CustomTextView customTextView, title_toolbar;
    private SharedPreferences sp;
    private RelativeLayout main;
    private ImageView apps;

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        sp = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        Ad.ShowBanner(this, findViewById(R.id.Banner));
        main = findViewById(R.id.main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title_toolbar = findViewById(R.id.title_toolbar);
        customTextView = findViewById(R.id.custom_txt);
        setSupportActionBar(toolbar);
        setStatusBarTranslucent(true);
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();

                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    Log.i("mhs", "11");
                    title_toolbar.setVisibility(View.VISIBLE);
                    customTextView.setVisibility(View.GONE);

                } else if (isShow) {
                    isShow = false;
                    Log.i("mhs", "22");
                    customTextView.setVisibility(View.VISIBLE);
                    title_toolbar.setVisibility(View.GONE);

                }
            }
        });


        packRecyclerView = findViewById(R.id.sticker_pack_list);
        stickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
        showStickerPackList(stickerPackList);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void showStickerPackList(List<StickerPack> stickerPackList) {
        allStickerPacksListAdapter = new PackListAdapter(stickerPackList, onAddButtonClickedListener);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new GridLayoutManager(PackListActivity.this, 3);
        //packLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        packRecyclerView.setLayoutManager(packLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(PackListActivity.this, R.dimen._5sdp);
        packRecyclerView.addItemDecoration(itemDecoration);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }


    private final PackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> {
        addStickerPackToWhatsApp(pack.identifier, pack.name);
    };

    private void recalculateColumnCount() {

            allStickerPacksListAdapter.setMaxNumberOfStickersInARow(1);

    }


    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
        private final WeakReference<PackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(PackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
            final PackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return Arrays.asList(stickerPackArray);
            }
            for (StickerPack stickerPack : stickerPackArray) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return Arrays.asList(stickerPackArray);
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            final PackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void menu(View v) {
        PopupWindow popupwindow_obj = popupDisplay();
        popupwindow_obj.showAsDropDown(v, -50, -90);
    }

    private PopupWindow popupDisplay() {

        final PopupWindow popupWindow = new PopupWindow(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup_layout, null);
        apps = (ImageView) view.findViewById(R.id.apps);
        ImageView rate = (ImageView) view.findViewById(R.id.rate);
        ImageView privacy = (ImageView) view.findViewById(R.id.privacy);
        ImageView share = (ImageView) view.findViewById(R.id.share);


        if (Pandora.get().get_Applist_Code() > 0) {
            if (Pandora.get().is_ApplistStatus_Ready()) {

                if (Pandora.get().get_ApplistStatus()) {
                    apps.setVisibility(View.VISIBLE);
                }
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Pandora.get().is_ApplistStatus_Ready()) {
                            if (Pandora.get().get_ApplistStatus()) {
                                apps.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }, 4000);

            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Pandora.get().is_SpacialApplist_Ready()) {
                        if (Pandora.get().get_spacialApplist()) {
                            apps.setVisibility(View.VISIBLE);
                            final Animation myAnim = AnimationUtils.loadAnimation(PackListActivity.this, R.anim.blink);
                            apps.startAnimation(myAnim);
                        }
                    }
                }

            }, 20000);

        } else {

            apps.setVisibility(View.GONE);

        }





        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apps(view);
                popupWindow.dismiss();
            }
        });
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                privacy(view);
                popupWindow.dismiss();
            }
        });
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rate(view);
                popupWindow.dismiss();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share(view);
                popupWindow.dismiss();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        return popupWindow;
    }


    public void rate(View view) {
        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
        Intent goMarket = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(goMarket);
    }
    public void ads(View v) {
        if (Pandora.get().is_Splash_Code_Ready()) {
            Ad.ShowEndSplash();

        } else {
            Ad.ShowInterstitial(PackListActivity.this);
        }

    }

    public void share(View view) {
        String shareBody = getString(R.string.shareApp);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.app_name) + "\n" + shareBody + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(Intent.createChooser(sharingIntent, ""));
    }

    public void apps(View view) {
        Ad.ShowAppList();

    }

    public void privacy(View view) {
        String url = getString(R.string.privacy_link);
        Intent i1 = new Intent(Intent.ACTION_VIEW);
        i1.setData(Uri.parse(url));
        startActivity(i1);
    }

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, 0, mItemOffset, 0);
        }
    }

    public void onBackPressed() {

        sp.edit().putInt("show", sp.getInt("show", 1) + 1).apply();
        if (sp.getInt("show", 0) % 3 == 0 && sp.getInt("show", 0) > 0 && !doubleBackToExitPressedOnce) {
            final Dialog dialog = showSingleOptionTextDialogOption(PackListActivity.this);
            final RatingBar rate = (RatingBar) dialog.findViewById(R.id.ratee);
            final TextView desc = (TextView) dialog.findViewById(R.id.desc);
            TextView t = (TextView) dialog.findViewById(R.id.titr);

            rate.setNumStars(5);


            if (!Pandora.get().is_Banner_Code_Ready()) {
                rateBanner = dialog.findViewById(R.id.rateBanner);
                AdRequest adRequest = new AdRequest.Builder().build();
                rateBanner.setVisibility(View.VISIBLE);
                rateBanner.loadAd(adRequest);
            }

            rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                    if (v == 5) {

                        dialog.dismiss();
                        sp.edit().putInt("show", -10000).apply();
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                        Intent goMarket = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(goMarket);

                    } else {
                        sp.edit().putInt("show", -10000).apply();
                        Toast.makeText(getApplicationContext(), "نظر شما با موفقیت ثبت شد", Toast.LENGTH_LONG).show();
                        dialog.dismiss();

                    }
                }
            });

            dialog.show();
        } else {

            if (doubleBackToExitPressedOnce) {
                if (Pandora.get().is_Splash_Code_Ready()) {
                    Ad.ShowEndSplash();
                    super.onBackPressed();
                } else {
                    super.onBackPressed();
                }
            }
            this.doubleBackToExitPressedOnce = true;
            Snackbar snackbar = Snackbar.make(main, "", Snackbar.LENGTH_LONG);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
            TextView textView = (TextView) layout.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setVisibility(View.INVISIBLE);
            View snackView = LayoutInflater.from(this).inflate(R.layout.custom_toast, null, false);
            TextView txtNoticToExit = (TextView) snackView.findViewById(R.id.txtCustomToast);

            txtNoticToExit.setText(getResources().getString(R.string.txtTwoStepForExitApp));
            layout.setPadding(0, 0, 0, 0);
            layout.addView(snackView, 0);
            snackbar.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 3000);


        }


    }

    private Dialog showSingleOptionTextDialogOption(PackListActivity content) {

        Dialog textDialog = new Dialog(content, R.style.DialogAnimation);
        textDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        textDialog.setContentView(R.layout.dialog_final);
        textDialog.setCancelable(true);
        return textDialog;
    }



}
