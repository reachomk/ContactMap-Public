package com.reachomk.contactmap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class NoInternetDialog extends DialogFragment {
    private MapsActivity activity;
    private String message;

    public NoInternetDialog(MapsActivity mapsActivity) {
        activity = mapsActivity;
        message = "The app will not be able to display your contacts' locations without internet access.";
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(this.message)
                .setPositiveButton("Go to internet settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        activity.startActivity(intent);
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
