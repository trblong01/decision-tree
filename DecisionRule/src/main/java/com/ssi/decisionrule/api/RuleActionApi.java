package com.ssi.decisionrule.api;

import com.ssi.decisionrule.dao.RuleActionDAO;
import com.ssi.decisionrule.entity.RuleNodeAction;
import com.ssi.decisionrule.process.RuleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ruleaction")
@CrossOrigin(origins = "*")
public class RuleActionApi {

    @Autowired
    RuleActionDAO ruleActionDAO;


    @GetMapping(value = "/{id}")
    public static List<RuleNodeAction> getByNodeId(@PathVariable("id") int nodeid) {
        Map<Integer, List<RuleNodeAction>> mapAction = RuleCache.getInstance().getMapRuleAction();
        if (mapAction != null) {
            return mapAction.get(nodeid);
        }

        return null;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Integer create(@RequestBody RuleNodeAction body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }
        return ruleActionDAO.create(body);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody RuleNodeAction body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }
        return ruleActionDAO.update(body);
    }

    @DeleteMapping(value = "/{ruleNodeId}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public int delete(@PathVariable("id") int id, @PathVariable("ruleNodeId") int ruleNodeId) {
        return ruleActionDAO.delete(id, ruleNodeId);
    }
}
