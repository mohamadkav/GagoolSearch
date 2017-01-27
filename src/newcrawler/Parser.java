package newcrawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

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
            for(Element paragraph : paragraphs){
                String text= Parser.arabicToDecimal(paragraph.text()).replaceAll("\\d","");//.replaceAll("\\[", "").replaceAll("\\]","").replaceAll("/","").replaceAll("\"","");
                text=removeSpecialChars(text);
                if(text.replaceAll(" ","").length()>50)
                    return text;
            }
            return paragraphs.first().text();
        }
        else throw new NullPointerException();

    }

    public static String getAllTextFromDoc(Document doc){
        Elements spanWithId = doc.select("table#noarticletext");
        StringBuilder stringBuilder=new StringBuilder();
        if (spanWithId.size() == 0) {
            for (Element element : doc.select("table"))
                element.remove();
            Elements paragraphs = doc.select(".mw-body-content p");
            for(Element paragraph : paragraphs){
                String text= Parser.arabicToDecimal(paragraph.text()).replaceAll("\\d","");//.replaceAll("\\[", "").replaceAll("\\]","").replaceAll("/","").replaceAll("\"","");
                text=removeSpecialChars(text);
                stringBuilder.append(text+" ");
            }
            return stringBuilder.toString();
        }
        else throw new NullPointerException();

    }


    private static String removeSpecialChars(String rawInput){
        StringBuilder stringBuilder=new StringBuilder();
        for(char c:rawInput.toCharArray()){
            if(c=='\u200c' || c==' '|| ((int)c>Integer.valueOf("0620",16)&&(int)c<Integer.valueOf("064A",16)) ||  ((int)c>Integer.valueOf("066E",16)&&(int)c<Integer.valueOf("06D2",16)) )
                stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static String getTitleFromDoc(Document doc){
        return doc.title().substring(0,doc.title().indexOf(wikiString));
    }

    public static List<String> extractLinksFromDoc(Document doc){
        List<String> toReturn=new ArrayList<>();
        Elements links = doc.select("a[href]");
        for(Element link:links) {
            String linkText=link.attr("abs:href");
            if (linkText.contains("https://fa.wikipedia.org/wiki/")&&
                    linkText.substring(linkText.indexOf("https:")+"https:".length()).indexOf(':')==-1&&
                    !linkText.contains("#")&&
                    !arabicToDecimal(link.text()).matches(".*\\d+.*")&&
                    !linkText.equals("https://fa.wikipedia.org/wiki/%D8%B5%D9%81%D8%AD%D9%87%D9%94_%D8%A7%D8%B5%D9%84%DB%8C")&&
                    !link.text().contains("ابهام\u200Cزدایی"))
                toReturn.add(linkText);
        }
        return toReturn;
    }

    private static String arabicToDecimal(String number) {
        char[] chars = new char[number.length()];
        for(int i=0;i<number.length();i++) {
            char ch = number.charAt(i);
            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';
            chars[i] = ch;
        }
        return new String(chars);
    }
}
