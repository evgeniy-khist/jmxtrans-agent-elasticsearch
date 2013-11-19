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
import java.util.HashMap;
import java.util.Map;
import org.jmxtrans.agent.AbstractOutputWriter;
import org.jmxtrans.agent.elasticsearch.ElasticSearch;
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
    private static final String ELASTICSEARCH_INDEX = "index";
    private static final String ELASTICSEARCH_INDEX_DEFAULT_VALUE = "jmxtrans";
    private static final String ELASTICSEARCH_TYPE = "type";
    private static final String ELASTICSEARCH_TYPE_DEFAULT_VALUE = "jmxtrans";
    
    private ElasticSearch elasticSearch;
    private String elasticSearchIndex;
    private String elasticSearchType;
    private Map<String, Object> document;
    
    @Override
    public void postConstruct(Map<String, String> settings) {
        super.postConstruct(settings);
        
        String elasticSearchHost = getString(settings, ELASTICSEARCH_HOST, ELASTICSEARCH_HOST_DEFAULT_VALUE);
        int elasticSearchPort = getInt(settings, ELASTICSEARCH_PORT, ELASTICSEARCH_PORT_DEFAULT_VALUE);
        elasticSearch = new ElasticSearch(elasticSearchHost, elasticSearchPort);
        
        elasticSearchIndex = getString(settings, ELASTICSEARCH_INDEX, ELASTICSEARCH_INDEX_DEFAULT_VALUE);
        elasticSearchType = getString(settings, ELASTICSEARCH_TYPE, ELASTICSEARCH_TYPE_DEFAULT_VALUE);
    }

    @Override
    public void preCollect() throws IOException {
        document = new HashMap<String, Object>();
        document.put("timestamp", System.currentTimeMillis());
    }

    @Override
    public void writeQueryResult(String name, String type, Object value) throws IOException {
        document.put(name, value);
    }
    
    @Override
    public void writeInvocationResult(String invocationName, Object value) throws IOException {
        writeQueryResult(invocationName, null, value);
    }

    @Override
    public void postCollect() throws IOException {
        elasticSearch.saveOrUpdate(elasticSearchIndex, elasticSearchType, document);
        document = null;
    }
}
