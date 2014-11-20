package com.qunar.wrapper.model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.qunar.wrapper.model.WrapperRequest;

public class WrapperObjectTest {

    @Test
    public void whenCreateNewWrapperTemplateThenGenerateJsonContent() throws Exception {
        // given
        WrapperRequest object = new WrapperRequest();
        object.setActionUrl("http://www.test.com");
        object.setContentType("UTF-8");
        object.setMethod("POST");
        Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("key1", "value1");
        inputs.put("key2", "value2");
        inputs.put("key2", "value3");
        inputs.put("key3", "value4");
        object.setBookingInputs(inputs);

        // then
        MatcherAssert.assertThat(JSON.toJSONString(object), Matchers.notNullValue());
    }

    @Test
    public void whenLoadTemplateJsonFileThenGenerateJsonObject() throws Exception {
        // given
        String jsonFilePath = "template/template.json";

        // when
        URL url = Resources.getResource(jsonFilePath);
        String jsonContent = Resources.toString(url, Charsets.UTF_8);

        // then
        WrapperRequest object = JSON.parseObject(jsonContent, WrapperRequest.class);
        MatcherAssert.assertThat(object, Matchers.notNullValue());

    }
}
