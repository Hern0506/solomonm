package org.solomonm.traffic.yugo.collect.scheduler.police.parse;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.transform.TransformerException;

// import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.solomonm.traffic.yugo.collect.global.d9bean.PacketRule;
import org.solomonm.traffic.yugo.collect.global.util.PacketUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// import com.nts.PacketRule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PacketParser extends PacketRule {

    // static Logger log = Logger.getLogger(PacketParser.class);
    
    public Hashtable<String, String> readPacketIn(HashMap<String, String> str, String rcvMessage) {
        Hashtable<String, String> ht = new Hashtable<String, String>();

        try {
            ht = readINCI(str, rcvMessage);
        } catch (Exception e) {
            log.error("readPacketIn Exception Message", e);
        }

        return ht;
    }

    private Hashtable<String, String> readINCI(HashMap<String, String> str, String Rcv) throws UnsupportedEncodingException {
        Hashtable<String, String> ReturnHt = new Hashtable<String, String>();
        PacketUtil pu = new PacketUtil();
        Document pDoc = null;

        try {
            String file = "org/solomonm/traffic/yugo/collect/scheduler/police/parse/Packet.xml";
            pDoc = getInstance(getClass(), file);
            NodeList nodeList = null;

            String strXPath = "/packetdefinition/packet[@name='INCI_INFO']/field";
            nodeList = XPathAPI.selectNodeList(pDoc, strXPath);

            int nodeLength = nodeList.getLength();

            for (int i = 0; i < nodeLength; i++) {
                Element elem = (Element) nodeList.item(i);
                int fieldLength = Integer.parseInt(elem.getAttribute("length")) * 2;
                String fieldType = elem.getAttribute("type");

                if (Rcv.length() >= fieldLength) {
                    String TempMessage = Rcv.substring(0, fieldLength);
                    Rcv = Rcv.substring(fieldLength);
                    String ElValue = new String("");

                    if (fieldType.equals("Hex")) {
                        ElValue = TempMessage;
                        log.debug("fieldName(" + elem.getAttribute("name") + "), length(" + fieldLength / 2 + "), value(" + TempMessage + ") => Hex(" + ElValue + ")");
                    } else if (fieldType.equals("String")) {
                        ElValue = pu.HexToString(TempMessage).trim();
                        log.debug("fieldName(" + elem.getAttribute("name") + "), length(" + fieldLength / 2 + "), value(" + TempMessage + ") => String(" + ElValue + ")");
                    } else if (fieldType.equals("Loop")) {
                        ElValue = Integer.toString(Integer.parseInt(TempMessage, 16));
                        log.debug("fieldName(" + elem.getAttribute("name") + "), length(" + fieldLength / 2 + "), value(" + TempMessage + ") => Loop(" + ElValue + ")");
                        ReturnHt.put(elem.getAttribute("name"), ElValue);

                        int loopCnt = Integer.parseInt(ElValue);
                        int childCnt = Integer.parseInt(elem.getAttribute("childCnt"));

                        String childName = null;

                        for (int j = 1; j <= loopCnt; j++) {
                            for (int k = 1; k <= childCnt; k++) {
                                ElValue = "";
                                int childLen = 0;
                                childName = elem.getAttribute("childName" + k);
                                childLen = Integer.parseInt(elem.getAttribute("childLength" + k)) * 2;
                                String childType = elem.getAttribute("childType" + k);

                                TempMessage = Rcv.substring(0, childLen);
                                Rcv = Rcv.substring(childLen);

                                if (childType.equals("Hex")) {
                                    ElValue = Long.toString(Long.parseLong(TempMessage, 16));
                                    log.debug("fieldName(" + elem.getAttribute("childName" + k) + j + "), length(" + childLen / 2 + "), value(" + TempMessage + ") => Hex(" + ElValue + ")");
                                } else if (childType.equals("Lsb")) {
                                    int lsbMes = Integer.parseInt(TempMessage, 16);

                                    int binary2 = 2;
                                    int binaryResult = 1;

                                    if (ElValue.equals("1")) {
                                        ElValue = "b1";
                                    } else {
                                        for (int br = 1; br <= 15; br++) {
                                            binaryResult = binaryResult * binary2;
                                            if (lsbMes == binaryResult) {
                                                ElValue = "b" + br;
                                            }
                                        }
                                    }

                                    log.debug("fieldName(" + elem.getAttribute("childName" + k) + j + "), length(" + childLen / 2 + "), value(" + TempMessage + ") => Hex(" + ElValue + ")");
                                } else if (childType.equals("Map")) {
                                    double mapValue = (Integer.parseInt(TempMessage, 16)) / (double) 10000000;
                                    ElValue = Double.toString(mapValue);
                                    log.debug("fieldName(" + elem.getAttribute("childName" + k) + j + "), length(" + childLen / 2 + "), value(" + TempMessage + ") => Hex(" + ElValue + ")");
                                } else if (childType.equals("Char")) {
                                    ElValue = pu.HexToString(TempMessage).trim();
                                    log.debug("fieldName(" + elem.getAttribute("childName" + k) + j + "), length(" + childLen / 2 + "), value(" + TempMessage + ") => String(" + ElValue + ")");
                                } else if (childType.equals("String")) {
                                    ElValue = str.get(childName + j);
                                    log.debug("fieldName(" + elem.getAttribute("childName" + k) + j + "), length(" + childLen / 2 + "), value(" + TempMessage + ") => String(" + ElValue + ")");
                                } else {
                                    log.debug("childType : [" + childType + "] is not defined [" + TempMessage + "], " + Rcv);
                                }

                                ReturnHt.put(elem.getAttribute("childName" + k) + j, ElValue);
                            }
                        }

                        continue;
                    } else {
                        log.debug("[" + fieldType + "] is not defined [" + TempMessage + "], " + Rcv);
                    }

                    ReturnHt.put(elem.getAttribute("name"), ElValue);
                }
            }
        } catch (TransformerException e) {
            log.error("readINCI TransformerException Message", e);
        }

        return ReturnHt;
    }

    public HashMap<String, String> parseStr(ByteBuffer inBuf) {
        HashMap<String, String> rtnList = new HashMap<String, String>();

        inBuf.clear();
        inBuf.order(ByteOrder.LITTLE_ENDIAN);

        Document pDoc = null;

        PacketUtil pU = new PacketUtil();

        try {
            String file = "org/solomonm/traffic/yugo/collect/scheduler/police/parse/Packet.xml";
            pDoc = getInstance(getClass(), file);
            NodeList nodeList = null;

            String strXPath = "/packetdefinition/packet[@name='INCI_INFO_KR']/field";
            nodeList = XPathAPI.selectNodeList(pDoc, strXPath);

            int nodeLength = nodeList.getLength();

            Element elem = null;

            byte[] fieldArr = null;
            String fieldType = null;
            int fieldLength;

            String HexTo = null;
            String byString = null;

            for (int j = 0; j < nodeLength; j++) {
                elem = (Element) nodeList.item(j);
                fieldType = elem.getAttribute("type");
                fieldLength = Integer.parseInt(elem.getAttribute("length"));

                if ("Loop".equals(fieldType)) {
                    fieldArr = new byte[fieldLength];
                    inBuf.get(fieldArr);
                    HexTo = pU.StringToHexString(new String(fieldArr));
                    byString = Integer.toString(Integer.parseInt(HexTo.toUpperCase(), 16));

                    int childLength = 0;
                    int loopCnt = Integer.parseInt(byString);
                    int clidCnt = Integer.parseInt(elem.getAttribute("childCnt"));

                    String childName = new String("");
                    String childType = new String("");

                    for (int i = 1; i <= loopCnt; i++) {
                        for (int l = 1; l <= clidCnt; l++) {
                            childName = elem.getAttribute("childName" + l);
                            childType = elem.getAttribute("childType" + l);
                            childLength = Integer.parseInt(elem.getAttribute("childLength" + l));

                            if ("Hex".equals(childType)) {
                                fieldArr = new byte[childLength];
                                inBuf.get(fieldArr);
                            } else if ("String".equals(childType)) {
                                fieldArr = new byte[childLength];
                                inBuf.get(fieldArr);
                                byString = new String(fieldArr, "euc-kr").trim();
                                rtnList.put(childName + i, byString);
                                log.debug("fieldName(" + elem.getAttribute("childName" + l) + i + "), length(" + childLength + ") => String(" + byString + ")");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("parseStr Exception Message", e);
        }

        return rtnList;
    }
}
