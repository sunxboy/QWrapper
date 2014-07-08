package com.qunar.wrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.BaseFlightInfo;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;

public class Wrapper_gjdaire8001Test {

    String dep = "HKG";
    String arr = "HKT";

    @Test
    public void whenBookingFlightThenCheckBookingInfo() throws Exception {
        Wrapper_gjdaire8001 wrapper = new Wrapper_gjdaire8001();
        // given
        DateTime bookDate = DateTime.now().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        BookingResult result = wrapper.getBookingInfo(searchParam);

        // then
        assertThat(result.isRet(), equalTo(true));

        BookingInfo booking = result.getData();
        assertThat(booking.getAction(), equalTo("http://ashley4.com/webaccess/cityairways/fareresult.php"));
        assertThat(booking.getContentType(), equalTo("text/html;charset=utf-8"));
        assertThat(booking.getMethod(), equalTo("post"));
        Map<String, String> inputs = booking.getInputs();
        assertThat(inputs.get("ro"), equalTo("0"));
        assertThat(inputs.get("from"), equalTo("HKG"));
        assertThat(inputs.get("to"), equalTo("HKT"));
        assertThat(inputs.get("cur"), equalTo("HKD"));
        assertThat(inputs.get("sdate"), equalTo(bookDate.toString("yyyy/MM/dd")));
        assertThat(inputs.get("edate"), equalTo(bookDate.toString("yyyy/MM/dd")));
        assertThat(inputs.get("adult"), equalTo("1"));
        assertThat(inputs.get("child"), equalTo("0"));
        assertThat(inputs.get("infant"), equalTo("0"));
        assertThat(inputs.get("view"), equalTo("0"));
        assertThat(inputs.get("btnsubmit"), equalTo("Flight Search"));
    }

    @Test
    public void whenTicketAvaliableThenCheckBookingResultHTML() throws Exception {
        Wrapper_gjdaire8001 wrapper = new Wrapper_gjdaire8001();
        // given
        DateTime bookDate = DateTime.now().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        String htmlContent = wrapper.getHtml(searchParam);

        // then
        assertThat(htmlContent, containsString("var json = '"));
    }

    @Test
    public void whenNoTicketAvaliableThenCheckBookingResultHTML() throws Exception {
        Wrapper_gjdaire8001 wrapper = new Wrapper_gjdaire8001();
        // given
        DateTime bookDate = DateTime.now().minusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        String htmlContent = wrapper.getHtml(searchParam);

        // then
        assertThat(htmlContent, containsString("Today Flight is full"));
    }

    @Test
    public void whenInvalidAirLineThenCheckBookingResultHTML() throws Exception {
        Wrapper_gjdaire8001 wrapper = new Wrapper_gjdaire8001();
        // given
        DateTime bookDate = DateTime.now().plusDays(3);
        String dep = "HKM";// invalid
        String arr = "HKD";// invalid
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        String htmlContent = wrapper.getHtml(searchParam);

        // then
        assertThat(htmlContent, containsString("Today Flight is full"));
    }

    @Test
    public void whenGotExceptionHTMLThenCheckProcessResult() throws Exception {
        Wrapper_gjdaire8001 wrapper = new Wrapper_gjdaire8001();
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
    public void whenGotNoTicketHTMLThenCheckProcessResult() throws Exception {
        Wrapper_gjdaire8001 wrapper = new Wrapper_gjdaire8001();
        // given
        String html = "Today Flight is full, select an other day or check later for any seat released. ";
        DateTime bookDate = DateTime.now().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        ProcessResultInfo processResult = wrapper.process(html, searchParam);

        // then
        assertThat(processResult.isRet(), equalTo(false));
        assertThat(processResult.getStatus(), equalTo(Constants.INVALID_DATE));
    }

    @Test
    public void whenGetResultHTMLThenCheckProcessResult() throws Exception {
        Wrapper_gjdaire8001 wrapper = new Wrapper_gjdaire8001();
        // given
        File file = new File(getClass().getResource("/gjdaire8001/result.txt").getFile());
        String html = FileUtils.readFileToString(file);
        DateTime bookDate = DateTime.now().withTimeAtStartOfDay().plusDays(2);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        ProcessResultInfo processResult = wrapper.process(html, searchParam);

        // then
        assertThat(processResult.isRet(), equalTo(true));
        assertThat(processResult.getStatus(), equalTo(Constants.SUCCESS));
        List<? extends BaseFlightInfo> flightList = processResult.getData();
        assertThat(flightList, hasSize(1));
        BaseFlightInfo flightInfo = flightList.get(0);
        FlightDetail flightDetail = flightInfo.getDetail();

        assertThat(flightDetail.getDepdate(), equalTo(bookDate.toDate()));
        List<String> flightnos = ImmutableList.of("E8257");
        assertThat(flightDetail.getFlightno(), equalTo(flightnos));
        assertThat(flightDetail.getMonetaryunit(), equalTo("HKD"));
        assertThat(flightDetail.getPrice(), equalTo(1600d));
        assertThat(flightDetail.getDepcity(), equalTo("HKG"));
        assertThat(flightDetail.getArrcity(), equalTo("HKT"));
        assertThat(flightDetail.getWrapperid(), equalTo(null));
        assertThat(flightDetail.getStatus(), equalTo(0));
        assertThat(flightDetail.getTax(), equalTo(0d));

        List<FlightSegement> flightSegements = flightInfo.getInfo();
        assertThat(flightSegements, hasSize(1));
        FlightSegement segements = flightSegements.get(0);
        assertThat(segements.getFlightno(), equalTo("E8257"));
        assertThat(segements.getDepDate(), equalTo(bookDate.toString("yyyy-MM-dd")));
        assertThat(segements.getDepairport(), equalTo("HKG"));
        assertThat(segements.getArrairport(), equalTo("HKT"));
        assertThat(segements.getDeptime(), equalTo("18:45"));
        assertThat(segements.getArrtime(), equalTo("21:30"));
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
