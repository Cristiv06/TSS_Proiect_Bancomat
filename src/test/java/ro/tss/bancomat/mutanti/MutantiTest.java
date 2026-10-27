package ro.tss.bancomat.mutanti;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.tss.bancomat.Bancomat;
import ro.tss.bancomat.Rezultat;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Demonstratii pentru cerintele 5 si 6.
 */
@DisplayName("5, 6 - Mutanti scrisi manual")
class MutantiTest {

    private static final int[] STOC_PLIN = {100, 100, 100, 100};

    private final Bancomat original = new Bancomat();

    /** Intrarile testului ales pentru cerinta 6: FrontieraTest.sumaMinima. */
    private Rezultat testAles(Bancomat program) {
        return program.retrage(10, 10000, 0, false, STOC_PLIN.clone());
    }

    @Test
    @DisplayName("5: mutantul echivalent se comporta identic pe un domeniu larg de intrari")
    void mutantEchivalent() {
        Bancomat mutant = new MutantEchivalent();
        List<int[]> stocuri = List.of(
                new int[]{100, 100, 100, 100}, new int[]{0, 0, 0, 0}, new int[]{1, 0, 0, 0},
                new int[]{1, 1, 1, 3}, new int[]{0, 0, 0, 38}, new int[]{2, 0, 3, 1}, new int[]{0, 3, 1, 7});
        int[] solduri = {0, 499, 3000, 10000};
        int[] retrase = {0, 4500, 9500};

        int comparatii = 0;
        for (int suma = -20; suma <= 5020; suma++) {
            for (int sold : solduri) {
                for (int retrasAzi : retrase) {
                    for (boolean premium : new boolean[]{false, true}) {
                        for (int[] stoc : stocuri) {
                            assertEquals(
                                    original.retrage(suma, sold, retrasAzi, premium, stoc.clone()),
                                    mutant.retrage(suma, sold, retrasAzi, premium, stoc.clone()));
                            comparatii++;
                        }
                    }
                }
            }
        }
        System.out.println("Mutant echivalent: " + comparatii + " comparatii, nicio diferenta");
    }

    @Test
    @DisplayName("6: testul ales omoara MutantOmorat")
    void mutantOmorat() {
        assertNotEquals(testAles(original), testAles(new MutantOmorat()));
    }

    @Test
    @DisplayName("6: testul ales NU omoara MutantNeomorat, dar mutantul nu e echivalent")
    void mutantNeomorat() {
        Bancomat mutant = new MutantNeomorat();
        assertEquals(testAles(original), testAles(mutant));

        // suma = 5000 distinge mutantul de original
        assertNotEquals(
                original.retrage(5000, 10000, 0, false, STOC_PLIN.clone()),
                mutant.retrage(5000, 10000, 0, false, STOC_PLIN.clone()));
    }
}
