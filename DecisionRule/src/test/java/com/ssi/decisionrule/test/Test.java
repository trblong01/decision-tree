/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.test;

import com.ssi.decisionrule.inout.RuleInput;
import com.ssi.decisionrule.process.RuleConstant;
import com.ssi.decisionrule.process.RuleConstant.Property;
import com.ssi.decisionrule.process.RuleProcess;
import com.ssi.decisionrule.utils.HibernateSessionFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;

/**
 *
 * @author longtb
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    private static final Logger logger = Logger.getLogger(Test.class);
    private static HashMap<Integer, String> mapProps;   
    private static final String fileProperty = "config/RuleProp.properties";

    public static void main(String[] args) {
        PropertyConfigurator.configure("config/log4j.properties");
        HibernateSessionFactory.setConfigFile("config/hibernate.cfg.xml");
        Session sessionOr = null;
        try {
            sessionOr = HibernateSessionFactory.getSession();

            
            if(!RuleProcess.getInstance().loadCache(sessionOr)){
                return;
            }            
            //create input            
            RuleInput ruleInput = new RuleInput();
            ruleInput.setRuleMapId(1);
            ruleInput.setMapProp(buildPropertyFromFile(fileProperty));


            RuleProcess.getInstance().process(ruleInput);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (sessionOr != null && sessionOr.isOpen()) {
                sessionOr.close();
            }

        }
    }

    public static HashMap buildPropertys() {
        mapProps = new HashMap<>();
        mapProps.put(RuleConstant.Property.TIME_DURATION, "12");
        mapProps.put(RuleConstant.Property.USER_ID, "5");
        mapProps.put(RuleConstant.Property.AREA, "HaNoi");
        mapProps.put(RuleConstant.Property.VOLUMN, "48");
        mapProps.put(RuleConstant.Property.NUMBER_OF_CUSTOMER, "10");
        mapProps.put(RuleConstant.Property.POSITION, "300");
        
        return mapProps;
    }

    public static HashMap buildPropertyFromFile(String path) {
        mapProps = new HashMap<>();
        Properties props = getPropertysFile(path);
        Property property = new Property();
        Field[] fields = Property.class.getFields();
        for (Field f : fields) {
            try {
                mapProps.put((Integer) (f.get(property)), props.getProperty(String.valueOf(f.get(property))));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        logger.debug("Property loaded :\n " + mapProps.toString());
        
        return mapProps;
    }

    public static Properties getPropertysFile(String url) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(url)) {
            prop.load(input);
            return prop;
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return null;
    }

}
