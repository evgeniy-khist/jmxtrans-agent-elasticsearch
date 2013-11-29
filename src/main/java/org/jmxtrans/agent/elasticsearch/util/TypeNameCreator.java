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

/**
 *
 * @author Evgeniy Khist
 */
public class TypeNameCreator {

    public static String fromPrefix(String name) {
        int firstDotIndex = name.indexOf(".");
        if (firstDotIndex < 1) {
            throw new IllegalArgumentException("Can't create type name from prefix " + name);
        }
        return name.substring(0, firstDotIndex);
    }
}
