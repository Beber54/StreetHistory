package com.example.bertr.streethistory;

import android.app.Dialog;
import android.content.Context;
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class UpdatePlaceDialog extends DialogFragment {

    /**
     * History Fragment
     */
    private HistoryFragment historyFragment;


    /**
     * Method used to set the history fragment
     *
     * @param histoF
     *      History Fragment
     */
    public void setHistoryFragment(HistoryFragment histoF) {
        historyFragment = histoF;
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

        View view = inflater.inflate(R.layout.edit, container, false);

        // Initializing the toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarDialogEdit);
        toolbar.setTitle("Edit");

        // Setting the edit text
        EditText editText = (EditText) view.findViewById(R.id.editDescPopup);
        editText.setText(getArguments().getString("desc"));

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
        return  dialog;
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
            postModification();
            dismiss();
            hideKeyboard();
            return true;
        } else if(id == android.R.id.home){
            dismiss();
            hideKeyboard();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    /**
     * Method used to save the new description in the remote database
     * (After clicking on 'Save')
     */
    private void postModification() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    if(getView() != null) {

                        // Creating the url with specific parameter for updating
                        EditText editText = (EditText) getView().findViewById(R.id.editDescPopup);
                        String newDesc = String.valueOf(editText.getText());
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("latitude", getArguments().getDouble("latitude"));
                        data.put("longitude", getArguments().getDouble("longitude"));
                        data.put("description", newDesc);
                        String u = "https://beber.000webhostapp.com/updateDesc";

                        // Posting data to the remote database (via PHP)
                        URL url = new URL(u);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");

                        System.out.println("params = " + encodeParams(data));

                        OutputStream os = urlConnection.getOutputStream();
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        bw.write(encodeParams(data).toString());
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
                                            historyFragment.getActivity().getBaseContext(),
                                            "New description saved!\r\nThe new description will appear next time.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                } else {
                                    Toast.makeText(
                                            historyFragment.getActivity().getBaseContext(),
                                            "Description not saved!\r\nVerify your internet connection...",
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
     * Method used to hide the keyboard when the dialog is closed
     * (After 'Cancel' or 'Save')
     */
    private void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getView().findViewById(R.id.editDescPopup).getWindowToken(), 0);
    }


}
