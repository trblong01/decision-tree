/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;

import com.ssi.decisionrule.inout.RuleInput;
import com.ssi.decisionrule.process.RuleConstant;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longtb
 */
public class RuleExp {

    private final static Logger logger = Logger.getLogger(RuleExp.class);
    // Operand Type
    public final static int TYPE_OPERAND_NO = 0;
    public final static int TYPE_OPERAND_YES = 1;

    // Operation Type
    public final static int TYPE_OPERATION_SMALL = 1;
    public final static int TYPE_OPERATION_LARGE = 3;
    public final static int TYPE_OPERATION_EQUAL = 2;
    public final static int TYPE_OPERATION_DIFFRE = 4;
    public final static int TYPE_OPERATION_IN = 5;
    public final static int TYPE_OPERATION_NOTIN = 6;


    private int id;
    private int propertyLeftId;
    private int propertyRightId;
    private int functionLeftId;
    private int functionRightId;
    private String paramSecondLeft;
    private String paramFirstLeft;
    private String paramSecondRight;
    private String paramFirstRight;
    private int operation;
    private int typeOperand;
    private RuleFunc functionLeft;
    private RuleFunc functionRight;
    private int ruleNodeId;

    // Function for execute Expression
    public boolean execute(RuleInput input) {
        boolean bFlag;
        ExpressionResult resultRight;

        String propertyLeft = input.getMapProp().get(this.propertyLeftId);
        String propertyRight = input.getMapProp().get(this.propertyRightId);

        ExpressionResult resultLeft = executeFunction(
                this.functionLeft, this.getParamFirstLeft(), this.getParamSecondLeft(),
                propertyLeft, input);

        if (resultLeft.getType() == ExpressionResult.RET_TYPE_NULL) {
            logger.error("ResultLeft of ruleExpId " + this.getId() + " return NULL");
            return false;
        }

        resultRight = new ExpressionResult();
        if (typeOperand == TYPE_OPERAND_YES) {

            resultRight.setStrValue(paramFirstRight);
            resultRight.setType(ExpressionResult.RET_TYPE_STRING);
        } else {
            resultRight = executeFunction(
                    this.functionRight, this.getParamFirstRight(), this.getParamSecondRight(),
                    propertyRight, input);
        }

        if (resultRight.getType() == ExpressionResult.RET_TYPE_NULL) {
            logger.error("ResultRight of ruleExpId " + this.getId() + " return NULL");
            return false;
        }

        // Comparision
        switch (operation) {
            case TYPE_OPERATION_SMALL:
                bFlag = Long.parseLong(resultLeft.toStringValue()) < Long.parseLong(resultRight.toStringValue());
                break;
            case TYPE_OPERATION_LARGE:
                bFlag = Long.parseLong(resultLeft.toStringValue()) > Long.parseLong(resultRight.toStringValue());
                break;
            case TYPE_OPERATION_EQUAL:
                bFlag = (resultLeft.toStringValue() == null ? resultRight.toStringValue() == null : resultLeft.toStringValue().equals(resultRight.toStringValue()));
                break;
            case TYPE_OPERATION_DIFFRE:
                bFlag = !(resultLeft.toStringValue() == null ? resultRight.toStringValue() == null : resultLeft.toStringValue().equals(resultRight.toStringValue()));
                break;
            case TYPE_OPERATION_IN:
                String strValueLeft = "";
                List<String> listStr = new ArrayList<>();
                if (resultLeft.getType() == ExpressionResult.RET_TYPE_STRING) {
                    strValueLeft = resultLeft.getStrValue();
                }
                if (resultRight.getType() == ExpressionResult.RET_TYPE_ARRAY) {
                    listStr = resultRight.getListStr();
                }
                bFlag = listStr.contains(strValueLeft);
                break;
            case TYPE_OPERATION_NOTIN:
                String valueLeftNotIn = "";
                List<String> list = new ArrayList<>();
                if (resultLeft.getType() == ExpressionResult.RET_TYPE_STRING) {
                    valueLeftNotIn = resultLeft.getStrValue();
                }

                if (resultRight.getType() == ExpressionResult.RET_TYPE_ARRAY) {
                    list = resultRight.getListStr();
                }

                bFlag = !list.contains(valueLeftNotIn);
                break;
            default:
                logger.error(String.format("Unknown operation %d. Not supported", getOperation()));  // Unknown type
                bFlag = false;
                break;
        }

        return bFlag;
    }

    public ExpressionResult executeFunction(
            RuleFunc func,
            String param1,
            String param2,
            String property, RuleInput input) {

        ExpressionResult result = new ExpressionResult();
        int iParam1;
        int iParam2;

        if (func == null || func.getId() == 0) {
            if (property == null) {
                result.setType(ExpressionResult.RET_TYPE_NULL);
                return result;
            }
            result.setStrValue(property);
            result.setType(ExpressionResult.RET_TYPE_STRING);

            return result;
        }

        logger.info("Execute function " + func.getName());
        switch (func.getId()) {
            case RuleConstant.Function.RULE_LENGTH_FUNC:
                result.setLongValue(property.length());
                result.setType(ExpressionResult.RET_TYPE_INT);
                break;
            case RuleConstant.Function.RULE_SUBSTR_FUNC:
                iParam1 = Integer.parseInt(param1);
                iParam2 = Integer.parseInt(param2);
                result.setStrValue(property.substring(iParam1, iParam2));
                result.setType(ExpressionResult.RET_TYPE_STRING);
                break;
            case RuleConstant.Function.RULE_PREFIX_FUNC:
                iParam1 = Integer.parseInt(param1);
                result.setStrValue(property.substring(0, iParam1));
                result.setType(ExpressionResult.RET_TYPE_STRING);
                break;
            case RuleConstant.Function.RULE_SUFFIX_FUNC:
                iParam1 = Integer.parseInt(param1);
                result.setStrValue(func.suffix(property, iParam1));
                result.setType(ExpressionResult.RET_TYPE_STRING);
                break;

            default:
                logger.error(String.format("Rule function not supported : %s[%d]",
                        func.getName(), func.getId()));
                result.setType(ExpressionResult.RET_TYPE_ERROR);
        }

        return result;
    }

