package crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Article;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by saeed on 1/4/2016.
 */
public class Parser {
    private static Parser mInstance;
    private Core mCore;

    private Parser(Core core) {
        mCore = core;
    }

    public static Parser getInstance(Core core) {
        if (mInstance == null) {
            mInstance = new Parser(core);
        }
        return mInstance;
    }

    public void parseFirstPage(Document doc) throws IOException {
        Elements listOfFirstDocsHeaders = getListOfFirstDocsHeaders(doc);
        for (int i = 0; i < ((listOfFirstDocsHeaders.size() > 10) ? 10 : listOfFirstDocsHeaders.size()); i++) {
            Element header = listOfFirstDocsHeaders.get(i);
            String url = processHeaders(header);
            mCore.scheduler.addUrl(Core.getAbsoluteUrl(url));
        }
    }

    public void parseDoc(String url, Document doc, ArrayList<String> references, ArrayList<String> citations)
        throws Exception{
//        if (url.contains("273488773"))
//            System.out.println(doc);
        Elements elements = doc.select(".pub-abstract div div");
        Element element;
//        if (elements == null || elements.html().equals("")) {
//            elements = doc.select(".publication-abstract-text span");
//        }
//        if (elements == null || elements.html().equals("")) {
//            elements = doc.select(".pub-abstract div");
//        }
        element = elements.first();
        String docPreString = element.html();
        String abstraction = docPreString.replace("\n<br>", "");
        elements = doc.select(".pub-title");
        if (elements == null || elements.html().equals(""))
            elements = doc.select(".publication-title");
        element = elements.first();
        mCore.addArticle(url, element.html(), abstraction, references, citations);
//        System.out.println(docString);
    }

    private Elements getListOfFirstDocsHeaders(Document doc) {
        Element firstDocsLi = doc.select(".journal-publications ul").first();
        return firstDocsLi.select("li div h5");
    }


    private String processHeaders(Element header) {
        Element headerLink = header.getElementsByTag("a").first();
        String title = headerLink.getElementsByTag("span").first().html();
        return headerLink.attr("href");
    }

    public ArrayList<String> parseCiteRefResponse(String preArticleUrl, String url, HttpResponse httpResponse, boolean isReference/**or citation!**/) {
        ArrayList<String> citationsUrl = new ArrayList<>();
        try {
            String response = buildResponseFromEntity(httpResponse.getEntity());
            ArrayList<String[]> citations = parseCitationsJson(response);
            for (String[] citation : citations) {
                mCore.itemPipeline.addUrl(preArticleUrl, url, citation[1], isReference);
                citationsUrl.add(Core.getAbsoluteUrl(citation[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return citationsUrl;
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

    private ArrayList<String[]> parseCitationsJson(String responseStr) {
        JsonArray citationArray = new JsonParser()
                .parse(responseStr)
                .getAsJsonObject()
                .getAsJsonObject("result")
                .getAsJsonObject("data")
                .getAsJsonArray("citationItems");
        ArrayList<String[]> citations = new ArrayList<>();
        int dismiss = 0;
        int i = 0;
        for (i = 0; i < ((citationArray.size() >= 10) ? 10 : citationArray.size()); i++) {
            JsonObject citation = citationArray.get(i).getAsJsonObject().getAsJsonObject("data");
            try {
                citations.add(new String[]{citation.get("title").getAsString()
                        , citation.get("url").getAsString()});
            } catch (NullPointerException e) {
                dismiss++;
            }
        }
        while (dismiss > 0 && citationArray.size() < i) {
            JsonObject citation = citationArray.get(i++).getAsJsonObject().getAsJsonObject("data");
            try {
                citations.add(new String[]{citation.get("title").getAsString()
                        , citation.get("url").getAsString()});
                dismiss--;
            } catch (NullPointerException e) {
                dismiss++;
            }
        }
        return citations;
    }
}
