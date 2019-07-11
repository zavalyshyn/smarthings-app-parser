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
		private static String project_root = "";
		private static AppDatabase appDB = new AppDatabase();
		private static String smartAppPath = "";
		/********************************************/
	
		public static void init(String project_root) {
			IgorGPotentialRiskScreener.project_root = project_root;
			IgorGPotentialRiskScreener.smartAppPath = project_root + "/input/smartapps";
			
		}
		
		static void extractInputInfo() {
			CompilerConfiguration CC = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
			IgorGSmartAppPreProcessor igorgSmartAppPreProcessor = new IgorGSmartAppPreProcessor(appDB);
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
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			
			System.out.println("############################################");
			
			// analyze the statistics, write to file, create graphs
			ArrayList<HashMap<String,ArrayList<String>>> apps = appDB.getAppList();
			ArrayList<String> sources = appDB.getAllSources();
			ArrayList<String> sinks = appDB.getAllSinks();
			ArrayList<String> capabilities = appDB.getAllCapabilities();
			ArrayList<String> subscriptions = appDB.getAllSubscriptions();
			ArrayList<String> actions = appDB.getAllActions();
			ArrayList<String> categories = appDB.getAllCategories();
			
			System.out.println("Namber of apps analyzed: " + apps.size());
			System.out.println("Number of sources: " + sources.size());
			System.out.println("Number of sinks: " + sinks.size());
			System.out.println("Number of capabilities: " + capabilities.size());
			System.out.println("Number of subscriptions: " + subscriptions.size());
			System.out.println("Number of sensitive actions: " + actions.size());
			System.out.println("Number of categories: " + categories.size());
			
			
			// write parser results to a JSON file
			JSONObject allAppsObj = createJSONFile(apps);

			// print the JSON object
			System.out.println(allAppsObj);
			
			
			//
			// generate tables
			//
			
			// sources VS sinks table
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
			
			// capabilities VS sinks table
			int[][] capabilitiesVSSinks = new int[capabilities.size()][sinks.size()];
			for (HashMap<String,ArrayList<String>> app : apps) {
				for (String capability : capabilities) {
					for (String sink : sinks) {
						if (hasCapability(app,capability) && hasSink(app,sink)) {
							int capabilityIndex = sources.indexOf(capability);
							int sinkIndex = sinks.indexOf(sink);
							if (capabilitiesVSSinks[capabilityIndex][sinkIndex]!=0) {
								int currentValue = capabilitiesVSSinks[capabilityIndex][sinkIndex];
								capabilitiesVSSinks[capabilityIndex][sinkIndex] = currentValue+1;
							} else {
								capabilitiesVSSinks[capabilityIndex][sinkIndex] = 1;
							}
						}
					}
				}
			}
			
			// subscriptions VS sinks table
			int[][] subscriptionsVSSinks = new int[subscriptions.size()][sinks.size()];
			for (HashMap<String,ArrayList<String>> app : apps) {
				for (String subscription : subscriptions) {
					for (String sink : sinks) {
						if (hasCapability(app,subscription) && hasSink(app,sink)) {
							int subscriptionIndex = sources.indexOf(subscription);
							int sinkIndex = sinks.indexOf(sink);
							if (subscriptionsVSSinks[subscriptionIndex][sinkIndex]!=0) {
								int currentValue = subscriptionsVSSinks[subscriptionIndex][sinkIndex];
								subscriptionsVSSinks[subscriptionIndex][sinkIndex] = currentValue+1;
							} else {
								subscriptionsVSSinks[subscriptionIndex][sinkIndex] = 1;
							}
						}
					}
				}
			}
			
			// print the table for debugging
			printGrid(sourcesVSSinks, sources.size(), sinks.size());
			printGrid(capabilitiesVSSinks, sources.size(), sinks.size());
			
			
			// export to csv file
			createCSVFileFrom2DArray(sourcesVSSinks, sources, sinks, "sourcesVSsinks");
			createCSVFileFrom2DArray(capabilitiesVSSinks, capabilities, sinks, "capabilitiesVSsinks");

		}
		
		
		
		// checkers
		
		private static boolean hasSource(HashMap<String,ArrayList<String>> app, String source) {
			ArrayList<String> sources = app.get("sources");
			return sources.contains(source);
		}
		
		private static boolean hasSink(HashMap<String,ArrayList<String>> app, String sink) {
			ArrayList<String> sinks = app.get("sinks");
			return sinks.contains(sink);
		}
		
		private static boolean hasAction(HashMap<String,ArrayList<String>> app, String action) {
			ArrayList<String> actions = app.get("actions");
			return actions.contains(action);
		}
		
		private static boolean hasSubscription(HashMap<String,ArrayList<String>> app, String subscription) {
			ArrayList<String> subscriptions = app.get("subscriptions");
			return subscriptions.contains(subscription);
		}
		
		private static boolean hasCapability(HashMap<String,ArrayList<String>> app, String capability) {
			ArrayList<String> capabilities = app.get("capabilities");
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
				ArrayList<String> appSubscriptions = map.get("subscriptions");
				ArrayList<String> appSources = map.get("sources");
				ArrayList<String> appSinks = map.get("sinks");
				ArrayList<String> appActions = map.get("actions");
//				System.out.println("Name: " + appName.toString());
//				System.out.println("Description: " + appDescription.toString());
//				System.out.println("Category: " + appCategory.toString());
//				System.out.println("Capabilities: " + appCapabilities.toString());
//				System.out.println("Subscriptions: " + appSubscriptions.toString());
//				System.out.println("Sensitive sources: " + appSources.toString());
//				System.out.println("Sensitive sinks: " + appSensSinks.toString());
//				System.out.println("Sensitive actions: " + appSensActions.toString());
//				System.out.println("HHHHHHHHHHHH");
				
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
				
				JSONArray subArray = new JSONArray();
				for (String sub : appSubscriptions) {
					subArray.add(sub);
				}
				appObj.put("subscriptions", subArray);
				
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
				for (String action : appActions) {
					actionsArray.add(action);
				}
				appObj.put("actions", actionsArray);
				
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
				
				//now fill in the rows with a row name in a first cell
				for (int i=0; i< rowsArray.size(); i++) {
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
		
		
		
		
		
}
