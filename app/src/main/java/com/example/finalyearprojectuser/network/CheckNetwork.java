package com.example.finalyearprojectuser.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

public class CheckNetwork extends BroadcastReceiver {

    // initialize listener
    public static ReceiverListener Listener;

    public void onReceive(Context context, Intent intent) {

        // initialize connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize network info
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // check condition
        if (Listener != null) {

            // when connectivity receiver
            // listener  not null
            // get connection status
            boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            // call listener method
            Listener.onNetworkChange(isConnected);
        }
    }

    public interface ReceiverListener {
        // create method
        void onNetworkChange(boolean isConnected);
    }

}
