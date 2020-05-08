## Sistema Informatico Integrato di Contabilità (SIC)
E' un sistema integrato per la gestione dei processi amministrativi contabili realizzato dalla Cooperativa EDP La Traccia per la Regione Basilicata.

## Descrizione
Il SIC è utilizzato, dalla Regione Basilicata, dal 2003 e via via evoluto a seconda delle esigenze normative e/o organizzative.
Dal 2013 (a seguito dell'entrata in vigore del Decreto Legislativo 118/2011) il SIC viene utilizzato anche dagli Enti Strumentali della Regione Basilicata e dal Consiglio Regionale.
Il Sistema è assimilabile ad un vero e proprio Enterprise Resource Planning (ERP) in quanto non gestisce solamente tutti gli aspetti contabili dell'Ente ma anche i processsi riguardanti Gestione Contratti, Gestione Patrimonio, ecc.
Nella [brochure](https://github.com/RegioneBasilicata/SistemaInformativoContabile/blob/master/Documents/SIC_brochure.pdf) sono evidenziate le principali carattersitche del Sistema.

## Framework, tecnologie e linguaggi utilizzati
 - Oracle DataBase vers. 11 e superiori
 - Oracle Application Express vers. 4.0 e superiori
 - PL/SQL
 - Java
 - Javascript
 - Jasper Report
 - JQuery

## Requisiti di installazione
|Requisito| Versione |Descrizione|
|-----------|-----------|---------|
| [Oracle Database](https://www.oracle.com/it/database/) | 11 |Base dati (qualsiasi release)
|[Oracle Application Express](https://apex.oracle.com/)|4|Ambiente di sviluppo
|[Tomcat](https://tomcat.apache.org) (o altri Apex compliant)|-|Application server 
|[JasperSoft Studio](https://community.jaspersoft.com)|>5|Report builder

## Installazione
Per tutti i dettagli circa le modalità di installazione del SIC leggere e seguire le istruzioni contenute nel Manuale di Gestione dell'applicazione

## Moduli
Di seguito i principali moduli da cui è composto il sistema
 1. Modulo Contabilità
 2. Modulo Provveditorato e Patrimonio 
 3. Modulo Contratti 
 4. Modulo Bilancio
 5. Modulo Reportistica

## Documenti 
Di seguito i documenti che accompagnano il codice

 1. Manuale di Gestione dell'Applicazione
 2. Manuale Utente
 3. [Script SQL per la creazione degli oggetti di Data Base](https://github.com/RegioneBasilicata/SistemaInformativoContabile/blob/master/SQL/SIC%20script%20DDL.sql)
 4. [File sorgente Apex dell'applicazione](https://github.com/RegioneBasilicata/SistemaInformativoContabile/blob/master/SQL/SIC_Application.sql.zip)
 5. [Dizionario dati DB](https://github.com/RegioneBasilicata/SistemaInformativoContabile/tree/master/SIC%20DB%20Documentation)

 
