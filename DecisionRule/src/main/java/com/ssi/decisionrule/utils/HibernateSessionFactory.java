package com.ssi.decisionrule.utils;

import com.ssi.decisionrule.entity.RuleNode;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

import java.io.File;
//import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class HibernateSessionFactory {
    private final static Logger logger = Logger.getLogger(RuleNode.class);
    private static final Configuration configuration = new Configuration();
    private static org.hibernate.SessionFactory sessionFactory;
    private static String configFile = "config/hibernate.cfg.xml";

    private HibernateSessionFactory() {
    }

    public static Session getSession() throws HibernateException {
        Session session;
        if (sessionFactory == null) {
            rebuildSessionFactory();
        }

        session = (sessionFactory != null) ? sessionFactory.openSession() : null;
        return session;
    }

    public static void rebuildSessionFactory() {
        try {
            configuration.configure(new File(configFile));

//            StandardPBEStringEncryptor encryptor =new StandardPBEStringEncryptor();
//            encryptor.setPassword("poiuytrewqlkjhgfdsamnbvcxz");
//            encryptor.setAlgorithm("PBEWITHMD5ANDDES");
//            String pass = encryptor.decrypt(configuration.getProperty("hibernate.connection.password"));
            configuration.setProperty("hibernate.connection.password", configuration.getProperty("hibernate.connection.password"));
            sessionFactory = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            logger.error("%%%% Error Creating SessionFactory ", e);
        }
    }

    public static org.hibernate.SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void setConfigFile(String configFile) {
        HibernateSessionFactory.configFile = configFile;
        sessionFactory = null;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }
}
