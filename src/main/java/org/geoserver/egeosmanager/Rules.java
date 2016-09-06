package org.geoserver.egeosmanager;

import it.egeos.geoserver.utils.exceptions.RuleExistsException;
import it.egeos.geoserver.utils.exceptions.RuleNotExistsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.geoserver.egeosmanager.abstracts.RulesXMLResource;
import org.geoserver.egeosmanager.annotations.Help;
import org.geoserver.egeosmanager.annotations.Parameter;
import org.geoserver.rest.format.DataFormat;

/**
 * 
 * @author Federico C. Guizzardi - cippinofg <at> gmail.com
 * 
 * Rules is a REST Callable for manage rules.
 * 
 */
@Help(text="This method allow to manage rules on Geoserver.")
public class Rules extends RulesXMLResource{
	public static String WORKSPACE_KEY="workspace";
	public static String LAYER_KEY="layer";
	public static String METHOD_KEY="method";
	public static String ROLE_KEY="role";
	public static String MISSING_KEY="missing";
	public static String APPEND_KEY="append";
	
	/*
	 * Enable POST
	 */
	@Override
	public boolean allowPost(){
		return true;
	}

	/*
	 * Enable DELETE
	 */
	@Override
	public boolean allowDelete(){
		return true;
	}

	/*
	 * Returns the list of rules 
	 */
	@Help(
		text="Returns a JSON object with rule paths as keys and a list of roles as value."		
	)	
	protected Object handleGetBody(DataFormat format) throws Exception{
		return manager.getRules();
	}
	
	/*
	 * Try to create a rule in layers.default or add all roles to an existing rule 
	 */
	@Help(
		text="Add a role to a rule if exists, otherwise creates a new rule.",
		requires = {
			@Parameter(name="workspace",description="the name of the workspace."),
			@Parameter(name="method",description="the name of the method (r,w,a)."),
			@Parameter(name="role",description="the name of the role you want to add to the rule."),
		},
		optionals = {			
			@Parameter(name="append",description="this flag avoid a creation of a new rule when no one is found."),
			@Parameter(name="layer",description="the name of the layer of the rule."),
		}
	)		
	protected void handlePostBody(HashMap<String, HashMap<String, String>> params,DataFormat format) throws Exception{
		String ws = params.get(REQUIRED).get(WORKSPACE_KEY);
		String md = params.get(REQUIRED).get(METHOD_KEY);
		String rl = params.get(REQUIRED).get(ROLE_KEY);
		String ap = params.get(OPTIONAL).get(APPEND_KEY);
		String ly = params.get(OPTIONAL).get(LAYER_KEY);
		
		ArrayList<String> roles = new ArrayList<String>(Arrays.asList(rl.split(",")));
		try {
			if (ap!=null && ap.trim().equalsIgnoreCase("true"))
				manager.addToRule(ws,ly,md.charAt(0), roles);
			else
				manager.createRule(ws,ly, md.charAt(0), roles);
		} 
		catch (RuleExistsException e) {
			manager.addToRule(ws,ly,md.charAt(0), roles);
		}
		manager.save();
		getResponse().setEntity(format.toRepresentation("ok"));
	}
	
	/*
	 * Try to remove a rule or a relation between a rule and a list of roles  
	 */
	@Help(
		text="Try to remove a rule or a relation between a rule and a list of roles.",
		requires = {
			@Parameter(name="workspace",description="the name of the workspace."),
			@Parameter(name="method",description="the name of the method (r,w,a)."),			
		},
		optionals = {			
			@Parameter(name="role",description="the name of the role you want to remove from the rule."),					
			@Parameter(name="missing",description="this flag allow to ignore if the rule you want to delete doesn't exist."),
			@Parameter(name="layer",description="the name of the layer of the rule."),
		}
	)	
	protected void handleDeleteBody(HashMap<String, HashMap<String, String>> params,DataFormat format) throws Exception{
		String res="ok";
		String ws = params.get(REQUIRED).get(WORKSPACE_KEY);
		String md = params.get(REQUIRED).get(METHOD_KEY);
		String rl = params.get(OPTIONAL).get(ROLE_KEY);
		String ms = params.get(OPTIONAL).get(MISSING_KEY);
		String ly = params.get(OPTIONAL).get(LAYER_KEY);
		
		try {
			if (rl!=null && rl.length()>0){
				//if there are a list of roles, we remove only that set from a rule
				ArrayList<String> roles = new ArrayList<String>(Arrays.asList(rl.split(",")));				
				manager.delToRule(ws, ly, md.charAt(0),roles,true);
			}
			else
				//no rules in parameters, so we delete the rule 
				manager.deleteRule(ws, ly, md.charAt(0));
		}
		catch (RuleNotExistsException e) {
			if (ms!=null && ms.trim().equalsIgnoreCase("true"))
				res=e.getMessage();				
			else
				throw e;
		}
		manager.save();
		getResponse().setEntity(format.toRepresentation(res));
	}
	
	@SuppressWarnings("serial")
	@Override
	protected ArrayList<String> getPOSTReq() {
		return new ArrayList<String>(){{
			add(WORKSPACE_KEY);			
			add(METHOD_KEY);
			add(ROLE_KEY);
		}};
	}

	@SuppressWarnings("serial")
	@Override
	protected ArrayList<String> getPOSTOpt() {
		return new ArrayList<String>(){{
			add(APPEND_KEY);
			add(LAYER_KEY);
		}};
	}

	@SuppressWarnings("serial")
	@Override
	protected ArrayList<String> getDELETEReq() {
		return new ArrayList<String>(){{
			add(WORKSPACE_KEY);
			add(METHOD_KEY);
		}};
	}

	@SuppressWarnings("serial")
	@Override
	protected ArrayList<String> getDELETEOpt() {
		return new ArrayList<String>(){{
			add(ROLE_KEY);
			add(MISSING_KEY);
			add(LAYER_KEY);
		}};
	}
}
