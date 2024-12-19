/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.process;

import com.ssi.decisionrule.entity.ResponseNode;
import com.ssi.decisionrule.entity.RuleNode;
import com.ssi.decisionrule.inout.RuleInput;
import com.ssi.decisionrule.inout.RuleOutput;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * @author longtb
 */
public class RuleProcess {

    private final Logger logger = Logger.getLogger(RuleProcess.class);
    private static RuleProcess ruleProcess;

    private RuleProcess() {
    }

    public static synchronized RuleProcess getInstance() {
        if (ruleProcess == null) {
            ruleProcess = new RuleProcess();
        }

        return ruleProcess;
    }

    public boolean loadCache(Session session) {
        return RuleCache.getInstance().loadCache(session);
    }

    public RuleOutput process(RuleInput input) {
        input.convertStringToID();
        RuleOutput output = new RuleOutput();

        logger.info("========== Processing rule for ruleMap: " + input.getRuleMapId());
        RuleNode ruleNode = RuleCache.getInstance().getRuleMapTree().get(input.getRuleMapId());
        while (true) {
            ResponseNode responseNode = ruleNode.execute(input);

            if (responseNode != null) {
                if (responseNode.getIsExitNode() == RuleConstant.EXIT_NODE) {
                    output.setEventId(ruleNode.getEventId());
                    output.setResultCode(ruleNode.getResultCode());
                    logger.info("Break here. This node is Exit node " + ruleNode.getRuleNodeID());
                    logger.debug(output.toString());
                    return output;
                }

                if (responseNode.isYes()) { // go to Yes Node
//                    if (ruleNode.getEvent() != null) {
//                        if (ruleNode.getEvent().getRootNode() != null) {
//                            ruleNode = ruleNode.getEvent().getRootNode();
//                        } else {
//                            ruleNode.setResultCode(RuleConstant.RULE_SUCCESS);
//                            output.setEventId(ruleNode.getEventId());
//                            output.setResultCode(ruleNode.getResultCode());
//                            return output;
//                        }
//                    }
                    if (ruleNode.getNextYesRuleNode() != null) {
                        ruleNode = ruleNode.getNextYesRuleNode();
                    } else {
                        ruleNode.setResultCode(RuleConstant.ResultCode.RESULT_ERROR_NO_YES_CHILD);
                        logger.info("Break here. No YES child of node " + ruleNode.getRuleNodeID());
                        break;
                    }

                } else {
                    if (ruleNode.getNextNoRuleNode() != null) {
                        ruleNode = ruleNode.getNextNoRuleNode();
                    } else {
                        ruleNode.setResultCode(RuleConstant.ResultCode.RESULT_ERROR_NO_NO_CHILD);
                        logger.info("Break here. No NO child of node " + ruleNode.getRuleNodeID());
                        break;
                    }
                }
            } else {
                logger.info("Break here. No responseNode!!!");
                break;
            }
        }

        if (ruleNode != null) {
            output.setEventId(ruleNode.getEventId());
            output.setResultCode(ruleNode.getResultCode());
            return output;
        }

        logger.debug(output.toString());
        return null;
    }

}
