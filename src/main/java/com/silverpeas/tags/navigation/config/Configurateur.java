package com.silverpeas.tags.navigation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Permet de configurer la connexion avec les éléments présents dans le fichier website.properties
 *
 * @author jvi
 */
public class Configurateur {

		/**
		 * The controller configuration
		 */
		private static Properties conf = null;

		private static final String APP_CONFIG_FILENAME = "website.properties";

		private static final String BUNDLE_FILE_NAME = "resources";
		private static Hashtable<Locale,ResourceBundle> bundles = new Hashtable<Locale,ResourceBundle>();

		static {
			InputStream configFile = Configurateur.class.getClassLoader().getResourceAsStream(APP_CONFIG_FILENAME);
			if (configFile == null) {
				throw new RuntimeException("Impossible de trouver le fichier de configuration principal");
			} else {
				try {
					conf = new Properties();
					conf.load(configFile);
				} catch (IOException e) {
					throw new RuntimeException("Impossible de charger le fichier de configuration principal");
				}
			}

		}

		public static String getConfigValue(String key) {
			return conf.getProperty(key);
		}

		public static int getConfigIntValue(String key) {
			return Integer.valueOf(conf.getProperty(key)).intValue();
		}

		public static String getBundle(String language, String key) {
			Locale locale= new Locale(language);
			if (!bundles.containsKey(locale)) {
				bundles.put(locale, ResourceBundle.getBundle(BUNDLE_FILE_NAME, locale));
			}
			return bundles.get(locale).getString(key);
		}
	}

