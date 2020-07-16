/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.nooshindroid.yastashir;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewViewHolder> {

    @NonNull
    private StickerPack stickerPack;
    int iii;
    private int cellLimit;
    private final int errorResource;
    BroadcastReceiver Receiver;
    private final LayoutInflater layoutInflater;
    private OnItemClicked onClick;
    public interface OnItemClicked {
        void onItemClick(int position);
    }
    PreviewAdapter(
            @NonNull final LayoutInflater layoutInflater,
            final int errorResource,
            @NonNull final StickerPack stickerPack) {
        this.cellLimit = 0;
        this.layoutInflater = layoutInflater;
        this.errorResource = errorResource;
        this.stickerPack = stickerPack;
        setHasStableIds(true);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        View itemView = layoutInflater.inflate(R.layout.sticker_image, viewGroup, false);
        PreviewViewHolder vh = new PreviewViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final PreviewViewHolder stickerPreviewViewHolder, final int i) {


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_CLOSE");

        Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                iii = intent.getIntExtra("position", 0);
                Log.i("mhsssss",iii+"");
                if (i == iii) {
                    stickerPreviewViewHolder.test.setImageResource(R.drawable.choose_stiker);

                } else {

                    stickerPreviewViewHolder.test.setImageResource(R.drawable.category_2);
                }
            }
        };
        stickerPreviewViewHolder.stickerPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClick(i);
            }
        });
        Apps.context.registerReceiver(Receiver, intentFilter);

        stickerPreviewViewHolder.stickerPreviewView.setImageResource(errorResource);
        stickerPreviewViewHolder.stickerPreviewView.setImageURI(PackLoader.getStickerAssetUri(stickerPack.identifier, stickerPack.getStickers().get(i).imageFileName));
    }

    @Override
    public int getItemCount() {
        int numberOfPreviewImagesInPack;
        numberOfPreviewImagesInPack = stickerPack.getStickers().size();
        return numberOfPreviewImagesInPack;
    }
    public void setOnClick(OnItemClicked onClick)
    {
        this.onClick=onClick;
    }
}
