/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.wshelloworld;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stagemonitor.core.Stagemonitor;
import org.stagemonitor.tracing.TracingPlugin;
import org.stagemonitor.tracing.soap.TracingClientSOAPHandler;
import org.stagemonitor.tracing.wrapper.SpanWrapper;
import org.stagemonitor.tracing.wrapper.StatelessSpanEventListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple set of tests for the HelloWorld Web Service to demonstrate accessing the web service using a client
 * 
 * @author lnewson@redhat.com
 */
public class ClientTest {
    /**
     * The name of the WAR Archive that will be used by Arquillian to deploy the application.
     */
    private static final String APP_NAME = "jboss-helloworld-ws";
    /**
     * The path of the WSDL endpoint in relation to the deployed web application.
     */
    private static final String WSDL_PATH = "HelloWorldService?wsdl";
    
    /**
     * The name for the Server URL System Property.
     */
    private static final String SERVER_URL_PROPERTY = "serverUrl";
    /**
     * The Default Server URL if one isn't specified as a System Property
     */
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080/";

    private static URL deploymentUrl;

    private HelloWorldService client;

    private List<SpanWrapper> reportedSpans;

    @BeforeClass
    public static void beforeClass() throws MalformedURLException
    {
        String deploymentUrl = System.getProperty(SERVER_URL_PROPERTY);
        
        // Check that the server URL property was set. If it wasn't then use the default.
        if (deploymentUrl == null || deploymentUrl.isEmpty()) {
            deploymentUrl = DEFAULT_SERVER_URL;
        }
        
        // Ensure that the URL ends with a forward slash
        if (!deploymentUrl.endsWith("/")) {
            deploymentUrl += "/";
        }
        
        // Ensure the App Name is specified in the URL
        if (!deploymentUrl.matches(".*" + APP_NAME + ".*"))
        {
            deploymentUrl += APP_NAME + "/";
        }
        
        // Add the WDSL Document location to the URL
        deploymentUrl += WSDL_PATH;
        
        System.out.println("WSDL Deployment URL: " + deploymentUrl);
        
        // Set the deployment url
        ClientTest.deploymentUrl = new URL(deploymentUrl);
    }

    @Before
    public void setup() {
        Stagemonitor.init();
        reportedSpans = new ArrayList<SpanWrapper>();
        TracingPlugin tracingPlugin = Stagemonitor.getPlugin(TracingPlugin.class);
        tracingPlugin.addSpanEventListenerFactory(new StatelessSpanEventListener() {
            @Override
            public void onFinish(SpanWrapper spanWrapper, String operationName, long durationNanos) {
                reportedSpans.add(spanWrapper);
            }
        });
        try {
            QName serviceName = new QName("http://www.jboss.org/jbossas/quickstarts/wshelloworld/HelloWorld", "HelloWorldService");
            Service service = Service.create(new URL(deploymentUrl, WSDL_PATH), serviceName);
            client = service.getPort(HelloWorldService.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSoapHandlerRegistered() throws Exception {
        final BindingProvider bindingProvider = (BindingProvider) client;
        boolean clientHandlerFound = false;
        final List<Handler> handlerChain = bindingProvider.getBinding().getHandlerChain();
        for (Handler handler : handlerChain) {
            if (handler instanceof TracingClientSOAPHandler) {
                clientHandlerFound = true;
            }
        }
        assertTrue(handlerChain.toString(), clientHandlerFound);
    }

    @Test
    public void testHello() throws Exception {
        System.out.println("[Client] Requesting the WebService to say Hello.");

        // Get a response from the WebService
        final String response = client.sayHello();
        assertEquals(response, "Hello World!");

        System.out.println("[WebService] " + response);
    }

}
