package com.sm.common.util;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 配置文件读取类
 * @author shenmiao
 */
public class PropertiesReader extends PropertyPlaceholderConfigurer {
	private static Properties props;

	public Properties mergeProperties() throws IOException {
		props = super.mergeProperties();
		return props;
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
}
