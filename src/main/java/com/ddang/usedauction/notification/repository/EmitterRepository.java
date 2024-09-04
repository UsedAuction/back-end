package com.ddang.usedauction.notification.repository;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Repository
public class EmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    // emitter 저장
    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {

        emitters.put(emitterId, sseEmitter);

        return sseEmitter;
    }

    // emitter 삭제
    public void deleteById(String emitterId) {
        emitters.remove(emitterId);
    }

    // 이벤트 저장
    public void saveEventCache(String emitterId, Object event) {
        eventCache.put(emitterId, event);
    }

    // 해당 memberId와 관련된 모든 emitter 찾기
    public Map<String, SseEmitter> findAllEmitterStartWithMemberId(String memberId) {

        Map<String, SseEmitter> result = emitters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(memberId + "_"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }

    // 해당 memberId와 관련된 모든 이벤트 찾기
    public Map<String, Object> findAllEventCacheStartWithMemberId(String memberId) {
        return eventCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(memberId + "_"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
