package com.example.kabs_.radiov2;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kabs- on 07/07/2017.
 */

public class FragmentFacebook extends Fragment{
    private final String TAG = "com.example.app.FragmentFacebook";

    private Activity mActivity;

    public void onAttach(Activity act){
        super.onAttach(act);

        this.mActivity = act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.facebook, container, false);

        //do whatever you want here - like adding a listview or anything

        return view;
    }
}
