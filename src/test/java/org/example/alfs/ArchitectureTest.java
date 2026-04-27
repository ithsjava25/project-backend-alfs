package org.example.alfs;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * Architecture Test
 *
 * <p>Verifies that the project follows a clean layered architecture and enforces separation of
 * concerns using ArchUnit.
 */
class ArchitectureTest {

  private final JavaClasses classes = new ClassFileImporter().importPackages("org.example.alfs");

  /**
   * Layer Rules
   *
   * <p>Enforces a clean layered architecture: Controller → Service → Repository. Controllers must
   * not access repositories directly, and services must not depend on controllers.
   */
  @Test
  void controllers_should_only_access_services_not_repositories() {
    noClasses()
        .that()
        .resideInAPackage("..controllers..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..repositories..")
        .check(classes);
  }

  @Test
  void services_should_not_depend_on_controllers() {
    noClasses()
        .that()
        .resideInAPackage("..services..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..controllers..")
        .check(classes);
  }

  /**
   * Repository Access Rule
   *
   * <p>Controllers, DTOs and mappers must not access repositories directly. Repository access
   * should go through the service layer.
   */
  @Test
  void controllers_dto_and_mapper_should_not_access_repositories() {
    noClasses()
        .that()
        .resideInAnyPackage("..controllers..", "..dto..", "..mapper..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..repositories..")
        .check(classes);
  }

  /**
   * Package Rules
   *
   * <p>Classes should be in correct packages.
   */
  @Test
  void controllers_should_be_in_controller_package() {
    classes()
        .that()
        .haveSimpleNameEndingWith("Controller")
        .should()
        .resideInAPackage("..controllers..")
        .check(classes);
  }

  /**
   * Service Package Rule
   *
   * <p>Services should reside in the services package. An exception is made for JwtService, which
   * is located in the security package.
   */
  @Test
  void services_should_be_in_services_package() {
    classes()
        .that()
        .haveSimpleNameEndingWith("Service")
        .should()
        .resideInAnyPackage("..services..", "..security..")
        .check(classes);
  }

  @Test
  void repositories_should_be_in_repositories_package() {
    classes()
        .that()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .resideInAPackage("..repositories..")
        .check(classes);
  }

  /**
   * Dtos should not depend on entities
   *
   * <p>Entities represent database structure and may change due to internal requirements, while
   * DTOs define what is exposed externally. If DTOs depend on entities, changes in the database can
   * unintentionally affect the API.
   */
  @Test
  void dto_should_not_depend_on_entities() {
    noClasses()
        .that()
        .resideInAPackage("..dto..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..entities..")
        .check(classes);
  }
}
