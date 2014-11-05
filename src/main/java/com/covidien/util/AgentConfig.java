package com.covidien.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import com.covidien.RssReMapperLauncher;

public class AgentConfig {
    private static final Logger logger = Utility.getLogger(AgentConfig.class);
    private String agentCmdPath;
    private File configFile;
    private String serverPath;

    private ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    private static final String[] SERVICES = {
            "DeviceManagementAgent", "GatewayAgent", "LaptopAgent" };
    public static final AgentConfig INSTANCE = new AgentConfig();
    private boolean hasAdministratorPermission = false;
    private static String SERVICE_NAME;

    private AgentConfig() {
        try {
            SERVICE_NAME = testServiceName();
            prepareAgentEvn();
            hasAdministratorPermission = checkPermission();
            if (!hasAdministratorPermission) {
                JOptionPane
                        .showMessageDialog(
                                null,
                                "This tool requires the administrator privilege,\nplease re-launch it using 'Run As Administrator'.",
                                "Warnning", JOptionPane.WARNING_MESSAGE);

            }
            // watch the configure file
            es.scheduleWithFixedDelay(new Runnable() {
                private long lastModified = configFile.lastModified();

                public void run() {
                    long lm = configFile.lastModified();
                    if (lm != lastModified) {
                        reloadConfig();
                    }
                }
            }, 30, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "The installation of Agent is not found, please install the Agent first.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public void stopMonitor() {
        es.shutdownNow();
    }

    public boolean hasAdministratorPermission() {
        return hasAdministratorPermission;
    }

    public String getAgentState() {
        return getAgentState(SERVICE_NAME);
    }

    public static String getAgentState(String service) {
        String cmd = new String("sc query " + service);
        String agentState = "ERROR";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader inBr = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                String ns = lineStr.trim();
                if (ns.indexOf("STATE") == 0) {
                    if (ns.indexOf("STOPPED") > -1) {
                        agentState = "STOPPED";
                    } else if (ns.indexOf("RUNNING") > -1) {
                        agentState = "RUNNING";
                    } else if (ns.indexOf("START_PENDING") > -1) {
                        agentState = "START_PENDING";
                    } else if (ns.indexOf("STOP_PENDING") > -1) {
                        agentState = "STOP_PENDING";
                    } else {
                        agentState = "ERROR";
                    }
                    break;
                }
            }
        } catch (IOException e) {

        }
        return agentState;
    }

    private static String testServiceName() {
        for (String name : SERVICES) {
            String sn = getAgentState(name);
            if (!sn.equals("ERROR")) {
                return name;
            }
        }
        throw new NullPointerException("the agent is not found.");
    }

    private void prepareAgentEvn()
        throws IOException {
        String cmd = new String("sc qc " + SERVICE_NAME);
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader inBr = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String lineStr;
        Pattern px = Pattern.compile(".+?:\\s*[\\\"]*(.+?)[\\\"]*\\s+//.*");
        while ((lineStr = inBr.readLine()) != null) {
            String ns = lineStr.trim();
            if (ns.indexOf("BINARY_PATH_NAME") == 0) {
                Matcher ma = px.matcher(ns);
                if (ma.find()) {
                    File f = new File(ma.group(1));
                    agentCmdPath = f.getParentFile().getParent() + "\\";
                } else {
                    agentCmdPath = "";
                }
                configFile = new File(agentCmdPath + "resource\\conf.properties");
                break;
            }
        }
        if (configFile == null) {
            throw new IOException("The configure file is not found!");
        }
    }

    public String getAgentRoot() {
        return agentCmdPath;
    }

    private void reloadConfig() {
            String url = getConfiguredServer();
            RssReMapperLauncher.getWindow().serverURLChanged(url);
    }

    public String getConfiguredServer() {
        Properties p = null;
        try{
            p = LoadProperties.loadEncryptedProperties(configFile.getAbsolutePath());
        } catch (EncryptionOperationNotPossibleException eonpe) {
            p = LoadProperties.loadProperties(configFile.getAbsolutePath());
        }
        if (p == null || p.isEmpty()) {
            logger.error("Errors happen when loading agent configuration file");
        }
        return p.getProperty("SERVER.WEBSERVICE.URL");
    }

    private boolean checkPermission() {
        try {
            Process p = Runtime.getRuntime().exec("whoami /groups");
            BufferedReader inBr = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = inBr.readLine()) != null) {
                if (line.indexOf(" S-1-5-32-544 ") > 0 && line.indexOf("Enabled group") > 0) {
                    inBr.close();
                    return true;
                }
            }
        } catch (IOException e) {
            // suit to low version windows OS.
            return checkPermissionForLowVersion();
        }
        return false;
    }

    private boolean checkPermissionForLowVersion() {
        try {
            Process process = Runtime.getRuntime().exec("net localgroup administrators");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String lineArray[] = line.replace("\\", "@#").split("@#");
                String tmp = lineArray.length > 1 ? lineArray[1] : lineArray[lineArray.length - 1];
                if (tmp.equals(System.getProperty("user.name"))) {
                    br.close();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setServerPath(String serverPath)
        throws IOException {
        BufferedReader reader = null;
        reader = new BufferedReader(new FileReader(configFile));
        StringBuffer buf = new StringBuffer();
        String line, sp = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            if (line.trim().indexOf(" ") != -1) {
                line = line.trim();
            }
            if (line.startsWith("SERVER.WEBSERVICE.URL")) {
                buf = buf.append("SERVER.WEBSERVICE.URL=" + serverPath);
            } else {
                buf = buf.append(line);
            }
            buf = buf.append(sp);
        }
        reader.close();
        FileOutputStream fos = new FileOutputStream(configFile);
        PrintWriter pw = new PrintWriter(fos);
        pw.write(buf.toString().toCharArray());
        pw.flush();
        pw.close();
    }

    public boolean restartAgent()
        throws IOException, InterruptedException {
        String cmd1 = "net restart " + SERVICE_NAME;
        Process p = Runtime.getRuntime().exec(cmd1);
        while (!"RUNNING".equals(getAgentState())) {
            Thread.sleep(3000);
        }
        //p = Runtime.getRuntime().exec(cmd1);
        int flag=p.waitFor();
        return flag == 1;
    }

}