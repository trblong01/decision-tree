package com.ssi.decisionrule.dao;

import com.ssi.decisionrule.entity.RuleExp;
import com.ssi.decisionrule.entity.RuleExpLink;
import com.ssi.decisionrule.process.RuleCache;
import com.ssi.decisionrule.utils.HibernateSessionFactory;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class RuleExpressionDAO {
    private static final Logger logger = Logger.getLogger(RuleExpressionDAO.class);

    private static final String DELETE = "delete from rule_expression where id=?";
    private static final String CREATE = "insert into  rule_expression(property_left_id, function_left_id, param_first_left, param_second_left, type_operand, property_right_id, function_right_id, param_first_right, param_second_right, operator_expression, rule_node_id) " +
            " values (?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "update rule_expression set property_left_id=? , function_left_id=? , param_first_left=?, param_second_left=?, type_operand=?, " +
            " property_right_id=? , function_right_id=? , param_first_right=?, param_second_right=?, operator_expression=?" +
            " where id=?";

    public Integer create(RuleExp body) {
        Session session = null;
        int result = -1;
        try {
            session = HibernateSessionFactory.getSession();

            result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS)) {
                    preStatement.setInt(1, body.getPropertyLeftId());
                    preStatement.setInt(2, body.getFunctionLeftId());
                    preStatement.setString(3, body.getParamFirstLeft());
                    preStatement.setString(4, body.getParamFirstRight());
                    preStatement.setInt(5, body.getTypeOperand());
                    preStatement.setInt(6, body.getPropertyRightId());
                    preStatement.setInt(7, body.getFunctionRightId());
                    preStatement.setString(8, body.getParamFirstRight());
                    preStatement.setString(9, body.getParamSecondRight());
                    preStatement.setInt(10, body.getOperation());
                    preStatement.setInt(11, body.getRuleNodeId());
                    preStatement.execute();
                    ResultSet rs = preStatement.getGeneratedKeys();
                    int generatedKey = 0;
                    if (rs.next()) {
                        generatedKey = rs.getInt(1);
                        body.setId(generatedKey);
                        RuleCache.getInstance().getMapRuleExp().put(generatedKey, body);
                        logger.info("Create rule expression successful " + generatedKey);
                    }

                    return generatedKey;
                } catch (SQLException e) {
                    logger.error("Exception when create rule expression " + body, e);
                    return -1;
                }
            });

            if (result > 0) {
                RuleCache.getInstance().getMapRuleExp().put(result, body);
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

    public int update(RuleExp body) {
        Session session = null;
        try {
            session = HibernateSessionFactory.getSession();

            int result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(UPDATE)) {
                    preStatement.setInt(1, body.getPropertyLeftId());
                    preStatement.setInt(2, body.getFunctionLeftId());
                    preStatement.setString(3, body.getParamFirstLeft());
                    preStatement.setString(4, body.getParamFirstRight());
                    preStatement.setInt(5, body.getTypeOperand());
                    preStatement.setInt(6, body.getPropertyRightId());
                    preStatement.setInt(7, body.getFunctionRightId());
                    preStatement.setString(8, body.getParamFirstRight());
                    preStatement.setString(9, body.getParamSecondRight());
                    preStatement.setInt(10, body.getOperation());
                    preStatement.setInt(11, body.getId());
                    logger.info("Update rule expression successful " + body);

                    return preStatement.executeUpdate();

                } catch (SQLException e) {
                    logger.error("Exception when create rule expression " + body, e);
                    return -1;
                }
            });

            if (result > 0) {
                RuleCache.getInstance().getMapRuleExp().put(body.getId(), body);
                RuleExpLink expLink = RuleCache.getInstance().getRuleNode(body.getRuleNodeId()).getNodeExpLink();
                if (expLink != null) {
                    expLink.update(RuleCache.getInstance());
                }
                return body.getId();
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

    public int delete(int id) {

        Session session = null;
        try {
            session = HibernateSessionFactory.getSession();

            int result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(DELETE)) {
                    preStatement.setInt(1, id);
                    logger.info("Delete rule expression successful " + id);

                    preStatement.execute();
                } catch (SQLException e) {
                    logger.error("Exception when delete expression action " + id, e);
                    return -1;
                }
                return 1;
            });

            if (result > 0) {
                RuleCache.getInstance().getMapRuleExp().remove(id);
            }
            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }

        }
        return -1;
    }
}
