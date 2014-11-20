package com.qunar.wrapper.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class WrapperRequest {

    String actionUrl;
    String contentType;
    String method;
    Map<String, String> bookingInputs = new LinkedHashMap<String, String>();
    String departureCity;
    String arrivalCity;
    String dateFormat;
    String departureDate;

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getBookingInputs() {
        return bookingInputs;
    }

    public void setBookingInputs(Map<String, String> bookingInputs) {
        this.bookingInputs = bookingInputs;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

}
