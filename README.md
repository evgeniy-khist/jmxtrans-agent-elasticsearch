jmxtrans-agent-elasticsearch
============================
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
        <host>localhost</host>
        <port>9200</port>
        <sslEnabled>false</sslEnabled>
        <index>jmxtrans-%{yyyy.MM.dd}</index>
        <nodeName>nodeName</nodeName><!--empty by default-->
        <usePrefixAsType>true</usePrefixAsType>
        <omitEmptyValues>true</omitEmptyValues>
    </outputWriter>
    <collectIntervalInSeconds>20</collectIntervalInSeconds>
</jmxtrans-agent>
```