package com.sahas.places;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
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

    private String placeId;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        textView = findViewById(R.id.textView);
                        textView2 = findViewById(R.id.textView2);

                        textView2.setMovementMethod(new ScrollingMovementMethod());
                        imageView = findViewById(R.id.imageView2);

                        // Initialize the SDK
                        Places.initialize(getApplicationContext(), apiKey);

                        // Create a Places client instance
                        placesClient = Places.createClient(MainActivity.this);


                        /**************************Current Location *********************/
                        getCurrentLocationLikelihood();
                        /***************************************************************************/

                        /**************************AUTOComplete Searc BAr*********************/
                        createAutoCompleteBar();
                        /***************************************************************************/

                        /*****ByDefault bhaktapur will be loaded . On AutoCompleteSearch succes selected place id will be placed and its detail and photo will be displayed*****/
                        // Define a Place ID.
                        placeId = "ChIJoWuAQq4a6zkRgl5OQHngSVQ"; //Place id for bhaktapur

                        /***************************Place Detail**********************/
                        getPlaceDetail();
                        /***************************************************************************/

                        /**********************Place Photo*************************/
                        getPlacePhoto();
                        /***************************************************************************/

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

    private void getCurrentLocationLikelihood() {
        //Use fields to definethe data types of return
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest findCurrentPlaceRequest =
                FindCurrentPlaceRequest.newInstance(placeFields);

        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(findCurrentPlaceRequest);
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
    }

    public void createAutoCompleteBar(){

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
                placeId = place.getId();

                getPlaceDetail();
                getPlacePhoto();

                textView.setText("Place:  " + place.getName() + ", " + place.getId());
                Log.i("MainActivity", "Place:  " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NonNull Status status) {
                textView.setText("An error occured: " + status);
                Log.i("MainActivity", "An error occured: " + status);
            }
        });




    }
public void getPlaceDetail(){

/***************************Place Detail**********************/
    // Specify the fields to return.
    List<Place.Field> placeFieldsDetails = Arrays.asList(Place.Field.ID, Place.Field.NAME);

    // Construct a request object, passing the place ID and fields array.
    FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFieldsDetails);

    placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
        Place place = response.getPlace();
        textView.setText("Place found: " + place.getName());
        Log.i(TAG, "Place found: " + place.getName());
    }).addOnFailureListener((exception) -> {
        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            int statusCode = apiException.getStatusCode();
            // Handle error with given status code.
            textView.setText("Place not found: " + exception.getMessage());
            Log.e(TAG, "Place not found: " + exception.getMessage());
        }
    });

/***************************************************************************/


}
public void getPlacePhoto(){

/**********************Place Photo*************************/


// Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
    List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);

// Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
    FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

    placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
        Place place = response.getPlace();

        // Get the photo metadata.
        PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

        // Get the attribution text.
        String attributions = photoMetadata.getAttributions();

        // Create a FetchPhotoRequest.
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500) // Optional.
                .setMaxHeight(300) // Optional.
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    });


/***************************************************************************/
}
}
