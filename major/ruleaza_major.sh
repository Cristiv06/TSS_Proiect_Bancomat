#!/usr/bin/env bash
# Mutation testing cu Major (cerinta 7.2).
# Se ruleaza din Git Bash, din radacina proiectului:   bash major/ruleaza_major.sh
#
# Necesita: Major 3.0.1 si JDK 11. Implicit sunt cautate in ~/tss-unelte;
# alte locatii se dau prin variabilele MAJOR_HOME si JAVA11_HOME.
set -euo pipefail

RADACINA=$(cd "$(dirname "$0")/.." && pwd)
MAJOR_HOME=${MAJOR_HOME:-$HOME/tss-unelte/major}
JAVA11_HOME=${JAVA11_HOME:-$(ls -d "$HOME"/tss-unelte/jdk-11* 2>/dev/null | head -1)}
IESIRE="$RADACINA/rapoarte/major"
LUCRU="$RADACINA/target/major"

# separatorul de classpath: ';' pe Windows, ':' in rest
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=';' ;; *) SEP=':' ;; esac
cale() { if [ "$SEP" = ';' ]; then cygpath -w "$1"; else echo "$1"; fi; }

# Maven: din PATH sau cel inclus in IntelliJ IDEA (care are si un JDK pentru Maven)
MVN=$(command -v mvn || true)
if [ -z "$MVN" ]; then
    IDEA=$(ls -d "/c/Program Files/JetBrains/IntelliJ IDEA"* 2>/dev/null | sort -r | head -1)
    MVN="$IDEA/plugins/maven-plugin/lib/maven3/bin/mvn"
    export JAVA_HOME=${JAVA_HOME:-$IDEA/jbr}
fi

echo "== 1. Compilare proiect si copiere dependinte de test"
(cd "$RADACINA" && "$MVN" -q -B clean test-compile dependency:copy-dependencies -DincludeScope=test)

export PATH="$JAVA11_HOME/bin:$PATH"
echo "== 2. Compilare fisier MML (javac: $(javac -version 2>&1))"
rm -rf "$LUCRU" && mkdir -p "$LUCRU/clase" "$LUCRU/analizor" "$IESIRE"
# mmlc scrie fisierul compilat langa sursa (<fisier>.mml.bin), deci se compileaza o copie
cp "$RADACINA/major/bancomat.mml" "$LUCRU/bancomat.mml"
(cd "$LUCRU" && "$MAJOR_HOME/bin/mmlc" "$(cale "$LUCRU/bancomat.mml")")

echo "== 3. Generare mutanti (Major ca plugin javac)"
SURSE="$RADACINA/src/main/java/ro/tss/bancomat"
(cd "$LUCRU" && "$MAJOR_HOME/bin/major" --mml "$(cale "$LUCRU/bancomat.mml.bin")" \
    -d "$(cale "$LUCRU/clase")" \
    "$(cale "$SURSE/Bancomat.java")" "$(cale "$SURSE/Rezultat.java")" "$(cale "$SURSE/Stare.java")")
cp "$LUCRU/mutants.log" "$IESIRE/mutants.log"

echo "== 4. Compilare analizor"
DEP="$(cale "$RADACINA/target/dependency")/*"
RT="$(cale "$MAJOR_HOME/lib/major-rt.jar")"
javac -d "$(cale "$LUCRU/analizor")" -cp "$DEP$SEP$RT" "$(cale "$RADACINA/major/MajorAnaliza.java")"

echo "== 5. Analiza mutantilor pentru fiecare set de teste"
CP="$(cale "$LUCRU/clase")$SEP$(cale "$RADACINA/target/test-classes")$SEP$DEP$SEP$RT$SEP$(cale "$LUCRU/analizor")"
P=ro.tss.bancomat
java -cp "$CP" MajorAnaliza "$(cale "$IESIRE/mutants.log")" "$(cale "$IESIRE")" \
    "1a_echivalenta=$P.EchivalentaTest" \
    "1b_frontiera=$P.FrontieraTest" \
    "1c_cauza_efect=$P.CauzaEfectTest" \
    "1_functionale=$P.EchivalentaTest,$P.FrontieraTest,$P.CauzaEfectTest" \
    "4_mcdc=$P.MCDCTest"

echo "Rezultatele sunt in $IESIRE"
