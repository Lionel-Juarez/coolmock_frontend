package com.example.hamacav1.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.example.hamacav1.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    public static String convertToIso8601(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            Log.e("convertToIso8601", "Error parsing date: " + e.getMessage());
            return null;
        }
    }

    public static void closeActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
        }
    }
}
