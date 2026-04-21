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
package org.apache.rocketmq.logappender.logback;
import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.status.ErrorStatus;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.logappender.common.ProducerInstance;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * Logback Appender Component
 */
public class RocketmqLogbackAppender extends AppenderBase<ILoggingEvent> {

    /**
     * Message tag define
     */
    private String tag;

    /**
     * Which topic to send log messages
     */
    private String topic;

    private String[] keys;

    /**
     * RocketMQ proxy endpoint
     */
    private String endpoint;

    /**
     * Log producer send instance
     */
    private Producer producer;

    private Layout layout;

    private PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();

    /**
     * Info,error,warn,callback method implementation
     */
    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        String logStr = this.layout.doLayout(event);

        //customize the code start
        String messageGroup = null;
        try {
            String hostName = "";
            try {
                hostName = Inet4Address.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            final String hostPort = context.getProperty("port");
            final String applicationId = context.getProperty("applicationId");

            if (hostName == null || hostPort == null || applicationId == null) {
                addError("hostname/hostport/applicationId could not be found in context.");
            } else {
                messageGroup = hostName + "-" + hostPort + "-" + applicationId;
            }

            ClientServiceProvider provider = ClientServiceProvider.loadService();
            MessageBuilder messageBuilder = provider.newMessageBuilder()
                    .setTopic(topic)
                    .setBody(logStr.getBytes(StandardCharsets.UTF_8));
            /**
             * Not required fields
             */
            if (messageGroup != null) {
                messageBuilder.setMessageGroup(messageGroup);
            }
            if (this.keys != null && this.keys.length != 0) {
                messageBuilder.setKeys(this.keys);
            }

            if (this.tag != null && this.tag.length() != 0) {
                messageBuilder.setTag(this.tag);
            }
            //currently there is no easy to add more properties as they return new HashMap<>(properties) in MessageImpl.getProperties()
            messageBuilder.addProperty(ProducerInstance.APPENDER_TYPE, ProducerInstance.LOGBACK_APPENDER);
            messageBuilder.addProperty("level", event.getLevel().toString());

            Map<String, String> copyOfPropertyMap = this.getContext().getCopyOfPropertyMap();
            if (copyOfPropertyMap != null) {
                Set<Map.Entry<String, String>> entries = copyOfPropertyMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    messageBuilder.addProperty(entry.getKey(), entry.getValue());
                }
            }
            Message msg = messageBuilder.build();
            //customize the code end

            producer.sendAsync(msg).whenComplete((receipt, throwable) -> {
                if (throwable != null) {
                }
            });
        } catch (Exception e) {
            addError("Could not send message in RocketmqLogbackAppender [" + name + "]. Message is : " + logStr, e);
        }
    }

    /**
     * Options are activated and become effective only after calling this method.
     */
    public void start() {
        int errors = 0;

        if (this.layout == null) {
            addStatus(new ErrorStatus("No layout set for the RocketmqLogbackAppender named \"" + name + "\".", this));
            errors++;
        }

        if (errors > 0 || !checkEntryConditions()) {
            return;
        }
        try {
            producer = ProducerInstance.getProducerInstance().getProducer(endpoint);
        } catch (Exception e) {
            addError("Starting RocketmqLogbackAppender [" + this.getName()
                    + "] endpoint:" + endpoint + " " + e.getMessage());
        }
        if (producer != null) {
            super.start();
        }
    }

    /**
     * When system exit,this method will be called to close resources
     */
    public synchronized void stop() {
        // The synchronized modifier avoids concurrent append and close operations
        if (!this.started) {
            return;
        }

        this.started = false;

        try {
            ProducerInstance.getProducerInstance().close();
        } catch (Exception e) {
            addError("Closeing RocketmqLogbackAppender [" + this.getName()
                    + "] endpoint:" + endpoint + " " + e.getMessage());
        }

        // Help garbage collection
        producer = null;
    }

    protected boolean checkEntryConditions() {
        String fail = null;

        if (this.topic == null) {
            fail = "No topic";
        }

        if (fail != null) {
            addError(fail + " for RocketmqLogbackAppender named [" + name + "].");
            return false;
        } else {
            return true;
        }
    }

    public Layout getLayout() {
        return this.layout;
    }

    /**
     * Set the pattern layout to format the log.
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys == null ? null : keys.split(",");
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
