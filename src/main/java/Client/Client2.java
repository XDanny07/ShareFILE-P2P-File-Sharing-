package Client;

import Database.Database;
import RecieveConnection.RecieveConnection;
import SendConnection.SendConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;

public class Client2 {
    final static File[] fileToSend = new File[1];

    public static void addstyle(JButton btn){
        btn.setFont(new Font("Arial", Font.BOLD, 16)); // Change font and size
        btn.setBackground(new Color(73, 190, 37)); // Change background color
        btn.setForeground(Color.WHITE); // Change text color
        btn.setFocusPainted(false); // Remove focus border
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Add padding
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void SendOrRecieve(JFrame frame , int id){
        frame.setTitle("Send or Recieve Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel welcomeLabel = new JLabel("Welcome to ShareFILE");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(welcomeLabel, gbc);

        JButton sendButton = new JButton("Send");
        addstyle(sendButton);
        JButton recieveButton = new JButton("Recieve");
        addstyle(recieveButton);

        gbc.gridy = 1;
        mainPanel.add(sendButton, gbc);

        gbc.gridy = 3;
        mainPanel.add(recieveButton, gbc);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                SendConnection.SendMenu(frame, fileToSend);
                frame.validate();
                frame.repaint();
            }
        });

        recieveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                try{
                    RecieveConnection.RecieveMenu(frame,id);
                }catch (IOException err){
                    System.out.println(err.getMessage());
                    return;
                }

                frame.validate();
                frame.repaint();
            }
        });

        frame.add(mainPanel);
        frame.pack();
        frame.setSize(600,600);
        frame.setVisible(true);
    }

    public static void LoginAndSignup(JFrame frame) {
        frame.setTitle("Login/Signup Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel welcomeLabel = new JLabel("Welcome to ShareFILE");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 25));
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(welcomeLabel, gbc);

        JButton loginButton = new JButton("Login");
        addstyle(loginButton);
        JButton signupButton = new JButton("Signup");
        addstyle(signupButton);
        JTextField usernameField = new JTextField(20);
        JTextField passwordField = new JTextField(20);
        JButton confirmButton = new JButton();
        addstyle(confirmButton);
        JLabel username = new JLabel("Username:");
        username.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel password = new JLabel("Password:");
        password.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel message = new JLabel("TEST");
        message.setFont(new Font("Arial", Font.BOLD, 15));
        gbc.gridy = 1;
        mainPanel.add(loginButton, gbc);

        gbc.gridy = 3;
        mainPanel.add(signupButton, gbc);

        JPanel loginSignupPanel = new JPanel(new GridBagLayout());

        gbc.gridy = 0;
        loginSignupPanel.add(username, gbc);
        gbc.gridy = 1;
        loginSignupPanel.add(usernameField, gbc);
        gbc.gridy = 2;
        loginSignupPanel.add(password, gbc);
        gbc.gridy = 3;
        loginSignupPanel.add(passwordField, gbc);
        gbc.gridy = 4;
        loginSignupPanel.add(confirmButton, gbc);
        gbc.gridy = 5;
        loginSignupPanel.add(message, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmButton.setText("Login");
                frame.getContentPane().removeAll();
                frame.add(loginSignupPanel);
                frame.validate();
                frame.repaint();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmButton.setText("Signup");
                frame.getContentPane().removeAll();
                frame.add(loginSignupPanel);
                frame.validate();
                frame.repaint();
            }
        });

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = usernameField.getText();
                String pass = passwordField.getText();
                if (confirmButton.getText().equals("Login")) {
                    try {
                        Connection conn = Database.getConnection();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * FROM userinfo WHERE username = '" + user + "' AND password = '" + pass + "'");
                        if (rs.next()) {
                            message.setText("Login successful.");
                            frame.getContentPane().removeAll();
                            SendOrRecieve(frame,rs.getInt(1));
                        } else {
                            message.setText("Login failed");
                        }
                        conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        Connection conn = Database.getConnection();
                        Statement stmt = conn.createStatement();
                        stmt.executeUpdate("INSERT INTO userinfo (username, password) VALUES ('" + user + "', '" + pass + "')");
                        conn.commit();
                        conn.close();
                        message.setText("Signup successful.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        frame.add(mainPanel);
        frame.pack();
        frame.setSize(600,600);
        frame.setVisible(true);
    }
    public static void main(String[] args) {

        Connection conn = Database.getConnection();

        final File[] fileToSend = new File[1];


        JFrame jFrame = new JFrame("ShareFILE");
        LoginAndSignup(jFrame);
    }

}
