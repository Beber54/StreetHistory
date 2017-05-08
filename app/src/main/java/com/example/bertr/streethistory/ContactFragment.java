package com.example.bertr.streethistory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import java.net.URL;


/**
 * Class used to display the Contact Fragment for a building
 * (3rd tab)
 */
public class ContactFragment extends Fragment {

    /**
     * Activity associated to the fragment
     */
    private InfoBuildingActivity activity;


    /**
     * Method used to initialize the fragment
     *
     * @return
     *      View associated to the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact, container, false);
        setRetainInstance(true);
        return view;
    }


    /**
     * Method used to display data from database
     * (Telephone number, Street, City, Country, Website... + Image)
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = (InfoBuildingActivity) getActivity();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                activity.prepareData();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayContactInformation();
                        displayImage();
                    }
                });

            }
        });

        thread.start();

    }


    /**
     * Method used to display the contact information
     */
    private void displayContactInformation() {

        if(getView() != null) {

            String htmlText = " %s ";
            WebView contactInformation = (WebView) getView().findViewById(R.id.contactInformationWV);
            contactInformation.setClickable(true);

            // Creating the HTML content for the Web View
            Building b = activity.getBuilding();
            String text = "<html>" +
                            "<head>" +
                              "<meta charset=\"UTF-8\">" +
                            "</head>" +
                            "<body style=\"text-align: center;\">";

            text += "<p>";
            if(!b.getWebsite().equals("null")) text += "<a href=\"" + b.getWebsite() + "\">" + b.getWebsite() + "</a><br>";
            if(!b.getPhone().equals("null")) text += b.getPhone() + "<br>";
            text += "</p><p>";
            if(!b.getStreet().equals("null")) text += b.getStreet() + "<br>";
            if(!b.getPostalCode().equals("null")) text += b.getPostalCode();
            if(!b.getCity().equals("null")) text += " " + b.getCity();
            if((!b.getCity().equals("null")) || (!b.getCity().equals("null"))) text += "<br>";
            if(!b.getCountry().equals("null")) text += b.getCountry();
            text += "</p></body></html>";
            contactInformation.loadData(String.format(htmlText, text), "text/html; charset=utf-8", "utf-8");


            // Hiding the progress bar and displaying the contact information
            getView().findViewById(R.id.progressBarContact).setVisibility(View.GONE);
            getView().findViewById(R.id.loadingContact).setVisibility(View.GONE);
            getView().findViewById(R.id.contactInformation).setVisibility(View.VISIBLE);

        }

    }


    /**
     * Method used to display an image got from Google Places API
     */
    private void displayImage() {

        if(getView() != null) {

            ImageView image = (ImageView) getView().findViewById(R.id.contactImg);
            if(image != null) {
                try {

                    // Creating the url with the photo reference (saved before in the database)
                    String u = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
                    u += activity.getBuilding().getImages().getJSONObject(2).getString("reference");
                    u += "&key=YOUR_API_KEY_HERE";
                    URL url = new URL(u);

                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    image.setImageBitmap(bmp);

                } catch (Exception e) {

                    // Displaying a specific image in case of an error
                    image.setImageResource(R.drawable.noimagefound);
                    image.setScaleType(ImageView.ScaleType.CENTER);
                    image.setScaleX(0.7f);
                    image.setScaleY(0.7f);
                    e.printStackTrace();
                }
            }


            // Hiding the progress bar
            getView().findViewById(R.id.progressBarContactImg).setVisibility(View.GONE);
            getView().findViewById(R.id.loadingContactImg).setVisibility(View.GONE);

        }

    }


}
