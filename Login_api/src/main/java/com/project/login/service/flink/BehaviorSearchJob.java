package com.project.login.service.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.login.model.event.UserBehaviorEvent;
import com.project.login.model.event.UserSearchEvent;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BehaviorSearchJob {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final int TOP_N = 10;

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // -------------------- 用户行为流 --------------------
        var behaviorStream = env
                .addSource(new RedisStreamSource(REDIS_HOST, "user_behavior_stream"))
                .map(map -> new UserBehaviorEvent(
                        Long.valueOf(map.get("user_id")),
                        Long.valueOf(map.get("target_id")),
                        map.get("tags"),
                        Integer.parseInt(map.get("weight")),
                        Long.parseLong(map.get("timestamp"))
                ))
                .map(event -> new Tuple2<>(event.userId(), event))
                .returns(TypeInformation.of(new TypeHint<Tuple2<Long, UserBehaviorEvent>>() {}));

        // 复制一个流用于 Top-N 热点计算
        var topNStream = behaviorStream
                .map(tuple -> tuple.f1); // 取 UserBehaviorEvent

        // -------------------- 输出用户行为 --------------------
        behaviorStream.map(tuple -> "BehaviorEvent: " + tuple.f1).print();

        // -------------------- 用户搜索流 --------------------
        var searchStream = env
                .addSource(new RedisStreamSource(REDIS_HOST, "user_search_stream"))
                .map(map -> new UserSearchEvent(
                        Long.valueOf(map.get("user_id")),
                        map.get("keyword"),
                        Long.parseLong(map.get("timestamp"))
                ))
                .map(event -> new Tuple2<>(event.userId(), event))
                .returns(TypeInformation.of(new TypeHint<Tuple2<Long, UserSearchEvent>>() {}));

        searchStream.map(tuple -> "SearchEvent: " + tuple.f1).print();

        // -------------------- 用户画像融合 --------------------
        behaviorStream
                .keyBy(tuple -> tuple.f0)
                .connect(searchStream.keyBy(tuple -> tuple.f0))
                .process(new UserProfileFusionFunction())
                .map(fused -> "FusedProfile: " + fused)
                .print();

        // -------------------- Top-N 热点计算 --------------------
        topNStream
                // 不按 noteId keyBy，而是使用全局 key（所有事件合并计算 Top-N）
                .keyBy(e -> 0) // 所有事件在同一个 key 上
                .process(new KeyedProcessFunction<Integer, UserBehaviorEvent, Void>() {

                    private ListState<UserBehaviorEvent> state;

                    @Override
                    public void open(org.apache.flink.configuration.Configuration parameters) {
                        ListStateDescriptor<UserBehaviorEvent> desc =
                                new ListStateDescriptor<>("behaviorListState", UserBehaviorEvent.class);
                        state = getRuntimeContext().getListState(desc);
                    }

                    @Override
                    public void processElement(UserBehaviorEvent value, Context ctx, Collector<Void> out) throws Exception {
                        state.add(value);
                        // 每 10 秒更新一次 Top-N
                        ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 10000);
                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Void> out) throws Exception {
                        long oneHourAgo = System.currentTimeMillis() - 3600_000L; // 1小时

                        List<UserBehaviorEvent> recentEvents = new ArrayList<>();
                        for (UserBehaviorEvent e : state.get()) {
                            if (e.timestamp() >= oneHourAgo) {
                                recentEvents.add(e);
                            }
                        }

                        // 更新状态，只保留最近1小时事件
                        state.update(recentEvents);

                        // 累加笔记权重
                        Map<Long, Integer> scoreMap = recentEvents.stream()
                                .collect(Collectors.groupingBy(UserBehaviorEvent::noteId,
                                        Collectors.summingInt(UserBehaviorEvent::weight)));

                        // Top-N
                        List<Long> topN = scoreMap.entrySet().stream()
                                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                                .limit(TOP_N)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        // 打印 Top-N 笔记 ID
                        System.out.println("Writing Top-N to Redis: " + topN);

                        // 写入 Redis
                        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
                            jedis.set("hot_notes", objectMapper.writeValueAsString(topN));
                        }
                    }

                });

        env.execute("Behavior + Search Stream with Fusion and Real-time Top-N Hot Notes");
    }
}
