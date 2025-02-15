package com.macindex.macindex;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;

class ExceptionHelper {

    public static void handleException(final Context thisContext, final Exception thisException,
                                                 final String exceptionModule, final String exceptionMessage) {
        if (thisContext != null) {
            final String basicInfo = "MacIndex Version: " + BuildConfig.VERSION_NAME + "\n"
                    + "Android Version: " + Build.VERSION.RELEASE + "\n"
                    + "Hardware Brand: " + Build.BRAND + "\n"
                    + "Hardware Model: " + Build.MODEL + "\n";

            final String exceptionDetails;
            if (thisException == null) {
                exceptionDetails = "Exception Detail Not Applicable";
            } else {
                thisException.printStackTrace();
                exceptionDetails = "Exception Details:" + "\n" + getStackTrace(thisException);
            }

            final String exceptionLog;
            if (exceptionModule != null && exceptionMessage != null) {
                Log.e(exceptionModule, exceptionMessage);
                exceptionLog = "Log Tag: " + exceptionModule + "\n"
                        + "Log Message: " + exceptionMessage + "\n" + "\n";
            } else {
                exceptionLog = "Logging Not Applicable" + "\n" + "\n";
            }

            handleExceptionDialog(thisContext, basicInfo + exceptionLog + exceptionDetails);
        }
    }

    private static void handleExceptionDialog(final Context thisContext, final String exceptionInfo) {
        final AlertDialog.Builder exceptionDialog = new AlertDialog.Builder(thisContext);
        exceptionDialog.setTitle(R.string.error);
        exceptionDialog.setMessage(R.string.error_information);
        exceptionDialog.setCancelable(false);
        exceptionDialog.setPositiveButton(R.string.error_dismiss, (dialogInterface, i) -> {
            System.exit(0);
        });
        exceptionDialog.setNegativeButton(R.string.error_copy_button, (dialogInterface, i) -> {
        });

        final View infoChunk = ((LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chunk_exception_dialog, null);
        final TextView exceptionInfoBox = infoChunk.findViewById(R.id.exceptionInfo);

        exceptionInfoBox.setText(exceptionInfo);
        exceptionDialog.setView(infoChunk);

        final AlertDialog exceptionDialogCreated = exceptionDialog.create();
        exceptionDialogCreated.show();

        // Override the negative button
        exceptionDialogCreated.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) thisContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ExceptionInfo", exceptionInfo);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(thisContext,
                    MainActivity.getRes().getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
        });
    }

    private static String getStackTrace(final Exception thisException) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        thisException.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
