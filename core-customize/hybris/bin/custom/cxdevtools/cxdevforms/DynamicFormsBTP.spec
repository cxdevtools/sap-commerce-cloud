# Dynamic Forms – Fachliche und technische Spezifikation für SAP BTP

Version: 1.1, 09.09.2026
Status: Konzeption; keine implementierte BTP-Anwendung
Referenz: parallele Commerce-Extraktion in cxdevforms
Normative Begriffe: MUSS = Abnahmeanforderung; SOLL = begründet abweichbar;
KANN = optionaler Ausbau. Neue Zielanforderungen sind nicht als Funktionen der
extrahierten Commerce-Extension zu verstehen.

## 1. Ziel, Systemgrenze und Lieferumfang

DF-01 Die Anwendung MUSS fachlichen Administratoren erlauben, Formulare ohne
Programmänderung anzulegen, zu lokalisieren, zu konfigurieren, zu prüfen und zu
veröffentlichen. Externe Frontends MUSS sie über eine stabile Formular-ID mit
maschinenlesbaren Definitionen versorgen. Die Anwendung MUSS eigenständig auf
SAP BTP betreibbar sein und DARF kein SAP-Commerce-Laufzeitsystem voraussetzen.

DF-02 Ein Modul MUSS Domänenlogik, Administration und externe API bereitstellen.
Die technische Aufteilung interner Packages darf keine fachlich unvollständigen
Deployments erzwingen. Eine Admin-Weboberfläche kann als eigenes statisches
Deployment ausgeliefert werden, bleibt aber Teil desselben Produkts und Vertrags.

DF-03 Die relationale Speicherung der Definitionen einschließlich Beziehungen,
Reihenfolgen, Übersetzungen und Regeln ist verpflichtend. PostgreSQL ist die
bevorzugte Zielvariante. MySQL ist eine zulässige Alternative nach dem in Abschnitt
10 beschriebenen Bereitstellungsnachweis. Ein JSON-Dokument als einzige Datenquelle
erfüllt die Anforderung nicht.

DF-04 Der erste Lieferumfang umfasst Modell, Admin-Oberfläche, Lese-API,
Validierungs- und Submission-API, Versionierung/Veröffentlichung, Import/Export,
Identität und Betrieb. Die validierte, dauerhafte Speicherung ausgefüllter Formulare
und ihre lesbare Administration sind verpflichtend (Abschnitt 9). Ein optional
aktivierbarer Event-Mesh-Adapter und ein standardmäßig deaktivierter E-Mail-Listener
mit Template-Fallback gehören ebenfalls zum Lieferumfang.

## 2. Nachgewiesener Commerce-Bestand und Abweichungen im BTP-Ziel

| Thema | Extrahierter Ist-Stand | BTP-Ziel |
| --- | --- | --- |
| Formular-ID | Global eindeutige DynamicForm.id | Stabil pro Mandant und Site; Version separat |
| Formulare | Typ, Titel, Beschreibung, Empfänger | Zusätzlich Status, Revisionen, UI-Konfiguration |
| Schritte | Optionale Feldgruppierung über stepId/stepTitle; kein eigener Schritttyp | Geordnete, lokalisierte Schritte mit Bedingungen |
| Felder | Geordnete Wurzelfelder, rekursive Unterfelder | Geordnete Platzierungen und zentrale Regeln |
| Auswahlwerte | ID, Label, Liste am Feld, n:m-Unterfelder | Lokale Options-IDs, Reihenfolge, geprüfte Referenzen |
| Aktiv | Nur Wurzelfelder werden beim Lesen gefiltert | Gilt für alle Felder, Optionen und Schritte |
| Bedingungen | Auswahlwert.childFields | Feld-/Schrittbedingungen mit definierter Semantik |
| Darstellung | Feldtyp; | Form-/Schritt-/Feld-Layout |
| Lesen | Alle Formulare / eine ID; keine Pagination | Publizierte Revisionen, Scope, Pagination, ETag |
| Nicht gefunden | HTTP 200, unbefülltes DTO | HTTP 404 |
| Lokalisierung | Commerce-Session und lokalisierte Attribute | Explizite Sprache und definierter Fallback |
| Schreiben | Backoffice / ImpEx | Gesicherte Admin-API und Admin-Oberfläche |
| Antworten | Separater Submission-Controller, Facade, Service, Itemtype und Commit-ClusterEvent | Persistente Annahme und Administration verpflichtend; Event Mesh optional aktivierbar |
| Empfänger | In DEFAULT/FULL öffentlich enthalten | Nur Admin-/Backend-Konfiguration |
| Datenhaltung | Commerce-Items und Relationen | Normalisierte relationale Datenbank |

Die Bestandsartefakte sind resources/cxdevforms-items.xml,
resources/cxdevforms-beans.xml, service, facade, populator,
controller/CxDynamicFormsController und CxDynamicFormsSubmissionsController und die zugehörigen Spring-/Backoffice-Dateien.
`DynamicFormType` ist dynamisch; `DynamicFormFieldType` ist ein festes Enum. Eine Backoffice-Wizard-Seite ist kein Frontend-Formularschritt.
`value` existiert nur im bisherigen Data-/DTO-Vertrag, nicht als persistierte
Attribute der Formulardefinition. Die BTP-Lese-API übernimmt sie nicht als fachliche
Konfigurationsattribute.

## 3. Rollen, Scope und Lebenszyklus

ADM-01 Die Rollen Leser, Redakteur, Freigeber und Administrator MUSS es getrennt
geben. Leser sehen Definitionen im Admin-Bereich; Redakteure bearbeiten Entwürfe;
Freigeber publizieren und archivieren; Administratoren verwalten Rollen, Sites,
Formulararten und Integrationskonfiguration. Berechtigungen sind im Backend zu
prüfen. Ein ausgeblendeter UI-Button reicht nicht aus.

ADM-02 Alle Zugriffe MUSS das Backend auf einen aus der vertrauenswürdigen
Identität abgeleiteten Mandanten beschränken. Site-Zugriff ist zusätzlich zu
prüfen. Eine Mandanten-ID aus einem Request allein gewährt keine Berechtigung.
Eine Installation KANN zunächst nur einen Mandanten konfigurieren; Tabellen und
API-Zugriff müssen dessen Trennung trotzdem durchgängig durchsetzen.

ADM-03 Eine Formulardefinition hat eine stabile fachliche ID. Änderungen erfolgen
in Revisionen mit Zuständen DRAFT, PUBLISHED, ARCHIVED. Pro Formular ist höchstens
eine Revision die aktuell veröffentlichte. Mehrere historische veröffentlichte
Revisionen dürfen für reproduzierbare Verarbeitung erhalten bleiben.

