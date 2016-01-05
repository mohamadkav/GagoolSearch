package crawler;

import com.sun.istack.internal.Nullable;
import models.Article;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by saeed on 1/4/2016.
 */
public class Downloader {
    public static final String AUTHORITY_HEADER = "authority";
    public static final String ACCEPT_HEADER = "accept";
    public static final String REFERER_HEADER = "referer";
    public static final String X_REQUESTED_WITH_HEADER = "x-requested-with";
    public static final String USER_AGENT_HEADER = "user-agent";
    public static final String USER_AGENT_VALUE = "Mozilla/5.0 (Macintosh;Intel Mac OS X 10_11_2) AppleWebKit/537.36(KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
    public static final String X_REQUEST_WITH_VALUE = "XMLHttpRequest";
    public static final String ACCEPT_VALUE = "application/json";
    public static final String AUTHORITY_VALUE = "www.researchgate.net";
    private static Downloader mInstance;
    private Core mCore;
    public static final String URL_PATTERN = "^[a-zA-Z/.:]+([0-9]+).*";
    private static Pattern sPublicationIdPattern = Pattern.compile(URL_PATTERN);

    private Downloader(Core core) {
        mCore = core;
    }

    public static Downloader getInstance(Core core) {
        if (mInstance == null) {
            mInstance = new Downloader(core);
        }
        return mInstance;
    }

    void run() throws InterruptedException, IOException {
        String url;
        String articleUrl;
        String pubId;
        Document doc;
        url = mCore.scheduler.getNextUrl();
        while (url != null && !mCore.isDone()) {
            System.out.println("downloading article: " + url);
            pubId = getPublicationId(url);
            doc = getArticlePage(url);
            System.out.println(doc);
            mCore.parser.parseDoc(url, doc);
            getReferences(url, pubId);
            getCitations(url, pubId);
            url = mCore.scheduler.getNextUrl();
        }
    }

    @Nullable
    public Document getArticlePage(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("time_out_in: link. the article had been added to the list again!");
            mCore.scheduler.addUrl(url);
            return null;
        }
    }

    public Document getPage(String link) {
        try {
            return Jsoup.connect(link).get();
        } catch (IOException e) {
            System.out.println("time_out_in: link. try again!!");
            return getPage(link);
        }
    }

    private void getReferences(String ref, String pubId) throws IOException {
        getCitesRefs(getRefUrl(pubId), ref);
    }

    private void getCitations(String ref, String pubId) throws IOException {
        getCitesRefs(getCitationUrl(pubId), ref);
    }


    private String getPublicationId(String ref) {
        Matcher publicationIdMatcher = sPublicationIdPattern.matcher(ref);
        if (!publicationIdMatcher.find()) {
            System.out.println("?!!!!?!?!??!");
            return "";
        }
        return publicationIdMatcher.group(1);
    }

    private void getCitesRefs(String url, String ref) throws IOException {
        HttpResponse response = getAjaxResult(ref, url);
        mCore.parser.parseCiteRefResponse(response);
    }


    private HttpResponse getAjaxResult(String ref, String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        request.addHeader(AUTHORITY_HEADER, AUTHORITY_VALUE);
        request.addHeader(ACCEPT_HEADER, ACCEPT_VALUE);
        request.addHeader(REFERER_HEADER, ref);
        request.addHeader(X_REQUESTED_WITH_HEADER, X_REQUEST_WITH_VALUE);
        request.addHeader(USER_AGENT_HEADER, USER_AGENT_VALUE);
        HttpResponse response = client.execute(request);
        return response;
    }

    private String getCitationUrl(String pubId) {
        return "https://www.researchgate.net/publicliterature.PublicationIncomingCitationsList.html?publicationUid=" + pubId + "&citedInPage=1&swapJournalAndAuthorPositions=0&showAbstract=1&showType=1&showPublicationPreview=1&totalCount=14&showContexts=1&publicationUid=" + pubId + "&limit=10&offset=0";
    }

    private String getRefUrl(String pubId) {
        return "https://www.researchgate.net/publicliterature.PublicationCitationsList.html?publicationUid=" + pubId + "&swapJournalAndAuthorPositions=0&showAbstract=1&showType=1&showPublicationPreview=1&publicationUid=" + pubId + "&sort=normal&limit=10&offset=0";
    }
}
