package com.ssi.decisionrule.api;

import com.ssi.decisionrule.entity.Event;
import com.ssi.decisionrule.process.RuleCache;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event")
@CrossOrigin(origins = "*")
public class EventApi {
    @GetMapping
    public List<Event> findAll() {
        Map<Integer, Event> mapEvent = RuleCache.getInstance().getMapEvent();
        if (mapEvent != null) {
            return new ArrayList<>(mapEvent.values());
        }

        return new ArrayList<>();
    }

    @GetMapping(value = "/{id}")
    public static Event getById(@PathVariable("id") int id) {
        return RuleCache.getInstance().getRuleEvent(id);
    }
}