ADM-04 Veröffentlichte Revisionen sind unveränderlich. Bearbeiten erzeugt einen
neuen Entwurf aus einer Revision. Publizieren MUSS vollständig validieren und den
Zeiger auf die aktuelle Revision atomar umschalten. Fehlgeschlagene Validierung
lässt die bisher publizierte Revision unverändert. Wiederherstellen einer älteren
Revision erfolgt als neuer Entwurf mit neuer Versionsnummer und erneuter Freigabe.

ADM-05 Ein Entwurf darf unvollständig zwischengespeichert werden; kaputte
Referenzen und ungültige Datenbankzustände dürfen nicht gespeichert werden.
Fachliche Unvollständigkeit MUSS die Oberfläche anzeigen. Archivierung entfernt
Formulare aus der öffentlichen Liste. Ein harter Löschvorgang ist nur für
unveröffentlichte, unreferenzierte Entwürfe zulässig.

ADM-06 Änderungen verwenden optimistische Sperren. Admin-GET liefert ETag;
PATCH, DELETE, Publizieren und Umordnen benötigen If-Match. Fehlender Header
führt zu 428, veralteter Stand zu 412. Die UI MUSS Konflikte erklären und einen
Vergleich bzw. erneutes Laden anbieten, ohne lokale Eingaben still zu verwerfen.

## 4. Fachliches Modell

MOD-01 Formular: ID, Formularart, Standard-/unterstützte Sprachen, Titel,
Beschreibung, Site, Revision, Status, Darstellungsmodus und geordnete Schritte.
Titel und Beschreibungen sind übersetzbar. Empfängerlisten und dynamische
Empfängerstrategien sind separate private Backend-Konfiguration.

MOD-02 Formularart entspricht DynamicFormType: stabiler Code, lokalisierter Name,
Beschreibung und Aktivstatus. Neue Arten dürfen Administratoren ohne Deployment
anlegen. Deaktivierung verbietet neue Publikationen mit dieser Art, verändert
aber keine bereits publizierte Revision.

MOD-03 Schritt: innerhalb der Revision eindeutige ID, Position, lokalisierter
Titel und Beschreibung, Aktivstatus, optional eine Sichtbarkeitsbedingung,
Layout und geordnete Felder. Ein Formular MUSS mindestens einen aktiven Schritt
besitzen. Ein fachlich einschrittiges Formular benutzt denselben Datentyp.

MOD-04 Felddefinition: revisionslokal eindeutige ID, Feldtyp, Label,
Beschreibung, Platzhalter, Standardwert, required, hidden, active, Constraints
und gegebenenfalls Auswahlwerte. Formularübergreifende Wiederverwendung erfolgt
im ersten Lieferumfang durch Kopieren einer Vorlage; nachträgliche Änderungen
an der Vorlage dürfen keine publizierte Revision verändern.

MOD-05 Feldplatzierung: Referenz auf eine Felddefinition, Schritt, Position und
optional übergeordnete Feldplatzierung. Im ersten Lieferumfang darf eine
Felddefinition nur einmal in einer Revision platziert werden. Eine Platzierung
gehört genau einem Schritt. Verschachtelte Unterfelder bleiben im selben Schritt
wie ihr Elternfeld. Schrittübergreifende Steuerung geschieht durch Regeln auf dem
Zielschritt, nicht durch mehrfaches Einhängen desselben Feldes.

MOD-06 Auswahlwert: pro Feld eindeutige, stabile ID, lokalisierte Bezeichnung,
Position und Aktivstatus. Die ID ist der technische Wert; das Label darf sich
sprachabhängig ändern. Auswahlbedingungen referenzieren IDs, niemals Labels.
RADIO/SELECT speichern genau eine Options-ID, CHECKBOXES eine Liste eindeutiger
Options-IDs. CHECKBOX ist ein einzelner Boolean.

MOD-07 Typen und Antwortwerte:

| Feldtyp | Antwortdatentyp | Regeln / Darstellung |
| --- | --- | --- |
| TEXT | String | einzeilig, Längengrenzen |
| EMAIL | String | E-Mail-Format, Längengrenzen; kein Versandnachweis |
| HIDDEN | typisierter String | keine sichtbare Eingabe; kein Speicher für Geheimnisse |
| PASSWORD | String | maskiert; nie als persistierter Standardwert oder in Logs |
| COLOR | String | #RRGGBB, normalisierte Großschreibung |
| TEXTAREA | String | mehrzeilig, Längengrenzen |
| NUMBER | Dezimalzahl als JSON-String | dezimale, locale-unabhängige Syntax; Min/Max |
| RADIO | String | eine aktive Options-ID |
| CHECKBOX | Boolean | bei required MUSS true sein |
| CHECKBOXES | String[] | eindeutige aktive Options-IDs, deterministische Optionsreihenfolge |
| SELECT | String | eine aktive Options-ID; Mehrfachwahl über CHECKBOXES |
| FILE | String[] | Referenzen auf geprüfte Uploads, keine Binärwerte in Definitionen |
| DATE | String | ISO-Kalenderdatum YYYY-MM-DD ohne Zeitzone |
| WEEK | String | ISO-Woche YYYY-Www mit Prüfung gültiger Wochenzahl |

Null oder ein fehlender Eintrag bedeutet keine Antwort; leere Strings und leere
Listen gelten für required als leer. Leerzeichen allein gelten bei Text als leer.
0 ist bei NUMBER ein gültiger Wert. NUMBER verarbeitet der Server mit
Dezimalarithmetik, nicht als binären Gleitkommawert. Die deklarative Typprüfung
MUSS identisch in Validierungs- und Submit-API gelten.

MOD-08 Numerische Grenzen sind inklusiv; minValue <= maxValue. Längengrenzen sind
nichtnegative Ganzzahlen und zählen Unicode-Codepoints. minLength <= maxLength.
Constraints müssen zum Feldtyp passen. Labels und Platzhalter sind Plaintext.
Beschreibungen dürfen nur einen festgelegten, serverseitig bereinigten
Rich-Text-Umfang enthalten. Beliebiges HTML, JavaScript, CSS oder auszuführende
Ausdrücke dürfen nicht als Layoutkonfiguration angenommen werden.

MOD-09 Standardwerte müssen dem Antwortdatentyp entsprechen und im
Publikationscheck validiert werden. Versteckte Werte sind für den Browser
sichtbare Daten. Geheimnisse und private Integrationsparameter gehören niemals
in Definitionen. Defaults für PASSWORD und FILE sind unzulässig.

MOD-10 Deklarative UI-Einstellungen: ein- oder zweispaltiges Layout für große
Ansichten, immer eine Spalte auf kleinen Ansichten; Feldbreite 1 oder 2;
Schrittanzeige als fortlaufend oder Assistent; lokalisierte Weiter-/Zurück-/
Abschlussbeschriftung. Die Reihenfolge im Datenmodell bestimmt zugleich die
Lese- und Tastaturreihenfolge. Kein freier CSS-Klassenname als funktionale API.
Unbekannte Layoutoptionen MUSS ein Renderer mit einer dokumentierten, zugänglichen
Standarddarstellung behandeln; unbekannte Feldtypen müssen sichtbar als
inkompatibel gemeldet werden und dürfen keinen unvollständigen Submit erlauben.

