package com.example.uberclone;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

public class ViewLocationMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Button btnGiveRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnGiveRide = findViewById(R.id.btnGiveRide);

        btnGiveRide.setText("I want to give " + getIntent().getStringExtra("rUsername")+ " a ride!");

        btnGiveRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
                requestCarQuery.whereEqualTo("username", getIntent().getStringExtra("rUsername"));
                requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null) {
                            for (ParseObject uberRequests = objects) {
                                uberRequests.put("driverOfMe", ParseUser.getCurrentUser().getUsername());
                                uberRequests.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Intent googleIntent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr=" +
                                                            getIntent().getDoubleExtra("dLatitude", 0) +
                                                            "," + getIntent().getDoubleExtra("dLongitude", 0) +
                                                            "&daddr=" + getIntent().getDoubleExtra("pLatitude", 0)
                                                            + "," + getIntent().getDoubleExtra("pLongitude", 0)));
                                            startActivity(googleIntent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

            }
        });
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude", 0), getIntent().getDoubleExtra("dLongitude", 0));

        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude", 0), getIntent().getDoubleExtra("pLongitude", 0));

       Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
       Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location"));

        ArrayList<Marker> myMarkers = new ArrayList<>();

        myMarkers.add(driverMarker);
        myMarkers.add(passengerMarker);

        for (Marker marker : myMarkers) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cameraUpdate);


    }
}
