package com.example.imgerapp.MetaData_Firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.imaging.ImageReadException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
/**
 * Connects app to Firebase. Contains logic of uploading pictures
 **/
public class FirebaseConnector {
    private String APPUSERID;
    private DatabaseReference mDatabase;
    private ArrayList<File> dataset;
    private ArrayList<Metadata> mdlist;
    public FirebaseConnector(String APPUSERID, ArrayList<File> dataset) throws IOException, ImageReadException {
        this.APPUSERID = APPUSERID;
        this.dataset=dataset;
        this.mdlist = new ArrayList<>();
        for(File file : dataset){         
            mdlist.add(new Metadata(file));
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void uploadImages(){
        final DatabaseReference user_ref= mDatabase.child("users").child(String.valueOf(APPUSERID));
        // Only upload data that has not previously been uploaded
        for (final Metadata img : mdlist){
            user_ref.getRef().child("pictures").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean already_exists = false;
                    for(DataSnapshot pic : dataSnapshot.getChildren()){
                        if(img.getName().equals(pic.getKey())){
                            already_exists=true;
                            break;
                        }
                    }
                    if(!already_exists){
                        // add upload img to firebase
                        user_ref.child("pictures").child(img.getName()).setValue(img);
                        
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        // remove pics from firebase which do not exist in gallery
        user_ref.child("pictures").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!hlpr_contains(mdlist,dataSnapshot.getKey())){
                    final String imgNameToRemove = dataSnapshot.getKey();
                    //remove index data from firebase
                    user_ref.child("tag_index").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            for(DataSnapshot imgInTag : dataSnapshot.getChildren()){
                                if(imgInTag.getValue(String.class).equals(imgNameToRemove)){
                                    imgInTag.getRef().removeValue();
                                }
                            }
                        }
                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        }
                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }
                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    //remove picture from firebase
                    user_ref.child("pictures").child(imgNameToRemove).removeValue();
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public static boolean hlpr_contains(ArrayList<Metadata> mdlist, String key){
        for (Metadata m: mdlist){
            if(key.equals(m.getName())){
                return true;
            }
        }
        return false;
    }
}
