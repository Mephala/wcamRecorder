package com.mephalay.main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mephalay on 1/19/2016.
 */
public class MotionDetectionTests {

    private static BigDecimal bd = new BigDecimal(0);


    private static int getBoxSize(Long time) {
        int boxSize = 20;
        Date calculationTime = new Date(time);
        Calendar c = Calendar.getInstance();
        c.setTime(calculationTime);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date morning = c.getTime();
        c.set(Calendar.HOUR_OF_DAY, 17);
        Date night = c.getTime();
        if (calculationTime.after(night) || calculationTime.before(morning)) {
            boxSize = 120;
        }
        return boxSize;
    }


    public static void main(String[] args) {
        try {
            String testFolder = "C:\\Users\\N56834\\Desktop\\Development\\wcam\\1453020690785";
            testMotion(testFolder);
            System.out.println(bd.toPlainString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void testMotion(String testFolder) throws InterruptedException {
        long start = System.currentTimeMillis();
        BigDecimal motionRate = calculateMotionRateInsideFolder(testFolder, getBoxSize(Long.parseLong(new File(testFolder).getName())));
        System.out.println("Calculated in " + (System.currentTimeMillis() - start) + " ms. Motion rate is:" + motionRate);

    }


    private static BigDecimal calculateMotionRateInsideFolder(final String folderPath, final int boxSize) throws InterruptedException {
        final AtomicInteger movementDetectionRate = new AtomicInteger(0);
        ExecutorService movementDetectionWorkers = Executors.newFixedThreadPool(1);
        for (int i = 0; i < 29; i++) {
            int startImgNum = i * 5;
            final String img1 = "img" + startImgNum + ".jpg";
            startImgNum += 5;
            final String img2 = "img" + startImgNum + ".jpg";
            movementDetectionWorkers.submit(new Runnable() {
                public void run() {
                    try {
                        detectMovementRate(img1, img2, folderPath, movementDetectionRate, boxSize);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });

        }
        movementDetectionWorkers.shutdown();
        movementDetectionWorkers.awaitTermination(99999L, TimeUnit.HOURS);
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal rate = hundred.multiply(new BigDecimal(movementDetectionRate.get())).divide(new BigDecimal(29), 2, BigDecimal.ROUND_HALF_UP);
        System.out.println("Calculated motion rate of folder:" + folderPath + " =====>" + rate + ", which is favored by #threads:" + movementDetectionRate.get());
        return rate;
    }

    private static void detectMovementRate(String img1, String img2, String folderPath, AtomicInteger movementDetectionRate, final int boxSize) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        final Long time = Long.parseLong(new File(folderPath).getName());
        final String img1Path = folderPath + File.separator + img1;
        final String img2Path = folderPath + File.separator + img2;
        int xlen = 1920 / boxSize;
        int ylen = 1080 / boxSize;
        final int[][][] img1pixelMeans = new int[xlen][ylen][3];
        final int[][][] img2pixelMeans = new int[xlen][ylen][3];
        Thread img1MeanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int[][][] img1pixels = getImagePixels(img1Path);
                    fillPixelMeans(img1pixels, time, img1pixelMeans, boxSize);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }
        });
        Thread img2MeanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int[][][] img2pixels = getImagePixels(img2Path);
                    fillPixelMeans(img2pixels, time, img2pixelMeans, boxSize);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }
        });
        img1MeanThread.start();
        img2MeanThread.start();
        img1MeanThread.join();
        img2MeanThread.join();
        boolean motionDetected = motionDetected(img1pixelMeans, img2pixelMeans, time);
        if (motionDetected)
            movementDetectionRate.getAndIncrement();
        System.out.println("Calculated single diff in:" + (System.currentTimeMillis() - start) + " ms.");
    }

    private static int[] getPixelData(BufferedImage img, int x, int y) {
        int argb = img.getRGB(x, y);
        int rgb[] = new int[]{
                (argb >> 16) & 0xff, //red
                (argb >> 8) & 0xff, //green
                (argb) & 0xff  //blue
        };
        return rgb;
    }

    private static int[][][] getImagePixels(String filePath) {
        int[][][] imagePixels = new int[1920][1080][3];
        try {

            BufferedImage bi = ImageIO.read(new File(filePath));
            int w = bi.getWidth();
            int h = bi.getHeight();
            for (int y = 0; y < h; y = y + 2) {
                for (int x = 0; x < w; x = x + 2) {
                    long nan = System.nanoTime();
                    int[] rgb = getPixelData(bi, x, y);
                    bd = bd.add(new BigDecimal(System.nanoTime() - nan));
                    imagePixels[x][y] = rgb;
                }
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }
        return imagePixels;
    }

    private static boolean motionDetected(int[][][] img1pixelMeans, int[][][] img2pixelMeans, Long time) {
        int boxSize = getBoxSize(time);
        int xlen = 1920 / boxSize;
        int ylen = 1080 / boxSize;
        int redTolerance = 5;
        int greenTolerance = 5;
        int blueTolerance = 5;
        boolean motionDetected = false;
        for (int x = 0; x < xlen; x++) {
            for (int y = 0; y < ylen; y++) {
                int r1 = img1pixelMeans[x][y][0];
                int r2 = img2pixelMeans[x][y][0];
                int g1 = img1pixelMeans[x][y][1];
                int g2 = img2pixelMeans[x][y][1];
                int b1 = img1pixelMeans[x][y][2];
                int b2 = img2pixelMeans[x][y][2];
                boolean redDiffer = Math.abs(r1 - r2) > redTolerance;
                boolean greenDiffer = Math.abs(r1 - r2) > greenTolerance;
                boolean blueDiffer = Math.abs(r1 - r2) > blueTolerance;
                if (redDiffer && greenDiffer && blueDiffer)
                    motionDetected = true;
            }
        }
        return motionDetected;
    }

    private static void fillPixelMeans(int[][][] imgPixels, Long time, int[][][] pixelMeans, int boxSize) throws IOException {

        for (int x = 0; x < 1920; x += boxSize) {
            for (int y = 0; y < 1080; y += boxSize) {
                int rtotal = 0;
                int gtotal = 0;
                int btotal = 0;
                for (int i = 0; i < boxSize; i++) {
                    for (int j = 0; j < boxSize; j++) {
                        rtotal += imgPixels[x + i][y + j][0];
                        gtotal += imgPixels[x + i][y + j][1];
                        btotal += imgPixels[x + i][y + j][2];
                    }
                }
                pixelMeans[x / boxSize][y / boxSize][0] = rtotal / (boxSize * boxSize);
                pixelMeans[x / boxSize][y / boxSize][1] = gtotal / (boxSize * boxSize);
                pixelMeans[x / boxSize][y / boxSize][2] = btotal / (boxSize * boxSize);
            }
        }
    }
//    private static int[][][] fillPixelMeans(int[][][] imgPixels, Long time) {
//        int boxSize = getBoxSize(time);
//        int xlen = 1920 / boxSize;
//        int ylen = 1080 / boxSize;
//        int[][][] pixelMeans = new int[xlen][ylen][3];
//        for (int x = 0; x < xlen; x=x+boxSize) {
//            for (int y = 0; y < ylen; y=y+boxSize) {
//                int istart = x * boxSize;
//                int jstart = y * boxSize;
//                int rtop = 0;
//                int gtop = 0;
//                int btop = 0;
//                for (int i = istart; i < istart + boxSize; i++) {
//                    for (int j = jstart; j < jstart + boxSize; j++) {
//                        try {
//                            rtop += imgPixels[i][j][0];
//                            gtop += imgPixels[i][j][1];
//                            btop += imgPixels[i][j][2];
//                        } catch (Throwable t) {
//                            t.printStackTrace();
//                            System.err.println(i + " ," + j + "," + istart + "," + jstart);
//                            System.exit(-1);
//                        }
//                    }
//                }
//                int rmean = rtop / (boxSize * boxSize);
//                int gmean = gtop / (boxSize * boxSize);
//                int bmean = btop / (boxSize * boxSize);
//                pixelMeans[x][y][0] = rmean;
//                pixelMeans[x][y][1] = gmean;
//                pixelMeans[x][y][2] = bmean;
//            }
//        }
//        return pixelMeans;
//    }
}
