package org.solomonm.traffic.yugo.collect.global.util;

public class PacketUtil {
    
    private final static String TWO_BYTE_STRING = "！〃＃＆＇（）＊＋，－．／０１２３４５６７８９：；〈＝〉ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ［￦］ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ　ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩ";
    private final static String[] CONVERT_ONE_BYTE = {"!","\"","#","&","'","(",")","*","+",",","-",".","/","0","1","2","3","4","5","6","7","8","9",":",";","<","=",">","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","[","\\","]","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","","I","II","III","IV","V","VI","VII","VIII","IX","X"};

    public String damit(String fuck) {
        int length = fuck.length();
        StringBuffer returnSB = new StringBuffer();

        for (int i = 0; i < length; i++) {
            int index = TWO_BYTE_STRING.indexOf(fuck.substring(i, i + 1));

            if (index != -1) {
                returnSB.append(CONVERT_ONE_BYTE[index]);
            } else {
                returnSB.append(fuck.substring(i, i + 1));
            }
        }

        return returnSB.toString();
    }

    public String fillZero(String str, int length) {
        int dif = length = str.length();
        String ReturnVal = "";
        
        if (dif > 0) {
            for (int i = 0; i < dif; i++) {
                str = "0" + str;
            }
        } else {
            str = str.substring(0, length);
        }

        ReturnVal = str;

        return ReturnVal;
    }

    public String fillZero2(String str, String param, int length) {
        int strLength = (str == null) ? 0 : str.length();
        int dif = length - strLength;
        String ReturnVal = "";

        if (dif > 0) {
            for (int i = 0; i < dif; i++) {
                str = param + str;
            }
        } else {
            str = str.substring(0, length);
        }

        ReturnVal = str;

        return ReturnVal;
    }

    public int CheckSum(String msg) {
        int xor = 0;
        int intMsgLength = msg.length() / 2;

        for (int i = 0; i < intMsgLength; i++) {
            String strTemp = msg.substring(0, 2);
            msg = msg.substring(2);
            xor ^= Integer.parseInt(strTemp, 16);
        }

        return xor;
    }

    public String HexToString(String strHex) {
        String ReturnVal = "";
        int j = strHex.length() / 2;

        for (int i = 0; i < j; i++) {
            String TempMessage = strHex.substring(0, 2);
            strHex = strHex.substring(2);
            int intVal = Integer.parseInt(TempMessage, 16);

            if (intVal > 127) {
                i++;
                String Temp = Integer.toHexString(intVal) + strHex.substring(0, 2);
                strHex = strHex.substring(2);
                intVal = Integer.parseInt(Temp, 16);
            }

            char charVal = (char) intVal;
            ReturnVal += charVal;
        }

        return ReturnVal;
    }

    public String StringToHexString(String str) {
        String ReturnVal = "";

        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            String s4 = Integer.toHexString(ch);

            ReturnVal += s4;
        }

        return ReturnVal;
    }

    public String AppendSpace(String str, int len) {
        while (str.length() < len) str = str + "20";
        return str;
    }

    public String AppendZero(String str, int len) {
        while (str.length() < len) str = str + "0";
        return str;
    }

    public String AppendZero2(String str, int len) {
        while (str.length() < len) str = str + "0";
        return str.substring(0, len);
    }

    public String StringToHex(String str) {
        String ReturnVal = "";

        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            String s4 = Integer.toHexString(ch);

            ReturnVal += s4;
        }

        return ReturnVal;
    }
}
