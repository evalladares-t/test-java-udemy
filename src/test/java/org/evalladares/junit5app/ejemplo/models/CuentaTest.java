package org.evalladares.junit5app.ejemplo.models;

import org.evalladares.junit5app.ejemplo.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CuentaTest {

  Cuenta cuenta;
  Cuenta cuenta1;

  @BeforeEach
  void initMethodTest() {
    System.out.println("Iniciando el método");
    this.cuenta = new Cuenta("Edward", new BigDecimal("10005.23434"));
    this.cuenta1 = new Cuenta("Joseph", new BigDecimal("2000.23434"));
  }

  @AfterEach
  void tearDown() {
    System.out.println("Finalizando el método de prueba");
  }

  @BeforeAll
  static void beforeAll() {
    System.out.println("Inicializando el test");
  }

  @AfterAll
  static void afterAll() {
    System.out.println("Finalizando el test");
  }

  @Test
  //@Disabled
  @DisplayName("Probando el nombre la cuenta corriente!")
  void testNombreCuenta() {

    String esperado = "Edward";
    String real = cuenta.getPersona();
    assertNotNull(real, () -> "La cuenta no puede ser nula");
    assertEquals(esperado, real, () -> "El nombre de la cuenta no es el que se esperaba");
    assertTrue(real.equals("Edward"), () -> "Nombre cuenta esperada debe ser igual a la real");

  }

  @Test
  void testSaldoCuenta() {
    assertEquals(10005.23434, cuenta.getSaldo().doubleValue());
    assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
  }

  @Test
  void testReferenciaCuenta() {
    assertNotEquals(cuenta, cuenta1);
  }

  @Test
  void testDebitoCuenta() {
    cuenta.debito(new BigDecimal("100"));

    assertNotNull(cuenta.getSaldo());
    assertEquals(9905, cuenta.getSaldo().intValue());

    assertEquals("9905.23434", cuenta.getSaldo().toPlainString());
  }

  @Test
  void testCreditoCuenta() {
    cuenta.credito(new BigDecimal("100"));

    assertNotNull(cuenta.getSaldo());
    assertEquals(10105, cuenta.getSaldo().intValue());

    assertEquals("10105.23434", cuenta.getSaldo().toPlainString());
  }

  @Test
  void testDineroInsuficienteExceptionCuenta() {
    Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
      cuenta.debito(new BigDecimal(15000));
    });

    String actual = exception.getMessage();
    String esperado = "Dinero Insuficiente";

    assertEquals(actual, esperado);
  }

  @Test
  void testTransferirDineroCuentas() {

    Banco banco = new Banco();
    banco.setNombre("ScotiaBank");
    banco.transferir(cuenta1, cuenta, new BigDecimal("500.23434"));

    assertEquals("1500.00000", cuenta1.getSaldo().toPlainString());
    assertEquals("10505.46868", cuenta.getSaldo().toPlainString());
  }

  @Test
    //@Disable
  void testRelacionBancoCuentas() {
    //fail
    Banco banco = new Banco();
    banco.setNombre("ScotiaBank");
    banco.addCuenta(cuenta);
    banco.addCuenta(cuenta1);

    assertAll(
            () -> {
              assertEquals(2, banco.getCuentas().size(),
                      () -> "El banco no tiene las cuentas esperadas");
            },
            () -> {
              assertEquals("ScotiaBank", cuenta.getBanco().getNombre(),
                      () -> "El nombre del banco no es el mismo");
            },
            () -> {
              assertEquals("ScotiaBank", cuenta1.getBanco().getNombre());
            },
            () -> {
              assertEquals("Edward", banco.getCuentas().stream().filter(c -> c.getPersona().equals("Edward")).findFirst().get().getPersona());
            },
            () -> {
              assertTrue(banco.getCuentas().stream().anyMatch(c -> c.getPersona().equals("Joseph")));
            }
    );

  }

  @Nested
  class TestOS {
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testSoloWindows() {
    }

    @Test
    @DisabledOnOs({OS.LINUX, OS.MAC})
    void testSoloLinuxMac() {
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testWindows() {
    }

  }

  @Nested
  class testJRE {
    @Test
    @EnabledOnJre(JRE.JAVA_11)
    void soloJDK19() {
    }

    @Test
    @DisabledOnJre(JRE.JAVA_19)
    void testNoJDK19() {
    }

  }

  @Nested
  class testSystemProperties {
    @Test
    void imprimirSystemProperties() {
      Properties properties = System.getProperties();
      properties.forEach((k, v) -> System.out.println(k + ":" + v));
    }

    @Test
    @EnabledIfSystemProperty(named = "java.version", matches = "11.0.12")
    void testJavaVersion() {
    }

    @Test
    @EnabledIfSystemProperty(named = "java.version", matches = ".*11.*")
    void testJavaVersionE() {
    }

    @Test
    @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
    void testSolo64() {
    }

    @Test
    @EnabledIfSystemProperty(named = "user.name", matches = "evall")
    void TestUserName() {
    }

  }

  @Test
  void testImprimirVariablesAmbiente() {
    Map<String, String> getenv = System.getenv();
    getenv.forEach((k, v) -> System.out.println(k + " = " + v));
  }

  @Test
  void testSaldoCuentaDev() {
    Boolean esDev = "dev".equals(System.getProperty("ENV"));
    assumeTrue(esDev);
    assertEquals(10005.23434, cuenta.getSaldo().doubleValue());
    assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
  }

  @Test
  void testSaldoCuentaDev2() {
    Boolean esDev = "dev".equals(System.getProperty("ENV"));
    assumingThat(esDev, () -> {
      assertEquals(10005.23434, cuenta.getSaldo().doubleValue());
      assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
      assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    });
  }

  @DisplayName("Probando Debito Cuenta Repetir")
  @RepeatedTest(value = 5, name = "{displayName} - Repetición número {currentRepetition} de {totalRepetitions}")
  void testSaldoCuentaRepetir(RepetitionInfo info) {
    if (info.getCurrentRepetition() == 3) {
      System.out.println("Estamos en la repetición " + info.getCurrentRepetition());
    }
    assertEquals(10005.23434, cuenta.getSaldo().doubleValue());
    assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
    assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
  }


  @ParameterizedTest(name = "numero {index} ejecutando con valor {argumentsWithNames}")
  @ValueSource(doubles = {100, 200, 300, 500, 700})
  void testDebitoCuentaPara(double monto) {
    cuenta.debito(new BigDecimal(monto));

    assertNotNull(cuenta.getSaldo());
    assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
  }

  @ParameterizedTest(name = "numero {index} ejecutando con valor {argumentsWithNames}")
  @CsvSource({"1,100", "2,200", "3,300"})
  void testDebitoCuentaCSVSource(String index, String monto) {
    System.out.println(index + " -> " + monto);
    cuenta.debito(new BigDecimal(monto));

    assertNotNull(cuenta.getSaldo());
    assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
  }

  @ParameterizedTest(name = "numero {index} ejecutando con valor {argumentsWithNames}")
  @CsvFileSource(resources = "/data.csv")
  void testDebitoCuentaCsvFileSource(String monto) {
    cuenta.debito(new BigDecimal(monto));
    assertNotNull(cuenta.getSaldo());
    assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
  }

  @ParameterizedTest(name = "numero {index} ejecutando con valor {argumentsWithNames}")
  @MethodSource("montoList")
  void testDebitoCuentaMethodSource(String monto) {
    cuenta.debito(new BigDecimal(monto));
    assertNotNull(cuenta.getSaldo());
    assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
  }

  static private List<String> montoList() {
    return Arrays.asList("100", "200", "800");
  }
}