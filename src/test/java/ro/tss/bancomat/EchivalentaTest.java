package ro.tss.bancomat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ro.tss.bancomat.Date.*;

/**
 * 1a) Partitionare de echivalenta (equivalence partitioning).
 *
 * <p>Clase individuale (vezi docs/Documentatie_TSS.pdf, sectiunea 1a):
 * <ul>
 *   <li>S_1 = {10 &lt;= suma &lt;= 5000, suma % 10 == 0}, S_2 = {suma &lt; 10},
 *       S_3 = {suma &gt; 5000}, S_4 = {10 &lt;= suma &lt;= 5000, suma % 10 != 0}</li>
 *   <li>F_1 = {suma &lt;= sold}, F_2 = {sold &lt; suma &lt;= sold + 1000}, F_3 = {suma &gt; sold + 1000}</li>
 *   <li>P_1 = {premium}, P_2 = {standard}</li>
 *   <li>L_1 = {retrasAzi + suma &lt;= limita(P)}, L_2 = {retrasAzi + suma &gt; limita(P)}</li>
 *   <li>T_1 = {stocul permite compunerea sumei}, T_2 = {stocul nu permite}</li>
 * </ul>
 * Cate un test pentru fiecare clasa globala (combinatie fezabila).
 */
@DisplayName("1a - Partitionare de echivalenta")
class EchivalentaTest {

    private final Bancomat bancomat = new Bancomat();

    // ---- suma invalida: S_2, S_3, S_4 (celelalte intrari nu mai conteaza) ----

    @Test
    @DisplayName("C_2: suma < 10 -> SUMA_INVALIDA")
    void sumaSubMinim() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(-100, 10000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("C_3: suma > 5000 -> SUMA_INVALIDA")
    void sumaPesteMaxim() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(6000, 10000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("C_4: suma nu este multiplu de 10 -> SUMA_INVALIDA")
    void sumaNemultiplu() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(1234, 10000, 0, false, stocPlin()));
    }

    // ---- S_1, F_1 (soldul acopera suma) ----

    @Test
    @DisplayName("C_1111: F_1 P_2 L_1 T_1 -> SUCCES")
    void standardSoldSuficientInLimitaStocSuficient() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 2000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("C_1112: F_1 P_2 L_1 T_2 -> BANCNOTE_INSUFICIENTE")
    void standardSoldSuficientInLimitaStocInsuficient() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(500, 2000, 0, false, stocInsuficient()));
    }

    @Test
    @DisplayName("C_112: F_1 P_2 L_2 -> LIMITA_DEPASITA")
    void standardSoldSuficientPesteLimita() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(500, 2000, 4800, false, stocPlin()));
    }

    @Test
    @DisplayName("C_1211: F_1 P_1 L_1 T_1 -> SUCCES")
    void premiumSoldSuficientInLimitaStocSuficient() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 2000, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("C_1212: F_1 P_1 L_1 T_2 -> BANCNOTE_INSUFICIENTE")
    void premiumSoldSuficientInLimitaStocInsuficient() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(500, 2000, 0, true, stocInsuficient()));
    }

    @Test
    @DisplayName("C_122: F_1 P_1 L_2 -> LIMITA_DEPASITA")
    void premiumSoldSuficientPesteLimita() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(500, 2000, 9800, true, stocPlin()));
    }

    // ---- S_1, F_2 (suma acoperita doar de descoperitul premium) ----

    @Test
    @DisplayName("C_2211: F_2 P_1 L_1 T_1 -> SUCCES (descoperit)")
    void premiumDescoperitInLimitaStocSuficient() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 100, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("C_2212: F_2 P_1 L_1 T_2 -> BANCNOTE_INSUFICIENTE")
    void premiumDescoperitInLimitaStocInsuficient() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(500, 100, 0, true, stocInsuficient()));
    }

    @Test
    @DisplayName("C_222: F_2 P_1 L_2 -> LIMITA_DEPASITA")
    void premiumDescoperitPesteLimita() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(500, 100, 9800, true, stocPlin()));
    }

    @Test
    @DisplayName("C_21: F_2 P_2 -> FONDURI_INSUFICIENTE")
    void standardSoldInsuficient() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(500, 100, 0, false, stocPlin()));
    }

    // ---- S_1, F_3 (nici descoperitul nu acopera suma) ----

    @Test
    @DisplayName("C_32: F_3 P_1 -> FONDURI_INSUFICIENTE")
    void premiumPesteDescoperit() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(2000, 100, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("C_31: F_3 P_2 -> FONDURI_INSUFICIENTE")
    void standardPesteDescoperit() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(2000, 100, 0, false, stocPlin()));
    }
}
