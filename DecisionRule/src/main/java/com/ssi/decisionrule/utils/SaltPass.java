/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssi.decisionrule.utils;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


public class SaltPass {

    private static final char[] PASSWORD = "VOCS20131402".toCharArray();

    private static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3));
        }
        return sb.toString();
    }

    public static String genPasswdOneWay(String passwd, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passwd.getBytes("UTF8"));
            String encrytPass = hex(md.digest()) + salt;
            md.reset();
            md.update(encrytPass.getBytes());
            return hex(md.digest());

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {

        }
        return null;
    }

    public static String genPasswd(String passwd, String salt) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            String tmpSalt = salt;
            if (salt.length() < 8) {
                for (int i = 0; i < 8 - salt.length(); i++) {
                    tmpSalt += "0";
                }
            } else {
                tmpSalt = salt.substring(salt.length() - 8, salt.length());
            }
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(tmpSalt.getBytes(), 20));
            byte[] encoded = Base64.getEncoder().encode(pbeCipher.doFinal(passwd.getBytes("UTF-8")));
            return new String(encoded);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            return passwd;
        }
    }

    public static String decodePasswd(String encrypt, String salt) {
//        byte[] decoded = Base64.decodeBase64(encrypt.getBytes());
//        
//        String StrDecoded = new String(decoded);
//        return String.valueOf((Long.parseLong(StrDecoded) - Long.parseLong(salt))/7-1);
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            String tmpSalt = salt;
            if (salt.length() < 8) {
                for (int i = 0; i < 8 - salt.length(); i++) {
                    tmpSalt += "0";
                }
            } else {
                tmpSalt = salt.substring(salt.length() - 8, salt.length());
            }
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(tmpSalt.getBytes(), 20));
            byte[] decoded = Base64.getDecoder().decode(encrypt.getBytes());
            return new String(pbeCipher.doFinal(decoded), "UTF-8");
        } catch (Exception ex) {
            return encrypt;
        }
    }
}
