package com.ssi.decisionrule.dao;

import com.ssi.decisionrule.entity.RuleMap;
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
public class RuleMapDAO {
    private static final Logger logger = Logger.getLogger(RuleMapDAO.class);

    private static final String CREATE_ACTION = "insert into  rule_map(name) values (?)";

    public Integer create(RuleMap body) {
        Session session = null;
        int result;
        try {
            session = HibernateSessionFactory.getSession();

            result = session.doReturningWork(con -> {

                try (PreparedStatement preStatement = con.prepareStatement(CREATE_ACTION, Statement.RETURN_GENERATED_KEYS)) {
                    preStatement.setString(1, body.getName());

                    preStatement.execute();
                    ResultSet rs = preStatement.getGeneratedKeys();
                    int generatedKey = 0;
                    if (rs.next()) {
                        generatedKey = rs.getInt(1);
                        body.setId(generatedKey);
                        RuleCache.getInstance().getRuleMapTree().put(generatedKey, null);
                        RuleCache.getInstance().getListRuleMap().add(body);
                        logger.info("Create rule map successful " + generatedKey);
                    }

                    return generatedKey;
                } catch (SQLException e) {
                    logger.error("Exception when create rule action " + body, e);
                    return -1;
                }
            });
            if (result > 0) {
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

}
