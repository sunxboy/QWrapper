package com.qunar.wrapper.task;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

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

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;

public class Wrapper_gjdairjj001Test {

    private Wrapper_gjdairjj001 wrapper;
    private String dep = "IOS";
    private String arr = "CNF";

    @Before
    public void prepare() {
        wrapper = new Wrapper_gjdairjj001();
    }

    @Test
    public void whenBookingFlightThenCheckBookingInfo() throws Exception {

        // given
        DateTime bookDate = DateTime.now().withTimeAtStartOfDay().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        BookingResult result = wrapper.getBookingInfo(searchParam);

        // then
        assertThat(result.isRet(), equalTo(true));
        BookingInfo info = result.getData();
        assertThat(info.getAction(), equalTo("http://book.tam.com.br/TAM/dyn/air/booking/upslDispatcher"));
        assertThat(info.getContentType(), equalTo("UTF-8"));
        assertThat(info.getMethod(), equalTo("post"));
        Map<String, String> inputs = info.getInputs();
        assertThat(inputs.get("WDS_CORPORATE_SALES"), equalTo("FALSE"));
        assertThat(inputs.get("SO_SITE_ISSUE_TKT_PER_PAX"), equalTo("TRUE"));
        assertThat(inputs.get("FORCE_OVERRIDE"), equalTo("TRUE"));
        assertThat(inputs.get("WDS_DOMAIN_NAME"), equalTo("tam.com.br"));
        assertThat(inputs.get("WDS_MARKET"), equalTo("OC"));
        assertThat(inputs.get("WDS_DISABLE_ATC_CHANGE_ITIN"), equalTo("TRUE"));
        assertThat(inputs.get("WDS_DISABLE_DEVICE_FINGERPRINT_MERCHANT_ID_PER_OID"), equalTo("FALSE"));
        assertThat(inputs.get("WDS_ONLINE_OPINION_EXIT_PERCENT"), equalTo("10"));
        assertThat(inputs.get("WDS_ACI_ENABLED_MARKETS"), equalTo("BR:OP:CO"));
        assertThat(inputs.get("WDS_MARKET_WITH_INSURANCE"), equalTo("BR:OP:CO:PE"));
        assertThat(inputs.get("WDS_DISABLE_ATC_REFUND"), equalTo("TRUE"));
        assertThat(inputs.get("SITE"), equalTo("JJBKJJBK"));
        assertThat(inputs.get("LANGUAGE"), equalTo("GB"));
        assertThat(inputs.get("WDS_MARKET"), equalTo("OC"));
        assertThat(inputs.get("FROM_PAGE"), equalTo("HOMESEARCH"));
        assertThat(inputs.get("B_DATE_1"), equalTo(bookDate.toString("yyyyMMddHHmm")));
        assertThat(inputs.get("B_LOCATION_1"), equalTo(dep));
        assertThat(inputs.get("E_LOCATION_1"), equalTo(arr));
        assertThat(inputs.get("TRIP_TYPE"), equalTo("O"));
        assertThat(inputs.get("adults"), equalTo("1"));
        assertThat(inputs.get("children"), equalTo("0"));
        assertThat(inputs.get("infants"), equalTo("0"));
        assertThat(inputs.get("COMMERCIAL_FARE_FAMILY_1"), equalTo("NEWBUNDLE"));
        assertThat(inputs.get("CORPORATE_CODE_INPUT"), equalTo(""));
        assertThat(inputs.get("SEARCH_COOKIE"), equalTo(""));
        assertThat(inputs.get("Referer"), equalTo("https://book.tam.com.br/TAM/dyn/air/entry"));

    }

    @Test
    public void whenTicketAvaliableThenCheckBookingResultHTML() throws Exception {
        // given
        DateTime bookDate = DateTime.now().withTimeAtStartOfDay().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        String htmlContent = wrapper.getHtml(searchParam);
        System.out.println(htmlContent);
        // then
        assertThat(htmlContent, containsString("var clientSideData ="));
    }

    @Test
    public void whenNoTicketAvaliableThenCheckBookingResultHTML() throws Exception {
        // given
        DateTime bookDate = DateTime.now().withTimeAtStartOfDay().minusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        String htmlContent = wrapper.getHtml(searchParam);

        // then
        assertThat(htmlContent, containsString("\"SERVER_SIDE_ERROR_OCCURED\":\"TRUE\""));
    }

    @Test
    public void whenGotExceptionHTMLThenCheckProcessResult() throws Exception {
        // given
        String html = "Exception";
        DateTime bookDate = DateTime.now().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        ProcessResultInfo processResult = wrapper.process(html, searchParam);

        // then
        assertThat(processResult.isRet(), equalTo(false));
        assertThat(processResult.getStatus(), equalTo(Constants.CONNECTION_FAIL));
    }

