package com.example.bertr.streethistory;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;


/**
 * Class used to display the fragment for Preferences
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {


    /**
     * Method used to initialize the fragment
     *
     * @return
     *      View associated to the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings, container, false);
        setRetainInstance(true);

        view.findViewById(R.id.mapStyleStandard).setOnClickListener(this);
        view.findViewById(R.id.mapStyleRetro).setOnClickListener(this);
        view.findViewById(R.id.mapStyleNight).setOnClickListener(this);
        view.findViewById(R.id.mapStyleAubergine).setOnClickListener(this);

        SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        setCheckboxes(view, preferences.getString("mapStyle", "standard"));
        return view;

    }


    /**
     * Method used to detect the click on one of the images
     */
    @Override
    public void onClick(View view) {

        if(getView() != null) {

            ImageView mapStyle = (ImageView) view;
            String tag = (String) mapStyle.getTag();
            setCheckboxes(getView(), tag);

            SharedPreferences preferences = getActivity().getSharedPreferences("prefs", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("mapStyle", tag);
            editor.apply();

        }

    }


    /**
     * Method used to select the right checkbox according to the image clicked
     */
    private void setCheckboxes(View view, String tag) {

        CheckBox checkBoxStandard = (CheckBox) view.findViewById(R.id.checkBoxStandard);
        CheckBox checkBoxRetro = (CheckBox) view.findViewById(R.id.checkBoxRetro);
        CheckBox checkBoxNight = (CheckBox) view.findViewById(R.id.checkBoxNight);
        CheckBox checkBoxAubergine = (CheckBox) view.findViewById(R.id.checkBoxAubergine);

        checkBoxStandard.setChecked(false);
        checkBoxRetro.setChecked(false);
        checkBoxNight.setChecked(false);
        checkBoxAubergine.setChecked(false);

        switch(tag){
            case "standard": checkBoxStandard.setChecked(true); break;
            case "retro": checkBoxRetro.setChecked(true); break;
            case "night": checkBoxNight.setChecked(true); break;
            case "aubergine": checkBoxAubergine.setChecked(true); break;
        }

    }

}
