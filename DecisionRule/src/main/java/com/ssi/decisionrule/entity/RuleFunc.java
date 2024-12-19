/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;


/**
 * @author longtb
 */
public class RuleFunc {

    private String name;
    private int id;
    private String description;

    public RuleFunc(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public RuleFunc(int id, String name, String description) {
        this.name = name;
        this.id = id;
        this.description = description;
    }


    String suffix(String str, int param) {
        if (str == null) {
            return null;
        }

        int len = str.length();
        if ((param > 0) && (param <= len)) {
            return str.substring(len - param, len);
        } else if (param > len) {
            return str;
        } else {
            return null;
        }
    }

    long add(long source, long num) {
        source += num;
        return source;
    }

    long substract(long source, long num) {
        source -= num;
        return source;
    }

    long reset(long current, long reset) {
        current = reset;
        return current;
    }

    long rotate(long current, long reset) {
        current = reset;
        return current;
    }

    public int length(String strProperty) {
        if (strProperty != null) {
            return strProperty.length();
        } else {
            return 0;
        }
    }

    public String substr(String str, int offset, int len) {
        if (offset >= len) {
            return "";
        }
        String strReturn;
        if (offset < 0) {
            offset = 0;
        }
        if (len > 0) {
            if (len < str.length()) {
                strReturn = str.substring(offset, len);
            } else {
                strReturn = str.substring(offset);
            }
        } else {
            int index = str.length() + len;
            strReturn = str.substring(offset, index);
        }

        return strReturn;
    }

    /**
     * Ham lay prefix cua xau
     *
     */
    public String prefix(String str, int param) {
        if (param > 0 && param <= str.length()) {
            return str.substring(0, param);
        } else if (param > str.length()) {

            return str;
        } else {
            return "";
        }
    }


    public boolean isContain(int i) {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "{" +
                "name:'" + name + '\'' +
                ", id:" + id +
                ", description:'" + description + '\'' +
                '}';
    }
}