    @Test
    public void whenGotNoResultResponseThenCheckProcessResult() throws Exception {
        // given
        String html = "\"Result\":false";
        DateTime bookDate = DateTime.now().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        ProcessResultInfo processResult = wrapper.process(html, searchParam);

        // then
        assertThat(processResult.isRet(), equalTo(true));
        assertThat(processResult.getStatus(), equalTo(Constants.NO_RESULT));
    }

    @Test
    public void whenGetResultHTMLThenCheckProcessResult() throws Exception {

        // given
        DateTime bookDate = DateTime.now().withTimeAtStartOfDay().plusDays(2);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, "CNF", "SAO");

        String html = wrapper.getHtml(searchParam);

        wrapper.process(html, searchParam);
    }

    @Test
    public void whenHaveFlightWithConnectionsThenDealWithConnections() throws Exception {
        String searchDepDate = "2014-07-24";

        URL url = Resources.getResource("regulartest.txt");
        String jsonContent = Resources.toString(url, Charsets.UTF_8);
        String markWithConnections = "Flights with connections";
        jsonContent = StringUtils.substringAfter(jsonContent, markWithConnections);

        if (StringUtils.isEmpty(jsonContent)) {
            return;
        }

        String contentWithConnections =
                StringUtils.substringBetween(jsonContent, "<tbody class=\"none\" data-wdk-toggle-transition=\"none\">", "</tbody> </table>");

        List<OneWayFlightInfo> flightList = Lists.newArrayList();
        Iterable<String> flightDetailsIterable = Splitter.on("<tr class=\"flight flightId-").trimResults().omitEmptyStrings().split(contentWithConnections);

        for (String flightDetailsItem : flightDetailsIterable) {
            List<Map<String, String>> oneWayFlightList = Lists.newArrayList();
            OneWayFlightInfo flight = new OneWayFlightInfo();
            List<FlightSegement> flightSegements = new ArrayList<FlightSegement>();

            Map<String, String> oneSegmentMinPriceTaxPairMap = Maps.newHashMap();
            Iterable<String> segmentDetailsIterable = Splitter.on("<tr class=\"flightNextSegment\"").trimResults().omitEmptyStrings().split(flightDetailsItem);
            for (String segmentDetailsItem : segmentDetailsIterable) {

                Map<String, String> segmentDetailsMap = Maps.newHashMap();
                String segementDetailsMatcherString =
                        "(data-departuredate|data-arrivaldate|data-departureairportcode|data-arrivalairportcode|data-flightnumber)\\s*=\\s*\"([^\"]+)\"";
                Matcher segementMatcher = Pattern.compile(segementDetailsMatcherString).matcher(segmentDetailsItem);
                while (segementMatcher.find()) {
                    segmentDetailsMap.put(segementMatcher.group(1), segementMatcher.group(2));
                }

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
            flightDetail.setDepdate(formatDepDate(searchDepDate));
            flightDetail.setFlightno(flightNumberList);
            flightDetail.setMonetaryunit("USD");
            flightDetail.setPrice(parsePrice(oneSegmentMinPriceTaxPairMap.get("data-cell-price-adt")).doubleValue());
            flightDetail.setTax(parsePrice(oneSegmentMinPriceTaxPairMap.get("data-cell-tax-adt")).doubleValue());

            flight.setDetail(flightDetail);
            flight.setInfo(flightSegements);
            flightList.add(flight);
        }

        System.out.println("--------------------");
        for (OneWayFlightInfo flightInfo : flightList) {
            System.out.println(flightInfo.toString());
        }
    }

    private Map<String, String> calcMinPriceTaxPairMap(String segmentDetailsItem) {
        String[] segmentPriceArray = StringUtils.substringsBetween(segmentDetailsItem, "<td", ">");
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

        Map<String, String> oneSegmentMinPriceTaxPairMap = sortPriceList(flightSegmentList).get(0);
        return oneSegmentMinPriceTaxPairMap;
    }

    @Test
    public void whenHaveDirectFlightThenDealWithDirect() throws Exception {

        String searchDepDate = "2014-07-24";
        URL url = Resources.getResource("regulartest.txt");
        String jsonContent = Resources.toString(url, Charsets.UTF_8);
        String contentWithDirect = StringUtils.substringBetween(jsonContent, "<tbody class=\"none\" data-wdk-toggle-transition=\"none\">", "</tbody> </table>");

        List<Map<String, String>> oneWayFlightList = Lists.newArrayList();
        String[] flightDetailsArray = StringUtils.substringsBetween(contentWithDirect, "<tr", "</tr>");
        // flight item include <tr> attributes and multiple <td>s which include flight details and multiple prices
        for (String flightDetailsItem : flightDetailsArray) {
            // match <tr> attributes which is the flight details
            Map<String, String> oneFlightLineDetailsMap = Maps.newHashMap();
            String flightDetailsmatcherString = "(flightnumber|departuredate|departureairportcode|arrivaldate|arrivalairportcode)\\s*=\\s*\"([^\"]+)\"";
            Matcher flightDetailsMatcher = Pattern.compile(flightDetailsmatcherString).matcher(flightDetailsItem);
            while (flightDetailsMatcher.find()) {
                oneFlightLineDetailsMap.put(flightDetailsMatcher.group(1), flightDetailsMatcher.group(2));
            }

            // match <td>s which include multiple prices, need to figure out the minimum price finally
            String[] flightLinePriceArray = StringUtils.substringsBetween(flightDetailsItem, "<td", ">");
            List<Map<String, String>> flightLineList = Lists.newArrayList();
            for (String priceItem : flightLinePriceArray) {
                String flightLinePriceMatcherString = "(data-cell-price-adt|data-cell-tax-adt)\\s*=\\s*\"([^\"]+)\"";
                Matcher flightLinePriceMatcher = Pattern.compile(flightLinePriceMatcherString).matcher(priceItem);
                Map<String, String> priceTaxPairMap = Maps.newConcurrentMap();
                while (flightLinePriceMatcher.find()) {
                    priceTaxPairMap.put(flightLinePriceMatcher.group(1), flightLinePriceMatcher.group(2));
                }
                flightLineList.add(priceTaxPairMap);
            }
            // sort prices at first, then the first one has the minimum value
            Map<String, String> oneFlightLineMinPriceTaxPairMap = sortPriceList(flightLineList).get(0);
            oneFlightLineDetailsMap.putAll(oneFlightLineMinPriceTaxPairMap);
            oneWayFlightList.add(oneFlightLineDetailsMap);
        }

        List<OneWayFlightInfo> oneWayFilghtInfoResult = Lists.newArrayList();
        for (Map<String, String> oneWayFlightMinPriceDetailsMap : oneWayFlightList) {

            String flightNumber = oneWayFlightMinPriceDetailsMap.get("flightnumber");
            String arrairport = oneWayFlightMinPriceDetailsMap.get("arrivalairportcode");
            String depairport = oneWayFlightMinPriceDetailsMap.get("departureairportcode");
            Date arrivaldate = parseGMTDateString(oneWayFlightMinPriceDetailsMap.get("arrivaldate"));
            Date departuredate = parseGMTDateString(oneWayFlightMinPriceDetailsMap.get("departuredate"));
            String price = oneWayFlightMinPriceDetailsMap.get("data-cell-price-adt");
            String tax = oneWayFlightMinPriceDetailsMap.get("data-cell-tax-adt");

            OneWayFlightInfo flightInfo = new OneWayFlightInfo();
            List<FlightSegement> flightSegements = new ArrayList<FlightSegement>();
            List<String> flightNumberList = new ArrayList<String>();
            flightNumberList.add(flightNumber);
            FlightDetail flightDetail = new FlightDetail();

            flightDetail.setArrcity(arrairport);
            flightDetail.setDepcity(depairport);
            flightDetail.setDepdate(formatDepDate(searchDepDate));
            flightDetail.setFlightno(flightNumberList);
            flightDetail.setMonetaryunit("USD");
            flightDetail.setPrice(parsePrice(price).doubleValue());
            flightDetail.setTax(parsePrice(tax).doubleValue());

            FlightSegement flightSegement = new FlightSegement();
            flightSegement.setArrDate(formatFlightDate(arrivaldate));
            flightSegement.setArrairport(arrairport);
            flightSegement.setArrtime(formatFlightTime(arrivaldate));

            flightSegement.setDepDate(formatFlightDate(departuredate));
            flightSegement.setDepairport(depairport);
            flightSegement.setDeptime(formatFlightTime(departuredate));
            flightSegement.setFlightno(flightNumber);

            flightSegements.add(flightSegement);

            flightInfo.setDetail(flightDetail);
            flightInfo.setInfo(flightSegements);
            oneWayFilghtInfoResult.add(flightInfo);
        }

        System.out.println("--------");
        for (OneWayFlightInfo oneWayFlightInfo : oneWayFilghtInfoResult) {
            System.out.println(oneWayFlightInfo.toString());
        }

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
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
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

                return parsePrice(e1.get("data-cell-price-adt")).compareTo(parsePrice(e2.get("data-cell-price-adt")));
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

    @Test
    public void testSubStringUtils() throws Exception {
        String test = "<tra xxx end <tr xxx end <tra yyy end <tr yyy";
        System.out.println(Splitter.on("<tra").trimResults().omitEmptyStrings().split(test));
    }

    private FlightSearchParam buildFlightSearchParam(DateTime bookDate, String dep, String arr) {
        FlightSearchParam searchParam = new FlightSearchParam();
        searchParam.setDep(dep);
        searchParam.setArr(arr);
        searchParam.setDepDate(bookDate.toString("yyyy-MM-dd"));
        searchParam.setTimeOut("60000");
        searchParam.setToken("");
        return searchParam;
    }
}
