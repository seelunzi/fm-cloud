package com.fm.cloud.bamboo.ribbon;

import com.fm.cloud.bamboo.BambooRequest;
import com.fm.cloud.bamboo.BambooRequestContext;
import com.fm.utils.WebUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;


/**
 * 用于@LoadBalance 标记的 RestTemplate，主要作用是用来获取request的相关信息，为后面的路由提供数据基础。
 */
public class BambooClientHttpRequestIntercptor implements ClientHttpRequestInterceptor{
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        URI uri = request.getURI();
        BambooRequest bambooRequest = BambooRequest.builder()
                .serviceId(uri.getHost())
                .uri(uri.getPath())
                .addMultiHeaders(request.getHeaders())
                .addMultiParams(WebUtils.getQueryParams(uri.getQuery()))
                .build();

        BambooRequestContext.initRequestContext(bambooRequest, bambooRequest.getParams().getFirst("version"));
        try {
            return execution.execute(request, body);
        }finally {
            BambooRequestContext.shutdownRequestContext();
        }
    }


}