package com.reachomk.contactmap;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_ALL = 1;
    private static final String[] PERMISSIONS = new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};

    private Location lastKnownLocation;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static ArrayList<Contact> contactList = new ArrayList<>();
    private boolean hasPerms;
    private boolean noContactAddrDialogShown = false;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationSettingsRequest.Builder builder;
    private int locationRequests = 0;
    public static float markerColor = BitmapDescriptorFactory.HUE_MAGENTA;


    public MapsActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Objects.requireNonNull(toolbar.getOverflowIcon()).setColorFilter(new BlendModeColorFilter(Color.WHITE, BlendMode.DST));
        }
        else Objects.requireNonNull(toolbar.getOverflowIcon()).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu_mapsactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.helpMenuItemMA:
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
       // getFineLocPerms();
        getPerms();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public void getPerms() {
        if (!hasPermissions()) {
           requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_ALL);
        }
        else {
            hasPerms = true;
            checkInternet();
            updateLocationUI();
           // getDeviceLocation();
            getContacts();
        }
    }

    private void checkInternet() {
        if (!internetIsAvailable(this.getApplicationContext())) {
            new NoInternetDialog(this).show(getSupportFragmentManager(), "tag");
        }
    }

    private boolean internetIsAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private boolean hasPermissions() {
        for(String perm: MapsActivity.PERMISSIONS) {
            Log.e(TAG, "void hasPermissions() - "+perm+" granted: "+ (ActivityCompat.checkSelfPermission(this.getApplicationContext(), perm) == PackageManager.PERMISSION_GRANTED));
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), perm) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (hasPerms) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getPerms();
                updateLocationUI();
            }
        } catch (SecurityException e)  {
            Log.e(TAG,"void updateLocationUI() - Exception: %s"+ e.getMessage());
        }
    }


    private void getDeviceLocation() {
        //
        //Get the best and most recent location of the device, which may be null in rare
        //cases when a location is not available.
        try {
            if (hasPerms) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                LatLng LKLLatLng = new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude());
                             //   map.moveCamera(CameraUpdateFactory.newLatLngZoom(LKLLatLng, DEFAULT_ZOOM));
                             //   MarkerOptions markerOptions = new MarkerOptions();
                             //   markerOptions.position(LKLLatLng);
                             //   markerOptions.title("Current Position");
                             //   markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                             //   currLocationMarker = map.addMarker(markerOptions);
                            }
                        } else {
                            Log.e(TAG, "void getDeviceLocation() - Current location is null. Using defaults.");
                            Log.e(TAG, "void getDeviceLocation() - Exception: %s"+ task.getException());;
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e(TAG, "void getDeviceLocation() - Exception: %s"+ e.getMessage());
        }
    }



    private void getContacts() {
        Toast.makeText(this, "Fetching contacts...", Toast.LENGTH_LONG);
          ContentResolver cr = this.getApplicationContext().getContentResolver();
          //String[] projection = {ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,ContactsContract.CommonDataKinds.StructuredPostal._ID};
          Cursor cur = cr.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, null, null, null);
          if(hasPerms) {
              if (cur != null) {
                  if(cur.getCount() > 0) {
                      while (cur.moveToNext()) {
                          String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                          String contAddr = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                          String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                          String lookup_key = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                          if(name != null && contAddr != null && id != null && lookup_key != null) {
                              Log.e(TAG, contAddr);
                              Contact c = new Contact(name, contAddr, lookup_key, id, this.getApplicationContext());
                              contactList.add(c);
                          }
                          else Log.e(TAG, "void getContacts() - something is null for contact lookup key " + lookup_key);
                      }
                      cur.close();
                      showContacts();
                  }
                  else{
                      cur.close();
                      Log.e(TAG, "void getContacts() - no contacts or contacts have no addresses. ");
                      new NoContactAddrDialog(this).show(this.getSupportFragmentManager(), TAG);
                      noContactAddrDialogShown = true;
                  }
                  if(contactList.size() < 1) {
                      Log.e(TAG, "void getContacts() - no contacts or contacts have no addresses. ");
                      if(!noContactAddrDialogShown) {
                          new NoContactAddrDialog(this).show(this.getSupportFragmentManager(), TAG);
                      }
                      noContactAddrDialogShown = true;
                  }
              }
          }
          else {
           //   getContactPerms();
              getPerms();
          }
  }

    private void showContacts() {
        MapsActivity superClass = this;
        for(Contact c: contactList) {
            new Thread(new Runnable() {
                public void run() {
                    //job done on a background thread, e.g. getFromLocation() call
                    LatLng contactLatLng = c.getAddrLatLng();
                    if(contactLatLng != null) {
                        //adds marker in main thread
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                Marker marker = map.addMarker(new MarkerOptions()
                                        .position(contactLatLng)
                                        .title(c.getName() +"'s Home")
                                        .snippet(c.getAddressStringOfficial()));
                                marker.setVisible(true);
                                marker.setTag(c);
                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(markerColor));
                                Log.e("Marker Added", c.getAddressString());
                                map.setOnMarkerClickListener(superClass);
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                    else Log.e("showContacts", "Contact lookup key " +c.getLookupKey()+" has null address");
                }
            }).start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) - requestcode: "+String.valueOf(requestCode));
        Log.e(TAG, "void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) - permissions: "+ Arrays.toString(permissions));
        Log.e(TAG, "void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) - grantResults: "+ Arrays.toString(grantResults));
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ALL:
                if (hasPermsGR(grantResults)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Log.e(TAG, "void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) - All permissions granted");
                    hasPerms = true;
                    checkInternet();
                    updateLocationUI();
                 //   getDeviceLocation();
                    getContacts();
                }
                else if(permissions.length == 0)  {
                    // getInternetPerms();
                    getPerms();
                }
                else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.

                    // Toast.makeText(this.getApplicationContext(), "Error: Internet permission denied. Quitting app.", Toast.LENGTH_LONG).show();
                    // finishAndRemoveTask();
                    new DenialDialog(this).show(getSupportFragmentManager(), "tag");
                }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    private boolean hasPermsGR(int[] grantResults) {
        if(grantResults.length > 0) {
            for(int i : grantResults) {
                if(i == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Contact c = (Contact) marker.getTag();
        if (c == null) {
            Log.e(TAG, "boolean onMarkerClick(Marker marker) - Marker w/ id " + marker.getId()+"- No contact assigned or contact assigned is null");
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = ContactsContract.Contacts.getLookupUri(Long.parseLong(c.getId()), c.getLookupKey());
        intent.setData(uri);
        startActivity(intent);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        if (map != null) this.getPerms();
    }

    private void startLocationUpdates() {
        setupContinuousLocation();
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
        catch (SecurityException e) {
            e.printStackTrace();
            getPerms();
        }
    }

    private void checkLocationSetting(LocationSettingsRequest.Builder builder) {

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void setupContinuousLocation() {
        updateLocationUI();
        getDeviceLocation();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                 /*   if(currLocationMarker != null) {
                        currLocationMarker.remove();
                    } */
                    LatLng locLL = new LatLng(location.getLatitude(), location.getLongitude());
                    if(locationRequests == 0) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locLL, DEFAULT_ZOOM));
                    }
                  /*  MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(locLL);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    currLocationMarker = map.addMarker(markerOptions); */
                    locationRequests++;
                }
            };
        };

        locationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        checkLocationSetting(builder);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                startLocationUpdates();
            } else {
                checkLocationSetting(builder);
            }
        }
    }

}
