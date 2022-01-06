
package com.netcorner.springboot.velocity;

import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class VelocityViewResolver extends AbstractTemplateViewResolver {



	public VelocityViewResolver() {
		setViewClass(requiredViewClass());
	}

	/**
	 * Requires {@link VelocityView}.
	 */
	@Override
	protected Class<?> requiredViewClass() {
		return VelocityView.class;
	}



	@Override
	protected void initApplicationContext() {
		super.initApplicationContext();
	}


	@Override
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		VelocityView view = (VelocityView) super.buildView(viewName);

		return view;
	}

}
