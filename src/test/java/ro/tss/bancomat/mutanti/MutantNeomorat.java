package ro.tss.bancomat.mutanti;

import ro.tss.bancomat.Bancomat;
import ro.tss.bancomat.Rezultat;
import ro.tss.bancomat.Stare;

/**
 * 6) Mutant NE-ECHIVALENT care NU este omorat de testul ales
 * ({@code FrontieraTest.sumaMinima}: retrage(10, 10000, 0, false, stocPlin) -> SUCCES [0, 0, 0, 1]).
 *
 * <p>Modificare (operator ROR, nodul [1]): {@code suma > SUMA_MAX} devine {@code suma >= SUMA_MAX}.
 * Pentru suma = 10 ambele programe intorc SUCCES [0, 0, 0, 1], deci testul nu il distinge.
 * Mutantul nu este echivalent: pentru suma = 5000 originalul intoarce SUCCES,
 * iar mutantul SUMA_INVALIDA (testul {@code FrontieraTest.sumaMaxima} il omoara).
 */
public class MutantNeomorat extends Bancomat {

    @Override
    public Rezultat retrage(int suma, int sold, int retrasAzi, boolean premium, int[] stoc) {
        if (suma < SUMA_MIN || suma >= SUMA_MAX || suma % 10 != 0) { // MUTATIE: > -> >=
            return Rezultat.eroare(Stare.SUMA_INVALIDA);
        }
        if (!(suma <= sold || (premium && suma <= sold + DESCOPERIT_PREMIUM))) {
            return Rezultat.eroare(Stare.FONDURI_INSUFICIENTE);
        }
        int limita = LIMITA_STANDARD;
        if (premium) {
            limita = LIMITA_PREMIUM;
        }
        if (retrasAzi + suma > limita) {
            return Rezultat.eroare(Stare.LIMITA_DEPASITA);
        }
        int[] bancnote = new int[DENOMINATII.length];
        int rest = suma;
        int i = 0;
        while (i < DENOMINATII.length && rest > 0) {
            int nr = rest / DENOMINATII[i];
            if (nr > stoc[i]) {
                nr = stoc[i];
            }
            bancnote[i] = nr;
            rest = rest - nr * DENOMINATII[i];
            i++;
        }
        if (rest != 0) {
            return Rezultat.eroare(Stare.BANCNOTE_INSUFICIENTE);
        }
        return Rezultat.succes(bancnote);
    }
}
