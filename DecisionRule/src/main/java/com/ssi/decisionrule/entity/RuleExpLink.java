/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;

import com.ssi.decisionrule.inout.RuleInput;
import com.ssi.decisionrule.process.RuleCache;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * @author longtb
 */
public class RuleExpLink {

    private final static Logger logger = Logger.getLogger(RuleExpLink.class);
    private RuleExp expression;
    private int id;
    private ArrayList<RuleExpLink> orList;
    private ArrayList<RuleExpLink> andList;

    boolean execute(RuleInput input) {
        boolean resp;
        if (expression != null) {
            return expression.execute(input);
        }
        if (orList != null) {
            for (RuleExpLink pNext : orList) {
                resp = pNext.execute(input);
                if (resp) {
                    return true;
                }
            }
            return false;
        }

        if (andList != null) {
            for (RuleExpLink pNext : andList) {
                resp = pNext.execute(input);
                if (!resp) {
                    return false;
                }
            }

            return true;
        }

        return true;
    }

    boolean expParser(String strExp, RuleCache ruleCache) {
        int len = strExp.length();
        int pos = 0;
        int i;
        if (len == 0) {
            return false;
        }

        if (strExp.charAt(0) == '(') {
            pos = 1;
            len = len - 1;
        }

        if (strExp.charAt(strExp.length() - 1) == ')') {
            len = len - 1;
        }

        String str = strExp.substring(pos, pos + len);
        try {
            id = Integer.parseInt(str);

            expression = ruleCache.getRuleExpression(id);
            if (expression == null) {
                logger.error(String.format("Expression not found %d", id));
            }

            return true;
        } catch (NumberFormatException e) {
            id = 0;
        }

        RuleExpLink pNew;
        if (str.contains("n")) { // ANDs processing
            andList = new ArrayList<>();
            String[] p = str.split("n");
            for (i = 0; i < p.length; i++) {
                pNew = new RuleExpLink();
                pNew.expParser(p[i], ruleCache);
                andList.add(pNew);
            }
        } else if (str.contains("o")) {  // ORs processing
            orList = new ArrayList<>();
            String[] p = str.split("o");
            for (i = 0; i < p.length; i++) {
                pNew = new RuleExpLink();
                pNew.expParser(p[i], ruleCache);
                orList.add(pNew);
            }
        } else {
            logger.error("Invalid Expression:[" + str + "]");
        }

        return true;
    }

    public boolean update(RuleCache ruleCache) {
        if (ruleCache == null) {
            return false;
        }
        // Update ID
        if (id != 0) {
            expression = ruleCache.getRuleExpression(id);
            if (expression == null) {
                logger.error(String.format("Expression not found %d", id));
                return false;
            }
            return true;
        }
        expression = null;
        // Update chuoi OR
        if (orList != null) {
            for (RuleExpLink pOr : orList) {
                if (pOr == null) {
                    return false;
                }
                pOr.update(ruleCache);
            }
            return true;
        }
        // Update AND String
        if (andList != null) {
            for (RuleExpLink pAnd : andList) {
                if (pAnd == null) {
                    return false;
                }
                pAnd.update(ruleCache);
            }
            return true;
        }

        return true;
    }
}
