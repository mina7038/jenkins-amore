package com.apgroup.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

   @Override
   public void addResourceHandlers(ResourceHandlerRegistry registry) {
	   registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/uploads/");
    }
    
    //@Override
    //public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //registry.addResourceHandler("/uploads/**")
         //       .addResourceLocations("file:/Users/mina/250518/uploads/");
    //}
    
}
