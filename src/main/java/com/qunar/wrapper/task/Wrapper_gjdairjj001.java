package com.qunar.wrapper.task;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.io.Resources;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import com.qunar.wrapper.model.WrapperRequest;
import com.qunar.wrapper.model.WrapperResponse;
import com.travelco.rdf.infocenter.InfoCenter;

public class Wrapper_gjdairjj001 implements QunarCrawler {

    private WrapperRequest wrapper = JSON.parseObject(readJson("gjdairjj001/request.json"), WrapperRequest.class);
    private WrapperResponse response = JSON.parseObject(readJson("gjdairjj001/response.json"), WrapperResponse.class);

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

        bookingInfo.setContentType(wrapper.getContentType());
        bookingInfo.setInputs(map);
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return bookingResult;

    }

    private void fillBookingInputMap(FlightSearchParam arg0) {
        Map<String, String> map = wrapper.getBookingInputs();
        String data = arg0.getDepDate().replaceAll("-", "") + "0000";
        map.put("B_DATE_1", data);
        map.put("B_LOCATION_1", arg0.getDep());
        map.put("E_LOCATION_1", arg0.getArr());

        String depcountry = InfoCenter.getCountry2CodeFromNameZh(InfoCenter.getCountryFromCity(InfoCenter.getCityFromAirportCode(arg0.getDep()), "zh"));
        String arrcountry = InfoCenter.getCountry2CodeFromNameZh(InfoCenter.getCountryFromCity(InfoCenter.getCityFromAirportCode(arg0.getArr()), "zh"));

        String COMMERCIAL_FARE_FAMILY = "JJINTECO";
        if (depcountry.equalsIgnoreCase("BR") && arrcountry.equalsIgnoreCase("BR")) {
            COMMERCIAL_FARE_FAMILY = "NEWBUNDLE";
        }
        map.put("COMMERCIAL_FARE_FAMILY_1", COMMERCIAL_FARE_FAMILY);
        wrapper.setBookingInputs(map);
    }

    public String getHtml(FlightSearchParam arg0) {

        QFPostMethod post = null;
        try {
            // get all query parameters from the url set by wrapperSearchInterface
            QFHttpClient httpClient = new QFHttpClient(arg0, false);
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

            post = new QFPostMethod(wrapper.getActionUrl());
            fillBookingInputMap(arg0);

            Map<String, String> inputMaps = wrapper.getBookingInputs();
            Map<String, NameValuePair> pairMap = Maps.transformEntries(inputMaps, new EntryTransformer<String, String, NameValuePair>() {
                @Override
                public NameValuePair transformEntry(String key, String value) {
                    return new NameValuePair(key, value);
                }
            });
            ;
            post.setRequestBody(Iterables.toArray(pairMap.values(), NameValuePair.class));
            post.setRequestHeader("Referer", inputMaps.get("Referer"));
            post.getParams().setContentCharset(wrapper.getContentType());

            httpClient.executeMethod(post);
            return post.getResponseBodyAsString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return "Exception";
    }

    private static String[] getValues(String source, String st, String end) {
        String target = "";
        int a, b;
        while (true) {
            a = source.indexOf(st);
            if (a == -1)
                break;
            b = source.indexOf(end, a + st.length());
            if (b == -1)
                break;
            target += source.substring(a + st.length(), b) + "##@@##";
            source = source.substring(b);
        }
        return target.split("##@@##");
    }

    public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {
        String html = arg0;

        ProcessResultInfo result = new ProcessResultInfo();
        if (response.getResponseException().equals(html)) {
            result.setRet(false);
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }
        if (html.contains(response.getResponseNoResult())) {
            result.setRet(true);
            result.setStatus(Constants.NO_RESULT);
            return result;
        }

        String moneyUnit = getMoneyUnit(html);
        List<OneWayFlightInfo> flightList = Lists.newArrayList();

        try {
            Date depDate = formatDepDate(arg1.getDepDate());
            String markWithDirection = response.getDealWithDirect().get("check");
            if (hasFlightWithDirection(html, markWithDirection)) {
                String startContentWithDirect = response.getDealWithDirect().get("start");
                String endContentWithDirect = response.getDealWithDirect().get("end");
                String directContent = StringUtils.substringBetween(html, startContentWithDirect, endContentWithDirect);
                String[] flightDetailsWithDirectArray = StringUtils.substringsBetween(directContent, "<tr", "</tr>");
                // include <tr> attributes and multiple <td>s which include flight details and multiple prices
                for (String flightWithDirectDetailsItem : flightDetailsWithDirectArray) {
                    // match <tr> attributes which is the flight details, only price is left
                    Map<String, String> flightWithDirectionDetailsMap = Maps.newHashMap();
                    String flightDetailsmatcherString = "(flightnumber|departuredate|departureairportcode|arrivaldate|arrivalairportcode)\\s*=\\s*\"([^\"]+)\"";
                    Matcher flightDetailsMatcher = Pattern.compile(flightDetailsmatcherString).matcher(flightWithDirectDetailsItem);
                    while (flightDetailsMatcher.find()) {
                        flightWithDirectionDetailsMap.put(flightDetailsMatcher.group(1), flightDetailsMatcher.group(2));
                    }

                    List<FlightSegement> flightSegements = buildFlightSegements(flightWithDirectionDetailsMap);

                    Map<String, String> oneFlightLineMinPriceTaxPairMap = calcMinPriceTaxPairMap(flightWithDirectDetailsItem);
                    String price = oneFlightLineMinPriceTaxPairMap.get("data-cell-price-adt");
                    String tax = oneFlightLineMinPriceTaxPairMap.get("data-cell-tax-adt");
                    FlightDetail flightDetail = buildDirectFlightDetails(moneyUnit, depDate, flightSegements, price, tax);
                    OneWayFlightInfo flightInfo = new OneWayFlightInfo();
                    flightInfo.setDetail(flightDetail);
                    flightInfo.setInfo(flightSegements);
                    flightList.add(flightInfo);
                }
            }

            String markWithConnections = response.getDealWithConnections().get("check");
            String jsonContentWithConnections = StringUtils.substringAfter(html, markWithConnections);
            if (hasFlightWithConnections(jsonContentWithConnections)) {
                String startWithConnections = response.getDealWithConnections().get("start");
                String endWithConnections = response.getDealWithConnections().get("end");
                String contentWithConnections = StringUtils.substringBetween(jsonContentWithConnections, startWithConnections, endWithConnections);
                Iterable<String> flightDetailsIterable =
                        Splitter.on("<tr class=\"flight flightId-").trimResults().omitEmptyStrings().split(contentWithConnections);
                for (String flightDetailsItem : flightDetailsIterable) {

                    List<Map<String, String>> oneWayFlightList = Lists.newArrayList();
                    OneWayFlightInfo flight = new OneWayFlightInfo();
                    List<FlightSegement> flightSegements = new ArrayList<FlightSegement>();

                    Map<String, String> oneSegmentMinPriceTaxPairMap = Maps.newHashMap();
                    Iterable<String> segmentDetailsIterable =
                            Splitter.on("<tr class=\"flightNextSegment\"").trimResults().omitEmptyStrings().split(flightDetailsItem);
                    for (String segmentDetailsItem : segmentDetailsIterable) {

                        Map<String, String> segmentDetailsMap = Maps.newHashMap();
                        String segementDetailsMatcherString =
                                "(data-departuredate|data-arrivaldate|data-departureairportcode|data-arrivalairportcode|data-flightnumber)\\s*=\\s*\"([^\"]+)\"";
                        Matcher segementMatcher = Pattern.compile(segementDetailsMatcherString).matcher(segmentDetailsItem);
                        while (segementMatcher.find()) {
                            segmentDetailsMap.put(segementMatcher.group(1), segementMatcher.group(2));
                        }

                        List<FlightSegement> flightSegements = buildFlightSegements(segmentDetailsMap);

                        // TODO
                        Map<String, String> minPriceTaxPairMap = calcMinPriceTaxPairMap(segmentDetailsItem);
                        oneSegmentMinPriceTaxPairMap.putAll(minPriceTaxPairMap);

                        oneWayFlightList.add(segmentDetailsMap);
                    }

                    List<String> flightNumberList = Lists.newArrayList();
                    for (Map<String, String> oneWayFlightMinPriceDetailsMap : oneWayFlightList) {
                        String flightNumber = oneWayFlightMinPriceDetailsMap.get("data-flightnumber");
                        String arrairport = oneWayFlightMinPriceDetailsMap.get("data-arrivalairportcode");
                        String depairport = oneWayFlightMinPriceDetailsMap.get("data-departureairportcode");
                        Date arrivaldate = parseGMTDateString(oneWayFlightMinPriceDetailsMap.get("data-arrivaldate"));
                        Date departuredate = parseGMTDateString(oneWayFlightMinPriceDetailsMap.get("data-departuredate"));

                        flightNumberList.add(flightNumber);

                        FlightSegement flightSegement = new FlightSegement();
                        flightSegement.setArrDate(formatFlightDate(arrivaldate));
                        flightSegement.setArrairport(arrairport);
                        flightSegement.setArrtime(formatFlightTime(arrivaldate));
                        flightSegement.setDepDate(formatFlightDate(departuredate));
                        flightSegement.setDepairport(depairport);
                        flightSegement.setDeptime(formatFlightTime(departuredate));
                        flightSegement.setFlightno(flightNumber);

                        flightSegements.add(flightSegement);
                    }

                    FlightDetail flightDetail = new FlightDetail();
                    FlightSegement firstSegement = flightSegements.get(0);
                    FlightSegement lastSegement = flightSegements.get(flightSegements.size() - 1);

                    flightDetail.setDepcity(firstSegement.getDepairport());
                    flightDetail.setArrcity(lastSegement.getArrairport());
                    flightDetail.setDepdate(depDate);
                    flightDetail.setFlightno(flightNumberList);
                    flightDetail.setMonetaryunit(moneyUnit);
                    flightDetail.setPrice(parsePrice(oneSegmentMinPriceTaxPairMap.get("data-cell-price-adt")).doubleValue());
                    flightDetail.setTax(parsePrice(oneSegmentMinPriceTaxPairMap.get("data-cell-tax-adt")).doubleValue());

                    flight.setDetail(flightDetail);
                    flight.setInfo(flightSegements);
                    flightList.add(flight);
                }
            }

            result.setRet(true);
            result.setStatus(Constants.SUCCESS);
            result.setData(flightList);
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            result.setRet(false);
            result.setStatus(Constants.PARSING_FAIL);
            return result;
        }
    }

    private boolean hasFlightWithConnections(String jsonContentWithConnections) {
        return !Strings.isNullOrEmpty(jsonContentWithConnections);
    }

    private FlightDetail buildDirectFlightDetails(String moneyUnit, Date depDate, List<FlightSegement> flightSegements, String price, String tax) {

        if (!flightSegements.isEmpty()) {
            FlightSegement flightSegement = flightSegements.get(0);
            FlightDetail flightDetail = new FlightDetail();
            flightDetail.setArrcity(flightSegement.getArrairport());
            flightDetail.setDepcity(flightSegement.getDepairport());
            flightDetail.setDepdate(depDate);
            flightDetail.setFlightno(Lists.newArrayList(flightSegement.getFlightno()));
            flightDetail.setMonetaryunit(moneyUnit);

            // String price = oneFlightLineMinPriceTaxPairMap.get("data-cell-price-adt");
            // String tax = oneFlightLineMinPriceTaxPairMap.get("data-cell-tax-adt");
            flightDetail.setPrice(parsePrice(price).doubleValue());
            flightDetail.setTax(parsePrice(tax).doubleValue());
            return flightDetail;
        }
        return null;
    }

    private List<FlightSegement> buildFlightSegements(Map<String, String> flightWithDirectionDetailsMap) throws ParseException {
        List<FlightSegement> flightSegements = Lists.newArrayList();
        if (!flightWithDirectionDetailsMap.isEmpty()) {
            String flightNumber = flightWithDirectionDetailsMap.get("flightnumber");
            String arrairport = flightWithDirectionDetailsMap.get("arrivalairportcode");
            String depairport = flightWithDirectionDetailsMap.get("departureairportcode");
            Date arrivaldate = parseGMTDateString(flightWithDirectionDetailsMap.get("arrivaldate"));
            Date departuredate = parseGMTDateString(flightWithDirectionDetailsMap.get("departuredate"));

            FlightSegement flightSegement = new FlightSegement();
            flightSegement.setArrDate(formatFlightDate(arrivaldate));
            flightSegement.setArrairport(arrairport);
            flightSegement.setArrtime(formatFlightTime(arrivaldate));

            flightSegement.setDepDate(formatFlightDate(departuredate));
            flightSegement.setDepairport(depairport);
            flightSegement.setDeptime(formatFlightTime(departuredate));
            flightSegement.setFlightno(flightNumber);
            flightSegements.add(flightSegement);
        }
        return flightSegements;
    }

    private boolean hasFlightWithDirection(String html, String markWithDirection) {
        return html.indexOf(markWithDirection) > 0;
    }

    private String getMoneyUnit(String html) {
        String checkMonetaryUnitString = response.getFlightDetail().get("monetaryunit").get("check");
        String monetaryUnitDefaultString = response.getFlightDetail().get("monetaryunit").get("default");
        String monetaryUnitString = response.getFlightDetail().get("monetaryunit").get("unit");
        String moneyUnit = hasFlightWithDirection(html, checkMonetaryUnitString) ? monetaryUnitString : monetaryUnitDefaultString;
        return moneyUnit;
    }

    private Date parseGMTDateString(String dateString) throws ParseException {
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.UK);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = df.parse(dateString);
        return date;
    }

    private String formatFlightTime(Date flightDate) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(flightDate);
    }

    private String formatFlightDate(Date flightDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(flightDate);
    }

    private Date formatDepDate(String depDateString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        // format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = format.parse(depDateString);
        return date;
    }

    private List<Map<String, String>> sortPriceList(List<Map<String, String>> priceList) {
        Predicate<Map<String, String>> notEmptyMapPredicate = new Predicate<Map<String, String>>() {
            @Override
            public boolean apply(Map<String, String> map) {
                return (map.size() > 0) && StringUtils.isNotEmpty(map.get("data-cell-price-adt"));
            }
        };

        List<Map<String, String>> newPriceList = Lists.newArrayList(Collections2.filter(priceList, notEmptyMapPredicate));
        Collections.sort(newPriceList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> e1, Map<String, String> e2) {
                return ComparisonChain.start().compare(parsePrice(e1.get("data-cell-price-adt")), parsePrice(e2.get("data-cell-price-adt"))).result();
            }
        });

        return newPriceList;

    }

    private BigDecimal parsePrice(String price) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        // symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        String pattern = "#.##";
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);

        // parse the string
        BigDecimal bigDecimal = null;
        try {
            bigDecimal = (BigDecimal) decimalFormat.parse(price);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return bigDecimal;
    }

    private Map<String, String> calcMinPriceTaxPairMap(String flightDetailsItem) {
        String[] segmentPriceArray = StringUtils.substringsBetween(flightDetailsItem, "<td", ">");
        List<Map<String, String>> flightSegmentList = Lists.newArrayList();
        for (String priceItem : segmentPriceArray) {
            String segmentPriceMatcherString = "(data-cell-price-adt|data-cell-tax-adt)\\s*=\\s*\"([^\"]+)\"";
            Matcher segmentPriceMatcher = Pattern.compile(segmentPriceMatcherString).matcher(priceItem);
            Map<String, String> priceTaxPairMap = Maps.newHashMap();
            while (segmentPriceMatcher.find()) {
                priceTaxPairMap.put(segmentPriceMatcher.group(1), segmentPriceMatcher.group(2));
            }

            if (!priceTaxPairMap.isEmpty()) {
                flightSegmentList.add(priceTaxPairMap);
            }
        }

        if (flightSegmentList.isEmpty()) {
            return Maps.newHashMap();
        }

        Map<String, String> minPriceTaxPairMap = sortPriceList(flightSegmentList).get(0);
        return minPriceTaxPairMap;
    }
}
