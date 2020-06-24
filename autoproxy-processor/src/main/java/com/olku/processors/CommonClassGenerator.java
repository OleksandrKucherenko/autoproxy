package com.olku.processors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.olku.annotations.AutoProxy;
import com.olku.annotations.AutoProxy.Flags;
import com.olku.annotations.AutoProxyClassGenerator;
import com.olku.annotations.RetBool;
import com.olku.annotations.RetNumber;
import com.olku.annotations.Returns;
import com.olku.generators.RetBoolGenerator;
import com.olku.generators.RetNumberGenerator;
import com.olku.generators.ReturnsGenerator;
import com.olku.generators.ReturnsPoet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import sun.reflect.annotation.AnnotationParser;

import static javax.tools.Diagnostic.Kind.NOTE;

/** Common Proxy Class generator. Class designed for inheritance. */
@SuppressWarnings({"WeakerAccess", "SameParameterValue", "UnnecessaryLocalVariable"})
public class CommonClassGenerator implements AutoProxyClassGenerator {
    /** Enable/Disable debug information. */
    public static boolean IS_DEBUG = AutoProxyProcessor.IS_DEBUG;

    //region Constants
    /** Represents Generic non-NULL value. */
    public static final Attribute.Compound GLOBAL_AFTER_CALL = new Attribute.Compound(null, com.sun.tools.javac.util.List.nil());
    /** Pre-call / predicate method name. */
    protected static final String PREDICATE = "predicate";
    /** Method for capturing results of call. */
    protected static final String AFTER_CALL = "afterCall";
    /** static method creator. */
    protected static final String CREATOR = "create";
    /** mapping method that allows to dispatch call to a proper method by it name. */
    protected static final String MAPPER = "dispatchByName";
    /** Annotation type name that is used for constants definition. */
    protected static final String METHODS = "M";
    /** parameter name. */
    protected static final String METHOD_NAME = "methodName";
    /** JavaDoc comment that inform developer how to fix issue with missed interface for old android API's. */
    private static final String BI_FUNCTION_FIX = "Copy this declaration to fix method demands for old APIs:\n\n" +
            "<pre>\n" +
            "package java.util.function;\n" +
            "\n" +
            "public interface BiFunction&lt;T, U, R&gt; {\n" +
            "    R apply(T t, U u);\n" +
            "}\n" +
            "</pre>";

    //endregion

    //region Members
    /** Data type for processing. */
    protected final TypeProcessor type;
    /** Writer for captured errors. */
    protected final StringWriter errors = new StringWriter();
    /** Resolved super type name. */
    protected final TypeName superType;
    /** Is any 'after calls' annotations found. */
    protected final AtomicBoolean isAnyAfterCalls = new AtomicBoolean();
    /** Lookup of "method name"-to-"arguments line". */
    protected final Map<String, Symbol.MethodSymbol> mappedCalls = new TreeMap<>();
    /** List of method names. */
    protected final Set<String> knownMethods = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    /** Result file. */
    protected JavaFile javaFile;
    //endregion

    //region Constructor

    /**
     * Main constructor.
     *
     * @param type reference on data type processor.
     */
    public CommonClassGenerator(@NonNull final TypeProcessor type) {
        this.type = type;

        superType = TypeName.get(this.type.element.asType());
    }
    //endregion

    //region Code generator

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compose(@NonNull final Filer filer) {
        try {
            composeInternal(filer);
        } catch (final Throwable ex) {
            ex.printStackTrace(new PrintWriter(errors));
            return false;
        }

        return true;
    }

