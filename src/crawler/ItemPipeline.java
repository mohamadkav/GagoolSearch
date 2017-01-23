package crawler;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by saeed on 1/4/2016.
 */
public class ItemPipeline {
    private static ItemPipeline mInstance;
    private Core mCore;
    private HashMap<String, Boolean> seenUrls;
    private HashMap<String, HashSet<String>> referenced = new HashMap<>();

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

    public void addUrl(String preArticleUrl, String baseUrl, String url, boolean isReference) {
        Boolean seen = seenUrls.get(Core.getAbsoluteUrl(url));
        if (seen != null) {
            //TODO: add to references?!
            mCore.setReferencesAndCitations(preArticleUrl, Core.getAbsoluteUrl(url));
//            System.out.println("TEKRARI>>>>> " + Core.getAbsoluteUrl(url));
        } else {
            mCore.getScheduler().addUrl(Core.getAbsoluteUrl(url));
            seenUrls.put(Core.getAbsoluteUrl(url), true);
            if (isReference) {
                HashSet<String> references = referenced.get(baseUrl);
                if (references == null) {
                    references = new HashSet<>();
                }
                references.add(url);
                referenced.put(baseUrl, references);
            } else {
                HashSet<String> cites  = new HashSet<>();
                cites.add(url);
                referenced.put(baseUrl, cites);
            }
        }
//        throw new RuntimeException("IN CONSTRUCTION");
    }
}
