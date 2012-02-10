package com.silverpeas.tags.navigation.filters;

import java.io.IOException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Redirect on page on user-agent conditions.
 * @author svuillet
 */
public class BrowserFilter implements Filter {
	
	private final static Logger LOGGER = Logger.getLogger(BrowserFilter.class);

	// Be default, support all moderns browsers
	private static final String[] DEFAULT_BROWSERS = { "Chrome", "Firefox", "Safari", "Opera", "MSIE 9", "MSIE 8" };
	
	// Configured params
	private String[] browserIds;
	private String badBrowserUrl;

	// Filter param keys
	public static final String KEY_BROWSER_IDS = "browserIds";
	public static final String KEY_BAD_BROWSER_URL = "badBrowserUrl";

	@Override
	public void destroy() {
		browserIds = null;
		badBrowserUrl = null;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {		
		String userAgent = ((HttpServletRequest) req).getHeader("User-Agent");
		if (userAgent != null) {
			for (String browser_id : browserIds) {
				if (userAgent.toLowerCase().contains(browser_id.toLowerCase())) {
					chain.doFilter(req, resp);
					return;
				}
			}
			// Unsupported browser
			String url = ((HttpServletRequest) req).getRequestURL().toString();
			if (url.contains(this.badBrowserUrl) || url.toLowerCase().endsWith(".jpg")
					|| url.toLowerCase().endsWith(".png") || url.toLowerCase().endsWith(".gif") 
					|| url.toLowerCase().endsWith(".ico") || url.toLowerCase().endsWith(".css") || url.toLowerCase().endsWith(".js")) {
				chain.doFilter(req, resp);
				return;
			}
			LOGGER.info("Badbrowser ! User-agent : " + userAgent);
			((HttpServletResponse) resp).sendRedirect(((HttpServletRequest) req).getContextPath() + badBrowserUrl);
		} else {
			// No filter for robots
			chain.doFilter(req, resp);
		}		
	}

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		URL conf = BrowserFilter.class.getClassLoader().getResource("log4j.properties");	
		if (conf != null) PropertyConfigurator.configure(conf);
		String ids = cfg.getInitParameter(KEY_BROWSER_IDS);
		this.browserIds = (ids != null) ? ids.split(",") : DEFAULT_BROWSERS;
		for (int i = 0; i < browserIds.length; i++) {
			browserIds[i] = browserIds[i].trim();			
		}
		
		badBrowserUrl = cfg.getInitParameter(KEY_BAD_BROWSER_URL);
		if (badBrowserUrl == null) {
			throw new IllegalArgumentException("BrowserFilter requires param badBrowserUrl");
		}
	}
}
