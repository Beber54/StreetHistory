package com.example.bertr.streethistory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPointStyle;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Class used to display the Map Fragment
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener {


    /**
     * Goole API Client
     */
    private GoogleApiClient gac;


    /**
     * Google Map displayed
     */
    private GoogleMap mapGoogle;


    /**
     * Last marker temporarily added
     */
    private Marker lastMarkerAdded;


    /**
     * Boolean used to know if a new building is currently being created
     */
    private boolean newMarker;


    /**
     * Method used to initialize the fragment
     *
     * @return
     *      View associated to the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.map, container, false);
        setRetainInstance(true);
        newMarker = false;

        // Creating an instance of GoogleAPIClient
        if (gac == null) {
            gac = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        return view;

    }


    /**
     * Method used to get the map from the XML layout
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapView mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }


    /**
     * Method used to set the listeners for the map
     *
     * @param googleMap
     *      Map displayed
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapGoogle = googleMap;
        mapGoogle.setOnMapLoadedCallback(this);
        mapGoogle.setOnMarkerClickListener(this);
        mapGoogle.setOnInfoWindowClickListener(this);
        mapGoogle.setOnMapLongClickListener(this);
        mapGoogle.setOnMapClickListener(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setMapStyle();
    }


    /**
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mapGoogle.setMyLocationEnabled(true);
        Location loc = LocationServices.FusedLocationApi.getLastLocation(gac);

        if(loc != null) {
            LatLng marker = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker, 17);
            mapGoogle.animateCamera(cameraUpdate);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}


    /**
     * Method used to be connected to the Google API Client
     */
    public void onStart() {
        gac.connect();
        super.onStart();
    }


    /**
     * Method used to be disconnected from the Google API Client
     */
    public void onStop() {
        gac.disconnect();
        super.onStop();
    }


    /**
     * Method used to set the map style with a specific thread
     */
    private void setMapStyle() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Activity.MODE_PRIVATE);
                        MapStyleOptions style;

                        switch(preferences.getString("mapStyle", "standard")) {
                            case "standard": style = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.standard); break;
                            case "retro": style = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.retro); break;
                            case "night": style = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.night); break;
                            case "aubergine": style = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.aubergine); break;
                            default: style = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.standard); break;
                        }

                        // Applyings some JSON for the style
                        mapGoogle.setMapStyle(style);
                    }
                });
            }
        });

        thread.start();

    }


    /**
     * Method used to display the markers according to the GeoJSON file (from PHP)
     */
    private void displayBuildingsWithGeoJson() {

        try {

            // Getting the GeoJSON
            String u = "https://beber.000webhostapp.com/GEOjson";
            URL url = new URL(u);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Length", "0");
            urlConnection.setUseCaches(false);
            urlConnection.setAllowUserInteraction(false);
            urlConnection.connect();

            InputStream in = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuilder buffer = new StringBuilder();

            while((line = br.readLine()) != null) {
                buffer.append(line);
            }

            urlConnection.disconnect();
            br.close();
            parseGeoJSON(buffer.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Method used to parse the GeoJSON and to display the associated markers
     *
     * @param b
     *      String with the JSON content
     */
    private void parseGeoJSON(String b) {

        try {

            JSONObject json = new JSONObject(b);
            GeoJsonLayer layer = new GeoJsonLayer(mapGoogle, json);
            layer.addLayerToMap();

            for(GeoJsonFeature f : layer.getFeatures()) {
                GeoJsonPointStyle point = new GeoJsonPointStyle();
                point.setTitle(f.getProperty("name"));
                point.setSnippet(f.getProperty("street"));
                point.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.location));
                f.setPointStyle(point);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Method used to display the building once the map is loaded
     */
    @Override
    public void onMapLoaded() {
        if(mapGoogle != null) {
            displayBuildingsWithGeoJson();
            if(getView() != null) getView().findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }


    /**
     * Method used to remove the new marker being created
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(newMarker) {
            newMarker = false;
            lastMarkerAdded.remove();
        }
        return false;
    }


    /**
     * Method used to display the Dialog or the TabLayout
     * (depends if a marker is added or not)
     */
    @Override
    public void onInfoWindowClick(Marker marker) {

        // If a marker is not new
        if(!newMarker) {
            Intent intent = new Intent(getContext(), InfoBuildingActivity.class);
            intent.putExtra("latitude", marker.getPosition().latitude);
            intent.putExtra("longitude", marker.getPosition().longitude);
            intent.putExtra("name", marker.getTitle());
            startActivity(intent);

        // If a marker is new
        } else {

            AddPlaceDialog addPlaceDialog = new AddPlaceDialog();
            addPlaceDialog.setMapFragment(this);
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", lastMarkerAdded.getPosition().latitude);
            bundle.putDouble("longitude", lastMarkerAdded.getPosition().longitude);
            addPlaceDialog.setArguments(bundle);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(android.R.id.content, addPlaceDialog);
            transaction.addToBackStack(null);
            transaction.commit();
            newMarker = false;

        }

    }


    /**
     * Method used to add temporarily a new marker after a long click
     */
    @Override
    public void onMapLongClick(LatLng latLng) {

        if(newMarker) {
            lastMarkerAdded.remove();
            newMarker = false;
        }

        MarkerOptions mo = new MarkerOptions()
                .position(latLng)
                .title("Do you want to create a new marker?")
                .snippet("Click here to describe the place...");

        lastMarkerAdded = mapGoogle.addMarker(mo);
        lastMarkerAdded.showInfoWindow();
        mapGoogle.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), mapGoogle.getCameraPosition().zoom));
        newMarker = true;

    }


    /**
     * Method used to remove the new marker added
     * if the user click somewhere else on the map
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if(newMarker) {
            lastMarkerAdded.remove();
            newMarker = false;
        }
    }


    /**
     * Method used to set the boolean value for a new marker
     */
    public void setNewMarker(boolean newM) {
        newMarker = newM;
    }


    /**
     * Method used to relaod the fragment
     */
    public void reloadFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragcontainer, new MapFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }


    /**
     * Method used to remove the last marker added
     */
    public void removeLastMarkerAdded() {
        lastMarkerAdded.remove();
    }

}
