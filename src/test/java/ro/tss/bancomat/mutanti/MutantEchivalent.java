package ro.tss.bancomat.mutanti;

import ro.tss.bancomat.Bancomat;
import ro.tss.bancomat.Rezultat;
import ro.tss.bancomat.Stare;

/**
 * 5) Mutant de ordinul 1 ECHIVALENT.
 *
 * <p>Modificare (operator ROR, nodul [14]): {@code if (rest != 0)} devine {@code if (rest > 0)}.
 *
 * <p>De ce este echivalent: {@code rest} porneste de la {@code suma > 0} si la fiecare pas scade cu
 * {@code nr * DENOMINATII[i]}, unde {@code nr <= rest / DENOMINATII[i]}, deci
 * {@code nr * DENOMINATII[i] <= rest}. Prin urmare {@code rest >= 0} mereu, iar pentru
 * {@code rest >= 0} conditiile {@code rest != 0} si {@code rest > 0} sunt identice.
 */
public class MutantEchivalent extends Bancomat {

    @Override
    public Rezultat retrage(int suma, int sold, int retrasAzi, boolean premium, int[] stoc) {
        if (suma < SUMA_MIN || suma > SUMA_MAX || suma % 10 != 0) {
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
        if (rest > 0) { // MUTATIE: rest != 0 -> rest > 0
            return Rezultat.eroare(Stare.BANCNOTE_INSUFICIENTE);
        }
        return Rezultat.succes(bancnote);
    }
}
