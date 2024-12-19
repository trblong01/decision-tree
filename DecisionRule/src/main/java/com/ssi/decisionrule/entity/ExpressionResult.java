/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author longtb
 */
public class ExpressionResult {

    // Define return type 
    public final static int RET_TYPE_NULL = 0;
    public final static int RET_TYPE_INT = 1;
    public final static int RET_TYPE_STRING = 2;
    public final static int RET_TYPE_ARRAY = 3;
    public final static int RET_TYPE_BOOL = 4;
    public final static int RET_TYPE_ERROR = -1;

    private int type;
    private long longValue;
    private String strValue;
    private List<String> listStr = new ArrayList<>();

    public boolean greater(Object anObject) {
        if (anObject instanceof ExpressionResult) {
            if (type == RET_TYPE_STRING) {
                long i = Long.parseLong(strValue);
                return i > ((ExpressionResult) anObject).getNumberValue();
            } else {
                return longValue > (long) anObject;
            }
        }

        if (anObject instanceof String) {
            String anotherString = (String) anObject;
            if (type == RET_TYPE_STRING) {
                return strValue.compareTo(anotherString) > 0;
            } else {
                String str = String.format("%d", longValue);
                return str.compareTo(anotherString) > 0;
            }
        }

        if (anObject instanceof Long) {
            if (type == RET_TYPE_STRING) {
                long i = Long.parseLong(strValue);
                return i > (long) anObject;
            } else {
                return longValue > (long) anObject;
            }
        }

        return false;
    }

    public boolean lesser(Object anObject) {
        if (anObject instanceof String) {
            String anotherString = (String) anObject;
            if (type == RET_TYPE_STRING) {
                return strValue.compareTo(anotherString) < 0;
            } else {
                String str = String.format("%d", longValue);
                return str.compareTo(anotherString) < 0;
            }
        }

        if (anObject instanceof Long) {
            if (type == RET_TYPE_STRING) {
                long i = Long.parseLong(strValue);
                return i < (long) anObject;
            } else {
                return longValue < (long) anObject;
            }

        }

        return false;
    }

    public boolean lesserOrEqual(Object anObject) {
        if (anObject instanceof String) {
            String anotherString = (String) anObject;
            if (type == RET_TYPE_STRING) {
                return strValue.compareTo(anotherString) <= 0;
            } else {
                String str = String.format("%d", longValue);
                return str.compareTo(anotherString) <= 0;
            }
        }

        if (anObject instanceof Long) {
            if (type == RET_TYPE_STRING) {
                long i = Long.valueOf(strValue);
                return i <= (long) anObject;
            } else {
                return longValue <= (long) anObject;
            }
        }

        return false;
    }

    public long getNumberValue() {
        if (type == RET_TYPE_INT) {
            return (int) this.longValue;
        } else {
            return Long.valueOf(strValue);
        }
    }

    public String toStringValue() {
        if (type == RET_TYPE_INT) {
            return String.format("%d", longValue);
        } else {
            return strValue;
        }
    }

    public Object getValue() {
        if (type == RET_TYPE_INT) {
            return longValue;
        } else {
            return strValue;
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public List<String> getListStr() {
        return listStr;
    }

    public void setListStr(List<String> listStr) {
        this.listStr = listStr;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }

        if (anObject instanceof String) {
            String anotherString = (String) anObject;
            if (type == RET_TYPE_STRING) {
                return strValue.equals(anotherString);
            } else {
                String str = String.format("%d", longValue);
                return str.equals(anotherString);
            }
        }

        if (anObject instanceof Integer) {
            if (type == RET_TYPE_STRING) {
                Integer i = Integer.valueOf(strValue);
                return anObject == i;
            } else {
                return (Integer) anObject == (int) longValue;
            }

        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.type;
        hash = 19 * hash + (int) (this.longValue ^ (this.longValue >>> 32));
        hash = 19 * hash + Objects.hashCode(this.strValue);
        return hash;
    }
}