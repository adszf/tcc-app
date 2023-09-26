package com.uninter.tcc.configuration;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {
	
	@Value(value = "${fixed.thread.pool}")
	private String fixedThreadPool;

	@Bean("taskExecutorKafka")
	public ExecutorService executorKafka() {
		return Executors.newFixedThreadPool(Integer.valueOf(fixedThreadPool));
	}

	@Bean("taskExecutorAnalysis")
	public ExecutorService executorAnalysis() {
		return Executors.newFixedThreadPool(Integer.valueOf(fixedThreadPool));
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}

	@Bean
	public WebMvcConfigurer webMvcConfigurer(AsyncTaskExecutor taskExecutor,
			CallableProcessingInterceptor callableProcessingInterceptor) {
		return new WebMvcConfigurer() {
			@Override
			public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
				configurer.setDefaultTimeout(3600000).setTaskExecutor(taskExecutor);
				configurer.registerCallableInterceptors(callableProcessingInterceptor);
				WebMvcConfigurer.super.configureAsyncSupport(configurer);
			}
		};
	}

	@Bean
	public CallableProcessingInterceptor callableProcessingInterceptor() {
		return new TimeoutCallableProcessingInterceptor() {
			@Override
			public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
				return super.handleTimeout(request, task);
			}
		};
	}
}
