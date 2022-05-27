
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.github.mapresultset.Table")
public class MappingProcessor extends AbstractProcessor {

	private Set<Element> annotatedElements = new HashSet<>();

	@Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init( processingEnv );

		System.out.println(processingEnv.getElementUtils());
		System.out.println(processingEnv.getTypeUtils());
		System.out.println(processingEnv.getMessager());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
		if ( roundEnvironment.processingOver() ) {
			System.out.println("Last round! Fight!");
			// processElements();
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
				System.out.println("Element: " + e);
				Element enclosingElement = e.getEnclosingElement();
				String name = e.getSimpleName().toString();
				System.out.println("element name: " + name);
				System.out.println("element enclosingElement: " + enclosingElement);
				System.out.println("element enclosedElements: " + e.getEnclosedElements());

				/*
				Class<?> clazz;
				try {
					if (enclosingElement.getKind() == ElementKind.CLASS) {
						clazz = Class.forName(enclosingElement + "$" + name);
					} else {
						clazz = Class.forName(e.toString());
					}
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
					throw new RuntimeException(ex);
				}
				System.out.println("Class: " + clazz);
				System.out.println("Class fields: ");
				for (var f : clazz.getFields())
					System.out.println(f.getName());
				System.out.println("Class methods: ");
				for (var m : clazz.getMethods())
					System.out.println(m.getName());
				*/
			}
		}
	}

	private void processElements() {
		for (var e : this.annotatedElements) {
			System.out.println("Element: " + e);
			Element enclosingElement = e.getEnclosingElement();
			String name = e.getSimpleName().toString();
			System.out.println("element name: " + name);
			System.out.println("element enclosingElement: " + enclosingElement);

			Class<?> clazz;
			try {
				if (enclosingElement.getKind() == ElementKind.CLASS) {
					clazz = Class.forName(enclosingElement + "$" + name);
				} else {
					clazz = Class.forName(e.toString());
				}
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			System.out.println("Class: " + clazz);
			System.out.println("Class fields: ");
			for (var f : clazz.getFields())
				System.out.println(f.getName());
			System.out.println("Class methods: ");
			for (var m : clazz.getMethods())
				System.out.println(m.getName());
		}
	}
}

/*

References:

https://www.baeldung.com/java-annotation-processing-builder


*/
