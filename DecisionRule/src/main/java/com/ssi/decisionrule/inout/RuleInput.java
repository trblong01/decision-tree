/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.inout;


import com.ssi.decisionrule.entity.Property;
import com.ssi.decisionrule.process.RuleCache;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * @author longtb
 */
public class RuleInput {

    private Map<Integer, String> mapProp;

    private JSONObject properties;


    private int ruleMapId;

    public RuleInput(JSONObject properties, int ruleMapId) {
        this.properties = properties;
        this.ruleMapId = ruleMapId;
    }

    public RuleInput() {
    }

    public int getRuleMapId() {
        return ruleMapId;
    }

    public void setRuleMapId(int ruleMapId) {
        this.ruleMapId = ruleMapId;
    }

    public Map<Integer, String> getMapProp() {
        return mapProp;
    }

    public void setMapProp(Map<Integer, String> mapProp) {
        this.mapProp = mapProp;
    }

    public JSONObject getProperties() {
        return properties;
    }

    public void setProperties(JSONObject properties) {
        this.properties = properties;
    }

    public void convertStringToID() {
        if (properties != null && properties.length() > 0) {
            if (mapProp == null) {
                mapProp = new HashMap<>();
            }

            Map<String, Property> mapByName = RuleCache.getInstance().getMapRulePropertyByName();
            for (String key : properties.keySet()) {
                if (mapByName.containsKey(key)) {
                    mapProp.put(mapByName.get(key).getId(), String.valueOf(properties.get(key)));
                }

            }
        }
    }

    @Override
    public String toString() {
        return "{" +
                "properties:" + properties +
                ", ruleMapId:" + ruleMapId +
                '}';
    }


    //    public void clear(){
//        mapProp.clear();       
//    }


}
