/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;

import com.ssi.decisionrule.inout.RuleInput;
import com.ssi.decisionrule.process.RuleCache;
import com.ssi.decisionrule.process.RuleConstant;
import com.ssi.decisionrule.utils.DateTimeUtils;
import org.apache.log4j.Logger;

import java.sql.Date;
import java.util.List;

/**
 * @author longtb
 */
public class RuleNode {
    private final static Logger logger = Logger.getLogger(RuleNode.class);

    // Constant for class typeTree
    public static final int TYPE_YES_TREE = 1;               // parent_node=rule_node, child_node = yes node
    public static final int TYPE_NO_TREE = 2;                // parent_node=rule_node child_node=no node
//    public static final int TYPE_TREE_ROOT = 3;              // root node of tree
//    public static final int TYPE_TREE_ROOT_OF_EVENT = 4;     // parent_node = event, child_node = rule_node

    private int ruleMapId;
    private int typeTree;
    private int childNodeID;
    private int eventId = 0;
    private int ruleNodeID;
    private int isExitNode;
    private String ruleExpress;
    private String ruleName;
    private Date effDate;
    private Date expDate;
    private int resultCode = RuleConstant.ResultCode.RESULT_SUCCESS;
    private int parentNodeId;
    private Event event;                        // Case tree_type = 1,4
    private RuleNode nextYesRuleNode;        // Case tree_type = 2
    private RuleNode nextNoRuleNode;        // Case tree_type = 2
    private boolean isRoot;                      // Case tree_type = 3
    private List<RuleNodeAction> actionList;
    private RuleExpLink nodeExpLink;

    public ResponseNode execute(RuleInput input) {
        logger.info("Execute RuleNode" + this.getRuleNodeID() + "[" + this.getRuleName() + "]");

        // Find Event if effected
        if (isEffDate() && isNotExpired()) {
            // Do Action list
            if (getActionList() != null) {
                ExpressionResult retExp;
                RuleExp exp = new RuleExp();
                // Do each Action
                for (int i = 0; i < getActionList().size(); i++) {
                    RuleNodeAction nodeAction = getActionList().get(i);
                    logger.info("Execute actionNode[" + nodeAction.getActionId() + "]");
                    retExp = exp.executeFunction(
                            nodeAction.getFunction(),
                            nodeAction.getParam1(),
                            nodeAction.getParam2(),
                            input.getMapProp().get(nodeAction.getSrcPropId()), input);

                    if (retExp.getType() != ExpressionResult.RET_TYPE_ERROR && nodeAction.getDestPropId() > 0) {
                        input.getMapProp().put(nodeAction.getDestPropId(), retExp.toStringValue());
                    }
                }
            }


            if (nodeExpLink != null) {
                ResponseNode responeNode = new ResponseNode();
                responeNode.setIsExitNode(isExitNode);
                responeNode.setIsYes(nodeExpLink.execute(input));
                logger.debug("=== Excute RuleExpLink of rulenode " + this.ruleNodeID
                        + " is " + responeNode.isYes());
                return responeNode;
            } else {
                logger.info("No expression in rule node " + this.ruleNodeID);
                resultCode = RuleConstant.ResultCode.RESULT_ERROR_NO_EXPRESSION;
            }

        } else {
            resultCode = RuleConstant.ResultCode.RESULT_ERROR_NOT_IN_EFFECT_DATE;
            logger.error("RuleNode[" + this.getRuleNodeID() + "] expire or not effected");
        }

        return null;
    }

    public boolean expressionParser(RuleCache ruleCache) {
        if (ruleExpress == null
                || ruleExpress.trim().isEmpty()
                || ruleExpress.equalsIgnoreCase("null")
                || ruleExpress.equalsIgnoreCase("(null)")) {
            return true;
        }

        int len = ruleExpress.length();

        StringBuilder pStr = new StringBuilder();
        char ch;

        for (int i = 0; i < len; i++) {
            ch = ruleExpress.charAt(i);
            if (((ch >= '0') && (ch <= '9'))
                    || (ch == '(') || (ch == ')')
                    || (ch == 'n') || (ch == 'o')) {
                pStr.append(ch);
            }
        }
        // Call ExpParser
        boolean ret = false;
        nodeExpLink = null;
        if (pStr.length() > 0) {
            nodeExpLink = new RuleExpLink();
            ret = nodeExpLink.expParser(pStr.toString(), ruleCache);
        }

        return ret;
    }

    boolean isNotExpired() {
        Date curDate = new Date(System.currentTimeMillis());
        if (this.getExpDate() != null) {
            return curDate.before(this.getExpDate());
        } else {
            return true;
        }
    }

    boolean isEffDate() {
        Date curDate = new Date(System.currentTimeMillis());
        if (this.getEffDate() != null) {
            return curDate.compareTo(this.getEffDate()) >=0;
        } else {
            return false;
        }
    }

    public int getParentNodeId() {
        return parentNodeId;
    }

