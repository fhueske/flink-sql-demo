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

import com.ververica.sqldemo.producer.records.Relation;
import com.ververica.sqldemo.producer.records.TpchRecord;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Produces TpchRecords (Orders, Lineitem, Rates) into Kafka topics.
 */
public class TpchRecordProducer {

    public static void main(String[] args) throws InterruptedException {

        boolean areSuppliersConfigured = false;
        boolean areConsumersConfigured = false;

        Supplier<TpchRecord> ordersSupplier = null;
        Supplier<TpchRecord> lineitemSupplier = null;
        Supplier<TpchRecord> ratesSupplier = null;

        Consumer<TpchRecord> ordersConsumer = null;
        Consumer<TpchRecord> lineitemConsumer = null;
        Consumer<TpchRecord> ratesConsumer = null;

        double speedup = 1.0d;

        // parse arguments
        int argOffset = 0;
        while(argOffset < args.length) {

            String arg = args[argOffset++];
            switch (arg) {
                case "--input":
                    String source = args[argOffset++];
                    switch (source) {
                        case "file":
                            String basePath = args[argOffset++];
                            try {
                                ordersSupplier = new FileReader(basePath + "/orders.tbl", Relation.ORDERS);
                                lineitemSupplier = new FileReader(basePath + "/lineitem.tbl", Relation.LINEITEM);
                                ratesSupplier = new FileReader(basePath + "/rates.tbl", Relation.RATES);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown input configuration");
                    }
                    areSuppliersConfigured = true;
                    break;
                case "--output":
                    String sink = args[argOffset++];
                    switch (sink) {
                        case "console":
                            ordersConsumer = new ConsolePrinter();
                            lineitemConsumer = new ConsolePrinter();
                            ratesConsumer = new ConsolePrinter();
                            break;
                        case "kafka":
                            String brokers = args[argOffset++];
                            ordersConsumer = new KafkaProducer("orders", brokers);
                            lineitemConsumer = new KafkaProducer("lineitem", brokers);
                            ratesConsumer = new KafkaProducer("rates", brokers);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown output configuration");
                    }
                    areConsumersConfigured = true;
                    break;
                case "--speedup":
                    speedup = Double.parseDouble(args[argOffset++]);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown parameter");
            }
        }

        // check if we have a source and a sink
        if (!areSuppliersConfigured) {
            throw new IllegalArgumentException("Input sources were not properly configured.");
        }
        if (!areConsumersConfigured) {
            throw new IllegalArgumentException("Output sinks were not properly configured");
        }

        // create three threads for each record type
        Thread ridesFeeder = new Thread(new TpchRecordFeeder(ordersSupplier, new Delayer(speedup), ordersConsumer));
        Thread faresFeeder = new Thread(new TpchRecordFeeder(lineitemSupplier, new Delayer(speedup), lineitemConsumer));
        Thread driverChangesFeeder = new Thread(new TpchRecordFeeder(ratesSupplier, new Delayer(speedup), ratesConsumer));

        // start emitting data
        ridesFeeder.start();
        faresFeeder.start();
        driverChangesFeeder.start();

        // wait for threads to complete
        ridesFeeder.join();
        faresFeeder.join();
        driverChangesFeeder.join();
    }

    public static class TpchRecordFeeder implements Runnable {

        private final Supplier<TpchRecord> source;
        private final Delayer delayer;
        private final Consumer<TpchRecord> sink;

        TpchRecordFeeder(Supplier<TpchRecord> source, Delayer delayer, Consumer<TpchRecord> sink) {
            this.source = source;
            this.delayer = delayer;
            this.sink = sink;
        }

        @Override
        public void run() {
            Stream.generate(source).sequential()
                    .map(delayer)
                    .forEachOrdered(sink);
        }
    }
}
