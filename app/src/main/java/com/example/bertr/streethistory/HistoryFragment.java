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
 * Class used to display the History Fragment for a building
 * (1st tab)
 */
public class HistoryFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.history, container, false);
        setRetainInstance(true);
        return view;
    }


    /**
     * Method used to display data from database
     * (Historical Description + Image)
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
                        displayHistory();
                        displayImage();
                    }
                });
            }
        });

        thread.start();

    }


    /**
     * Method used to display the historical description
     */
    private void displayHistory() {

        if(getView() != null) {

            // Creating the HTML content for the Web View
            String htmlText = " %s ";
            String data = "<body style=\"text-align: justify; font-size: 0.9em;\">" + activity.getBuilding().getDescription() + "</body>";
            WebView webView = (WebView) getView().findViewById(R.id.descWeb);
            webView.loadData(String.format(htmlText, data), "text/html", "utf-8");

            // Hiding the progress bar and displaying the historical description
            getView().findViewById(R.id.progressBarDesc).setVisibility(View.GONE);
            getView().findViewById(R.id.loadingDesc).setVisibility(View.GONE);
            getView().findViewById(R.id.historicalDescription).setVisibility(View.VISIBLE);

        }

    }


    /**
     * Method used to display an image got from Google Places API
     */
    private void displayImage() {

        if(getView() != null) {

            ImageView image = (ImageView) getView().findViewById(R.id.descImg);
            if(image != null) {
                try {

                    // Creating the url with the photo reference (saved before in the database)
                    String u = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
                    u += activity.getBuilding().getImages().getJSONObject(0).getString("reference");
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
            getView().findViewById(R.id.progressBarDescImg).setVisibility(View.GONE);
            getView().findViewById(R.id.loadingDescImg).setVisibility(View.GONE);

        }

    }

}
