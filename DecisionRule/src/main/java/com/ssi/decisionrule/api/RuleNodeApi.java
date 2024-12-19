package com.ssi.decisionrule.api;

import com.ssi.decisionrule.dao.RuleNodeDAO;
import com.ssi.decisionrule.entity.RuleExp;
import com.ssi.decisionrule.entity.RuleNode;
import com.ssi.decisionrule.process.RuleCache;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rulenode")
@CrossOrigin(origins = "*")
public class RuleNodeApi {
    @Autowired
    RuleNodeDAO ruleNodeDAO;

    @GetMapping(value = "/{nodeid}")
    public static String getByNodeId(@PathVariable("nodeid") int nodeid) {
        RuleNode ruleNode = RuleCache.getInstance().getRuleNode(nodeid);
        if (ruleNode == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Not found rule node " + nodeid);
        } else {
            List<RuleExp> listRuleExp = RuleCache.getInstance().getMapRuleExp().values().stream().filter(e -> e.getRuleNodeId() == nodeid)
                    .collect(Collectors.toList());

            return new JSONObject(ruleNode.toString(listRuleExp)).toString();
        }

    }

    @PostMapping
//    @ResponseStatus(HttpStatus.OK)
    public Integer create(@RequestBody Object body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }
        JSONObject data = new JSONObject((LinkedHashMap) body);
        return ruleNodeDAO.create(data);
    }

    @PutMapping
//    @ResponseStatus(HttpStatus.OK)
    public int update(@RequestBody LinkedHashMap body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }
        JSONObject data = new JSONObject(body);
        return ruleNodeDAO.update(data);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public int delete(@PathVariable("id") int id) {
        return ruleNodeDAO.delete(id);
    }
}
