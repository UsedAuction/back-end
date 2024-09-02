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
        log.info("save()save()save()save()save()");
        emitters.put(emitterId, sseEmitter);
        log.info("save emitterId: " + emitterId);
        log.info("save sseEmitter:" + sseEmitter);
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
        log.info("memberId: " + memberId);
        log.info("emitters: {}", emitters);
        log.info("emitters keyset: {}", emitters.keySet());
        Map<String, SseEmitter> result = emitters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(memberId))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        log.info("findAllEmitterStartWithMemberId(): " + result);
        return result;
    }

    // 해당 memberId와 관련된 모든 이벤트 찾기
    public Map<String, Object> findAllEventCacheStartWithMemberId(String memberId) {
        return eventCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(memberId))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