## 5. Abhängigkeiten, Auswertung und Formularschritte

RULE-01 Bedingungen sind typisierte Daten. Die Sprache besteht aus Gruppen
ALL bzw. ANY und Prädikaten EQ, NEQ, IN, CONTAINS_ANY, IS_EMPTY, IS_NOT_EMPTY.
Ein Prädikat referenziert die ID eines Quellfeldes und gegebenenfalls Literale oder
Options-IDs. ALL verlangt alle, ANY mindestens eines der Prädikate; leere Gruppen
sind ungültig. Eine fehlende Bedingung bedeutet true. Kein eval, SQL oder Skript.

RULE-02 EQ/NEQ vergleichen einzelne typkompatible Werte; IN prüft einen skalaren
Wert gegen eine Literalliste; CONTAINS_ANY prüft CHECKBOXES gegen Options-IDs.
Ein nicht beantwortetes oder nicht effektives Quellfeld erfüllt nur IS_EMPTY;
alle anderen Prädikate einschließlich NEQ sind dann false. Vergleich ist
sprachunabhängig, ID-/String-Vergleich exakt und Groß-/Kleinschreibung beachtend.

RULE-03 Eine Platzierung kann visibleWhen und requiredWhen besitzen. Ein Schritt
kann visibleWhen besitzen. Effektive Pflicht = statisches required ODER
requiredWhen, jedoch nur für effektive Felder. Ein Feld ist effektiv, wenn sein
Schritt aktiv und sichtbar ist, das Feld aktiv ist, alle Elternplatzierungen
effektiv sind und seine Sichtbarkeitsbedingung erfüllt ist. hidden beeinflusst
nur die Darstellung, nicht die Effektivität; erforderliche HIDDEN-Felder müssen
durch einen zulässigen Default oder eine deklarierte Backend-Kontextquelle
versorgt werden. Fehlt eine solche Quelle, ist die Veröffentlichung abzulehnen.

RULE-04 Auswahlwert.childFields des Bestands wird durch sichtbare
Feldplatzierungen mit Bedingungen auf die Quelloption übersetzt. Mehrere Optionen,
die dasselbe Unterfeld aktivieren, werden durch ANY verbunden. Fachliches UND
zwischen unterschiedlichen Feldern benötigt eine ausdrückliche ALL-Gruppe.
Bei CHECKBOXES werden die wirksamen Unterfelder aller ausgewählten Optionen
vereinigt; gemeinsame Zielplatzierungen erscheinen einmal in ihrer Position.

RULE-05 Das Abhängigkeitsnetz MUSS azyklisch sein. Selbstbezüge, direkte und
indirekte Zyklen sind beim Publizieren mit einem Pfad der beteiligten Felder/
Schritte zurückzuweisen. Der Prüfgraph enthält sowohl explizite Regelkanten als
auch Schritt-/Elternfeld-Effektivität. So ist etwa ein Schritt unzulässig, dessen
Sichtbarkeit von einem Feld in diesem Schritt abhängt. Referenzen auf spätere
Schritte sind im ersten Lieferumfang unzulässig; innerhalb eines Schritts gilt
eine topologische Auswertung.

RULE-06 Bei jeder Antwortänderung MUSS die Engine in topologischer Reihenfolge
Effektivität und Pflicht neu bestimmen. Nicht effektive Antworten werden aus dem
aktiven Antwortzustand entfernt; sie dürfen keine weiteren Regeln auslösen und
nicht validiert oder übermittelt werden. Wird ein Feld wieder effektiv, beginnt
es mit seinem gültigen Default oder leer. Dieses Verhalten gilt identisch beim
Frontend und bei serverseitiger Validierung. Der Server vertraut keinem vom
Client gemeldeten Sichtbarkeitszustand.

RULE-07 Weiter führt zum nächsten sichtbaren Schritt und prüft alle effektiven
Felder des aktuellen Schritts. Zurück führt ohne Pflichtprüfung zum vorherigen
sichtbaren Schritt. Übersprungene Schritte zählen nicht zur sichtbaren
Fortschrittsanzeige. Entfällt der aktuelle Schritt nach einer Änderung, wird der
nächste noch sichtbare Schritt gewählt, sonst der letzte sichtbare Vorgänger.
Falls keine Schritte sichtbar sind, zeigt die UI einen lokalisierten Leerzustand
und deaktiviert den Abschluss. Ein Abschluss validiert alle effektiven Felder.

RULE-08 Als anfängliche Produktgrenzen gelten 50 Schritte, 500 Feldplatzierungen,
100 Optionen pro Feld, 10 Verschachtelungsebenen, 10 Regelgruppenebenen und 1 MiB
unkomprimierte Definition pro Revision. Das Backend MUSS Überschreitungen
verständlich zurückweisen. Diese Grenzen sind konfigurierbar und bei Änderung
mit passenden Lasttests zu belegen.

## 6. Verwaltung und Admin-Oberfläche

UI-01 Die Navigation MUSS mindestens Formulare, Formulararten, Einbettungen und
Administration enthalten. Ein Formular öffnet Übersicht, Schritte/Felder,
Bedingungen, Darstellung, Übersetzungen, Vorschau und Revisionen. Die Oberfläche
MUSS deutsche und englische Bedienbeschriftungen anbieten.

UI-02 Die Formularliste MUSS suchen, nach ID/Titel/Art/Status/Site filtern,
sortieren und paginieren. Nutzer können anlegen, kopieren, bearbeiten,
validieren, veröffentlichen, archivieren und Versionen vergleichen, jeweils
entsprechend ihrer Rolle. Die Formular-ID einer publizierten Definition ist
nicht umbenennbar; eine neue fachliche ID entsteht durch Kopieren.

UI-03 Schritte, Felder und Optionen MUSS man anlegen, bearbeiten, entfernen und
umordnen können. Umordnen braucht neben Drag-and-drop eine Tastaturalternative.
Entfernen referenzierter Elemente MUSS die betroffenen Regeln anzeigen und
entweder gemeinsam konsistent ändern oder den Vorgang blockieren. Es darf keine
stille kaskadierende Änderung einer anderen publizierten Revision geben.

UI-04 Die Bedingungsverwaltung MUSS passende Quellfelder, Operatoren und
Optionswerte anbieten, nur typkompatible Vergleiche zulassen und eine lesbare
Zusammenfassung anzeigen. Fehler zeigen die genaue Schritt-/Feld-/Regelstelle.
Die Ansicht MUSS erklären können, warum ein Feld oder Schritt in der Vorschau
sichtbar, unsichtbar oder erforderlich ist.

