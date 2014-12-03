package com.jwoolston.internal;

import com.jwoolston.Final;
import com.jwoolston.Mutable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * @see javax.annotation.processing.AbstractProcessor
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public final class FinalizerProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        findAndParseTargets(roundEnv);
        return true;
    }

    private void findAndParseTargets(RoundEnvironment roundEnv) {
        // Process each @Final element.
        for (Element element : roundEnv.getElementsAnnotatedWith(Final.class)) {
            try {
                parseFinalize(element);
            } catch (Exception e) {
                final StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));

                error(element, "Unable to process @Final.\n\n%s", stackTrace);
            }
        }
    }

    private void parseFinalize(Element element) {
        // Check for the other field annotation.
        if (element.getAnnotation(Mutable.class) != null) {
            error(element, "Only one of @Final and @Mutable is allowed. (%s)", element.getSimpleName());
            return;
        }

        // Finalize this element
        finalizeElement(element);

        // Finalize child elements
        finalizeChildren(element);
    }

    private void finalizeElement(Element element) {
        log(element, "Adding final modifier.");
        element.getModifiers().add(Modifier.FINAL);
    }

    private void finalizeChildren(Element element) {
        final List<? extends Element> children = element.getEnclosedElements();
        for (Element child : children) {
            if (child.getAnnotation(Mutable.class) != null) {
                // The coder asked for this element to be mutable, along with its children. Skip it.
                log(child, "@Mutable annotation found. Skipping child element. (%s)", child.getSimpleName());
            } else {
                // Add the final marker to this element
                finalizeElement(child);
            }
        }
    }

    private void log(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(NOTE, message, element);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }
}
