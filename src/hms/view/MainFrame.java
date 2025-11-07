package hms.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("호텔 관리 시스템");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(37, 99, 235));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("호텔 관리 시스템");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton loginButton = new JButton("로그인");
        JButton signupButton = new JButton("회원가입");

        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(243, 244, 246));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        welcomePanel.add(new JLabel("<html><h2>환영합니다</h2><p>호텔 관리 시스템에 오신 것을 환영합니다.</p></html>"));

        panel.add(welcomePanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        gridPanel.setOpaque(false);

        gridPanel.add(createCard("객실 관리", "객실 예약 및 관리"));
        gridPanel.add(createCard("고객 관리", "고객 정보 조회 및 관리"));
        gridPanel.add(createCard("예약 관리", "예약 현황 조회"));

        panel.add(gridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(31, 41, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel footerLabel = new JLabel("호텔 관리 시스템 © 2025");
        footerLabel.setForeground(Color.WHITE);

        panel.add(footerLabel);
        return panel;
    }

    private JPanel createCard(String title, String description) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel(description);
        descLabel.setForeground(Color.GRAY);
        card.add(descLabel, BorderLayout.CENTER);

        JButton button = new JButton("바로가기");
        card.add(button, BorderLayout.SOUTH);

        return card;
    }
}