package com.ssi.decisionrule;

import com.ssi.decisionrule.process.RuleProcess;
import com.ssi.decisionrule.utils.HibernateSessionFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiServer {
    private static final Logger logger = Logger.getLogger(ApiServer.class);

    public static void main(String[] args) {
        SpringApplication.run(ApiServer.class, args);
        PropertyConfigurator.configure("config/log4j.properties");
        HibernateSessionFactory.setConfigFile("config/hibernate.cfg.xml");
        Session sessionDB = null;
        try {
            sessionDB = HibernateSessionFactory.getSession();


            if (!RuleProcess.getInstance().loadCache(sessionDB)) {
                logger.error("Load rule cache fail");
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (sessionDB != null && sessionDB.isOpen()) {
                sessionDB.close();
            }

        }
    }
}
