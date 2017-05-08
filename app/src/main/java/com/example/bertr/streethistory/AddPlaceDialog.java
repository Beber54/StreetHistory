package com.example.bertr.streethistory;

import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;


/**
 * Class used to display a dialog
 * (to add a new building in the remote database)
 */
public class AddPlaceDialog extends DialogFragment {


    /**
     * Map fragment associated
     */
    private MapFragment mapFragment;


    /**
     * Method used to set the map fragment
     *
     * @param mapF
     *      Map Fragment
     */
    public void setMapFragment(MapFragment mapF) {
        mapFragment = mapF;
    }


    /**
     * Method used to initialize the dialog layout
     *
     * @return
     *      View associated to the dialog
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog, container, false);

        // Initializing the toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarDialog);
        toolbar.setTitle("Add New Place");

        // Setting the toolbar as an action bar
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        ActionBar actionBar = appCompatActivity.getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }

        setHasOptionsMenu(true);
        return view;

    }


    /**
     * Method used to initialize the dialog without title by default
     *
     * @return
     *      Dialog freshly created
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    /**
     * Method used to add a 'SAVE' button to the dialog
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menudialog, menu);
    }


    /**
     * Method used to detect which option was used by the user
     * ('Cancel' or 'Save')
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_save) {
            postBuilding();
            dismiss();
            hideKeyboard();
            mapFragment.removeLastMarkerAdded();
            return true;
        } else if(id == android.R.id.home){
            mapFragment.setNewMarker(false);
            dismiss();
            hideKeyboard();
            mapFragment.removeLastMarkerAdded();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    /**
     * Method used to save the building in the remote database
     * (After clicking on 'Save')
     */
    private void postBuilding() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                // Creating the url with specific parameters for the building
                double latitude = getArguments().getDouble("latitude");
                double longitude = getArguments().getDouble("longitude");
                HashMap<String, Object> data = reverseGeocoding(latitude, longitude);
                String u = "https://beber.000webhostapp.com/buildingP";
                StringBuilder params = encodeParams(data);

                try {
                    if((latitude != 0) && (longitude != 0)) {

                        // Posting data to the remote database (via PHP)
                        URL url = new URL(u);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");

                        OutputStream os = urlConnection.getOutputStream();
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        bw.write(params.toString());
                        bw.flush();
                        bw.close();
                        os.close();
                        urlConnection.connect();

                        // Verifying the code status & running a new thread for displaying a toast
                        final int codeStatus = urlConnection.getResponseCode();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(codeStatus == 200) {
                                    Toast.makeText(
                                            mapFragment.getActivity().getBaseContext(),
                                            "New building saved!\r\nThe new marker will be displayed next time.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                } else {
                                    Toast.makeText(
                                            mapFragment.getActivity().getBaseContext(),
                                            "Building not saved!\r\nVerify your internet connection...",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        thread.start();

    }


    /**
     * Method used to encode parameters for the url
     *
     * @return
     *      String of encoded parameters
     */
    private StringBuilder encodeParams(HashMap<String, Object> data) {

        StringBuilder params = new StringBuilder();
        boolean first = true;

        for(String k : data.keySet()) {

            if(first) {
                first = false;
            } else {
                params.append("&");
            }

            try {
                params.append(URLEncoder.encode(k, "UTF-8"));
                params.append("=");
                params.append(URLEncoder.encode(String.valueOf(data.get(k)), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        return params;

    }


    /**
     * Method used to get data from a latitude and a longitude
     *
     * @return
     *      Data (street, city, country...) according to the coordinates
     */
    private HashMap<String, Object> reverseGeocoding(double latitude, double longitude) {

        // Using geocoder to get data about the place
        HashMap<String, Object> data = new HashMap<String, Object>();
        String name = ((EditText) getView().findViewById(R.id.editNamePlace)).getText().toString();
        String desc = ((EditText) getView().findViewById(R.id.editDescPlace)).getText().toString();
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> adresses = null;

        try {
            adresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if((adresses != null) && (adresses.size() > 0)) {
            Address address = adresses.get(0);
            data.put("street", address.getAddressLine(0));
            data.put("postalCode", address.getPostalCode());
            data.put("country", address.getCountryName());
            data.put("city", address.getLocality());
        }

        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("name", name);
        data.put("description", desc);
        return data;

    }


    /**
     * Method used to hide the keyboard when the dialog is closed
     * (After 'Cancel' or 'Save')
     */
    private void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getView().findViewById(R.id.editDescPlace).getWindowToken(), 0);
    }

}
