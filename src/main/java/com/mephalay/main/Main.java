package com.mephalay.main;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mephalay on 1/7/2016.
 */
public class Main {


    public static void detectMovement(List<String> fileNames) {
        try {
            List<File> files = new ArrayList<File>();
            for (int i = fileNames.size() - 1000; i < fileNames.size(); i++) {
                files.add(new File(fileNames.get(i)));
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public static void main(String[] args) {

        try {
            recordSample();
//            Webcam webcam = Webcam.getDefault();
//            Dimension dimension = new Dimension();
//            dimension.setSize(1980, 1080);
//            webcam.getDevice().setResolution(dimension);
//            webcam.open();
//            ExecutorService es = Executors.newCachedThreadPool();
//            Long movieName = System.currentTimeMillis();
//            final List<String> list = new ArrayList<String>();
//            long capStart = System.currentTimeMillis();
//            int i = 0;
//            while((System.currentTimeMillis() - capStart) < 300000){
//                final int tmp =i;
//                final long start = System.currentTimeMillis();
//                final BufferedImage image = webcam.getImage();
//                es.submit(new Runnable() {
//                    public void run() {
//                        String filePath = "D:\\wcam\\img"+tmp+".jpg";
//                        try {
//                            ImageIO.write(image, "JPG", new File(filePath));
//                            list.add(filePath);
//                        } catch (Throwable t) {
//                            t.printStackTrace();
//                        }
//                    }
//                });
//                long differ = System.currentTimeMillis()-start;
//                if (differ < 40)
//                    Thread.sleep(40 - differ);
//                i++;
//            }
//            for (int i = 0; i < 50; i++) {
//                final int tmp =i;
//                final long start = System.currentTimeMillis();
//                final BufferedImage image = webcam.getImage();
//                es.submit(new Runnable() {
//                    public void run() {
//                        String filePath = "D:\\wcam\\img"+tmp+".jpg";
//                        try {
//                            ImageIO.write(image, "JPG", new File(filePath));
//                            list.add(filePath);
//                        } catch (Throwable t) {
//                            t.printStackTrace();
//                        }
//                    }
//                });
//                long differ = System.currentTimeMillis()-start;
//                if (differ < 40)
//                    Thread.sleep(40 - differ);
//            }
//            es.shutdown();
//            es.awaitTermination(999999L, TimeUnit.HOURS);
//            System.out.println("DONE!!!");
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


    public static void recordSample() {
        try {
            Webcam webcam = Webcam.getDefault();
            Dimension dimension = new Dimension();
            dimension.setSize(1980, 1080);
            webcam.getDevice().setResolution(dimension);
            webcam.open();
            final ExecutorService videoRenderingService = Executors.newCachedThreadPool();
            for (int k = 0; k < 600; k++) {
                final ExecutorService webcamImageCreatorService = Executors.newCachedThreadPool();
                File recordFolder = new File("D:\\wcam\\" + System.currentTimeMillis());
                recordFolder.mkdir();
                final String recordFolderPath = recordFolder.getAbsolutePath();
                System.out.println("Capturing images for folder:" + recordFolderPath);
                Long movieName = System.currentTimeMillis();
                final List<String> list = new ArrayList<String>();
                long capStart = System.currentTimeMillis();
                int i = 0;
                while ((System.currentTimeMillis() - capStart) < 30000) {
                    final int tmp = i;
                    final long start = System.currentTimeMillis();
                    final BufferedImage image = webcam.getImage();
                    webcamImageCreatorService.submit(new Runnable() {
                        public void run() {
                            String filePath = recordFolderPath + File.separator + "img" + tmp + ".jpg";
                            try {
                                ImageIO.write(image, "JPG", new File(filePath));
                                list.add(filePath);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    });
                    long differ = System.currentTimeMillis() - start;
                    if (differ < 40)
                        Thread.sleep(40 - differ);
                    i++;
                }
                System.out.println("Done capturing images for folder:" + recordFolderPath + ". Now creating video rendering thread and resuming capturing...");
                videoRenderingService.submit(new Runnable() {
                    public void run() {
                        try {
                            webcamImageCreatorService.shutdown();
                            webcamImageCreatorService.awaitTermination(999999L, TimeUnit.HOURS);
                            Runtime rt = Runtime.getRuntime();
                            String ffmpegCommand = "ffmpeg -framerate 45/10 -i " + recordFolderPath + File.separator + "img%d.jpg -c:v libx264 -r 30 -pix_fmt yuv420p " + recordFolderPath + File.separator + "out.mp4";
//            String command = "java -version";
                            String[] command =
                                    {
                                            "cmd",
                                    };
                            Process p = Runtime.getRuntime().exec(command);
                            new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
                            new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
                            PrintWriter stdin = new PrintWriter(p.getOutputStream());
                            stdin.println(ffmpegCommand);
                            // write any other commands you want here
                            stdin.close();
                            int returnCode = p.waitFor();
                            System.out.println("FFMPEG returned " + returnCode);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
            }
            videoRenderingService.shutdown();
            videoRenderingService.awaitTermination(999999L, TimeUnit.HOURS);
            System.out.println("DONE");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    static class SyncPipe implements Runnable {
        public SyncPipe(InputStream istrm, OutputStream ostrm) {
            istrm_ = istrm;
            ostrm_ = ostrm;
        }

        public void run() {
            try {
                final byte[] buffer = new byte[1024];
                for (int length = 0; (length = istrm_.read(buffer)) != -1; ) {
                    ostrm_.write(buffer, 0, length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private final OutputStream ostrm_;
        private final InputStream istrm_;
    }

}
