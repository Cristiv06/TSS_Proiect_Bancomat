package ro.tss.bancomat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ro.tss.bancomat.Date.*;

/**
 * 1b) Analiza valorilor de frontiera (boundary value analysis).
 *
 * <p>Pentru fiecare clasa de echivalenta de la 1a se testeaza valorile de pe frontiera
 * si cele imediat vecine (vezi docs/Documentatie_TSS.pdf, sectiunea 1b).
 */
@DisplayName("1b - Valori de frontiera")
class FrontieraTest {

    private final Bancomat bancomat = new Bancomat();

    private Rezultat standard(int suma) {
        return bancomat.retrage(suma, 10000, 0, false, stocPlin());
    }

    // ---- frontierele lui suma: 10 si 5000, multiplu de 10 ----

    @Test
    @DisplayName("suma = 0 -> SUMA_INVALIDA (multiplu de 10, sub minim)")
    void sumaZero() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), standard(0));
    }

    @Test
    @DisplayName("suma = 9 -> SUMA_INVALIDA")
    void sumaNoua() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), standard(9));
    }

    @Test
    @DisplayName("suma = 10 -> SUCCES (minimul)")
    void sumaMinima() {
        assertEquals(succes(0, 0, 0, 1), standard(10));
    }

    @Test
    @DisplayName("suma = 11 -> SUMA_INVALIDA (nu e multiplu de 10)")
    void sumaUnsprezece() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), standard(11));
    }

    @Test
    @DisplayName("suma = 4990 -> SUCCES (foloseste toate denominatiile)")
    void sumaSubMaxim() {
        assertEquals(succes(24, 1, 1, 4), standard(4990));
    }

    @Test
    @DisplayName("suma = 5000 -> SUCCES (maximul)")
    void sumaMaxima() {
        assertEquals(succes(25, 0, 0, 0), standard(5000));
    }

    @Test
    @DisplayName("suma = 5001 -> SUMA_INVALIDA")
    void sumaPesteMaximCuUnu() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), standard(5001));
    }

    @Test
    @DisplayName("suma = 5010 -> SUMA_INVALIDA (multiplu de 10, peste maxim)")
    void sumaPesteMaximCuZece() {
        assertEquals(eroare(Stare.SUMA_INVALIDA), standard(5010));
    }

    // ---- frontiera suma <= sold (client standard) ----

    @Test
    @DisplayName("standard: sold = suma -> SUCCES")
    void standardSoldEgalSuma() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 500, 0, false, stocPlin()));
    }

    @Test
    @DisplayName("standard: sold = suma - 1 -> FONDURI_INSUFICIENTE")
    void standardSoldSubSuma() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(500, 499, 0, false, stocPlin()));
    }

    // ---- frontiera suma <= sold + 1000 (client premium) ----

    @Test
    @DisplayName("premium: suma = sold + 1000 -> SUCCES")
    void premiumDescoperitMaxim() {
        assertEquals(succes(7, 1, 0, 0), bancomat.retrage(1500, 500, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("premium: suma = sold + 1001 -> FONDURI_INSUFICIENTE")
    void premiumDescoperitDepasit() {
        assertEquals(eroare(Stare.FONDURI_INSUFICIENTE), bancomat.retrage(1500, 499, 0, true, stocPlin()));
    }

    @Test
    @DisplayName("premium: sold = 0, suma = 1000 -> SUCCES")
    void premiumSoldZero() {
        assertEquals(succes(5, 0, 0, 0), bancomat.retrage(1000, 0, 0, true, stocPlin()));
    }

    // ---- frontiera limitei zilnice: 5000 standard, 10000 premium ----

    @Test
    @DisplayName("standard: retrasAzi + suma = 5000 -> SUCCES")
    void standardLimitaAtinsa() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 10000, 4500, false, stocPlin()));
    }

    @Test
    @DisplayName("standard: retrasAzi + suma = 5001 -> LIMITA_DEPASITA")
    void standardLimitaDepasita() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(500, 10000, 4501, false, stocPlin()));
    }

    @Test
    @DisplayName("premium: retrasAzi + suma = 5500 -> SUCCES (peste limita standard)")
    void premiumPesteLimitaStandard() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 10000, 5000, true, stocPlin()));
    }

    @Test
    @DisplayName("premium: retrasAzi + suma = 10000 -> SUCCES")
    void premiumLimitaAtinsa() {
        assertEquals(succes(2, 1, 0, 0), bancomat.retrage(500, 10000, 9500, true, stocPlin()));
    }

    @Test
    @DisplayName("premium: retrasAzi + suma = 10001 -> LIMITA_DEPASITA")
    void premiumLimitaDepasita() {
        assertEquals(eroare(Stare.LIMITA_DEPASITA), bancomat.retrage(500, 10000, 9501, true, stocPlin()));
    }

    // ---- frontiera stocului ----

    @Test
    @DisplayName("stoc exact cat trebuie pentru 380 -> SUCCES")
    void stocExact() {
        assertEquals(succes(1, 1, 1, 3), bancomat.retrage(380, 10000, 0, false, stoc(1, 1, 1, 3)));
    }

    @Test
    @DisplayName("stoc cu o bancnota de 10 lipsa pentru 380 -> BANCNOTE_INSUFICIENTE")
    void stocCuOBancnotaLipsa() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(380, 10000, 0, false, stoc(1, 1, 1, 2)));
    }

    @Test
    @DisplayName("stoc gol -> BANCNOTE_INSUFICIENTE")
    void stocGol() {
        assertEquals(eroare(Stare.BANCNOTE_INSUFICIENTE), bancomat.retrage(380, 10000, 0, false, stoc(0, 0, 0, 0)));
    }

    @Test
    @DisplayName("doar bancnote de 10 -> SUCCES cu 38 x 10")
    void doarBancnoteMici() {
        assertEquals(succes(0, 0, 0, 38), bancomat.retrage(380, 10000, 0, false, stoc(0, 0, 0, 38)));
    }

    @Test
    @DisplayName("lipsa bancnote de 200 -> SUCCES cu 2 x 100")
    void inlocuireDenominatie() {
        assertEquals(succes(0, 2, 0, 0), bancomat.retrage(200, 10000, 0, false, stoc(0, 2, 0, 0)));
    }
}