    // Run python script
    public ExpressionResult executeFunctionScript(
            RuleFunc func,
            String param1,
            String param2,
            String property, RuleInput input) {
        return null;

        //call script by RuleFunc name is same name of file
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param ruleExpressionId the id to set
     */
    public void setId(int ruleExpressionId) {
        this.id = ruleExpressionId;
    }

    /**
     * @return the propertyLeftId
     */
    public int getPropertyLeftId() {
        return propertyLeftId;
    }

    /**
     * @param propertyLeftId the propertyLeftId to set
     */
    public void setPropertyLeftId(int propertyLeftId) {
        this.propertyLeftId = propertyLeftId;
    }

    /**
     * @return the propertyRightId
     */
    public int getPropertyRightId() {
        return propertyRightId;
    }

    /**
     * @param propertyRightId the propertyRightId to set
     */
    public void setPropertyRightId(int propertyRightId) {
        this.propertyRightId = propertyRightId;
    }

    /**
     * @return the functionLeftId
     */
    public int getFunctionLeftId() {
        return functionLeftId;
    }

    /**
     * @param functionLeftId the functionLeftId to set
     */
    public void setFunctionLeftId(int functionLeftId) {
        this.functionLeftId = functionLeftId;
    }

    /**
     * @return the functionRightId
     */
    public int getFunctionRightId() {
        return functionRightId;
    }

    /**
     * @param functionRightId the functionRightId to set
     */
    public void setFunctionRightId(int functionRightId) {
        this.functionRightId = functionRightId;
    }

    /**
     * @return the paramSecondLeft
     */
    public String getParamSecondLeft() {
        return paramSecondLeft;
    }

    /**
     * @param paramSecondLeft the paramSecondLeft to set
     */
    public void setParamSecondLeft(String paramSecondLeft) {
        this.paramSecondLeft = paramSecondLeft;
    }

    /**
     * @return the paramFirstLeft
     */
    public String getParamFirstLeft() {
        return paramFirstLeft;
    }

    /**
     * @param paramFirstLeft the paramFirstLeft to set
     */
    public void setParamFirstLeft(String paramFirstLeft) {
        this.paramFirstLeft = paramFirstLeft;
    }

    /**
     * @return the paramSecondRight
     */
    public String getParamSecondRight() {
        return paramSecondRight;
    }

    /**
     * @param paramSecondRight the paramSecondRight to set
     */
    public void setParamSecondRight(String paramSecondRight) {
        this.paramSecondRight = paramSecondRight;
    }

    /**
     * @return the paramFirstRight
     */
    public String getParamFirstRight() {
        return paramFirstRight;
    }

    /**
     * @param paramFirstRight the paramFirstRight to set
     */
    public void setParamFirstRight(String paramFirstRight) {
        this.paramFirstRight = paramFirstRight;
    }

    /**
     * @return the operation
     */
    public int getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(int operation) {
        this.operation = operation;
    }

    /**
     * @return the typeOperand
     */
    public int getTypeOperand() {
        return typeOperand;
    }

    /**
     * @param typeOperand the typeOperand to set
     */
    public void setTypeOperand(int typeOperand) {
        this.typeOperand = typeOperand;
    }

    /**
     * @return the functionLeft
     */
    public RuleFunc getFunctionLeft() {
        return functionLeft;
    }

    /**
     * @param functionLeft the functionLeft to set
     */
    public void setFunctionLeft(RuleFunc functionLeft) {
        this.functionLeft = functionLeft;
    }

    /**
     * @return the functionRight
     */
    public RuleFunc getFunctionRight() {
        return functionRight;
    }

    /**
     * @param functionRight the functionRight to set
     */
    public void setFunctionRight(RuleFunc functionRight) {
        this.functionRight = functionRight;
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
                "id:" + id +
                ", propertyLeftId:" + propertyLeftId +
                ", propertyRightId:" + propertyRightId +
                ", functionLeftId:" + functionLeftId +
                ", functionRightId:" + functionRightId +
                ", paramSecondLeft:'" + paramSecondLeft + '\'' +
                ", paramFirstLeft:'" + paramFirstLeft + '\'' +
                ", paramSecondRight:'" + paramSecondRight + '\'' +
                ", paramFirstRight:'" + paramFirstRight + '\'' +
                ", operation:" + operation +
                ", typeOperand:" + typeOperand +
                ", functionLeft:" + functionLeft +
                ", functionRight:" + functionRight +
                ", ruleNodeId:" + ruleNodeId +
                '}';
    }
}
