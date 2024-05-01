package RecieveConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.sql.*;
import Client.Client2;
import Database.Database;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RecieveConnection {
    static ArrayList<MyFile> myFiles = new ArrayList<>();
    static int fileId = 0;
    public static void RecieveMenu(JFrame frame,int id) throws IOException {
        frame.setSize(600  , 600);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel header = new JPanel();
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        JLabel jlTitle = new JLabel("ShareFILE Receiver  ");
        JButton viewhistory = new JButton("View History");
        Client2.addstyle(viewhistory);
        viewhistory.setFont(new Font("Arial", Font.BOLD, 20));
        viewhistory.setAlignmentX(Component.RIGHT_ALIGNMENT);
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(jlTitle);
        header.add(viewhistory);
        jPanel.add(header);
        frame.add(jScrollPane);
        frame.setVisible(true);

        ServerSocket serverSocket = new ServerSocket(1234);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        int fileNameLength = dataInputStream.readInt();
                        if (fileNameLength > 0) {
                            byte[] fileNameBytes = new byte[fileNameLength];
                            dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                            String fileName = new String(fileNameBytes);
                            int fileContentLength = dataInputStream.readInt();
                            if (fileContentLength > 0) {
                                byte[] fileContentBytes = new byte[fileContentLength];
                                dataInputStream.readFully(fileContentBytes, 0, fileContentBytes.length);
                                JPanel jpFileRow = new JPanel();
                                jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.X_AXIS));
                                JLabel jlFileName = new JLabel(fileName);
                                jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
                                jlFileName.setBorder(new EmptyBorder(10,0, 10,0));
                                try{
                                    Connection conn = Database.getConnection();
                                    PreparedStatement stmt = conn.prepareStatement("INSERT INTO userhistory (u_id,filename,date) VALUES ("+id+",'"+fileName+"',NOW())");
                                    stmt.executeUpdate();
                                    conn.commit();
                                }catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                                if (getFileExtension(fileName).equalsIgnoreCase("txt")) {
                                    jpFileRow.setName((String.valueOf(fileId)));
                                    jpFileRow.addMouseListener(getMyMouseListener());
                                    jpFileRow.add(jlFileName);
                                    jPanel.add(jpFileRow);
                                    frame.validate();
                                } else {
                                    jpFileRow.setName((String.valueOf(fileId)));
                                    jpFileRow.addMouseListener(getMyMouseListener());
                                    jpFileRow.add(jlFileName);
                                    jPanel.add(jpFileRow);
                                    frame.validate();
                                }
                                myFiles.add(new MyFile(fileId, fileName, fileContentBytes, getFileExtension(fileName)));
                                fileId++;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        viewhistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame historyFrame = new JFrame("History");
                historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                historyFrame.setSize(600, 600);
                JPanel historyPanel = new JPanel();
                historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
                List<String> records = fetchDatabaseRecords(id);
                for (String record : records) {
                    JLabel recordLabel = new JLabel(record);
                    recordLabel.setFont(new Font("Arial", Font.BOLD, 20));
                    historyPanel.add(recordLabel);
                }
                JScrollPane scrollPane = new JScrollPane(historyPanel);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                historyFrame.add(scrollPane);
                historyFrame.setVisible(true);
            }
        });
    }

    public static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "No extension found.";
        }
    }

    public static MouseListener getMyMouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel jPanel = (JPanel) e.getSource();
                int fileId = Integer.parseInt(jPanel.getName());
                for (MyFile myFile : myFiles) {
                    if (myFile.getId() == fileId) {
                        JFrame jfPreview = createFrame(myFile.getName(), myFile.getData(), myFile.getFileExtension());
                        jfPreview.setVisible(true);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
    }

    public static List<String> fetchDatabaseRecords(int id) {
        List<String> records = new ArrayList<String>();
        try {
            Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM userhistory where u_id = "+id+" ORDER BY date");
            int i = 1;
            while (rs.next()) {
                String filename = rs.getString("filename");
                String date = rs.getString("date");
                records.add(i+"    |    " + filename + "   |   "+ date);
                i++;
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return records;
    }

    public static JFrame createFrame(String fileName, byte[] fileData, String fileExtension) {
        JFrame jFrame = new JFrame("ShareFILE Download");
        jFrame.setSize(400, 400);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        JLabel jlTitle = new JLabel("ShareFILE Download");
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));
        JLabel jlPrompt = new JLabel("Are you sure you want to download " + fileName + "?");
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 20));
        jlPrompt.setBorder(new EmptyBorder(20,0,10,0));
        jlPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton jbYes = new JButton("Yes");
        Client2.addstyle(jbYes);
        jbYes.setPreferredSize(new Dimension(150, 75));
        JButton jbNo = new JButton("No");
        Client2.addstyle(jbNo);
        jbNo.setPreferredSize(new Dimension(150, 75));
        JLabel jlFileContent = new JLabel();
        jlFileContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (fileExtension.equalsIgnoreCase("txt")) {
            jlFileContent.setText("<html>" + new String(fileData) + "</html>");
        } else {
            jlFileContent.setIcon(new ImageIcon(fileData));
        }

        jbYes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File fileToDownload = new File(fileName);
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);
                    fileOutputStream.write(fileData);
                    fileOutputStream.close();
                    jFrame.dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        jbNo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
            }
        });

        jPanel.add(jlTitle);
        jPanel.add(jlPrompt);
        jPanel.add(jlFileContent);
        jPanel.add(jbYes);
        jPanel.add(jbNo);
        jFrame.add(jPanel);
        return jFrame;
    }
}