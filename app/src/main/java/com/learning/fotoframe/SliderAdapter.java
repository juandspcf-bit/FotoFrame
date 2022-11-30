package com.learning.fotoframe;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.Holder> {
    ListPhotosFragmentV2.MyLink[] images;
    private BiConsumer<ImageView, Integer> consumerImageView;

    public SliderAdapter(ListPhotosFragmentV2.MyLink[] images, BiConsumer<ImageView, Integer> consumerImageView) {
        this.images = images;
        this.consumerImageView = consumerImageView;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item,parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder viewHolder, int position) {


        //viewHolder.imageView.setImageResource(images[position]);
        consumerImageView.accept(viewHolder.imageView, position);

    }



    @Override
    public int getCount() {
        return images.length;
    }



    public class Holder extends SliderViewAdapter.ViewHolder{

        ImageView imageView;

        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);


        }
    }


}


