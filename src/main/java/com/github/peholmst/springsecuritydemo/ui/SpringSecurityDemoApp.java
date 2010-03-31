/*
 * Copyright 2010 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.peholmst.springsecuritydemo.ui;

import java.util.Locale;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.peholmst.springsecuritydemo.VersionInfo;
import com.vaadin.Application;
import com.vaadin.ui.Window;
import com.vaadin.ui.Component.Event;

/**
 * TODO document me!
 * @author Petter Holmström
 */
@Component("applicationBean")
@Scope(value = "session")
public class SpringSecurityDemoApp extends Application {

	private static final long serialVersionUID = -1412284137848857188L;

	/**
	 * 
	 */
	protected transient final Log logger = LogFactory.getLog(getClass());
	
	@Resource
	private transient MessageSource messages;
	
	@Resource
	private transient AuthenticationManager authenticationManager;
	
	private LoginView loginView;

	private MainView mainView;

	private static final Locale[] SUPPORTED_LOCALES = { Locale.US,
			new Locale("fi", "FI"), new Locale("sv", "SE") };

	private static final String[] LOCALE_NAMES = { "English", "Suomi", "Svenska" };

	@Override
	public Locale getLocale() {
		/*
		 * Fetch the locale resolved by Spring in the application servlet
		 */
		return LocaleContextHolder.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale);
	}

	@SuppressWarnings("serial")
	@Override
	public void init() {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing application [" + this + "]");
		}
		// Create the views
		loginView = new LoginView(this, authenticationManager);
		loginView.setSizeFull();
		mainView = new MainView(this);
		mainView.setSizeFull();

		setTheme("SpringSecurityDemo"); // We use a custom theme
		
		final Window loginWindow = new Window(getMessage("app.title", getVersion()),
				loginView);
		setMainWindow(loginWindow);
		
		loginView.addListener(new com.vaadin.ui.Component.Listener() {
			@Override
			public void componentEvent(Event event) {
				if (event instanceof LoginView.LoginEvent) {
					if (logger.isDebugEnabled()) {
						logger.debug("User logged on [" + ((LoginView.LoginEvent) event).getAuthentication() + "]");
					}
					/*
					 * A user has logged on, which means we can ditch the login view
					 * and open the main view instead. We also have to update the security context holder.
					 */
					SecurityContextHolder.getContext().setAuthentication(((LoginView.LoginEvent) event).getAuthentication());
					removeWindow(loginWindow);
					setMainWindow(new Window(getMessage("app.title", getVersion()), mainView));					
				}
			}
		});		
	}

	@Override
	@PreDestroy
	public void close() {
		if (logger.isDebugEnabled()) {
			logger.debug("Closing application [" + this + "]");
		}
		// Clear the security context to log the user out
		SecurityContextHolder.clearContext();
		super.close();
	}

	@Override
	public Object getUser() {
		// Get the user object form Spring Security
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	@Override
	public String getVersion() {
		return VersionInfo.getApplicationVersion();
	}

	/**
	 * TODO Document me!
	 * @param code
	 * @param args
	 * @return
	 */
	public String getMessage(String code, Object... args) {
		return messages.getMessage(code, args, getLocale());
	}
	
	/**
	 * TODO Document me!
	 * @return
	 */
	public Locale[] getSupportedLocales() {
		return SUPPORTED_LOCALES;
	}
	
	/**
	 * TODO Document me!
	 * 
	 * @param locale
	 * @return
	 */
	public String getLocaleDisplayName(Locale locale) {
		for (int i = 0; i < SUPPORTED_LOCALES.length; i++) {
			if (locale.equals(SUPPORTED_LOCALES[i])) {
				return LOCALE_NAMES[i];
			}
		}
		return "Unsupported Locale";
	}
}