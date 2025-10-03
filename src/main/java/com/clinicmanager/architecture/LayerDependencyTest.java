// package com.clinicmanager.architecture;
//
// import com.tngtech.archunit.core.importer.ImportOption;
// import com.tngtech.archunit.junit.AnalyzeClasses;
// import com.tngtech.archunit.junit.ArchTest;
// import com.tngtech.archunit.lang.ArchRule;
//
// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
//
// @AnalyzeClasses(packages = "com.clinicmanager", importOptions =
// ImportOption.DoNotIncludeTests.class)
// class LayerDependencyTest {
//
//    private static final String GUI_PACKAGE = "com.clinicmanager.gui..";
//
//    @ArchTest
//    static final ArchRule modelLayerShouldNotDependOnGui = noClasses()
//            .that().resideInAPackage("com.clinicmanager.model..")
//            .should().dependOnClassesThat().resideInAnyPackage(GUI_PACKAGE);
//
//    @ArchTest
//    static final ArchRule serviceLayerShouldNotDependOnGui = noClasses()
//            .that().resideInAPackage("com.clinicmanager.service..")
//            .should().dependOnClassesThat().resideInAnyPackage(GUI_PACKAGE);
//
//    @ArchTest
//    static final ArchRule timeLayerShouldNotDependOnGui = noClasses()
//            .that().resideInAPackage("com.clinicmanager.time..")
//            .should().dependOnClassesThat().resideInAnyPackage(GUI_PACKAGE);
// }
