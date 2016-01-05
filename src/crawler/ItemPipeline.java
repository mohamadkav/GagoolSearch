package crawler;

import models.Article;

import java.util.HashMap;

/**
 * Created by saeed on 1/4/2016.
 */
public class ItemPipeline {
    private static ItemPipeline mInstance;
    private Core mCore;
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

    public void addUrl(String url) {
        Boolean seen = seenUrls.get(mCore.getAbsoluteUrl(url));
        if (seen != null) {
            System.out.println("TEKRARI>>>>> " + mCore.getAbsoluteUrl(url));
        } else {
            mCore.scheduler.addUrl(mCore.getAbsoluteUrl(url));
            seenUrls.put(mCore.getAbsoluteUrl(url), true);
        }
//        throw new RuntimeException("IN CONSTRUCTION");
    }
}
