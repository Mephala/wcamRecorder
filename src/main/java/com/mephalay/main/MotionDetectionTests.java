package com.mephalay.main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
            String testFolder = "D:\\wcam\\1453019576484";
            testMotion(testFolder);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void testMotion(String testFolder) throws InterruptedException {
        long start = System.currentTimeMillis();
        BigDecimal motionRate = calculateMotionRateInsideFolder(testFolder);
        System.out.println("Calculated in " + (System.currentTimeMillis() - start) + " ms. Motion rate is:" + motionRate);

    }


    private static BigDecimal calculateMotionRateInsideFolder(final String folderPath) throws InterruptedException {
        final AtomicInteger movementDetectionRate = new AtomicInteger(0);
        ExecutorService movementDetectionWorkers = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 29; i++) {
            int startImgNum = i * 5;
            final String img1 = "img" + startImgNum + ".jpg";
            startImgNum += 5;
            final String img2 = "img" + startImgNum + ".jpg";
            movementDetectionWorkers.submit(new Runnable() {
                public void run() {
                    detectMovementRate(img1, img2, folderPath, movementDetectionRate);
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

    private static void detectMovementRate(String img1, String img2, String folderPath, AtomicInteger movementDetectionRate) {
        Long time = Long.parseLong(new File(folderPath).getName());
        int[][][] img1pixels = getImagePixels(folderPath + File.separator + img1);
        int[][][] img1pixelMeans = createPixelMeans(img1pixels, time);
        int[][][] img2pixels = getImagePixels(folderPath + File.separator + img2);
        int[][][] img2pixelMeans = createPixelMeans(img2pixels, time);
        boolean motionDetected = motionDetected(img1pixelMeans, img2pixelMeans, time);
        if (motionDetected)
            movementDetectionRate.getAndIncrement();
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
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int[] rgb = getPixelData(bi, x, y);
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

    private static int[][][] createPixelMeans(int[][][] imgPixels, Long time) {
        int boxSize = getBoxSize(time);
        int xlen = 1920 / boxSize;
        int ylen = 1080 / boxSize;
        int[][][] pixelMeans = new int[xlen][ylen][3];
        for (int x = 0; x < xlen; x++) {
            for (int y = 0; y < ylen; y++) {
                int istart = x * boxSize;
                int jstart = y * boxSize;
                int rtop = 0;
                int gtop = 0;
                int btop = 0;
                for (int i = istart; i < istart + boxSize; i++) {
                    for (int j = jstart; j < jstart + boxSize; j++) {
                        try {
                            rtop += imgPixels[i][j][0];
                            gtop += imgPixels[i][j][1];
                            btop += imgPixels[i][j][2];
                        } catch (Throwable t) {
                            t.printStackTrace();
                            System.err.println(i + " ," + j + "," + istart + "," + jstart);
                            System.exit(-1);
                        }

                    }
                }
                int rmean = rtop / (boxSize * boxSize);
                int gmean = gtop / (boxSize * boxSize);
                int bmean = btop / (boxSize * boxSize);
                pixelMeans[x][y][0] = rmean;
                pixelMeans[x][y][1] = gmean;
                pixelMeans[x][y][2] = bmean;
            }
        }
        return pixelMeans;
    }
}
