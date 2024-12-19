/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.inout;

/**
 * @author longtb
 */
public class RuleOutput {

    private int eventId;
    private int resultCode;

    public RuleOutput() {

    }

    public RuleOutput(int evId, int result) {
        eventId = evId;
        resultCode = result;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "{" + "eventId:" + eventId
                + ", resultCode:" + resultCode + '}';
    }

}
