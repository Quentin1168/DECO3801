package com.example.deco3801project;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;

    public int[] imagesArray = {R.drawable.background1, R.drawable.background2, R.drawable.background3};
    public String[] titleArray = {"WITCH","ONE","ULIKE?"};
    public String[] descriptionArray = {"description here1", "description here2", "description here3"};
    public int[] backgroundColourArray = {Color.rgb(55,55,55),
            Color.rgb(239,55,55), Color.rgb(110,49,89)};

    public SliderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view==object);
    }

    @Override
    public int getCount() {
        return titleArray.length;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide, container, false);

        LinearLayout linearLayout = (LinearLayout)  view.findViewById(R.id.linearLayout);
        ImageView imageView = (ImageView) view.findViewById(R.id.slideimg);
        TextView t1_title = (TextView) view.findViewById(R.id.txtTitle);
        TextView t2_desc = (TextView) view.findViewById(R.id.txtDescription);
        linearLayout.setBackgroundColor(backgroundColourArray[position]);
        imageView.setImageResource(imagesArray[position]);
        t1_title.setText(titleArray[position]);
        t2_desc.setText(descriptionArray[position]);
        container.addView(view);
        return view;
    }
}
