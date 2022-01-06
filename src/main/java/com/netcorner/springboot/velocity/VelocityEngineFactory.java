/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netcorner.springboot.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VelocityEngineFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private final Map<String, Object> velocityProperties = new HashMap<String, Object>();

	private String resourceLoaderPath;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private boolean preferFileSystemAccess = true;

	private boolean overrideLogging = true;


	/**
	 * Set the location of the Velocity config file.
	 * Alternatively, you can specify all properties locally.
	 * @see #setVelocityProperties
	 * @see #setResourceLoaderPath
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set Velocity properties, like "file.resource.loader.path".
	 * Can be used to override values in a Velocity config file,
	 * or to specify all necessary properties locally.
	 * <p>Note that the Velocity resource loader path also be set to any
	 * Spring resource location via the "resourceLoaderPath" property.
	 * Setting it here is just necessary when using a non-file-based
	 * resource loader.
	 * @see #setVelocityPropertiesMap
	 * @see #setConfigLocation
	 * @see #setResourceLoaderPath
	 */
	public void setVelocityProperties(Properties velocityProperties) {
		CollectionUtils.mergePropertiesIntoMap(velocityProperties, this.velocityProperties);
	}

	/**
	 * Set Velocity properties as Map, to allow for non-String values
	 * like "ds.resource.loader.instance".
	 * @see #setVelocityProperties
	 */
	public void setVelocityPropertiesMap(Map<String, Object> velocityPropertiesMap) {
		if (velocityPropertiesMap != null) {
			this.velocityProperties.putAll(velocityPropertiesMap);
		}
	}


	public void setResourceLoaderPath(String resourceLoaderPath) {
		this.resourceLoaderPath = resourceLoaderPath;
	}

	/**
	 * Set the Spring ResourceLoader to use for loading Velocity template files.
	 * The default is DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.context.ApplicationContext
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Return the Spring ResourceLoader to use for loading Velocity template files.
	 */
	protected ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * Set whether to prefer file system access for template loading.
	 * File system access enables hot detection of template changes.
	 * <p>If this is enabled, VelocityEngineFactory will try to resolve the
	 * specified "resourceLoaderPath" as file system resource (which will work
	 * for expanded class path resources and ServletContext resources too).
	 * <p>Default is "true". Turn this off to always load via SpringResourceLoader
	 * (i.e. as stream, without hot detection of template changes), which might
	 * be necessary if some of your templates reside in an expanded classes
	 * directory while others reside in jar files.
	 * @see #setResourceLoaderPath
	 */
	public void setPreferFileSystemAccess(boolean preferFileSystemAccess) {
		this.preferFileSystemAccess = preferFileSystemAccess;
	}

	/**
	 * Return whether to prefer file system access for template loading.
	 */
	protected boolean isPreferFileSystemAccess() {
		return this.preferFileSystemAccess;
	}


	public void setOverrideLogging(boolean overrideLogging) {
		this.overrideLogging = overrideLogging;
	}


	/**
	 * Prepare the VelocityEngine instance and return it.
	 * @return the VelocityEngine instance
	 * @throws IOException if the config file wasn't found
	 * @throws VelocityException on Velocity initialization failure
	 */
	public VelocityEngine createVelocityEngine() throws IOException, VelocityException {
		VelocityEngine velocityEngine = newVelocityEngine();
		Map<String, Object> props = new HashMap<String, Object>();

		// Load config file if set.
		if (this.configLocation != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Loading Velocity config from [" + this.configLocation + "]");
			}
			CollectionUtils.mergePropertiesIntoMap(PropertiesLoaderUtils.loadProperties(this.configLocation), props);
		}

		// Merge local properties if set.
		if (!this.velocityProperties.isEmpty()) {
			props.putAll(this.velocityProperties);
		}

		// Set a resource loader path, if required.
		if (this.resourceLoaderPath != null) {
			initVelocityResourceLoader(velocityEngine, this.resourceLoaderPath);
		}

		// Log via Commons Logging?
