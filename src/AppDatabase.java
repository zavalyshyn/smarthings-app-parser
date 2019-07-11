package igorparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * This is the database class with all the collected
 * data on the apps analyzed
 */
public class AppDatabase {

	private ArrayList<HashMap<String,ArrayList<String>>> appList = new ArrayList<>();
	private String test;
	
	public AppDatabase() {
	}
	
	public void createNewAppEntry() {
		HashMap<String,ArrayList<String>> app = new HashMap<>();
		ArrayList<String> appName = new ArrayList<>();
		app.put("name", appName);
		ArrayList<String> appDescription = new ArrayList<>();
		app.put("description", appDescription);
		ArrayList<String> appCategory = new ArrayList<>();
		app.put("category", appCategory);
		ArrayList<String> appCapabilities = new ArrayList<>();
		app.put("capabilities", appCapabilities);
		ArrayList<String> appSubscriptions = new ArrayList<>();
		app.put("subscriptions", appSubscriptions);
		ArrayList<String> appSources = new ArrayList<>();
		app.put("sources", appSources);
		ArrayList<String> appActions = new ArrayList<>();
		app.put("actions", appActions);
		ArrayList<String> appSinks = new ArrayList<>();
		app.put("sinks", appSinks);
		
		appList.add(app);
		
	}
	
	///
	/// SETTERS
	///
	
	
	public void setAppName(String name) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentName = currentApp.get("name");
		currentName.add(name);		
	}
	
	public void setAppDescription(String description) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentDescription = currentApp.get("description");
		currentDescription.add(description);		
	}
	
	public void setAppCategory(String category) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentCategory = currentApp.get("category");
		currentCategory.add(category);		
	}
	
	public void setAppCapability(String capability) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentCapabilities = currentApp.get("capabilities");
		if (!currentCapabilities.contains(capability)) {
			currentCapabilities.add(capability);
		}
	}
	
	public void setAppSubscription(String subscription) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentSubscriptions = currentApp.get("subscriptions");
		if (!currentSubscriptions.contains(subscription)) {
			currentSubscriptions.add(subscription);
		}
	}
	
	public void setSource(String source) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentSources = currentApp.get("sources");
		if (!currentSources.contains(source)) {
			currentSources.add(source);
		}
	}
	
	public void setAction(String action) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentActions = currentApp.get("actions");
		if (!currentActions.contains(action)) {
			currentActions.add(action);
		}
	}
	
	public void setSink(String sink) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentSinks = currentApp.get("sinks");
		if (!currentSinks.contains(sink)) {
			currentSinks.add(sink);
		}
	}
	
	
	///
	/// GETTERS
	///
	
	private HashMap<String,ArrayList<String>> getLatestApp()  {
		int listSize = appList.size();
		HashMap<String, ArrayList<String>> currentApp = appList.get(listSize-1);
		return currentApp;
	}
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppList() {
		return this.appList;
	}
	
	public ArrayList<String> getAllCapabilities() {
		ArrayList<String> capabilities = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> appCaps = appMap.get("capabilities");
			for (String cap : appCaps) {
				if (!capabilities.contains(cap)) {
					capabilities.add(cap);
				}
			}
		}
		return capabilities;
	}
	
	public ArrayList<String> getAllSubscriptions() {
		ArrayList<String> subscriptions = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> appSubs = appMap.get("subscriptions");
			for (String sub : appSubs) {
				if (!subscriptions.contains(sub)) {
					subscriptions.add(sub);
				}
			}
		}
		return subscriptions;
	}
	
	public ArrayList<String> getAllSources() {
		ArrayList<String> sources = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> appSources = appMap.get("sources");
			for (String source : appSources) {
				if (!sources.contains(source)) {
					sources.add(source);
				}
			}
		}
		return sources;
	}
	
	public ArrayList<String> getAllSinks() {
		ArrayList<String> sinks = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> appSinks = appMap.get("sinks");
			for (String sink : appSinks) {
				if (!sinks.contains(sink)) {
					sinks.add(sink);
				}
			}
		}
		return sinks;
	}
	
	public ArrayList<String> getAllActions() {
		ArrayList<String> actions = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> action = appMap.get("actions");
			for (String act : action) {
				if (!actions.contains(act)) {
					actions.add(act);
				}
			}
		}
		return actions;
	}
	
	public ArrayList<String> getAllCategories() {
		ArrayList<String> categories = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> category = appMap.get("category");
			for (String cat : category) {
				if (!categories.contains(cat)) {
					categories.add(cat);
				}
			}
		}
		return categories;
	}
	
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForCategory(String category) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("category").contains(category)) {
				apps.add(appMap);
			}
		}
		return apps;
	}
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForCapability(String capability) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("capabilities").contains(capability)) {
				apps.add(appMap);
			}
		}
		return apps;
	}
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForSubscription(String subscription) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("subscriptions").contains(subscription)) {
				apps.add(appMap);
			}
		}
		return apps;
	}
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForSource(String source) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("sources").contains(source)) {
				apps.add(appMap);
			}
		}
		return apps;
	}
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForAction(String action) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("actions").contains(action)) {
				apps.add(appMap);
			}
		}
		return apps;
	}
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForSink(String sink) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("sinks").contains(sink)) {
				apps.add(appMap);
			}
		}
		return apps;
	}
	

}
