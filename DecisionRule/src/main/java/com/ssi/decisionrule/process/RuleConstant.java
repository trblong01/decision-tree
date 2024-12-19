/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.process;

/**
 * @author longtb
 */
public class RuleConstant {


    public static class ResultCode {
        public final static int RESULT_ERROR_COMMON = -1;
        public final static int RESULT_ERROR_NOT_IN_EFFECT_DATE = -2;
        public final static int RESULT_ERROR_NO_EXPRESSION = -3;
        public final static int RESULT_ERROR_NO_YES_CHILD = -4;
        public final static int RESULT_ERROR_NO_NO_CHILD = -5;

        public final static int RESULT_SUCCESS = 1;

    }

    public final static int EXIT_NODE = 1;

    public final static String BRANCH_NO = "NO";
    public final static String BRANCH_YES = "YES";

    public static class Property {
        public final static int USER_ID = 1;
        public final static int TIME_DURATION = 2;
        public final static int VOLUMN = 3;
        public final static int NUMBER_OF_CUSTOMER = 4;
        public final static int AREA = 5;
        public final static int POSITION = 6;
    }

    public static class Function {

        public final static int RULE_LENGTH_FUNC = 1;
        public final static int RULE_SUBSTR_FUNC = 2;
        public final static int RULE_PREFIX_FUNC = 3;
        public final static int RULE_SUFFIX_FUNC = 4;

    }


}