UI-05 Vorschau MUSS Entwürfe mit demselben Regelvertrag wie die externe API
rendern und Sprache, schmale/breite Ansicht sowie Testantworten wechseln können.
Vorschau darf weder Formulare publizieren noch echte Antworten verschicken.

UI-06 Übersetzungsansicht MUSS Vollständigkeit je Sprache zeigen. Fallbackfolge:
explizit angefragte unterstützte Sprache, deren konfigurierte Basissprache,
Formular-Standardsprache. Die Antwort nennt requestedLocale und resolvedLocale;
pro Attribut greift derselbe Fallback. Fehlende Pflichtübersetzungen der
Standardsprache blockieren Publikation. Stabile IDs werden niemals übersetzt.

UI-07 Lade-, Leer-, Fehler- und Berechtigungszustände sind für Listen und Editoren
zu implementieren. Nicht gespeicherte Änderungen brauchen eine Verlassensabfrage.
Tastaturbedienung, zugeordnete Labels, sichtbarer Fokus, Fehlerzusammenfassung
und Fokusführung bei Schrittwechsel sind Abnahmekriterien. Modale Darstellung
benötigt Fokusbegrenzung und Fokusrückgabe an das auslösende Element.

UI-08 Import/Export verwendet einen dokumentierten JSON-Vertrag mit
schemaVersion. Import bietet zunächst einen Dry-run mit Referenzprüfung,
Konfliktbericht und Zählung der Änderungen. Erst ein separater Commit schreibt
einen neuen Entwurf atomar. Vorhandene publizierte Revisionen werden nie ersetzt.
Unbekannte Schema-Versionen sind abzulehnen. Ein Export umfasst alle Sprachen,
Reihenfolgen und Regeln; private Integrationsdaten sind separat berechtigt.

## 7. Relationales Persistenzmodell

DB-01 Technische Primärschlüssel sind UUIDs; fachliche IDs bleiben eigene
Zeichenketten. Alle mandantenabhängigen Tabellen enthalten tenant_id.
Referenzen MUSS die Datenbank mit zusammengesetzten Fremdschlüsseln gegen
Mandanten-/Revisionsvermischung absichern. Ausnahmen sind ausschließlich
absichtlich mandantenweite Referenzen wie eine Formularart.

| Tabelle | Wesentliche Spalten und Beziehungen |
| --- | --- |
| tenant | id, code |
| site | id, tenant_id, code, default_locale |
| form_type | id, tenant_id, code, active |
| form_type_i18n | tenant_id, form_type_id, locale, name, description |
| form_definition | id, tenant_id, site_id, code, current_published_revision_id |
| form_revision | id, tenant_id, form_id, revision_no, status, type_id, default_locale, lock_version, created/updated/published_at/by |
| form_revision_locale | tenant_id, revision_id, locale |
| form_revision_i18n | tenant_id, revision_id, locale, title, description, next/back/finish_label |
| form_step | id, tenant_id, revision_id, code, position, active, layout_columns |
| form_step_i18n | tenant_id, revision_id, step_id, locale, title, description |
| field_definition | id, tenant_id, revision_id, code, field_type, active, hidden, required, min/max_value, min/max_length |
| field_i18n | tenant_id, revision_id, field_id, locale, label, description, placeholder |
| field_placement | id, tenant_id, revision_id, step_id, field_id, parent_placement_id nullable, position, column_span |
| field_option | id, tenant_id, revision_id, field_id, code, position, active |
| field_option_i18n | tenant_id, revision_id, option_id, locale, label |
| field_default | tenant_id, revision_id, field_id, scalar_string/decimal/boolean/date/week nullable |
| field_default_option | tenant_id, revision_id, field_id, option_id, position |
| rule | id, tenant_id, revision_id, target_placement_id OR target_step_id, effect VISIBLE/REQUIRED |
| rule_group | id, tenant_id, revision_id, rule_id, parent_group_id nullable, operator ALL/ANY, position |
| rule_predicate | id, tenant_id, revision_id, group_id, source_field_id, operator, position |
| rule_operand | id, tenant_id, revision_id, predicate_id, position, option_id OR typisiertes Literal |
| embedding | id, tenant_id, site_id, code, view_mode, lock_version |
| embedding_form | tenant_id, embedding_id, form_id, position |
| embedding_i18n | tenant_id, embedding_id, locale, headline, description, toggle_text |
| recipient_config | id, tenant_id, revision_id, strategy_code, private integration reference |
| recipient_address | tenant_id, recipient_config_id, position, address |
| audit_event | id, tenant_id, actor, action, object_id, revision_id, timestamp, change_summary |

DB-02 Unique Constraints: Site-Code pro Mandant; Formular-Code pro Mandant/Site;
Revisionsnummer pro Formular; Schritt-/Feld-Code pro Revision; Options-Code pro
Feld; eine Feldplatzierung pro Feld/Revision; eine Position pro Schritt für
Feldplatzierungen; eine Optionsposition pro Feld; eine Schrittposition pro
Revision; eine Übersetzung je Objekt/Locale. Eltern-/Kindfelder werden in der
Feldpositionsfolge des Schritts geführt; parent_placement_id bestimmt die
Gruppierung. Der Publikationscheck sichert eine zusammenhängende, konsistente
Elternreihenfolge ab.

DB-03 Regelziele müssen genau eine Platzierung oder einen Schritt referenzieren;
REQUIRED ist nur für Felder zulässig. Pro Ziel/Effekt gibt es höchstens eine
Regel. Pro Regel gibt es genau eine Wurzelgruppe. Fremdschlüssel, Check Constraints
und Backendvalidierung erzwingen diese Form. Vergleichsoperanden enthalten genau
einen zum Quelltyp passenden Wert; Mehrfachwerte stehen in mehreren Zeilen.

DB-04 Dezimale Grenzen/Defaults verwenden DECIMAL(30,10), Zeiten UTC,
Kalenderdaten DATE, Sprachen normalisierte Sprachcodes. Flags verwenden Boolean
bzw. die entsprechende MySQL-Abbildung. Case-sensitive fachliche Codes brauchen
unter MySQL eine passende explizite Collation. Konfigurationsrelationen dürfen
nicht durch kommaseparierte ID-Strings ersetzt werden.

DB-05 JSON/Text darf exportierte Snapshots, nichtkritische Erweiterungsmetadaten
oder Audit-Diffs aufnehmen. Die verbindlichen Felder, Übersetzungen, Beziehungen
und Bedingungen bleiben relational. Laufzeitdefinitionen dürfen aus einem
unveränderlichen, validierten Snapshot gecacht werden, der jederzeit aus der
relationalen Revision reproduzierbar ist.

