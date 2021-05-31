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


public class Intro5 extends Fragment {

	public Intro5() {
		// Required empty public constructor
	}

	public static Intro5 newInstance() {
		return new Intro5();
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
		rootView.findViewById(R.id.main).setBackgroundColor(Color.parseColor("#1554BF"));
		introIcon.setImageResource(R.drawable.ic_intro_sleep_timer);
		introText.setText("Sleep Timer");
		introExplain.setText("Listen to music while sleeping and turn off music automatically with sleep timer.");

		return rootView;
	}


}
