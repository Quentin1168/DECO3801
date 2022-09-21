package com.example.deco3801project;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    // Access background resources
    Resources res;
    Drawable bgImage1;
    Drawable bgImage2;
    Drawable bgImage3;
    public Drawable[] bgImages;
    // Text buttons
    public String[] buttonsArray = {"","","Get Started!"};
    // Use this to place images on top of background
    public int[] imagesArray = {R.drawable.drinkwater, R.drawable.nfcbottle, R.drawable.carryit};
    // Title message & description array
    public String[] titleArray = {"Welcome!","Less hassle with our Smart Bottle","Start Today!"};
    public String[] descriptionArray = {"Our smart water bottle & app are simpler and better",
            "Our smart bottle automatically gets your water intake",
            "Just with your age and gender, or your essential daily water intake"};
    public int[] backgroundColourArray = {Color.LTGRAY, Color.LTGRAY, Color.LTGRAY};

    public SliderAdapter(Context context) {
        this.context = context;
        res = this.context.getResources();
        this.bgImages = new Drawable[3];
        this.bgImages = setBgImages();
        bgImage1 = ResourcesCompat.getDrawable(res, R.drawable.drinkwater, null);
        bgImage2 = ResourcesCompat.getDrawable(res, R.drawable.nfcbottle, null);
        bgImage3 = ResourcesCompat.getDrawable(res, R.drawable.carryit, null);
    }

    // set background images to an array if we want to use in slider
    private Drawable[] setBgImages() {
        bgImages[0] = bgImage1;
        bgImages[1] = bgImage2;
        bgImages[2] = bgImage3;
        return bgImages;
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
        FrameLayout frameLayout = (FrameLayout)  view.findViewById(R.id.frameLayout);
        ImageView imageView = (ImageView) view.findViewById(R.id.slideimg);
        TextView t1_title = (TextView) view.findViewById(R.id.txtTitle);
        TextView t2_desc = (TextView) view.findViewById(R.id.txtDescription);
        TextView t3_btn = (TextView) view.findViewById(R.id.txtButton);
        //frameLayout.setBackground(bgImages[position]);
        frameLayout.setBackgroundColor(backgroundColourArray[position]);
        imageView.setImageResource(imagesArray[position]);
        t1_title.setText(titleArray[position]);
        t2_desc.setText(descriptionArray[position]);
        t3_btn.setText(buttonsArray[position]);
        container.addView(view);
        return view;
    }

}
