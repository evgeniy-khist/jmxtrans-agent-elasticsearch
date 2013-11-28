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
package org.jmxtrans.agent.util;

import java.util.Map;

/**
 *
 * @author Evgeniy Khist
 */
public class JsonUtils {
    
    public static String toJson(Map<String, Object> document, boolean omitEmptyValues) {
        int length = document.size();
        int i = 0;
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (omitEmptyValues && entry.getValue() == null) {
                continue;
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof Number) {
                sb.append(entry.getValue());
            } else {
                sb.append("\"").append(entry.getValue()).append("\"");
            }
            if (++i < length) {
                sb.append(",");
            }
        }
        return sb.append("}").toString();
    }
}
