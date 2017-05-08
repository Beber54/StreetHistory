package com.example.bertr.streethistory;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Class used to display the Home Fragment of the application
 */
public class HomeFragment extends Fragment {


    /**
     * Method used to initialize the fragment
     *
     * @return
     *      View associated to the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home, container, false);
        setRetainInstance(true);

        Typeface fontTitle = Typeface.createFromAsset(getContext().getAssets(), "moon_flower.ttf");
        TextView street = (TextView) view.findViewById(R.id.street);
        TextView history = (TextView) view.findViewById(R.id.history);
        street.setTypeface(fontTitle);
        history.setTypeface(fontTitle);
        street.setTextSize(60);
        history.setTextSize(60);

        return view;

    }

}
