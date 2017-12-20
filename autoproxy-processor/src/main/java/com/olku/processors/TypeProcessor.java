package com.olku.processors;

import android.support.annotation.*;

import com.olku.annotations.AutoProxy;
import com.olku.annotations.AutoProxyClassGenerator;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import sun.reflect.annotation.AnnotationParser;

import static javax.tools.Diagnostic.Kind.NOTE;

/** Helper that extracts items for processing. */
public class TypeProcessor {
    public static boolean IS_DEBUG = AutoProxyProcessor.IS_DEBUG;

    final Element element;
    final String flatClassName;
    final Name elementName;
    final Name packageName;
    final TypeMirror elementType;
    final AutoProxy annotation;
    final Messager logger;
    final ArrayList<Element> methods;

    /** Main constructor. */
    public TypeProcessor(@NonNull final Element element, @NonNull final Messager logger) {
        this.element = element;
        this.logger = logger;

        elementName = element.getSimpleName();
        flatClassName = flatName(element);

        final Symbol.PackageSymbol packageInfo = (Symbol.PackageSymbol) findPackage(element);
        packageName = packageInfo.getQualifiedName();
        elementType = element.asType();

        final Attribute.Compound ap = findAutoProxy(element.getAnnotationMirrors());
        this.annotation = extractAnnotation(ap);
        methods = new ArrayList<>();
    }

    /** Compose flat name for provided class element. Nested classes will be divided by '$' symbol. */
    public String flatName(@NonNull final Element classInfo) {
        StringBuilder builder = new StringBuilder();

        Element start = classInfo;
        String divider = "";

        while (null != start && !(start instanceof PackageElement)) {
            builder.insert(0, start.getSimpleName() + divider);

            start = ((Symbol) start).owner;

            divider = "$";
        }

        return builder.toString();
    }

    /** Find package name for provided class element. */
    @NonNull
    public PackageElement findPackage(@NonNull final Element classInfo) {
        Element start = classInfo;

        while (null != start && !(start instanceof PackageElement)) {
            start = ((Symbol) start).owner;
        }

        if (null != start)
            return (PackageElement) start;

        throw new AssertionError("Cannot find a package name for class. " + classInfo);
    }

    /** Extract methods from all inheritance methods. */
    public void extractMethods(@NonNull final Types typeUtils) {
        final Set<? extends Element> elements = inheritance(typeUtils, (TypeElement) element);

        // extract methods for overriding
        for (final Element clazz : elements) {
            for (final Element subElement : clazz.getEnclosedElements()) {
                if (subElement.getKind() != ElementKind.METHOD) continue;

                // skip static methods
                if (subElement.getModifiers().contains(Modifier.STATIC)) continue;

                // only public methods allowed
                if (!subElement.getModifiers().contains(Modifier.PUBLIC)) continue;

                methods.add(subElement);
            }
        }
    }

    @NonNull
    public static Set<? extends Element> inheritance(@NonNull final Types typeUtils,
                                                     @NonNull final TypeElement elem) {
        // https://stackoverflow.com/questions/30616589/how-to-get-the-super-class-name-in-annotation-processing
        final Set<Element> elements = new HashSet<>();

        TypeElement elemInner = elem;

        while (!isObject(elemInner)) {
            elements.add(elemInner);

            // extended or implemented interfaces
            final List<? extends TypeMirror> interfaces = elemInner.getInterfaces();
            for (final TypeMirror tm : interfaces) {
                final TypeElement elemInterface = (TypeElement) typeUtils.asElement(tm);

                elements.addAll(inheritance(typeUtils, elemInterface));
            }

            // extended classes
            elemInner = (TypeElement) typeUtils.asElement(elemInner.getSuperclass());
            if (null == elemInner) break;
        }

        return elements;
    }

    private static boolean isObject(@NonNull final TypeElement elem) {
        return "java.lang.Object".equals(elem.getQualifiedName().toString());
    }

    @NonNull
    private Attribute.Compound findAutoProxy(@NonNull final List<? extends AnnotationMirror> mirrors) {
        for (final AnnotationMirror mirror : mirrors) {
            final TypeElement element = (TypeElement) mirror.getAnnotationType().asElement();

            try {
                final Class<?> aClass = CommonClassGenerator.extractClass(element);

                if (AutoProxy.class == aClass) {
                    return (Attribute.Compound) mirror;
                }
            } catch (Throwable ignored) {
                // do nothing
            }
        }

        throw new RuntimeException("AutoProxy annotation not found");
    }

    @Override
    public String toString() {
        return "" + "\n"
                + "Package name         : " + packageName.toString() + "\n"
//                + "Package root name    : " + flatClassName.toString() + "\n"
                + "Element type         : " + elementType.toString() + "\n"
                + "Element name         : " + elementName.toString() + "\n"
                + "Annotation           : " + annotation.toString() + "\n"
//                + "Element kind         : " + element.asType().getKind() + "\n"
                + "Methods              : " + Arrays.toString(methods.toArray())
                + "\n";
    }

    @NonNull
    public String toShortString() {
        return "AutoProxy Processing : " + elementType.toString();
    }

    /** Get new instance of class generator. */
    @NonNull
    public AutoProxyClassGenerator generator() {
        // https://area-51.blog/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
        try {
            Class<?> generator = annotation.value();

            // quick jump to default generator
            if (generator == AutoProxyClassGenerator.class || generator == AutoProxy.Default.class)
                return new CommonClassGenerator(this);

            // create new instance by reflection
            return (AutoProxyClassGenerator) generator.getConstructor().newInstance();
        } catch (Throwable e) {
            logger.printMessage(Kind.ERROR, "CANNOT USE CUSTOM GENERATOR: " + e.getMessage());

            return new CommonClassGenerator(this);
        }
    }

    @NonNull
    private AutoProxy extractAnnotation(@Nullable final Attribute.Compound annotation) {
        // extract default values, https://stackoverflow.com/questions/16299717/how-to-create-an-instance-of-an-annotation
        if (IS_DEBUG) logger.printMessage(NOTE, "extracting: " + (null != annotation ? annotation.toString() : "NULL"));

        // default values of Yield
        final Map<String, Object> map = new HashMap<>();
        map.put("value", AutoProxy.Default.class);

        // overrides
        if (null != annotation) {
            for (final Map.Entry<Symbol.MethodSymbol, Attribute> entry : annotation.getElementValues().entrySet()) {
                final String key = entry.getKey().name.toString();
                Object value = entry.getValue().getValue();

                if (value instanceof Type.ClassType) {
                    final Name name = ((Type.ClassType) value).asElement().getQualifiedName();

                    try {
                        value = Class.forName(name.toString());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Cannot extract class information. " + name, e);
                    }
                }

                map.put(key, value);
            }
        }

        // new instance
        return (AutoProxy) AnnotationParser.annotationForMap(AutoProxy.class, map);
    }
}
