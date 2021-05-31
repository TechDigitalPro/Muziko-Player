package com.whitebird.music.fragments.Intro;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.whitebird.music.R;

public class Intro6 extends Fragment {

    public Intro6() {
        // Required empty public constructor
    }

    public static Intro6 newInstance() {
        return new Intro6();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_intro, container, false);

        ImageView introIcon = rootView.findViewById(R.id.introIcon);

        TextView introText = rootView.findViewById(R.id.introText);
        TextView introExplain = rootView.findViewById(R.id.introExplain);
        rootView.findViewById(R.id.main).setBackgroundColor(Color.parseColor("#5B0086"));
        introIcon.setImageResource(R.drawable.ic_intro_tag_edit);
        introText.setText("Tag Editor");
        introExplain.setText("Organise your music collection with tag editor.");

        return rootView;
    }
}
