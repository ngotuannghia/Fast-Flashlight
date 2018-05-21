package com.smobileteam.flashlight.screenlight;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.view.ColorPicker;
import com.smobileteam.flashlight.view.OnColorPickerListener;

/**
 * Created by Duong Anh Son on 3/16/2017.
 * FlashlightBitbucket
 */

public class ColorScreenFragment extends Fragment implements OnColorPickerListener{
    private RelativeLayout mBackgroundColor;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.color_screen_fragment, container, false);
        mBackgroundColor = (RelativeLayout) rootView.findViewById(R.id.root);
        mBackgroundColor.setBackgroundColor(Color.WHITE);
        final ColorPicker colorPicker = (ColorPicker)  rootView.findViewById(R.id.color_picker);
        colorPicker.setOnColorPickerListene(this);
        mBackgroundColor.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return rootView;
    }


    @Override
    public void updateColor(int color) {
        mBackgroundColor.setBackgroundColor(color);
    }

    @Override
    public void OnInnerWheelClick() {
        getActivity().onBackPressed();
    }
}
