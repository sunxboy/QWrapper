package com.qunar.wrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;

public class Wrapper_gjdairjj001Test {

    private Wrapper_gjdairjj001 wrapper = new Wrapper_gjdairjj001();
    private String dep = "BSB";
    private String arr = "RIO";

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
        assertThat(inputs.get("COMMERCIAL_FARE_FAMILY_1"), equalTo("JJINTECO"));
        assertThat(inputs.get("CORPORATE_CODE_INPUT"), equalTo(""));
        assertThat(inputs.get("SEARCH_COOKIE"), equalTo(""));
        assertThat(inputs.get("Referer"), equalTo("http://book.tam.com.br/TAM/dyn/air/homeSearch"));

    }

    @Test
    public void whenTicketAvaliableThenCheckBookingResultHTML() throws Exception {
        // given
        DateTime bookDate = DateTime.now().withTimeAtStartOfDay().plusDays(3);
        FlightSearchParam searchParam = buildFlightSearchParam(bookDate, dep, arr);

        // when
        String htmlContent = wrapper.getHtml(searchParam);

        // then
        assertThat(htmlContent, equalTo("var clientSideData ="));

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
