package com.rental.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 控制器配置类
 */
@Configuration
public class ControllerConfig {

    /**
     * 确保控制器映射优先级高于静态资源处理
     */
    @Bean
    public FilterRegistrationBean<org.springframework.web.filter.HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
        FilterRegistrationBean<org.springframework.web.filter.HiddenHttpMethodFilter> filterRegistrationBean =
            new FilterRegistrationBean<>(new org.springframework.web.filter.HiddenHttpMethodFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
