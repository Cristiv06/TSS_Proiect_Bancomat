package ro.tss.bancomat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ro.tss.bancomat.Date.*;

/**
 * 1c) Graful cauza-efect (cause-effect graphing).
 *
 * <p>Cauze:
 * C1 = 10 &lt;= suma &lt;= 5000, C2 = suma % 10 == 0, C3 = suma &lt;= sold, C4 = premium,
 * C5 = suma &lt;= sold + 1000, C6 = retrasAzi + suma &lt;= 5000,
 * C7 = retrasAzi + suma &lt;= 10000, C8 = stocul permite compunerea sumei.
 *
 * <p>Efecte:
 * E1 = SUMA_INVALIDA, E2 = FONDURI_INSUFICIENTE, E3 = LIMITA_DEPASITA,
 * E4 = BANCNOTE_INSUFICIENTE, E5 = SUCCES.
 *
 * <p>Fiecare test corespunde unei coloane T1..T11 din tabelul de decizie
 * (docs/Documentatie_TSS.pdf, sectiunea 1c).
 */
@DisplayName("1c - Graf cauza-efect")
class CauzaEfectTest {

    private final Bancomat bancomat = new Bancomat();

    @Test
    @DisplayName("T1: !C1 -> E1")
    void t1() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(0, 10000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("T2: C1 !C2 -> E1")
    void t2() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(155, 10000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("T3: C1 C2 !C3 !C4 -> E2")
    void t3() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(500, 100, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("T4: C1 C2 !C3 C4 !C5 -> E2")
    void t4() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(2000, 100, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("T5: C1 C2 C3 !C4 !C6 -> E3")
    void t5() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(500, 2000, 4800, false, stocPlin()));
    }

    @Test
    @DisplayName("T6: C1 C2 C3 C4 !C6 !C7 -> E3")
    void t6() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(500, 2000, 9800, true, stocPlin()));
    }

    @Test
    @DisplayName("T7: C1 C2 C3 !C4 C6 !C8 -> E4")
    void t7() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(500, 2000, 0, false, stocInsuficient()));
    }

    @Test
    @DisplayName("T8: C1 C2 C3 !C4 C6 C8 -> E5")
    void t8() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 2000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("T9: C1 C2 !C3 C4 C5 C6 C8 -> E5 (descoperit)")
    void t9() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 100, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("T10: C1 C2 C3 C4 !C6 C7 C8 -> E5 (limita premium)")
    void t10() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 2000, 6000, true, stocPlin()));
    }

    @Test
    @DisplayName("T11: C1 C2 !C3 C4 C5 C6 !C8 -> E4")
    void t11() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(500, 100, 0, true, stocInsuficient()));
    }
}
