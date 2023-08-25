package com.github.dactiv.framework.spring.web;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Undertow 配置
 *
 * @author maurice.chen
 */
// FIXME 该类应该判断是否用了 Undertow 在启用配置信息。
@Configuration
public class UndertowServletWebServerFactoryConfiguration implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    private final SpringWebMvcProperties properties;

    public UndertowServletWebServerFactoryConfiguration(SpringWebMvcProperties properties) {
        this.properties = properties;
    }

    @Override
    public void customize(UndertowServletWebServerFactory factory) {

        factory.addDeploymentInfoCustomizers(deploymentInfo -> {

            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(false, properties.getWebSocketDeploymentBuffers()));

            deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
        });
    }

}