DB-06 Speichern, Kopieren, Umordnen, Import und Veröffentlichung sind jeweils
transaktional. Der current_published_revision_id muss auf eine Revision desselben
Formulars/Mandanten zeigen. Mandanten-/Site-/Code-Suche, Fremdschlüssel und
Statuslisten benötigen Indizes. Listen und Definitionen dürfen keine unbeschränkten
N+1-Abfragen verursachen. Datenbankschemata werden versioniert migriert;
Produktionsstart darf kein destruktives automatisches Schema-Recreate ausführen.

## 8. HTTP-API und Frontend-Vertrag

API-01 Alle Endpunkte nutzen HTTPS und JSON mit versioniertem Präfix. API-Version
und Formularrevision sind unterschiedliche Größen. Die vollständigen Schemas,
Beispiele, Statuscodes und Authentisierung MUSS eine OpenAPI-Beschreibung liefern.
Die BTP-API ist ein neuer Vertrag; es gibt keine stillschweigende OCC-Kompatibilität.

Öffentliche, je nach Formularzugriffspolitik anonym oder authentisiert lesbare API:

| Methode / Pfad | Verhalten |
| --- | --- |
| GET /api/v1/sites/{siteId}/forms | Nur aktuell publizierte Formulare; type, locale, limit, cursor |
| GET /api/v1/sites/{siteId}/forms/{formId} | Vollständige publizierte Definition mit Schritten, Feldern und Regeln |
| GET /api/v1/sites/{siteId}/forms/{formId}/revisions/{revision} | Noch freigegebene unveränderliche Revision |
| POST /api/v1/sites/{siteId}/forms/{formId}/validate | Antworten gegen explizite Revision prüfen; keine Persistenz |
| POST /api/v1/sites/{siteId}/forms/{formId}/submissions | Validierte Antworten speichern; 201 bzw. 422 |
| GET /api/v1/sites/{siteId}/embeddings/{embeddingId} | Öffentliche Einbettung mit ViewMode und Formular-IDs |

API-02 Listen verwenden eine stabile Code-/ID-Sortierung und opake Cursor,
Standardlimit 20, Maximum 100. Sie liefern items und nextCursor. Einzeldefinitionen
enthalten alle zum Rendern nötigen Daten, keine zyklischen Objektreferenzen und
keine Empfänger, internen Benutzerinformationen oder Integrationsgeheimnisse.

API-03 Beispiel einer Definition (gekürzt um zusätzliche Metadaten):

```json
{
  "schemaVersion": "1.0",
  "id": "contact",
  "revision": 3,
  "type": "CONTACT",
  "requestedLocale": "de",
  "resolvedLocale": "de",
  "title": "Kontakt",
  "presentation": {"stepMode": "WIZARD", "columns": 1},
  "steps": [
    {"id": "topic", "title": "Anliegen", "position": 0, "fields": ["reason"]},
    {"id": "details", "title": "Technische Angaben", "position": 1,
     "fields": ["serial"], "visibleWhen": "technical-details"}
  ],
  "fields": [
    {"id": "reason", "fieldType": "SELECT", "label": "Grund", "required": true,
     "active": true, "hidden": false,
     "options": [{"id": "general", "label": "Allgemein", "position": 0},
                 {"id": "technical", "label": "Technik", "position": 1}]},
    {"id": "serial", "fieldType": "TEXT", "label": "Seriennummer",
     "active": true, "hidden": false, "required": true, "maxLength": 80}
  ],
  "rules": [
    {"id": "technical-details", "expression": {"op": "ALL", "children": [
      {"fieldId": "reason", "op": "EQ", "value": "technical"}
    ]}}
  ]
}
```

Felder stehen in einer flachen Liste mit stabilen IDs; steps[].fields definiert
Wurzelfelder in Reihenfolge. Für Unterfelder enthält eine Feldplatzierung
parentFieldId, position und visibleWhen. Ein öffentliches Feldobjekt kombiniert
Felddefinition und genau eine Platzierung. Regeln referenzieren ausschließlich
IDs desselben Dokuments. Die öffentliche Regel-ID ist ein stabiler Bezeichner
innerhalb der Revision, getrennt von der technischen Datenbank-UUID.

API-04 Validierungsrequest:

```json
{"revision": 3, "answers": {"reason": "technical", "serial": "ABC-123"}}
```

Erfolg: HTTP 200 mit valid=true, effectiveStepIds, effectiveFieldIds und errors=[].
Fachlich ungültige Antworten: HTTP 200, valid=false, feldbezogene Fehler mit code,
fieldId, stepId und lokalisierter message. Unbekannte Antwortfelder,
nicht effektive Felder oder unzulässige Options-IDs MUSS die API als Fehler
melden; sie dürfen nicht stillschweigend in eine Verarbeitung gelangen.
Die API darf keine Passwort-/Dateiinhalte oder kompletten Antworten spiegeln.

API-05 Transportfehler: 400 für syntaktisch ungültige Requests/Parameter, 401 bei
fehlender erforderlicher Identität, 403 für fehlende Rechte, 404 für nicht
sichtbare/unbekannte Formulare oder Revisionen, 409 für Lebenszykluskonflikte,
412/428 für Sperrbedingungen, 413 für Größenlimits, 422 für fachlich ungültige
Admin-Konfiguration, 429 für Rate-Limits mit Retry-After. Fehlerkörper:
code, message, correlationId, optional violations mit path/code/message;
keine Stacktraces oder internen SQL-/Identitätsdaten.

API-06 Definitions-GET liefert ETag und unterstützt If-None-Match/304. Der ETag
berücksichtigt Revision, Sprache und öffentliche Darstellung. Mandant, Site,
Zugriffspolitik und Sprache gehören in Cache-Schlüssel. Private Inhalte sind
nicht öffentlich cachebar. Publizieren/Archivieren invalidiert den Latest-Zeiger;
eine archivierte Revision darf nicht über einen noch gültigen öffentlichen Cache
weiter verfügbar bleiben. Cache-Laufzeiten und Invalidierung sind zu testen.

API-07 Admin-Endpunkte stehen unter /admin/api/v1/sites/{siteId} und verlangen
eine Admin-Identität. Minimaler Vertrag:

| Operation | Endpunkt |
| --- | --- |
| Liste / Neuanlage | GET /forms, POST /forms |
| Metadaten / Entwurf / Löschen | GET /forms/{id}, POST /forms/{id}/revisions, DELETE /forms/{id}/revisions/{rev} |
| Entwurf lesen / atomar ändern | GET/PATCH /forms/{id}/revisions/{rev} |
| Prüfen / Veröffentlichen | POST /forms/{id}/revisions/{rev}/validate, POST .../publish |
| Archivieren | POST /forms/{id}/archive |
| Vergleich / Export | GET /forms/{id}/revisions/{rev}/diff?against=..., GET .../export |
| Import | POST /imports/validate, POST /imports/{validatedImportId}/commit |
| Formulararten | GET/POST /form-types, GET/PATCH/DELETE /form-types/{code} |
| Einbettungen | GET/POST /embeddings, GET/PATCH/DELETE /embeddings/{id} |
| Anfragen | GET /submissions, GET /submissions/{id}, GET /forms/{id}/submissions; getrennte Antwortrechte |

