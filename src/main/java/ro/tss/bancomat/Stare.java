package ro.tss.bancomat;

/**
 * Starea finala a unei cereri de retragere (efectele din specificatie).
 */
public enum Stare {
    /** suma ceruta nu este in intervalul [10, 5000] sau nu este multiplu de 10 */
    SUMA_INVALIDA,
    /** soldul (plus descoperitul, pentru clientii premium) nu acopera suma */
    FONDURI_INSUFICIENTE,
    /** suma ar depasi limita zilnica de retragere */
    LIMITA_DEPASITA,
    /** suma nu poate fi compusa din bancnotele aflate in bancomat */
    BANCNOTE_INSUFICIENTE,
    /** retragere efectuata */
    SUCCES
}
