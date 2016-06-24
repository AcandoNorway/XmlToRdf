
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

import java.io.InputStream;

/**
 * Created by veronika on 4/26/16.
 * <p>
 * A class describing the vocabulary of arkivstruktur.
 */
public class Arkiv {

    public static final String BASE_URI = "http://www.arkivverket.no/standarder/noark5/arkivstruktur";
    public static final String NS = BASE_URI + "/";
    public static final String PREFIX = "arkiv";
    private static final String ONTOLOGY = "ontology.ttl";
    private static ValueFactory factory = SimpleValueFactory.getInstance();
    /**
     * CLASSES
     */

    public static final IRI Basisregistrering = factory.createIRI(NS + "Basisregistrering");
    public static final IRI Registrering = factory.createIRI(NS + "Registrering");
    public static final IRI Journalpost = factory.createIRI(NS + "Journalpost");
    public static final IRI Journalpoststatus = factory.createIRI(NS + "Journalpoststatus");
    public static final IRI Dokumentbeskrivelse = factory.createIRI(NS + "Dokumentbeskrivelse");
    public static final IRI Dokumentobjekt = factory.createIRI(NS + "Dokumentobjekt");
    public static final IRI Korrespondansepart = factory.createIRI(NS + "Korrespondansepart");
    public static final IRI Mappe = factory.createIRI(NS + "Mappe");
    public static final IRI Saksmappe = factory.createIRI(NS + "Saksmappe");
    public static final IRI skjermingOpphoererDato = factory.createIRI(NS + "skjermingOpphoererDato");
    public static final IRI skjermingsvarighet = factory.createIRI(NS + "skjermingsvarighet");
    public static final IRI saksaar = factory.createIRI(NS + "saksaar");
    public static final IRI moetedato = factory.createIRI(NS + "moetedato");
    public static final IRI ElektroniskSignatur = factory.createIRI(NS + "ElektroniskSignatur");
    public static final IRI elektroniskSignatur = factory.createIRI(NS + "elektroniskSignatur");
    public static final IRI kassasjon = factory.createIRI(NS + "kassasjon");
    public static final IRI Kassasjon = factory.createIRI(NS + "Kassasjon");
    public static final IRI kassasjonsdato = factory.createIRI(NS + "kassasjonsdato");
    public static final IRI sakspart = factory.createIRI(NS + "sakspart");
    public static final IRI Sakspart = factory.createIRI(NS + "Sakspart");
    /**
     * PREDICATES
     */

