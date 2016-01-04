package crawler;

import models.Article;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by saeed on 1/4/2016.
 */
public class ItemPipeline {
    private static ItemPipeline mInstance;
    private Core mCore;
    int count;
    HashMap<String, Boolean> seenUrls;

    private ItemPipeline(Core core) {
        mCore = core;
        seenUrls = new HashMap<>();
    }

    public static ItemPipeline getInstance(Core core) {
        if (mInstance == null) {
            mInstance = new ItemPipeline(core);
        }
        return mInstance;
    }

    public void processArticle(Article article) {

    }

    public void addArticle(Article article) {
        Boolean seen = seenUrls.get(article.getAbsoluteUrl());
        if (seen != null) {
            System.out.println("TEKRARI>>>>> " + article.getAbsoluteUrl());
        } else {
            mCore.scheduler.addArticle(article);
            count++;
            seenUrls.put(article.getAbsoluteUrl(), true);
        }
//        throw new RuntimeException("IN CONSTRUCTION");
    }
}
