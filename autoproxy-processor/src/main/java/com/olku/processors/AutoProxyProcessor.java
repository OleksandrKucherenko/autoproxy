package com.olku.processors;

import androidx.annotation.NonNull;

import com.google.auto.service.AutoService;
import com.olku.annotations.AutoProxy;
import com.olku.annotations.AutoProxyClassGenerator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * Annotation processor. Generate proxy class for interface.
 *
 * @see <a href="https://docs.gradle.org/nightly/userguide/java_plugin.html#sec:incremental_annotation_processing">Incremental Annotation Processing</a>
 */
@AutoService(Processor.class)
@SuppressWarnings("unused")
public class AutoProxyProcessor extends AbstractProcessor {
    public static boolean IS_DEBUG = false;

    private Messager logger;
    private Types typesUtil;
    private Elements elementsUtil;
    private Filer filer;
    /** @see <a href="https://github.com/evernote/android-state/commit/0072478291e2735223d6c14cb79a6b26524ec075#diff-6">Support incremental annotation processing</a> */
    private HashMap<String, List<Element>> mMapGeneratedFileToOriginatingElements = new LinkedHashMap<>();

    @Override
    public synchronized void init(final ProcessingEnvironment pe) {
        super.init(pe);

        logger = pe.getMessager();
        typesUtil = pe.getTypeUtils();
        elementsUtil = pe.getElementUtils();
        filer = pe.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<>();
        annotations.add(AutoProxy.class.getCanonicalName());
        annotations.add(AutoProxy.Yield.class.getCanonicalName());

        return annotations;
    }

    @Override
    public Set<String> getSupportedOptions() {
        // https://docs.gradle.org/nightly/userguide/java_plugin.html#sec:incremental_annotation_processing
        return Collections.singleton("org.gradle.annotation.processing.aggregating");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        // Writer for captured errors.
        final StringWriter errors = new StringWriter();
        int failed = 0;

        // iterate via all interfaces/classes with @AutoProxy declaration
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoProxy.class)) {
            final long now = System.nanoTime();

            TypeProcessor tp = null;
            try {
                tp = new TypeProcessor(element, logger, processingEnv);
                tp.extractMethods(typesUtil);

                if (IS_DEBUG) logger.printMessage(NOTE, tp.toString());

                final AutoProxyClassGenerator generator = tp.generator();

                if (generator.compose(filer)) {
                    if (IS_DEBUG) logger.printMessage(NOTE, "--- file: " + generator.getName());
                    if (IS_DEBUG) logger.printMessage(NOTE, "--- elements: " + generator.getOriginating().size());
                    if (IS_DEBUG) logger.printMessage(NOTE, "--- elements: " + generator.getOriginating().get(0).getSimpleName());

                    mMapGeneratedFileToOriginatingElements.put(generator.getName(), generator.getOriginating());
                } else {
                    logger.printMessage(IS_DEBUG ? ERROR : NOTE, generator.getErrors());
                }
            } catch (Throwable e) {
                e.printStackTrace(new PrintWriter(errors));
                failed++;
            }

            final long end = System.nanoTime();
            if (!IS_DEBUG) {
                logger.printMessage(NOTE, (null != tp ? tp.toShortString() : "TypeProcessor FAILED!") +
                        " takes: " + TimeUnit.NANOSECONDS.toMillis(end - now) + "ms\r\n");
            }
        }

        // TODO: compose utility class that used for CREATOR's methods

        //    /**
        //     * Represents a function with two arguments.
        //     *
        //     * @param <T1> the first argument type
        //     * @param <T2> the second argument type
        //     * @param <R>  the result type
        //     */
        //    public interface Func2<T1, T2, R> {
        //        R call(T1 t1, T2 t2);
        //    }

        if (failed > 0) {
            logger.printMessage(ERROR, errors.toString());
        }

        return true;
    }

    @NonNull
    public HashMap<String, List<Element>> getMapGeneratedFileToOriginatingElements() {
        return mMapGeneratedFileToOriginatingElements;
    }
}
