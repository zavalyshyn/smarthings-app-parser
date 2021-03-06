package igorparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.control.SourceUnit;

import edu.ksu.cis.bandera.jjjc.gparser.smartapppreprocessor.GDeviceInputInfo;
import edu.ksu.cis.bandera.jjjc.gparser.smartapppreprocessor.GInputInfo;
import edu.ksu.cis.bandera.jjjc.gparser.smartapppreprocessor.GOtherInputInfo;
import edu.ksu.cis.bandera.jjjc.gparser.smartapppreprocessor.GSubscriptionInfo;
import edu.ksu.cis.bandera.jjjc.gparser.util.GUtil;

/* This class is used to get all of the input info in a block
 * of source code: monitor device, control device, number, date,
 * time, text, ... 
 * */
public class IgorGInputInfoGetter extends ClassCodeVisitorSupport {
	
	private AppDatabase appDB;
	private SmartThingsApiReference apiRef;
	
	private List<String> normalSources = new ArrayList<String>();
	private List<String> sensitiveSources = new ArrayList<String>();
	private List<String> sensitiveCapabilities = new ArrayList<String>();
	private List<String> sensitiveSubscriptions = new ArrayList<String>();
	private List<String> sensitiveActions = new ArrayList<String>();
	private List<String> sensitiveSinks = new ArrayList<String>();
	private List<String> deviceEvents = new ArrayList<String>();
	
	public IgorGInputInfoGetter(AppDatabase database, SmartThingsApiReference smartthingsApiRef)	{
		
		this.appDB = database;
		this.apiRef = smartthingsApiRef;
		
		// initialize all the sensitive sources and sinks
		
		
		// freely available info
		sensitiveSources.add("location");
		sensitiveSources.add("mode");
		// hub information
		sensitiveSources.add("firmwareVersionString");
		sensitiveSources.add("localIP");
		sensitiveSources.add("hubId");
		
		// capabilities
		// sensitiveCapabilities.add("capability.switch");	// can be any device, including IP camera or Alarm
		sensitiveCapabilities.add("capability.alarm");	// commands: off, siren, strobe, both
		sensitiveCapabilities.add("capability.doorControl");
		sensitiveCapabilities.add("capability.estimatedTimeOfArrival");
		sensitiveCapabilities.add("capability.garageDoorControl");
		sensitiveCapabilities.add("capability.geolocation");
		sensitiveCapabilities.add("capability.imageCapture");
		sensitiveCapabilities.add("capability.lockOnly");
		sensitiveCapabilities.add("capability.lock");
		sensitiveCapabilities.add("capability.lockCodes");
		sensitiveCapabilities.add("capability.motionSensor");
		sensitiveCapabilities.add("capability.presenceSensor");
		sensitiveCapabilities.add("capability.smokeDetector");
		sensitiveCapabilities.add("capability.soundSensor");
		sensitiveCapabilities.add("capability.speechRecognition");
		sensitiveCapabilities.add("capability.videoClips");
		sensitiveCapabilities.add("capability.videoStream");
		sensitiveCapabilities.add("capability.sleepSensor");
		
		// events subscriptions
		sensitiveSubscriptions.add("mode");
		sensitiveSubscriptions.add("location");
		sensitiveSubscriptions.add("alarm");
		sensitiveSubscriptions.add("alarm.off");
		sensitiveSubscriptions.add("eta");
		sensitiveSubscriptions.add("door");
		sensitiveSubscriptions.add("door.open");
		sensitiveSubscriptions.add("door.closed");
		sensitiveSubscriptions.add("door.unknown");
		sensitiveSubscriptions.add("door.closing");
		sensitiveSubscriptions.add("door.opening");
		sensitiveSubscriptions.add("latitude");
		sensitiveSubscriptions.add("longitude");
		sensitiveSubscriptions.add("lock");
		sensitiveSubscriptions.add("lock.locked");
		sensitiveSubscriptions.add("lock.unknown");
		sensitiveSubscriptions.add("lock.unlocked");
		sensitiveSubscriptions.add("lock.unlocked with timeout");
		sensitiveSubscriptions.add("motion");
		sensitiveSubscriptions.add("motion.inactive");
		sensitiveSubscriptions.add("motion.active");
		sensitiveSubscriptions.add("presence");
		sensitiveSubscriptions.add("presence.present");
		sensitiveSubscriptions.add("presence.not present");
		sensitiveSubscriptions.add("sound.detected");
		sensitiveSubscriptions.add("sound.not detected");
		sensitiveSubscriptions.add("phraseSpoken");
		sensitiveSubscriptions.add("image");	// from camera
		sensitiveSubscriptions.add("videoClip");
		sensitiveSubscriptions.add("stream");	// video
		sensitiveSubscriptions.add("codeReport");	//lock codes
		sensitiveSubscriptions.add("codeChanged");
		sensitiveSubscriptions.add("usercode");	// new code for the door lock !!
		sensitiveSubscriptions.add("sleeping");	// status event generated by fitness tracker when the user sleeps
		sensitiveSubscriptions.add("sleeping.sleeping");
		sensitiveSubscriptions.add("sleeping.not sleeping");
		
		
		// sensitive actions the apps might perform
		sensitiveActions.add("getLocation");	// the Location into which this SmartApp has been installed.
		sensitiveActions.add("sendEvent");	// if an app tries to send event as if it is coming from a device
		sensitiveActions.add("sendLocationEvent");	// Sends a LOCATION Event, e.g. "sunrise" "sunset" 
		sensitiveActions.add("setLocationMode");	// an app can change location mode into any, e.g. home instead of away
		sensitiveActions.add("setMode");	// Set the mode for the Location
		sensitiveActions.add("take");	// for imagecapture from cameras	 // too coarse, causes false-positives
		sensitiveActions.add("getLocalIP");
		sensitiveActions.add("textToSpeech");
		sensitiveActions.add("sendHubCommand");	// can be used to scan for or send commands to other devices in the network
		sensitiveActions.add("unlock");	// unlocks the door
		sensitiveActions.add("open");
		sensitiveActions.add("updateCodes");	//for door lock
		sensitiveActions.add("setCode");
		sensitiveActions.add("deleteCode");
		sensitiveActions.add("requestCode");
		sensitiveActions.add("captureClip");
		sensitiveActions.add("startStream");
		sensitiveActions.add("stopStream");
		
		// sensitive sinks the app might use to exfiltrate the data
		sensitiveSinks.add("sendSms");
		sensitiveSinks.add("sendSmsMessage");
		// sensitiveSinks.add("sendNotificationToContacts");
		// sensitiveSinks.add("sendPush");
		sensitiveSinks.add("mappings");
		sensitiveSinks.add("httpGet");
		sensitiveSinks.add("httpDelete");
		sensitiveSinks.add("httpHead");
		sensitiveSinks.add("httpPost");
		sensitiveSinks.add("httpPostJson");
		sensitiveSinks.add("httpPutJson");
		sensitiveSinks.add("asynchttp_v1");
		
	}
	
