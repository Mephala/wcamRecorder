package com.mephalay.main;

import com.github.sarxos.webcam.Webcam;
import com.mephalay.JpegImagesToMovie;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by Mephalay on 1/7/2016.
 */
public class Main {


    public static void main(String[] args){

        try {
            Webcam webcam = Webcam.getDefault();
            webcam.open();
            List<String> list = new ArrayList<String>();
            for (int i = 0; i <250 ; i++) {
                BufferedImage image = webcam.getImage();
                String filePath =  "C:\\Users\\masraf\\Desktop\\Programming\\wcam\\test"+i+".jpg";
                ImageIO.write(image, "JPG", new File(filePath));
                list.add(filePath);
                Thread.sleep(40);
            }
            File directory = new File("C:\\Users\\masraf\\Desktop\\Programming\\wcam");



            Vector<String> imgLst = new Vector<String>();
            for (String imagePath : list) {
                imgLst.add(imagePath);
            }

            JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
            MediaLocator oml;
            String fileName = directory+"\\moneyMoney.mov";
            if ((oml = imageToMovie.createMediaLocator(fileName)) == null) {
                System.err.println("Cannot build media locator from: " + fileName);
                System.exit(0);
            }
            int interval = 50;
            imageToMovie.doIt(576, 432, -1, imgLst, oml);



//            DataOutputStream out = new DataOutputStream(new FileOutputStream(directory.getPath()+"/movie.avi"));
//            for( String n : list ) {
//
//
////			if( i < 338 || i > 800 )
////				continue;
//
//                System.out.println("Reading in: "+n);
//                DataInputStream in = new DataInputStream(new FileInputStream(n));
//
////			while( true ) {
////				byte data[] = filterData(in);
////				if( data == null )
////					break;
////
////				out.write(data);
////			}
//
//                while( in.available() > 0 ) {
//                    byte data[] = new byte[ in.available()];
//                    in.read(data);
//                    out.write(data);
//                }
//
//                in.close();
//            }
//            out.close();
//
        } catch (Throwable t) {
            t.printStackTrace();
        }




    }



}
