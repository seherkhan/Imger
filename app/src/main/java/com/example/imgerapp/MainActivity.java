package com.example.imgerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.imgerapp.MetaData_Firebase.FirebaseConnector;
import com.example.imgerapp.MetaData_Firebase.PictureDataset;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import org.apache.commons.imaging.ImageReadException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
/**
 * This is the MainActivity (the entry point of the app)
 * Here, permission is requested to access phone data
 * **/

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String APPUSERID; // deleting and reinstalling the app, will change this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Tag", "signInAnonymously:success");
                            checkPermissions();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Tag", "signInAnonymously:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",Toast.LENGTH_SHORT).show();
                            System.exit(0);
                        }
                    }
                });
    }

    public void checkPermissions() {
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
        else {
            //Toast.makeText(getApplicationContext(), "Permission already granted.", Toast.LENGTH_LONG).show();
            getPhoneData();
        }

    }
    // Permission tag is added in manifest and then also asked during runtime
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoneData();
            } else {
                System.exit(0);
            }
        }
    }


    private void getPhoneData() {
        while(true) {
            APPUSERID = mAuth.getCurrentUser().getUid();
            if(APPUSERID!=null){
                break;
            }
        }

        PictureDataset pd = new PictureDataset(true);
        int i = 0;
        while (pd.dataset != null) { ;
            Intent intent = new Intent(this, GalleryActivity.class);
            Bundle args = new Bundle();
            args.putString("APPUSERID",APPUSERID);
            args.putSerializable("LIST", pd.dataset);
            intent.putExtra("BUNDLE", args);
            startActivity(intent);
            i++;

            try {
                uploadData(pd.dataset);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ImageReadException e) {
                e.printStackTrace();
            }
            if (i == 1) break;
        }

    }

    public void uploadData(ArrayList<File> dataset) throws IOException, ImageReadException {
        FirebaseConnector fc = new FirebaseConnector(APPUSERID,dataset);
        fc.uploadImages();
    }


}
