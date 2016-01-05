package models;

/**
 * Created by saeed on 12/29/2015.
 */
public class Article {
    String title;
    String url;
    long id;
    String abstraction;

    public Article(String title, String url, long id, String abstraction) {
        this.title = title;
        this.url = url;
        this.id = id;
        this.abstraction = abstraction;
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "ID:" + id + "\nTITLE:" + title + "\n\tURL:" + url + "\n\nABSTRACTION:" + abstraction;
    }

    public String getTitle() {
        return title;
    }
}