    // Registrering
    public static final IRI systemID = factory.createIRI(NS + "systemID");
    public static final IRI opprettetDato = factory.createIRI(NS + "opprettetDato");
    public static final IRI avsluttetDato = factory.createIRI(NS + "avsluttetDato");
    public static final IRI opprettetAv = factory.createIRI(NS + "opprettetAv");
    public static final IRI arkivertDato = factory.createIRI(NS + "arkivertDato");
    public static final IRI arkivertAv = factory.createIRI(NS + "arkivertAv");
    public static final IRI mottattDato = factory.createIRI(NS + "mottattDato");
    public static final IRI sendtDato = factory.createIRI(NS + "sendtDato");
    // Basisregistrering
    public static final IRI registreringsID = factory.createIRI(NS + "registreringsID");
    public static final IRI tittel = factory.createIRI(NS + "tittel");
    // Journalpost
    public static final IRI journalaar = factory.createIRI(NS + "journalaar");
    public static final IRI journalposttype = factory.createIRI(NS + "journalposttype");
    public static final IRI journalstatus = factory.createIRI(NS + "journalstatus");
    public static final IRI journaldato = factory.createIRI(NS + "journaldato");
    public static final IRI journalsekvensnummer = factory.createIRI(NS + "journalsekvensnummer");
    public static final IRI journalpostnummer = factory.createIRI(NS + "journalpostnummer");
    public static final IRI antallVedlegg = factory.createIRI(NS + "antallVedlegg");
    public static final IRI verifisertDato = factory.createIRI(NS + "verifisertDato");
    public static final IRI konverteringskommentar = factory.createIRI(NS + "konverteringskommentar");
    public static final IRI konverteringsverktoey = factory.createIRI(NS + "konverteringsverktoey");
    public static final IRI klasseID = factory.createIRI(NS + "klasseID");
    public static final IRI mappeID = factory.createIRI(NS + "mappeID");
    public static final IRI arkivskaperID = factory.createIRI(NS + "arkivskaperID");
    public static final IRI moetenummer = factory.createIRI(NS + "moetenummer");
    public static final IRI sakspartID = factory.createIRI(NS + "sakspartID");
    public static final IRI sakssekvensnummer = factory.createIRI(NS + "sakssekvensnummer");
    public static final IRI beskrivelse = factory.createIRI(NS + "beskrivelse");
    public static final IRI noekkelord = factory.createIRI(NS + "noekkelord");
    public static final IRI arkivskaperNavn = factory.createIRI(NS + "arkivskaperNavn");
    public static final IRI forfatter = factory.createIRI(NS + "forfatter");
    public static final IRI offentligTittel = factory.createIRI(NS + "offentligTittel");
    public static final IRI arkivstatus = factory.createIRI(NS + "arkivstatus");
    public static final IRI arkivdelstatus = factory.createIRI(NS + "arkivdelstatus");
    public static final IRI saksstatus = factory.createIRI(NS + "saksstatus");
    public static final IRI dokumentstatus = factory.createIRI(NS + "dokumentstatus");
    public static final IRI moeteregistreringsstatus = factory.createIRI(NS + "moeteregistreringsstatus");
    public static final IRI presedensstatus = factory.createIRI(NS + "presedensstatus");
    public static final IRI dokumenttype = factory.createIRI(NS + "dokumenttype");
    public static final IRI merknadstype = factory.createIRI(NS + "merknadstype");
    public static final IRI moeteregistreringstype = factory.createIRI(NS + "moeteregistreringstype");
    public static final IRI klassifikasjonstype = factory.createIRI(NS + "klassifikasjonstype");
    public static final IRI korrespondanseparttype = factory.createIRI(NS + "korrespondanseparttype");
    public static final IRI moetesakstype = factory.createIRI(NS + "moetesakstype");
    public static final IRI slettingstype = factory.createIRI(NS + "slettingstype");
    public static final IRI utlaantDato = factory.createIRI(NS + "utlaantDato");
    public static final IRI forfallsdato = factory.createIRI(NS + "forfallsdato");
    public static final IRI offentlighetsvurdertDato = factory.createIRI(NS + "offentlighetsvurdertDato");
    public static final IRI presedensDato = factory.createIRI(NS + "presedensDato");
    public static final IRI journalStartDato = factory.createIRI(NS + "journalStartDato");
    public static final IRI journalSluttDato = factory.createIRI(NS + "journalSluttDato");
    public static final IRI referanseForloeper = factory.createIRI(NS + "referanseForloeper");
    public static final IRI referanseArvtaker = factory.createIRI(NS + "referanseArvtaker");
    public static final IRI referanseArkivdel = factory.createIRI(NS + "referanseArkivdel");
    public static final IRI referanseSekundaerKlassifikasjon = factory.createIRI(NS + "referanseSekundaerKlassifikasjon");
    public static final IRI referanseTilMappe = factory.createIRI(NS + "referanseTilMappe");
    public static final IRI referanseTilRegistrering = factory.createIRI(NS + "referanseTilRegistrering");
    public static final IRI referanseAvskrivesAvJournalpost = factory.createIRI(NS + "referanseAvskrivesAvJournalpost");
    public static final IRI tilknyttetRegistreringSom = factory.createIRI(NS + "tilknyttetRegistreringSom");
    public static final IRI referanseDokumentfil = factory.createIRI(NS + "referanseDokumentfil");
    public static final IRI referanseTilKlasse = factory.createIRI(NS + "referanseTilKlasse");
    public static final IRI referanseForrigeMoete = factory.createIRI(NS + "referanseForrigeMoete");
    public static final IRI referanseNesteMoete = factory.createIRI(NS + "referanseNesteMoete");
    public static final IRI referanseTilMoeteregistrering = factory.createIRI(NS + "referanseTilMoeteregistrering");
    public static final IRI referanseFraMoeteregistrering = factory.createIRI(NS + "referanseFraMoeteregistrering");
    public static final IRI dokumentmedium = factory.createIRI(NS + "dokumentmedium");
    public static final IRI oppbevaringssted = factory.createIRI(NS + "oppbevaringssted");
    public static final IRI sakspartNavn = factory.createIRI(NS + "sakspartNavn");
    public static final IRI sakspartRolle = factory.createIRI(NS + "sakspartRolle");
    public static final IRI administrativEnhet = factory.createIRI(NS + "administrativEnhet");
    public static final IRI saksansvarlig = factory.createIRI(NS + "saksansvarlig");
    public static final IRI saksbehandler = factory.createIRI(NS + "saksbehandler");
    public static final IRI journalenhet = factory.createIRI(NS + "journalenhet");
    public static final IRI utlaantTil = factory.createIRI(NS + "utlaantTil");
    public static final IRI merknadstekst = factory.createIRI(NS + "merknadstekst");
    public static final IRI presedensHjemmel = factory.createIRI(NS + "presedensHjemmel");
    public static final IRI rettskildefaktor = factory.createIRI(NS + "rettskildefaktor");
    public static final IRI seleksjon = factory.createIRI(NS + "seleksjon");
    public static final IRI utvalg = factory.createIRI(NS + "utvalg");
    public static final IRI moetested = factory.createIRI(NS + "moetested");
    public static final IRI moetedeltakerNavn = factory.createIRI(NS + "moetedeltakerNavn");
    public static final IRI moetedeltakerFunksjon = factory.createIRI(NS + "moetedeltakerFunksjon");
    public static final IRI korrespondansepartNavn = factory.createIRI(NS + "korrespondansepartNavn");
    public static final IRI postadresse = factory.createIRI(NS + "postadresse");
    public static final IRI postnummer = factory.createIRI(NS + "postnummer");
    public static final IRI poststed = factory.createIRI(NS + "poststed");
    public static final IRI land = factory.createIRI(NS + "land");
    public static final IRI epostadresse = factory.createIRI(NS + "epostadresse");
    public static final IRI telefonnummer = factory.createIRI(NS + "telefonnummer");
    public static final IRI kontaktperson = factory.createIRI(NS + "kontaktperson");
    public static final IRI kassasjonsvedtak = factory.createIRI(NS + "kassasjonsvedtak");
    public static final IRI bevaringstid = factory.createIRI(NS + "bevaringstid");
    public static final IRI kassasjonshjemmel = factory.createIRI(NS + "kassasjonshjemmel");
    public static final IRI tilgangsrestriksjon = factory.createIRI(NS + "tilgangsrestriksjon");
    public static final IRI skjermingshjemmel = factory.createIRI(NS + "skjermingshjemmel");
    public static final IRI skjermingMetadata = factory.createIRI(NS + "skjermingMetadata");
    public static final IRI skjermingDokument = factory.createIRI(NS + "skjermingDokument");
    public static final IRI gradering = factory.createIRI(NS + "gradering");
    public static final IRI elektroniskSignaturSikkerhetsnivaa = factory.createIRI(NS + "elektroniskSignaturSikkerhetsnivaa");
    public static final IRI elektroniskSignaturVerifisert = factory.createIRI(NS + "elektroniskSignaturVerifisert");
    public static final IRI brukerNavn = factory.createIRI(NS + "brukerNavn");
    public static final IRI brukerRolle = factory.createIRI(NS + "brukerRolle");
    public static final IRI brukerstatus = factory.createIRI(NS + "brukerstatus");
    public static final IRI administrativEnhetNavn = factory.createIRI(NS + "administrativEnhetNavn");
    public static final IRI administrativEnhetsstatus = factory.createIRI(NS + "administrativEnhetsstatus");
    public static final IRI referanseOverordnetEnhet = factory.createIRI(NS + "referanseOverordnetEnhet");
    public static final IRI avsluttetAv = factory.createIRI(NS + "avsluttetAv");
    public static final IRI antallJournalposter = factory.createIRI(NS + "antallJournalposter");
    public static final IRI merknadsdato = factory.createIRI(NS + "merknadsdato");
    public static final IRI merknadRegistrertAv = factory.createIRI(NS + "merknadRegistrertAv");
    public static final IRI slettetDato = factory.createIRI(NS + "slettetDato");
    public static final IRI slettetAv = factory.createIRI(NS + "slettetAv");
    public static final IRI konvertertDato = factory.createIRI(NS + "konvertertDato");
    public static final IRI konvertertAv = factory.createIRI(NS + "konvertertAv");
    public static final IRI tilknyttetAv = factory.createIRI(NS + "tilknyttetAv");
    public static final IRI avskrevetAv = factory.createIRI(NS + "avskrevetAv");
    public static final IRI avskrivningsmaate = factory.createIRI(NS + "avskrivningsmaate");
    public static final IRI verifisertAv = factory.createIRI(NS + "verifisertAv");
    public static final IRI graderingsdato = factory.createIRI(NS + "graderingsdato");
    public static final IRI gradertAv = factory.createIRI(NS + "gradertAv");
    public static final IRI nedgraderingsdato = factory.createIRI(NS + "nedgraderingsdato");
    public static final IRI nedgradertAv = factory.createIRI(NS + "nedgradertAv");
    public static final IRI presedensGodkjentDato = factory.createIRI(NS + "presedensGodkjentDato");
    public static final IRI presedensGodkjentAv = factory.createIRI(NS + "presedensGodkjentAv");
    public static final IRI kassertDato = factory.createIRI(NS + "kassertDato");
    public static final IRI kassertAv = factory.createIRI(NS + "kassertAv");
    public static final IRI flytTil = factory.createIRI(NS + "flytTil");
    public static final IRI flytMottattDato = factory.createIRI(NS + "flytMottattDato");
    public static final IRI flytSendtDato = factory.createIRI(NS + "flytSendtDato");
    public static final IRI flytStatus = factory.createIRI(NS + "flytStatus");
    public static final IRI flytMerknad = factory.createIRI(NS + "flytMerknad");
    public static final IRI flytFra = factory.createIRI(NS + "flytFra");
    public static final IRI referanseArkivenhet = factory.createIRI(NS + "referanseArkivenhet");
    public static final IRI referanseMetadata = factory.createIRI(NS + "referanseMetadata");
    public static final IRI endretDato = factory.createIRI(NS + "endretDato");
    public static final IRI endretAv = factory.createIRI(NS + "endretAv");
    public static final IRI tidligereVerdi = factory.createIRI(NS + "tidligereVerdi");
    public static final IRI nyVerdi = factory.createIRI(NS + "nyVerdi");
    public static final IRI variantformat = factory.createIRI(NS + "variantformat");
    public static final IRI format = factory.createIRI(NS + "format");
    public static final IRI formatDetaljer = factory.createIRI(NS + "formatDetaljer");
    public static final IRI sjekksum = factory.createIRI(NS + "sjekksum");
    public static final IRI sjekksumAlgoritme = factory.createIRI(NS + "sjekksumAlgoritme");
    public static final IRI filstoerrelse = factory.createIRI(NS + "filstoerrelse");
    public static final IRI konvertertFraFormat = factory.createIRI(NS + "konvertertFraFormat");
    public static final IRI konvertertTilFormat = factory.createIRI(NS + "konvertertTilFormat");
    public static final IRI dokumentbeskrivelse = factory.createIRI(NS + "dokumentbeskrivelse");
    public static final IRI dokumentetsDato = factory.createIRI(NS + "dokumentetsDato");
    public static final IRI dokumentobjekt = factory.createIRI(NS + "dokumentobjekt");
    public static final IRI korrespondansepart = factory.createIRI(NS + "korrespondansepart");
    public static final IRI parent = factory.createIRI(NS + "parent");
    public static final IRI versjonsnummer = factory.createIRI(NS + "versjonsnummer");
    public static final IRI dokumentnummer = factory.createIRI(NS + "dokumentnummer");
    public static final IRI tilknyttetDato = factory.createIRI(NS + "tilknyttetDato");
    public static final IRI arkivperiodeSluttDato = factory.createIRI(NS + "arkivperiodeSluttDato");
    public static final IRI avskrivningsdato = factory.createIRI(NS + "avskrivningsdato");
    public static final IRI arkivperiodeStartDato = factory.createIRI(NS + "arkivperiodeStartDato");
    public static final IRI saksdato = factory.createIRI(NS + "saksdato");
    public static final IRI avskrivning = factory.createIRI(NS + "avskrivning");
    public static final IRI Avskrivning = factory.createIRI(NS + "Avskrivning");

