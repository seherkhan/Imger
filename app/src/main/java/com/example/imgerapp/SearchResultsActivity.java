package com.example.imgerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

/**
 * Gallery where filtered pictures are displayed
 **/
public class SearchResultsActivity extends AppCompatActivity {
    private ArrayList<File> dataset;
    private String APPUSERID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // span count = 2, two images in each row
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this,2);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.glidegallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        APPUSERID = args.getString("APPUSERID");
        dataset = (ArrayList<File>) args.getSerializable("LIST");
        Toast.makeText(getApplicationContext(), "Number of filtered images: "+dataset.size(), Toast.LENGTH_LONG).show();
        SearchResultsActivity.ImageGalleryAdapter adp = new SearchResultsActivity.ImageGalleryAdapter(this,dataset);
        recyclerView.setAdapter(adp);
    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>  {
        private ArrayList<File> dataset;
        private Context context;

        public ImageGalleryAdapter(Context context, ArrayList<File> dataset) {
            this.context = context;
            this.dataset = dataset;
        }
        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View photoView = inflater.inflate(R.layout.gallery_img, parent, false);
            ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int i) {
            ImageView imageView = holder.mPhotoImageView;
            Glide.with(context)
                    .load(Uri.fromFile(dataset.get(i)))
                    .placeholder(R.drawable.ic_loading)
                    .into(imageView);
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView mPhotoImageView;

            public MyViewHolder(View itemView) {

                super(itemView);
                mPhotoImageView = (ImageView) itemView.findViewById(R.id.img);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int i = getAdapterPosition();
                if(i != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    Bundle args = new Bundle();
                    args.putString("APPUSERID",APPUSERID);
                    args.putSerializable("IMAGE", dataset.get(i));
                    intent.putExtra("BUNDLE", args);
                    startActivity(intent);
                }
            }
        }

    }
}