//		if (this.overrideLogging) {
//			velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new CommonsLogLogChute());
//		}

		// Apply properties to VelocityEngine.
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			velocityEngine.setProperty(entry.getKey(), entry.getValue());
		}

		postProcessVelocityEngine(velocityEngine);

		// Perform actual initialization.
		velocityEngine.init();

		return velocityEngine;
	}

	/**
	 * Return a new VelocityEngine. Subclasses can override this for
	 * custom initialization, or for using a mock object for testing.
	 * <p>Called by {@code createVelocityEngine()}.
	 * @return the VelocityEngine instance
	 * @throws IOException if a config file wasn't found
	 * @throws VelocityException on Velocity initialization failure
	 * @see #createVelocityEngine()
	 */
	protected VelocityEngine newVelocityEngine() throws IOException, VelocityException {
		return new VelocityEngine();
	}

	protected void initVelocityResourceLoader(VelocityEngine velocityEngine, String resourceLoaderPath) {
		if (isPreferFileSystemAccess()) {
			// Try to load via the file system, fall back to SpringResourceLoader
			// (for hot detection of template changes, if possible).
			try {
				StringBuilder resolvedPath = new StringBuilder();
				String[] paths = StringUtils.commaDelimitedListToStringArray(resourceLoaderPath);
				for (int i = 0; i < paths.length; i++) {
					String path = paths[i];
					Resource resource = getResourceLoader().getResource(path);
					File file = resource.getFile();  // will fail if not resolvable in the file system
					if (logger.isDebugEnabled()) {
						logger.debug("Resource loader path [" + path + "] resolved to file [" + file.getAbsolutePath() + "]");
					}
					resolvedPath.append(file.getAbsolutePath());
					if (i < paths.length - 1) {
						resolvedPath.append(',');
					}
				}
				velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
				velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
				velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, resolvedPath.toString());
			}
			catch (IOException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cannot resolve resource loader path [" + resourceLoaderPath +
							"] to [java.io.File]: using SpringResourceLoader", ex);
				}
				initSpringResourceLoader(velocityEngine, resourceLoaderPath);
			}
		}
		else {
			// Always load via SpringResourceLoader
			// (without hot detection of template changes).
			if (logger.isDebugEnabled()) {
				logger.debug("File system access not preferred: using SpringResourceLoader");
			}
			initSpringResourceLoader(velocityEngine, resourceLoaderPath);
		}
	}

	protected void initSpringResourceLoader(VelocityEngine velocityEngine, String resourceLoaderPath) {
		velocityEngine.setProperty(
				RuntimeConstants.RESOURCE_LOADER, SpringResourceLoader.NAME);
		velocityEngine.setProperty(
				SpringResourceLoader.SPRING_RESOURCE_LOADER_CLASS, SpringResourceLoader.class.getName());
		velocityEngine.setProperty(
				SpringResourceLoader.SPRING_RESOURCE_LOADER_CACHE, "true");
		velocityEngine.setApplicationAttribute(
				SpringResourceLoader.SPRING_RESOURCE_LOADER, getResourceLoader());
		velocityEngine.setApplicationAttribute(
				SpringResourceLoader.SPRING_RESOURCE_LOADER_PATH, resourceLoaderPath);
	}

	/**
	 * To be implemented by subclasses that want to perform custom
	 * post-processing of the VelocityEngine after this FactoryBean
	 * performed its default configuration (but before VelocityEngine.init).
	 * <p>Called by {@code createVelocityEngine()}.
	 * @param velocityEngine the current VelocityEngine
	 * @throws IOException if a config file wasn't found
	 * @throws VelocityException on Velocity initialization failure
	 * @see #createVelocityEngine()
	 * @see VelocityEngine#init
	 */
	protected void postProcessVelocityEngine(VelocityEngine velocityEngine)
			throws IOException, VelocityException {
	}

}