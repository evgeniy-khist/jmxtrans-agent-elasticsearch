/*
 * Copyright 2013 Evgeniy Khist
 *
 * Licensed under the Apache License, ArticleVersion 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jmxtrans.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jmxtrans.agent.elasticsearch.util.IndexNameBuilder;
import org.jmxtrans.agent.elasticsearch.util.TypeNameCreator;
import static org.jmxtrans.agent.util.ConfigurationUtils.getString;
import static org.jmxtrans.agent.util.ConfigurationUtils.getInt;

/**
 *
 * @author Evgeniy Khist
 */
public class ElasticSearchOutputWriter extends AbstractOutputWriter {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    
    private static final String ELASTICSEARCH_HOST = "elasticsearchHost";
    private static final String ELASTICSEARCH_HOST_DEFAULT_VALUE = "localhost";
    
    private static final String ELASTICSEARCH_PORT = "elasticsearchPort";
    private static final int ELASTICSEARCH_PORT_DEFAULT_VALUE = 9200;
    
    private static final String ELASTICSEARCH_CLUSTER_NAME = "elasticsearchClusterName";
    private static final String ELASTICSEARCH_CLUSTER_NAME_DEFAULT_VALUE = "elasticsearch";
    
    private static final String ELASTICSEARCH_INDEX = "elasticsearchIndex";
    private static final String ELASTICSEARCH_INDEX_DEFAULT_VALUE = "jmxtrans-%{yyyy.MM.dd}";
    
    private static final String USE_PREFIX_AS_TYPE = "usePrefixAsType";
    private static final boolean USE_PREFIX_AS_TYPE_DEFAULT_VALUE = true;
    
    private static final String TYPE_DEFAULT_VALUE = "jmxtrans";
    
    private static final String NODE_NAME = "nodeName";
    
    private Client elasticsearchClient;

    private IndexNameBuilder indexNameBuilder;
    private boolean usePrefixAsType;
    
    private String host;
    private String nodeName;
    
    private ThreadLocal<Map<String, Map<String, Object>>> documents = new ThreadLocal<Map<String, Map<String, Object>>>() {

        @Override
        protected Map<String, Map<String, Object>> initialValue() {
            return new HashMap<>();
        }
    };
    
    @Override
    public void postConstruct(Map<String, String> settings) {
        super.postConstruct(settings);
        
        String elasticSearchHost = getString(settings, ELASTICSEARCH_HOST, ELASTICSEARCH_HOST_DEFAULT_VALUE);
        int elasticSearchPort = getInt(settings, ELASTICSEARCH_PORT, ELASTICSEARCH_PORT_DEFAULT_VALUE);
        String elasticsearchClusterName = getString(settings, ELASTICSEARCH_CLUSTER_NAME, ELASTICSEARCH_CLUSTER_NAME_DEFAULT_VALUE);
        
        Settings elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", elasticsearchClusterName)
                .put("client.transport.sniff", true)
                .build();

        elasticsearchClient = new TransportClient(elasticsearchSettings)
                .addTransportAddress(new InetSocketTransportAddress(elasticSearchHost, elasticSearchPort));

        logger.log(getInfoLevel(), 
                String.format("ElasticSearchOutputWriter is configured with host=%s, port=%s", elasticSearchHost, elasticSearchPort));
        
        String indexNamePattern = getString(settings, ELASTICSEARCH_INDEX, ELASTICSEARCH_INDEX_DEFAULT_VALUE);
        indexNameBuilder = new IndexNameBuilder(indexNamePattern);
        
        usePrefixAsType = Boolean.parseBoolean(
                getString(settings, USE_PREFIX_AS_TYPE, String.valueOf(USE_PREFIX_AS_TYPE_DEFAULT_VALUE)));
        
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        
        nodeName = getString(settings, NODE_NAME, null);
    }
    
    @Override
    public void preDestroy() {
        elasticsearchClient.close();
    }

    @Override
    public void writeQueryResult(String name, String type, Object value) throws IOException {
        Map<String, Map<String, Object>> currentThreadDocuments = documents.get();
        String documentType = getType(name);
        if (!currentThreadDocuments.containsKey(documentType)) {
            currentThreadDocuments.put(documentType, newDocument());
        }
        Map<String, Object> document = currentThreadDocuments.get(documentType);
        document.put(name, value);
    }
    
    @Override
    public void writeInvocationResult(String invocationName, Object value) throws IOException {
        writeQueryResult(invocationName, null, value);
    }

    @Override
    public void postCollect() throws IOException {
        Date timestamp = new Date();
        Map<String, Map<String, Object>> currentThreadDocuments = documents.get();
        for (Map.Entry<String, Map<String, Object>> entry : currentThreadDocuments.entrySet()) {
            String type = entry.getKey();
            
            Map<String, Object> document = entry.getValue();
            document.put("@timestamp", new SimpleDateFormat(TIMESTAMP_FORMAT).format(timestamp));
            
            IndexResponse response = elasticsearchClient
                    .prepareIndex(indexNameBuilder.build(timestamp), type)
                    .setSource(document)
                    .execute()
                    .actionGet();
            
            logger.log(getInfoLevel(), 
                    String.format("Metrics sent to ElasticSearch responseId=%s",  response.getId()));
        }
        documents.remove();
    }
    
    private String getType(String name) {
        return usePrefixAsType ? TypeNameCreator.fromPrefix(name) : TYPE_DEFAULT_VALUE;
    }

    private Map<String, Object> newDocument() {
        Map<String, Object> document = new HashMap<>();
        if (host != null) {
            document.put("host", host);
        }
        if (nodeName != null) {
            document.put("nodeName", nodeName);
        }
        return document;
    }
}
