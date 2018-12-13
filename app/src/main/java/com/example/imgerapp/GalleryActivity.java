package com.example.imgerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.imgerapp.MetaData_Firebase.PictureDataset;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Gallery where all pictures are displayed
 **/
public class GalleryActivity extends AppCompatActivity {
    private ArrayList<File> dataset;
    private String APPUSERID;
    private DatabaseReference this_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // span count = 2, two images in each row
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this,2);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.glidegallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        APPUSERID = args.getString("APPUSERID");
        dataset = (ArrayList<File>) args.getSerializable("LIST");
        
	if(dataset.size()==0){
            ((Button) findViewById(R.id.filterButton)).setEnabled(false);
            Toast.makeText(getApplicationContext(), "No jpeg images to load:\n\"ImgerDummy\" folder not found in DCIM or was empty.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Number of jpeg images in gallery: "+dataset.size(), Toast.LENGTH_LONG).show();
        }
       
        GalleryActivity.ImageGalleryAdapter adp = new GalleryActivity.ImageGalleryAdapter(this,dataset);
        recyclerView.setAdapter(adp);

        this_user = FirebaseDatabase.getInstance().getReference().child("users/"+args.getString("APPUSERID"));
    }

    public void optionsClicked(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.gallery_activity_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.gallery_filesize) {
                    Toast.makeText(getApplicationContext(), "By filesize", Toast.LENGTH_LONG).show();
                    filterDialog_filesize();
                }
                else if(menuItem.getItemId() == R.id.gallery_date) {
                    Toast.makeText(getApplicationContext(), "By date", Toast.LENGTH_LONG).show();
                    filterDialog_date();
                }
                else if(menuItem.getItemId() == R.id.gallery_tag) {
                    Toast.makeText(getApplicationContext(), "By tag", Toast.LENGTH_LONG).show();
                    filterDialog_tag();
                }
                else if(menuItem.getItemId() == R.id.gallery_action) {
                    Toast.makeText(getApplicationContext(), "By all", Toast.LENGTH_LONG).show();
                    openFilterOptions();
                }
                return true;
            }
        });
        popup.show();
    }

    private void openFilterOptions() {
        Intent intent = new Intent(this, FilterOptionsActivity.class);
        Bundle args = new Bundle();
        args.putString("APPUSERID",APPUSERID);
        intent.putExtra("BUNDLE", args);
        startActivity(intent);
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

    public void filterDialog_date(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.paramDate);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        final EditText min = new EditText(this);
        final EditText max = new EditText(this);
        min.setHint("Min (MM/DD/YYYY)");
        max.setHint("Max (MM/DD/YYYY)");
        min.setInputType(InputType.TYPE_CLASS_DATETIME);
        max.setInputType(InputType.TYPE_CLASS_DATETIME);
        layout.addView(min);
        layout.addView(max);
        dialog.setView(layout);

        dialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(GalleryActivity.this,"OK pressed",Toast.LENGTH_LONG).show();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        float low;
                        float high;
                        try {
                            System.out.println(sdf.parse(min.getText().toString()));
                            low = sdf.parse(min.getText().toString()).getTime();
                        } catch (ParseException e) {
                            low = 0;
                        }
                        try {
                            System.out.println(sdf.parse(max.getText().toString()));
                            high = sdf.parse(max.getText().toString()).getTime();
                        } catch (ParseException e) {
                            high = Calendar.getInstance().getTime().getTime();
                        }
                        initSearchResults_dateOrfilesize(low,high,"date");
                    }
                });

        dialog.setNegativeButton("Back",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog1 = dialog.create();
        dialog1.show();
    }

    public void filterDialog_filesize(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.paramFilesize);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final EditText min = new EditText(this);
        final EditText max = new EditText(this);
        min.setHint("Min");
        max.setHint("Max");
        min.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        max.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(min);
        layout.addView(max);
        dialog.setView(layout);


        // when save clicked
        dialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(GalleryActivity.this,"OK pressed",Toast.LENGTH_LONG).show();
                        float low;
                        float high;
                        try{
                            low = Float.valueOf(min.getText().toString());
                        }
                        catch (NumberFormatException e){
                            low = 0f;
                        }
                        try{
                            high = Float.valueOf(max.getText().toString());
                        }
                        catch (NumberFormatException e){
                            high = 100f;
                        }
                        initSearchResults_dateOrfilesize(low,high,"filesize");
                    }
                });

        dialog.setNegativeButton("Back",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog1 = dialog.create();
        dialog1.show();
    }
    public void filterDialog_tag(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.paramTag);

        final EditText tag = new EditText(this);
        tag.setHint("comma separated");
        dialog.setView(tag);


        // when save clicked
        dialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(GalleryActivity.this,"OK pressed",Toast.LENGTH_LONG).show();
                        String keywordparam = tag.getText().toString().trim().toLowerCase();
                        if (!TextUtils.isEmpty(keywordparam)) {
                            String[] vals_keywords = keywordparam.split("\\s*,\\s*");
                            for (int i = 0; i < vals_keywords.length; i++) {
                                vals_keywords[i] = vals_keywords[i].trim();
                            }
                            initSearchResults_tag(new HashSet<String>(Arrays.asList(vals_keywords)));
                        }

                    }
                });

        dialog.setNegativeButton("Back",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog1 = dialog.create();
        dialog1.show();
    }

    private void initSearchResults_dateOrfilesize(final float low, final float high, final String type){
        // type is "filesize" or "date"
        final ArrayList<String> paths = new ArrayList<>();
        this_user.child("pictures").orderByChild(type).startAt(low).endAt(high).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    paths.add(child.child("dir").getValue(String.class));
                }
                PictureDataset filtered_pd = new PictureDataset(new ArrayList<>(paths));
                Intent intent = new Intent(GalleryActivity.this, SearchResultsActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("LIST", filtered_pd.getDataset());
                args.putString("APPUSERID",APPUSERID);
                intent.putExtra("BUNDLE", args);
                GalleryActivity.this.startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void initSearchResults_tag(final HashSet<String> vals){
        final Set<String> paths = new HashSet<>();
        this_user.child("tag_index").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) { // get all tags
                    if (vals.contains(child.getKey())) { // if tag is a specified val
                        for (DataSnapshot gchild : child.getChildren()) { // get all its children
                            
                            paths.add(gchild.getValue(String.class));
                        }
                    }
                }
                
                PictureDataset filtered_pd = new PictureDataset(new ArrayList<>(paths));
                Intent intent = new Intent(GalleryActivity.this, SearchResultsActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("LIST", filtered_pd.getDataset());
                args.putString("APPUSERID",APPUSERID);
                intent.putExtra("BUNDLE", args);
                GalleryActivity.this.startActivity(intent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }
}