PATCH einer Revision enthält eine vollständige, validierbare Änderungseinheit
für Schritte/Felder/Optionen/Regeln/Übersetzungen. JSON Merge Patch ersetzt Arrays
vollständig; kein implizites Mischen nach Array-Index. Der Dienst MUSS Größenlimits,
Referenzen und If-Match prüfen. Import-IDs sind kurzlebig und an Nutzer, Mandant,
Site und geprüften Dateninhalt gebunden; Commit prüft Konflikte erneut.
Formulararten sind mandantenweit und ihre Rechte gelten unabhängig vom Site-Pfad.

API-08 Ein optionaler OCC-Kompatibilitätsadapter KANN die bisherigen /forms-Routen,
Array-Liste und BASIC/DEFAULT/FULL-Selektion abbilden. Er MUSS separat versioniert
und getestet werden. Echte Schritte und allgemeine Regeln lassen sich nicht
verlustfrei in childFields abbilden; inkompatible Definitionen müssen eindeutig
abgelehnt werden. Keine stillschweigende Abflachung der fachlichen Steuerung.

FE-01 Das Frontend lädt eine Definition per ID, bindet seinen Antwortzustand an
die gelieferte Revision, erzeugt Eingabeelemente über eine Feldtyp-Registry und
verwendet die in Abschnitt 5 definierte Regelauswertung. Frontend-Validierung
unterstützt Nutzer; Backendvalidierung bleibt verbindlich. Bei Abschluss wird
die ursprüngliche Revision mitgegeben. Ein zwischenzeitliches Publishing darf
die bereits angezeigte Definition nicht unbemerkt austauschen.

FE-02 Ladefehler zeigen eine wiederholbare Fehlermeldung, unbekannte/archivierte
Formulare einen Nicht-verfügbar-Zustand. Bei einer nicht mehr zulässigen Revision
muss ein bewusstes Neuladen erfolgen. Antworten dürfen nicht automatisch auf
anders typisierte Felder einer neuen Revision übertragen werden.

## 9. Verpflichtender Scope: Antworten und Verarbeitung

SUB-01 Ein separater Submissions-Controller MUSS POST /api/v1/sites/{siteId}/
forms/{formId}/submissions bereitstellen. Der Vertrag enthält revision, answers und
Idempotency-Key. Erfolg liefert 201, submissionId und status. Derselbe Schlüssel
mit gleichem Request darf keine Doppelverarbeitung auslösen; anderer Inhalt mit
demselben Schlüssel liefert 409. Die Bindung umfasst Mandant, Site, Identität und
Formular. Die Aufbewahrungszeit des Schlüssels ist im API-Vertrag zu definieren.

SUB-02 Der Server MUSS vor Persistenz die freigegebene Revision, Typen,
Pflichtfelder, Regeln, Constraints und Dateireferenzen prüfen. Empfänger werden
serverseitig aus privaten Strategien bestimmt. Eine dynamische Empfängerauswahl
darf nur freigegebene fachliche Zielcodes annehmen, keine beliebige Adresse.

SUB-03 Antworten benötigen eigene relationale Tabellen submission, answer,
answer_option und attachment_reference mit Revisionsbezug. Ein normalisiertes
Antwort-JSON und ein unveränderlicher Snapshot mit Feld-/Options-IDs, Schritten,
Reihenfolge und allen Übersetzungen MUSS zusätzlich für Darstellung und
Integrationen verfügbar sein. Alle Daten werden über die Service-Schicht atomar
in der persistenten Datenbank gespeichert; In-Memory-Speicherung genügt nicht. Verarbeitung an
Ticket-/Mail-/Integrationsdienste erfolgt über eine transaktionale Outbox mit
Retry, fachlichem Status und Idempotenz. Ein Fehler beim Versand darf keine
zweite Antwort anlegen. Passwortwerte dürfen nicht standardmäßig dauerhaft
gespeichert werden; die konkrete fachliche Nutzung bedarf eines eigenen Vertrags.

SUB-04 FILE braucht einen separaten Uploadvertrag mit Größen-/Typgrenzen,
Berechtigungs-/Besitzprüfung, Ablauf nicht verwendeter Uploads und Prüfung des
Inhalts vor Verarbeitung. Die Definition enthält nur erlaubte Typen/Grenzen.
Ohne konfigurierten Uploadanbieter MUSS Publizieren eines FILE-Felds im
annahmefähigen Formular blockieren. Reine Definitions-Renderer müssen die
fehlende Uploadfähigkeit explizit behandeln.

SUB-05 Aufbewahrung, Löschung, Zugriff und Export von Antworten werden pro
Formular festgelegt. Audit- und Betriebslogs dürfen keine vollständigen Antworten,
Passwörter oder Uploadinhalte aufnehmen. Löschregeln berücksichtigen Datenbank,
Dateispeicher, Outbox und Backups gemäß dem beschlossenen Betriebskonzept.

SUB-06 Der Controller MUSS Formulardaten an eine Facade bzw. Anwendungsschicht
übergeben. Diese MUSS die Validierung nach MOD/RULE vor dem Service-Aufruf
verbindlich ausführen. Antworten sind anhand der konfigurierten Feld-IDs zu
übermitteln; lokalisierte Labels sind keine Schlüssel. Auswahlwerte referenzieren
Options-IDs. Ein fachlich ungültiger POST liefert 422 und persistiert weder
Submission noch Outbox-Ereignis. Fehler werden intern bei Spring über Errors /
BindingResult repräsentiert und extern gemäß API-05 mit stabilen Fehlercodes,
fieldId, stepId und lokalisierter message ausgegeben; keine rejectedValue-Daten.
Syntaktisch fehlerhafte Requests liefern 400, unbekannte/unzugängliche Formulare
404. Ein erfolgreicher POST liefert erst nach DB-Commit 201 CREATED mit
submissionId, formId, revision, submittedAt und status. Die reine Validate-API
behält den 200/valid=false-Vertrag aus API-04.

SUB-07 Metadaten umfassen serverseitigen UTC-Zeitpunkt, Mandant, Site,
Formular-ID und Revision, authentisierten Benutzer bzw. expliziten anonymen
Status, aufgelöste Sprache, begrenzten Browser-User-Agent und correlationId.
Eine Absender-IP ist gemäß projektseitiger Datenerfassungsrichtlinie optional;
Forwarded-Header dürfen nur über konfigurierte vertrauenswürdige Proxies
aufgelöst werden. Identität, Zeit und Scope dürfen nicht aus dem Antwort-JSON
übernommen werden. Metadaten dürfen keine Tokens oder Session-Cookies enthalten.

