package com.smobileteam.flashlight.view;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by alessandro crugnola on 12/12/15.
 */
final class TooltipUtils {
    private TooltipUtils() { }

    @Nullable
    static Activity getActivity(@Nullable Context cont) {
        if (cont == null) {
            return null;
        } else if (cont instanceof Activity) {
            return (Activity) cont;
        } else if (cont instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) cont).getBaseContext());
        }
        return null;
    }

    static boolean equals(@Nullable Object a, @Nullable Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    static boolean rectContainsRectWithTolerance(@NonNull final Rect parentRect, @NonNull final Rect childRect, final int t) {
        return parentRect.contains(childRect.left + t, childRect.top + t, childRect.right - t, childRect.bottom - t);
    }
}