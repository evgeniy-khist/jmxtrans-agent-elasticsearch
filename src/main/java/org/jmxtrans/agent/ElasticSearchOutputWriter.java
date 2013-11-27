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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.jmxtrans.agent.elasticsearch.Document;
import org.jmxtrans.agent.elasticsearch.ElasticSearch;
import org.jmxtrans.agent.elasticsearch.IndexNameBuilder;
import org.jmxtrans.agent.elasticsearch.TypeNameCreator;
import static org.jmxtrans.agent.util.ConfigurationUtils.getString;
import static org.jmxtrans.agent.util.ConfigurationUtils.getInt;

/**
 *
 * @author Evgeniy Khist
 */
public class ElasticSearchOutputWriter extends AbstractOutputWriter {

    private static final String ELASTICSEARCH_HOST = "host";
    private static final String ELASTICSEARCH_HOST_DEFAULT_VALUE = "localhost";
    
    private static final String ELASTICSEARCH_PORT = "port";
    private static final int ELASTICSEARCH_PORT_DEFAULT_VALUE = 9200;
    
    private static final String ELASTICSEARCH_SSL_ENABLED = "sslEnabled";
    private static final boolean ELASTICSEARCH_SSL_ENABLED_DEFAULT_VALUE = false;
    
    private static final String INDEX = "index";
    private static final String INDEX_DEFAULT_VALUE = "jmxtrans-%{yyyy.MM.dd}";
    
    private static final String USE_PREFIX_AS_TYPE = "usePrefixAsType";
    private static final boolean USE_PREFIX_AS_TYPE_DEFAULT_VALUE = true;
    
    private static final String TYPE_DEFAULT_VALUE = "jmxtrans";
    
    private static final String NODE_NAME = "nodeName";
    
    private ElasticSearch elasticSearch;

    private IndexNameBuilder indexNameBuilder;
    private boolean usePrefixAsType;
    
    private String host;
    private String nodeName;
    
    private ThreadLocal<Map<String, Document>> documents = new ThreadLocal<Map<String, Document>>() {

        @Override
        protected Map<String, Document> initialValue() {
            return new ConcurrentHashMap<>();
        }
    };
    
    @Override
    public void postConstruct(Map<String, String> settings) {
        super.postConstruct(settings);
        
        String elasticSearchHost = getString(settings, ELASTICSEARCH_HOST, ELASTICSEARCH_HOST_DEFAULT_VALUE);
        int elasticSearchPort = getInt(settings, ELASTICSEARCH_PORT, ELASTICSEARCH_PORT_DEFAULT_VALUE);
        boolean elasticSearchSslEnabled = Boolean.parseBoolean(
                getString(settings, ELASTICSEARCH_SSL_ENABLED, String.valueOf(ELASTICSEARCH_SSL_ENABLED_DEFAULT_VALUE)));
        
        elasticSearch = new ElasticSearch(elasticSearchHost, elasticSearchPort, elasticSearchSslEnabled);
        
        logger.log(getInfoLevel(), "ElasticSearchOutputWriter is configured with host=" + elasticSearchHost + 
                ", port=" + elasticSearchPort + ", sslEnabled=" + elasticSearchSslEnabled);
        
        String indexNamePattern = getString(settings, INDEX, INDEX_DEFAULT_VALUE);
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
    public void writeQueryResult(String name, String type, Object value) throws IOException {
        Map<String, Document> currentDocuments = documents.get();
        String documentType = getType(name);
        if (!currentDocuments.containsKey(documentType)) {
            currentDocuments.put(documentType, new Document(new Date(), host, nodeName));
        }
        Document document = currentDocuments.get(documentType);
        document.put(name, value);
    }
    
    @Override
    public void writeInvocationResult(String invocationName, Object value) throws IOException {
        writeQueryResult(invocationName, null, value);
    }

    @Override
    public void postCollect() throws IOException {
        Map<String, Document> currentDocuments = documents.get();
        for (Map.Entry<String, Document> entry : currentDocuments.entrySet()) {
            int responseCode = elasticSearch.saveOrUpdate(indexNameBuilder.build(new Date()), entry.getKey(), entry.getValue());
            logger.log(getInfoLevel(), "Metrics sent to ElasticSearch responseCode=" + responseCode);
        }
        documents.remove();
    }
    
    private String getType(String name) {
        return usePrefixAsType ? TypeNameCreator.fromPrefix(name) : TYPE_DEFAULT_VALUE;
    }
}
