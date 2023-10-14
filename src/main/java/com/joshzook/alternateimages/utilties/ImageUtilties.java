package com.joshzook.alternateimages.utilties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ImageUtilties {

    public static byte[] downloadFileBytes(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096]; // 4KB buffer
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static BufferedImage appendImageSideBySide(List<BufferedImage> imgs) {
        int totalWidth = 0;
        int maxHeight = 0;
        for (BufferedImage img : imgs) {
            totalWidth += img.getWidth();
            maxHeight = Math.max(maxHeight, img.getHeight());
        }

        BufferedImage concatenatedImage = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatenatedImage.createGraphics();
        int x = 0;

        for (BufferedImage image : imgs) {
            g2d.drawImage(image, x, 0, null);
            x += image.getWidth();
        }

        g2d.dispose();
        return concatenatedImage;
    }

    public static BufferedImage scaleImage(BufferedImage image, int displayWidth, int displayHeight) {
        // Calculate the scaling factors for width and height
        double scaleX = (double) displayWidth / image.getWidth();
        double scaleY = (double) displayHeight / image.getHeight();

        // Use the smaller scaling factor to fit the image within the display
        double scale = Math.min(scaleX, scaleY);

        // Calculate the scaled image dimensions
        int scaledWidth = (int) (image.getWidth() * scale);
        int scaledHeight = (int) (image.getHeight() * scale);

        // Create a new image for the scaled result
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();

        // Center the scaled image on the display
        BufferedImage centeredImage = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
        g = centeredImage.createGraphics();

        int x = (displayWidth - scaledWidth) / 2;
        int y = (displayHeight - scaledHeight) / 2;

        g.drawImage(scaledImage, x, y, null);
        g.dispose();
        return centeredImage;
    }

    public static BufferedImage appendVertically(List<BufferedImage> imgs) {
        int maxWidth = imgs.stream().mapToInt(BufferedImage::getWidth).max().orElse(0);
        int totalHeight = imgs.stream().mapToInt(BufferedImage::getHeight).sum();

        BufferedImage appendedImage = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        int y = 0;

        for (BufferedImage image : imgs) {
            appendedImage.getGraphics().drawImage(image, 0, y, null);
            y += image.getHeight();
        }

        return appendedImage;
    }

    public static byte[] getByteArray(BufferedImage img) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to byte array", e);
        }
    }

    public static byte[] resizeImage(byte[] imageBytes, int targetWidth, int targetHeight) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bis);

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
            Graphics2D graphics = resizedImage.createGraphics();
            graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", bos);
            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
