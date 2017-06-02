package sample.jdbc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.stagemonitor.core.Stagemonitor;
import org.stagemonitor.web.servlet.ServletPlugin;

@SpringBootApplication
public class SampleJdbcApplication implements EmbeddedServletContainerCustomizer {

	public static void main(String[] args) throws Exception {
		Stagemonitor.init();
		SpringApplication.run(SampleJdbcApplication.class, args);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		container.addInitializers(new ServletContextInitializer() {
			@Override
			public void onStartup(ServletContext servletContext) throws ServletException {
				new ServletPlugin().onStartup(null, servletContext);
			}
		});
	}

}
