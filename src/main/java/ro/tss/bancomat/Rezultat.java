package ro.tss.bancomat;

import java.util.Arrays;

/**
 * Rezultatul unei retrageri: starea si, in caz de SUCCES, numarul de bancnote
 * eliberate pentru fiecare denominatie din {@link Bancomat#DENOMINATII}.
 * Pentru orice alta stare, vectorul de bancnote contine doar zerouri.
 */
public final class Rezultat {

    private final Stare stare;
    private final int[] bancnote;

    private Rezultat(Stare stare, int[] bancnote) {
        this.stare = stare;
        this.bancnote = bancnote.clone();
    }

    public static Rezultat succes(int[] bancnote) {
        return new Rezultat(Stare.SUCCES, bancnote);
    }

    public static Rezultat eroare(Stare stare) {
        return new Rezultat(stare, new int[Bancomat.DENOMINATII.length]);
    }

    public Stare getStare() {
        return stare;
    }

    public int[] getBancnote() {
        return bancnote.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rezultat)) {
            return false;
        }
        Rezultat other = (Rezultat) o;
        return stare == other.stare && Arrays.equals(bancnote, other.bancnote);
    }

    @Override
    public int hashCode() {
        return 31 * stare.hashCode() + Arrays.hashCode(bancnote);
    }

    @Override
    public String toString() {
        return stare + " " + Arrays.toString(bancnote);
    }
}
