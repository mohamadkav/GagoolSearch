package models;

/**
 * Created by saeed on 12/29/2015.
 */
public class Article {
    String title;
    String url;
    String BASE_URL = "https://www.researchgate.net/";

    public Article(String title, String url) {
        this.title = title;
        this.url = url;
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "TITLE:" + title + "\n\tURL:" + url;
    }

    public String getAbsoluteUrl() {
        return BASE_URL + url;
    }

    public String getTitle() {
        return title;
    }
}
