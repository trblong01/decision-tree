package com.ssi.decisionrule.dao;

import com.ssi.decisionrule.entity.RuleNode;
import com.ssi.decisionrule.process.RuleCache;
import com.ssi.decisionrule.process.RuleConstant;
import com.ssi.decisionrule.utils.DateTimeUtils;
import com.ssi.decisionrule.utils.HibernateSessionFactory;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RuleNodeDAO {
    private static final Logger logger = Logger.getLogger(RuleNodeDAO.class);

    private static final String DELETE_NODE = "delete from rule_node where id in (";
    private static final String DELETE_TREE = "delete from rule_tree where parent_node in (";
    private static final String DELETE_TREE_CHILD_CONDITION = ") or  child_node in (";
    private static final String CREATE = "insert into  rule_node(rule_node_name, eff_date, is_root) values (?, ?, ?)";
    private static final String CREATE_TREE = "insert into  rule_tree(type_tree, parent_node, child_node, rule_map_id) values (?, ?, ?, ?)";
    private static final String UPDATE_TREE = "update rule_tree set type_tree=?, parent_node=?, child_node=?, rule_map_id=? where parent_node=? and type_tree=?";
    private static final String UPDATE = "update rule_node set rule_node_name=? , eff_date=? , exp_date=?, exit_map=?, expression=?, event_id=? where id=?";

    private static final String DELETE_RULE_EXPRESSION = "delete from rule_expression where rule_node_id in (";
    private static final String DELETE_RULE_ACTION = "delete from rule_action where rule_node_id in (";


    public Integer create(JSONObject body) {
        Session session = null;
        int newNodeId = -2;
        int ruleMapId = body.optInt("ruleMapId", -1);
        int parentNodeId = body.optInt("parentId", -1);
        if (ruleMapId < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Rulemap is emtpy");

        }
        try {
            logger.info("Node create: " + body);


            RuleNode ruleNode;
            session = HibernateSessionFactory.getSession();
            boolean isRoot = parentNodeId < 0;
//            if(parentNodeId < 0 || body.optBoolean("isNo", false)){
            String ruleName = body.optString("name", null);
            Date date = new Date(System.currentTimeMillis());
            newNodeId = session.doReturningWork(con -> {
                try (PreparedStatement preStatement = con.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS)) {
                    preStatement.setString(1, ruleName);
                    preStatement.setDate(2, date);
                    preStatement.setBoolean(3, isRoot);
                    preStatement.execute();
                    ResultSet rs = preStatement.getGeneratedKeys();


                    if (rs.next()) {
                        return rs.getInt(1);
                    }

                    return -1;
                } catch (SQLException e) {
                    logger.error("Exception when create rule node " + body, e);
                    return -1;
                }
            });

            if (newNodeId < 0) {
                return newNodeId;
            }
            ruleNode = new RuleNode();
            ruleNode.setRuleNodeID(newNodeId);
            ruleNode.setRuleName(ruleName);
            ruleNode.setEffDate(date);
            ruleNode.setRoot(isRoot);
            ruleNode.setRuleMapId(ruleMapId);
            RuleCache.getInstance().getMapRuleNode().put(newNodeId, ruleNode);
            if (parentNodeId < 0) {
                RuleCache.getInstance().getRuleMapTree().put(ruleMapId, ruleNode);
            }
//            }


//            String sql = null;
            int childId = 0;
            if (parentNodeId > 0) {
                RuleNode parentNode = RuleCache.getInstance().getMapRuleNode().get(parentNodeId);
                if (parentNode == null) {
                    logger.error("Not found parentNode " + parentNodeId);
                    return -1;
                } else {

                    ruleMapId = parentNode.getRuleMapId();
                    childId = newNodeId;
                    String sql = CREATE_TREE;
                    boolean isUpdate = false;
                    int typeTree = RuleNode.TYPE_NO_TREE;
                    if (body.optString("branch", RuleConstant.BRANCH_NO).equals(RuleConstant.BRANCH_NO)) {
                        parentNode.setNextNoRuleNode(ruleNode);
                    } else {
                        if (parentNode.isRoot()) {
                            sql = UPDATE_TREE;
                            isUpdate = true;
                        }
                        typeTree = RuleNode.TYPE_YES_TREE;
                        parentNode.setNextYesRuleNode(ruleNode);
                    }

                    updateTree(typeTree, parentNodeId, childId, ruleMapId, isUpdate, sql, session);
                }
            } else {
                //root Node
                parentNodeId = newNodeId;
                updateTree(RuleNode.TYPE_YES_TREE, parentNodeId, childId, ruleMapId, false, CREATE_TREE, session);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }

        }
        return newNodeId;
    }

    private void updateTree(int treeType, int parentNode, int child, int ruleMap, boolean isUpdate, String sql, Session session) {
        session.doWork(con -> {
            try (PreparedStatement preStatement = con.prepareStatement(sql)) {
                preStatement.setInt(1, treeType);
                preStatement.setInt(2, parentNode);
                preStatement.setInt(3, child);
                preStatement.setInt(4, ruleMap);

                if (isUpdate) {
                    preStatement.setInt(5, parentNode);
                    preStatement.setInt(6, treeType);
                }

                preStatement.execute();

            } catch (SQLException e) {
                logger.error("Exception when update tree ", e);
            }
        });
    }

    public int update(JSONObject body) {
        Session session = null;
        try {
            session = HibernateSessionFactory.getSession();
            int id = body.optInt("id", 0);
            String name = body.optString("name", null);
            Date effDate = DateTimeUtils.convertToSQLDate(body.optString("effDate", null));
            Date expDate = DateTimeUtils.convertToSQLDate(body.optString("expDate", null));
            int exitHere = body.optInt("exitMap");
            String expression = body.optString("expressionLink", "");
            int eventId = body.optInt("eventId", 0);

            int result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(UPDATE)) {
                    preStatement.setString(1, name);
                    preStatement.setDate(2, effDate);
                    preStatement.setDate(3, expDate);
                    preStatement.setInt(4, exitHere);
                    preStatement.setString(5, expression);
                    preStatement.setInt(6, eventId);
                    preStatement.setInt(7, id);
                    logger.info("Update rule node successful " + body);

                    return preStatement.executeUpdate();

                } catch (SQLException e) {
                    logger.error("Exception when update node action " + body, e);
                    return -1;
                }
            });
            if (result >= 0) {
                RuleNode ruleNode = RuleCache.getInstance().getMapRuleNode().get(id);
                ruleNode.setRuleName(name);
                ruleNode.setEffDate(effDate);
                ruleNode.setExpDate(expDate);
                ruleNode.setIsExitNode(exitHere);
                ruleNode.setRuleExpress(expression);
                ruleNode.setEventId(eventId);
                ruleNode.setEvent(RuleCache.getInstance().getRuleEvent(eventId));
                RuleCache.getInstance().getMapRuleNode().put(id, ruleNode);
                return id;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }

        }

        return -1;
    }


    //delete node, child nodes, actions, expressions
    public int delete(int id) {

        Session session = null;
        List<Integer> listId = new ArrayList<>();
        getNodeAndChildsId(id, listId);
        if (listId.size() > 0) {
            String listIdsStr = listId.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            try {

                String sqlDeleteNode = DELETE_NODE + listIdsStr + ")";
                String sqlDeleteTree = DELETE_TREE + listIdsStr + DELETE_TREE_CHILD_CONDITION + listIdsStr + ")";
                String sqlDeleteRuleExpr = DELETE_RULE_EXPRESSION + listIdsStr + ")";
                String sqlDeleteRuleAction = DELETE_RULE_ACTION + listIdsStr + ")";
                session = HibernateSessionFactory.getSession();
                boolean result = session.doReturningWork(con -> {

                    Statement statement = con.createStatement();
                    statement.addBatch(sqlDeleteNode);
                    statement.addBatch(sqlDeleteTree);
                    statement.addBatch(sqlDeleteRuleExpr);
                    statement.addBatch(sqlDeleteRuleAction);
                    statement.executeBatch();

                    statement.close();
                    return true;
                });
                if (result) {
//                    RuleNode ruleNode = RuleCache.getInstance().getMapRuleNode().get(id);
//                    ruleNode.setNextNoRuleNode(null);
                    deleteCacheNodes(listId);

                    RuleCache.getInstance().getMapRuleAction().remove(id);
                    RuleCache.getInstance().getMapRuleExp().entrySet().removeIf(e -> e != null
                            && e.getValue().getRuleNodeId() == id);
                    return id;
                }

            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }

            }

        }
        return -1;
    }

    private void getNodeAndChildsId(int id, List<Integer> listId) {
        RuleNode ruleNode = RuleCache.getInstance().getMapRuleNode().get(id);

        if (ruleNode != null) {

            listId.add(id);

            RuleNode noNode = ruleNode.getNextNoRuleNode();
            if (noNode != null) {
//                listId.add(noNode.getRuleNodeID());
                getNodeAndChildsId(noNode.getRuleNodeID(), listId);
            }

            RuleNode yesNode = ruleNode.getNextYesRuleNode();
            if (yesNode != null) {
//                listId.add(yesNode.getRuleNodeID());
                getNodeAndChildsId(yesNode.getRuleNodeID(), listId);
            }

        }
    }

    private void deleteCacheNodes(List<Integer> ids) {
        if (ids != null) {
            for (Integer id : ids) {
                RuleNode ruleNode = RuleCache.getInstance().getMapRuleNode().remove(id);
                ruleNode.setNextNoRuleNode(null);
                ruleNode.setNextYesRuleNode(null);
                ruleNode.setActionList(null);
                ruleNode.setRuleExpress(null);
                ruleNode.setEvent(null);
                ruleNode.setEventId(-1);
                if (ruleNode.isRoot()) {
                    RuleCache.getInstance().getRuleMapTree().put(ruleNode.getRuleMapId(), null);
                }

                int parentId = ruleNode.getParentNodeId();
                RuleNode parentNode = RuleCache.getInstance().getMapRuleNode().get(parentId);
                if (parentNode != null) {
                    if (parentNode.getNextNoRuleNode() != null && parentNode.getNextNoRuleNode().getRuleNodeID() == id) {
                        parentNode.setNextNoRuleNode(null);
                    }

                    if (parentNode.getNextYesRuleNode() != null && parentNode.getNextYesRuleNode().getRuleNodeID() == id) {
                        parentNode.setNextYesRuleNode(null);
                    }

                }
            }
        }
    }
}
