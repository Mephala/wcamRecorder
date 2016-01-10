package com.mephalay.main;

import com.github.sarxos.webcam.Webcam;
import org.jcodec.api.awt.SequenceEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mephalay on 1/7/2016.
 */
public class Main {


    public static void main(String[] args) {

        try {

            Webcam webcam = Webcam.getDefault();
            Dimension dimension = new Dimension();
            dimension.setSize(1980, 1080);
            webcam.getDevice().setResolution(dimension);
            webcam.open();
            ExecutorService es = Executors.newCachedThreadPool();
            Long movieName = System.currentTimeMillis();
            final List<String> list = new ArrayList<String>();
            for (int i = 0; i < 75000; i++) {
                final int tmp =i;
                final long start = System.currentTimeMillis();
                final BufferedImage image = webcam.getImage();
                es.submit(new Runnable() {
                    public void run() {
                        String filePath = "C:\\Users\\masraf\\Desktop\\Programming\\wcam\\img"+tmp+".jpg";
                        try {
                            ImageIO.write(image, "JPG", new File(filePath));
                            list.add(filePath);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
                long differ = System.currentTimeMillis()-start;
                if (differ < 40)
                    Thread.sleep(40 - differ);
            }
            es.shutdown();
            es.awaitTermination(999999L, TimeUnit.HOURS);
            System.out.println("DONE!!!");
//            Collections.sort(list, new Comparator<String>() {
//                public int compare(String o1, String o2) {
//                    int sepIndex1 = o1.lastIndexOf("\\");
//                    int sepIndex2 = o2.lastIndexOf("\\");
//                    String num1 = o1.substring(sepIndex1+1).replace(".jpg","");
//                    String num2 = o2.substring(sepIndex2+1).replace(".jpg","");
//                    Long num1Long = Long.parseLong(num1);
//                    Long num2Long = Long.parseLong(num1);
//                    return num1Long.compareTo(num2Long);
//                }
//            });
//            SequenceEncoder enc = new SequenceEncoder(new File("C:\\Users\\masraf\\Desktop\\Programming\\wcam\\" + movieName+".mp4"));
//
//            for (String s : list) {
//                BufferedImage bufferedImage = ImageIO.read(new File(s));
//                enc.encodeImage(bufferedImage);
//            }
//            enc.finish();
//            SequenceEncoder enc2 = new SequenceEncoder(new File("C:\\Users\\masraf\\Desktop\\Programming\\wcam\\" + movieName+".avi"));
//
//            for (String s : list) {
//                BufferedImage bufferedImage = ImageIO.read(new File(s));
//                enc2.encodeImage(bufferedImage);
//            }
//            enc2.finish();
//            SequenceEncoder enc3 = new SequenceEncoder(new File("C:\\Users\\masraf\\Desktop\\Programming\\wcam\\" + movieName+".ogg"));
//
//            for (String s : list) {
//                BufferedImage bufferedImage = ImageIO.read(new File(s));
//                enc3.encodeImage(bufferedImage);
//            }
//            enc3.finish();
//            SequenceEncoder enc4 = new SequenceEncoder(new File("C:\\Users\\masraf\\Desktop\\Programming\\wcam\\" + movieName+".flv"));
//
//            for (String s : list) {
//                BufferedImage bufferedImage = ImageIO.read(new File(s));
//                enc4.encodeImage(bufferedImage);
//            }
//            enc4.finish();


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
