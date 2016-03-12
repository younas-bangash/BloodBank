package com.blood.donor.patient.bank;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class Config {
    public static final boolean DEBUGMODE=true;
    public static final double mSearchingDistance=3;
    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
