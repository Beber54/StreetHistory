package com.example.bertr.streethistory;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;


/**
 * Class used to display the ReviewsFragment for a building
 * (2nd tab)
 */
public class ReviewsFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.reviews, container, false);
        setRetainInstance(true);
        return view;
    }


    /**
     * Method used to display data from database
     * (Authors, Ratings, Comments...)
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
                        displayReviews();
                        displayImage();
                    }
                });
            }
        });

        thread.start();

    }


    /**
     * Method used to the reviews
     */
    private void displayReviews() {

        if(getView() != null) {
            try {

                // Filling all the UI components of the layout
                Building b = activity.getBuilding();
                JSONArray comments = b.getComments();
                Resources res = getResources();

                for(int i = 0; i < comments.length(); i++) {

                    JSONObject c = comments.getJSONObject(i);
                    int id = res.getIdentifier("viewStub" + (i+1), "id", activity.getPackageName());
                    ViewStub stub = (ViewStub) getView().findViewById(id);
                    stub.setLayoutResource(R.layout.comment);
                    View inflated = stub.inflate();

                    TextView author = (TextView) inflated.findViewById(R.id.author);
                    TextView stars = (TextView) inflated.findViewById(R.id.numberOfStars);
                    TextView comment = (TextView) inflated.findViewById(R.id.comment);

                    author.setText(c.getString("author"));
                    stars.setText(String.valueOf(c.getInt("rating")));
                    comment.setText(c.getString("text"));

                }

            } catch(Exception e) {
                e.printStackTrace();
            }


            // Hiding the progress bars and displaying the reviews
            getView().findViewById(R.id.progressBarReviews).setVisibility(View.GONE);
            getView().findViewById(R.id.loadingReviews).setVisibility(View.GONE);
            getView().findViewById(R.id.line1).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.line2).setVisibility(View.VISIBLE);

        }

    }


    /**
     * Method used to display an image got from Google Places API
     */
    private void displayImage() {

        if(getView() != null) {

            ImageView image = (ImageView) getView().findViewById(R.id.reviewsImg);
            if(image != null) {
                try {

                    // Creating the url with the photo reference (saved before in the database)
                    String u = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
                    u += activity.getBuilding().getImages().getJSONObject(1).getString("reference");
                    u += "&key=YOUR_API_KEY_HERE";
                    URL url = new URL(u);

                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    image.setImageBitmap(bmp);

                } catch(Exception e) {

                    // Displaying a specific image in case of an error
                    image.setImageResource(R.drawable.noimagefound);
                    image.setScaleType(ImageView.ScaleType.CENTER);
                    image.setScaleX(0.7f);
                    image.setScaleY(0.7f);
                    e.printStackTrace();
                }
            }


            // Hiding the progress bar
            getView().findViewById(R.id.progressBarReviewsImg).setVisibility(View.GONE);
            getView().findViewById(R.id.loadingReviewsImg).setVisibility(View.GONE);

        }

    }

}
