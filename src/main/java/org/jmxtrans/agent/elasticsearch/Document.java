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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jmxtrans.agent.util.JsonUtils;

/**
 *
 * @author Evgeniy Khist
 */
public class Document {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    
    private Map<String, Object> document = new ConcurrentHashMap<>();

    public Document(Date timestamp, String host, String nodeName) {
        document.put("@timestamp", new SimpleDateFormat(TIMESTAMP_FORMAT).format(timestamp));
        if (host != null) {
            document.put("host", host);
        }
        if (nodeName != null) {
            document.put("nodeName", nodeName);
        }
    }
    
    public Object put(String key, Object value) {
        return document.put(key, value);
    }

    public String toJson() {
        return JsonUtils.toJson(document);
    }
}
