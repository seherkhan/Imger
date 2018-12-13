package com.example.imgerapp;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
/**
 * View selected image with it metadata
 * **/
public class ImageActivity extends AppCompatActivity{
    private File image;
    private LinearLayout tv;
    private TextView downloadTV;
    private String APPUSERID;
    private String filename;
    private String dir;
    private HashSet<String> updatedtags=null;
    private String downloadLink;
    private DatabaseReference tag_index;
    private DatabaseReference this_image;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        APPUSERID = args.getString("APPUSERID");
        image = (File) args.getSerializable("IMAGE");
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageURI(Uri.fromFile(image));
        filename=image.getName().substring(0,image.getName().indexOf('.'));
        dir=image.getAbsolutePath();

        tag_index = FirebaseDatabase.getInstance().getReference().child("users/"+APPUSERID+"/tag_index");
        this_image = FirebaseDatabase.getInstance().getReference().child("users/"+APPUSERID+"/pictures/"+filename);

        storage = FirebaseStorage.getInstance("gs://imgerapp-f4810.appspot.com");

        tv = findViewById(R.id.metadata_list);
        downloadTV = findViewById(R.id.download);
        getDataInTextFieldsFromFirebase();
    }

    public void initTagDialog(View v){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.editTag);
        final EditText input = new EditText(ImageActivity.this);
        final String[] original_tags = new String[1];
        // when dialog opens, get value of tag and set it in text box
        this_image.getRef().child("tag").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                original_tags[0]=ds.getValue(String.class).substring(1,ds.getValue(String.class).length()-1);
                input.setText(ds.getValue(String.class).substring(1,ds.getValue(String.class).length()-1).replace(", ",","));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);
        // when save clicked
        alertDialogBuilder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // get current value of textfield
                        String updated_tags = input.getText().toString().replaceAll("[.#$\\[\\]]","").trim().toLowerCase();
                        // find org-new = tags removed
                        // find new-org = tags added
                        Set<String> originaltags = new HashSet<>(Arrays.asList(original_tags[0].split("\\s*,\\s*"))); //space with comma because string rep of set
                        updatedtags = new HashSet<>(Arrays.asList(updated_tags.split("\\s*,\\s*")));
                        Set<String> removetags = new HashSet<>(Arrays.asList(original_tags[0].split("\\s*,\\s*"))); //space with comma because string rep of set
                        Set<String> addtags = new HashSet<>(Arrays.asList(updated_tags.split("\\s*,\\s*")));
                        removetags.removeAll(updatedtags);
                        addtags.removeAll(originaltags);
                        if(updated_tags.length()==0){

                            downloadTV.setText(R.string.noLinkAvaliable);
                            downloadTV.setOnClickListener(null);
                            removeImage();
                        }
                        else if(removetags.size()!=0 || addtags.size()!=0){
                            downloadTV.setText("uploading...");
                            uploadImage(updatedtags);
                        }
                        for(String removetag : removetags){
                            tag_index.child(removetag).addListenerForSingleValueEvent(new ValueEventListener(){
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot fileInTag : dataSnapshot.getChildren()){
                                        if(fileInTag.getValue().equals(dir)){
                                            fileInTag.getRef().removeValue();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                        for(String addtag : addtags){
                            DatabaseReference newFileInTag = tag_index.child(addtag).push();
                            newFileInTag.setValue(dir);
                        }


                        ((TextView)tv.getChildAt(23)).setText(updatedtags.toString());
                        this_image.child("tag").setValue(updatedtags.toString());
                        Toast.makeText(ImageActivity.this,"Changes saved",Toast.LENGTH_LONG).show();
                    }
                });

        alertDialogBuilder.setNegativeButton("Back",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void getDataInTextFieldsFromFirebase(){
        this_image.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                ((TextView)tv.getChildAt(1)).setText(filename);
                ((TextView)tv.getChildAt(3)).setText(ds.child("dir").getValue(String.class));
                ((TextView)tv.getChildAt(5)).setText(ds.child("type").getValue(String.class));
                if(ds.child("filesize").exists()){
                    ((TextView)tv.getChildAt(7)).setText(ds.child("filesize").getValue(Float.class)+" MB");
                }
                if(ds.child("l").exists() && ds.child("w").exists()){
                    ((TextView)tv.getChildAt(11)).setText(ds.child("w").getValue(Float.class) +" x "+ds.child("l").getValue(Float.class));
                }

                if(ds.child("iso").exists()){
                    ((TextView)tv.getChildAt(19)).setText(ds.child("iso").getValue(Float.class)+"");
                }
                if(ds.child("focal").exists()){
                    ((TextView)tv.getChildAt(13)).setText(ds.child("focal").getValue(Float.class) +" mm");
                }
                if(ds.child("s").exists()){
                    ((TextView)tv.getChildAt(15)).setText(ds.child("s").getValue(Float.class)+"");
                }
                if(ds.child("f").exists()){
                    ((TextView)tv.getChildAt(17)).setText(ds.child("f").getValue(Float.class)+"");
                }
                if(ds.child("gps_latN").exists() && ds.child("gps_longE").exists()) {
                    ((TextView)tv.getChildAt(21)).setText(ds.child("gps_latN").getValue(Float.class)+" N, "+ds.child("gps_longE").getValue(Float.class)+" E");
                }
                if(ds.child("dateString").exists()){
                    ((TextView)tv.getChildAt(9)).setText(ds.child("dateString").getValue(String.class));
                }
                if(ds.child("tag").exists()){
                    ((TextView)tv.getChildAt(23)).setText(ds.child("tag").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        Uri file = Uri.fromFile(image);
        storage.getReference().child(APPUSERID+"/"+file.getLastPathSegment()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                downloadTV.setText(uri.toString());
                downloadTV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(ClipData.newPlainText("url",downloadTV.getText()));
                        Toast.makeText(ImageActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                downloadTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dl();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                downloadTV.setText(R.string.noLinkAvaliable);
                downloadTV.setOnClickListener(null);
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void uploadImage(Set updatedtags){
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://imgerapp-f4810.appspot.com");
        StorageReference storageRef = storage.getReference();
        Uri file = Uri.fromFile(image);
        final StorageReference imageRef = storageRef.child(APPUSERID+"/"+file.getLastPathSegment());

        //update image

        file = Uri.fromFile(setExifTag(image));

        UploadTask uploadTask = imageRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ImageActivity.this,"Image not uploaded",Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ImageActivity.this,"Image uploaded",Toast.LENGTH_LONG).show();
            }});

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ImageActivity.this,"Image upload successful",Toast.LENGTH_LONG).show();
                    final Uri uri = task.getResult();
                    downloadTV.setText(uri.toString());
                    downloadTV.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            cm.setPrimaryClip(ClipData.newPlainText("url",downloadTV.getText()));
                            Toast.makeText(ImageActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    downloadTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dl();
                        }
                    });

                } else {
                    Toast.makeText(ImageActivity.this,"Image upload unsuccessful",Toast.LENGTH_LONG).show();
                    downloadTV.setOnClickListener(null);
                }
            }
        });
    }
    public void removeImage(){
        Uri file = Uri.fromFile(image);
        final StorageReference imageRef = storage.getReference().child(APPUSERID+"/"+file.getLastPathSegment());

        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ImageActivity.this,"Image deletion successful",Toast.LENGTH_LONG).show();
                tv.setOnClickListener(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ImageActivity.this,"Image does not exist",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void dl(){
        StorageReference imageRef = storage.getReference().child(APPUSERID+"/"+image.getName());

        File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),image.getName());
        //localFile = File.createTempFile("images", "jpg");

        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ImageActivity.this,"Image downloading...",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ImageActivity.this,"Download failed.",Toast.LENGTH_LONG).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                Toast.makeText(ImageActivity.this,"Image downloaded.",Toast.LENGTH_LONG).show();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public File setExifTag(final File jpegImageFile) {
        File updatedFile = null;
        try {
            updatedFile = File.createTempFile("tmpFile",image.getName().substring(image.getName().indexOf('.')+1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(updatedFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JpegImageMetadata jpegMetadata=null;
        try {
            jpegMetadata = (JpegImageMetadata) Imaging.getMetadata(jpegImageFile);
        } catch (ImageReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TiffOutputSet tiffOutputSet=null;
        if (jpegMetadata!=null) {
            final TiffImageMetadata exif = jpegMetadata.getExif();
            if (exif!=null) { // image has some exif data
                try {
                    tiffOutputSet = exif.getOutputSet();
                } catch (ImageWriteException e) {
                    e.printStackTrace();
                }
            }
            else{
                tiffOutputSet = new TiffOutputSet();
            }
        }
        try {
            tiffOutputSet.getOrCreateExifDirectory().removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
        } catch (ImageWriteException e) {
            e.printStackTrace();
        }
        try {
            if(updatedtags!=null){ tiffOutputSet.getOrCreateExifDirectory().add(ExifTagConstants.EXIF_TAG_USER_COMMENT,updatedtags.toString());
            }
        } catch (ImageWriteException e) {
            e.printStackTrace();
        }
        try {
            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, tiffOutputSet);
        } catch (ImageReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageWriteException e) {
            e.printStackTrace();
        }
        return updatedFile;
    }
}


