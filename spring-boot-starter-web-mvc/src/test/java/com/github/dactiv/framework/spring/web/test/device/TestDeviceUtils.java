package com.github.dactiv.framework.spring.web.test.device;

import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import nl.basjes.parse.useragent.UserAgent;
import org.junit.jupiter.api.Test;

public class TestDeviceUtils {

    @Test
    public void testGetDevice() {
        UserAgent agent = DeviceUtils.getDevice("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11");
        System.out.println(agent.toString());
        for (String fieldName : agent.getAvailableFieldNamesSorted()) {
            System.out.println(fieldName + " = " + agent.getValue(fieldName));
        }
    }
}
