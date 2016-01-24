package ui;

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
    public static final String LOGO_IMAGE_JPG = "logo.jpg";
    public static final int QUERY_FIELD_HEIGHT = 30;
    private Color bgColor = new Color(236, 230, 198);
    private JLabel logo;
    private JTextField queryField;
    private JButton submitButton;

    public HomePageFrame() {
        setFrame();
        setLogo();
        setQueryField();
        setSubmitButton();
        setVisible(true);
    }

    private void setSubmitButton() {
        submitButton = new JButton("search");
        submitButton.setBounds(
                MARGIN_HEIGHT,
                LOGO_HEIGHT + 2 * MARGIN_HEIGHT,
                100,
                QUERY_FIELD_HEIGHT
        );
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //we hope the action is clicked :|
                //TODO: add search here!
            }
        });
        add(submitButton);
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
