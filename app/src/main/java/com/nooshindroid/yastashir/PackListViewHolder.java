/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.nooshindroid.yastashir;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PackListViewHolder extends RecyclerView.ViewHolder {

    View container;
    LinearLayout imageRowView;

    PackListViewHolder(final View itemView) {
        super(itemView);
        container = itemView;
        imageRowView = itemView.findViewById(R.id.sticker_packs_list_item_image_list);
    }
}