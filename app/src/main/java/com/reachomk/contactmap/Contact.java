package com.reachomk.contactmap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

//TODOne!:implement runnable to call geocoder in new thread
class Contact {
    private String name;
    private Address addressObj;
    private String lookup_key;
    private Context context;
    private String id;
    private String addressString;

    public Contact(String name, String address, String lookup_key, String id, Context context) {
        this.name = name;
        this.lookup_key = lookup_key;
        this.id = id;
        this.context = context;
        this.addressString = address;
    }

    public String getName() {
        return name;
    }

    public Address getAddressObj() {
        return addressObj;
    }

    public String getLookupKey() {
        return lookup_key;
    }

    public String getAddressString() {
        return addressString;
    }

    @NonNull
    @Override
    public String toString() {
        return getName()+": " +getAddressString();
    }

    public Address getLocationFromAddress(String strAddress, Context context){

        Geocoder coder = new Geocoder(context);
        List<Address> addressList;
        try {
            addressList = coder.getFromLocationName(strAddress,5);
            if (addressList==null||addressList.size() < 1) {
                Log.e("getLocationFromAddress", "No addresses inAddressList for contact "+this.lookup_key);
                return null;
            }
            return addressList.get(0);
        } catch (IOException e) {
            Log.e("Error", Log.getStackTraceString(e));
        }
        return null;
    }

    public LatLng getAddrLatLng() {
        this.addressObj = getLocationFromAddress(addressString, context);

        if(addressObj != null) {
            return new LatLng(addressObj.getLatitude(), addressObj.getLongitude());
        }
        else Log.w("getAddrLatLng", "null LatLng returned to null address");
        return null;
    }

    public String getId() {
        return id;
    }

    public String getAddressStringOfficial() {
        return this.addressObj.getAddressLine(0);
    }
}
