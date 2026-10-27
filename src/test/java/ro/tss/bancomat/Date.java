package ro.tss.bancomat;

/**
 * Date comune folosite de seturile de teste.
 */
final class Date {

    private Date() {
    }

    /** Stoc suficient pentru orice suma valida (100 de bancnote din fiecare denominatie). */
    static int[] stocPlin() {
        return new int[]{100, 100, 100, 100};
    }

    /** Stoc care nu permite compunerea lui 500: o singura bancnota de 200. */
    static int[] stocInsuficient() {
        return new int[]{1, 0, 0, 0};
    }

    static int[] stoc(int b200, int b100, int b50, int b10) {
        return new int[]{b200, b100, b50, b10};
    }

    static Rezultat succes(int b200, int b100, int b50, int b10) {
        return Rezultat.succes(new int[]{b200, b100, b50, b10});
    }

    static Rezultat eroare(Stare stare) {
        return Rezultat.eroare(stare);
    }
}
