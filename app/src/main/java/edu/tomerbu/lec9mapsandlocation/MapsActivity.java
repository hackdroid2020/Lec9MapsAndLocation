package edu.tomerbu.lec9mapsandlocation;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    //properties:
    GoogleMap mMap;
    EditText etSearch;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //when we insert a static fragment (XML fragment tag)
        //find the fragment by id:
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //once we have the fragment reference: -> ask the fragment to load the map:
        if (fragment != null)
            fragment.getMapAsync(this);

        etSearch = findViewById(R.id.edit_search);
        btnSearch = findViewById(R.id.button_search);

        btnSearch.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //marker - and animate the map to the marker:
        LatLng beach = new LatLng(32.190553, 34.80741);
        moveMap(beach);
    }

    private void moveMap(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));

        //animate the camera to the location:
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    @Override
    public void onClick(View v) {
        String text = etSearch.getText().toString();
        //the user input is a String -> how do we show it on the map?

        //INTERNET Permission:
        Geocoder coder = new Geocoder(this);
        try {
            List<Address> addressList = coder.getFromLocationName(text, 1);
            if (addressList.size() == 0) {
                //no location found:
                Toast.makeText(this, "No Results", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addressList.get(0);
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();

           moveMap(new LatLng(latitude, longitude));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
