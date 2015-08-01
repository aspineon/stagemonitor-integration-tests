package org.springframework.samples.petclinic.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

@WebFilter("/*")
public class HackyCorsFilter implements Filter {

	@Override
//	@MonitorRequests
	public void init(FilterConfig filterConfig) throws ServletException {
		sleep();
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		httpServletResponse.addHeader("X-Frame-Options", "DENY");
		httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
		httpServletResponse.addHeader("Access-Control-Allow-Headers", "Content-Type");
		httpServletResponse.addHeader("Access-Control-Allow-Method", "GET, PUT, POST, DELETE, OPTIONS");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