	@Override
	protected SourceUnit getSourceUnit() {
		return null;
	}
	
	@Override
	public void visitMapExpression(MapExpression expression) {
		for (MapEntryExpression mapEntryExpr : expression.getMapEntryExpressions()) {
			String entryKey = mapEntryExpr.getKeyExpression().getText();
			String entryValue = mapEntryExpr.getValueExpression().getText();
			if (entryKey.equals("type")) {
				if (entryValue.contains("capability.")) {
					// String[] capability = entryValue.split("\\.");
					// System.out.println("IGORDEBUG, capability, " + entryValue);
					// appDB.setAppCapability(capability[1]);
					appDB.setAppCapability(entryValue);
					if (sensitiveCapabilities.contains(entryValue)) {
						appDB.setSensitiveCapability(entryValue);
					}
				}
				else if (entryValue.equals("mode") || entryValue.equals("phone")) {
					//System.out.println("IGORDEBUG, sensitiveinfo, " + entryValue);
					appDB.setSensitiveSource(entryValue);
				}
				else if (sensitiveSources.contains(entryValue)) {
					appDB.setSensitiveSource(entryValue);
				}
			}
		}
	}
	
	@Override
	public void visitMethodCallExpression(MethodCallExpression mce)	{
		
		String methText;
		
		Expression methMethod = mce.getMethod();
		
		
		// System.out.println("CHECK!: Parsing a section: " + methMethod.getText());

		// ################################
		// #### Igor's App Parser Code ####
		// ################################
		
		
		Expression myMethod = mce.getMethod();
		Expression myArgs = mce.getArguments();
		
		String methodText = methMethod.getText();
		
		ArrayList<String> inferredActions = appDB.getInferredActionsForCurrentApp();
		
		
		// processing "definition" block
		if (methodText.equals("definition")) {
			Expression exp = ((TupleExpression)myArgs).getExpression(0);
//			List<Expression> exprList = GUtil.buildExprList(myArgs);
//			System.out.println("Here are my exprList: " + exprList);
			if (exp instanceof NamedArgumentListExpression) {
				for (MapEntryExpression mapEntryExpr : ((NamedArgumentListExpression) exp).getMapEntryExpressions()) {
					if (mapEntryExpr.getKeyExpression().getText().equals("name")) {
						// System.out.println("IGORDEBUG, name, " + mapEntryExpr.getValueExpression().getText());
						appDB.setAppName(mapEntryExpr.getValueExpression().getText());
						//appList.add(mapEntryExpr.getValueExpression().getText());
					}
					if (mapEntryExpr.getKeyExpression().getText().equals("description")) {
						// System.out.println("IGORDEBUG, description, " + mapEntryExpr.getValueExpression().getText());
						appDB.setAppDescription(mapEntryExpr.getValueExpression().getText());
					}
					if (mapEntryExpr.getKeyExpression().getText().equals("category")) {
						// System.out.println("IGORDEBUG, category, " + mapEntryExpr.getValueExpression().getText());
						appDB.setAppCategory(mapEntryExpr.getValueExpression().getText());
					}
				}
			}
			else if (exp instanceof MapExpression) {
				for (MapEntryExpression  meExpr: ((MapExpression)exp).getMapEntryExpressions()) {
					String exprKeyText = meExpr.getKeyExpression().getText();
					String exprValueText = meExpr.getValueExpression().getText();
					if (exprKeyText.equals("name")) {
						// System.out.println("IGORDEBUG, name, " + exprValueText);
						//appList.add(exprValueText);
						appDB.setAppName(exprValueText);
					}
					if (exprKeyText.equals("description")) {
						// System.out.println("IGORDEBUG, description, " + exprValueText);
						appDB.setAppDescription(exprValueText);
					}
					if (exprKeyText.equals("name")) {
						// System.out.println("IGORDEBUG, category, " + exprValueText);
						appDB.setAppCategory(exprValueText);
					}
				}
			}
		}
		
		// processing "preferences" block
		else if  (methodText.equals("preferences")) {
//			System.out.println("These are my args: " + myArgs);
//			System.out.println("Type of myArgs: " + myArgs.getClass());
			if (myArgs instanceof ArgumentListExpression) {
				for (Expression expr : ((ArgumentListExpression) myArgs).getExpressions()) {
					if (expr instanceof ClosureExpression) {
						parseClosureExpression(expr);
					}
				}
			}
		}
		
		// check for dynamic pages that act as preferences
		else if (methodText.equals("dynamicPage")) {
			// System.out.println("We have a dynamic page");
			// System.out.println("here it is: " + myArgs);
			parseDynamicPage(myArgs);
		}
		
		// check if an app tries to perform a sensitive action
		else if (sensitiveActions.contains(methodText)) {
			// System.out.println("IGORDEBUG, sensitiveaction, " + methodText);
			appDB.setSensitiveAction(methodText);
			appDB.setActualAction(methodText);
		}
		
		
		else if (inferredActions.contains(methodText)) {
			appDB.setActualAction(methodText);
		}
		
		// check if an app declared any of the sensitive sinks
		else if (sensitiveSinks.contains(methodText)) {
			// System.out.println("IGORDEBUG, sensitivesink, " + methodText);
			appDB.setSink(methodText);
		}
		
		else if (methodText.equals("subscribe"))  {
			// System.out.println("FOUND SUBSCRIBE: " + myArgs);
			List<Expression> expLst = ((ArgumentListExpression)myArgs).getExpressions();
			for (Expression exp : expLst) {
				if (exp instanceof ConstantExpression) {
					if (sensitiveSources.contains(exp.getText())) {
						// System.out.println("IGORDEBUG, sensitiveinfo, " + exp.getText());
						appDB.setSensitiveSource(exp.getText());
						// System.out.println("IGORDEBUG, subscriptions, " + exp.getText());
						appDB.setAppActualSubscription(exp.getText());
					}
					if (sensitiveSubscriptions.contains(exp.getText())) {
						appDB.setSensitiveSubscription(exp.getText());
						appDB.setAppActualSubscription(exp.getText());
					}
					ArrayList<String> inferredSubscriptions = appDB.getInferredSubscriptionsForCurrentApp();
					if (inferredSubscriptions.contains(exp.getText())) {
						appDB.setAppActualSubscription(exp.getText());
					}
					 else {
						 // System.out.println("IGORDEBUG, subscriptions, " + exp.getText());
						 appDB.setAppActualSubscription(exp.getText());
					}
				}
				else if (exp instanceof VariableExpression) {
					if (sensitiveSources.contains(exp.getText())) {
						// System.out.println("IGORDEBUG, sensitiveinfo, " + exp.getText());
						appDB.setSensitiveSource(exp.getText());
						// System.out.println("IGORDEBUG, subscriptions, " + exp.getText());
						appDB.setAppActualSubscription(exp.getText());
					} 
					if (sensitiveSubscriptions.contains(exp.getText())) {
						appDB.setSensitiveSubscription(exp.getText());
						appDB.setAppActualSubscription(exp.getText());
					}
					ArrayList<String> inferredSubscriptions = appDB.getInferredSubscriptionsForCurrentApp();
					if (inferredSubscriptions.contains(exp.getText())) {
						appDB.setAppActualSubscription(exp.getText());
					}
					else {
						// System.out.println("IGORDEBUG, subscriptions, " + exp.getText());
						 appDB.setAppActualSubscription(exp.getText());
					}
				}
			}
		}
		
		// check if we have a call by reflection attempt
		else if (methodText.contains("$")) {
			// System.out.println("IGORDEBUG, CallByReflection Detected, " + methodText);
			appDB.setActualAction("callbyreflection");
		}
		

		
		// check for missed sections with possible capabilities
		else if (methodText.equals("section") && myArgs.toString().contains("capability."))  {
			if (myArgs instanceof ArgumentListExpression) {
				List<Expression> exprList = ((ArgumentListExpression)myArgs).getExpressions();
				for (Expression exp : exprList) {
					if (exp instanceof ClosureExpression) {
						parseClosureExpression(exp);
					}
				}
			}
		}
		
		else if (myArgs instanceof ArgumentListExpression) {
			List<Expression> lstexp = ((ArgumentListExpression)myArgs).getExpressions();
			for (Expression exp: lstexp) {
				if (exp instanceof ClosureExpression) {
					parseClosureExpression(exp);
				}
			}
		}
		
		// check if we still missed some capabilities after all the previous checks
		else if (myArgs.toString().contains("capability.")) {
			 // System.out.println("### ERROR!!!!! ###: We might have skipped some capabilities!!!");
			 // System.out.println(myArgs);
		}
		
		
		// #################################
		// ############# end ###############
		// #################################
		
	}
	
	
	
	
	//helper methods
	public void parseClosureExpression(Expression closureExpr) {
		if (closureExpr instanceof ClosureExpression) {
			Statement blockStatement = ((ClosureExpression) closureExpr).getCode();
			if (blockStatement instanceof BlockStatement) {
				List<Statement> stmtList = ((BlockStatement) blockStatement).getStatements();
				for (Statement stmt : stmtList) {	// we could just grab the first element by calling stmtList.get(0)
					if (stmt instanceof ExpressionStatement) {
						Expression methodCallExpr = ((ExpressionStatement) stmt).getExpression();
						if (methodCallExpr instanceof MethodCallExpression) {
							Expression argListExpr = ((MethodCallExpression) methodCallExpr).getArguments();
							if (argListExpr instanceof ArgumentListExpression) {
								for (Expression arglstExpr : ((ArgumentListExpression)argListExpr).getExpressions()) {
//									Expression secClosureExpr = ((ArgumentListExpression)argListExpr).getExpression(1);
									if (arglstExpr instanceof ClosureExpression) {
										Statement blockStmt = ((ClosureExpression)arglstExpr).getCode();
										for (Statement statem : ((BlockStatement)blockStmt).getStatements()) {
											if (statem instanceof ExpressionStatement) {
												Expression inputExpr = ((ExpressionStatement)statem).getExpression();
												if (inputExpr instanceof MethodCallExpression) {
													String inputExprText = ((MethodCallExpression)inputExpr).getMethod().getText();
													if (inputExprText.equals("input")) {
														Expression argument = ((MethodCallExpression)inputExpr).getArguments();
															if (argument instanceof ArgumentListExpression) {
																for (Expression argExpression : ((ArgumentListExpression)argument).getExpressions()) {
																	String argumentText = argExpression.getText();
																	if (argumentText.contains("capability.")) {
																		// String[] capability = argumentText.split("\\.");
																		// System.out.println("IGORDEBUG, capability, " + argumentText);
																		// appDB.setAppCapability(capability[1]);
																		appDB.setAppCapability(argumentText);
																		if (sensitiveCapabilities.contains(argumentText)) {
																			// System.out.println("IGORDEBUG, sensitiveinfo, " + argumentText);
																			appDB.setSensitiveCapability(argumentText);
																		}
																	} else if (argumentText.equals("mode")) {
																		// System.out.println("IGORDEBUG, sensitiveinfo, mode");
																		appDB.setSensitiveSource("mode");
																	} else if (argumentText.equals("phone")) {
																		// System.out.println("IGORDEBUG, sensitiveinfo, phone");
																		appDB.setSensitiveSource("phone");
																	} else if (sensitiveSources.contains(argumentText)) {
																		// System.out.println("IGORDEBUG, sensitiveinfo, " + argumentText);
																		appDB.setSensitiveSource(argumentText);
																	}
																	
																	if (argExpression instanceof ClosureExpression) {
																		parseClosureExpression(argExpression);
																	}
																}
															}
															else if (argument instanceof TupleExpression) {
																List<Expression> nALE = ((TupleExpression)argument).getExpressions();
																for (Expression mEExp : nALE) {
																	if (mEExp instanceof NamedArgumentListExpression) {
																		for (Expression e : ((NamedArgumentListExpression)mEExp).getMapEntryExpressions()) {
																			if (e instanceof MapEntryExpression) {
																				String eKey = ((MapEntryExpression)e).getKeyExpression().getText();
																				String eValue = ((MapEntryExpression)e).getValueExpression().getText();
																				if (eKey.equals("type")) {
																					if (eValue.contains("capability.")) {
																						// String[] capability = eValue.split("\\.");
																						// System.out.println("IGORDEBUG, capability, " + eValue);
																						// appDB.setAppCapability(capability[1]);
																						appDB.setAppCapability(eValue);
																						if (sensitiveCapabilities.contains(eValue)) {
																							// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
																							appDB.setSensitiveCapability(eValue);
																						}
																					}
																					else if (eValue.equals("mode") || eValue.equals("phone")) {
																						// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
																						appDB.setSensitiveSource(eValue);
																					}
																					else if (sensitiveSources.contains(eValue)) {
																						// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
																						appDB.setSensitiveSource(eValue);
																					}
																				}
																			}
																		}
																	}
																}
															}
													} 
													else if (inputExprText.equals("section")) {
														Expression args = ((MethodCallExpression)inputExpr).getArguments();
														if (args instanceof ArgumentListExpression) {
															for (Expression argExpr : ((ArgumentListExpression)args).getExpressions()) {
																if (argExpr instanceof ClosureExpression) {
//																	System.out.println("Found a section!!!!!!!!!");
//																	System.out.println("Here: " + argExpr);
																	findCapabilityInDynamicPage(argExpr);
																}
															}
														}
													}
													else {
														// System.out.println("### ATTENTION!!!!! ###: There is something weird instead of regular input section!");
														// System.out.println("### ATTENTION!!!!! ###: The method text is: " + inputExprText);
														// System.out.println("### ATTENTION!!!!! ###: Here it is: " + ((MethodCallExpression)inputExpr));
													}
												}
											}
											
										}
									}
									else if (arglstExpr instanceof ConstantExpression) {
										String exprText = arglstExpr.getText();
										if (exprText.contains("capability.")) {
											// String[] capability = exprText.split("\\.");
											// System.out.println("IGORDEBUG, capability, " + exprText);
											// appDB.setAppCapability(capability[1]);
											appDB.setAppCapability(exprText);
											if (sensitiveCapabilities.contains(exprText)) {
												// System.out.println("IGORDEBUG, sensitiveinfo, " + exprText);
												appDB.setSensitiveCapability(exprText);
											}
										} else if (exprText.equals("mode")) {
											// System.out.println("IGORDEBUG, sensitiveinfo, mode");
											appDB.setSensitiveSource("mode");
										} else if (exprText.equals("phone")) {
											// System.out.println("IGORDEBUG, sensitiveinfo, phone");
											appDB.setSensitiveSource("phone");
										} else if (sensitiveSources.contains(exprText)) {
											// System.out.println("IGORDEBUG, sensitiveinfo, " + exprText);
											appDB.setSensitiveSource(exprText);
										}
									}
								}
							}
							// check for capabilities in a non-standard preferences section
							else if (argListExpr instanceof TupleExpression) {
								List<Expression> nALE = ((TupleExpression)argListExpr).getExpressions();
								for (Expression mEExp : nALE) {
									if (mEExp instanceof NamedArgumentListExpression) {
										for (Expression e : ((NamedArgumentListExpression)mEExp).getMapEntryExpressions()) {
											if (e instanceof MapEntryExpression) {
												String eKey = ((MapEntryExpression)e).getKeyExpression().getText();
												String eValue = ((MapEntryExpression)e).getValueExpression().getText();
												if (eKey.equals("type")) {
													if (eValue.contains("capability.")) {
														// String[] capability = eValue.split("\\.");
														// System.out.println("IGORDEBUG, capability, " + eValue);
														// appDB.setAppCapability(capability[1]);
														appDB.setAppCapability(eValue);
														if (sensitiveCapabilities.contains(eValue)) {
															// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
															appDB.setSensitiveCapability(eValue);
														}
														
													}
													else if (eValue.equals("mode") || eValue.equals("phone")) {
														// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
														appDB.setSensitiveSource(eValue);
													} 
													else if (sensitiveSources.contains(eValue)) {
														// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
														appDB.setSensitiveSource(eValue);
													}
												}
											}
										}
									}
								}
							}
							
							else {
								// System.out.println("### ERROR!!!!! ###: We have a non-standard preferences section");
								// System.out.println("### ERROR!!!!! ### Here is it: " + argListExpr.getText());
							}
						}
						
					}
				}
			}
		}
		else {
			// System.out.println("### ERROR!!!!! ###: Exprected ClosureExpression, but received this: " + closureExpr);
		}
	}
	
