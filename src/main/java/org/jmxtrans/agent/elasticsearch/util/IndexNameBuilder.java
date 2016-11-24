/**
 * The MIT License
 * Copyright © 2013 Evgeniy Khyst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jmxtrans.agent.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Evgeniy Khyst
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
