package com.example.imgerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.imgerapp.MetaData_Firebase.PictureDataset;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity to define search parameters
 **/
public class FilterOptionsActivity extends AppCompatActivity {
    private DatabaseReference this_user;
    private String APPUSERID;
    boolean includeNull=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_options);
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        APPUSERID = args.getString("APPUSERID");
        this_user = FirebaseDatabase.getInstance().getReference().child("users/"+args.getString("APPUSERID")+"/pictures");
     }


    public void filter(View v){
        includeNull = ((CheckBox)findViewById(R.id.includeNull)).isChecked();
        this_user.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            HashSet<String> paths = new HashSet<>();
            int counter =0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    System.out.println("IMAGE: "+ds.getKey());
                    String dir = ds.child("dir").getValue(String.class);

                    Float filesize=null;
                    Float l=null;
                    Float w=null;
                    Float iso=null;
                    Float focal=null;
                    Float s=null;
                    Float f=null;
                    Float createdon=null;
                    Float gps_latN=null;
                    Float gps_longE=null;

                    if(ds.child("filesize").exists()){
                        filesize = ds.child("filesize").getValue(Float.class);
                    }
                    if(!lte("filesize",filesize) || !gte("filesize",filesize)){
                        continue;
                    }
                    if(ds.child("l").exists()){
                        l = ds.child("l").getValue(Float.class);
                    }
                    if(!lte("l",l) || !gte("l",l)){
                        continue;
                    }
                    if(ds.child("w").exists()){
                        w = ds.child("w").getValue(Float.class);
                    }
                    if(!lte("w",w) || !gte("w",w)){
                        continue;
                    }
                    if(ds.child("iso").exists()){
                        iso = ds.child("iso").getValue(Float.class);
                    }
                    if(!lte("iso",iso) || !gte("iso",iso)){
                        continue;
                    }
                    if(ds.child("focal").exists()){
                        focal = ds.child("focal").getValue(Float.class);
                    }
                    if(!lte("focal",focal) || !gte("focal",focal)){
                        continue;
                    }
                    if(ds.child("s").exists()){
                        s = ds.child("s").getValue(Float.class);
                    }
                    if(!lte("s",s) || !gte("s",s)){
                        continue;
                    }
                    if(ds.child("f").exists()){
                        f = ds.child("f").getValue(Float.class);
                    }
                    if(!lte("f",f) || !gte("f",f)){
                        continue;
                    }
                    if(ds.child("gps_latN").exists()){
                        gps_latN = ds.child("gps_latN").getValue(Float.class);
                    }
                    if(!lte("gps_latN",gps_latN) || !gte("gps_latN",gps_latN)){
                        continue;
                    }
                    if(ds.child("gps_longE").exists()){
                        gps_longE = ds.child("gps_longE").getValue(Float.class);
                    }
                    if(!lte("gps_longE",gps_longE) || !gte("gps_longE",gps_longE)){
                        continue;
                    }
                    if(ds.child("date").exists()){
                        createdon = ds.child("date").getValue(Float.class);
                    }
                    if(!lte_date(createdon) || !gte_date(createdon)){
                        continue;
                    }
                    String tag = ds.child("tag").getValue(String.class).substring(1,ds.child("tag").getValue(String.class).length()-1);
                    if(!contains(tag)){
                        continue;
                    }
                    paths.add(dir);
                    counter++;
                }

                PictureDataset filtered_pd = new PictureDataset(new ArrayList<>(paths));
                Intent intent = new Intent(FilterOptionsActivity.this, SearchResultsActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("LIST", filtered_pd.getDataset());
                args.putString("APPUSERID",APPUSERID);
                intent.putExtra("BUNDLE", args);
                FilterOptionsActivity.this.startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
    private boolean lte(String paramlabel_min, Float val){
        String param = ((TextView) findViewById(
                getResources().getIdentifier(paramlabel_min+"_min", "id", getPackageName()))
        ).getText().toString();
        if(!TextUtils.isEmpty(param)){
            if(val!=null){
                return Float.valueOf(param)<=val;
            }
            else{
                return includeNull;
            }
        }
        else{
            return true;
        }
    }
    private boolean gte(String paramlabel_max, Float val){
        String param = ((TextView) findViewById(
                getResources().getIdentifier(paramlabel_max+"_max", "id", getPackageName()))
        ).getText().toString();
        if(!TextUtils.isEmpty(param)){
            if(val!=null){
                return Float.valueOf(param)>=val;
            }
            else{
                return includeNull;
            }
        }
        else{
            return true;
        }
    }
    private boolean lte_date(Float val){
        String param = ((TextView) findViewById(R.id.createdon_min)).getText().toString();
        if(!TextUtils.isEmpty(param)){
            if(val!=null){
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                float float_param = 0;
                try {
                    float_param = Float.valueOf(sdf.parse(param).getTime());
                } catch (ParseException e) {
                }
                return float_param<=val;
            }
            else{
                return includeNull;
            }
        }
        else{
            return true;
        }
    }
    private boolean gte_date(Float val){
        String param = ((TextView) findViewById(R.id.createdon_max)).getText().toString();
        if(!TextUtils.isEmpty(param)){
            if(val!=null){
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                float float_param = Calendar.getInstance().getTime().getTime();
                try {
                    float_param = Float.valueOf(sdf.parse(param).getTime());
                } catch (ParseException e) {
                }
                return float_param>=val;
            }
            else{
                return includeNull;
            }
        }
        else{
            return true;
        }
    }
    public boolean contains(String tag) {
        String keywordparam = ((TextView) findViewById(R.id.tag)).getText().toString().trim().toLowerCase();
        if (!TextUtils.isEmpty(keywordparam)) {
            String[] vals_keywords = keywordparam.split("\\s*,\\s*");
            for(int i=0;i<vals_keywords.length;i++){
                vals_keywords[i]=vals_keywords[i].trim();
            }
            if (!TextUtils.isEmpty(tag)) {
                // return true if intersection of tag and keywordparam is not null
                Set<String> set_vals_keywords = new HashSet<>(Arrays.asList(vals_keywords));
                Set<String> tagsOfImage = new HashSet<>(Arrays.asList(tag.split(", ")));
                tagsOfImage.retainAll(set_vals_keywords);
                return tagsOfImage.size() > 0;
            } else {
                // if no tag in picture
                return includeNull;
            }
        } else {
            return true;
        }
    }
}




