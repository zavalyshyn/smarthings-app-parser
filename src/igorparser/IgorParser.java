package igorparser;
import java.util.Arrays;

import igorparser.IgorGDriver;

public class IgorParser {

	public static void main(String[] args) {
		String project_root = System.getProperty("user.dir");
		IgorGDriver driver = new IgorGDriver();
		
		/* Do system initializations */
		driver.init(project_root);
		
		/* Translate SmartApps' Groovy source code into Promela code */
		driver.run();
	}
}
