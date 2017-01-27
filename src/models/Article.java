package models;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saeed on 12/29/2015.
 */
public class Article {
    public static String ID_KEY = "id";
    public static String TITLE_KEY = "title";
    private static String URL_KEY = "url";
    public static String ABSTRACTION_KEY = "abstraction";
    private static String REF_KEY = "referredURLs";
    public static String TEXT_KEY = "allText";



    private int id;
    private String title;
    private String url;
    private String abstraction;
    private String allText;
    private List<String> referredURLs;

    public Article(String title, String url, int id, String abstraction,String allText,List<String>referredURLs) {
        this.title = title;
        this.url = url;
        this.id = id;
        this.abstraction = abstraction;
        this.referredURLs=referredURLs;
        this.allText=allText;
    }

    @Override
    public String toString() {
        return "ID:" + id + "\nTITLE:" + title + "\n\tURL:" + url + "\n\nABSTRACTION:" + abstraction;
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

    public List<String> getReferredURLs() {
        return referredURLs;
    }

    public void setReferredURLs(List<String> referredURLs) {
        this.referredURLs = referredURLs;
    }

    public String getAllText() {
        return allText;
    }

    public void setAllText(String allText) {
        this.allText = allText;
    }

    public JsonObject getJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Article.ID_KEY, id);
        jsonObject.addProperty(Article.TITLE_KEY, title);
        jsonObject.addProperty(Article.URL_KEY, url);
        jsonObject.addProperty(Article.ABSTRACTION_KEY, abstraction);
        jsonObject.addProperty(Article.TEXT_KEY,allText);
        JsonElement jsonElement = new Gson().toJsonTree(referredURLs);
        jsonObject.add(Article.REF_KEY, jsonElement);
        return jsonObject;
    }

}
