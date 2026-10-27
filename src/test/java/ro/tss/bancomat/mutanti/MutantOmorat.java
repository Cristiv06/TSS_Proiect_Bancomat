package ro.tss.bancomat.mutanti;

import ro.tss.bancomat.Bancomat;
import ro.tss.bancomat.Rezultat;
import ro.tss.bancomat.Stare;

/**
 * 6) Mutant NE-ECHIVALENT care este OMORAT de testul ales
 * ({@code FrontieraTest.sumaMinima}: retrage(10, 10000, 0, false, stocPlin) -> SUCCES [0, 0, 0, 1]).
 *
 * <p>Modificare (operator ROR, nodul [1]): {@code suma < SUMA_MIN} devine {@code suma <= SUMA_MIN}.
 * Pentru suma = 10 programul original intoarce SUCCES, mutantul intoarce SUMA_INVALIDA.
 */
public class MutantOmorat extends Bancomat {

    @Override
    public Rezultat retrage(int suma, int sold, int retrasAzi, boolean premium, int[] stoc) {
        if (suma <= SUMA_MIN || suma > SUMA_MAX || suma % 10 != 0) { // MUTATIE: < -> <=
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
