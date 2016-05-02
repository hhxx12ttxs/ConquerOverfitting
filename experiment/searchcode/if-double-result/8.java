package com.metabroadcast.common.persistence.mongo.health;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.joda.time.Duration;

import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;
import com.metabroadcast.common.ids.UUIDGenerator;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public final class MongoIOProbe implements HealthProbe {

    private final UUIDGenerator idGen = new UUIDGenerator();
    private final DBCollection collection;
    private final String hostId;
    
    private Duration maxWriteDuration = new Duration(100);
    private Duration maxReadDuration = new Duration(100);
    
    public MongoIOProbe(Mongo mongo) {
        collection = mongo.getDB("healthprobe").getCollection("health");
        hostId = anIdentifierForThisHost();
    }

    @Override
    public ProbeResult probe() throws Exception {
        ProbeResult result = new ProbeResult(title());
        String value = idGen.generate();
        write(result, value);
        read(result, value);
        return result;
    }
    
    private void read(ProbeResult probe, String value) {
        long startTime = System.currentTimeMillis();
        DBObject found = collection.findOne(new BasicDBObject(MongoConstants.ID, hostId));
        long duration = System.currentTimeMillis() - startTime;
        if (found == null) {
            probe.addFailure("read", "row not found");
            return;
        }
        if (!value.equals(found.get("value"))) {
            probe.addFailure("read", "read possible stale value");
            return;
        }
        probe.add("read", duration + "ms",  duration < maxReadDuration.getMillis());
    }

    private void write(ProbeResult probe, String value) {
        long startTime = System.currentTimeMillis();
        BasicDBObject dbo = new BasicDBObject(MongoConstants.ID, hostId);
        dbo.put("value", value);
        WriteResult writeAttempt = collection.save(dbo);
        CommandResult result = writeAttempt.getLastError();
        long duration = System.currentTimeMillis() - startTime;
        
        if ((Double) result.get("ok") != 1.0) {
            probe.addFailure("write", result.getErrorMessage());
        } else {
           probe.add("write", duration + "ms", duration < maxWriteDuration.getMillis());
        }
    }

    @Override
    public String title() {
        return "Mongo IO probe";
    }

    @Override
    public String slug() {
        return "mongo-io";
    }
    
    public MongoIOProbe withWriteConcern(WriteConcern writeConcern) {
        collection.setWriteConcern(writeConcern);
        return this;
    }
    
    // We just need a string unique to this host so that probes on different
    // hosts don't collide.
    private static String anIdentifierForThisHost() {
        try {
            return anIpAddress().replaceAll("[^0-9]", "");
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static String anIpAddress() throws SocketException {
        for (NetworkInterface netint : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!"lo0".equals(netint.getDisplayName())) {
                Enumeration<InetAddress> addresses = netint.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    return addresses.nextElement().getHostAddress();
                }
            }
        }
        throw new IllegalArgumentException("Cannot determine IP address for this host");
    }
}

