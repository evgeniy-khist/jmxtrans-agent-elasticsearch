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
package org.jmxtrans.agent.elasticsearch;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static org.jmxtrans.agent.util.JsonUtils.toJson;

/**
 *
 * @author Evgeniy Khist
 */
public class ElasticSearch {

    private final String elasticsearchHost;
    private final int elasticsearchPort;

    public ElasticSearch(String elasticsearchHost, int elasticsearchPort) {
        this.elasticsearchHost = elasticsearchHost;
        this.elasticsearchPort = elasticsearchPort;
    }
    
    public void saveOrUpdate(String index, String type, Map<String, Object> document) {
        try {
            URL url = new URL("http://" + elasticsearchHost + ":" + elasticsearchPort + "/" + index + "/" + type + "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(toJson(document));
            osw.flush();
            osw.close();
            System.out.println("org.jmxtrans.agent.elasticsearch.ElasticSearch.saveOrUpdate: ResponseCode=" + connection.getResponseCode());
        } catch(Exception e) {
            System.err.println("org.jmxtrans.agent.elasticsearch.ElasticSearch.saveOrUpdate: Error=" + e.getMessage());
        }
    }
}
