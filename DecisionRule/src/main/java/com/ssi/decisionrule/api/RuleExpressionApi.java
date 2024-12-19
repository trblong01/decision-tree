package com.ssi.decisionrule.api;

import com.ssi.decisionrule.dao.RuleExpressionDAO;
import com.ssi.decisionrule.entity.RuleExp;
import com.ssi.decisionrule.process.RuleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expression")
@CrossOrigin(origins = "*")
public class RuleExpressionApi {
    @Autowired
    RuleExpressionDAO ruleExpressionDAO;


    @GetMapping(value = "/{id}")
    public static List<RuleExp> getByNodeId(@PathVariable("id") int nodeid) {
        return RuleCache.getInstance().getMapRuleExp().values().stream()
                .filter(e -> e.getRuleNodeId() == nodeid).collect(Collectors.toList());

    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Integer create(@RequestBody RuleExp body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }
        return ruleExpressionDAO.create(body);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody RuleExp body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }
        return ruleExpressionDAO.update(body);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public int delete(@PathVariable("id") int id) {
        return ruleExpressionDAO.delete(id);
    }
}
