package com.example.p22005unipifirechat.utils;

import android.content.Context;
import android.widget.ImageView;
import com.example.p22005unipifirechat.R;

public class AvatarUtils {

    public static void setAvatar(ImageView imageView, String avatarId) {
        // by default set a simple user avatar
        imageView.setImageResource(R.drawable.simpleuser);

        if (avatarId == null || avatarId.equals("default") || avatarId.isEmpty()) {
            return;
        }

        Context context = imageView.getContext();

        String cleanName = avatarId.replace(".png", "").trim();

        // drawable's id
        int resId = context.getResources().getIdentifier(
                cleanName,
                "drawable",
                context.getPackageName()
        );

        // replace the image with the avatar that resId points to
        if (resId != 0) {
            //build-in method by ImageView class
            imageView.setImageResource(resId);
        }
    }
}