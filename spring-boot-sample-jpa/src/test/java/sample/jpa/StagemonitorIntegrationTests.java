/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.jpa;


import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.stagemonitor.core.Stagemonitor;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleJpaApplication.class)
@WebIntegrationTest
public class StagemonitorIntegrationTests {

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	private HttpClient httpClient;

	@BeforeClass
	public static void initStagemonitor() {
		// the earlier stagemonitor is initialized the less files have to be retransformend, which is a expensive operation
		Stagemonitor.init();
	}

	@Before
	public void setUp() throws Exception {
		httpClient = HttpClientBuilder.create().setDefaultHeaders(Arrays.asList(new BasicHeader("Accept", "text/html"))).build();
		this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	public void testHome() throws Exception {
		this.mvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(xpath("//tbody/tr").nodeCount(4));
	}

	@Test
	public void testStagemonitorIframeIsInjected() throws Exception {
		final String page = httpClient.execute(new HttpGet("http://localhost:" + this.port), new BasicResponseHandler());
		assertTrue(page, page.contains("<iframe id=\"stagemonitor-modal\""));
	}

	@Test
	public void testStagemonitorSqlQueriesAreCollected() throws Exception {
		final String requestTrace = getRequestTrace(httpClient.execute(new HttpGet("http://localhost:" + this.port), new BasicResponseHandler()));
		assertTrue(requestTrace, requestTrace.contains("JpaNoteRepository.findAll"));
		assertTrue(requestTrace, requestTrace.contains("select note0_.id as id1_0_, note0_.body as body2_0_, note0_.title as title3_0_ from note note0_"));
	}

	private String getRequestTrace(String htmlPage) {
		Matcher m = Pattern.compile("data = (.*),").matcher(htmlPage);
		assertTrue(htmlPage, m.find());
		return m.group(1);
	}

}
