package fr.dla.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {

        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("fr.dla.app");

        noClasses()
            .that()
                .resideInAnyPackage("fr.dla.app.service..")
            .or()
                .resideInAnyPackage("fr.dla.app.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..fr.dla.app.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses);
    }
}
