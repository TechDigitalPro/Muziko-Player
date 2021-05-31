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


public class Intro4 extends Fragment {

	public Intro4() {
		// Required empty public constructor
	}

	public static Intro4 newInstance() {
		return new Intro4();
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
		rootView.findViewById(R.id.main).setBackgroundColor(Color.parseColor("#1865D9"));
		introIcon.setImageResource(R.drawable.ic_intro_cut);
		introText.setText("Mp3 Cutter");
		introExplain.setText("Cut the best out of your song fast and easily.");

		return rootView;
	}


}
