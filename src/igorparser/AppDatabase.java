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
	private SmartThingsApiReference apiRef;
	
	public AppDatabase(SmartThingsApiReference smartthingsApiRef) {
		this.apiRef = smartthingsApiRef;
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
		ArrayList<String> appSensitiveCapabilities = new ArrayList<>();
		app.put("sensitiveCapabilities", appSensitiveCapabilities);
		ArrayList<String> appSensitiveSubscriptions = new ArrayList<>();
		app.put("sensitiveSubscriptions", appSensitiveSubscriptions);
		ArrayList<String> appInferredSubscriptions = new ArrayList<>();
		app.put("inferredSubscriptions", appInferredSubscriptions);
		ArrayList<String> appActualSubscriptions = new ArrayList<>();
		app.put("actualSubscriptions", appActualSubscriptions);
		ArrayList<String> appSources = new ArrayList<>();
		app.put("sources", appSources);
		ArrayList<String> appInferredActions = new ArrayList<>();
		app.put("inferredActions", appInferredActions);
		ArrayList<String> appSensitiveActions = new ArrayList<>();
		app.put("sensitiveActions", appSensitiveActions);
		ArrayList<String> appActualActions = new ArrayList<>();
		app.put("actualActions", appActualActions);
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
			// set inferred actions and subscriptions
			ArrayList<String> actions = apiRef.getCommandsForCapability(capability);
			for (String action : actions) {
				this.setInferredAction(action);
			}
			ArrayList<String> subscriptions = apiRef.getAttributesForCapability(capability);
			for (String sub : subscriptions) {
				this.setInferredSubscription(sub);
			}
		}
	}
	
	public void setSensitiveCapability(String capability) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentSensitiveCapabilities = currentApp.get("sensitiveCapabilities");
		if (!currentSensitiveCapabilities.contains(capability)) {
			currentSensitiveCapabilities.add(capability);
		}
	}
	
	public void setSensitiveSubscription(String subscription) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentSensitiveSubscriptions = currentApp.get("sensitiveSubscriptions");
		if (!currentSensitiveSubscriptions.contains(subscription)) {
			currentSensitiveSubscriptions.add(subscription);
		}
	}
	
	public void setAppActualSubscription(String subscription) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentActualSubscriptions = currentApp.get("actualSubscriptions");
		if (!currentActualSubscriptions.contains(subscription)) {
			currentActualSubscriptions.add(subscription);
		}
	}
	
	public void setInferredSubscription(String subscription) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentInferredSubscriptions = currentApp.get("inferredSubscriptions");
		if (!currentInferredSubscriptions.contains(subscription)) {
			currentInferredSubscriptions.add(subscription);
		}
	}
	
	public void setSensitiveSource(String source) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentSources = currentApp.get("sources");
		if (!currentSources.contains(source)) {
			currentSources.add(source);
		}
	}
	
	public void setSensitiveAction(String action) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentSensitiveActions = currentApp.get("sensitiveActions");
		if (!currentSensitiveActions.contains(action)) {
			currentSensitiveActions.add(action);
		}
	}
	
	public void setActualAction(String action) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentActualActions = currentApp.get("actualActions");
		if (!currentActualActions.contains(action)) {
			currentActualActions.add(action);
		}
	}
	
	public void setInferredAction(String action) {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		ArrayList<String> currentInferredActions = currentApp.get("inferredActions");
		if (!currentInferredActions.contains(action)) {
			currentInferredActions.add(action);
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
	
	public ArrayList<String> getInferredSubscriptionsForCurrentApp() {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		return currentApp.get("inferredSubscriptions");
	}
	
	public ArrayList<String> getInferredActionsForCurrentApp() {
		HashMap<String, ArrayList<String>> currentApp = getLatestApp();
		return currentApp.get("inferredActions");
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
	
	public ArrayList<String> getAllSensitiveCapabilities() {
		ArrayList<String> sensitiveCapabilities = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> appCaps = appMap.get("sensitiveCapabilities");
			for (String cap : appCaps) {
				if (!sensitiveCapabilities.contains(cap)) {
					sensitiveCapabilities.add(cap);
				}
			}
		}
		return sensitiveCapabilities;
	}
	
	public ArrayList<String> getAllSensitiveSubscriptions() {
		ArrayList<String> sensitiveSubscriptions = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> appSubs = appMap.get("sensitiveSubscriptions");
			for (String sub : appSubs) {
				if (!sensitiveSubscriptions.contains(sub)) {
					sensitiveSubscriptions.add(sub);
				}
			}
		}
		return sensitiveSubscriptions;
	}
	
	public ArrayList<String> getAllActualSubscriptions() {
		ArrayList<String> actualSubscriptions = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> appActualSubs = appMap.get("actualSubscriptions");
			for (String sub : appActualSubs) {
				if (!actualSubscriptions.contains(sub)) {
					actualSubscriptions.add(sub);
				}
			}
		}
		return actualSubscriptions;
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
	
	public ArrayList<String> getAllActualActions() {
		ArrayList<String> actualActions = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> actActions = appMap.get("actualActions");
			for (String act : actActions) {
				if (!actualActions.contains(act)) {
					actualActions.add(act);
				}
			}
		}
		return actualActions;
	}
	
	public ArrayList<String> getAllSensitiveActions() {
		ArrayList<String> sensitiveActions = new ArrayList<String>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			ArrayList<String> sensActions = appMap.get("sensitiveActions");
			for (String act : sensActions) {
				if (!sensitiveActions.contains(act)) {
					sensitiveActions.add(act);
				}
			}
		}
		return sensitiveActions;
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
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForActualSubscription(String subscription) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("actualSubscriptions").contains(subscription)) {
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
	
	public ArrayList<HashMap<String,ArrayList<String>>> getAppsForActualAction(String action) {
		ArrayList<HashMap<String,ArrayList<String>>> apps = new ArrayList<HashMap<String,ArrayList<String>>>();
		for (HashMap<String,ArrayList<String>> appMap : this.appList) {
			if (appMap.get("actualActions").contains(action)) {
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