    /**
     * Compose Plain logic without exception handling wrapper.
     *
     * @param filer instance needed for file creation.
     */
    protected void composeInternal(@NonNull Filer filer) throws Exception {
        // is generation flag for forced afterCall set
        final boolean hasAfterCalls = ((this.type.annotation.flags() & Flags.AFTER_CALL) == Flags.AFTER_CALL);
        isAnyAfterCalls.set(hasAfterCalls);

        // compose class
        final FieldSpec[] members = createMembers();
        final TypeSpec.Builder classSpec = createClass(members);

        // constructor and predicate
        classSpec.addMethod(createConstructor().build());
        classSpec.addMethod(createPredicate().build());

        // auto-generate method proxy calls
        createMethods(classSpec);

        // if any after call annotation found in class/methods
        if (isAnyAfterCalls.get()) {
            classSpec.addMethod(createAfterCall().build());
        }

        // if allowed creator method
        if ((this.type.annotation.flags() & Flags.CREATOR) == Flags.CREATOR) {
            classSpec.addMethod(createCreator().build());
        }

        // if allowed mapper method
        if ((this.type.annotation.flags() & Flags.MAPPING) == Flags.MAPPING) {
            classSpec.addMethod(createMapper().build());
        }

        classSpec.addType(createMethodsMapper().build());

        classSpec.addOriginatingElement(type.element);

        // save class to disk
        javaFile = JavaFile.builder(type.packageName.toString(), classSpec.build()).build();
        javaFile.writeTo(filer);
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public String getErrors() {
        return errors.toString();
    }

    /** {@inheritDoc} */
    @NonNull
    @Override
    public String getName() {
        if (null == javaFile) return "";

        return javaFile.toJavaFileObject().getName();
    }

    /** {@inheritDoc} */
    @NonNull
    @Override
    public List<Element> getOriginating() {
        if (null == javaFile) return Collections.emptyList();

        return javaFile.typeSpec.originatingElements;
    }
    //endregion

    //region Implementation
    @NonNull
    protected FieldSpec[] createMembers() {
        final List<FieldSpec> fields = new ArrayList<>();

        final TypeName typeOfField = getSuperTypeName();
        final FieldSpec.Builder builder = FieldSpec.builder(typeOfField, "inner", Modifier.PROTECTED, Modifier.FINAL);
        fields.add(builder.build());

        return fields.toArray(new FieldSpec[0]);
    }

    /** Get supper type that should be used as a inner instance data type. */
    @NonNull
    private TypeName getSuperTypeName() {
        return this.type.getAnnotationSuperTypeAsTypeName();
    }

    @NonNull
    protected TypeSpec.Builder createClass(@NonNull final FieldSpec... members) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("Proxy_" + type.flatClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        // TODO: mimic annotations of the super type

        // @javax.annotation.Generated("AutoProxy Auto Generated Code")
        builder.addAnnotation(AnnotationSpec.builder(javax.annotation.Generated.class)
                .addMember("value", "$S", "AutoProxy Auto Generated Code")
                .build());

        if (ElementKind.INTERFACE == type.element.getKind()) {
            builder.addSuperinterface(superType);

            copyTypeGenericVariables(builder);
        } else if (ElementKind.CLASS == type.element.getKind()) {
            builder.superclass(superType);

            copyTypeGenericVariables(builder);
        } else {
            final String message = "Unsupported data type: " + type.element.getKind() + ", " + type.elementType;
            errors.write(message + "\n");

            throw new UnsupportedOperationException(message);
        }

        for (final FieldSpec member : members) {
            builder.addField(member);
        }

        return builder;
    }

    /** copy generic parameters */
    private void copyTypeGenericVariables(final TypeSpec.Builder builder) {
        if (!(superType instanceof ParameterizedTypeName)) return;

        ParameterizedTypeName ptn = (ParameterizedTypeName) superType;

        for (final TypeName typeName : ptn.typeArguments) {
            if (!(typeName instanceof TypeVariableName)) continue;

            builder.addTypeVariable((TypeVariableName) typeName);
        }
    }

    /** copy generic parameters for method. */
    @SuppressWarnings("unused")
    private void copyMethodGenericVariables(final MethodSpec.Builder builder) {
        if (!(superType instanceof ParameterizedTypeName)) return;

        ParameterizedTypeName ptn = (ParameterizedTypeName) superType;

        for (final TypeName typeName : ptn.typeArguments) {
            if (!(typeName instanceof TypeVariableName)) continue;

            builder.addTypeVariable((TypeVariableName) typeName);
        }
    }

    @NonNull
    protected MethodSpec.Builder createConstructor() {
        final TypeName innerMemberSuperType = getSuperTypeName();
        final ParameterSpec.Builder param = ParameterSpec.builder(innerMemberSuperType, "instance", Modifier.FINAL)
                .addAnnotation(NonNull.class);

        final MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(param.build())
                .addStatement("this.inner = $N", "instance");

        return builder;
    }

    /**
     * Create methods for provided class.
     *
     * @param classSpec instance of class specification builder
     * @throws Exception give a change to exceptions in depth to deliver the real cause
     */
    protected void createMethods(@NonNull final TypeSpec.Builder classSpec) throws Exception {
        // compose methods
        RuntimeException runtimeError = null;
        for (final Element method : type.methods) {
            if (!(method instanceof Symbol.MethodSymbol)) {
                final String message = "Unexpected method type: " + method.getClass().getSimpleName();
                errors.write(message + "\n");

                runtimeError = new UnsupportedOperationException(message);
                continue;
            }

            classSpec.addMethod(createMethod((Symbol.MethodSymbol) method).build());
        }

        // if were detected exception, throw it
        if (null != runtimeError) {
            throw runtimeError;
        }
    }

    /**
     * Create predicate method declaration.
     *
     * @return instance of the method builder.
     */
    @NonNull
    protected MethodSpec.Builder createPredicate() {
        // TODO: resolve potential name conflict

        final String methodName = PREDICATE;
        final MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        builder.returns(boolean.class);

        final ParameterSpec pMethodNames = ParameterSpec.builder(String.class, METHOD_NAME, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess(METHODS)).build())
                .addAnnotation(AnnotationSpec.builder(NonNull.class).build())
                .build();

        builder.addParameter(pMethodNames);

        // varargs 
        builder.varargs(true);
        builder.addParameter(Object[].class, "args", Modifier.FINAL);

        return builder;
    }

