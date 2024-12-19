/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.process;

import com.ssi.decisionrule.entity.*;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author longtb
 */
public final class RuleCache {

    private static final Logger logger = Logger.getLogger(RuleCache.class);
    private static RuleCache ruleCache;
    private Map<Integer, RuleExp> mapRuleExp;
    private Map<Integer, RuleNode> mapRuleNode;
    private Map<Integer, Event> mapEvent;
    private Map<Integer, List<RuleNodeAction>> mapRuleAction;
    private List<RuleMap> listRuleMap;
    private Map<Integer, RuleFunc> mapRuleFunct = new HashMap<>();
    private Map<Integer, Property> mapRuleProperty = new HashMap<>();
    private Map<String, Property> mapRulePropertyByName = new HashMap<>();
    private Map<Integer, RuleNode> ruleMapTree;

    private RuleCache() {
    }

    public static synchronized RuleCache getInstance() {
        if (ruleCache == null) {
            ruleCache = new RuleCache();
        }
        return ruleCache;
    }

    public boolean loadCache(Session sessionOra) {
        listRuleMap = new ArrayList<>();
        ruleMapTree = new HashMap<>();
        mapEvent = new HashMap<>();
        mapRuleNode = new HashMap<>();

        if (!loadRuleMap(sessionOra) || !loadEvents(sessionOra) || !loadProperties(sessionOra) || !loadRuleFunctions(sessionOra) ||
                !loadRuleExpressions(sessionOra) || !loadRuleActions(sessionOra)) {
            logger.error("Load RuleCache unsuccesful");
            return false;
        }

        listRuleMap.forEach((ruleMap) -> {
            RuleNode root = loadRuleMapDetail(sessionOra, ruleMap.getId());
            if (root != null) {
                ruleMapTree.put(ruleMap.getId(), root);
            }
        });

        return true;
    }

    public RuleNode loadRuleMapDetail(Session session, int profileMapId) {

        RuleNode ruleNode = loadRuleNodes(session, profileMapId);
        if (ruleNode == null) {
            return null;
        }

        updateRuleExpressions();

        return ruleNode;
    }

    public boolean loadRuleMap(Session session) {


        return session.doReturningWork(con -> {
            String sql = "SELECT id, name, description FROM rule_map";
            try (PreparedStatement preStatement = con.prepareStatement(sql)) {
                ResultSet result1 = preStatement.executeQuery();
                while (result1.next()) {
                    RuleMap ruleMap = new RuleMap();
                    ruleMap.setId(result1.getInt(1));
                    ruleMap.setName(result1.getString(2));
                    ruleMap.setDescription(result1.getString(3));
                    listRuleMap.add(ruleMap);
                }
            } catch (SQLException e) {
                logger.error("Exception when loadRuleMap " + e.getMessage(), e);
                return false;
            }
            logger.info("Load RULE_MAP successful [ROWS]=" + listRuleMap.size());
            return true;
        });
    }

    // update data after RuleExpressions is loaded
    public void updateRuleExpressions() {
        mapRuleNode.values().stream().filter((RuleNode pNode) -> pNode.getNodeExpLink() != null)
                .forEach((pNode) -> pNode.getNodeExpLink().update(this));
    }

    private boolean loadEvents(Session session) {

        return session.doReturningWork(con -> {
            if (mapEvent != null) {
                mapEvent.clear();
            } else {
                mapEvent = new HashMap<>();
            }

            try (PreparedStatement preStatement = con.prepareStatement("SELECT id,event_name FROM event ORDER BY id"
//                sql.append("SELECT id,event_name FROM event WHERE id IN ( ");
//                sql.append("SELECT parent_node FROM rule_tree WHERE type_tree=4 and rule_map_id=:1 ");
//                sql.append("UNION ALL ");
//                sql.append("SELECT child_node FROM rule_tree WHERE type_tree in (1,3) and rule_map_id=:1) ORDER BY id");
            )) {
                ResultSet result1 = preStatement.executeQuery();
                result1.setFetchSize(50);
                int i = 0;
                while (result1.next()) {
                    Event e = new Event();
                    e.setId(result1.getInt(1));
                    e.setName(result1.getString(2));
                    mapEvent.put(e.getId(), e);
                    i++;
                }
                logger.info("Load EVENTS successful [ROWS]=" + i);
            } catch (SQLException e) {
                logger.error("Exception when loadEvents:" + e.getMessage(), e);
                return false;
            }
            return true;
        });
    }

