/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.nooshindroid.yastashir;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pandora.Pandora;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class PackDetailsActivity extends AddStickerPackAct implements PreviewAdapter.OnItemClicked {

    /**
     * Do not change below values of below 3 lines as this is also used by WhatsApp
     */
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private PreviewAdapter stickerPreviewAdapter;
    private int numColumns;
    private View addButton;
    private View alreadyAddedText;
    private StickerPack stickerPack;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ImageView next, previuos, preview_img;
    private int pos = 0;
    private int pos_;
    private SharedPreferences sp;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sticker_pack_details);
        Ad.ShowBanner(this, findViewById(R.id.Banner));
        if (Pandora.get().is_Splash_Code_Ready()) {
            Ad.ShowEndSplash();

        } else {
            Ad.ShowInterstitial(PackDetailsActivity.this);
        }

        sp = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);


        setStatusBarTranslucent(true);
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        window.setAttributes(winParams);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false);
        stickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);
        //  TextView packNameTextView = findViewById(R.id.pack_name);
        addButton = findViewById(R.id.add_to_whatsapp_button);
        alreadyAddedText = findViewById(R.id.already_added_text);
        // layoutManager = new GridLayoutManager(this, 1);
        layoutManager = new GridLayoutManager(PackDetailsActivity.this, 3);
        recyclerView = findViewById(R.id.sticker_list);
        next = findViewById(R.id.next);
        preview_img = findViewById(R.id.preview_show);
        previuos = findViewById(R.id.previous);
        recyclerView.setLayoutManager(layoutManager);
        try {
            // get input stream
            InputStream ims = getAssets().open(stickerPack.name + "/" + stickerPack.getStickers().get(0).imageFileName);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            preview_img.setImageDrawable(d);
            ims.close();
         //  pos = 0;
        } catch (IOException ex) {
            return;
        }

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(PackDetailsActivity.this, R.dimen._5sdp);
        recyclerView.addItemDecoration(itemDecoration);
        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter = new PreviewAdapter(getLayoutInflater(), R.drawable.error_bg, stickerPack);
            recyclerView.setAdapter(stickerPreviewAdapter);
        }


        addButton.setOnClickListener(v -> addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
        }
        stickerPreviewAdapter.setOnClick(PackDetailsActivity.this); // Bind the listener


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (pos < stickerPack.getStickers().size()) {

                    try {
                        // get input stream
                        InputStream ims = getAssets().open(stickerPack.name + "/" + stickerPack.getStickers().get(pos).imageFileName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        preview_img.setImageDrawable(d);
                        ims.close();
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("com.package.ACTION_CLOSE");
                        broadcastIntent.putExtra("position", pos);
                        sendBroadcast(broadcastIntent);
                        layoutManager.scrollToPosition(pos);
                    } catch (IOException ex) {
                        return;
                    }
                    pos++;
                    pos_ = pos - 1;
                }

                sp.edit().putInt("show", sp.getInt("show", 1) + 1).apply();
                if (sp.getInt("show", 0) % 5 == 0) {
                    ads_(view);
                }

            }
        });
        previuos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pos_ - 1 >= 0) {
                    try {
                        // get input stream
                        InputStream ims = getAssets().open(stickerPack.name + "/" + stickerPack.getStickers().get(pos_ - 1).imageFileName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        preview_img.setImageDrawable(d);
                        ims.close();
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("com.package.ACTION_CLOSE");
                        broadcastIntent.putExtra("position", (pos_ - 1));
                        sendBroadcast(broadcastIntent);
                        layoutManager.scrollToPosition(pos_ - 1);
                    } catch (IOException ex) {
                        return;
                    }
                    pos_--;
                    pos = pos_ + 1;
                }
                sp.edit().putInt("show", sp.getInt("show", 1) + 1).apply();
                if (sp.getInt("show", 0) % 5 == 0) {
                    ads_(view);
                }

            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void updateAddUI(Boolean isWhitelisted) {
        if (isWhitelisted) {
            addButton.setVisibility(View.GONE);
            alreadyAddedText.setVisibility(View.VISIBLE);
        } else {
            addButton.setVisibility(View.VISIBLE);
            alreadyAddedText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(int position) {


        try {
            // get input stream
            InputStream ims = getAssets().open(stickerPack.name + "/" + stickerPack.getStickers().get(position).imageFileName);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            preview_img.setImageDrawable(d);
            ims.close();
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.package.ACTION_CLOSE");
            broadcastIntent.putExtra("position", position);
            sendBroadcast(broadcastIntent);
            layoutManager.scrollToPosition(position);
            pos = position + 1;
            pos_ = position;
        } catch (IOException ex) {
            return;
        }

        sp.edit().putInt("show", sp.getInt("show", 1) + 1).apply();
        if (sp.getInt("show", 0) % 5 == 0) {
            if (Pandora.get().is_Splash_Code_Ready()) {
                Ad.ShowEndSplash();

            } else {
                Ad.ShowInterstitial(PackDetailsActivity.this);
            }
        }

    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, Boolean> {
        private final WeakReference<PackDetailsActivity> stickerPackDetailsActivityWeakReference;

        WhiteListCheckAsyncTask(PackDetailsActivity stickerPackListActivity) {
            this.stickerPackDetailsActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final Boolean doInBackground(StickerPack... stickerPacks) {
            StickerPack stickerPack = stickerPacks[0];
            final PackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            //noinspection SimplifiableIfStatement
            if (stickerPackDetailsActivity == null) {
                return false;
            }
            return WhitelistCheck.isWhitelisted(stickerPackDetailsActivity, stickerPack.identifier);
        }

        @Override
        protected void onPostExecute(Boolean isWhitelisted) {
            final PackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            if (stickerPackDetailsActivity != null) {
                stickerPackDetailsActivity.updateAddUI(isWhitelisted);
            }
        }
    }


    @Override
    protected void onResume() {


        super.onResume();
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

    public void back(View view) {
        onBackPressed();
    }

    public void ads_(View v) {
        if (Pandora.get().is_Splash_Code_Ready()) {
            Ad.ShowEndSplash();

        } else {
            Ad.ShowInterstitial(PackDetailsActivity.this);
        }

    }

}
