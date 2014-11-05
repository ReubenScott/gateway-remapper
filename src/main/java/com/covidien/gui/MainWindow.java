package com.covidien.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.covidien.util.AgentConfig;
import com.covidien.util.CheckSSLConnection;
import com.covidien.util.DomXML;

public class MainWindow extends JFrame implements ActionListener, WindowListener {
    private static final long serialVersionUID = -6228311567187657412L;
    JLabel jl, jl2, jlhead;
    JTextField jf;
    JPanel jp;
    JButton jexit;
    private JButton jmp, jcc;
    JComboBox jc;
    String[] arrs;
    int currentj;
    String[] cagentState;
    public static String[] serverName, serverValue;
    public static String serverPath, defaultServerPath, oldServerName;
    JProgressBar jbar;
    public static boolean newbool;

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    public static int stateTime = 0;
    public static String stateTimeState = "";
    public static int xmlStateTime = 6;
    private boolean checking = true;

    public MainWindow(String[] serverName, String[] s, boolean bool, String serverPath, String agentState) {
        setResizable(false);

        MainWindow.serverName = serverName;
        MainWindow.serverValue = s;
        MainWindow.newbool = bool;
        String currentServer = "";

        MainWindow.xmlStateTime = 6;

        if (serverPath == null) {
            defaultServerPath = MainWindow.serverPath = serverPath = "No URL";
        } else {
            defaultServerPath = MainWindow.serverPath = serverPath;
        }
        arrs = s;
        int j = -1;
        for (int i = 0; i < s.length; i++) {
            if (s[i].toLowerCase().equals(serverPath.toLowerCase())) {
                oldServerName = currentServer = serverName[i];
                j = i;
                this.currentj = j;
            }
        }
        if (currentServer.equals("")) {
            currentServer = "Unknown";
        }
        this.setLayout(new FlowLayout());
        ImageIcon icon = new ImageIcon(MainWindow.class.getClassLoader().getResource("resources/title.PNG"));
        this.setIconImage(icon.getImage());
        this.setTitle("RSS Remapper");
        jlhead = new JLabel(currentServer + " (" + serverPath.toLowerCase() + ")");
        jlhead.setOpaque(true);

        jp = new JPanel(new FlowLayout(FlowLayout.LEFT));

        jp.setBorder(BorderFactory.createTitledBorder("Current Server"));
        jp.setPreferredSize(new Dimension(370, 60));
        jp.add(jlhead);
        this.add(jp);

        if (agentState == null) {
            agentState = "Agent is not installed";
        }
        jl2 = new JLabel(agentState);
        jl2.setPreferredSize(new Dimension(280, 20));
        if (agentState.equals("RUNNING")) {
            jl2.setBackground(Color.green);
        } else if (agentState.equals("STOPPED")) {
            jl2.setBackground(Color.lightGray);
        }
        jl2.setOpaque(true);

        jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jp.setPreferredSize(new Dimension(370, 60));
        jp.setBorder(BorderFactory.createTitledBorder("Agent Status"));

        JPanel app = new JPanel();
        JPanel jpimg = null;
        try {
            jpimg = new ImagePane(MainWindow.class.getClassLoader().getResourceAsStream("resources/AKG.png"));
        } catch (IOException e2) {
            return;
        }
        jpimg.setPreferredSize(new Dimension(60, 25));
        jp.add(jpimg);
        jp.add(jl2);
        this.add(jp);

        jp = new JPanel(new GridBagLayout());
        jp.setPreferredSize(new Dimension(370, 180));
        jp.setBorder(BorderFactory.createTitledBorder("Settings"));

        JPanel selectPanel = new JPanel(new GridLayout(2, 1));
        selectPanel.setBorder(BorderFactory.createTitledBorder("Available Servers"));

        JPanel urlPanel = new JPanel();

        jc = new JComboBox(serverName);

        if (j != -1) {
            jc.setSelectedIndex(j);
        }
        jc.setPreferredSize(new Dimension(250, 30));
        jc.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getSource() == jc && e.getStateChange() == ItemEvent.SELECTED) {
                    int i = jc.getSelectedIndex();
                    MainWindow.serverPath = DomXML.INSTANCE.getServerUrl(i);
                    jf.setText(MainWindow.serverPath);
                }
            }
        });

        urlPanel.add(jc);
        selectPanel.add(urlPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        // gbc.fill = GridBagConstraints.HORIZONTAL;
        jp.add(selectPanel, gbc);

        JPanel urlPanel3 = new JPanel();
        urlPanel3.setBorder(BorderFactory.createTitledBorder("URL"));

        jf = new JTextField(serverPath);
        jf.setEditable(bool);
        jf.setPreferredSize(new Dimension(230, 25));
        jf.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent arg0) {
                MainWindow.serverPath = jf.getText();
            }

            public void insertUpdate(DocumentEvent arg0) {
                MainWindow.serverPath = jf.getText();
            }

            public void removeUpdate(DocumentEvent arg0) {
                MainWindow.serverPath = jf.getText();
            }

        });
        urlPanel3.add(jf);

        selectPanel.add(urlPanel3);

        JPanel selectPanel2 = new JPanel();
        selectPanel2.setLayout(new BorderLayout());
        selectPanel2.setBorder(BorderFactory.createTitledBorder("Remapping"));
        jmp = new JButton("Remap");
        jmp.setPreferredSize(new Dimension(80, 50));
        jmp.addActionListener(this);
        jcc = new JButton("Test");
        jcc.setPreferredSize(new Dimension(80, 50));
        jcc.addActionListener(this);
        selectPanel2.add(jmp, BorderLayout.NORTH);
        selectPanel2.add(jcc, BorderLayout.SOUTH);

        GridBagConstraints gbcx = new GridBagConstraints();
        gbcx.gridx = 6;
        gbcx.gridy = 0;
        gbcx.gridheight = 1;
        gbcx.gridwidth = 1;
        gbcx.fill = GridBagConstraints.VERTICAL;
        jp.add(selectPanel2, gbcx);
        this.add(jp);

        jp = new JPanel();
        jp.setPreferredSize(new Dimension(370, 70));
        jp.setBorder(BorderFactory.createTitledBorder("Status"));
        jbar = new JProgressBar();
        jbar.setPreferredSize(new Dimension(350, 25));
        jbar.setMinimum(0);
        jbar.setMaximum(100);
        jbar.setValue(0);
        jbar.setAutoscrolls(true);
        jp.add(jbar);
        this.add(jp);

        app = new JPanel(new FlowLayout());
        app.setPreferredSize(new Dimension(370, 70));
        app.setLayout(new FlowLayout(FlowLayout.LEFT));

        try {
            jpimg = new ImagePane(MainWindow.class.getClassLoader().getResourceAsStream("resources/logo.PNG"));
        } catch (IOException e1) {
            return;
        }
        jpimg.setPreferredSize(new Dimension(200, 30));
        app.add(jpimg);

        jexit = new JButton("Exit");
        jexit.addActionListener(this);

        JPanel exp = new JPanel();
        exp.setPreferredSize(new Dimension(160, 35));
        exp.setLayout(new FlowLayout(FlowLayout.RIGHT));

        exp.add(jexit);
        app.add(exp);
        this.add(app);
        this.setSize(400, 480);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener(this);

        // check the Agent status 10 sec. period

        service.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (!checking) {
                    return;
                }
                AgentConfig cagent = AgentConfig.INSTANCE;
                String cagentState = null;
                cagentState = cagent.getAgentState();
                if (!checking) {
                    return;
                }
                if (cagentState == null) {
                    jc.setEnabled(false);
                    jmp.setEnabled(false);
                    jl2.setText("Agent is not installed");
                    jl2.setBackground(Color.red);
                } else {
                    jl2.setText(cagentState);
                }
                if (cagentState.equals("RUNNING")) {
                    jbar.setValue(100);
                    jmp.setEnabled(true);
                    jl2.setBackground(Color.green);
                } else {
                    if ("START_PENDING".equals(cagentState)) {
                        jbar.setValue(75);
                    } else if ("STOP_PENDING".equals(cagentState)) {
                        jbar.setValue(50);
                    }
                    jl2.setBackground(Color.lightGray);
                }
                jl2.setOpaque(true);
            }
        }, 0, 2, TimeUnit.SECONDS);

    }

    public void serverURLChanged(String url) {
        int j = -1;
        for (int i = 0; i < serverValue.length; i++) {
            if (serverValue[i].toLowerCase().equals(url.toLowerCase())) {
                if (oldServerName.equals(serverName[i])) {
                    return;
                }
                oldServerName = serverName[i];
                j = i;
                this.currentj = j;
                break;
            }
        }
        if (j != -1) {
            jc.setSelectedIndex(j);
            jc.repaint();
        } else {
            oldServerName = "Unknown";
        }
        jlhead.setText(oldServerName + " (" + url + ")");
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if (service != null) {
            service.shutdownNow();
        }
        AgentConfig.INSTANCE.stopMonitor();
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {

    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowOpened(WindowEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == jexit) {
            System.exit(0);
        } else if (source == jcc) {
            int i = jc.getSelectedIndex();
            String cserverName = DomXML.INSTANCE.getServerUrl(i);
            try {
                if (CheckSSLConnection.check(cserverName)) {
                    JOptionPane.showMessageDialog(this, "The server can be accessed!", "Test Connection",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "The server can not be accessed!", "Test Connection",
                    JOptionPane.ERROR_MESSAGE);
        } else if (source == jmp) {
            AgentConfig agent = AgentConfig.INSTANCE;

            String cserverName = (String) jc.getSelectedItem();
            if (cserverName.equals(MainWindow.oldServerName)) {
                return;
            }
            jbar.setValue(10);
            jmp.setEnabled(false);
            jl2.setBackground(Color.lightGray);
            checking = false;
            try {
                jl2.setText("Changing the Agent settings...");
                agent.setServerPath(MainWindow.serverPath);
                MainWindow.defaultServerPath = MainWindow.serverPath;
                MainWindow.oldServerName = cserverName;
                jlhead.setText(cserverName + " (" + MainWindow.serverPath + ")");
                jbar.setValue(20);
                jl2.setText("Agent settings changed");
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(this, "Failed to update the agent configuration file.", "Error!",
                        JOptionPane.ERROR_MESSAGE);
                jmp.setEnabled(true);
                checking = true;
                jbar.setValue(100);
                return;
            }
            jl2.setText("restarting Agent...");
            try {
                if (!agent.restartAgent()) {
                    jmp.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Can not restart Agent for following reason", "Error!",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "Can not restart Agent for following reason:\n" + e1.getMessage(),
                        "Error!", JOptionPane.ERROR_MESSAGE);
            }
            checking = true;

        }
    }
}
