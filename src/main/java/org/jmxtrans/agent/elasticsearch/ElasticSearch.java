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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Evgeniy Khist
 */
public class ElasticSearch {

    private final String host; 
    private final int port;
    private final boolean sslEnabled;
    private final String urlStr;

    public ElasticSearch(String elasticsearchHost, int elasticsearchPort, boolean sslEnabled) {
        this.host = elasticsearchHost;
        this.port = elasticsearchPort;
        this.sslEnabled = sslEnabled;
        this.urlStr = (sslEnabled ? "https" : "http") + "://" + elasticsearchHost + ":" + elasticsearchPort + "/";
    }

    public int saveOrUpdate(String index, String type, Document document) throws IOException {
        URL url = new URL(urlStr + index + "/" + type + "/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream())) {
            osw.write(document.toJson());
            osw.flush();
        }
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }
}
