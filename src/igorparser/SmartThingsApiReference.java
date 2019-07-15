package igorparser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SmartThingsApiReference {
	
	private HashMap<String,HashMap<String,ArrayList<String>>> capabilties = new HashMap<>();
	private String project_root;

	public SmartThingsApiReference(String projectFolder) {
		
		this.project_root = projectFolder;
		
		// read and load the capabilities from the JSON reference file
		JSONParser parser = new JSONParser();
		
		// System.out.println("Reading SmartThings API Capabilities JSON file ...");
		
		try {
			JSONArray capabilitiesArray = (JSONArray) parser.parse(new FileReader(project_root + "/input/smartapps/configs/capability-reference.json"));
			for (Object capObject : capabilitiesArray) {
				JSONObject capability = (JSONObject) capObject;
				String capName = (String) capability.get("name");
				HashMap<String,ArrayList<String>> capabilityMap = new HashMap<>();
				ArrayList<String> attributes = new ArrayList<>();
				ArrayList<String> commands = new ArrayList<>();
				JSONArray capAttributes = (JSONArray) capability.get("attributes");
				for (Object attrObj : capAttributes) {
					JSONObject attribute = (JSONObject) attrObj;
					String attrName = (String) attribute.get("name");
					attributes.add(attrName);
					List<String> attrValues = (List) attribute.get("values");
					for (String attrValue : attrValues) {
						attributes.add(attrName+"."+attrValue);
					}
				}
				JSONArray capCommands = (JSONArray) capability.get("commands");
				for (Object comObj : capCommands) {
					JSONObject command = (JSONObject) comObj;
					String comName = (String) command.get("name");
					commands.add(comName);
				}
				ArrayList<String> nameArr = new ArrayList<>();
				nameArr.add(capName);
				capabilityMap.put("name", nameArr);
				capabilityMap.put("attributes", attributes);
				capabilityMap.put("commands", commands);
				this.capabilties.put(capName, capabilityMap);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Couldn't find and read a capability-reference.json file in " + project_root + "/input/smartapps/configs/ folder");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	//
	// Getters
	//
	
	public HashMap<String,ArrayList<String>> getCapability(String capName) {
		if (this.capabilties.containsKey(capName)) {
			return this.capabilties.get(capName);
		}
		else {
			System.out.println("Unknown capability: " + capName);
			return null;
		}
	}
	
	public ArrayList<String> getAttributesForCapability(String capName) {
		if (this.capabilties.containsKey(capName)) {
			return this.capabilties.get(capName).get("attributes");
		}
		else {
			System.out.println("Unknown capability: " + capName);
			return null;
		}
	}
	
	public ArrayList<String> getCommandsForCapability(String capName) {
		if (this.capabilties.containsKey(capName)) {
			return this.capabilties.get(capName).get("commands");
		}
		else {
			System.out.println("Unknown capability: " + capName);
			return null;
		}
	}
	
	public boolean isValidCapability(String capName) {
		return this.capabilties.containsKey(capName);
	}
	
	public HashMap<String,HashMap<String,ArrayList<String>>> getAllCapabilities() {
		return this.capabilties;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
