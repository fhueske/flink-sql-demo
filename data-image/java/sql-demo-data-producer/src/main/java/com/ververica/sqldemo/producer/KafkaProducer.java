/*
 * Copyright 2019 Ververica GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ververica.sqldemo.producer;

import com.ververica.sqldemo.producer.serde.Serializer;
import com.ververica.sqldemo.producer.records.TpchRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.util.Properties;
import java.util.function.Consumer;

/**
 * Produces TaxiRecords into a Kafka topic.
 */
public class KafkaProducer implements Consumer<TpchRecord> {

    private final String topic;
    private final org.apache.kafka.clients.producer.KafkaProducer<byte[], byte[]> producer;
    private final Serializer serializer;

    public KafkaProducer(String kafkaTopic, String kafkaBrokers) {
        this.topic = kafkaTopic;
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(createKafkaProperties(kafkaBrokers));
        this.serializer = new Serializer();
    }

    @Override
    public void accept(TpchRecord record) {
        // serialize record as JSON
        byte[] data = serializer.toBytes(record);
        // create producer record and publish to Kafka
        ProducerRecord<byte[], byte[]> kafkaRecord = new ProducerRecord<>(topic, data);
        producer.send(kafkaRecord);
    }

    /**
     * Create configuration properties for Kafka producer.
     *
     * @param brokers The brokers to connect to.
     * @return A Kafka producer configuration.
     */
    private static Properties createKafkaProperties(String brokers) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        return kafkaProps;
    }

}