    /**
     * Create afterCall method declaration.
     *
     * @return instance of the method builder.
     */
    @NonNull
    protected MethodSpec.Builder createAfterCall() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder(AFTER_CALL);
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        builder.addTypeVariable(TypeVariableName.get("T", Object.class));

        builder.returns(TypeVariableName.get("T"));

        final ParameterSpec pMethodNames = ParameterSpec.builder(String.class, METHOD_NAME, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess(METHODS)).build())
                .addAnnotation(AnnotationSpec.builder(NonNull.class).build())
                .build();
        builder.addParameter(pMethodNames);

        builder.addParameter(TypeVariableName.get("T"), "result", Modifier.FINAL);

        return builder;
    }

    /**
     * Special static method for simplified class instance creation.
     *
     * @return static method builder
     */
    @NonNull
    protected MethodSpec.Builder createCreator() {
//  Output:
//        public static <T extends View> UiChange<T> creator(@NonNull final UiChange<T> instance, Func2<String, Object[], Boolean> action) {
//            return new Proxy_UiChange<T>(instance) {
//                @Override
//                public boolean predicate(final String methodName, final Object... args) {
//                    return action.call(methodName, args);
//                }
//                @Override
//                public <R> R afterCall(final String methodName, final R result) {
//                    return result;
//                };
//            };
//        }

        final MethodSpec.Builder builder = MethodSpec.methodBuilder(CREATOR);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        copyMethodGenericVariables(builder);
        builder.returns(superType);

        final TypeName innerMemberType = getSuperTypeName();
        builder.addParameter(innerMemberType, "instance", Modifier.FINAL);
//        builder.addParameter(ParameterizedTypeName.get(Func2.class, String.class, Object[].class, Boolean.class), "action", Modifier.FINAL);
        builder.addParameter(ParameterizedTypeName.get(BiFunction.class, String.class, Object[].class, Boolean.class), "action", Modifier.FINAL);
        builder.addJavadoc(BI_FUNCTION_FIX);

        final String afterCallOverride = '\n' +
                "  @Override\n" +
                "  public <T> T afterCall(final String methodName, final T result) {\n" +
                "    return result;\n" +
                "  };\n";

        final String predicateOverride = '\n' +
                "  @Override\n" +
                "  public boolean predicate(final String methodName, final Object... args) {\n" +
                "    return action.apply(methodName, args);\n" +
                "  }\n";

        builder.addCode("" +
                "return new $L(instance) {\n" +
                predicateOverride +
                (isAnyAfterCalls.get() ? afterCallOverride : "") +
                "};\n", "Proxy_" + type.flatClassName);

        return builder;
    }

    @NonNull
    protected MethodSpec.Builder createMapper() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder(MAPPER);
        builder.addModifiers(Modifier.PUBLIC);

        builder.addTypeVariable(TypeVariableName.get("T", Object.class));
        builder.returns(TypeVariableName.get("T"));

        final ParameterSpec pMethodNames = ParameterSpec.builder(String.class, METHOD_NAME, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess(METHODS)).build())
                .addAnnotation(AnnotationSpec.builder(NonNull.class).build())
                .build();
        builder.addParameter(pMethodNames);

        // varargs
        builder.varargs(true);
        builder.addParameter(Object[].class, "args", Modifier.FINAL);

        // add boxing/unboxing trick
        builder.addCode("final Object result;\n");

        for (final String name : mappedCalls.keySet()) {
            final Symbol.MethodSymbol ms = mappedCalls.get(name);
            final Type returnType = ms.getReturnType();
            final boolean hasReturn = returnType.getKind() != TypeKind.VOID;
            final String methodName = ms.getSimpleName().toString();
            final String params = composeCallParamsFromArray(ms, "args");

            builder.beginControlFlow("if($L.$L.equals(methodName))", METHODS, toConstantName(name));
            if (hasReturn) {
                builder.addStatement("return (T)(result = this.inner.$N($L))", methodName, params);
            } else {
                builder.addStatement("this.inner.$N($L)", methodName, params);
                builder.addStatement("return (T)null");
            }
            builder.endControlFlow();
        }

        // fallback
        builder.addCode("return (T)null;\n");

        return builder;
    }

    /** Compose inner interface with all method names. */
    @NonNull
    protected TypeSpec.Builder createMethodsMapper() {
        final TypeSpec.Builder builder = TypeSpec.annotationBuilder(METHODS)
                .addModifiers(Modifier.PUBLIC);

        final List<String> constants = new ArrayList<>(knownMethods.size());
        final StringBuilder format = new StringBuilder().append("{");

        String prefix = "";

        for (final String name : mappedCalls.keySet()) {
            final Symbol.MethodSymbol ms = mappedCalls.get(name);
            final String methodName = ms.getSimpleName().toString();

            constants.add(METHODS + "." + toConstantName(name));
            format.append(prefix).append("$L");

            builder.addField(FieldSpec.builder(String.class, toConstantName(name))
                    .addJavadoc("{@link #$L($L)}", methodName, composeCallParamsTypes(ms))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", name)
                    .build());

            prefix = ", ";
        }

        format.append("}"); // close array

        builder.addAnnotation(AnnotationSpec.builder(StringDef.class)
                .addMember("value", format.toString(), constants.toArray())
                .build());

        return builder;
    }

    /** Compose list of method parameters data types, comma separated. */
    @NonNull
    private String composeCallParamsTypes(@NonNull final Symbol.MethodSymbol ms) {
        String delimiter = "";
        final StringBuilder result = new StringBuilder();

        final com.sun.tools.javac.util.List<Symbol.VarSymbol> parameters = ms.getParameters();

        for (int i = 0, len = parameters.size(); i < len; i++) {
            final Symbol.VarSymbol param = parameters.get(i);
            final TypeName paramType = TypeName.get(param.asType());

            // compose parameters list for forwarding
            result.append(delimiter).append(paramType.toString());
            delimiter = ", ";
        }

        return result.toString();
    }

    /** Compose extracting of method parameters from vararg array with data type casting. */
    @NonNull
    private String composeCallParamsFromArray(@NonNull final Symbol.MethodSymbol ms,
                                              @NonNull final String arrayName) {
        String delimiter = "";
        final StringBuilder result = new StringBuilder();

        final com.sun.tools.javac.util.List<Symbol.VarSymbol> parameters = ms.getParameters();

        for (int i = 0, len = parameters.size(); i < len; i++) {
            final Symbol.VarSymbol param = parameters.get(i);

            // mimic parameter of the method: name, type, modifiers
            final TypeName paramType = TypeName.get(param.asType());
            final String parameterName = param.name.toString();
            final String parameterExtract = String.format(Locale.US, "(%s)%s[%d] /*%s*/", paramType.toString(), arrayName, i, parameterName);

            // compose parameters list for forwarding
            result.append(delimiter).append(parameterExtract);
            delimiter = ", ";
        }

        return result.toString();
    }

    /** Create override method of the proxy class. */
    @NonNull
    protected MethodSpec.Builder createMethod(final Symbol.MethodSymbol ms) throws Exception {
// Output:
//        @NonNull
//        public final UiChange<T> translateX(final int diff) {
//            if (!predicate( M.translateX_diff, diff )) {
//                // return current instance
//                return (UiChange<T>)this;
//            }
//            return this.inner.translateX(diff);
//        }

        final String methodName = ms.getSimpleName().toString();
        final MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);

        builder.addModifiers(Modifier.FINAL, Modifier.PUBLIC);

        // extract annotations of return type / method. copy all, except @Yield & @AfterCall
        mimicMethodAnnotations(builder, ms);

        // extract our own annotations
        final Attribute.Compound yield = findYieldMethodAnnotation(ms);
        final Attribute.Compound after = withGlobalOverride(findAfterMethodAnnotation(ms));

        // extract return type
        final Type returnType = ms.getReturnType();
        final boolean hasReturn = returnType.getKind() != TypeKind.VOID;
        builder.returns(TypeName.get(returnType));

        // extract parameters
        final StringBuilder arguments = mimicParameters(builder, ms);

        // method name with unique signature
        final String uniqueMethodName = methodName + asMethodNamePart(arguments);
        mappedCalls.put(uniqueMethodName, ms);
        knownMethods.add(uniqueMethodName);
        knownMethods.add(methodName);

        // extract throws
        mimicThrows(builder, ms);

        // expected: if (!predicate( Methods.translateX_diff, diff ))
        builder.beginControlFlow("if (!$L( $L.$L$L ))", PREDICATE,
                METHODS, toConstantName(uniqueMethodName),
                (arguments.length() == 0 ? "" : ", ") + arguments);

        // generate default return value
        if (hasReturn || null != yield) {
            if (null != yield) builder.addComment("" + yield);
            createYieldPart(builder, returnType, yield);
        } else {
            // line should correspond default value of AutoProxy.Yield.value() == Returns.THROWS
            builder.addStatement("throw new $T($S)", UnsupportedOperationException.class, "cannot resolve return value.");
        }

        builder.endControlFlow();

        // generate return
        if (null == after) {
            builder.addStatement((hasReturn ? "return " : "") + "this.inner.$N($L)", methodName, arguments);
        } else {
            isAnyAfterCalls.set(true);

            if (hasReturn) {
                builder.addStatement("return $L($L.$L, this.inner.$N($L))", AFTER_CALL,
                        METHODS, toConstantName(uniqueMethodName),
                        methodName, arguments);
            } else {
                builder.addStatement("this.inner.$N($L)", methodName, arguments);
                builder.addStatement("$L($L.$L, null)", AFTER_CALL,
                        METHODS, toConstantName(uniqueMethodName));
            }
        }

        return builder;
    }

    /**
     * Convert provided name to CONSTANT name.
     *
     * @param name name of the method that should be converted to suitable constant field name.
     * @return name suitable for Constant field declaration
     */
    @NonNull
    protected String toConstantName(@NonNull final String name) {
        return name.toUpperCase(Locale.US);
    }

    /**
     * Convert array of arguments string as a valid method name.
     *
     * @param arguments String representation of arguments array.
     * @return name suitable for Constant field declaration
     */
    @NonNull
    private String asMethodNamePart(@NonNull final StringBuilder arguments) {
        return ((arguments.length() > 0) ? "_" : "") + // delimiter
                arguments.toString().replaceAll(", ", "_");
    }

    /** Extract afterCall method annotation with respect to global AutoProxy flags. */
    @Nullable
    private Attribute.Compound withGlobalOverride(@Nullable final Attribute.Compound afterMethodAnnotation) {
        if (null != afterMethodAnnotation) return afterMethodAnnotation;

        final boolean hasAfterCalls = ((this.type.annotation.flags() & Flags.AFTER_CALL) == Flags.AFTER_CALL);

        if (hasAfterCalls) {
            return GLOBAL_AFTER_CALL;
        }

        return null;
    }

    /** Compose default value return if proxy do not allows call to inner instance. */
    protected void createYieldPart(@NonNull final MethodSpec.Builder builder,
                                   @NonNull final Type returnType,
                                   @Nullable final Attribute.Compound yield) throws Exception {
        // create return based on @Yield annotation values
        final AutoProxy.Yield annotation = extractYield(yield);
        final String value = annotation.value();
        final Class<?> adapter = annotation.adapter();
        final ReturnsPoet poet;

        if (RetBool.class == adapter || RetBoolGenerator.class == adapter) {
            poet = RetBoolGenerator.getInstance();
        } else if (Returns.class == adapter && isRetBoolValue(value)) {
            poet = RetBoolGenerator.getInstance();
        } else if (RetNumber.class == adapter || RetNumberGenerator.class == adapter) {
            poet = RetNumberGenerator.getInstance();
        } else if (Returns.class == adapter && isRetNumberValue(value)) {
            poet = RetNumberGenerator.getInstance();
        } else if (Returns.class == adapter || ReturnsGenerator.class == adapter) {
            poet = ReturnsGenerator.getInstance();
        } else {
            // create instance of generator by reflection info
            final Constructor<?> ctr = adapter.getConstructor();
            poet = (ReturnsPoet) ctr.newInstance();
        }

        final boolean composed = poet.compose(returnType, value, builder);

        if (!composed) {
            ReturnsGenerator.getInstance().compose(returnType, Returns.THROWS, builder);
        }
    }

    private boolean isRetBoolValue(String value) {
        return RetBool.TRUE.equals(value) || RetBool.FALSE.equals(value);
    }

    private boolean isRetNumberValue(String value) {
        return RetNumber.ZERO.equals(value) || RetNumber.MAX.equals(value) || RetNumber.MIN.equals(value) || RetNumber.MINUS_ONE.equals(value);
    }

    @NonNull
    protected AutoProxy.Yield extractYield(@Nullable final Attribute.Compound yield) throws Exception {
        // default values of Yield
        final Map<String, Object> map = AutoProxy.DefaultYield.asMap();

        // overrides
        if (null != yield) {
            // extract default values, https://stackoverflow.com/questions/16299717/how-to-create-an-instance-of-an-annotation
            if (IS_DEBUG) type.logger.printMessage(NOTE, "extracting: " + yield.toString());

            for (final Map.Entry<Symbol.MethodSymbol, Attribute> entry : yield.getElementValues().entrySet()) {
                final String key = entry.getKey().name.toString();
                Object value = entry.getValue().getValue();

                if (value instanceof Type.ClassType) {
                    final Name name = ((Type.ClassType) value).asElement().getQualifiedName();

                    value = Class.forName(name.toString());
                }

                map.put(key, value);
            }
        } else { // apply global configuration
            if (IS_DEBUG)
                type.logger.printMessage(NOTE, "used global config: " + this.type.annotation.defaultYield());

            map.put("value", this.type.annotation.defaultYield());
        }

        // new instance
        return (AutoProxy.Yield) AnnotationParser.annotationForMap(AutoProxy.Yield.class, map);
    }
    //endregion

    //region Helpers

    /**
     * Mimic annotations of the method, but exclude @Yield annotation during processing.
     *
     * @param builder instance of poet builder used for composing method
     * @param ms      reference on instance of a method information
     * @throws Exception method can fail in depth, allow raising of exception on top
     */
    public static void mimicMethodAnnotations(@NonNull final MethodSpec.Builder builder,
                                              @NonNull final Symbol.MethodSymbol ms) throws Exception {
        if (ms.hasAnnotations()) {
            for (final Attribute.Compound am : ms.getAnnotationMirrors()) {
                if (extractClass(am) == AutoProxy.Yield.class) continue;
                if (extractClass(am) == AutoProxy.AfterCall.class) continue;

                final AnnotationSpec.Builder builderAnnotation = mimicAnnotation(am);
                if (null != builderAnnotation) {
                    builder.addAnnotation(builderAnnotation.build());
                }
            }
        }
    }

    @Nullable
    public static Attribute.Compound findAfterMethodAnnotation(@NonNull final Symbol.MethodSymbol ms) throws Exception {
        if (ms.hasAnnotations()) {
            for (final Attribute.Compound am : ms.getAnnotationMirrors()) {
                if (extractClass(am) == AutoProxy.AfterCall.class) return am;
            }
        }

        return null;
    }

    @Nullable
    public static Attribute.Compound findYieldMethodAnnotation(@NonNull final Symbol.MethodSymbol ms) throws Exception {
        if (ms.hasAnnotations()) {
            for (final Attribute.Compound am : ms.getAnnotationMirrors()) {
                if (extractClass(am) == AutoProxy.Yield.class) return am;
            }
        }

        return null;
    }

    /**
     * Compose exceptions throwing signature.
     *
     * @param builder instance of poet method builder
     * @param ms      reference on source method information
     */
    public static void mimicThrows(@NonNull final MethodSpec.Builder builder,
                                   @NonNull final Symbol.MethodSymbol ms) {
        for (final Type typeThrown : ms.getThrownTypes()) {
            builder.addException(TypeName.get(typeThrown));
        }
    }

    /**
     * Compose method parameters that mimic original code.
     *
     * @param builder reference on poet class instance that used for method composing
     * @param ms      reference on method signature details, symbols, parameters
     * @return reference on string builder with enumerated parameters
     * @throws Exception can fail during mimicing signature of the method
     */
    @NonNull
    public static StringBuilder mimicParameters(@NonNull final MethodSpec.Builder builder,
                                                @NonNull final Symbol.MethodSymbol ms) throws Exception {
        String delimiter = "";
        final StringBuilder arguments = new StringBuilder();

        final com.sun.tools.javac.util.List<Symbol.VarSymbol> parameters = ms.getParameters();

        for (int i = 0, len = parameters.size(); i < len; i++) {
            final Symbol.VarSymbol param = parameters.get(i);

            // mimic parameter of the method: name, type, modifiers
            final TypeName paramType = TypeName.get(param.asType());
            final String parameterName = param.name.toString();
            final ParameterSpec.Builder parameter = ParameterSpec.builder(paramType, parameterName, Modifier.FINAL);

            if (param.hasAnnotations()) {
                // DONE: copy annotations of parameter
                for (final Attribute.Compound am : param.getAnnotationMirrors()) {
                    final AnnotationSpec.Builder builderAnnotation = mimicAnnotation(am);

                    if (null != builderAnnotation) {
                        parameter.addAnnotation(builderAnnotation.build());
                    }
                }
            }

            // support VarArgs if needed
            builder.varargs(ms.isVarArgs() && i == len - 1);
            builder.addParameter(parameter.build());

            // compose parameters list for forwarding
            arguments.append(delimiter).append(parameterName);
            delimiter = ", ";
        }

        return arguments;
    }

    /**
     * Compose annotation spec from mirror the original code.
     *
     * @param am instance of compound attribute that contains class information
     * @return instance of annotation builder or NULL
     * @throws Exception can potentially raise exception
     */
    @SuppressWarnings("RedundantThrows")
    @Nullable
    public static AnnotationSpec.Builder mimicAnnotation(@NonNull final Attribute.Compound am) throws Exception {
        final Class<?> clazz;

        try {
            clazz = extractClass(am);
            return AnnotationSpec.builder(clazz);
        } catch (final Throwable ignored) {
            // Not all annotations can be extracted, annotations marked as @Retention(SOURCE)
            // cannot be extracted by our code
//            if(IS_DEBUG) throw ignored;
        }

        return null;
    }

    /**
     * Extract reflection Class&lt;?&gt; information from compound.
     *
     * @param am reference on compound attribute that represents class
     * @return found class reflection information
     * @throws ClassNotFoundException provided wrong class reference
     */
    @NonNull
    public static Class<?> extractClass(@NonNull final Attribute.Compound am) throws ClassNotFoundException {
        final TypeElement te = (TypeElement) am.getAnnotationType().asElement();

        return extractClass(te);
    }

    /**
     * Extract reflection Class&lt;?&gt; information from type element.
     *
     * @param te reference on type element
     * @return found class reflection information
     * @throws ClassNotFoundException provided wrong class name.
     */
    @NonNull
    public static Class<?> extractClass(@NonNull final TypeElement te) throws ClassNotFoundException {
        final Name name;

        if (te instanceof Symbol.ClassSymbol) {
            final Symbol.ClassSymbol cs = (Symbol.ClassSymbol) te;

            // this method is more accurate for nested classes
            name = cs.flatName();
        } else {
            name = te.getQualifiedName();
        }

        final String className = name.toString();

        try {
            return Class.forName(className).asSubclass(Annotation.class);
        } catch (ClassNotFoundException ex) {
            // it can be sub-type, try another approach bellow
        }

        final int dot = className.lastIndexOf(".");
        final String innerFix2 = className.substring(0, dot) + "$" + className.substring(dot + 1);
        return Class.forName(innerFix2).asSubclass(Annotation.class);
    }
    //endregion
}
