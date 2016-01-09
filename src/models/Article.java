package models;

import com.google.gson.JsonObject;

/**
 * Created by saeed on 12/29/2015.
 */
public class Article {
    public static String ID_KEY = "id";
    public static String TITLE_KEY = "title";
    public static String URL_KEY = "url";
    public static String ABSTRACTION_KEY = "abstraction";
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

    public long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getAbstraction() {
        return abstraction;
    }

    public JsonObject getJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Article.ID_KEY, id);
        jsonObject.addProperty(Article.TITLE_KEY, title);
        jsonObject.addProperty(Article.URL_KEY, url);
        jsonObject.addProperty(Article.ABSTRACTION_KEY, abstraction);
        return jsonObject;
    }
}
