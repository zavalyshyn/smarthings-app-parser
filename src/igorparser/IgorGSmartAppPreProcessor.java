package igorparser;

import java.util.ArrayList;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

public class IgorGSmartAppPreProcessor extends CompilationCustomizer {

	private AppDatabase appDB;
	private SmartThingsApiReference apiRef;

	public IgorGSmartAppPreProcessor(AppDatabase database, SmartThingsApiReference smartthingsAPIRef) {
		super(CompilePhase.CONVERSION);
		this.appDB = database;
		this.apiRef = smartthingsAPIRef;
	}
	
	
	@Override
	public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
		
		IgorGInputInfoGetter gIIH;
		
		/* Get all input info */
		// IgorGExpressionInfoGetter gMCV = new IgorGExpressionInfoGetter();
		
//			classNode.visitContents(gMCV);
		gIIH = new IgorGInputInfoGetter(appDB,apiRef);
		classNode.visitContents(gIIH);
		// System.out.println("######################################################");
		// System.out.println("######################################################");
		// System.out.println("######################################################");
		
	}
	
}
