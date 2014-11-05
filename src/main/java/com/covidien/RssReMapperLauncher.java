package com.covidien;

import javax.swing.JOptionPane;

import com.covidien.gui.MainWindow;
import com.covidien.util.AgentConfig;
import com.covidien.util.DomXML;

public class RssReMapperLauncher {
    public static String[] serverValue, serverName;
    private static boolean enable;
    private static MainWindow window;

    public static MainWindow getWindow() {
        return window;
    }

    public static void main(String[] args) {
        String[] serverName = {
                "PRODUCTION", "QA", "SANDBOX", "DEVELOPMENT","INTEGRATION-TEST" };
        String[] serverValue = {
                "rss-app.covidien.com:8443", "rssqa-app.covidien.com:8443", "rssqa-app01.covidien.com:8443",
                "rssqa-app02.covidien.com:8443","rssqa-int.covidien.com:8443" };
        DomXML domxml = DomXML.INSTANCE;
        domxml.set(serverName, serverValue);

        RssReMapperLauncher.serverValue = domxml.getServerValue();
        RssReMapperLauncher.serverName = domxml.getServerName();
        RssReMapperLauncher.enable = domxml.getEnableEdit();

        AgentConfig agent = AgentConfig.INSTANCE;
        String agentState = null;

        agentState = agent.getAgentState();

        String serverPath = null;
        if (agentState != null) {
            serverPath = agent.getConfiguredServer();
            if(serverPath == null ) {
                JOptionPane.showMessageDialog(null, "Server path not found");
                return;
            }
        }
        window = new MainWindow(RssReMapperLauncher.serverName, RssReMapperLauncher.serverValue,
                RssReMapperLauncher.enable, serverPath, agentState);
    }
}
