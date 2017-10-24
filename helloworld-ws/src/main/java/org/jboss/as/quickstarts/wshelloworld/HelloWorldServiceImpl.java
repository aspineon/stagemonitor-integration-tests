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

import org.stagemonitor.core.Stagemonitor;
import org.stagemonitor.tracing.SpanContextInformation;
import org.stagemonitor.tracing.TracingPlugin;
import org.stagemonitor.tracing.utils.SpanUtils;
import org.stagemonitor.tracing.wrapper.SpanWrapper;
import org.stagemonitor.tracing.wrapper.StatelessSpanEventListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import io.opentracing.tag.Tags;

/**
 * The implementation of the HelloWorld JAX-WS Web Service.
 *
 * @author lnewson@redhat.com
 */
@WebService(serviceName = "HelloWorldService", portName = "HelloWorld", name = "HelloWorld", endpointInterface = "org.jboss.as.quickstarts.wshelloworld.HelloWorldService", targetNamespace = "http://www.jboss.org/jbossas/quickstarts/wshelloworld/HelloWorld")
public class HelloWorldServiceImpl implements HelloWorldService {

    private HelloWorldService soapClient;

    private final List<SpanWrapper> reportedSpans = Collections.synchronizedList(new ArrayList<SpanWrapper>());

    @PostConstruct
    public void onPostConstruct() {
        TracingPlugin tracingPlugin = Stagemonitor.getPlugin(TracingPlugin.class);
        tracingPlugin.addSpanEventListenerFactory(new StatelessSpanEventListener() {
            @Override
            public void onFinish(SpanWrapper spanWrapper, String operationName, long durationNanos) {
                reportedSpans.add(spanWrapper);
            }
        });
        QName serviceName = new QName("http://www.jboss.org/jbossas/quickstarts/wshelloworld/HelloWorld", "HelloWorldService");
        try {
            soapClient = Service
                    .create(new URL("http://localhost:8080/jboss-helloworld-ws/HelloWorldService?wsdl"), serviceName)
                    .getPort(HelloWorldService.class);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String sayHello() {
        reportedSpans.clear();
        final SpanContextInformation contextInformation = SpanContextInformation.getCurrent();
        checkState(contextInformation != null, "No current span found");
        checkState(contextInformation.getSpanWrapper().getStringTag(SpanUtils.OPERATION_TYPE).equals("soap"), "Current span is not of type soap");
        checkState(contextInformation.getParent() != null, "No parent span found");
        checkState(contextInformation.getParent().getSpanWrapper().getStringTag(SpanUtils.OPERATION_TYPE).equals("http"), "Current span is not of type http");
        reportedSpans.clear();
        final String result = soapClient.sayHelloToName("World");
        List<String> operationNames = new ArrayList<String>();
        for (SpanWrapper reportedSpan : reportedSpans) {
            operationNames.add(String.format("%s (%s %s)",reportedSpan.getOperationName(), reportedSpan.getStringTag(Tags.SPAN_KIND.getKey()), reportedSpan.getStringTag(SpanUtils.OPERATION_TYPE)));
        }
        final List<String> expectedOperationNames = Arrays.asList("sayHelloToName (client soap)", "POST /HelloWorldService (server http)", "sayHelloToName (server soap)");
        checkState(operationNames.containsAll(expectedOperationNames), "Expected operationNames to contain %s, but got %s", expectedOperationNames, operationNames);
        reportedSpans.clear();
        return result;
    }

    static void checkState(boolean expression,
                                   String errorMessageTemplate,
                                   Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
        }
    }

    @Override
    public String sayHelloToName(final String name) {

        /* Create a list with just the one value */
        final List<String> names = new ArrayList<String>();
        names.add(name);

        return sayHelloToNames(names);
    }

    @Override
    public String sayHelloToNames(final List<String> names) {
        return "Hello " + createNameListString(names);
    }

    /**
     * Creates a list of names separated by commas or an and symbol if its the last separation. This is then used to say
     * hello to the list of names.
     *
     * i.e. if the input was {John, Mary, Luke} the output would be John, Mary & Luke
     *
     * @param names A list of names
     * @return The list of names separated as described above.
     */
    private String createNameListString(final List<String> names) {

        /*
         * If the list is null or empty then assume the call was anonymous.
         */
        if (names == null || names.isEmpty()) {
            return "Anonymous!";
        }

        final StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {

            /*
             * Add the separator if its not the first string or the last separator since that should be an and (&) symbol.
             */
            if (i != 0 && i != names.size() - 1)
                nameBuilder.append(", ");
            else if (i != 0 && i == names.size() - 1)
                nameBuilder.append(" & ");

            nameBuilder.append(names.get(i));
        }

        nameBuilder.append("!");

        return nameBuilder.toString();
    }
}
