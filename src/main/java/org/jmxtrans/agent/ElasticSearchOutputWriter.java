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
import org.jmxtrans.agent.elasticsearch.ElasticSearch;
import org.jmxtrans.agent.elasticsearch.IndexNameBuilder;
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
    
    private static final String ELASTICSEARCH_SSL_ENABLED = "ssl";
    private static final boolean ELASTICSEARCH_SSL_ENABLED_DEFAULT_VALUE = false;
    
    private static final String ELASTICSEARCH_INDEX = "index";
    private static final String ELASTICSEARCH_INDEX_DEFAULT_VALUE = "jmxtrans-%{yyyy.MM.dd}";
    
    private static final String ELASTICSEARCH_TYPE = "jmxtrans";
    
    private static final String NODE_NAME = "nodeName";
    
    private ElasticSearch elasticSearch;
    
    private String indexNamePattern;
    private IndexNameBuilder indexNameBuilder;
    
    private String host;
    private String nodeName;
    
    private ThreadLocal<Map<String, Object>> document = new ThreadLocal<>();
    
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
        
        indexNamePattern = getString(settings, ELASTICSEARCH_INDEX, ELASTICSEARCH_INDEX_DEFAULT_VALUE);
        indexNameBuilder = new IndexNameBuilder(indexNamePattern);
        
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        
        nodeName = getString(settings, NODE_NAME, null);
    }

    @Override
    public void preCollect() throws IOException {
        this.document.set(newDocument(new Date(), host, nodeName));
    }

    @Override
    public void writeQueryResult(String name, String type, Object value) throws IOException {
        document.get().put(name, value);
    }
    
    @Override
    public void writeInvocationResult(String invocationName, Object value) throws IOException {
        writeQueryResult(invocationName, null, value);
    }

    @Override
    public void postCollect() throws IOException {
        int responseCode = elasticSearch.saveOrUpdate(indexNameBuilder.build(new Date()), ELASTICSEARCH_TYPE, document.get());
        logger.log(getInfoLevel(), "Metrics sent to ElasticSearch responseCode=" + responseCode);
        document.remove();
    }
    
    private static Map<String, Object> newDocument(Date timestamp, String host, String nodeName) {
        Map<String, Object> document = new HashMap<>();
        document.put("@timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(timestamp));
        if (host != null) {
            document.put("host", host);
        }
        if (nodeName != null) {
            document.put("nodeName", nodeName);
        }
        return document;
    }
}
