/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;

/**
 * @author longtb
 */
public class ResponseNode {

    private boolean isYes = false;
    private int isExitNode = 0;

    /**
     * @return the hasResultNode
     */
    public boolean isYes() {
        return isYes;
    }

    /**
     * @param resultNode the hasResultNode to set
     */
    public void setIsYes(boolean resultNode) {
        this.isYes = resultNode;
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

}
