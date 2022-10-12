package com.reachomk.contactmap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class NoContactAddrDialog extends DialogFragment {
    private MapsActivity activity;
    private String message;

    public NoContactAddrDialog(MapsActivity mapsActivity) {
        activity = mapsActivity;
        message = "Your contacts don't have any addresses added to them or you don't have any contacts. You need to go to the contacts app to add this. ";
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(this.message)
                    .setPositiveButton("Go to Contacts app", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        // Creates a new Intent to insert or edit a contact
                        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        // Sets the MIME type
                        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                        // Add code here to insert extended data, if desired
                        // Sends the Intent with an request ID
                        startActivity(intent);
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

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag).addToBackStack(null);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e("IllegalStateException", "Exception", e);
        }

    }
}
