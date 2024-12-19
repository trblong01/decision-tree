/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.entity;

/**
 * @author longtb
 */
public class Event {

    private int id;
    private String name;
    private RuleNode rootNode;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the rootNode
     */
    public RuleNode getRootNode() {
        return rootNode;
    }

    /**
     * @param rootNode the rootNode to set
     */
    public void setRootNode(RuleNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public String toString() {
        return "{" + "id=" + id + ", name=" + name + '}';
    }
}
