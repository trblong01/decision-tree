package com.ssi.decisionrule.dao;

import com.ssi.decisionrule.entity.RuleNodeAction;
import com.ssi.decisionrule.process.RuleCache;
import com.ssi.decisionrule.utils.HibernateSessionFactory;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class RuleActionDAO {
    private static final Logger logger = Logger.getLogger(RuleActionDAO.class);

    private static final String DELETE_ACTION = "delete from rule_action where id=?";
    private static final String CREATE_ACTION = "insert into  rule_action(rule_node_id, src_prop_id, func_id, param_1, param_2, dest_prop_id) values (?, ?, ?, ?, ?,?)";
    private static final String UPDATE_ACTION = "update  rule_action set src_prop_id=? , func_id=? , param_1=?, param_2=?, dest_prop_id=? where id=?";

    public Integer create(RuleNodeAction body) {
        Session session = null;
        int result = -1;
        try {
            session = HibernateSessionFactory.getSession();

            result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(CREATE_ACTION, Statement.RETURN_GENERATED_KEYS)) {
                    preStatement.setInt(1, body.getRuleNodeId());
                    preStatement.setInt(2, body.getSrcPropId());
                    preStatement.setInt(3, body.getFuncId());
                    preStatement.setString(4, body.getParam1());
                    preStatement.setString(5, body.getParam2());
                    preStatement.setInt(6, body.getDestPropId());
                    preStatement.execute();
                    ResultSet rs = preStatement.getGeneratedKeys();
                    int generatedKey = 0;
                    if (rs.next()) {
                        generatedKey = rs.getInt(1);
                        body.setActionId(generatedKey);
                        List<RuleNodeAction> ruleNodeActionList = RuleCache.getInstance().getMapRuleAction().computeIfAbsent(body.getRuleNodeId(), k -> new ArrayList<>());
                        ruleNodeActionList.add(body);
                        logger.info("Create rule action successful " + generatedKey);
                    }

                    return generatedKey;
                } catch (SQLException e) {
                    logger.error("Exception when create rule action " + body, e);
                    return -1;
                }
            });

            if (result > 0) {
                updateAction(body);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }

        }
        return result;
    }

    public int update(RuleNodeAction body) {
        Session session = null;
        try {
            session = HibernateSessionFactory.getSession();

            int result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(UPDATE_ACTION)) {
                    preStatement.setInt(1, body.getSrcPropId());
                    preStatement.setInt(2, body.getFuncId());
                    preStatement.setString(3, body.getParam1());
                    preStatement.setString(4, body.getParam2());
                    preStatement.setInt(5, body.getDestPropId());
                    preStatement.setInt(6, body.getActionId());
                    logger.info("Update rule action successful " + body);

                    return preStatement.executeUpdate();

                } catch (SQLException e) {
                    logger.error("Exception when create rule action " + body, e);
                    return -1;
                }
            });

            if (result > 0) {
                updateAction(body);
                return body.getActionId();
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

    public int delete(int id, int ruleNodeId) {

        Session session = null;
        try {
            session = HibernateSessionFactory.getSession();

            Integer result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(DELETE_ACTION)) {
                    preStatement.setInt(1, id);
                    logger.info("Delete rule action successful " + id);

                    preStatement.execute();
                } catch (SQLException e) {
                    logger.error("Exception when delete rule action " + id, e);
                    return -1;
                }
                return 1;
            });

            if (result > 0) {
                Map<Integer, List<RuleNodeAction>> mapRuleAction = RuleCache.getInstance().getMapRuleAction();
                List<RuleNodeAction> listRuleAction = mapRuleAction.get(ruleNodeId);
                if (listRuleAction != null) {
                    listRuleAction.removeIf(e -> e.getActionId() == id);
                    mapRuleAction.put(ruleNodeId, listRuleAction);
                }
                RuleCache.getInstance().getMapRuleNode().get(ruleNodeId).setActionList(listRuleAction);
                return result;
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

    private void updateAction(RuleNodeAction body) {
        body.setFunction(RuleCache.getInstance().getRuleFunction(body.getFuncId()));
        Map<Integer, List<RuleNodeAction>> mapRuleAction = RuleCache.getInstance().getMapRuleAction();
        List<RuleNodeAction> listRuleAction = mapRuleAction.computeIfAbsent(body.getRuleNodeId(), k -> new ArrayList<>());
        listRuleAction.removeIf(e->e.getActionId() == body.getActionId());
        listRuleAction.add(body);
        RuleCache.getInstance().getMapRuleNode().get(body.getRuleNodeId()).setActionList(listRuleAction);
    }
}
