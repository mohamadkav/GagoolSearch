package newcrawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by mohammad on 1/23/17.
 */
public class Parser {
    private static final String wikiString=" - ویکی\u200Cپدیا، دانشنامهٔ آزاد";
    public static String getAbstractFromDoc(Document doc){
        Elements spanWithId = doc.select("table#noarticletext");

        if (spanWithId.size() == 0) {
            for (Element element : doc.select("table"))
                element.remove();
            Elements paragraphs = doc.select(".mw-body-content p");
            Element firstParagraph = paragraphs.first();
            return (firstParagraph.text());
        }
        else throw new NullPointerException();

    }

    public static String getTitleFromDoc(Document doc){
        return doc.title().substring(0,doc.title().indexOf(wikiString));
    }
}
