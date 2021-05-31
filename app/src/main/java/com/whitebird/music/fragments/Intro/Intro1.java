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

public class Intro1 extends Fragment {

	public Intro1() {
		// Required empty public constructor
	}

	public static Intro1 newInstance() {
		return new Intro1();
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
        rootView.findViewById(R.id.main).setBackgroundColor(Color.parseColor("#1292AA"));

        TextView introText = rootView.findViewById(R.id.introText);
        TextView introExplain = rootView.findViewById(R.id.introExplain);

		introIcon.setImageResource(R.drawable.ic_intro_ringtone);
		introText.setText("Set RingTone");
		introExplain.setText("Set the start and end time of song and its ready Ringtone");

		return rootView;
	}


}
