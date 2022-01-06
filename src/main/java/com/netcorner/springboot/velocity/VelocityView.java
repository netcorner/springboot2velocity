

package com.netcorner.springboot.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.NestedIOException;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

public class VelocityView extends AbstractTemplateView {

	private Map<String, Class<?>> toolAttributes;

	private String dateToolAttribute;

	private String numberToolAttribute;

	private String encoding;

	private boolean cacheTemplate = false;

	private VelocityEngine velocityEngine;

	private Template template;


	public void setToolAttributes(Map<String, Class<?>> toolAttributes) {
		this.toolAttributes = toolAttributes;
	}


	public void setDateToolAttribute(String dateToolAttribute) {
		this.dateToolAttribute = dateToolAttribute;
	}


	public void setNumberToolAttribute(String numberToolAttribute) {
		this.numberToolAttribute = numberToolAttribute;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	protected String getEncoding() {
		return this.encoding;
	}


	public void setCacheTemplate(boolean cacheTemplate) {
		this.cacheTemplate = cacheTemplate;
	}


	protected boolean isCacheTemplate() {
		return this.cacheTemplate;
	}


	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}


	protected VelocityEngine getVelocityEngine() {
		return this.velocityEngine;
	}


	@Override
	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();

		if (getVelocityEngine() == null) {
			// No explicit VelocityEngine: try to autodetect one.
			setVelocityEngine(autodetectVelocityEngine());
		}
	}


	protected VelocityEngine autodetectVelocityEngine() throws BeansException {
		try {
			VelocityConfig velocityConfig = BeanFactoryUtils.beanOfTypeIncludingAncestors(
					getApplicationContext(), VelocityConfig.class, true, false);
			return velocityConfig.getVelocityEngine();
		}
		catch (NoSuchBeanDefinitionException ex) {
			throw new ApplicationContextException(
					"Must define a single VelocityConfig bean in this web application context " +
					"(may be inherited): VelocityConfigurer is the usual implementation. " +
					"This bean may be given any name.", ex);
		}
	}


	@Override
	public boolean checkResource(Locale locale) throws Exception {
		try {
			// Check that we can get the template, even if we might subsequently get it again.
			this.template = getTemplate(getUrl());
			return true;
		}
		catch (ResourceNotFoundException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("No Velocity view found for URL: " + getUrl());
			}
			return false;
		}
		catch (Exception ex) {
			throw new NestedIOException(
					"Could not load Velocity template for URL [" + getUrl() + "]", ex);
		}
	}


	@Override
	protected void renderMergedTemplateModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		exposeHelpers(model, request);

		Context velocityContext = createVelocityContext(model, request, response);
		exposeHelpers(velocityContext, request, response);
		exposeToolAttributes(velocityContext, request);

		doRender(velocityContext, response);
	}
	protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception {
	}

	protected Context createVelocityContext(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		return createVelocityContext(model);
	}

	protected Context createVelocityContext(Map<String, Object> model) throws Exception {
		return new VelocityContext(model);
	}


	protected void exposeHelpers(
            Context velocityContext, HttpServletRequest request, HttpServletResponse response) throws Exception {

		exposeHelpers(velocityContext, request);
	}

	protected void exposeHelpers(Context velocityContext, HttpServletRequest request) throws Exception {
	}


	protected void exposeToolAttributes(Context velocityContext, HttpServletRequest request) throws Exception {
		// Expose generic attributes.
		if (this.toolAttributes != null) {
			for (Map.Entry<String, Class<?>> entry : this.toolAttributes.entrySet()) {
				String attributeName = entry.getKey();
				Class<?> toolClass = entry.getValue();
				try {
					Object tool = toolClass.newInstance();
					initTool(tool, velocityContext);
					velocityContext.put(attributeName, tool);
				}
				catch (Exception ex) {
					throw new NestedServletException("Could not instantiate Velocity tool '" + attributeName + "'", ex);
				}
			}
		}

	}

	protected void initTool(Object tool, Context velocityContext) throws Exception {
	}


	/**
	 * Render the Velocity view to the given response, using the given Velocity
	 * context which contains the complete template model to use.
	 * <p>The default implementation renders the template specified by the "url"
	 * bean property, retrieved via {@code getTemplate}. It delegates to the
	 * {@code mergeTemplate} method to merge the template instance with the
	 * given Velocity context.
	 * <p>Can be overridden to customize the behavior, for example to render
	 * multiple templates into a single view.
	 * @param context the Velocity context to use for rendering
	 * @param response servlet response (use this to get the OutputStream or Writer)
	 * @throws Exception if thrown by Velocity
	 * @see #setUrl
	 * @see #getTemplate()
	 * @see #mergeTemplate
	 */
	protected void doRender(Context context, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering Velocity template [" + getUrl() + "] in VelocityView '" + getBeanName() + "'");
		}
		mergeTemplate(getTemplate(), context, response);
	}

	/**
	 * Retrieve the Velocity template to be rendered by this view.
	 * <p>By default, the template specified by the "url" bean property will be
	 * retrieved: either returning a cached template instance or loading a fresh
	 * instance (according to the "cacheTemplate" bean property)
	 * @return the Velocity template to render
	 * @throws Exception if thrown by Velocity
	 * @see #setUrl
	 * @see #setCacheTemplate
	 * @see #getTemplate(String)
	 */
	protected Template getTemplate() throws Exception {
		// We already hold a reference to the template, but we might want to load it
		// if not caching. Velocity itself caches templates, so our ability to
		// cache templates in this class is a minor optimization only.
		if (isCacheTemplate() && this.template != null) {
			return this.template;
		}
		else {
			return getTemplate(getUrl());
		}
	}

	/**
	 * Retrieve the Velocity template specified by the given name,
	 * using the encoding specified by the "encoding" bean property.
	 * <p>Can be called by subclasses to retrieve a specific template,
	 * for example to render multiple templates into a single view.
	 * @param name the file name of the desired template
	 * @return the Velocity template
	 * @throws Exception if thrown by Velocity
	 * @see VelocityEngine#getTemplate
	 */
	protected Template getTemplate(String name) throws Exception {
		return (getEncoding() != null ?
				getVelocityEngine().getTemplate(name, getEncoding()) :
				getVelocityEngine().getTemplate(name));
	}

	/**
	 * Merge the template with the context.
	 * Can be overridden to customize the behavior.
	 * @param template the template to merge
	 * @param context the Velocity context to use for rendering
	 * @param response servlet response (use this to get the OutputStream or Writer)
	 * @throws Exception if thrown by Velocity
	 * @see Template#merge
	 */
	protected void mergeTemplate(
            Template template, Context context, HttpServletResponse response) throws Exception {

		try {
			template.merge(context, response.getWriter());
		}
		catch (MethodInvocationException ex) {
			Throwable cause = ex.getWrappedThrowable();
			throw new NestedServletException(
					"Method invocation failed during rendering of Velocity view with name '" +
					getBeanName() + "': " + ex.getMessage() + "; reference [" + ex.getReferenceName() +
					"], method '" + ex.getMethodName() + "'",
					cause==null ? ex : cause);
		}
	}



}