    private RuleNode loadRuleNodes(Session session, int nRuleMapID) {

        return session.doReturningWork(con -> {
            RuleNode aRootNode = new RuleNode();


            RuleNode pNode;

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT a.id,a.rule_node_name,a.exit_map,a.eff_date, a.exp_date, a.expression, a.event_id, a.is_root FROM rule_node a WHERE id in (");
            sql.append("SELECT child_node FROM rule_tree WHERE type_tree in (1,2) and rule_map_id = ? ");
            sql.append("UNION ALL ");
            sql.append("SELECT parent_node FROM rule_tree WHERE type_tree in (1,2) and rule_map_id= ?) ORDER BY a.id");

            try {
                PreparedStatement preStatement = con.prepareStatement(sql.toString());
                preStatement.setInt(1, nRuleMapID);
                preStatement.setInt(2, nRuleMapID);
                ResultSet result = preStatement.executeQuery();
                result.setFetchSize(50);
                int eventID;
                int nRoot = 0;
                while (result.next()) {
                    RuleNode rn = new RuleNode();
                    rn.setRuleMapId(nRuleMapID);
                    rn.setRuleNodeid(result.getInt(1));
                    rn.setRuleName(result.getString(2));
                    rn.setIsExitNode(result.getInt(3));
                    rn.setEffDate(result.getDate(4));
                    rn.setExpDate(result.getDate(5));

                    eventID = result.getInt(7);
                    if (eventID > 0) {
                        Event event = getRuleEvent(eventID);
                        if (event != null) {
                            rn.setEventId(eventID);
                            rn.setEvent(getRuleEvent(eventID));
                        }
                    }
                    boolean isRoot = result.getBoolean(8);
                    rn.setRoot(isRoot);
                    if (isRoot) {
                        nRoot++;
                        aRootNode = rn;
                    }
                    rn.setRuleExpress(result.getString(6));
                    rn.setActionList(mapRuleAction.get(rn.getRuleNodeID()));
                    mapRuleNode.put(rn.getRuleNodeid(), rn);


                }

                result.close();
                preStatement.close();

                //Load child node and event node
                sql.setLength(0);
                sql.append("SELECT child_node,type_tree FROM rule_tree WHERE rule_map_id=? and parent_node=? and type_tree<3"); // Chi lay cac type_tree in (1,2)
                preStatement = con.prepareStatement(sql.toString());
                preStatement.setInt(1, nRuleMapID);
                int nYes;
                int nNo;
                int nChildNode;
                int nTypeTree;


                // Loop parent node
                for (Entry<Integer, RuleNode> entry : mapRuleNode.entrySet()) {
                    pNode = entry.getValue();

                    preStatement.setInt(2, pNode.getRuleNodeid());
                    result = preStatement.executeQuery();
                    nYes = 0;
                    nNo = 0;
                    while (result.next()) {
                        nChildNode = result.getInt(1);
                        nTypeTree = result.getInt(2);
                        RuleNode pChild;
                        // Find child node
                        switch (nTypeTree) {
                            case RuleNode.TYPE_YES_TREE:
//                                    Event event = getRuleEvent(nChildNode);
//                                    if (event != null) {
//                                        pNode.setEventId(nChildNode);
//                                        pNode.setTypeTree(nTypeTree);
//                                        pNode.setEvent(event);
//                                    } else {
//                                        logger.error(String.format("Event not found:  NodeID=%d,child_node=%d,Type_Tree=%d",
//                                                pNode.getRuleNodeid(), nChildNode, nTypeTree));
//                                    }
                                if (nChildNode > 0) {
                                    pChild = getRuleNode(nChildNode);
                                    if (pChild != null) {
                                        pNode.setChildNodeID(nChildNode);
                                        pNode.setNextYesRuleNode(pChild);
                                        pChild.setParentNodeId(pNode.getRuleNodeID());
                                    } else {
                                        logger.error(String.format("Child Node not found:  NodeID=%d,child_node=%d,Type_Tree=%d",
                                                pNode.getRuleNodeid(), nChildNode, nTypeTree));
                                    }
                                    nYes++;
                                }
                                break;
                            case RuleNode.TYPE_NO_TREE:
                                // Child node
                                pChild = getRuleNode(nChildNode);
                                if (pChild != null) {
                                    pNode.setChildNodeID(nChildNode);
                                    pNode.setNextNoRuleNode(pChild);
                                    pChild.setParentNodeId(pNode.getRuleNodeID());
                                } else {
                                    logger.error(String.format("Child Node not found:  NodeID=%d,child_node=%d,Type_Tree=%d",
                                            pNode.getRuleNodeid(), nChildNode, nTypeTree));
                                }
                                nNo++;
                                break;
//                                case RuleNode.TYPE_TREE_ROOT:
//                                    nRoot++;
//                                    pNode.setRoot(true);
//                                    aRootNode = pNode;
//
//                                    // Child node la event
//                                    event = getRuleEvent(nChildNode);
//                                    if (event != null) {
//                                        pNode.setEventId(nChildNode);
//                                        pNode.setEvent(event);
//                                    } else {
//                                        logger.error(String.format("Event not found:  NodeID=%d,child_node=%d,Type_Tree=%d",
//                                                pNode.getRuleNodeid(), nChildNode, nTypeTree));
//                                    }
//                                    nYes++;
//                                    break;
                            default:
                                logger.error(String.format("Invalid value in rule tree table: NodeID=%d,child_node=%d,Type_Tree=%d",
                                        pNode.getRuleNodeid(), nChildNode, nTypeTree));
                        }
                    }

                    if (nYes > 1) {
                        logger.error(String.format("Too many Event for node: %d", pNode.getRuleNodeid()));
                    }

                    if (nNo > 1) {
                        logger.error(String.format("Too many Child Node for node: %d", pNode.getRuleNodeid()));
                    }

                    result.close();
                }

                preStatement.close();

                if (nRoot > 1) {
                    logger.error("Too many rootNode");
                    return null;
                } else if (nRoot == 0) {
                    logger.info("rootNode notFound, ruleMap " + nRuleMapID);
                    return null;
                }

//                    //set child cho event
//                    sql.setLength(0);
//                    sql.append("SELECT child_node FROM rule_tree WHERE rule_map_id=? and parent_node=? and type_tree=4");
//                    preStatement = con.prepareStatement(sql.toString());
//                    preStatement.setInt(1, nRuleMapID);
//
//                    for (Entry<Integer, Event> entry : mapEvent.entrySet()) {
//                        Event event = entry.getValue();
//                        preStatement.setInt(2, event.getId());
//                        result = preStatement.executeQuery();
//                        nYes = 0;
//                        while (result.next()) {
//                            nChildNode = result.getInt(1);
//                            pNode = getRuleNode(nChildNode);
//                            if (pNode != null) {
//                                event.setRootNode(pNode);
//                            } else {
//                                logger.error(String.format("RuleNode not found: Event(%s [%d])->NodeID=%d",
//                                        event.getName(), event.getId(), nChildNode));
//                            }
//
//                            nYes++;
//                        }
//
//                        result.close();
//                    }
//                    preStatement.close();

            } catch (SQLException e) {
                logger.error("Exception when loadRuleNodes:" + e.getMessage(), e);
                return null;
            }

            logger.info("Load tree for rulemap[" + nRuleMapID + "] successful. Root node: " + aRootNode.getRuleNodeID());
            return aRootNode;

        });
    }

    private boolean loadRuleExpressions(Session session) {
        return session.doReturningWork(con -> {
            PreparedStatement preStatement;
            ResultSet result1;
            int i = 0;

            if (mapRuleExp != null) {
                mapRuleExp.clear();
            } else {
                mapRuleExp = new HashMap<>();
            }

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id,property_left_id,function_left_id,param_first_left,param_second_left,type_operand,property_right_id,function_right_id,param_first_right,param_second_right,operator_expression, rule_node_id FROM rule_expression ORDER BY id");

            try {
                preStatement = con.prepareStatement(sql.toString());
                result1 = preStatement.executeQuery();
                result1.setFetchSize(50);

                while (result1.next()) {
                    RuleExp pExp = new RuleExp();
                    pExp.setId(result1.getInt(1));
                    pExp.setPropertyLeftId(result1.getInt(2));
                    pExp.setFunctionLeftId(result1.getInt(3));
                    pExp.setParamFirstLeft(result1.getString(4));
                    pExp.setParamSecondLeft(result1.getString(5));
                    pExp.setTypeOperand(result1.getInt(6));
                    pExp.setPropertyRightId(result1.getInt(7));
                    pExp.setFunctionRightId(result1.getInt(8));
                    pExp.setParamFirstRight(result1.getString(9));
                    pExp.setParamSecondRight(result1.getString(10));
                    pExp.setOperation(result1.getInt(11));
                    pExp.setRuleNodeId(result1.getInt(12));
                    mapRuleExp.put(pExp.getId(), pExp);
                    pExp.setFunctionLeft(getRuleFunction(pExp.getFunctionLeftId()));
                    pExp.setFunctionRight(getRuleFunction(pExp.getFunctionRightId()));

                    i++;
                }

                logger.info("Load RULE_EXP[ROWS]=" + i + " successful");

                result1.close();
                preStatement.close();
            } catch (SQLException e) {
                logger.error("Exception when loadRuleExpressions:" + e.getMessage(), e);
                return false;
            }
            return true;
        });
    }

    boolean loadRuleActions(Session session) {

        if (mapRuleAction == null) {
            mapRuleAction = new HashMap<>();
        } else {
            mapRuleAction.clear();
        }


        return session.doReturningWork(con -> {
            try {
                PreparedStatement preStatement;
                ResultSet result1;
                int tempId;

                preStatement = con.prepareStatement("SELECT  id,rule_node_id, src_prop_id, func_id,param_1,param_2,dest_prop_id FROM rule_action ORDER BY id");
                result1 = preStatement.executeQuery();
                result1.setFetchSize(50);

                int i = 0;
                int ruleNodeId;

                while (result1.next()) {
                    RuleNodeAction action = new RuleNodeAction();
                    action.setActionId(result1.getInt(1));
                    ruleNodeId = result1.getInt(2);
                    action.setRuleNodeId(ruleNodeId);
                    action.setSrcPropId(result1.getInt(3));
                    action.setFuncId(result1.getInt(4));
                    action.setParam1(result1.getString(5));
                    action.setParam2(result1.getString(6));
                    action.setDestPropId(result1.getInt(7));

                    tempId = action.getFuncId();
                    if (tempId > 0) {
                        if (getRuleFunction(tempId) != null) {
                            action.setFunction(getRuleFunction(tempId));
                        } else {
                            logger.error(String.format("Invalid data, Function ID=%d not found", tempId));
                        }
                    }

                    i++;
                    if (mapRuleAction.get(ruleNodeId) == null) {
                        List<RuleNodeAction> newList = new ArrayList<>();
                        newList.add(action);
                        mapRuleAction.put(ruleNodeId, newList);
                    } else {
                        mapRuleAction.get(ruleNodeId).add(action);
                    }
                }

                logger.info("Load RULE_ACTION[ROWS]=" + i + " successful");

                result1.close();
                preStatement.close();

            } catch (SQLException e) {
                logger.error("Exception when loadRuleActions:" + e.getMessage(), e);
                return false;
            }
            return true;
        });
    }

    private boolean loadProperties(Session session) {
        mapRuleProperty.clear();

        return session.doReturningWork(con -> {
            // do something useful
            String sql = "SELECT id, name, value FROM property ORDER BY id";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                ResultSet result1 = stmt.executeQuery();
                int i = 0;
                while (result1.next()) {
                    Property rf = new Property(
                            result1.getInt(1),
                            result1.getString(2),
                            result1.getString(3));
                    mapRuleProperty.put(rf.getId(), rf);
                    mapRulePropertyByName.put(rf.getName(), rf);

                    i++;
                }

                logger.info("Load RULE_Property[ROWS]=" + i + " successful");
            } catch (SQLException e) {
                logger.error("Exception when loadProperties:" + e.getMessage(), e);
                return false;
            }

            return true;
        });
    }

