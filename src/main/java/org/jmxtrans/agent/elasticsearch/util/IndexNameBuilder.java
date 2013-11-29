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
package org.jmxtrans.agent.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Evgeniy Khist
 */
public class IndexNameBuilder {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%\\{(.+)\\}");
    private final String indexNamePattern;

    public IndexNameBuilder(String indexNamePattern) {
        this.indexNamePattern = indexNamePattern;
    }

    public String build(Date date) {
        StringBuffer sb = new StringBuffer();
        Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher(indexNamePattern);
        while (placeholderMatcher.find()) {
            placeholderMatcher.appendReplacement(sb, format(placeholderMatcher.group(1), date));
        }
        placeholderMatcher.appendTail(sb);
        return sb.toString();
    }
    
    private String format(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }
}
