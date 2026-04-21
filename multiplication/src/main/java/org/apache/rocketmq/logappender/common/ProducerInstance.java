/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.logappender.common;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;

import java.io.IOException;
import java.time.Duration;

/**
 * Common Producer component
 */
public class ProducerInstance {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    public static final String APPENDER_TYPE = "APPENDER_TYPE";

    public static final String LOG4J_APPENDER = "LOG4J_APPENDER";

    public static final String LOG4J2_APPENDER = "LOG4J2_APPENDER";

    public static final String LOGBACK_APPENDER = "LOGBACK_APPENDER";

    public static final String DEFAULT_GROUP = "rocketmq_appender";

    private static ProducerInstance instance = new ProducerInstance();

    private Producer producer;

    public static ProducerInstance getProducerInstance() {
        return instance;
    }

    public Producer getProducer(String endpoint) throws ClientException {
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(endpoint)
                .setRequestTimeout(REQUEST_TIMEOUT);
        ClientConfiguration configuration = builder.build();
        producer = provider.newProducerBuilder()
                .setClientConfiguration(configuration)
                .build();
        return producer;
    }

    public void close() throws IOException {
        if (producer != null) {
            producer.close();
        }
    }
}
