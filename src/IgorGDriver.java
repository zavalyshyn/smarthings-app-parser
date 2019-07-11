package igorparser;

public class IgorGDriver {
	
	private String classPath;

	/**
	 * Create a new IgorGDriver.
	 */
	public IgorGDriver() {
	}
	
	public void init(String project_root) {
		this.classPath = project_root;
		IgorGPotentialRiskScreener.init(project_root);
	}

	/**
	 *
	 */
	public void run() {
		System.out.println("Running ...");

		try {
			IgorGPotentialRiskScreener.extractInputInfo();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("Finished running.");
	}
	

}


