package com.getaplot.getaplot.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by Elia on 10/24/2017.
 */

public class Location {

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String TAG = "Location";
    public static Boolean mLocationPermissionsGranted = false;
    private static FusedLocationProviderClient mFusedLocationProviderClient;
    private static String district = "";
    private static String locality;
    private List<Address> list;
    private GpsTracker gpsTracker;

    public static String getDeviceLocation(final Context mContext, final Activity activity) {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            android.location.Location mLocation = (android.location.Location) task.getResult();
                            if (mLocation == null) {

                                GpsTracker gpsTracker = new GpsTracker(mContext);
                                mLocation = gpsTracker.getLocation(activity);
                            }
                            if (mLocation == null) {

                                return;


                            } else {
                                //todo run agps service to give codination


                                Log.d(TAG, "onComplete: foundlocation " + mLocation);
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                                String userplace = preferences.getString("province", "");
                                Log.d(TAG, "onComplete: " + userplace);
                                if (userplace.equals("Current Location")) {
                                    Log.d(TAG, "onComplete: starting to look for the district");

                                    Log.d(TAG, "getFromGeocorder: Getting from geocoder first time");
                                    try {
                                        Geocoder geocoder = new Geocoder(mContext);
                                        List<Address> list = null;
                                        list = geocoder.getFromLocation(mLocation.getLatitude(),
                                                mLocation.getLongitude(), 1);
                                        try {
                                            Address address = list.get(0);
                                            Log.d(TAG, "onComplete: The Address is " + address);
                                            district = address.getLocality();

                                            Log.d(TAG, "onComplete: district from geocoder " + district);
                                            if (!district.equals("")) {
                                                SharedPreferences sharedPreferences = activity.getSharedPreferences("district", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("district", district);
                                                Log.d(TAG, "getUserLocalityFromLocality: Users Location from geocoder on file is " + district);
                                                editor.apply();

                                            }

                                        } catch (IndexOutOfBoundsException e) {

                                        }
                                        Log.d(TAG, "onComplete: " + district);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (IndexOutOfBoundsException e) {

                                    }

                                }


                                if (district.equals("")) {
                                    Log.d(TAG, "getDistrictFromVolley: trying volley now");

                                    try {
                                        Log.d(TAG, "onComplete: foundloction");

                                        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

                                        // Initialize a new JsonObjectRequest instance
                                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                                Request.Method.GET,
                                                "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                                                        mLocation.getLatitude() +
                                                        "," +
                                                        mLocation.getLongitude() +
                                                        "&key=AIzaSyD5kxVwLNINwHggoIqKvTeDWME478Sml8c",
                                                null,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                        String result;
                                                        try {
                                                            Log.d(TAG, "onResponse: " + response);
                                                            result = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                                                            Log.d(TAG, "onResponse: " + result.trim());
                                                            result.replace(" ", "");
                                                            Log.d(TAG, "onResponse: " + result);
                                                            district = result.substring(result.indexOf(",") + 2);

                                                            if (district.contains("Mbarara")) {
                                                                district = "Mbarara";
                                                            } else {
                                                                if (district.contains("Kampala")) {
                                                                    district = "Kampala";
                                                                } else if (district.contains("Masaka")) {
                                                                    district = "Masaka";
                                                                } else if (district.contains("Jinja")) {
                                                                    district = "Jinja";
                                                                } else if (district.contains("Bushenyi")) {
                                                                    district = "Bushenyi";
                                                                } else if (district.contains("Ishaka")) {
                                                                    district = "Ishaka";
                                                                } else if (district.contains("Entebbe")) {
                                                                    district = "Entebbe";
                                                                } else if (district.contains("Lyantonde")) {
                                                                    district = "Lyantonde";
                                                                } else if (district.contains("Masindi")) {
                                                                    district = "Masindi";
                                                                } else if (district.contains("Isingiro")) {
                                                                    district = "Isingiro";
                                                                } else if (district.contains("Gomba")) {
                                                                    district = "Gomba";
                                                                } else if (district.contains("Luweero")) {
                                                                    district = "Luweero";
                                                                } else if (district.contains("Nakaseke")) {
                                                                    district = "Nakaseke";
                                                                } else if (district.contains("Bukomansimbi")) {
                                                                    district = "Bukomansimbi";
                                                                } else if (district.contains("Ntungamo")) {
                                                                    district = "Ntungamo";
                                                                } else if (district.contains("Mubende")) {
                                                                    district = "Mubende";
                                                                } else if (district.contains("Mityana")) {
                                                                    district = "Mityana";
                                                                } else if (district.contains("Tororo")) {
                                                                    district = "Tororo";
                                                                } else if (district.contains("Kasese")) {
                                                                    district = "Kasese";
                                                                } else if (district.contains("Kabale")) {
                                                                    district = "Kabale";
                                                                } else if (district.contains("Gulu")) {
                                                                    district = "Gulu";
                                                                } else if (district.contains("Karamoja")) {
                                                                    district = "Karamoja";
                                                                } else if (district.contains("Parisa")) {
                                                                    district = "Parisa";
                                                                } else {
                                                                    district = "";
                                                                }
                                                            }

                                                            if (!district.equals("")) {
                                                                SharedPreferences sharedPreferences = activity.getSharedPreferences("district", Context.MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                editor.putString("district", district);
                                                                Log.d(TAG, "getUserLocalityFromLocality: Users Location on file is from volley " + district);
                                                                editor.apply();
                                                            }
                                                            Log.d(TAG, "onResponse: " + district);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }


                                                    }
                                                },
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {

                                                    }
                                                }
                                        );
                                        requestQueue.add(jsonObjectRequest);
//-----------------------SAAVES THE REASULTS OF LOCATION TO A PREF FILE--------------------------------//
                                        if (!district.equals("")) {
                                            SharedPreferences sharedPreferences = activity.getSharedPreferences("district", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("district", district);
                                            Log.d(TAG, "getUserLocalityFromLocality: Users Location on file is " + district);
                                            editor.apply();

                                        }
                                        Log.d(TAG, "onComplete: " + district);

                                    } catch (Exception e) {

                                        Log.d(TAG, "onComplete: Exception:" + e.getMessage());
                                        e.printStackTrace();
                                        Log.d(TAG, "onComplete: " + e.getLocalizedMessage());
                                    }
                                }


                            }

                        } else {
                            Toast.makeText(mContext, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
        Log.d(TAG, "getDeviceLocation: final district is " + district);
        return district;
    }

    private static void getDistrictFromVolley(Context mContext, double latitude, double longitude, final Activity activity) {

    }

    private static void getFromGeocorder(Context mContext, double latitude, double longitude, Activity activity) {

    }

    //------------------------LOCATION PERMISSIONS------------------------------------------//
    public void getLocationPermission(Context context, Activity activity) {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(context,
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context,
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;

            } else {
                ActivityCompat.requestPermissions(activity,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(activity,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


}
