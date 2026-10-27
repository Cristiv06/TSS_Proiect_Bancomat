import major.mutation.Config;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Analizor pentru mutantii generati de Major, care ruleaza teste JUnit 5.
 *
 * <p>Analizorul inclus in Major (task-ul Ant "junit") suporta doar JUnit 3/4. Major compileaza
 * insa toti mutantii intr-o singura clasa (mutant schemata): mutantul activ se alege la rulare
 * prin {@code major.mutation.Config.__M_NO} (0 = programul original). Acest program face acelasi
 * lucru ca analizorul Major, dar ruleaza testele prin JUnit Platform Launcher:
 * <ol>
 *   <li>ruleaza setul de teste pe original (trebuie sa treaca) si afla ce mutanti sunt acoperiti;</li>
 *   <li>pentru fiecare mutant acoperit seteaza {@code __M_NO} si ruleaza din nou testele;
 *       mutantul este omorat daca cel putin un test esueaza (assert, exceptie sau timeout).</li>
 * </ol>
 *
 * <p>Argumente: {@code <mutants.log> <director_iesire> <nume_set>=<Clasa1>,<Clasa2> ...}
 */
public class MajorAnaliza {

    private static final class Mutant {
        final int id;
        final String operator;
        final int linie;
        final String descriere;

        Mutant(int id, String operator, int linie, String descriere) {
            this.id = id;
            this.operator = operator;
            this.linie = linie;
            this.descriere = descriere;
        }
    }

    public static void main(String[] args) throws IOException {
        List<Mutant> mutanti = citesteMutanti(Paths.get(args[0]));
        Path iesire = Paths.get(args[1]);
        Files.createDirectories(iesire);
        System.out.println("Mutanti generati de Major: " + mutanti.size());

        try (PrintWriter rezumat = new PrintWriter(Files.newBufferedWriter(iesire.resolve("rezumat.csv"), StandardCharsets.UTF_8))) {
            rezumat.println("set,generati,acoperiti,omorati,vii,scor_brut");
            for (int a = 2; a < args.length; a++) {
                String[] parti = args[a].split("=", 2);
                analizeaza(parti[0], parti[1].split(","), mutanti, iesire, rezumat);
            }
        }
        // opreste eventualele fire ramase in bucle infinite dupa timeout
        System.exit(0);
    }

    private static void analizeaza(String set, String[] clase, List<Mutant> mutanti, Path iesire,
                                   PrintWriter rezumat) throws IOException {
        Config.__M_NO = 0;
        Config.reset();
        if (ruleaza(clase) > 0) {
            throw new IllegalStateException("Testele setului " + set + " esueaza pe programul original");
        }
        Set<Integer> acoperiti = new HashSet<>(Config.getCoverageList());

        int omorati = 0;
        int vii = 0;
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(iesire.resolve(set + ".csv"), StandardCharsets.UTF_8))) {
            csv.println("id,operator,linie,stare,descriere");
            for (Mutant m : mutanti) {
                String stare;
                if (!acoperiti.contains(m.id)) {
                    stare = "NEACOPERIT";
                    vii++;
                } else {
                    Config.__M_NO = m.id;
                    boolean omorat = ruleaza(clase) > 0;
                    Config.__M_NO = 0;
                    stare = omorat ? "OMORAT" : "VIU";
                    if (omorat) {
                        omorati++;
                    } else {
                        vii++;
                    }
                }
                csv.println(m.id + "," + m.operator + "," + m.linie + "," + stare + ",\"" + m.descriere.replace("\"", "\"\"") + "\"");
            }
        }
        double scor = 100.0 * omorati / mutanti.size();
        System.out.printf("%-16s generati=%d acoperiti=%d omorati=%d vii=%d scor brut=%.2f%%%n",
                set, mutanti.size(), acoperiti.size(), omorati, vii, scor);
        rezumat.printf(java.util.Locale.ROOT, "%s,%d,%d,%d,%d,%.2f%n",
                set, mutanti.size(), acoperiti.size(), omorati, vii, scor);
    }

    /** Ruleaza clasele de test si intoarce numarul de teste esuate. */
    private static long ruleaza(String[] clase) {
        LauncherDiscoveryRequest cerere = request()
                .selectors(java.util.Arrays.stream(clase).map(c -> selectClass(c.trim())).toArray(org.junit.platform.engine.DiscoverySelector[]::new))
                // un mutant poate produce o bucla infinita (de ex. stergerea lui i++)
                .configurationParameter("junit.jupiter.execution.timeout.default", "2 s")
                .configurationParameter("junit.jupiter.execution.timeout.thread.mode.default", "SEPARATE_THREAD")
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.execute(cerere, listener);
        TestExecutionSummary sumar = listener.getSummary();
        if (sumar.getTestsFoundCount() == 0) {
            throw new IllegalStateException("Nu am gasit teste in " + String.join(",", clase));
        }
        return sumar.getTotalFailureCount();
    }

    /** Format mutants.log: id:operator:original:inlocuire:metoda:linie:descriere */
    private static List<Mutant> citesteMutanti(Path log) throws IOException {
        List<Mutant> mutanti = new ArrayList<>();
        for (String rand : Files.readAllLines(log, StandardCharsets.UTF_8)) {
            if (rand.isBlank()) {
                continue;
            }
            String[] c = rand.split(":", 7);
            mutanti.add(new Mutant(Integer.parseInt(c[0]), c[1], Integer.parseInt(c[5]), c[6]));
        }
        return mutanti;
    }
}
