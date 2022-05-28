package com.github.mapresultset;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"com.github.mapresultset.Table", "com.github.mapresultset.Query"})
public class MappingProcessor extends AbstractProcessor {

	private Set<Element> annotatedElements = new HashSet<>();

	@Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init( processingEnv );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		if ( roundEnvironment.processingOver() ) {
			System.out.println("Last round! Fight!");
			// TODO do I need to do anything here?!
		} else {
			processAnnotations(annotations, roundEnvironment);
		}

		// https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/AbstractProcessor.html#process-java.util.Set-javax.annotation.processing.RoundEnvironment-
        return true;
    }

    private void processAnnotations(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		for ( TypeElement annotation : annotations ) {
			System.out.println("Annotation: " + annotation);
			Set<? extends Element> annotatedElements
					= roundEnvironment.getElementsAnnotatedWith(annotation);
			System.out.println("Annotated Elements: " + annotatedElements);
			this.annotatedElements.addAll(annotatedElements);
			for (var e : annotatedElements) {
				System.out.println("Element: " + e + " and it's type is " + e.getKind());
				System.out.println("AnnotationMirrors: " + e.getAnnotationMirrors());
				for (var am : e.getAnnotationMirrors()) {
					System.out.println(am.getElementValues());
				}
				Element enclosingElement = e.getEnclosingElement();
				String name = e.getSimpleName().toString();
				System.out.println("element name: " + name);
				System.out.println("element enclosingElement: " + enclosingElement);
				System.out.println("element enclosedElements: " + e.getEnclosedElements());

				for (var enclosed : e.getEnclosedElements()) {
					System.out.println(enclosed + " type is " + enclosed.getKind());
				}


				if (e instanceof VariableElement ve) {
					String query = (String) ve.getConstantValue();
					if (query == null) {
						throw new RuntimeException("Variable annotated with @Query must be final and not null");
					}
					System.out.println(query);
					// TODO save query in a list then generate the code
					//   that does the mapping ...
				}

				// Create map file
				try {
					writeBuilderFile(e.toString() + "MapResultSet", Map.of("setId", "int"));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void writeBuilderFile(String className, Map<String, String> setterMap) 
			throws IOException {

		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
				"Creating map file ... className = " + className);
	    String packageName = null;
	    int lastDot = className.lastIndexOf('.');
	    if (lastDot > 0) {
	        packageName = className.substring(0, lastDot);
	    }

	    String simpleClassName = className.substring(lastDot + 1);
	    String builderClassName = className + "Builder";
	    String builderSimpleClassName = builderClassName
	      .substring(lastDot + 1);

	    JavaFileObject builderFile = processingEnv.getFiler()
	      .createSourceFile(builderClassName);
	    
	    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

	        if (packageName != null) {
	            out.print("package ");
	            out.print(packageName);
	            out.println(";");
	            out.println();
	        }

	        out.print("public class ");
	        out.print(builderSimpleClassName);
	        out.println(" {");
	        out.println();

	        out.print("    private ");
	        out.print(simpleClassName);
	        out.print(" object = new ");
	        out.print(simpleClassName);
	        out.println("();");
	        out.println();

	        out.print("    public ");
	        out.print(simpleClassName);
	        out.println(" build() {");
	        out.println("        return object;");
	        out.println("    }");
	        out.println();

	        setterMap.entrySet().forEach(setter -> {
	            String methodName = setter.getKey();
	            String argumentType = setter.getValue();

	            out.print("    public ");
	            out.print(builderSimpleClassName);
	            out.print(" ");
	            out.print(methodName);

	            out.print("(");

	            out.print(argumentType);
	            out.println(" value) {");
	            out.print("        object.");
	            out.print(methodName);
	            out.println("(value);");
	            out.println("        return this;");
	            out.println("    }");
	            out.println();
	        });

	        out.println("}");
	        out.flush();
	    }
	}
}

/*

References:

https://www.baeldung.com/java-annotation-processing-builder

https://www.zdnet.com/article/writing-and-processing-custom-annotations-part-3/

*/
