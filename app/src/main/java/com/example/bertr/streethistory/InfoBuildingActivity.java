package com.example.bertr.streethistory;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Activity used to display a TabLayout for a building
 */
public class InfoBuildingActivity extends AppCompatActivity {


    /**
     * Current building described in betwwen the three fragments
     */
    private Building building;


    /**
     * Extras used to get the coordinates of the building
     * (got from the marker clicked)
     */
    private Bundle extras;


    /**
     * Method used to initialize the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.building);

        // Setting the view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpagertab);
        viewPager.setOffscreenPageLimit(3);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        // Getting the data from the intent
        extras = getIntent().getExtras();

        // Initializing the toolbar as an action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar;

        if((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if(extras != null) actionBar.setTitle(extras.getString("name"));
        }

    }


    /**
     * Method used to prepare the building according to the JSON (from PHP)
     */
    public void prepareData() {
        JSONObject json = getBuildingInfo(extras.getDouble("latitude"), extras.getDouble("longitude"));
        building = new Building(json);
    }


    /**
     * Method used to get the JSON Object linked to the building
     *
     * @return
     *      Building in JSON format
     */
    private JSONObject getBuildingInfo(double latitude, double longitude) {

        // Creating the URL
        String u = "https://beber.000webhostapp.com/building";
        u += "?latitude=" + latitude;
        u += "&longitude=" + longitude;

        try {
            if((latitude != 0) && (longitude != 0)) {

                // Getting the data from database (via PHP)
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

                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                }

                urlConnection.disconnect();
                br.close();
                return new JSONObject(buffer.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


    /**
     * Method used to execute an action when the back button is pressed
     */
    @Override
    public void onBackPressed(){
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Method used to detect when the back button is pressed
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    /**
     * Method used to get the current building described
     *
     * @return
     *      Current Building
     */
    public Building getBuilding() {
        return building;
    }


}
