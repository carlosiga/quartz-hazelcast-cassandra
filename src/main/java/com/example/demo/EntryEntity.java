package com.example.demo;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;

@Table("HzEntry")
public class EntryEntity implements Serializable {

    @PrimaryKeyColumn(name="id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    String id;

    @Column("data")
    String data;

    public EntryEntity(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
