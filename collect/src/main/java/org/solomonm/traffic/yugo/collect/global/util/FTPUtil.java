package org.solomonm.traffic.yugo.collect.global.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FTPUtil {
    
    FTPClient ftpClient = null;

    public FTPUtil(String host, int port, String user, String pswd) throws Exception {
        int reply = 0;
        ftpClient = new FTPClient();

        ftpClient.setDefaultPort(port);
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        ftpClient.connect(host, port);

        reply = ftpClient.getReplyCode();
        log.info("Work Start!!! " + host); //기존192.169.1.18
        
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new Exception("FTP Server Refused Connection.");
        }
        
        if(!ftpClient.login(user, pswd)) {
            ftpClient.logout();
            throw new Exception("FTP Failed to Login in to Server.");
        }

        log.info("FTP Connect Success [Connected to " + host + " on " + ftpClient.getRemotePort() + "]");

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
    }

    public void uploadFile(String localFileFullName, String fileName, String hostDir) throws Exception {
        ftpClient.changeWorkingDirectory(hostDir);

        try (InputStream input = new FileInputStream(new File(localFileFullName))) {
            this.ftpClient.storeFile(hostDir + fileName, input);
        }
    }

    public void downloadFile(String localDir, String fileName, String hostDir) throws Exception {
        ftpClient.changeWorkingDirectory(hostDir);

        try (FileOutputStream fos = new FileOutputStream(new File(localDir, fileName))) {
            this.ftpClient.retrieveFile(hostDir + fileName, fos);
        }
    }

    public void disconnect() {
        if (this.ftpClient.isConnected()) {
            try {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
            } catch (IOException f) {
                log.error("disconnect Exception Message", f);
            } 
        }
    }
}
