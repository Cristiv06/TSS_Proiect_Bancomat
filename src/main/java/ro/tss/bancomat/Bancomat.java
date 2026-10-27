package ro.tss.bancomat;
public class Bancomat {

    /** Denominatiile bancnotelor, in ordine descrescatoare. */
    public static final int[] DENOMINATII = {200, 100, 50, 10};

    public static final int SUMA_MIN = 10;
    public static final int SUMA_MAX = 5000;
    public static final int DESCOPERIT_PREMIUM = 1000;
    public static final int LIMITA_STANDARD = 5000;
    public static final int LIMITA_PREMIUM = 10000;

    /**
     * Proceseaza o cerere de retragere de numerar.
     *
     * <p>Preconditii (nu sunt verificate de metoda): {@code sold >= 0},
     * {@code retrasAzi >= 0}, {@code stoc} are 4 elemente nenegative.
     *
     * @param suma      suma ceruta, in lei
     * @param sold      soldul contului, in lei
     * @param retrasAzi suma retrasa deja in ziua curenta, in lei
     * @param premium   daca titularul are cont premium
     * @param stoc      numarul de bancnote disponibile pentru 200, 100, 50, 10 lei
     * @return starea retragerii si, la succes, bancnotele eliberate
     */
    public Rezultat retrage(int suma, int sold, int retrasAzi, boolean premium, int[] stoc) {
        if (suma < SUMA_MIN || suma > SUMA_MAX || suma % 10 != 0) {                    // [1]
            return Rezultat.eroare(Stare.SUMA_INVALIDA);                                 // [2]
        }
        if (!(suma <= sold || (premium && suma <= sold + DESCOPERIT_PREMIUM))) {         // [3]
            return Rezultat.eroare(Stare.FONDURI_INSUFICIENTE);                          // [4]
        }
        int limita = LIMITA_STANDARD;                                                    // [5]
        if (premium) {                                                                   // [5]
            limita = LIMITA_PREMIUM;                                                     // [6]
        }
        if (retrasAzi + suma > limita) {                                                 // [7]
            return Rezultat.eroare(Stare.LIMITA_DEPASITA);                               // [8]
        }
        int[] bancnote = new int[DENOMINATII.length];                                    // [9]
        int rest = suma;                                                                 // [9]
        int i = 0;                                                                       // [9]
        while (i < DENOMINATII.length && rest > 0) {                                     // [10]
            int nr = rest / DENOMINATII[i];                                              // [11]
            if (nr > stoc[i]) {                                                          // [11]
                nr = stoc[i];                                                            // [12]
            }
            bancnote[i] = nr;                                                            // [13]
            rest = rest - nr * DENOMINATII[i];                                           // [13]
            i++;                                                                         // [13]
        }
        if (rest != 0) {                                                                 // [14]
            return Rezultat.eroare(Stare.BANCNOTE_INSUFICIENTE);                         // [15]
        }
        return Rezultat.succes(bancnote);                                                // [16]
    }                                                                                    // [17]
}
