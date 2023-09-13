package org.solomonm.traffic.yugo.collect.global.d9bean;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PacketRule extends D9Bean {
    
    String content;

    static Document xmldoc;

    Element methodElement;

    long lastupdated;

    public static final String remarksAll = "#";

    public static final String remarksFrom = "//";

    public static final String CRLF = "\r\n";

    public void setSentence(String content) {
        this.content = content;
        this.lastupdated = System.currentTimeMillis();
    }

    public String getSentence() {
        return this.content;
    }

    public static PacketRule getInstance(String file) throws Exception {
        InputStream in = load(file);

        if (in == null) {
            log.error("잘못된 Resource명[" + file + "]입니다");
            throw new IOException("잘못된 Resource명[" + file + "]입니다");
        }

        PacketRule ret = getInstance(in, file.toLowerCase().endsWith(".xml"));
        
        in.close();

        return ret;
    }

    public static Document getInstance(Class<?> cls, String file) {
        try {
            InputStream in = load(cls, file);
            
            if (in == null) {
                log.error("잘못된 Resource명[" + file + "]입니다");
                throw new IOException("잘못된 Resource명[" + file + "]입니다");
            }

            PacketRule ret = getInstance(in, file.toLowerCase().endsWith(".xml"));

            in.close();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.getBuffer().toString());
        }

        return xmldoc;
    }

    private static synchronized PacketRule getInstance(InputStream in, boolean xml) throws Exception {
        PacketRule ret = new PacketRule();
        String content = readAll(in, " ");
        ret.setSentence(content);

        if (xml)
            ret.parse(new ByteArrayInputStream(content.getBytes()));

        return ret;
    }

    private synchronized void parse(InputStream in) throws Exception {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setValidating(false);
        documentFactory.setNamespaceAware(false);
        DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
        xmldoc = docBuilder.parse(in);

        if (xmldoc == null) {
            in.close();
            log.error("Invalid inputstream has no valid xml resource");
            throw new IOException("Invalid inputstream has no valid xml resource");
        }
    }

    public static String xmlToString(Node node) {
        String strResult = "";

        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("method", "xml");
            transformer.setOutputProperty("encoding", "euc-kr");
            transformer.transform(source, result);
            strResult = stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return strResult;
    }

    public String toString() {
        try {
            return "Contents:{" + xmlToString(this.methodElement) + "}";
        } catch (Exception e) {
            e.printStackTrace();
            return "Contents:{}";
        }
    }

    public static InputStream load(String file) throws IOException {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(file);
    }

    public static InputStream load(Class cls, String file) throws IOException {
        return cls.getClassLoader().getResourceAsStream(file);
    }

    public static String readAll(InputStream in, String cr) throws IOException {
        if (cr == null)
            cr = "\r\n";

        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        String line;

        while ((line = bin.readLine()) != null) {
            line = line.trim();

            if (line.startsWith("#"))
                continue;

            int pos = line.indexOf("//");

            if (pos == 0)
                continue;

            if (pos != -1)
                line = line.substring(0, pos);

            sbuf.append(line);
            sbuf.append(cr);
        }

        return sbuf.toString();
    }
}
