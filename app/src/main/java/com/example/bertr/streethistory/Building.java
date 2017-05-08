package com.example.bertr.streethistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Class used to get an instance of a building
 * thanks to data retrieved from database
 */
public class Building {

    private String name;
    private double latitude;
    private double longitude;
    private String street;
    private String postalCode;
    private String city;
    private String country;
    private String phone;
    private String website;
    private String description;
    private JSONArray comments;
    private JSONArray images;


    /**
     * Constructor of a building used to initialize all the attributes
     *
     * @param json
     *      JSON Object got from database
     */
    public Building(JSONObject json) {

        try {

            name = json.getString("name");
            latitude = json.getDouble("latitude");
            longitude = json.getDouble("longitude");
            street = json.getString("street");
            postalCode = json.getString("postalCode");
            city = json.getString("city");
            country = json.getString("country");
            phone = json.getString("phone");
            website = json.getString("website").replace("\\", "");
            description = json.getString("description");
            comments = json.getJSONArray("comments");
            images = json.getJSONArray("images");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getStreet() {
        return street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

    public String getDescription() {
        return description;
    }

    public JSONArray getComments() {
        return comments;
    }

    public JSONArray getImages() {
        return images;
    }

}
