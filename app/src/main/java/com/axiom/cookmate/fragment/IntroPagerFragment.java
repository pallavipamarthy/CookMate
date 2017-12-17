package com.axiom.cookmate.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axiom.cookmate.R;

public class IntroPagerFragment extends Fragment {

    private String mHeading;
    private String mText;

    public static IntroPagerFragment newInstance(String heading, String text) {
        IntroPagerFragment fragment = new IntroPagerFragment();
        Bundle args = new Bundle();
        args.putString("heading", heading);
        args.putString("text", text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHeading = getArguments().getString("heading");
        mText = getArguments().getString("text");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pager_fragment, container, false);
        TextView headingView = (TextView) view.findViewById(R.id.pager_heading_view);
        headingView.setText(mHeading);
        TextView textView = (TextView) view.findViewById(R.id.pager_text_view);
        textView.setText(mText);

        return view;
    }
}