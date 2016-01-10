package com.mephalay.main;

import com.github.sarxos.webcam.Webcam;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mephalay on 1/10/2016.
 */
public class XugglerMain {

    private static Dimension screenBounds;
    public static int indexVideo = 1;
    private static final double FRAME_RATE = 50;

    private static final int SECONDS_TO_RUN_FOR = 20;
    private static final String OUTPUT_FILE = "C:/video/today.mp4";

    public static void main(String[] arguments) {
        final IMediaWriter writer = ToolFactory.makeWriter(OUTPUT_FILE);
        screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4,
                screenBounds.width / 2, screenBounds.height / 2);
        long startTime = System.nanoTime();

        Webcam webcam = Webcam.getDefault();
        Dimension dimension = new Dimension();
        dimension.setSize(1280, 720);
        webcam.getDevice().setResolution(dimension);
        webcam.open();

        for (int index = 0; index < 15; index++) {

            BufferedImage bgrScreen = webcam.getImage();
            System.out.println("time stamp = "+ (System.nanoTime() - startTime));
            bgrScreen = convertToType(bgrScreen, BufferedImage.TYPE_3BYTE_BGR);
            // encode the image to stream #0
            //writer.encodeVideo(0, bgrScreen, (System.nanoTime() - startTime)/2,TimeUnit.NANOSECONDS);
            // encode the image to stream #0
            writer.encodeVideo(0, bgrScreen, System.nanoTime() - startTime,
                    TimeUnit.NANOSECONDS);
            // sleep for frame rate milliseconds
            try {
                Thread.sleep((long) (100));
            } catch (InterruptedException e) {
                // ignore
            }
        }
        writer.close();
    }

    private static BufferedImage getVideoImage() {

        File imgLoc = new File("C:/images/" + indexVideo + ".png");
        BufferedImage img = null;
        try {
            img = ImageIO.read(imgLoc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(imgLoc.getName());
        indexVideo++;
        return img;
    }

    public static BufferedImage convertToType(BufferedImage sourceImage,
                                              int targetType) {

        BufferedImage image;

        // if the source image is already the target type, return the source
        // image
        if (sourceImage.getType() == targetType) {
            image = sourceImage;
        }
        // otherwise create a new image of the target type and draw the new
        // image
        else {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;

    }
}
