package com.example.hamacav1.util;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.example.hamacav1.R;

public class Utils {

    public static void showError(Context context, String error) {
        int messageId;
        Resources res = context.getResources();

        if ("error.IOException".equals(error)) {
            messageId = R.string.error_connection;
        } else {
            messageId = R.string.error_unknown;
        }

        Toast.makeText(context, res.getString(messageId), Toast.LENGTH_LONG).show();
    }
}
