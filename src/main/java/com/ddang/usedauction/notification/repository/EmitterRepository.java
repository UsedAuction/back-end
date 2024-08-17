package com.ddang.usedauction.notification.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public void saveEventCache(String eventCacheId, Object event) {
        eventCache.put(eventCacheId, event);
    }

    // 해당 memberId와 관련된 모든 emitter 찾기
    public Map<String, SseEmitter> findAllEmitterStartWithMemberId(String memberId) {
        return emitters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(memberId))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // 해당 memberId와 관련된 모든 이벤트 찾기
    public Map<String, Object> findAllEventCacheStartWithMemberId(String memberId) {
        return eventCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(memberId))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
