//package com.uninter.tcc.configuration;
//
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
//import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
//import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
//import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
//import org.springframework.boot.actuate.endpoint.web.*;
//import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
//import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
//import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//import org.springframework.util.ReflectionUtils;
//import org.springframework.util.StringUtils;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
//
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.oas.annotations.EnableOpenApi;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.service.Contact;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Configuration
////@EnableWebMvc
//@EnableOpenApi
////@EnableWebMvc
////@EnableSwagger2
//public class SwaggerConfiguration {
//	@Bean
//	public Docket api() {
//		return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo()).select()
//				.apis(RequestHandlerSelectors
//						.basePackage("com.uninter.tcc.controller")/* RequestHandlerSelectors.any() */)
//				.paths(PathSelectors.any()).build();
//
//	}
//
//	private ApiInfo apiInfo() {
//		return new ApiInfoBuilder().title("Simple Spring Boot REST API")
//				.description("Um exemplo de aplicação Spring Boot REST API").version("1.0.0")
//				.license("Apache License Version 2.0").licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
//				.contact(new Contact("Adson", "URL DO GIT", "E-MAIL")).build();
//	}
//
//	@Bean
//	public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
//			ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier,
//			EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
//			WebEndpointProperties webEndpointProperties, Environment environment) {
//		List<ExposableEndpoint<?>> allEndpoints = new ArrayList();
//		Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
//		allEndpoints.addAll(webEndpoints);
//		allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
//		allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
//		String basePath = webEndpointProperties.getBasePath();
//		EndpointMapping endpointMapping = new EndpointMapping(basePath);
//		boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment,
//				basePath);
//		return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
//				corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath),
//				shouldRegisterLinksMapping);
//	}
//
//	private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
//			String basePath) {
//		return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath)
//				|| ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
//	}
//
//	
//	/*
//	 * public void addResourceHandlers(ResourceHandlerRegistry registry) {
//	 * registry.addResourceHandler("swagger-ui.html")
//	 * .addResourceLocations("classpath:/META-INF/resources/");
//	 * 
//	 * registry.addResourceHandler("/webjars/**")
//	 * .addResourceLocations("classpath:/META-INF/resources/webjars/"); }
//	 */
//	 @Bean
//	    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
//	        return new BeanPostProcessor() {
//
//	            @Override
//	            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//	                if (bean instanceof WebMvcRequestHandlerProvider ) {
//	                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
//	                }
//	                return bean;
//	            }
//
//	            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
//	                List<T> copy = mappings.stream()
//	                    .filter(mapping -> mapping.getPatternParser() == null)
//	                    .collect(Collectors.toList());
//	                mappings.clear();
//	                mappings.addAll(copy);
//	            }
//
//	            @SuppressWarnings("unchecked")
//	            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
//	                try {
//	                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
//	                    field.setAccessible(true);
//	                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
//	                } catch (IllegalArgumentException | IllegalAccessException e) {
//	                    throw new IllegalStateException(e);
//	                }
//	            }
//	        };
//	    }    
//}