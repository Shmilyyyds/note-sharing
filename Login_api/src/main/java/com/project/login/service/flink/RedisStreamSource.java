package com.project.login.service.flink;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import redis.clients.jedis.*;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;

import java.util.List;
import java.util.Map;

public class RedisStreamSource implements SourceFunction<Map<String, String>> {

    private volatile boolean running = true;
    private final String host;
    private final String streamKey;
    private StreamEntryID lastId;

    public RedisStreamSource(String host, String streamKey) {
        this.host = host;
        this.streamKey = streamKey;
        this.lastId = StreamEntryID.LAST_ENTRY; // 只读新消息
    }

    @Override
    public void run(SourceContext<Map<String, String>> ctx) throws Exception {
        try (Jedis jedis = new Jedis(host, 6379)) {

            while (running) {
                List<Map.Entry<String, List<StreamEntry>>> res =
                        jedis.xread(
                                XReadParams.xReadParams().block(0).count(1),
                                Map.of(streamKey, lastId)
                        );

                if (res != null) {
                    for (Map.Entry<String, List<StreamEntry>> stream : res) {
                        for (StreamEntry entry : stream.getValue()) {
                            ctx.collect(entry.getFields());
                            lastId = entry.getID();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void cancel() {
        running = false;
    }
}

