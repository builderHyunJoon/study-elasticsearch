package com.test.elastic.config;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
    @Bean
    public RestHighLevelClient prdClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("", 9200, "http"))
        );
    }

    @Bean
    public RestHighLevelClient devClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("", 9200, "http"))
        );
    }
}
