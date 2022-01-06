package com.netcorner.hotelanalysis.config;

import com.netcorner.hotelanalysis.utils.StringTools;
import com.netcorner.velocity.VelocityConfigurer;
import com.netcorner.velocity.VelocityViewResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;

import java.util.Properties;

/**
 * Created by shijiufeng on 2019/3/6.
 */
@Configuration
public class VelocityConfig {
    @Value("${spring.velocity.prefix:/templates/}")
    private String prefix;

    @Value("${spring.velocity.suffix:.html}")
    private String suffix;

    @Bean
    public VelocityConfigurer getVelocityConfigurer()
    {


        VelocityConfigurer velocityConfigurer=new VelocityConfigurer();
        velocityConfigurer.setResourceLoaderPath(prefix);
        Properties properties=new Properties();
        properties.put("velocimacro.permissions.allow.inline.local.scope",true);
        properties.put("input.encoding","UTF-8");
        properties.put("output.encoding","UTF-8");
        velocityConfigurer.setVelocityProperties(properties);
        return velocityConfigurer;
    }

    @Bean
    public ViewResolver getVelocityViewResolver()
    {

        VelocityViewResolver velocityViewResolver=new VelocityViewResolver();
        velocityViewResolver.setCache(false);
        velocityViewResolver.setPrefix(prefix);
        velocityViewResolver.setSuffix(suffix);
        velocityViewResolver.setDateToolAttribute("date");
        velocityViewResolver.setNumberToolAttribute("number");
        velocityViewResolver.setContentType("text/html;charset=UTF-8");
        velocityViewResolver.setExposeSpringMacroHelpers(true);
        velocityViewResolver.setExposeRequestAttributes(true);
        velocityViewResolver.setRequestContextAttribute("request");
        //velocityViewResolver.setToolboxConfigLocation("/velocity-toolbox.xml");
        //velocityViewResolver.setViewClass(VelocityToolbox20View.class);

//        Properties properties=new Properties();
//        velocityViewResolver.setVelocityProperties(properties);
        return velocityViewResolver;
    }
}
