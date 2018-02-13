package ch.gibmit.straessle.dominik.memory_3;

/**
 * Created by dominik on 02.11.17.
 */

public class Card {

    private String text = "";
    private String imagePath = "";

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
