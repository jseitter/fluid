package com.subside.fluid;

import java.net.*;
import java.io.*;

/**
 * This class is used to verify that a client connection is 
 * coming from an allowed host. 
 *
 * @author Lars Samuelsson
 */
public class ClientValidator {
    public static synchronized boolean isAllowed(InetAddress address, String ruleFile) {
    	boolean all = false;
    	try {
    		BufferedReader rules = new BufferedReader(new 
    				InputStreamReader(new FileInputStream(ruleFile)));
    		String rule, host;
    		while((rule = rules.readLine()) != null) {
    			if(rule.startsWith("+")) {
    				host = rule.substring(1, rule.length()).trim();
    				if(host.equals(address.getHostName()) ||
    						host.equals(address.getHostAddress()))
    					return true;
    				if(host.toUpperCase().equals("ALL"))
    					all = true;
    			}
    			if(rule.startsWith("-")) {
    				host = rule.substring(1, rule.length()).trim();
    				if(host.equals(address.getHostName()) ||
    						host.equals(address.getHostAddress()))
    					return false;
    				if(host.toUpperCase().equals("ALL"))
    					all = false;
    			}
    		}
    	}
    	catch(IOException e) {
    		return false; // allow none if file is bad
    	}
    	return all;
    }
}
