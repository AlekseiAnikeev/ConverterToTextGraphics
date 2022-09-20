package ru.agentche.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

/**
 * @author Aleksey Anikeev aka AgentChe
 * Date of creation: 11.07.2022
 */
public class ImageConverter implements TextGraphicsConverter {
    private int width;
    private int height;
    private double maxRatio;
    private int newWidth;
    private int newHeight;
    private TextColorSchema schema;

    public ImageConverter() {
        schema = new ColorToChar();
    }

    @Override
    public String convert(String url) throws IOException, BadImageSizeException {
        BufferedImage image = getBufferedImage(url);
        return getConvertImage(image);
    }

    @Override
    public void setMaxWidth(int width) {
        this.width = width;
    }

    @Override
    public void setMaxHeight(int height) {
        this.height = height;
    }

    @Override
    public void setMaxRatio(double maxRatio) {
        this.maxRatio = maxRatio;
    }

    @Override
    public void setTextColorSchema(TextColorSchema schema) {
        this.schema = schema;
    }

    /**
     * Метод для проверки соотношение сторон
     *
     * @param url ссылка на изображение
     * @return возвращаем изображение
     * @throws IOException           может кинуть исключение при отсутствии изображения
     * @throws BadImageSizeException может кинуть исключение о неправильном соотношении сторон
     */
    private BufferedImage getBufferedImage(String url) throws IOException, BadImageSizeException {
        BufferedImage image = ImageIO.read(new URL(url));
        double ratio;
        if (image.getWidth() / image.getHeight() > image.getHeight() / image.getWidth()) {
            ratio = (double) image.getWidth() / image.getHeight();
        } else {
            ratio = (double) image.getHeight() / image.getWidth();
        }
        if (maxRatio != 0 && ratio > maxRatio) {
            throw new BadImageSizeException(ratio, maxRatio);
        }
        return image;
    }

    /**
     * Метод конвертации изображения в символьную графику
     *
     * @param image оригинал изображение
     * @return возврат изображения состоящего из символов
     */
    private String getConvertImage(BufferedImage image) {
        WritableRaster bwRaster = getWritableRaster(image);
        char[][] pixels = new char[newHeight][newWidth];
        for (int h = 0; h < pixels.length; h++) {
            for (int w = 0; w < pixels[h].length; w++) {
                int color = bwRaster.getPixel(w, h, new int[3])[0];
                pixels[h][w] = schema.convert(color);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (char[] pixel : pixels) {
            for (char c : pixel) {
                sb.append(c);
                sb.append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Метод для масштабирования изображения и получения объекта класса WritableRaster
     * для дальнейшего прогона по пикселям изображения
     *
     * @param image оригинал изображения
     * @return возвращаем черно-белое изображение
     */
    private WritableRaster getWritableRaster(BufferedImage image) {
        if (image.getWidth() > width || image.getHeight() > height) {
            double tmpWidth;
            double tmpHeight;
            if (width != 0) {
                tmpWidth = (double) image.getWidth() / width;
            } else {
                tmpWidth = 1;
            }
            if (height != 0) {
                tmpHeight = (double) image.getHeight() / height;
            } else {
                tmpHeight = 1;
            }
            double divider = Math.max(tmpWidth, tmpHeight);
            newWidth = (int) (image.getWidth() / divider);
            newHeight = (int) (image.getHeight() / divider);
        } else {
            newWidth = image.getWidth();
            newHeight = image.getHeight();
        }
        Image scaleImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage bwImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = bwImage.createGraphics();
        graphics.drawImage(scaleImage, 0, 0, null);

        return bwImage.getRaster();
    }
}
