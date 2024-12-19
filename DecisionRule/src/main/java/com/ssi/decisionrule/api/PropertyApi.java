package com.ssi.decisionrule.api;


import com.ssi.decisionrule.entity.Property;
import com.ssi.decisionrule.process.RuleCache;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/property")
@CrossOrigin(origins = "*")
public class PropertyApi {

    @GetMapping
    public List<Property> findAll() {
        return new ArrayList<>(RuleCache.getInstance().getMapRuleProperty().values());
    }

    @GetMapping(value = "/{id}")
    public static Property getById(@PathVariable("id") int id) {
        return RuleCache.getInstance().getMapRuleProperty().get(id);
    }

}
