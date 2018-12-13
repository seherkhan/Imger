package com.example.imgerapp.MetaData_Firebase;

import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * holds list of jpg files of pictures
 * **/
public class PictureDataset {
    public ArrayList<File> dataset;
    private static File dcimPublicDir;

    /**
     * Use this constructor to make dataset using a list of paths
     * Called by FilterOptionsActivity
     **/
    public PictureDataset(ArrayList<String> paths){
        dataset=new ArrayList<>();
        for(String path: paths){
            dataset.add(new File(path));
        }
    }
    /**
     * Use this constructor if you want to create PictureDataset using phone memory
     * (Can be from dummy folder in gallery, TEST = true
     * or from all gallery files, TEST = false
     * )
     * */
    public PictureDataset(boolean TEST){
        dcimPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        dataset = new ArrayList<>();
        if(TEST)
            updateDatasetFromDummyDCIM();
        else
            updateDatasetFromDCIM(); //(not to be used in INF 551 project)

    }

    public int size(){
        return dataset.size();
    }
    /**
     * Assumes ImgerDummy folder in DCIM exists
     * only fetches .jpg files from this folder
     * **/
    private void updateDatasetFromDummyDCIM(){
        File dummyDir = new File(dcimPublicDir,"ImgerDummy");
        File[] picsInDummyDir = getAllFilesInDir(dummyDir,"jpg");
        if(picsInDummyDir!=null){
            for(File pic : picsInDummyDir){
                dataset.add(pic);
            }
        }
    }

    /**
     * Function not used in INF551 project
     * stores images in DCIM and in the folders of DCIM in dataset. (Does not search for images further down
     * **/
    private void updateDatasetFromDCIM(){
        File[] dirsInDCIM = getAllFilesInDir(dcimPublicDir);

        // get all jpgs in DCIM to dataset
        File[] picsInDCIM = getAllFilesInDir(dcimPublicDir,"jpg");

        // add all jpgs in DCIM to dataset
        for(File pic : picsInDCIM){
            dataset.add(pic);
        }

        // add all jpgs in all dirs in DCIM to dataset
        for(File dir : dirsInDCIM){
            File[] picsInDir = getAllFilesInDir(dir,"jpg");
            for(File pic : picsInDir){         
                dataset.add(pic);
            }
        }
    }
    /**
     * returns directories in dir
     * */
    private static File[] getAllFilesInDir(File dir){
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
    }
    /**
     * returns files with extension .ext in dir
     * **/
    private static File[] getAllFilesInDir(File dir, final String ext){
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.toString().endsWith("."+ext.toLowerCase()) || pathname.toString().endsWith("."+ext.toUpperCase());
            }
        });
    }

    public ArrayList<File> getDataset() {
        return dataset;
    }
}
