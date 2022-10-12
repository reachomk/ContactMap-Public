package com.reachomk.contactmap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DenialDialog extends DialogFragment {
    private String message;
    private MapsActivity activity;
    private int requestCode;

    public DenialDialog(MapsActivity m) {
        Log.e("DenialDialog", String.valueOf(requestCode));
        this.activity = m;
        message = "The app will not be able to display your contacts' locations without the required permissions. ";
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(this.message)
                .setPositiveButton("Enable Permissions", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.getPerms();
                    }
                })
                .setNegativeButton("Quit app", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finishAndRemoveTask();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
