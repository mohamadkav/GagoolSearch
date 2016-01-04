package crawler;

import com.sun.istack.internal.Nullable;
import models.Article;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by saeed on 1/4/2016.
 */
public class Scheduler {
    private static Scheduler mInstance = new Scheduler();
    private Queue<Article> articles;

    private Scheduler() {
        articles = new LinkedList<>();
    }

    public static Scheduler getInstance() {
        if (mInstance == null) {
            mInstance = new Scheduler();
        }
        return mInstance;
    }

    public void addArticle(Article article) {
        articles.add(article);
    }

    /**
     * @return next article or null if there is no article left!
     */
    @Nullable
    public Article getNextArticle() {
        return articles.poll();
    }
}
