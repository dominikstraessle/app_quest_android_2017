package ch.appquest.pixelmaler;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by stra5 on 07.12.2017.
 */

public class PixelRaster {

    private ArrayList<Pixel> pixelList = new ArrayList<>();

    private float pixelHeight;
    private float pixelWidth;

    private boolean isErasing = false;


    public ArrayList<Pixel> getPixelList() {
        return pixelList;
    }

    public PixelRaster() {
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                pixelList.add(new Pixel(i, j, Color.WHITE));
            }
        }
    }

    public void addPixel(float x, float y, int color) {
        Pixel pixel = new Pixel((int) Math.floor(x / pixelWidth), (int) Math.floor(y / pixelHeight), color);
//        boolean contains = pixelList.contains(pixel);
        if (isErasing) {
            pixelList.remove(pixel);
            pixel.setColor(Color.WHITE);
            pixelList.add(pixel);
        } else {
            pixelList.remove(pixel);
            pixelList.add(pixel);
        }
    }

    public boolean isErasing() {
        return isErasing;
    }

    public void setErasing(boolean erasing) {
        isErasing = erasing;
    }

    public float getPixelHeight() {
        return pixelHeight;
    }

    public float getPixelWidth() {
        return pixelWidth;
    }

    public void setPixelHeight(float pixelHeight) {
        this.pixelHeight = pixelHeight;
    }

    public void setPixelWidth(float pixelWidth) {
        this.pixelWidth = pixelWidth;
    }

    public void setNew() {
        pixelList.clear();
    }
}
