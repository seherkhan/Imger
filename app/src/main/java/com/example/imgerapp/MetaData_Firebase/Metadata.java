package com.example.imgerapp.MetaData_Firebase;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
/**
 * Extracts metadata of a given image (file object)
 * **/
public class Metadata {
    public String filename;
    public String type;
    public String dir;
    public Double filesize; // in MBs
    public ArrayList<Integer> datetimefields=new ArrayList<>();
    public Float date;
    public Integer w;
    public Integer l;
    public Integer focal;
    public Float s;
    public Float f;
    public Integer iso;
    public Double gps_latN;
    public Double gps_longE;
    public String tag;

    public Metadata(File file) throws IOException, ImageReadException {
        ImageInfo image=null;
        TiffImageMetadata tf=null;

        filename= file.getName();
        type= FilenameUtils.getExtension(filename);
        dir=file.getAbsolutePath();
        filesize = file.length()/(1024.0*1024.0); // in MB

        try{
            image = Imaging.getImageInfo(file);
        }
        catch(NullPointerException e){
        }
        if(image!=null){
            l = image.getHeight(); //height
            w = image.getWidth(); //width
        }

        try{
            tf = ((JpegImageMetadata) Imaging.getMetadata(file)).getExif();
        }
        catch(NullPointerException e){
        }
        if(tf!=null){
            try {
                String datetime = tf.getFieldValue(TiffTagConstants.TIFF_TAG_DATE_TIME)[0];
                String[] tmp_dtstr = datetime.split("[ :]");
                if (tmp_dtstr.length == 6) {
                    for (int i = 0; i < 6; i++) {
                        datetimefields.add(Integer.parseInt(tmp_dtstr[i]));
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                    date = Float.valueOf(sdf.parse(datetime).getTime());
                }
            }catch(NullPointerException e){} catch (ParseException e) {}
            try {
                s=tf.getFieldValue(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME)[0].floatValue();
            }catch(NullPointerException e){}

            try{
                f=tf.getFieldValue(ExifTagConstants.EXIF_TAG_FNUMBER)[0].floatValue();
            }catch (NullPointerException e){}

            try {
                iso = new Integer(tf.getFieldValue(ExifTagConstants.EXIF_TAG_ISO)[0]);
            }catch(NullPointerException e){}

            try{
                focal = new Integer(tf.getFieldValue(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT)[0]);
            }
            catch(NullPointerException e){}

            if (tf.getGPS() != null) {
                this.gps_latN = tf.getGPS().getLatitudeAsDegreesNorth();
                this.gps_longE = tf.getGPS().getLongitudeAsDegreesEast();
            }
        }
        this.tag="[]";
    }
    /**
    * Prints all fields of the metadata. For testing purposes.
    **/
    public void printAllFields(){
        System.out.println(filename);
        System.out.println(type);
        System.out.println(dir);
        System.out.println(filesize);
        System.out.println(getDateString());
        System.out.println(date);
        System.out.println(w);
        System.out.println(l);
        System.out.println(focal);
        System.out.println(s);
        System.out.println(f);
        System.out.println(iso);
        System.out.println(gps_latN);
        System.out.println(gps_longE);
        System.out.println(tag);
    }
    public String getDateString(){
        return datetimefields.get(0)+"-"+datetimefields.get(1)+"-"+datetimefields.get(2)+" "+
                datetimefields.get(3)+":"+datetimefields.get(4)+":"+datetimefields.get(5);
    }

    public String getName(){
        return filename.substring(0,filename.indexOf('.'));
    }
    
}
