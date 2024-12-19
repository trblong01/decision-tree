/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;

/**
 * @author longtb
 */
public class RuleNodeAction {

    private int actionId;
    private int srcPropId;
    private int destPropId;
    private int funcId;
    private String param1;
    private String param2;
    private RuleFunc function;
    private int ruleNodeId;

    /**
     * @return the actionId
     */
    public int getActionId() {
        return actionId;
    }

    /**
     * @param actionId the actionId to set
     */
    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    /**
     * @return the srcPropId
     */
    public int getSrcPropId() {
        return srcPropId;
    }

    /**
     * @param srcPropId the srcPropId to set
     */
    public void setSrcPropId(int srcPropId) {
        this.srcPropId = srcPropId;
    }

    /**
     * @return the destPropId
     */
    public int getDestPropId() {
        return destPropId;
    }

    /**
     * @param destPropId the destPropId to set
     */
    public void setDestPropId(int destPropId) {
        this.destPropId = destPropId;
    }

    /**
     * @return the funcId
     */
    public int getFuncId() {
        return funcId;
    }

    /**
     * @param funcId the funcId to set
     */
    public void setFuncId(int funcId) {
        this.funcId = funcId;
    }

    /**
     * @return the param1
     */
    public String getParam1() {
        return param1;
    }

    /**
     * @param param1 the param1 to set
     */
    public void setParam1(String param1) {
        this.param1 = param1;
    }

    /**
     * @return the param2
     */
    public String getParam2() {
        return param2;
    }

    /**
     * @param param2 the param2 to set
     */
    public void setParam2(String param2) {
        this.param2 = param2;
    }

    /**
     * @return the function
     */
    public RuleFunc getFunction() {
        return function;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(RuleFunc function) {
        this.function = function;
    }

    public int getRuleNodeId() {
        return ruleNodeId;
    }

    public void setRuleNodeId(int ruleNodeId) {
        this.ruleNodeId = ruleNodeId;
    }

    @Override
    public String toString() {
        return "{" +
                "actionId:" + actionId +
                ", srcPropId:" + srcPropId +
                ", destPropId:" + destPropId +
                ", funcId:" + funcId +
                ", param1:'" + param1 + '\'' +
                ", param2:'" + param2 + '\'' +
                ", function:" + function +
                ", ruleNodeId:" + ruleNodeId +
                '}';
    }
}