    public void setParentNodeId(int parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    /**
     * @return the ruleNodeID
     */
    public int getRuleNodeid() {
        return ruleNodeID;
    }

    /**
     * @param ruleNodeid the ruleNodeID to set
     */
    public void setRuleNodeid(int ruleNodeid) {
        this.ruleNodeID = ruleNodeid;
    }

    /**
     * @return the isExitNode
     */
    public int getIsExitNode() {
        return isExitNode;
    }

    /**
     * @param isExitNode the isExitNode to set
     */
    public void setIsExitNode(int isExitNode) {
        this.isExitNode = isExitNode;
    }

    /**
     * @return the ruleExpress
     */
    public String getRuleExpress() {
        return ruleExpress;
    }

    /**
     * @param ruleExpress the ruleExpress to set
     */
    public void setRuleExpress(String ruleExpress) {
        this.ruleExpress = ruleExpress;
        expressionParser(RuleCache.getInstance());
    }

    /**
     * @return the ruleName
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * @param ruleName the ruleName to set
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * @return the effDate
     */
    public Date getEffDate() {
        return effDate;
    }

    /**
     * @param effDate the effDate to set
     */
    public void setEffDate(Date effDate) {
        this.effDate = effDate;
    }

    /**
     * @return the expDate
     */
    public Date getExpDate() {
        return expDate;
    }

    /**
     * @param expDate the expDate to set
     */
    public void setExpDate(Date expDate) {
        this.expDate = expDate;
    }

    /**
     * @return the resultCode
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * @param resultCode the resultCode to set
     */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * @return the actionList
     */
    public List<RuleNodeAction> getActionList() {
        return actionList;
    }

    /**
     * @param actionResultList the actionList to set
     */
    public void setActionList(List<RuleNodeAction> actionResultList) {
        this.actionList = actionResultList;
    }

    /**
     * @return the typeTree
     */
    public int getTypeTree() {
        return typeTree;
    }

    /**
     * @param typeTree the typeTree to set
     */
    public void setTypeTree(int typeTree) {
        this.typeTree = typeTree;
    }

    /**
     * @return the childNodeID
     */
    public int getChildNodeID() {
        return childNodeID;
    }

    /**
     * @param childNodeID the childNodeID to set
     */
    public void setChildNodeID(int childNodeID) {
        this.childNodeID = childNodeID;
    }

    /**
     * @return the eventId
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * @param eventId the eventId to set
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * @return the nextRuleNode
     */
    public RuleNode getNextNoRuleNode() {
        return nextNoRuleNode;
    }

    /**
     * @param nextNoRuleNode the nextRuleNode to set
     */
    public void setNextNoRuleNode(RuleNode nextNoRuleNode) {
        this.nextNoRuleNode = nextNoRuleNode;
    }


    /**
     * @return the fRoot
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * @param root the fRoot to set
     */
    public void setRoot(boolean root) {
        this.isRoot = root;
    }

    public RuleNode getNextYesRuleNode() {
        return nextYesRuleNode;
    }

    public void setNextYesRuleNode(RuleNode nextYesRuleNode) {
        this.nextYesRuleNode = nextYesRuleNode;
    }

    public int getRuleNodeID() {
        return ruleNodeID;
    }

    public void setRuleNodeID(int ruleNodeID) {
        this.ruleNodeID = ruleNodeID;
    }


    public RuleExpLink getNodeExpLink() {
        return nodeExpLink;
    }

    public void setNodeExpLink(RuleExpLink nodeExpLink) {
        this.nodeExpLink = nodeExpLink;
    }

    public int getRuleMapId() {
        return ruleMapId;
    }

    public void setRuleMapId(int ruleMapId) {
        this.ruleMapId = ruleMapId;
    }

    @Override
    public String toString() {
        return "{" +
                "ruleMapId=" + ruleMapId +
                ", typeTree=" + typeTree +
                ", childNodeID=" + childNodeID +
                ", eventId=" + eventId +
                ", ruleNodeID=" + ruleNodeID +
                ", isExitNode=" + isExitNode +
                ", ruleExpress='" + ruleExpress + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", effDate=" + effDate +
                ", expDate=" + expDate +
                ", resultCode=" + resultCode +
                ", parentNodeId=" + parentNodeId +
                ", isRoot=" + isRoot +
                ", actionList=" + actionList +
                '}';
    }


    public String toString(List<RuleExp> lstRuleExp) {
        return "{" +
                "ruleMapId:" + ruleMapId +
                ", typeTree:" + typeTree +
                ", childNodeID:" + childNodeID +
                ", eventId:" + eventId +
                ", ruleNodeID:" + ruleNodeID +
                ", isExitNode:" + isExitNode +
                ", ruleExpress:'" + ruleExpress + '\'' +
                ", ruleName:'" + ruleName + '\'' +
                ", effDate:'" + DateTimeUtils.convertSQLDate2String(effDate) + '\'' +
                ", expDate:'" + DateTimeUtils.convertSQLDate2String(expDate) + '\'' +
                ", resultCode:" + resultCode +
                ", parentNodeId:" + parentNodeId +
                ", isRoot:" + isRoot +
                ", actionList:" + actionList +
                ", ruleExpList:" + lstRuleExp.toString() +
                '}';
    }
}
