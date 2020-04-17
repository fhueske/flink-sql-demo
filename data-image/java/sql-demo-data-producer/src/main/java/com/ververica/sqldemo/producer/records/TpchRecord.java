package com.ververica.sqldemo.producer.records;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TpchRecord {

    private Map<Relation, DateFormat> dateFormats = new HashMap<>();

    private final String record;
    private final Relation relation;

    public TpchRecord(String record, Relation relation) {
        this.record = record;
        this.relation = relation;
    }

    public String getRecord() {
        return record;
    }

    public Relation getRelation() {
        return relation;
    }

    public Date getEventTime() {

        String[] fields = record.split("\\|");

        final String ts;
        switch(relation) {
            case LINEITEM:
                ts = fields[11];
                break;
            case ORDERS:
                ts = fields[5];
                break;
            case RATES:
                ts = fields[0];
                break;
            default:
                ts = "Invalid";
                break;
        }

        DateFormat df = dateFormats
                .computeIfAbsent(relation, r -> new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S"));

        try {
            return df.parse(ts);
        } catch (ParseException e) {
            return new Date(1970, 1, 1);
        }
    }
}