    private boolean loadRuleFunctions(Session session) {
        mapRuleFunct.clear();


        return session.doReturningWork(con -> {
            // do something useful
            String sql = "SELECT id,rule_func_name, description FROM rule_function ORDER BY id";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                ResultSet result1 = stmt.executeQuery();
                int i = 0;
                while (result1.next()) {
                    RuleFunc rf = new RuleFunc(
                            result1.getInt(1),
                            result1.getString(2),
                            result1.getString(3));
                    mapRuleFunct.put(rf.getId(), rf);
                    i++;
                }

                logger.info("Load RULE_FUNC[ROWS]=" + i + " successful");
            } catch (SQLException e) {
                logger.error("Exception when loadRuleFunctions:" + e.getMessage(), e);
                return false;
            }

            return true;
        });
    }

    public Map<Integer, RuleFunc> getMapRuleFunct() {
        return mapRuleFunct;
    }

    public void setpRuleFunctions(HashMap<Integer, RuleFunc> pRuleFunctions) {
        this.mapRuleFunct = pRuleFunctions;
    }


    public Map<Integer, RuleExp> getMapRuleExp() {
        return mapRuleExp;
    }

    public void setpRuleExpression(HashMap<Integer, RuleExp> pRuleExpression) {
        this.mapRuleExp = pRuleExpression;
    }

    public Map<Integer, RuleNode> getMapRuleNode() {
        return mapRuleNode;
    }

    public void setpRuleNode(HashMap<Integer, RuleNode> pRuleNode) {
        this.mapRuleNode = pRuleNode;
    }

    public Map<Integer, Event> getMapEvent() {
        return mapEvent;
    }

    public void setpRuleEvent(HashMap<Integer, Event> pRuleEvent) {
        this.mapEvent = pRuleEvent;
    }

    public RuleFunc getRuleFunction(int id) {
        return mapRuleFunct.get(id);
    }

    public Event getRuleEvent(int id) {
        return mapEvent.get(id);
    }

    public RuleNode getRuleNode(int id) {
        return mapRuleNode.get(id);
    }

    public RuleExp getRuleExpression(int id) {
        return mapRuleExp.get(id);
    }

    public List<RuleMap> getListRuleMap() {
        return listRuleMap;
    }

    public void setListRuleMap(List<RuleMap> listRuleMap) {
        this.listRuleMap = listRuleMap;
    }

    public Map<Integer, RuleNode> getRuleMapTree() {
        return ruleMapTree;
    }

    public void setRuleMapTree(Map<Integer, RuleNode> aprofileRuleMap) {
        ruleMapTree = aprofileRuleMap;
    }

    public Map<Integer, Property> getMapRuleProperty() {
        return mapRuleProperty;
    }

    public void setMapRuleProperty(Map<Integer, Property> mapRuleProperty) {
        this.mapRuleProperty = mapRuleProperty;
    }

    public void setMapRuleExp(Map<Integer, RuleExp> mapRuleExp) {
        this.mapRuleExp = mapRuleExp;
    }

    public void setMapRuleNode(Map<Integer, RuleNode> mapRuleNode) {
        this.mapRuleNode = mapRuleNode;
    }

    public void setMapEvent(Map<Integer, Event> mapEvent) {
        this.mapEvent = mapEvent;
    }

    public Map<Integer, List<RuleNodeAction>> getMapRuleAction() {
        return mapRuleAction;
    }

    public void setMapRuleAction(Map<Integer, List<RuleNodeAction>> mapRuleAction) {
        this.mapRuleAction = mapRuleAction;
    }

    public void setMapRuleFunct(Map<Integer, RuleFunc> mapRuleFunct) {
        this.mapRuleFunct = mapRuleFunct;
    }

    public Map<String, Property> getMapRulePropertyByName() {
        return mapRulePropertyByName;
    }

    public void setMapRulePropertyByName(Map<String, Property> mapRulePropertyByName) {
        this.mapRulePropertyByName = mapRulePropertyByName;
    }
}
