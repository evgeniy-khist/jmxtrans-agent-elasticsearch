jmxtrans-agent-elasticsearch
============================
**jmxtrans-agent-elasticsearch** adds to [jmxtrans-agent](https://github.com/jmxtrans/jmxtrans-agent) [ElasticSearchOutputWriter](https://github.com/evgeniy-khist/jmxtrans-agent-elasticsearch/blob/master/src/main/java/org/jmxtrans/agent/ElasticSearchOutputWriter.java) that sends metrics to [Elasticsearch](www.elasticsearch.org). It has zero dependencies to ease integration.

```xml
<jmxtrans-agent>
    <queries>
        <query objectName="java.lang:type=OperatingSystem" attribute="SystemLoadAverage" resultAlias="os.systemLoadAverage"/>
        <query objectName="java.lang:type=OperatingSystem" attribute="FreePhysicalMemorySize" resultAlias="os.freePhysicalMemorySize"/>
        <query objectName="java.lang:type=OperatingSystem" attribute="FreeSwapSpaceSize" resultAlias="os.freeSwapSpaceSize"/>
        <query objectName="java.lang:type=OperatingSystem" attribute="OpenFileDescriptorCount" resultAlias="os.openFileDescriptorCount"/>

        <query objectName="java.lang:type=Memory" attribute="HeapMemoryUsage" key="used" resultAlias="jvm.heapMemoryUsage.used"/>
        <query objectName="java.lang:type=Memory" attribute="HeapMemoryUsage" key="committed" resultAlias="jvm.heapMemoryUsage.committed"/>
        <query objectName="java.lang:type=Memory" attribute="NonHeapMemoryUsage" key="used" resultAlias="jvm.nonHeapMemoryUsage.used"/>
        <query objectName="java.lang:type=Memory" attribute="NonHeapMemoryUsage" key="committed" resultAlias="jvm.nonHeapMemoryUsage.committed"/>

        <query objectName="java.lang:type=ClassLoading" attribute="LoadedClassCount" resultAlias="jvm.loadedClasses"/>

        <query objectName="java.lang:type=Threading" attribute="ThreadCount" resultAlias="jvm.threadCount"/>
        <query objectName="java.lang:type=Threading" attribute="DaemonThreadCount" resultAlias="jvm.daemonThreadCount"/>
        <query objectName="java.lang:type=Threading" attribute="TotalStartedThreadCount" resultAlias="jvm.totalStartedThreadCount"/>
        <query objectName="java.lang:type=Threading" attribute="PeakThreadCount" resultAlias="jvm.peakThreadCount"/>
    </queries>
    <outputWriter class="org.jmxtrans.agent.ElasticSearchOutputWriter">
        <elasticsearchHost>localhost</elasticsearchHost>
        <elasticsearchPort>9300</elasticsearchPort>
        <elasticsearchClusterName>elasticsearch</elasticsearchClusterName>
        <elasticsearchIndex>jmxtrans-%{yyyy.MM.dd}</elasticsearchIndex>
        <nodeName>nodeName</nodeName><!--empty by default-->
        <usePrefixAsType>true</usePrefixAsType>
    </outputWriter>
    <collectIntervalInSeconds>20</collectIntervalInSeconds>
</jmxtrans-agent>
```

If value of `elasticsearchIndex` contains date placeholder like `%{yyyy.MM.dd}` it will be replaced with current date and time string representation based on specified format.
