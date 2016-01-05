package crawler;

import com.sun.istack.internal.Nullable;
import models.Article;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by saeed on 1/4/2016.
 */
public class Scheduler {
    private static Scheduler mInstance = new Scheduler();
    private Queue<String> urls;

    private Scheduler() {
        urls = new LinkedList<>();
    }

    public static Scheduler getInstance() {
        if (mInstance == null) {
            mInstance = new Scheduler();
        }
        return mInstance;
    }

    public void addUrl(String url) {
        urls.add(url);
    }

    /**
     * @return next article or null if there is no article left!
     */
    @Nullable
    public String getNextUrl() {
        return urls.poll();
    }
}