SUB-08 Die Administration MUSS unter Formulare eine separate Kategorie „Anfragen“
mit paginierter Liste und Filtern für Formular, Revision, Zeitraum, Site und Status
anbieten. Formulardetails verlinken auf die zugehörigen Anfragen. Eine getrennte
Berechtigung für Servicemitarbeiter steuert Lesen und Export von Antworten;
Definitionsleserechte implizieren keinen Zugriff auf personenbezogene Antworten.
Die Detailansicht zeigt zuerst einen schreibgeschützten Renderer mit Feldlabel und
Antwort untereinander, lokalisierte Options-/Enum-Bezeichnungen sowie getrennte
Sektionen pro Schritt in ursprünglicher Reihenfolge. Boolean, fehlende Antworten
und Metadaten werden lokalisiert; technische IDs dienen als letzter Fallback.
Der unveränderliche Snapshot verhindert eine nachträgliche Umdeutung alter Antworten.
Roh-JSON und Snapshot gehören ausschließlich in einen separaten technischen Tab.
Alle Antwortwerte werden als Text escaped; Leer-, Lade- und Fehlerzustände sowie
lange Texte, Tastaturbedienung und Sprachwechsel sind abzudecken.

SUB-09 Nach erfolgreichem Commit MUSS ein internes Ereignis
DynamicFormSubmissionCreated bereitstehen. Da es in BTP kein Commerce-ClusterEvent
gibt, MUSS ein konfigurierbarer Adapter dieses Ereignis über SAP BTP Event Mesh
publizieren können. Der Adapter ist standardmäßig deaktiviert. Seine transaktionale
Outbox wird zusammen mit der Submission geschrieben; Veröffentlichung erfolgt
nach Commit mit Retry/Backoff, Dead-Letter-Behandlung und Monitoring. Ein Brokerfehler
ändert keine bereits erfolgreiche 201-Antwort und erzeugt keine zweite Submission.
Das versionierte Ereignis enthält eventId, eventType, occurredAt, tenantId, siteId,
formId, revision, submissionId und correlationId; vollständige Antworten und
Benutzerdaten gehören nicht in die Standardnachricht. Projektspezifische Consumer
laden autorisiert über die Submission-Referenz nach. Topic, Service Binding,
Berechtigungen und Schema werden dokumentiert. Zustellung ist mindestens einmal;
Consumer müssen anhand eventId idempotent reagieren. Rollback erzeugt kein Ereignis.

SUB-10 Ein vorbereiteter, standardmäßig deaktivierter E-Mail-Consumer MUSS
mitgeliefert werden. Aktivierung und Empfängerauflösung erfolgen ausschließlich
serverseitig. Pro Formularrevision wird ein privates lokalisiertes Thymeleaf-HTML-
Template konfiguriert; nur berechtigte Administratoren dürfen es ändern. Auswahl:
angeforderte Sprache, Formular-Standardsprache, statisches mitgeliefertes Fallback.
Ungültige konfigurierte Templates führen zu einem nachvollziehbaren Versandfehler,
nicht stillschweigend zu einem anderen Template. Templates erhalten einen typisierten
answers-Zugriff auf das gesamte JSON, answersJson, den lokalisierten View mit Schritten
und freigegebene Metadaten. Das Fallback stellt alle Antworten lesbar und escaped dar.
Antwortwerte dürfen nie selbst als Template ausgewertet werden. Die Commerce-
Vorbereitung verwendet cxdevtools HtmlEmailGenerator/HtmlEmailService; die BTP-
Implementierung MUSS gleichwertige abstrahierte Generator-/Versanddienste ohne
Commerce-Laufzeitabhängigkeit bereitstellen. Versandfehler ändern den Speichererfolg
nicht; Retry, Versandstatus und Idempotenz müssen Mehrfachversand soweit technisch
möglich vermeiden und die Ungewissheit nach SMTP-Annahme explizit behandeln.

## 10. BTP-Bereitstellung und Betrieb

OPS-01 Zielarchitektur: stateless Backend und Admin-Webanwendung auf SAP BTP mit
relationalem, persistentem Datenbankdienst. Cloud Foundry ist die bevorzugte
Deployment-Variante dieser Konzeption; Kyma ist eine mögliche alternative
Betriebsentscheidung. Anwendung und Datenbank benötigen getrennte DEV/TEST/PROD-
Instanzen. Eine PostgreSQL- oder MySQL-Instanz im flüchtigen App-Dateisystem ist
nicht zulässig.

OPS-02 Für PostgreSQL ist eine Instanz von „PostgreSQL on SAP BTP, Hyperscaler
Option“ im vorgesehenen Subaccount/Space zu provisionieren und per Service Binding
an das Backend anzubinden. SAP dokumentiert Entitlements, Anlage über das BTP
Cockpit und Application Binding. Die konkrete Region und der Serviceplan müssen
vor Projektstart im Zielaccount verifiziert werden. Quelle:
https://help.sap.com/docs/postgresql-on-sap-btp/postgresql-on-sap-btp-hyperscaler-option/provision-postgresql-using-cockpit
(abgerufen 09.09.2026).

OPS-03 Diese Spezifikation verspricht keinen allgemein verfügbaren nativen
MySQL-Serviceplan in jedem BTP-Account. MySQL ist erst wählbar, wenn der Betreiber
einen im BTP-Betriebskonzept provisionierten Dienst einschließlich privater
Konnektivität, Binding/Secret-Verwaltung, Backup und Support nachgewiesen hat.
Eine angebundene Hyperscaler-Datenbank benötigt eine ausdrückliche Entscheidung,
ob sie die Anforderung „in der BTP bereitgestellt“ im Projekt erfüllt. Andernfalls
ist PostgreSQL nach OPS-02 verbindlich. SAP dokumentiert beispielsweise die
private Anbindung von Azure-Diensten einschließlich Azure Database for MySQL:
https://help.sap.com/docs/private-link/private-link1/consume-azure-services-in-sap-btp
(abgerufen 09.09.2026).

OPS-04 Authentisierung MUSS an den vorgesehenen BTP-Identitätsdienst angebunden
werden; Admin- und externe API benötigen getrennte Scopes/Audiences. Tokens sind
serverseitig auf Signatur, Aussteller, Zielgruppe, Ablauf und Rollen zu prüfen.
CORS erlaubt nur konfigurierte Frontend-Ursprünge. Browser erhalten keine
Datenbankcredentials. Secrets kommen aus Service Bindings/Secret-Verwaltung,
nicht aus Repository oder öffentlichen Konfigurationsendpunkten.