	public void parseDynamicPage(Expression inExpr) {
		if (inExpr instanceof ArgumentListExpression) {
			List<Expression> closExprList = ((ArgumentListExpression)inExpr).getExpressions();
			for (Expression expr : closExprList) {
				if (expr instanceof ClosureExpression) {
					Statement blStmt = ((ClosureExpression)expr).getCode();
					if (blStmt instanceof BlockStatement) {
						List<Statement> exprStmtList = ((BlockStatement)blStmt).getStatements();
						for (Statement exprStmt : exprStmtList) {
							if (exprStmt instanceof ExpressionStatement) {
								Expression methCallExpr = ((ExpressionStatement)exprStmt).getExpression();
								if (methCallExpr instanceof MethodCallExpression) {
									String name = ((MethodCallExpression) methCallExpr).getMethod().getText();
									if (name.equals("section")) {
										Expression args = ((MethodCallExpression)methCallExpr).getArguments();
										if (args instanceof ArgumentListExpression) {
											for (Expression argExpr : ((ArgumentListExpression)args).getExpressions()) {
												if (argExpr instanceof ClosureExpression) {
//													System.out.println("Found a section!!!!!!!!!");
//													System.out.println("Here: " + argExpr);
													findCapabilityInDynamicPage(argExpr);
												}
											}
										}
									}
								}
							}
							else if (exprStmt instanceof IfStatement) {
								Statement ifstmt = ((IfStatement)exprStmt).getIfBlock();
								// TODO: continue here and think about else block as well
								if (ifstmt instanceof BlockStatement) {
									List<Statement> stmtList = ((BlockStatement) ifstmt).getStatements();
									for (Statement stmt : stmtList) {
										if (stmt instanceof ExpressionStatement) {
											Expression methodCallExpr = ((ExpressionStatement) stmt).getExpression();
											if (methodCallExpr instanceof MethodCallExpression) {
												Expression argListExpr = ((MethodCallExpression) methodCallExpr).getArguments();
												if (argListExpr instanceof ArgumentListExpression) {
													for (Expression arglstExpr : ((ArgumentListExpression)argListExpr).getExpressions()) {
														if (arglstExpr instanceof ClosureExpression) {
															parseClosureExpression(arglstExpr);
														}
													}
												}
											}
										}
									}
										
								}
							}
							else if (exprStmt instanceof ForStatement) {
								Statement forstmt = ((ForStatement)exprStmt).getLoopBlock();
								if (forstmt instanceof BlockStatement) {
									List<Statement> stmtList = ((BlockStatement) forstmt).getStatements();
									for (Statement stmt : stmtList) {
										if (stmt instanceof ExpressionStatement) {
											Expression methodCallExpr = ((ExpressionStatement) stmt).getExpression();
											if (methodCallExpr instanceof MethodCallExpression) {
												Expression argListExpr = ((MethodCallExpression) methodCallExpr).getArguments();
												if (argListExpr instanceof ArgumentListExpression) {
													for (Expression arglstExpr : ((ArgumentListExpression)argListExpr).getExpressions()) {
														if (arglstExpr instanceof ClosureExpression) {
															parseClosureExpression(arglstExpr);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void findCapabilityInDynamicPage(Expression inExpr) {
		Statement blockStatement = ((ClosureExpression) inExpr).getCode();
//		System.out.println("Here is a statement list: " + statement);
		List<Statement> stmtList = ((BlockStatement) blockStatement).getStatements();
		for (Statement stmt : stmtList) {	// we could just grab the first element by calling stmtList.get(0)
			if (stmt instanceof ExpressionStatement) {
				Expression methodCallExpr = ((ExpressionStatement) stmt).getExpression();
				if (methodCallExpr instanceof MethodCallExpression) {
					String methodName = ((MethodCallExpression) methodCallExpr).getMethod().getText();
					if (methodName.equals("ifUnset") || methodName.equals("ifSet") || methodName.equals("input")) {
						Expression argListExpr = ((MethodCallExpression) methodCallExpr).getArguments();
						if (argListExpr instanceof ArgumentListExpression) {
							for (Expression argExpr : ((ArgumentListExpression)argListExpr).getExpressions()) {
								if (argExpr instanceof ConstantExpression) {
									String argumentText = argExpr.getText();
									if (argumentText.contains("capability.")) {
										// String[] capability = argumentText.split("\\.");
										// System.out.println("IGORDEBUG, capability, " + argumentText);
										// appDB.setAppCapability(capability[1]);
										appDB.setAppCapability(argumentText);
										if (sensitiveCapabilities.contains(argumentText)) {
											// System.out.println("IGORDEBUG, sensitiveinfo, " + argumentText);
											appDB.setSensitiveCapability(argumentText);
										}
									} else if (argumentText.equals("mode")) {
										// System.out.println("IGORDEBUG, sensitiveinfo, mode");
										appDB.setSensitiveSource("mode");
									} else if (argumentText.equals("phone")) {
										// System.out.println("IGORDEBUG, sensitiveinfo, phone");
										appDB.setSensitiveSource("phone");
									} else if (sensitiveSources.contains(argumentText)) {
										// System.out.println("IGORDEBUG, sensitiveinfo, " + argumentText);
										appDB.setSensitiveSource(argumentText);
									}
									
									if (argExpr instanceof ClosureExpression) {
										parseClosureExpression(argExpr);
									}
									
								}
							}
						}
						else if (argListExpr instanceof TupleExpression) {
							List<Expression> tupleExprList = ((TupleExpression)argListExpr).getExpressions();
							for (Expression tupleExpr : tupleExprList) {
								if (tupleExpr instanceof NamedArgumentListExpression) {
									List<MapEntryExpression> mapEntryExprList = ((NamedArgumentListExpression)tupleExpr).getMapEntryExpressions();
									for (Expression mapEntry : mapEntryExprList) {
										if (mapEntry instanceof MapEntryExpression) {
											String expKey = ((MapEntryExpression)mapEntry).getKeyExpression().getText();
											String expValue = ((MapEntryExpression)mapEntry).getValueExpression().getText();
											if (expKey.equals("type") && expValue.contains("capability.")) {
												// String[] capability = expValue.split("\\.");
												// System.out.println("IGORDEBUG, capability, " + expValue);
												// appDB.setAppCapability(capability[1]);
												appDB.setAppCapability(expValue);
												if (sensitiveCapabilities.contains(expValue)) {
													// System.out.println("IGORDEBUG, sensitiveinfo, " + expValue);
													appDB.setSensitiveCapability(expValue);
												}
											} 
											else if (sensitiveSources.contains(expValue)) {
												// System.out.println("IGORDEBUG, sensitiveinfo, " + expValue);
												appDB.setSensitiveSource(expValue);
											}
										}
									}
								}
							}
						}
					} else {
						// System.out.println("### ATTENTION!!!!! ###: We have something else instead of an input in the dynamic page");
						// System.out.println("### ATTENTION!!!!! ### Here it is: " + methodCallExpr.getText());
					}
				}
				else if (methodCallExpr instanceof DeclarationExpression) {
					// skip declaration
				}
				else {
					// System.out.println("### ERROR!!!!! ### Expected MethodCallExpression but received this: " + methodCallExpr.toString());
				}
			}
			else if (stmt instanceof IfStatement) {
				Statement blkstmt = ((IfStatement)stmt).getIfBlock();
				if (blkstmt instanceof BlockStatement) {
					List<Statement> stmtlist = ((BlockStatement)blkstmt).getStatements();
					for (Statement st : stmtlist) {
						if (st instanceof ExpressionStatement) {
							Expression exp = ((ExpressionStatement)st).getExpression();
							if (exp instanceof MethodCallExpression) {
								Expression args = ((MethodCallExpression)exp).getArguments();
								if (args instanceof ArgumentListExpression) {
									for (Expression argExpr : ((ArgumentListExpression)args).getExpressions()) {
										if (argExpr instanceof ConstantExpression) {
											String argumentText = argExpr.getText();
											if (argumentText.contains("capability.")) {
												// String[] capability = argumentText.split("\\.");
												// System.out.println("IGORDEBUG, capability, " + argumentText);
												// appDB.setAppCapability(capability[1]);
												appDB.setAppCapability(argumentText);
												if (sensitiveCapabilities.contains(argumentText)) {
													// System.out.println("IGORDEBUG, sensitiveinfo, " + argumentText);
													appDB.setSensitiveCapability(argumentText);
												}
											} else if (argumentText.equals("mode")) {
												// System.out.println("IGORDEBUG, sensitiveinfo, mode");
												appDB.setSensitiveSource("mode");
											} else if (argumentText.equals("phone")) {
												// System.out.println("IGORDEBUG, sensitiveinfo, phone");
												appDB.setSensitiveSource("phone");
											} else if (sensitiveSources.contains(argumentText)) {
												// System.out.println("IGORDEBUG, sensitiveinfo, " + argumentText);
												appDB.setSensitiveSource(argumentText);
											}
											
											if (argExpr instanceof ClosureExpression) {
												parseClosureExpression(argExpr);
											}
											
										}
										else if (argExpr instanceof MapExpression) {
											List<MapEntryExpression> lste = ((MapExpression)argExpr).getMapEntryExpressions();
											for (MapEntryExpression me : lste) {
												if (me instanceof MapEntryExpression) {
													String eKey = ((MapEntryExpression)me).getKeyExpression().getText();
													String eValue = ((MapEntryExpression)me).getValueExpression().getText();
													if (eKey.equals("type")) {
														if (eValue.contains("capability.")) {
															// String[] capability = eValue.split("\\.");
															// System.out.println("IGORDEBUG, capability, " + eValue);
															// appDB.setAppCapability(capability[1]);
															appDB.setAppCapability(eValue);
															if (sensitiveCapabilities.contains(eValue)) {
																// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
																appDB.setSensitiveCapability(eValue);
															}
														}
														else if (eValue.equals("mode") || eValue.equals("phone")) {
															// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
															appDB.setSensitiveSource(eValue);
														} 
														else if (sensitiveSources.contains(eValue)) {
															// System.out.println("IGORDEBUG, sensitiveinfo, " + eValue);
															appDB.setSensitiveSource(eValue);
														}
													}
												}
											}
	
										}
									}
								}
							}
						}
					}
					
				}
				
			}
			else {
				// System.out.println("### ERROR!!!!! ### Exprected ExpressionStatement but received this: " + stmt);
			}
		}
	}
}





