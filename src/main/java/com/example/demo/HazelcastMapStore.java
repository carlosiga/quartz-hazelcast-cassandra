package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.MapStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class HazelcastMapStore<V extends Serializable> implements
        MapStore<String, V> {

    static final Logger log = LoggerFactory.getLogger(HazelcastMapStore.class);

    private Class<V> valueClass;

    private EntryRepository dao;

    private final ObjectMapper mapper = new ObjectMapper();

    public HazelcastMapStore(EntryRepository dao, Class<V> valueClass) {
        this.valueClass = valueClass;
        this.dao = dao;
    }

    @Override
    public void store(final String key, final V value) {
        log.info("Storing key " + key + " with value " + value);
        try {
            dao.save(new EntryEntity(key, mapper.writeValueAsString(value)));
        } catch (JsonProcessingException ex) {
            log.error("Error parsing given object " + value, ex);
        }
    }

    @Override
    public void storeAll(final  Map<String, V> map) {
        map.entrySet().stream().
                forEach((entrySet) -> {
                    store(entrySet.getKey(), entrySet.getValue());
                });
    }

    @Override
    public void delete(final String key) {
        log.info("Deleting key " + key);
        dao.delete(key);
    }

    @Override
    public void deleteAll(final Collection<String> keys) {
        keys.stream().
                forEach((key) -> {
                    delete(key);
                });
    }

    @Override
    public V load(final String key) {
        log.info("Loading");
        final EntryEntity entry = dao.findOne(key);
        return entry == null ? null : fromJson(entry.getData());
    }

    @Override
    public Map<String, V> loadAll(Collection<String> keys) {
        log.info("Loading All");
        final Map<String, V> map = new HashMap<>();
        dao.findAll(keys).iterator().forEachRemaining((entry) -> {
                    map.put(entry.getId(), fromJson(entry.getData()));
                });
        return map;
    }

    @Override
    public Set<String> loadAllKeys() {
        final Iterable<EntryEntity> list = dao.findAll();
        final Set<String> set = new HashSet<>();
        list.iterator().
                forEachRemaining((item) -> {
                    set.add(item.getId());
                });
        return set;
    }

    private V fromJson(final String json) {

        try {
            return mapper.readValue(json, valueClass);
        } catch (IOException ex) {
            log.error("Error deserializing object " + json, ex);
        }
        return null;
    }

}