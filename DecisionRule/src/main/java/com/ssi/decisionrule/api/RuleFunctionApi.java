package com.ssi.decisionrule.api;

import com.ssi.decisionrule.entity.RuleFunc;
import com.ssi.decisionrule.process.RuleCache;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/function")
@CrossOrigin(origins = "*")
public class RuleFunctionApi {

    @GetMapping
    public List<RuleFunc> findAll() {
        return new ArrayList<>(RuleCache.getInstance().getMapRuleFunct().values());
    }

    @GetMapping(value = "/{id}")
    public static RuleFunc getById(@PathVariable("id") int id) {
        return RuleCache.getInstance().getMapRuleFunct().get(id);
    }

}
