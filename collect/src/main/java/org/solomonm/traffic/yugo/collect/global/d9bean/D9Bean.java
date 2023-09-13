package org.solomonm.traffic.yugo.collect.global.d9bean;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class D9Bean {
    
    private Hashtable HTREQ = null;

    private Hashtable HTRES = null;

    private HashMap HMREQ = null;

    private HashMap HMRES = null;

    private String CLASSID = null;

    private String METHODID = null;

    private String ERRMSG = null;

    private String ERRCODE = null;

    protected static int HASH_TYPE = 0;

    protected static int HMAP_TYPE = 1;

    protected static int ERROR_TYPE = 2;

    public Object execute(String action) {
        Object oResult = null;

        try {
            Method method = getMethod(action);
            oResult = method.invoke(this, null);
        } catch (IllegalAccessException e) {
            setException(e);
        } catch (InvocationTargetException e) {
            setException(e);
        } catch (Throwable e) {
            setException(e);
        }

        destory();

        return oResult;
    }

    private void destory() {}

    protected Method getMethod(String methodNm) {
        if (methodNm == null || methodNm.trim().equals(""))
            throw new IllegalArgumentException("method name is not a valid string");
        
            StringBuffer idb = new StringBuffer(methodNm);
        String name = idb.toString().trim();
        Method m = getMethod(getClass(), name);
        
        if (m == null)
            throw new IllegalArgumentException("can not find method named '" + name + "' ");

        return m;
    }

    private static Method getMethod(Class src, String name) {
        Method[] meths = src.getMethods();
        for (int i = 0; i < meths.length; i++) {
            if (meths[i].getName().equals(name))
                return meths[i];
        }

        return null;
    }

    public void setException(Throwable e) {
        this.ERRMSG = e.getMessage();
        this.ERRCODE = "-9999";
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log.error(e.getMessage());
        log.debug(sw.getBuffer().toString());
    }

    public static String executeTimeLog(long startTime, long finishTime) {
        return "execute " + ((finishTime - startTime) / 1000000L) + "ms";
    }

    public static String catchLog(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer().toString();
    }

    public Hashtable getHTREQ() {
        return this.HTREQ;
    }

    public void setHTREQ(Hashtable hTREQ) {
        this.HTREQ = hTREQ;
    }

    public Hashtable getHTRES() {
        return this.HTRES;
    }

    public void setHTRES(Hashtable hTRES) {
        this.HTRES = hTRES;
    }

    public HashMap getHMREQ() {
        return this.HMREQ;
    }

    public void setHMREQ(HashMap hMREQ) {
        this.HMREQ = hMREQ;
    }

    public HashMap getHMRES() {
        return this.HMRES;
    }

    public void setHMRES(HashMap hMRES) {
        this.HMRES = hMRES;
    }

    public String getCLASSID() {
        return this.CLASSID;
    }

    public void setCLASSID(String cLASSID) {
        this.CLASSID = cLASSID;
    }

    public String getMETHODID() {
        return this.METHODID;
    }

    public void setMETHODID(String mETHODID) {
        this.METHODID = mETHODID;
    }

    public String getERRMSG() {
        return this.ERRMSG;
    }

    public void setERRMSG(String eRRMSG) {
        this.ERRMSG = eRRMSG;
    }

    public String getERRCODE() {
        return this.ERRCODE;
    }

    public void setERRCODE(String eRRCODE) {
        this.ERRCODE = eRRCODE;
    }
}
