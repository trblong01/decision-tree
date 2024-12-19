package com.ssi.decisionrule.api;

import com.ssi.decisionrule.dao.RuleMapDAO;
import com.ssi.decisionrule.entity.RuleMap;
import com.ssi.decisionrule.entity.RuleNode;
import com.ssi.decisionrule.inout.RuleInput;
import com.ssi.decisionrule.process.RuleCache;
import com.ssi.decisionrule.process.RuleConstant;
import com.ssi.decisionrule.process.RuleProcess;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rulemap")
@CrossOrigin(origins = "*")
public class RuleMapApi {

    @Autowired
    RuleMapDAO ruleMapDAO;

    @GetMapping
    public List<RuleMap> findAll() {
        return RuleCache.getInstance().getListRuleMap();
    }

    @GetMapping(value = "/{id}")
    public static RuleMap getById(@PathVariable("id") int id) {
        return RuleCache.getInstance().getListRuleMap().stream().filter(e -> e.getId() == id).findFirst().get();
    }

    @GetMapping(value = "/tree/{id}")
    public static String getRuleTreeById(@PathVariable("id") int id) {
        RuleNode root = RuleCache.getInstance().getRuleMapTree().get(id);
        if (root != null) {
            return buildTree2JSON(root, null).toString();
        }
        return null;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Integer create(@RequestBody RuleMap body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }
        return ruleMapDAO.create(body);
    }

    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    public String run(@RequestBody JSONObject body) {
        if (body == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Body is emtpy");
        }

        int ruleMapId = body.optInt("ruleMapId", 0);
        if (ruleMapId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ruleMapId is <=0 ");
        }

        JSONObject jsonProperties = body.getJSONObject("properties");
        if (jsonProperties == null || jsonProperties.length() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Properties are empty");
        }
        RuleInput input = new RuleInput(jsonProperties, ruleMapId);
        input.convertStringToID();
        return RuleProcess.getInstance().process(input).toString();
    }

    private static JSONObject buildTree2JSON(RuleNode node, String branch) {
        JSONObject result = new JSONObject();
        result.put("name", node.getRuleName());
        result.put("id", node.getRuleNodeID());
        if (node.isRoot()) {
            result.put("isRoot", true);
        } else {
            result.put("parentId", node.getParentNodeId());
        }
        if (branch != null) {
            result.put("branch", branch);
        }

//        if(node.getNextNoRuleNode() != null || node.getEvent() !=null){
        List<JSONObject> listChildren = new ArrayList<>();

        if (node.getNextNoRuleNode() != null) {
            listChildren.add(buildTree2JSON(node.getNextNoRuleNode(), RuleConstant.BRANCH_NO));
        }
        if (node.getNextYesRuleNode() != null) {
            listChildren.add(buildTree2JSON(node.getNextYesRuleNode(), RuleConstant.BRANCH_YES));
        }

        result.put("children", listChildren);
//        }

        return result;
    }

}
