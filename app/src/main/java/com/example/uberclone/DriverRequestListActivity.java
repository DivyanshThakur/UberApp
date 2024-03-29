package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearByDriverRequests;
    private ArrayAdapter adapter;

    private ArrayList<Double> passengersLatitudes;
    private ArrayList<Double> passengersLongitudes;

    private ArrayList<String> requestCarUsernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequests = findViewById(R.id.btnNearbyRequests);
        btnGetRequests.setOnClickListener(this);


        passengersLatitudes = new ArrayList<>();
        passengersLongitudes = new ArrayList<>();
        requestCarUsernames = new ArrayList<>();


        try {

            listView = findViewById(R.id.requestListView);
            nearByDriverRequests = new ArrayList<>();
            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriverRequests);
            listView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(DriverRequestListActivity.this, "Unknown error", Toast.LENGTH_LONG).show();
        }

        nearByDriverRequests.clear();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
        }
        listView.setOnItemClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_logout, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.driverLogoutItem) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        finish();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        } else if (Build.VERSION.SDK_INT < 23) {
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestsListView(currentDriverLocation);
        }
    }

    private void updateRequestsListView(Location driverLocation) {

        if (driverLocation != null) {

            final ParseGeoPoint currentDriverLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());

            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", currentDriverLocation);
            requestCarQuery.whereDoesNotExist("driverOfMe");
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {

                        if (objects.size() > 0) {

                            if (nearByDriverRequests.size() > 0) {
                                nearByDriverRequests.clear();
                            }
                            if (passengersLatitudes.size() > 0) {
                                passengersLatitudes.clear();
                            }
                            if (passengersLongitudes.size() > 0) {
                                passengersLongitudes.clear();
                            }
                            if (requestCarUsernames.size() > 0) {
                                requestCarUsernames.clear();
                            }


                            for (ParseObject nearRequests : objects) {


                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequests.get("passengerLocation");
                                Double milesDistanceToPassenger = currentDriverLocation.distanceInMilesTo(pLocation);

                                Float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10f;

                                nearByDriverRequests.add("There are " + roundedDistanceValue + " miles to " + nearRequests.get("username"));

                                passengersLatitudes.add(pLocation.getLatitude());
                                passengersLongitudes.add(pLocation.getLongitude());

                                requestCarUsernames.add(nearRequests.get("username") + "");
                            }

                        } else {
                            Toast.makeText(DriverRequestListActivity.this, "Sorry. There are no requests yet", Toast.LENGTH_LONG).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location dLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (dLocation != null) {

                Intent intent = new Intent(this, ViewLocationMapActivity.class);

                intent.putExtra("dLatitude", dLocation.getLatitude());
                intent.putExtra("dLongitude", dLocation.getLongitude());
                intent.putExtra("pLatitude", passengersLatitudes.get(i));
                intent.putExtra("pLongitude", passengersLongitudes.get(i));
                intent.putExtra("rUsername", requestCarUsernames.get(i));

                startActivity(intent);

            }
        }
    }
}
