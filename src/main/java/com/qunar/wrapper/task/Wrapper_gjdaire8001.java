package com.qunar.wrapper.task;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.Resources;
import com.google.common.primitives.Doubles;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.wrapper.model.WrapperResponse;
import com.qunar.wrapper.model.WrapperRequest;

public class Wrapper_gjdaire8001 implements QunarCrawler {

    private WrapperRequest wrapper = JSON.parseObject(readJson("gjdaire8001/request.json"), WrapperRequest.class);
    private WrapperResponse response = JSON.parseObject(readJson("gjdaire8001/response.json"), WrapperResponse.class);

    private String readJson(String jsonFilePath) {
        String jsonContent = "";

        URL url = Resources.getResource(jsonFilePath);
        try {
            jsonContent = Resources.toString(url, Charsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return jsonContent;
    }

    public BookingResult getBookingInfo(FlightSearchParam arg0) {

        BookingResult bookingResult = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction(wrapper.getActionUrl());
        bookingInfo.setMethod(wrapper.getMethod());
        fillBookingInputMap(arg0);
        Map<String, String> map = wrapper.getBookingInputs();
        bookingInfo.setInputs(map);
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return bookingResult;

    }

    private void fillBookingInputMap(FlightSearchParam arg0) {
        Map<String, String> map = wrapper.getBookingInputs();
        map.put("from", arg0.getDep());
        map.put("to", arg0.getArr());
        map.put("sdate", arg0.getDepDate().replaceAll("-", "/"));
        wrapper.setBookingInputs(map);
    }

    public String getHtml(FlightSearchParam arg0) {
        QFGetMethod get = null;
        try {
            QFHttpClient httpClient = new QFHttpClient(arg0, false);

            fillBookingInputMap(arg0);
            Map<String, String> inputs = wrapper.getBookingInputs();

            String getUrl = wrapper.getActionUrl() + "?" + Joiner.on("&").withKeyValueSeparator("=").join(inputs);
            get = new QFGetMethod(getUrl);

            httpClient.executeMethod(get);
            return get.getResponseBodyAsString();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != get) {
                get.releaseConnection();
            }
        }
        return "Exception";
    }

    public ProcessResultInfo process(String resultHtml, FlightSearchParam arg1) {
        String html = resultHtml;
        ProcessResultInfo result = new ProcessResultInfo();
        if (response.getResponseException().equals(html)) {
            result.setRet(false);
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }
        if (html.contains(response.getResponseNoResult())) {
            result.setRet(false);
            result.setStatus(Constants.INVALID_DATE);
            return result;
        }

        String jsonStr = org.apache.commons.lang.StringUtils.substringBetween(html, response.getResultStart(), response.getResultEnd());
        try {
            List<OneWayFlightInfo> flightList = Lists.newArrayList();
            JSONArray ajson = JSON.parseArray(jsonStr);

            for (int i = 0; i < ajson.size(); i++) {
                JSONObject ojson = ajson.getJSONObject(i);
                String flightNo = ojson.getString(response.getFlightNo().get("value")).replaceAll("[^a-zA-Z\\d]", "");
                FlightDetail flightDetail = buildFlightDetails(arg1, ojson, flightNo);

                List<FlightSegement> flightSegements = new ArrayList<FlightSegement>();
                FlightSegement flightSegement = buildSegement(ojson, flightNo);
                flightSegements.add(flightSegement);

                OneWayFlightInfo baseFlight = new OneWayFlightInfo();
                baseFlight.setDetail(flightDetail);
                baseFlight.setInfo(flightSegements);
                flightList.add(baseFlight);
            }
            result.setRet(true);
            result.setStatus(Constants.SUCCESS);
            result.setData(flightList);
            return result;
        }
        catch (Exception e) {
            result.setRet(false);
            result.setStatus(Constants.PARSING_FAIL);
            return result;
        }
    }

    private FlightDetail buildFlightDetails(FlightSearchParam arg1, JSONObject ojson, String flightNo) {
        String depDateString = response.getFlightDetail().get("depdate").get("value");
        final String priceString = response.getFlightDetail().get("price").get("value");
        String monetaryUnitString = response.getFlightDetail().get("monetaryunit").get("value");

        JSONArray classArray = ojson.getJSONArray("class");
        Ordering<JSONObject> jsonObjectOrderFunc = new Ordering<JSONObject>() {
            @Override
            public int compare(JSONObject left, JSONObject right) {
                return Doubles.compare(left.getDoubleValue(priceString), right.getDoubleValue(priceString));
            }
        };
        JSONObject[] jsonArray = classArray.toArray(new JSONObject[classArray.size()]);
        JSONObject minPriceObject = jsonObjectOrderFunc.min(Lists.newArrayList(jsonArray));

        FlightDetail flightDetail = new FlightDetail();
        flightDetail.setDepdate(ojson.getDate(depDateString));
        flightDetail.setFlightno(Lists.newArrayList(flightNo));
        flightDetail.setMonetaryunit(minPriceObject.getString(monetaryUnitString));
        flightDetail.setPrice(minPriceObject.getDoubleValue(priceString));
        flightDetail.setDepcity(arg1.getDep());
        flightDetail.setArrcity(arg1.getArr());
        flightDetail.setWrapperid(arg1.getWrapperid());
        return flightDetail;
    }

    private FlightSegement buildSegement(JSONObject ojson, String flightNo) {
        String depDateString = response.getFlightSegement().get("depDate").get("value");
        String depAirportString = response.getFlightSegement().get("depairport").get("value");
        String arrAirportString = response.getFlightSegement().get("arrairport").get("value");
        String depTimeString = response.getFlightSegement().get("deptime").get("value");
        String arrTimeString = response.getFlightSegement().get("arrtime").get("value");

        FlightSegement seg = new FlightSegement();
        seg.setFlightno(flightNo);
        seg.setDepDate(ojson.getString(depDateString));
        seg.setDepairport(ojson.getString(depAirportString));
        seg.setArrairport(ojson.getString(arrAirportString));
        seg.setDeptime(ojson.getString(depTimeString));
        seg.setArrtime(ojson.getString(arrTimeString));
        return seg;
    }

}
