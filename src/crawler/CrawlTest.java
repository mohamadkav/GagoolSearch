package crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Article;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by saeed on 12/29/2015.
 */
public class CrawlTest {

    private static final String SEPARATOR = "===========================================================";
    public static final String URL_PATTERN = "^[a-zA-Z/.:]+([0-9]+).*";
    public static final String FIRST_LINK = "https://www.researchgate.net/researcher/8159937_Zoubin_Ghahramani";
    private static Pattern sPublicationIdPattern = Pattern.compile(URL_PATTERN);

    public void run() throws IOException {
        Document doc = Jsoup.connect(FIRST_LINK).get();
        Elements listOfFirstDocsHeaders = getListOfFirstDocsHeaders(doc);
        for (Element header : listOfFirstDocsHeaders) {
            processHeaders(header);
        }
        doc = Jsoup.connect("https://www.researchgate.net/publication/248589608_A_K_Hypotheses_Other_Belief_Updating_Model")
                .get();
        System.out.println(SEPARATOR);
        System.out.println(doc);
        System.out.println(SEPARATOR);
        Elements list = getListOfCitations(doc);
        System.out.println(list.size());
        for (Element header : list) {
            System.out.println(header);
            System.out.println(SEPARATOR);
        }
        System.out.println(SEPARATOR);
        String ref = "https://www.researchgate.net/publication/285458515_A_General_Framework_for_Constrained_Bayesian_Optimization_using_Information-based_Search?ev=auth_pub";//"https://www.researchgate.net/publication/45893028_Learning_the_Structure_of_Deep_Sparse_Graphical_Models";
        getReferences(ref);
        System.out.println("END");
        getCitations(ref);
    }

    private String buildResponseFromEntity(HttpEntity entity)
            throws IllegalStateException, IOException {

        BufferedReader r = new BufferedReader(new InputStreamReader(
                entity.getContent()));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    private void processHeaders(Element header) {
        System.out.println(header);
        Element headerLink = header.getElementsByTag("a").first();
        String title = headerLink.getElementsByTag("span").first().html();
        String url = headerLink.attr("href");
        Article article = new Article(title, url);
        System.out.println(article);
    }

    private Elements getListOfFirstDocsHeaders(Document doc) {
        Element firstDocsLi = doc.select(".journal-publications ul").first();
        return firstDocsLi.select("li div h5");
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
        String responseStr = getAjaxResult(ref, url);
        System.out.println(SEPARATOR);
        ArrayList<String[]> citations = parseCitationsJson(responseStr);
        for (String[] citation : citations) {
            System.out.println(citation[0] + " - " + citation[1]);
        }
    }

    private void getReferences(String ref) throws IOException {
        String pubId = getPublicationId(ref);
        String url = getRefUrl(pubId);
        getCitesRefs(url, ref);
    }

    private String getCitationUrl(String pubId) {
        return "https://www.researchgate.net/publicliterature.PublicationIncomingCitationsList.html?publicationUid=" + pubId + "&citedInPage=1&swapJournalAndAuthorPositions=0&showAbstract=1&showType=1&showPublicationPreview=1&totalCount=14&showContexts=1&publicationUid=" + pubId + "&limit=10&offset=0";
    }

    private String getRefUrl(String pubId) {
        return "https://www.researchgate.net/publicliterature.PublicationCitationsList.html?publicationUid=" + pubId + "&swapJournalAndAuthorPositions=0&showAbstract=1&showType=1&showPublicationPreview=1&publicationUid=" + pubId + "&sort=normal&limit=10&offset=0";
    }

    private void getCitations(String ref) throws IOException {
        String pubId = getPublicationId(ref);
        System.out.println(pubId);
        String url = getCitationUrl(pubId);
        getCitesRefs(url, ref);
    }

    private ArrayList<String[]> parseCitationsJson(String responseStr) {
        JsonArray citationArray = new JsonParser()
                .parse(responseStr)
                .getAsJsonObject()
                .getAsJsonObject("result")
                .getAsJsonObject("data")
                .getAsJsonArray("citationItems");
        ArrayList<String[]> citations = new ArrayList<>();
        for (int i = 0; i < ((citationArray.size() >= 10) ? 10 : citationArray.size()); i++) {
            JsonObject citation = citationArray.get(i).getAsJsonObject().getAsJsonObject("data");
            citations.add(new String[]{citation.get("title").getAsString()
                    , citation.get("url").getAsString()});
        }
        return citations;
    }

    private String getAjaxResult(String ref, String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        request.addHeader("authority", "www.researchgate.net");
        request.addHeader("accept", "application/json");
        request.addHeader("referer", ref);
        request.addHeader("x-requested-with", "XMLHttpRequest");
        request.addHeader("user-agent", "Mozilla/5.0 (Macintosh;Intel Mac OS X 10_11_2) AppleWebKit/537.36(KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        HttpResponse response = client.execute(request);
        return buildResponseFromEntity(response.getEntity());
    }

    private Elements getListOfCitations(Document doc) {
        return doc.select(".pub-citations-list ul li div h5");
    }
}
