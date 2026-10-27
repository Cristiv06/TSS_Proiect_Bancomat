package ro.tss.bancomat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ro.tss.bancomat.Date.*;

/**
 * 4) Set de teste care satisface criteriul MC/DC.
 *
 * <p>Deciziile metodei (numerotarea nodurilor din graf):
 * <ul>
 *   <li>[1] D1 = A || B || C, cu A: suma &lt; 10, B: suma &gt; 5000, C: suma % 10 != 0</li>
 *   <li>[3] D2 = A || (B &amp;&amp; C), cu A: suma &lt;= sold, B: premium, C: suma &lt;= sold + 1000
 *       (ramura de eroare se ia cand D2 este fals)</li>
 *   <li>[5] D3 = premium</li>
 *   <li>[7] D4 = retrasAzi + suma &gt; limita</li>
 *   <li>[10] D5 = A &amp;&amp; B, cu A: i &lt; 4, B: rest &gt; 0</li>
 *   <li>[11] D6 = nr &gt; stoc[i]</li>
 *   <li>[14] D7 = rest != 0</li>
 * </ul>
 * Perechile de teste care arata influenta independenta a fiecarei conditii sunt
 * descrise in DOCUMENTATIE.md, sectiunea 4.
 */
@DisplayName("4 - MC/DC")
class MCDCTest {

    private final Bancomat bancomat = new Bancomat();

    @Test
    @DisplayName("M1: D1=(F,F,F) D2=(T,F,T) D3=F D4=F D5=(T,T)/(T,F) D6=F D7=F -> SUCCES")
    void m1() {
        assertEquals(succes(0, 1, 0, 0), bancomat.retrage(100, 1000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("M2: D1=(T,F,F) -> SUMA_INVALIDA")
    void m2() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(0, 1000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("M3: D1=(F,T,F) -> SUMA_INVALIDA")
    void m3() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(6000, 1000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("M4: D1=(F,F,T) -> SUMA_INVALIDA")
    void m4() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), bancomat.retrage(15, 1000, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("M5: D2=(F,F,T) -> FONDURI_INSUFICIENTE")
    void m5() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(500, 100, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("M6: D2=(F,T,T) D3=T -> SUCCES")
    void m6() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 100, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("M7: D2=(F,T,F) -> FONDURI_INSUFICIENTE")
    void m7() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(1500, 100, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("M8: D4=T -> LIMITA_DEPASITA")
    void m8() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(100, 1000, 4950, false, stocPlin()));
    }

    @Test
    @DisplayName("M9: D5=(F,T) D6=T D7=T -> BANCNOTE_INSUFICIENTE")
    void m9() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(100, 1000, 0, false, stoc(0, 0, 0, 0)));
    }
}
