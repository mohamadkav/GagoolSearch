package models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by saeed on 12/29/2015.
 */
public class Article {
    public static String ID_KEY = "id";
    public static String TITLE_KEY = "title";
    public static String URL_KEY = "url";
    public static String ABSTRACTION_KEY = "abstraction";

    int id;
    String title;
    String url;
    String abstraction;

    ArrayList<Article> references = new ArrayList<>();
    ArrayList<Article> citations = new ArrayList<>();

    public Article(String title, String url, int id, String abstraction) {
        this.title = title;
        this.url = url;
        this.id = id;
        this.abstraction = abstraction;
    }

    @Override
    public String toString() {
        String string = "ID:" + id + "\nTITLE:" + title + "\n\tURL:" + url + "\n\nABSTRACTION:" + abstraction
                + "\n==========================\n"
                + "References(" + references.size() + "):";
        for (Article article : references) {
            string += "\n" + article.id + " - " + article.title;
        }
        string += "\n==========================\n"
                + "Citations(" + citations.size() + "):";
        for (Article article : citations) {
            string += "\n" + article.id + " - " + article.title;
        }


        return string;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
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
//        Gson gson = new Gson();
//        jsonObject.addProperty("references", gson.toJson(references));
//        jsonObject.addProperty("citations", gson.toJson(citations));
        return jsonObject;
    }

    public void addReference(Article article) {
        references.add(article);
    }

    public void addCitation(Article article) {
        citations.add(article);
    }
}
