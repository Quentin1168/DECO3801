package com.example.deco3801project;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;

    Resources res;
    Drawable bgImage1;
    Drawable bgImage2;
    Drawable bgImage3;
    public Drawable[] bgImages = new Drawable[3];

    public int[] imagesArray = {R.drawable.background1, R.drawable.background2, R.drawable.background3};
    public String[] titleArray = {"Welcome to our Water Intake App","Bottle Image Here","Sign Up?"};
    public String[] descriptionArray = {"description here1", "description here2", "description here3"};
    public int[] backgroundColourArray = {Color.LTGRAY,
            Color.LTGRAY, Color.LTGRAY};

    public SliderAdapter(Context context) {
        this.context = context;
        res = this.context.getResources();
        this.bgImages = setBgImages();
        bgImage1 = ResourcesCompat.getDrawable(res, R.drawable.background1, null);
        bgImage2 = ResourcesCompat.getDrawable(res, R.drawable.background2, null);
        bgImage3 = ResourcesCompat.getDrawable(res, R.drawable.background3, null);
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
        container.removeView((FrameLayout)object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide, container, false);
        FrameLayout linearLayout = (FrameLayout)  view.findViewById(R.id.linearLayout);
        //ImageView imageView = (ImageView) view.findViewById(R.id.slideimg);
        TextView t1_title = (TextView) view.findViewById(R.id.txtTitle);
        TextView t2_desc = (TextView) view.findViewById(R.id.txtDescription);
        linearLayout.setBackground(bgImages[position]);
        linearLayout.setBackgroundColor(backgroundColourArray[position]);
        //imageView.setImageResource(imagesArray[position]);
        t1_title.setText(titleArray[position]);
        t2_desc.setText(descriptionArray[position]);
        container.addView(view);
        return view;
    }

    private Drawable[] setBgImages() {
        bgImages[0] = bgImage1;
        bgImages[1] = bgImage2;
        bgImages[2] = bgImage3;
        return bgImages;
    }


}
