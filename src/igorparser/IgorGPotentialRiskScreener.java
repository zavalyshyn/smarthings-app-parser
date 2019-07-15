package igorparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import groovy.lang.GroovyShell;


public class IgorGPotentialRiskScreener {

		/********************************************/
		private static String project_root;
		private static AppDatabase appDB;
		private static String smartAppPath;
		private static SmartThingsApiReference smartthingsAPIRef;
		/********************************************/
	
		public static void init(String projectRootFolder) {
			project_root = projectRootFolder;
			smartAppPath = project_root + "/input/smartapps";
			smartthingsAPIRef = new SmartThingsApiReference(project_root);
			appDB = new AppDatabase(smartthingsAPIRef);
			
		}
		
		static void extractInputInfo() {
			CompilerConfiguration CC = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
			IgorGSmartAppPreProcessor igorgSmartAppPreProcessor = new IgorGSmartAppPreProcessor(appDB,smartthingsAPIRef);
			GroovyShell gShell;
			File smartAppFolder = new File(smartAppPath);
			List<String> gClassPath;

			gClassPath = CC.getClasspath();
			gClassPath.add(project_root + "/lib/groovy/SmartThings.jar");
			CC.setClasspathList(gClassPath);
			CC.addCompilationCustomizers(igorgSmartAppPreProcessor);
			gShell = new GroovyShell(CC);
			for (File file : smartAppFolder.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".groovy")) {
					try {
						System.out.println("Parsing: " + file.getName());
						appDB.createNewAppEntry();
						gShell.parse(file);	// better
						// gShell.evaluate(file);	// this will also try to compile the files. Doesn't work properly.
					} catch (CompilationFailedException e1) {
						// TODO Auto-generated catch block
						// fail silently
						// e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						// e1.printStackTrace();
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
			
			
			System.out.println("############################################");
			
			System.out.println();
			System.out.println("STATISTICS");
			System.out.println();
			
			// analyze the statistics, write to file, create graphs
			ArrayList<HashMap<String,ArrayList<String>>> apps = appDB.getAppList();
			ArrayList<String> sources = appDB.getAllSources();
			ArrayList<String> sinks = appDB.getAllSinks();
			ArrayList<String> capabilities = appDB.getAllCapabilities();
			ArrayList<String> sensitiveCapabilities = appDB.getAllSensitiveCapabilities();
			ArrayList<String> actualSubscriptions = appDB.getAllActualSubscriptions();
			ArrayList<String> sensitiveSubscriptions = appDB.getAllSensitiveSubscriptions();
			ArrayList<String> actualActions = appDB.getAllActualActions();
			ArrayList<String> sensitiveActions = appDB.getAllSensitiveActions();
			ArrayList<String> categories = appDB.getAllCategories();
			
			System.out.println("Namber of apps analyzed: " + apps.size());
			System.out.println("Number of sensitive sources: " + sources.size());
			System.out.println("Number of sensitive sinks: " + sinks.size());
			System.out.println("Number of capabilities: " + capabilities.size());
			System.out.println("Number of sensitive capabilities: " + sensitiveCapabilities.size());
			System.out.println("Number of subscriptions: " + actualSubscriptions.size());
			System.out.println("Number of sensitive subscriptions: " + sensitiveSubscriptions.size());
			System.out.println("Number of sensitive actions: " + sensitiveActions.size());
			System.out.println("Number of categories: " + categories.size());
			
			System.out.println();
			
			System.out.println("Number of apps with sensitive sinks: ");
			
			for (String sink: sinks) {
				int i=0;
				for (HashMap<String,ArrayList<String>> app : apps) {
					if (hasSink(app,sink)) {
						i+=1;
					}
				}
				System.out.println(sink + ": " + i + "/" + apps.size() + " (" + i*100/apps.size()+ "%)");
			}
			
			System.out.println();
			
			System.out.println("Number of apps with an exposed endpoint (webapps): ");
			
			int i =0;
			for (HashMap<String,ArrayList<String>> app : apps) {
				if (hasSink(app,"mappings")) {
					i+=1;
				}
			}
			System.out.println("mappings" + ": " + i + "/" + apps.size() + " (" + i*100/apps.size()+ "%)");
			
			System.out.println();
			
			System.out.println("Number of apps that make HTTP requests: ");
			
			int k=0;
			for (HashMap<String,ArrayList<String>> app : apps) {
				if (hasSink(app,"httpGet") || hasSink(app,"httpPost") || hasSink(app,"httpPostJson") || hasSink(app,"httpDelete") || hasSink(app,"httpPutJson")) {
					k+=1;
				}
			}
			System.out.println("HTTP Requests" + ": " + k + "/" + apps.size() + " (" + k*100/apps.size()+ "%)");
			
			System.out.println();
			
			System.out.println("Number of apps with sensitive sources: ");
			
			for (String source: sources) {
				int l=0;
				for (HashMap<String,ArrayList<String>> app : apps) {
					if (hasSource(app,source)) {
						l+=1;
					}
				}
				System.out.println(source + ": " + l + "/" + apps.size() + " (" + l*100/apps.size()+ "%)");
			}
			
			System.out.println();
			
			System.out.println("Number of apps with sensitive capabilities: ");
			
			for (String capability: sensitiveCapabilities) {
				int t=0;
				for (HashMap<String,ArrayList<String>> app : apps) {
					if (hasSensitiveCapability(app, capability)) {
						t+=1;
					}
				}
				System.out.println(capability + ": " + t + "/" + apps.size() + " (" + t*100/apps.size()+ "%)");
			}
			
			System.out.println();
			
			System.out.println("Number of apps with sensitive actions: ");
			
			for (String action: sensitiveActions) {
				int t=0;
				for (HashMap<String,ArrayList<String>> app : apps) {
					if (hasSensitiveAction(app, action)) {
						t+=1;
					}
				}
				System.out.println(action + ": " + t + "/" + apps.size() + " (" + t*100/apps.size()+ "%)");
			}
			
			System.out.println("############################################");
			
			// write parser results to a JSON file
			JSONObject allAppsObj = createJSONFile(apps);

			// print the JSON object
			// System.out.println(allAppsObj);
			
			
			//
			// generate tables
			//
			
			// sensitive sources VS sensitive sinks table
			int[][] sourcesVSSinks = new int[sources.size()][sinks.size()];
			for (HashMap<String,ArrayList<String>> app : apps) {
				for (String source : sources) {
					for (String sink : sinks) {
						if (hasSource(app,source) && hasSink(app,sink)) {
							int sourceIndex = sources.indexOf(source);
							int sinkIndex = sinks.indexOf(sink);
							if (sourcesVSSinks[sourceIndex][sinkIndex]!=0) {
								int currentValue = sourcesVSSinks[sourceIndex][sinkIndex];
								sourcesVSSinks[sourceIndex][sinkIndex] = currentValue+1;
							} else {
								sourcesVSSinks[sourceIndex][sinkIndex] = 1;
							}
						}
					}
				}
			}
			
			// sensitive capabilities VS sensitive sinks table
			int[][] sensCapabilitiesVSSinks = new int[sensitiveCapabilities.size()][sinks.size()];
			for (HashMap<String,ArrayList<String>> app : apps) {
				for (String capability : sensitiveCapabilities) {
					for (String sink : sinks) {
						if (hasSensitiveCapability(app,capability) && hasSink(app,sink)) {
							int capabilityIndex = sensitiveCapabilities.indexOf(capability);
							int sinkIndex = sinks.indexOf(sink);
							if (sensCapabilitiesVSSinks[capabilityIndex][sinkIndex]!=0) {
								int currentValue = sensCapabilitiesVSSinks[capabilityIndex][sinkIndex];
								sensCapabilitiesVSSinks[capabilityIndex][sinkIndex] = currentValue+1;
							} else {
								sensCapabilitiesVSSinks[capabilityIndex][sinkIndex] = 1;
							}
						}
					}
				}
			}
			
			// sensitive subscriptions VS sensitive sinks table
			int[][] sensSubscriptionsVSSinks = new int[sensitiveSubscriptions.size()][sinks.size()];
			for (HashMap<String,ArrayList<String>> app : apps) {
				for (String subscription : sensitiveSubscriptions) {
					for (String sink : sinks) {
						if (hasSensitiveSubscription(app,subscription) && hasSink(app,sink)) {
							int subscriptionIndex = sensitiveSubscriptions.indexOf(subscription);
							int sinkIndex = sinks.indexOf(sink);
							if (sensSubscriptionsVSSinks[subscriptionIndex][sinkIndex]!=0) {
								int currentValue = sensSubscriptionsVSSinks[subscriptionIndex][sinkIndex];
								sensSubscriptionsVSSinks[subscriptionIndex][sinkIndex] = currentValue+1;
							} else {
								sensSubscriptionsVSSinks[subscriptionIndex][sinkIndex] = 1;
							}
						}
					}
				}
			}
			
			// sensitive actions VS sensitive sinks table
			int[][] sensActionsVSSinks = new int[sensitiveActions.size()][sinks.size()];
			for (HashMap<String,ArrayList<String>> app : apps) {
				for (String action : sensitiveActions) {
					for (String sink : sinks) {
						if (hasSensitiveAction(app,action) && hasSink(app,sink)) {
							int actionIndex = sensitiveActions.indexOf(action);
							int sinkIndex = sinks.indexOf(sink);
							if (sensActionsVSSinks[actionIndex][sinkIndex]!=0) {
								int currentValue = sensActionsVSSinks[actionIndex][sinkIndex];
								sensActionsVSSinks[actionIndex][sinkIndex] = currentValue+1;
							} else {
								sensActionsVSSinks[actionIndex][sinkIndex] = 1;
							}
						}
					}
				}
			}
			
			// print the table for debugging
			// System.out.println("sourcesVSSinks");
			// printGrid(sourcesVSSinks, sources.size(), sinks.size());
			// System.out.println("capabilitiesVSSinks");
			// printGrid(capabilitiesVSSinks, capabilities.size(), sinks.size());
			// System.out.println("subscriptionsVSSinks");
			// printGrid(subscriptionsVSSinks, subscriptions.size(), sinks.size());
			// System.out.println("actionsVSSinks");
			// printGrid(actionsVSSinks, actions.size(), sinks.size());
			
			
			// export to a csv file
			createCSVFileFrom2DArray(sourcesVSSinks, sources, sinks, "sourcesVSsinks");
			createCSVFileFrom2DArray(sensCapabilitiesVSSinks, sensitiveCapabilities, sinks, "sensCapabilitiesVSsinks");
			createCSVFileFrom2DArray(sensSubscriptionsVSSinks, sensitiveSubscriptions, sinks, "sensSubscriptionsVSsinks");
			createCSVFileFrom2DArray(sensActionsVSSinks, sensitiveActions, sinks, "sensActionsVSSinks");
			
		}
		
		
		//
		// checkers
		//
		
		private static boolean hasSource(HashMap<String,ArrayList<String>> app, String source) {
			ArrayList<String> sources = app.get("sources");
			return sources.contains(source);
		}
		
		private static boolean hasSink(HashMap<String,ArrayList<String>> app, String sink) {
			ArrayList<String> sinks = app.get("sinks");
			return sinks.contains(sink);
		}
		
		private static boolean hasActualAction(HashMap<String,ArrayList<String>> app, String action) {
			ArrayList<String> actions = app.get("actualActions");
			return actions.contains(action);
		}
		
		private static boolean hasSensitiveAction(HashMap<String,ArrayList<String>> app, String action) {
			ArrayList<String> actions = app.get("sensitiveActions");
			return actions.contains(action);
		}
		
		private static boolean hasActualSubscription(HashMap<String,ArrayList<String>> app, String subscription) {
			ArrayList<String> subscriptions = app.get("actualSubscriptions");
			return subscriptions.contains(subscription);
		}
		
		private static boolean hasSensitiveSubscription(HashMap<String,ArrayList<String>> app, String subscription) {
			ArrayList<String> subscriptions = app.get("sensitiveSubscriptions");
			return subscriptions.contains(subscription);
		}
		
		private static boolean hasCapability(HashMap<String,ArrayList<String>> app, String capability) {
			ArrayList<String> capabilities = app.get("capabilities");
			return capabilities.contains(capability);
		}
		
		private static boolean hasSensitiveCapability(HashMap<String,ArrayList<String>> app, String capability) {
			ArrayList<String> capabilities = app.get("sensitiveCapabilities");
			return capabilities.contains(capability);
		}
		
		private static boolean hasCategory(HashMap<String,ArrayList<String>> app, String category) {
			ArrayList<String> categories = app.get("category");
			return categories.contains(category);
		}
		

		//
		// helpers
		//
		
		public static void printGrid(int[][] inputArray, int rowsNum, int colsNum) {
			for(int i = 0; i < rowsNum; i++) {
		      for(int j = 0; j < colsNum; j++) {
		         System.out.printf("%5d ",inputArray[i][j]);
		      }
		      System.out.println();
		      System.out.println();
			}
		}
		
		
		public static JSONObject createJSONFile(ArrayList<HashMap<String,ArrayList<String>>> apps) {
			JSONObject allAppsObj = new JSONObject();
			JSONArray appsArray = new JSONArray();
			
			for (HashMap<String,ArrayList<String>> map : apps) {
				ArrayList<String> appName = map.get("name");
				ArrayList<String> appDescription = map.get("description");
				ArrayList<String> appCategory = map.get("category");
				ArrayList<String> appCapabilities = map.get("capabilities");
				ArrayList<String> appSensitiveCapabilities = map.get("sensitiveCapabilities");
				ArrayList<String> appActualSubscriptions = map.get("actualSubscriptions");
				ArrayList<String> appSensitiveSubscriptions = map.get("sensitiveSubscriptions");
				ArrayList<String> appSources = map.get("sources");
				ArrayList<String> appSinks = map.get("sinks");
				ArrayList<String> appActualActions = map.get("actualActions");
				ArrayList<String> appSensitiveActions = map.get("sensitiveActions");
//				 System.out.println("Name: " + appName.toString());
//				 System.out.println("Description: " + appDescription.toString());
//				 System.out.println("Category: " + appCategory.toString());
//				 System.out.println("Capabilities: " + appCapabilities.toString());
//				 System.out.println("Sensitivie Capabilities: " + appSensitiveCapabilities.toString());
//				 System.out.println("Actual Subscriptions: " + appActualSubscriptions.toString());
//				 System.out.println("Sensitive Subscriptions: " + appSensitiveSubscriptions.toString());
//				 System.out.println("Sensitive sources: " + appSources.toString());
//				 System.out.println("Sensitive sinks: " + appSinks.toString());
//				 System.out.println("Actual actions: " + appActualActions.toString());
//				 System.out.println("Sensitive actions: " + appSensitiveActions.toString());
//				 System.out.println("####################################");
				
				// Create a JSON object representing each app
				JSONObject appObj = new JSONObject();
				if (!appName.isEmpty()) {
					appObj.put("name", appName.get(0));
				}
				if (!appDescription.isEmpty()) {
					appObj.put("description", appDescription.get(0));
				}
				if (!appCategory.isEmpty()) {
					appObj.put("category", appCategory.get(0));
				}
				
				JSONArray capArray = new JSONArray();
				for (String cap : appCapabilities) {
					capArray.add(cap);
				}
				appObj.put("capabilities", capArray);
				
				JSONArray sensCapArray = new JSONArray();
				for (String cap : appSensitiveCapabilities) {
					sensCapArray.add(cap);
				}
				appObj.put("sensitiveCapabilities", sensCapArray);
				
				JSONArray subArray = new JSONArray();
				for (String sub : appActualSubscriptions) {
					subArray.add(sub);
				}
				appObj.put("subscriptions", subArray);
				
				JSONArray subSenArray = new JSONArray();
				for (String sub : appSensitiveSubscriptions) {
					subSenArray.add(sub);
				}
				appObj.put("sensitiveSubscriptions", subSenArray);
				
				JSONArray sourcesArray = new JSONArray();
				for (String source : appSources) {
					sourcesArray.add(source);
				}
				appObj.put("sources", sourcesArray);
				
				JSONArray sinksArray = new JSONArray();
				for (String sink : appSinks) {
					sinksArray.add(sink);
				}
				appObj.put("sinks", sinksArray);
				
				JSONArray actionsArray = new JSONArray();
				for (String action : appActualActions) {
					actionsArray.add(action);
				}
				appObj.put("actions", actionsArray);
				
				JSONArray SensActionsArray = new JSONArray();
				for (String action : appSensitiveActions) {
					SensActionsArray.add(action);
				}
				appObj.put("sensitiveActions", SensActionsArray);
				
				appsArray.add(appObj);
			}
			
			allAppsObj.put("apps", appsArray);
			
			// write to JSON file
			try (FileWriter file = new FileWriter(smartAppPath + "/appanalysis.json")) {
				file.write(allAppsObj.toJSONString());
				file.flush();
				file.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return allAppsObj;
		}
		
		
		public static void createCSVFileFrom2DArray(int[][] dataArray, ArrayList<String> rowsArray, ArrayList<String> colsArray, String filename) {
			
			try (FileWriter file = new FileWriter(smartAppPath + "/" + filename + ".csv")) {
				
				StringBuilder sb = new StringBuilder();
				
				//create the cols names
				sb.append(" ");	// cell [0][0] is empty
				sb.append(","); // cell [0][0] is empty
				for (String col : colsArray) {
					sb.append(col);
					if (colsArray.indexOf(col)!=colsArray.size()-1) { // do not put coma if it's a last column
						sb.append(",");
					}
				}
				sb.append("\n");
				
				//now fill the matrix with a row name in a first cell
				for (int i=0; i< rowsArray.size(); i++) {
					int[] rowArray = dataArray[i];
					if (!hasNonZeroElement(rowArray)) {	// fix to avoid empty lines in csv file
						continue;
					}
					sb.append(rowsArray.get(i));
					sb.append(",");
					for (int j=0; j<colsArray.size(); j++) {
						sb.append(dataArray[i][j]);
						if (j!=colsArray.size()-1) {	// do not put coma if it's a last column
							sb.append(",");
						}
					}
					sb.append("\n");
				}
				
				file.write(sb.toString());
				file.flush();
				file.close();

			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private static boolean hasNonZeroElement(int[] array) {
			for (int i=0; i<array.length; i++) {
				if (array[i]!=0) return true;
			}
			return false;
		}
}
