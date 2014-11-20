package com.qunar.wrapper.model;

import java.util.Map;

import com.google.common.collect.Maps;

public class WrapperResponse {
    String responseException;
    String responseNoResult;
    String resultStart;
    String resultEnd;
    Map<String, String> dealWithDirect = Maps.newHashMap();
    Map<String, String> dealWithConnections = Maps.newHashMap();
    Map<String, String> flightNo = Maps.newHashMap();
    Map<String, Map<String, String>> flightSegement = Maps.newHashMap();
    Map<String, Map<String, String>> flightDetail = Maps.newHashMap();

    public String getResponseException() {
        return responseException;
    }

    public void setResponseException(String responseException) {
        this.responseException = responseException;
    }

    public String getResponseNoResult() {
        return responseNoResult;
    }

    public void setResponseNoResult(String responseNoResult) {
        this.responseNoResult = responseNoResult;
    }

    public String getResultStart() {
        return resultStart;
    }

    public void setResultStart(String resultStart) {
        this.resultStart = resultStart;
    }

    public String getResultEnd() {
        return resultEnd;
    }

    public void setResultEnd(String resultEnd) {
        this.resultEnd = resultEnd;
    }

    public Map<String, String> getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(Map<String, String> flightNo) {
        this.flightNo = flightNo;
    }

    public Map<String, Map<String, String>> getFlightSegement() {
        return flightSegement;
    }

    public void setFlightSegement(Map<String, Map<String, String>> flightSegement) {
        this.flightSegement = flightSegement;
    }

    public Map<String, Map<String, String>> getFlightDetail() {
        return flightDetail;
    }

    public void setFlightDetail(Map<String, Map<String, String>> flightDetail) {
        this.flightDetail = flightDetail;
    }

    public Map<String, String> getDealWithConnections() {
        return dealWithConnections;
    }

    public void setDealWithConnections(Map<String, String> dealWithConnections) {
        this.dealWithConnections = dealWithConnections;
    }

    public Map<String, String> getDealWithDirect() {
        return dealWithDirect;
    }

    public void setDealWithDirect(Map<String, String> dealWithDirect) {
        this.dealWithDirect = dealWithDirect;
    }

}