OPS-05 Pflichtartefakte: reproduzierbarer Build, Deployment-Descriptor, deklarierte
Serviceabhängigkeiten, Umgebungsparameter, Schema-Migrationen und Runbook für
Installation, Update, Rollback, Restore und Credential-Rotation. Migrationen
müssen mit der gewählten Datenbank in CI getestet werden. Wird Unterstützung für
beide Datenbanken ausgeliefert, sind dieselben Akzeptanztests für beide Pflicht.

OPS-06 Logs sind strukturiert, enthalten correlationId und technische Ereignisse,
keine Antwortinhalte. Metriken umfassen API-Latenz/-Fehler, Publikationsfehler,
Cachetreffer, DB-Poolauslastung und gegebenenfalls Outbox-Rückstände. Liveness darf
keinen Datenbankzugriff erzwingen; Readiness MUSS die Betriebsfähigkeit prüfen.
Health-Endpunkte dürfen keine Credentials oder internen Details offenlegen.

OPS-07 Vorgeschlagene Abnahmeziele: p95 < 500 ms für eine gecachte vollständige
Definition bei 100 parallelen Lesern; 99,9 % monatliche Verfügbarkeit; RPO <= 24 h
und RTO <= 4 h. Dies sind Produktziele, keine behaupteten Zusagen eines BTP-Plans.
Sie sind anhand repräsentativer maximaler Definitionen, gewähltem Plan und
Restore-Probe zu bestätigen oder vor Implementierung begründet anzupassen.

## 11. Abnahmekriterien und Tests

| ID | Szenario / erwartetes Ergebnis |
| --- | --- |
| A01 | Admin erstellt zweisprachiges Formular mit drei Schritten; Speicherung und erneutes Laden erhalten Daten und Reihenfolge vollständig. |
| A02 | Auswahl general blendet Technikschritt aus; technical blendet ihn mit Pflicht-Seriennummer ein. Zurück-/Weiter-Navigation bleibt konsistent. |
| A03 | Mehrfachauswahl aktiviert die Vereinigung der Zielunterfelder ohne doppelte Platzierungen. Abwahl entfernt unzulässige Antworten. |
| A04 | Inaktive Felder, Eltern oder Schritte unterdrücken auch abhängige Felder; hidden allein ändert Effektivität nicht. |
| A05 | Direkter, indirekter und über Schritte laufender Zyklus verhindern Publikation mit verständlichem Fehlerpfad. |
| A06 | Ungültige Zahlen-/Längengrenzen, nicht passende Defaults und fremde Options-IDs werden abgelehnt. 0 und false bleiben typgerecht unterscheidbar. |
| A07 | Neue Publikation beeinflusst keine laufende, revisionsgebundene Formularinstanz; alte freigegebene Revision bleibt eindeutig abrufbar. |
| A08 | Zwei parallele Bearbeitungen verursachen 412 statt Datenverlust; eine fehlgeschlagene Publikation ändert die öffentliche Definition nicht. |
| A09 | Fremder Mandant/Site, fehlende Rollen und Entwurfsabruf über öffentliche API liefern keine privaten Daten. Empfänger fehlen in allen öffentlichen Projektionen. |
| A10 | Sprachfallback, Unicode-Längenzählung, DATE/WEEK und Dezimalgrenzen sind zwischen Frontend und Backend durch gemeinsame Testvektoren belegt. |
| A11 | ETag/304 und Cursor-Pagination funktionieren; Publish-/Archivierung invalidiert Latest-Caches ohne Scope-Leak. |
| A12 | Import/Export-Rundlauf erhält alle Sprachen und Regeln; fehlerhafter Import schreibt keine Teilobjekte. |
| A13 | Definitionen nach Prozessneustart und Restore sind identisch; DB-Constraints verhindern mandantenfremde Referenzen. |
| A14 | Vorschau, schmale Ansicht, Tastaturbedienung, Fokusführung und Fehlerzustände werden in Browser-E2E-Tests geprüft. |
| A15 | Last-/Größenlimits, Rate-Limits und definierte Betriebsziele sind mit repräsentativen Daten gemessen. |
| A16 | doppelte Requests erzeugen genau eine Antwort/Outbox-Verarbeitung; manipulierte Antworten/Empfänger/Dateireferenzen werden abgelehnt. |

| A17 | Ungültiger Submission-POST liefert 422 mit deutschen/englischen Feldfehlern; keine Daten und kein Ereignis werden gespeichert. Gültiger POST liefert 201 erst nach DB-Commit. |
| A18 | Reload nach Prozessneustart erhält Antworten und serverseitige Metadaten. Nach Änderungen an Definition/Labels bleiben alte Anfragen durch Snapshot korrekt lesbar. |
| A19 | Servicemitarbeiter sieht Schrittsektionen, lokalisierte Auswahlwerte und einen separaten JSON-Tab; fehlende Antwortrechte verweigern Liste, Details und Export. |
| A20 | DB-Rollback veröffentlicht kein Ereignis. Broker-Ausfall nach Commit erhält Submission und Outbox; Retry und doppelte Zustellung erzeugen keine doppelte fachliche Verarbeitung. |
| A21 | Mail-Consumer ist zunächst deaktiviert. Aktivierung verwendet das konfigurierte lokalisierte Template bzw. Fallback; HTML in Antworten bleibt escaped. Versandfehler verändern die gespeicherte Anfrage nicht. |

Zusätzlich verpflichtend: Unit-Tests für Regelengine/Validierung,
API-Vertragstests, Integrationstests gegen die reale gewählte relationale Datenbank,
Transaktions-/Concurrency-Tests, Berechtigungstests und Restore-Probe. Ein
In-Memory-Datenbanktest ersetzt keinen PostgreSQL-/MySQL-Integrationstest.

## 12. Vor Implementierungsbeginn festzulegende Entscheidungen

1. BTP-Zielaccount, Region, Runtime, Datenbankplan und Entitlements bestätigen;
   PostgreSQL nach OPS-02 bleibt bis zu einer anderen genehmigten Entscheidung Ziel.
2. Anonyme versus authentisierte Formulare und Site-/Mandantenmodell konkretisieren.
3. Produktgrenzen und Betriebsziele bestätigen; unterstützte Sprachen bestimmen.
4. Uploadanbieter, Event-Mesh-Topic/Binding und Aktivierung der mitzuliefernden
   Integrationsadapter festlegen; Speicherung und Anfragen-Administration sind Pflichtscope.
5. Identitätsdienst, Integrationsziele und Aufbewahrungsregeln festlegen.
6. Migrierbare Bestandsfälle und erforderlichen OCC-Kompatibilitätsumfang abnehmen.

Diese Entscheidungen sind Umsetzungsparameter der späteren BTP-Anwendung. Sie
blockieren nicht die parallele Extraktion der bestehenden cxdevforms-Extension.
