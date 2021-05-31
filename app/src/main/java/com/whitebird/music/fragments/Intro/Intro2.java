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


public class Intro2 extends Fragment {

	public Intro2() {
		// Required empty public constructor
	}

	public static Intro2 newInstance() {
		return new Intro2();
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
		rootView.findViewById(R.id.main).setBackgroundColor(Color.parseColor("#E2001A"));

        TextView introText = rootView.findViewById(R.id.introText);
        TextView introExplain = rootView.findViewById(R.id.introExplain);

		introIcon.setImageResource(R.drawable.ic_intro_send_au_clip);
		introText.setText("Send Au-Clip");
		introExplain.setText("Express your thoughts with AU-Clips. Select position and simply share.");

		return rootView;
	}


}