    public static final IRI skjerming = factory.createIRI(NS + "skjerming");
    public static final IRI Skjerming = factory.createIRI(NS + "Skjerming");
    public static final IRI Arkiv = factory.createIRI(NS + "Arkiv");
    public static final IRI Moetedeltaker = factory.createIRI(NS + "Moetedeltaker");

    public static final IRI Klasse = factory.createIRI(NS + "Klasse");
    public static final IRI Klassifikasjonssystem = factory.createIRI(NS + "Klassifikasjonssystem");

    public static final IRI moetedeltaker = factory.createIRI(NS + "moetedeltaker");
    public static final IRI Arkivdel = factory.createIRI(NS + "Arkivdel");
    public static final IRI arkivskaper = factory.createIRI(NS + "arkivskaper");
    public static final IRI Arkivskaper = factory.createIRI(NS + "Arkivskaper");
    public static final IRI Moetemappe = factory.createIRI(NS + "Moetemappe");
    public static final IRI Moeteregistrering = factory.createIRI(NS + "Moeteregistrering");
    public static final IRI Merknad  = factory.createIRI(NS + "Merknad");
    public static final IRI merknad  = factory.createIRI(NS + "merknad");
    public static final IRI mottaker  = factory.createIRI(NS + "mottaker");
    public static final IRI avsender  = factory.createIRI(NS + "avsender");




    public static InputStream getOntology() {
        InputStream resourceAsStream = Arkiv.class.getClassLoader().getResourceAsStream(ONTOLOGY);

        return resourceAsStream;
    }
}
