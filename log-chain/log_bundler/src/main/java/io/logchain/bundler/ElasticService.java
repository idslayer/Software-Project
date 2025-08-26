package io.logchain.bundler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import io.logchain.bundler.config.ElasticConfig;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
public class ElasticService {
    final ElasticConfig elasticConfig;
    final ObjectMapper mapper;

    public RestHighLevelClient createClient() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticConfig.getUser(), elasticConfig.getPassword()));

        RestClientBuilder builder = RestClient.builder(HttpHost.create(elasticConfig.getAddress()))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                );

        return new RestHighLevelClient(builder);
    }

    public String verifyLog(String log) {
        return "ok";
    }

    public List<?> searchLogs(long startTsMillis, long endTsMillis, String message, String hash, int page, int size) throws IOException {
        var client = createClient();

        SearchRequest searchRequest = new SearchRequest(elasticConfig.getNormLogIndex());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        var boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("timestamp")
                        .gte(startTsMillis)
                        .lte(endTsMillis));
        if (!message.isBlank()) {
            boolQuery.must(QueryBuilders.matchQuery("message", message));
        }
        if (!hash.isBlank()) {
            boolQuery.must(QueryBuilders.matchQuery("hash", hash));
        }

        sourceBuilder.query(boolQuery);
        sourceBuilder.from(page * size);
        sourceBuilder.size(size);
        searchRequest.source(sourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        var items = Streams.stream(response.getHits().iterator())
                .map(SearchHit::getSourceAsMap)
                .toList();
        client.close();
        return items;
    }

    public List<Map<String, Object>> queryAnchorInSameBatchByHash(String hash) throws IOException {
        var client = createClient();
        // Step 1: Find the log with the given hash
        SearchRequest findLogRequest = new SearchRequest(elasticConfig.getAnchorIndex());
        SearchSourceBuilder findLogSource = new SearchSourceBuilder();
        findLogSource.query(QueryBuilders.matchQuery("log.hash", hash));
        findLogSource.size(1);

        findLogRequest.source(findLogSource);
        SearchResponse findLogResponse = client.search(findLogRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = findLogResponse.getHits().getHits();
        if (hits.length == 0) {
            client.close();
            return emptyList();
        }
        String batchId = (String) hits[0].getSourceAsMap().get("batchId");
        if (batchId == null) {
            client.close();
            return emptyList();
        }
        // Step 2: Query all anchor with the same batchId
        SearchRequest batchLogsRequest = new SearchRequest(elasticConfig.getAnchorIndex());
        SearchSourceBuilder batchLogsSource = new SearchSourceBuilder();
        batchLogsSource.query(QueryBuilders.termQuery("batchId", batchId));
        batchLogsSource.size(1000); // limit to 1000 logs for safety
        batchLogsRequest.source(batchLogsSource);
        SearchResponse batchLogsResponse = client.search(batchLogsRequest, RequestOptions.DEFAULT);
        List<Map<String, Object>> logs = Streams.stream(batchLogsResponse.getHits().iterator())
                .map(SearchHit::getSourceAsMap)
                .toList();
        client.close();
        return logs;
    }
}
