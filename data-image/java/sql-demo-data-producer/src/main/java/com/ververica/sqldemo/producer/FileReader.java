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

import com.ververica.sqldemo.producer.serde.Deserializer;
import com.ververica.sqldemo.producer.records.Relation;
import com.ververica.sqldemo.producer.records.TpchRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FileReader implements Supplier<TpchRecord> {

    private final Iterator<TpchRecord> records;
    private final String filePath;

    public FileReader(String filePath, Relation relation) throws IOException {

        this.filePath = filePath;
        Deserializer deserializer = new Deserializer(relation);
        try {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));

            Stream<String> lines = reader.lines().sequential();
            records = lines.map(l -> deserializer.parseFromString(l)).iterator();

        } catch (IOException e) {
            throw new IOException("Error reading records from file: " + filePath, e);
        }
    }

    @Override
    public TpchRecord get() {

        if (records.hasNext()) {
            return records.next();
        } else {
            throw new NoSuchElementException("All records read from " + filePath);
        }
    }
}
