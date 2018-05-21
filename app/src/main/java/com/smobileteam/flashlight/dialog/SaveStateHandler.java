package com.smobileteam.flashlight.dialog;

import android.os.Bundle;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

/**
 * Created by Duong Anh Son on 4/3/2017.
 * FlashlightBitbucket
 */

public class SaveStateHandler {
    private static final String KEY_DIALOG_ID = "id";

    private SparseArray<WeakReference<AbsBaseDialog<?>>> handledDialogs;

    public SaveStateHandler() {
        handledDialogs = new SparseArray<>();
    }

    public void saveInstanceState(Bundle outState) {
        for (int index = handledDialogs.size() - 1; index >= 0; index--) {
            WeakReference<AbsBaseDialog<?>> dialogRef = handledDialogs.valueAt(index);
            if (dialogRef.get() == null) {
                handledDialogs.remove(index);
                continue;
            }
            AbsBaseDialog<?> dialog = dialogRef.get();
            if (dialog.isShowing()) {
                dialog.onSaveInstanceState(outState);
                outState.putInt(KEY_DIALOG_ID, handledDialogs.keyAt(index));
                return;
            }
        }
    }

    void handleDialogStateSave(int id, AbsBaseDialog<?> dialog) {
        handledDialogs.put(id, new WeakReference<AbsBaseDialog<?>>(dialog));
    }

    public static boolean wasDialogOnScreen(Bundle savedInstanceState) {
        return savedInstanceState.keySet().contains(KEY_DIALOG_ID);
    }

    public static int getSavedDialogId(Bundle savedInstanceState) {
        return savedInstanceState.getInt(KEY_DIALOG_ID, -1);
    }
}
