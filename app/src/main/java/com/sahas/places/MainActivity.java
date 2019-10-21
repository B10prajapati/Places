package com.sahas.places;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String apiKey = "AIzaSyAMSlQx1OuofuGh5OuZRocNAUHMvEPGL7c";

    private String TAG = MainActivity.class.getName();

    private TextView textView;

    private TextView textView2;

    private PlacesClient placesClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        textView = (TextView) findViewById(R.id.textView);
                        textView2 = (TextView) findViewById(R.id.textView2);

                        // Initialize the SDK
                        Places.initialize(getApplicationContext(), apiKey);

                        // Create a Places client instance
                        placesClient = Places.createClient(MainActivity.this);


                        /**************************Current Location *********************/

                        //Use fields to definethe data types of return
                        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

                        // Use the builder to create a FindCurrentPlaceRequest.
                        FindCurrentPlaceRequest request =
                                FindCurrentPlaceRequest.newInstance(placeFields);

                        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                        placeResponse.addOnCompleteListener(task -> {

                            String logMessage = "";

                            if (task.isSuccessful()){
                                FindCurrentPlaceResponse findCurrentPlaceResponse = task.getResult();
                                for (PlaceLikelihood placeLikelihood : findCurrentPlaceResponse.getPlaceLikelihoods()) {
                                    logMessage += String.format("Place '%s' has likelihood: %f",
                                            placeLikelihood.getPlace().getName(),
                                            placeLikelihood.getLikelihood()) + "\n\n";
                                    Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                            placeLikelihood.getPlace().getName(),
                                            placeLikelihood.getLikelihood()));
                                }
                                textView2.setText(logMessage);
                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof ApiException) {
                                    ApiException apiException = (ApiException) exception;
                                    logMessage += "Place not found: " + apiException.getStatusCode();

                                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                                }

                                textView2.setText(logMessage);
                            }
                        });








/**************************AUTOComplete Searc BAr*********************/

                        // Initialize the AutocompleteSupportFragment
                        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

                        // Filters autocommplete results to a specfic country
                        autocompleteFragment.setCountry("NP");

                        // Filters autocomplete results to a specific type
                        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);

                        // Specify the types of place data on return
                        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

                        // Set up a PlaceSelectionListener to handle the response
                        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                            @Override
                            public void onPlaceSelected(@NonNull Place place) {
                                textView.setText("Place:  " + place.getName() + ", " + place.getId());
                                Log.i("MainActivity", "Place:  " + place.getName() + ", " + place.getId());
                            }

                            @Override
                            public void onError(@NonNull Status status) {
                                textView.setText("An error occured: " + status);
                                Log.i("MainActivity", "An error occured: " + status);
                            }
                        });

                        // Define a Place ID.
                        String placeId = "Bhaktapur";
/*
// Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

// Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            textView2.setText("Place found: " + place.getName());
            Log.i(TAG, "Place found: " + place.getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                textView2.setText("Place not found: " + exception.getMessage());
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });*/
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }
}
