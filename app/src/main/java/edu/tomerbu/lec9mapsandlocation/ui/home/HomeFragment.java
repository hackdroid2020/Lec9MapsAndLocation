package edu.tomerbu.lec9mapsandlocation.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import edu.tomerbu.lec9mapsandlocation.R;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    //Fused: GPS + Satellite
    private HomeViewModel homeViewModel;
    private FusedLocationProviderClient mApiClient;
    private TextView textView;
    private GoogleMap mMap;

    //new anonymous inner class (Callback for location updates)
    //this way we can start listening when the fragment resumes
    //and stop listening when the fragment is stopped

    //Location Updates Part 1)
    //Callback when the location changes -> update the map:
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            textView.setText(location.toString());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }
    };

    //4 so the context is not null when we init it.
    private void initClient() {
        if (getContext() != null)
            mApiClient = new FusedLocationProviderClient(getContext());
    }


    @Override
    public void onResume() {
        super.onResume();
        //start location updates:
        if (checkPermission())
            startUpdatingLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop requesting location:
        mApiClient.removeLocationUpdates(locationCallback);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        //we want to delay the init until we have context.
        initClient();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!checkPermission()) {
            askForLocationPermission();
        }

        //add a fragment: in a fragment: //getChildFragmentManager

        SupportMapFragment mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(this);
        getChildFragmentManager().beginTransaction().replace(R.id.frame, mapFragment).commit();

    }

    //5 Last Known location:
    private void getUsersLocation() {
        //last known location: Maybe null
        //Task<Location>
        mApiClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {

                String text = location.getLatitude() + "," + location.getLongitude();
                textView.setText(text);
                //Geocoder may help us convert this to address
            } else {
                Toast.makeText(getContext(), "No Last Known Location", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to get Location. Turn on location services on your device", Toast.LENGTH_SHORT).show();
        });
    }

    //Location Updates Part 2)
    //Location request:
    private LocationRequest getLocationRequest() {
        LocationRequest request = new LocationRequest();
        //request.setNumUpdates(1);
        request.setInterval(2 * 1000); //(2 seconds)
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Battery consumption (vs) accuracy
        request.setFastestInterval(1000); //if other apps already get the location more rapidly -> we want in.
        // request.setSmallestDisplacement(100); //only callback on updates over 100M
        return request;
    }

    //Location Updates Part 3)
    //request location updates:
    private void startUpdatingLocation() {
        //Looper.getMainLooper()
        mApiClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
    }


    //Permission: 1
    //the fragment should request permission:
    private void askForLocationPermission() {
        if (getActivity() != null)
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
    }

    //Permission: 2
    //test if we have permission
    private boolean checkPermission() {
        int result = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }

    //Permission: 3
    //onRequestPermissionResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //called when the user clicks the dialog
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 1) {
            //we have permission:
        }
    }

    //when the map is ready -> save a reference to it
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
    }
}