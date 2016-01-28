package ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by saeed on 1/24/2016.
 */
public class HomePageFrame extends JFrame {
    public static final int FRAME_WIDTH = 700;
    public static final int FRAME_HEIGHT = 700;
    public static final int LOGO_HEIGHT = 100;
    public static final int MARGIN_HEIGHT = 30;
    public static final int SUBMIT_BUTTON_Y = LOGO_HEIGHT + 2 * MARGIN_HEIGHT;
    public static final String LOGO_IMAGE_JPG = "logo.jpg";
    public static final int QUERY_FIELD_HEIGHT = 30;
    private Color bgColor = new Color(236, 230, 198);
    private JLabel logo;
    private JTextField queryField;
    private JButton submitButton;
    private JsonObject testResult;
    private DefaultListModel<String> resultListModel;
    private JList resultsList;
    JScrollPane resultScrollPane;

    public HomePageFrame() {
        setTest();
        setFrame();
        setLogo();
        setQueryField();
        setSubmitButton();
        setResultList();
        setVisible(true);
    }

    private void setResultList() {
        resultScrollPane = new JScrollPane();
        resultListModel = new DefaultListModel<>();
        resultsList = new JList(resultListModel);
        resultScrollPane.setViewportView(resultsList);
        resultScrollPane.setBounds(MARGIN_HEIGHT,
                SUBMIT_BUTTON_Y + MARGIN_HEIGHT + QUERY_FIELD_HEIGHT,
                FRAME_WIDTH - 2 * MARGIN_HEIGHT,
                QUERY_FIELD_HEIGHT * 5);
        add(resultScrollPane);
    }

    private void setTest() {
        testResult = new JsonObject();
        testResult.addProperty("total", 3);
        testResult.addProperty("max_score", 0.5);
        JsonArray hits = new JsonArray();

        JsonObject test = new JsonObject();
        test.addProperty("_index", "gagool");
        test.addProperty("_type", "article");
        test.addProperty("_id", "1");
        test.addProperty("_score", 0.5);
        JsonObject source = new JsonObject();
        source.addProperty("id", 1);
        source.addProperty("title", "salam");
        source.addProperty("url", "yaChize.com");
        source.addProperty("abstraction", "kheyli jamoJoor");
        source.addProperty("page_rank", 0.6);
        test.add("_source", source);
        hits.add(test);

        test = new JsonObject();
        test.addProperty("_index", "gagool");
        test.addProperty("_type", "article");
        test.addProperty("_id", "2");
        test.addProperty("_score", 0.4);
        source = new JsonObject();
        source.addProperty("id", 2);
        source.addProperty("title", "salam2");
        source.addProperty("url", "yaChize.com2");
        source.addProperty("abstraction", "kheyli jamoJoor2");
        source.addProperty("page_rank", 0.5);
        test.add("_source", source);
        hits.add(test);

        test = new JsonObject();
        test.addProperty("_index", "gagool");
        test.addProperty("_type", "article");
        test.addProperty("_id", "3");
        test.addProperty("_score", 0.3);
        source = new JsonObject();
        source.addProperty("id", 3);
        source.addProperty("title", "salam3");
        source.addProperty("url", "yaChize.com3");
        source.addProperty("abstraction", "kheyli jamoJoor3");
        source.addProperty("page_rank", 0.9);
        test.add("_source", source);
        hits.add(test);

        testResult.add("hits", hits);
    }

    private void setSubmitButton() {
        submitButton = new JButton("search");
        submitButton.setBounds(
                MARGIN_HEIGHT,
                SUBMIT_BUTTON_Y,
                100,
                QUERY_FIELD_HEIGHT
        );
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //we hope the action is clicked :|
                //TODO: add search here!
                addResultToList(testResult);
            }
        });
        add(submitButton);
    }

    private void addResultToList(JsonObject result) {
        resultListModel.clear();
        for (JsonElement jsonElement : ((JsonArray) result.get("hits"))) {
            resultListModel.addElement(parseArticle((JsonObject) jsonElement));
        }
    }

    private String parseArticle(JsonObject article) {
        JsonObject json = (JsonObject) article.get("_source");
        return "(" + json.get("id").getAsString() + ") " + json.get("title").getAsString() + "<->"
                + json.get("abstraction").getAsString() + "<->"
                + json.get("url").getAsString() + "<->"
                + json.get("page_rank").getAsString();
    }

    private void setQueryField() {
        queryField = new JTextField();
        queryField.setEditable(true);
        queryField.setBounds(MARGIN_HEIGHT,
                LOGO_HEIGHT + MARGIN_HEIGHT,
                FRAME_WIDTH - 2 * MARGIN_HEIGHT,
                QUERY_FIELD_HEIGHT);
        queryField.setHorizontalAlignment(SwingConstants.CENTER);
        add(queryField);
    }

    private void setFrame() {
        setLayout(null);
        getContentPane().setBackground(bgColor);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
    }

    private void setLogo() {
        logo = new JLabel();
        logo.setIcon(new ImageIcon(LOGO_IMAGE_JPG));
        logo.setBounds(0, 0, FRAME_WIDTH, LOGO_HEIGHT);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        add(logo);
    }

    public String getQuery() {
        return queryField.getText();
    }
}
