package org.solomonm.traffic.yugo.collect.global.vo;

import lombok.Data;

@Data
public class DmbAccInciVo {
    
    private String inciId;
    private String occurTime;
    private String endExpectTime;
    private long linkId;
    private int roadClass;
    private String conditionCode;
    private String inciTitle;
    private String inciContent;
    private double xgps;
    private double ygps;
    private String roadName;
    
}
