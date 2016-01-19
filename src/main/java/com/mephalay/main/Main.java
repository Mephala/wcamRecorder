package com.mephalay.main;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static BigDecimal calculateMotionRateInsideFolder(final String folderPath) throws InterruptedException {
        final AtomicInteger movementDetectionRate = new AtomicInteger(0);
        ExecutorService movementDetectionWorkers = Executors.newFixedThreadPool(1);
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
        String img1Path = folderPath + File.separator + img1;
        File img1File = new File(img1Path);
        String img2Path = folderPath + File.separator + img2;
        File img2File = new File(img2Path);
        if (!img1File.exists() || !img2File.exists()) {
            System.out.println("FPS is going to be lower than expected for folder:" + folderPath);
            return;
        }
        int[][][] img1pixels = getImagePixels(img1Path);
        int[][][] img1pixelMeans = createPixelMeans(img1pixels, time);
        int[][][] img2pixels = getImagePixels(img2Path);
        int[][][] img2pixelMeans = createPixelMeans(img2pixels, time);
        boolean motionDetected = motionDetected(img1pixelMeans, img2pixelMeans, time);
        if (motionDetected)
            movementDetectionRate.getAndIncrement();
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


    public static void recordSample() {
        try {
            Webcam webcam = Webcam.getDefault();
            Dimension dimension = new Dimension();
            dimension.setSize(1980, 1080);
            webcam.getDevice().setResolution(dimension);
            webcam.open();
            final int HOURS_TO_RECORD = 22;
            final int RENDERING_THREADS = 1;
            final int loopLimitBasedOnRecordHour = calculateLoopLimit(HOURS_TO_RECORD);
            final ExecutorService renderingService = Executors.newFixedThreadPool(RENDERING_THREADS);
            for (int k = 0; k < loopLimitBasedOnRecordHour; k++) {
                final ExecutorService webcamImageCreatorService = Executors.newCachedThreadPool();
                final File recordFolder = new File("D:\\wcam\\" + System.currentTimeMillis());
                recordFolder.mkdir();
                final String recordFolderPath = recordFolder.getAbsolutePath();
                System.out.println("Capturing images for folder:" + recordFolderPath);
                final List<String> list = new ArrayList<String>();
                long capStart = System.currentTimeMillis();
                int i = 0;
                while ((System.currentTimeMillis() - capStart) < 30000) {
                    final int tmp = i;
                    final long start = System.currentTimeMillis();
                    final BufferedImage image = webcam.getImage();
                    final Date captureTime = new Date(start);
                    webcamImageCreatorService.submit(new Runnable() {
                        public void run() {
                            String filePath = recordFolderPath + File.separator + "img" + tmp + ".jpg";
                            try {
                                addTimeStamp(image, captureTime);
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
                System.out.println("Done capturing images for folder:" + recordFolderPath + ". Now creating video rendering and motion detection service thread and resuming capturing...");
                renderingService.submit(new Runnable() {
                    public void run() {
                        try {
                            long start = System.currentTimeMillis();
                            webcamImageCreatorService.shutdown();
                            webcamImageCreatorService.awaitTermination(999999L, TimeUnit.HOURS);

                            Thread motionDetection = new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        //Detecting motion rate
                                        long start = System.currentTimeMillis();
                                        BigDecimal movementRate = calculateMotionRateInsideFolder(recordFolderPath);
                                        boolean createMRFile = new File(recordFolderPath + File.separator + movementRate.toPlainString() + ".mr").createNewFile();
                                        if (!createMRFile) {
                                            //TODO Log something if it fails...
                                        }
                                        System.out.println("Motion detection calculated in " + (System.currentTimeMillis() - start) + " ms.");
                                    } catch (Throwable t) {
                                        System.out.println("Failed to detect movement.");
                                    }
                                }
                            });
                            motionDetection.start();
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
                            System.err.println("Waiting motion detection process...");
                            motionDetection.join();
                            System.out.println("Deleting images to save up space...");
                            File[] filesUnderRecordFolder = recordFolder.listFiles();
                            for (File file : filesUnderRecordFolder) {
                                if (file.getName().contains(".jpg"))
                                    file.delete();
                            }
                            System.out.println("Finished rendering process for this iteration. Duration:" + (System.currentTimeMillis() - start));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
            }
            renderingService.shutdown();
            renderingService.awaitTermination(99999999L, TimeUnit.HOURS);
            System.out.println("DONE");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static int calculateLoopLimit(int hours_to_record) {
        return hours_to_record * 60 * 2;
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

    private static void addTimeStamp(BufferedImage old, Date captureTime) {
        int w = old.getWidth();
        int h = old.getHeight();
        Graphics2D g2d = old.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, 20));
        String s = captureTime.toString();
        FontMetrics fm = g2d.getFontMetrics();
        int x = old.getWidth() - fm.stringWidth(s) - 5;
        int y = fm.getHeight();
        g2d.drawString(s, x, y);
        g2d.dispose();
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
            System.err.println("Failed for filePath:" + filePath);
            t.printStackTrace();
        }
        return imagePixels;
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


}
