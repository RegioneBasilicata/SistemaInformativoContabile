package it.latraccia.utility;


import it.latraccia.entity.sic.richiesta.CreateDelDocumentoTypes;
import it.latraccia.entity.sic.richiesta.CreateDelDocumentoTypes.BeneficiariType;
import it.latraccia.entity.sic.risposta.COGType;
import it.latraccia.entity.sic.risposta.ListaCOGType;
import it.latraccia.entity.sic.risposta.RispostaInterrogaMandatiBeneficiariAttoTypes;
import it.latraccia.entity.sic.risposta.RispostaInterrogaMandatiBeneficiariAttoTypes.MandatoType;
import it.latraccia.entity.sic.risposta.RispostaInterrogazioneContrattiTypes;
import it.latraccia.entity.sic.risposta.RispostaInterrogazioneMandatiTrasparenzaTypes;
import it.latraccia.entity.sic.risposta.impl.COGTypeImpl;
import it.latraccia.entity.sic.risposta.impl.ListaCOGTypeImpl;
import it.latraccia.entity.sic.risposta.impl.RispostaInterrogaMandatiBeneficiariAttoTypesImpl;
import it.latraccia.entity.sic.risposta.impl.RispostaInterrogazioneContrattiTypesImpl;
import it.latraccia.entity.sic.risposta.impl.RispostaInterrogazioneMandatiTrasparenzaTypesImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DB 
{
	private String tnsnames;
	private String url;
	private String username;
	private String password;
	private String cliente;
	private String messaggioBlocco;
	private Connection connection = null;

	public DB() 
	{
		try 
		{
			// LETTURA XML
			InputStream in = getClass().getResourceAsStream("dati_db_cluster.xml");
			// InputStream in = getClass().getResourceAsStream("dati_db_cluster_POTENZA.xml");
			// first of all we request out
			// DOM-implementation:
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// then we have to create document-loader:
			DocumentBuilder loader = factory.newDocumentBuilder();
			// loading a DOM-tree...
			Document document = loader.parse(in);
			// normalize text representation
			document.getDocumentElement().normalize();
			// at last, we get a root element:
			Element root = document.getDocumentElement();
			NodeList rootlist = root.getChildNodes();
			Element database = (Element) rootlist.item(0);

			// nuovo metodo per cluster
			tnsnames = database.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
			url = "jdbc:oracle:thin:@" + tnsnames;
			username = database.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
			password = database.getChildNodes().item(2).getChildNodes().item(0).getNodeValue();
			cliente = rootlist.item(1).getChildNodes().item(0).getNodeValue();
			messaggioBlocco = rootlist.item(2).getChildNodes().item(0).getNodeValue();

			// Load the JDBC driver
			String driverName = "oracle.jdbc.driver.OracleDriver";

			Class.forName(driverName);
			// Create a connection to the database
			// connection = DriverManager.getConnection(url, username,
			// password);
			connection = getConnection();
			// System.out.println("connessione ok");
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}  
		catch (ParserConfigurationException e1) 
		{
			e1.printStackTrace();
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private Connection getConnection() 
	{
		try 
		{
			return DriverManager.getConnection(url, username, password);
		} 
		catch (SQLException e) 
		{
			System.out.println("Errore in DB.getConnection: \r\nurl: "+url+" username: "+username+ " password: "+password);
			e.printStackTrace();
		}
		return null;
	}

	public void closeDB() 
	{
		try 
		{
			if(connection!=null&&!connection.isClosed())
				connection.close();
		} 
		catch (SQLException e) 
		{
			System.out.println("Errore nella chiusura della connessione...");
			e.printStackTrace();
		}
	}

	public List interrogaCapitolo(String annoBilancio, String struttura) throws SQLException
	{
		List r = new ArrayList();
		InterrogaCapitoloResult icr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();
			
		String query;
		if (cliente.equalsIgnoreCase("giunta"))
			// **** per prendere anche capitoli condivisi in anni successivi al "+annoBilancio+" ma non in "+annoBilancio+" (per poter assumenre impegni sul pluriennale
			query = "SELECT nvl(blocco_preimpegni_impegni, 'N') blocco_preimpegni_impegni, nvl(competenza_bloccata, 0) competenza_bloccata, nvl(a.flag_perenti, 'N') perenti, a.anno anno_bilancio, a.eu || a.codice codice_capitolo, a.descrizione descrizione_capitolo, a.capitolo upb, nb1.codice || '.' || nb2.codice miss_prog FROM fin_t_strutture_capitoli s, fin_t_articoli a, nb_livello1 nb1, nb_livello2 nb2, fin_t_dati_generali d, fin_t_bilancio b WHERE a.anno = "+annoBilancio+" AND nvl(a.abilitato, 'N') = 'S' AND b.anno = a.anno AND b.eu || b.articolo = a.eu || a.codice AND s.capitolo_anno = a.anno AND a.eu || a.codice = s.capitolo_eu || s.capitolo_codice AND s.fin_t_strutture_codice = '"+struttura+"' AND s.data_assestamento = TO_DATE('01011900', 'ddmmyyyy') AND nb1.id (+) = nb2.id_livello1 AND a.nb_id_padre = nb2.id (+) AND d.anno_finanziario = a.anno AND nvl(d.attivo_provvedimenti, 'N') = 'S' AND nvl(a.nb_fpv, 'N') <> 'S' UNION "
					+ "SELECT nvl(blocco_preimpegni_impegni, 'N') blocco_preimpegni_impegni, nvl(competenza_bloccata, 0) competenza_bloccata, nvl(a.flag_perenti, 'N') perenti, to_number("+annoBilancio+") anno_bilancio, a.eu || a.codice codice_capitolo, a.descrizione descrizione_capitolo, a.capitolo upb, nb1.codice || '.' || nb2.codice miss_prog FROM fin_t_strutture_capitoli s, fin_t_articoli a, nb_livello1 nb1, nb_livello2 nb2, fin_t_dati_generali d, fin_t_bilancio b WHERE a.anno > "+annoBilancio+" AND nvl(a.abilitato, 'N') = 'S' AND b.anno = a.anno AND b.eu || b.articolo = a.eu || a.codice AND s.capitolo_anno = a.anno AND a.eu || a.codice = s.capitolo_eu || s.capitolo_codice AND s.fin_t_strutture_codice = '"+struttura+"' AND s.data_assestamento = TO_DATE('01011900', 'ddmmyyyy') AND nb1.id (+) = nb2.id_livello1 AND a.nb_id_padre = nb2.id (+) AND d.anno_finanziario = a.anno AND nvl(d.attivo_provvedimenti, 'N') = 'S' AND nvl(a.nb_fpv, 'N') <> 'S'";
		else if (cliente.equalsIgnoreCase("arpab") || cliente.equalsIgnoreCase("alsia"))
			query = "select nvl(BLOCCO_PREIMPEGNI_IMPEGNI,'N') BLOCCO_PREIMPEGNI_IMPEGNI, nvl(competenza_bloccata,0) competenza_bloccata ,nvl(a.flag_perenti,'N') perenti,  a.anno anno_bilancio,a.eu||a.codice codice_capitolo,a.descrizione descrizione_capitolo,a.capitolo upb, nb1.codice||'.'||nb2.codice miss_prog from fin_t_strutture_capitoli s ,fin_t_articoli a,nb_livello1 nb1,  nb_livello2 nb2, fin_t_dati_generali d, fin_t_bilancio b where a.anno = "+annoBilancio+" and nvl(a.abilitato,'N') = 'S' and b.anno=a.anno and b.eu||b.articolo=a.eu||a.codice  and s.capitolo_anno = a.anno and a.eu||a.codice = s.capitolo_eu||s.capitolo_codice and s.fin_t_strutture_codice = '"+struttura+"' and s.data_assestamento = to_date('01011900','ddmmyyyy')and nb1.id (+ ) = nb2.id_livello1  and a.nb_id_padre = nb2.id ( + ) and d.anno_finanziario = a.anno and nvl(d.attivo_provvedimenti,'N') = 'S' and nvl(a.nb_fpv,'N') <> 'S'";
		else if (cliente.equalsIgnoreCase("consiglio"))
			query = "select nvl(BLOCCO_PREIMPEGNI_IMPEGNI,'N') BLOCCO_PREIMPEGNI_IMPEGNI, nvl(competenza_bloccata,0) competenza_bloccata ,nvl(a.flag_perenti,'N') perenti,  a.anno anno_bilancio,a.eu||a.codice codice_capitolo,a.descrizione descrizione_capitolo,a.capitolo upb,nb1.codice||'.'||nb2.codice ||'.'|| a.NB_MACROAGGREGATO ||'.'|| a.NB_TITOLO miss_prog from fin_t_strutture_capitoli s ,fin_t_articoli a,nb_livello1 nb1,  nb_livello2 nb2, fin_t_dati_generali d, fin_t_bilancio b where a.anno = "+annoBilancio+" and nvl(a.abilitato,'N') = 'S' and b.anno=a.anno and b.eu||b.articolo=a.eu||a.codice  and s.capitolo_anno = a.anno and a.eu||a.codice = s.capitolo_eu||s.capitolo_codice and s.fin_t_strutture_codice = '"+struttura+"' and s.data_assestamento = to_date('01011900','ddmmyyyy')and nb1.id (+ ) = nb2.id_livello1  and a.nb_id_padre = nb2.id ( + ) and d.anno_finanziario = a.anno and nvl(d.attivo_provvedimenti,'N') = 'S' and nvl(a.nb_fpv,'N') <> 'S'";
		else
			return r;
							
		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			icr = new InterrogaCapitoloResult();
			icr.setAnnoBilancio(rs.getInt("anno_bilancio"));
			icr.setCodiceCapitolo(rs.getString("codice_capitolo"));
			icr.setDescrizioneCapitolo(rs.getString("descrizione_capitolo"));
			icr.setUPB(rs.getString("upb"));
			icr.setMissioneProgramma(rs.getString("miss_prog"));
			icr.setPerenti(rs.getString("perenti"));


			//Modifica Michele
			//prendo info su disponibilita
			InterrogaBilancioResult ibr= interrogaBilancio(rs.getString("anno_bilancio"), icr.getCodiceCapitolo());
			icr.setDisponibilitaCassa(ibr.getDispCassa());
			icr.setDisponibilitaCompetenza(ibr.getDispCompetenza());
			//Modifica Michele Fine
			
			icr.setBloccoLiquidazioni(rs.getString("BLOCCO_PREIMPEGNI_IMPEGNI"));
			icr.setCassaBloccata(rs.getDouble("competenza_bloccata"));
			
			if(icr.getBloccoLiquidazioni().equalsIgnoreCase("S")){
				icr.setCodiceRisposta("1");
				icr.setDescrizioneRisposta("Blocco Generazione Impegni: " + messaggioBlocco);
			}
			else{
				icr.setCodiceRisposta("0");
				icr.setDescrizioneRisposta("Nessun blocco");
				if (icr.getCassaBloccata()!=0){
					icr.setDescrizioneRisposta("Blocchi su competenza per euro "+ icr.getCassaBloccata() + ". " + messaggioBlocco);
				}
			}

			r.add(icr);
		}

		s.close();

		return r;
	}

	public List interrogaCapitoliLiq(String annoBilancio, String struttura) throws SQLException
	{
		/*
		 * Se in fint_bilancio i campi blocco_preimpegni_impegni e blocco_liquidazioni valgono 'S' devo restituire come codiceRisposta 2 
		 * Nella descrizioneCapitolo devo inserire 'Blocco Generazione Liquidazioni' 
		 */
		List r = new ArrayList();
		InterrogaCapitoloResult icr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();
		
		String query;
		if (cliente.equalsIgnoreCase("giunta") || cliente.equalsIgnoreCase("arpab") || cliente.equalsIgnoreCase("alsia"))			
			// modifica Francesco Ferrante 23/12/2016
			query = "SELECT nvl(a.flag_perenti,'N') perenti, nvl(b.blocco_liquidazioni,'N') blocco_liquidazioni, nvl(b.cassa_bloccata,0) cassa_bloccata, a.anno anno_bilancio, a.eu||a.codice codice_capitolo,a.DESC_CAPITOLO descrizione_capitolo,a.upb upb, a.LIV1 ||  '.' ||  a.LIV2 miss_prog	FROM fin_t_strutture_capitoli s, vw_nb_articoli a, fin_t_bilancio b, fin_t_dati_generali g WHERE a.anno = "+annoBilancio+" and nvl(a.abilitato,'N') = 'S' AND a.anno = b.anno AND a.eu || a.codice = b.eu || b.articolo AND s.capitolo_anno = a.anno AND a.eu ||  a.codice = s.capitolo_eu || s.capitolo_codice AND s.fin_t_strutture_codice = '"+struttura+"' AND s.data_assestamento = TO_DATE('01011900','ddmmyyyy') AND g.anno_finanziario = a.anno AND nvl(g.attivo_provvedimenti,'N') = 'S' UNION SELECT DISTINCT nvl(a.flag_perenti,'N') perenti, nvl(b.blocco_liquidazioni,'N') blocco_liquidazioni, nvl(b.cassa_bloccata,0) cassa_bloccata, a.anno anno_bilancio, a.eu ||  a.CODICE codice_capitolo, a.DESC_CAPITOLO descrizione_capitolo, a.UPB upb, a.liv1 || '.' ||  a.liv2 miss_prog FROM vw_nb_articoli a, bas_ind b, fin_t_documenti d, fin_t_bilancio b, fin_t_dati_generali g WHERE d.doc_type LIKE 'IMP%' AND d.segment3 = '"+struttura+"' AND d.segment8 <= "+annoBilancio+" AND a.anno = b.anno AND a.eu || a.codice = b.eu || b.articolo AND nvl(d.open_balance_proposed,0) > 0 AND d.doc_id = b.doc_id AND b.ind_anno = "+annoBilancio+"	AND b.ind_cap = a.eu || a.codice AND b.ind_anno = a.anno AND g.anno_finanziario = a.anno AND nvl(g.attivo_provvedimenti,'N') = 'S' AND nvl(a.nb_fpv,'N') <> 'S'";		
		else if (cliente.equalsIgnoreCase("consiglio"))
			// modifica Francesco Ferrante 23/12/2016
			query = "SELECT nvl(a.flag_perenti,'N') perenti, nvl(b.blocco_liquidazioni,'N') blocco_liquidazioni, nvl(b.cassa_bloccata,0) cassa_bloccata, a.anno anno_bilancio, a.eu||a.codice codice_capitolo,a.DESC_CAPITOLO descrizione_capitolo,a.upb upb, a.LIV1 ||  '.' ||  a.LIV2 || '.' || a.macro_agg || '.' || a.NB_TITOLO miss_prog FROM fin_t_strutture_capitoli s, vw_nb_articoli a, fin_t_bilancio b, fin_t_dati_generali g WHERE a.anno = "+annoBilancio+" and nvl(a.abilitato,'N') = 'S' AND a.anno = b.anno AND a.eu || a.codice = b.eu || b.articolo AND s.capitolo_anno = a.anno AND a.eu ||  a.codice = s.capitolo_eu || s.capitolo_codice AND s.fin_t_strutture_codice = '"+struttura+"' AND s.data_assestamento = TO_DATE('01011900','ddmmyyyy') AND g.anno_finanziario = a.anno AND nvl(g.attivo_provvedimenti,'N') = 'S' UNION SELECT DISTINCT nvl(a.flag_perenti,'N') perenti, nvl(b.blocco_liquidazioni,'N') blocco_liquidazioni, nvl(b.cassa_bloccata,0) cassa_bloccata, a.anno anno_bilancio, a.eu ||  a.CODICE codice_capitolo, a.DESC_CAPITOLO descrizione_capitolo, a.UPB upb, a.LIV1 ||  '.' ||  a.LIV2 || '.' || a.macro_agg || '.' || a.NB_TITOLO miss_prog FROM vw_nb_articoli a, bas_ind b, fin_t_documenti d, fin_t_bilancio b, fin_t_dati_generali g WHERE d.doc_type LIKE 'IMP%' AND d.segment3 = '"+struttura+"' AND d.segment8 <= "+annoBilancio+" AND a.anno = b.anno AND a.eu || a.codice = b.eu || b.articolo AND nvl(d.open_balance_proposed,0) > 0 AND d.doc_id = b.doc_id AND b.ind_anno = "+annoBilancio+"	AND b.ind_cap = a.eu || a.codice AND b.ind_anno = a.anno AND g.anno_finanziario = a.anno AND nvl(g.attivo_provvedimenti,'N') = 'S' AND nvl(a.nb_fpv,'N') <> 'S'";
		else
			return r;
			
		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			icr = new InterrogaCapitoloResult();
			icr.setAnnoBilancio(rs.getInt("anno_bilancio"));
			icr.setCodiceCapitolo(rs.getString("codice_capitolo"));
			icr.setDescrizioneCapitolo(rs.getString("descrizione_capitolo"));
			icr.setUPB(rs.getString("upb"));
			icr.setMissioneProgramma(rs.getString("miss_prog"));
			icr.setPerenti(rs.getString("perenti"));

			//Modifica Michele
			//prendo info su disponibilita
			InterrogaBilancioResult ibr= interrogaBilancio(rs.getString("anno_bilancio"), icr.getCodiceCapitolo());
			icr.setDisponibilitaCassa(ibr.getDispCassa());
			icr.setDisponibilitaCompetenza(ibr.getDispCompetenza());
			//Modifica Michele Fine
			
			icr.setBloccoLiquidazioni(rs.getString("blocco_liquidazioni"));
			icr.setCassaBloccata(rs.getDouble("cassa_bloccata"));
			
			if(icr.getBloccoLiquidazioni().equalsIgnoreCase("S")){
				icr.setCodiceRisposta("2");
				icr.setDescrizioneRisposta("Blocco Generazione Liquidazioni: " + messaggioBlocco);
			}
			else{
				icr.setCodiceRisposta("0");
				icr.setDescrizioneRisposta("Nessun blocco");
				if (icr.getCassaBloccata()!=0){
					icr.setDescrizioneRisposta("Blocchi su cassa per euro "+ icr.getCassaBloccata() + ". " + messaggioBlocco);
				}
			}
			
			

			r.add(icr);
		}

		s.close();

		return r;
	}

	public InterrogaBilancioResult interrogaBilancio(String annoBilancio, String capitolo) throws SQLException
	{
		InterrogaBilancioResult ibr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call GENERO_DOCUMENTI.leggo_capitolo(?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(3, Types.VARCHAR); //descrizione_capitolo
		cs.registerOutParameter(4, Types.VARCHAR); //capitolo_por
		cs.registerOutParameter(5, Types.VARCHAR); //flag_perente
		cs.registerOutParameter(6, Types.VARCHAR); //conto_economica1
		cs.registerOutParameter(7, Types.VARCHAR); //conto_economica2
		cs.registerOutParameter(8, Types.VARCHAR); //conto_economica3
		cs.registerOutParameter(9, Types.VARCHAR); //conto_economica4
		cs.registerOutParameter(10, Types.VARCHAR); //conto_economica5
		cs.registerOutParameter(11, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(12, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT
		if (annoBilancio == null || annoBilancio.equals(""))
		{
			cs.setNull(1, Types.NUMERIC);
		}
		else
		{
			cs.setInt(1, Integer.parseInt(annoBilancio));  //anno
		}

		cs.setString(2, capitolo);  //capitolo

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		ibr = new InterrogaBilancioResult();
		if (cs.getInt(11)!=0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			ibr.setCodiceRisposta(cs.getInt(11));
			ibr.setDescrizioneRisposta(cs.getString(12));
		}
		else
		{
			ibr.setAnnoBilancio(Integer.parseInt(annoBilancio));
			ibr.setCodiceCapitolo(capitolo);
			ibr.setDescrizioneCapitolo(cs.getString(3));  
			ibr.setPor(cs.getString(4));
			ibr.setPerenti(cs.getString(5));
			ibr.setContoEconomica1(cs.getString(6));
			ibr.setContoEconomica2(cs.getString(7));
			ibr.setContoEconomica3(cs.getString(8));
			ibr.setContoEconomica4(cs.getString(9));
			ibr.setContoEconomica5(cs.getString(10));

			CallableStatement cs1;

			cs1 = connection.prepareCall("{call GENERO_DOCUMENTI.leggo_bilancio(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

			// Setto il tipo dei parametri di OUTPUT
			cs1.registerOutParameter(3, Types.NUMERIC); //competenza
			cs1.registerOutParameter(4, Types.NUMERIC); //variazione_competenza
			cs1.registerOutParameter(5, Types.NUMERIC); //assestamento_competenza
			cs1.registerOutParameter(6, Types.NUMERIC); //disponibilita_competenza 
			cs1.registerOutParameter(7, Types.NUMERIC); //cassa
			cs1.registerOutParameter(8, Types.NUMERIC); //variazione_cassa
			cs1.registerOutParameter(9, Types.NUMERIC); //assestamento_cassa
			cs1.registerOutParameter(10, Types.NUMERIC); //disponibilità_cassa
			cs1.registerOutParameter(11, Types.NUMERIC); //blocco_preimpegni
			cs1.registerOutParameter(12, Types.NUMERIC); //pre_impegni
			cs1.registerOutParameter(13, Types.NUMERIC); //impegni_accertamenti_compe
			cs1.registerOutParameter(14, Types.NUMERIC); //variazioni
			cs1.registerOutParameter(15, Types.NUMERIC); //liquidazioni_compe
			cs1.registerOutParameter(16, Types.NUMERIC); //mandati_reversali_compe
			cs1.registerOutParameter(17, Types.NUMERIC); //mandati_reversali_res
			cs1.registerOutParameter(18, Types.NUMERIC); //competenza_bloccata
			cs1.registerOutParameter(19, Types.NUMERIC); //cassa_bloccata
			cs1.registerOutParameter(20, Types.NUMERIC); //variazione_competenza_piu
			cs1.registerOutParameter(21, Types.NUMERIC); //variazione_competenza_meno
			cs1.registerOutParameter(22, Types.NUMERIC); //assestamento_competenza_piu
			cs1.registerOutParameter(23, Types.NUMERIC); //assestamento_competenza_meno
			cs1.registerOutParameter(24, Types.NUMERIC); //variazione_cassa_piu
			cs1.registerOutParameter(25, Types.NUMERIC); //variazione_cassa_meno
			cs1.registerOutParameter(26, Types.NUMERIC); //assestamento_cassa_piu
			cs1.registerOutParameter(27, Types.NUMERIC); //assestamento_cassa_meno
			cs1.registerOutParameter(28, Types.NUMERIC); //codice_risposta
			cs1.registerOutParameter(29, Types.VARCHAR); //descrizione_risposta

			// Setto il valore per i parametri di INPUT
			cs1.setInt(1, Integer.parseInt(annoBilancio));  //anno
			cs1.setString(2, capitolo);  //capitolo

			// Execute the stored procedure and retrieve the IN/OUT value
			cs1.execute();

			//ibr.setCassa(cs1.getDouble(7));
			ibr.setCodiceRisposta(cs1.getInt(28));
			//ibr.setCompetenza(cs1.getDouble(3));
			ibr.setDescrizioneRisposta(cs1.getString(29));
			ibr.setDispCassa(cs1.getDouble(10));
			ibr.setDispCompetenza(cs1.getDouble(6));

			//ibr.setImpegni(cs1.getDouble(13));
			//ibr.setMandati(cs1.getDouble(16));
			//ibr.setPreImpegni(cs1.getDouble(12));
			//ibr.setResiduo(0);
			
			/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
			if (cs1 != null) {
				cs1.close();
			}
			/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
			
		}

		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		if (cs != null) {
			cs.close();
		}
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */

		return ibr;
	}

	//Modifica by Leo: nuovo metodo che accetta anche il parametro struttura
	public InterrogaBilancioResult interrogaBilancio(String annoBilancio, String capitolo, String struttura) throws SQLException
	{
		InterrogaBilancioResult ibr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call GENERO_DOCUMENTI.leggo_capitolo(?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(3, Types.VARCHAR); //descrizione_capitolo
		cs.registerOutParameter(4, Types.VARCHAR); //capitolo_por
		cs.registerOutParameter(5, Types.VARCHAR); //flag_perente
		cs.registerOutParameter(6, Types.VARCHAR); //conto_economica1
		cs.registerOutParameter(7, Types.VARCHAR); //conto_economica2
		cs.registerOutParameter(8, Types.VARCHAR); //conto_economica3
		cs.registerOutParameter(9, Types.VARCHAR); //conto_economica4
		cs.registerOutParameter(10, Types.VARCHAR); //conto_economica5
		cs.registerOutParameter(11, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(12, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT
		if (annoBilancio == null || annoBilancio.equals(""))
		{
			cs.setNull(1, Types.NUMERIC);
		}
		else
		{
			cs.setInt(1, Integer.parseInt(annoBilancio));  //anno
		}

		cs.setString(2, capitolo);  //capitolo

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		ibr = new InterrogaBilancioResult();
		if (cs.getInt(11)!=0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			ibr.setCodiceRisposta(cs.getInt(11));
			ibr.setDescrizioneRisposta(cs.getString(12));
		}
		else
		{
			ibr.setAnnoBilancio(Integer.parseInt(annoBilancio));
			ibr.setCodiceCapitolo(capitolo);
			ibr.setDescrizioneCapitolo(cs.getString(3));  
			ibr.setPor(cs.getString(4));
			ibr.setPerenti(cs.getString(5));
			ibr.setContoEconomica1(cs.getString(6));
			ibr.setContoEconomica2(cs.getString(7));
			ibr.setContoEconomica3(cs.getString(8));
			ibr.setContoEconomica4(cs.getString(9));
			ibr.setContoEconomica5(cs.getString(10));

			CallableStatement cs1;

			cs1 = connection.prepareCall("{call GENERO_DOCUMENTI.leggo_bilancio(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

			// Setto il tipo dei parametri di OUTPUT
			cs1.registerOutParameter(3, Types.NUMERIC); //competenza
			cs1.registerOutParameter(4, Types.NUMERIC); //variazione_competenza
			cs1.registerOutParameter(5, Types.NUMERIC); //assestamento_competenza
			cs1.registerOutParameter(6, Types.NUMERIC); //disponibilita_competenza 
			cs1.registerOutParameter(7, Types.NUMERIC); //cassa
			cs1.registerOutParameter(8, Types.NUMERIC); //variazione_cassa
			cs1.registerOutParameter(9, Types.NUMERIC); //assestamento_cassa
			cs1.registerOutParameter(10, Types.NUMERIC); //disponibilità_cassa
			cs1.registerOutParameter(11, Types.NUMERIC); //blocco_preimpegni
			cs1.registerOutParameter(12, Types.NUMERIC); //pre_impegni
			cs1.registerOutParameter(13, Types.NUMERIC); //impegni_accertamenti_compe
			cs1.registerOutParameter(14, Types.NUMERIC); //variazioni
			cs1.registerOutParameter(15, Types.NUMERIC); //liquidazioni_compe
			cs1.registerOutParameter(16, Types.NUMERIC); //mandati_reversali_compe
			cs1.registerOutParameter(17, Types.NUMERIC); //mandati_reversali_res
			cs1.registerOutParameter(18, Types.NUMERIC); //competenza_bloccata
			cs1.registerOutParameter(19, Types.NUMERIC); //cassa_bloccata
			cs1.registerOutParameter(20, Types.NUMERIC); //variazione_competenza_piu
			cs1.registerOutParameter(21, Types.NUMERIC); //variazione_competenza_meno
			cs1.registerOutParameter(22, Types.NUMERIC); //assestamento_competenza_piu
			cs1.registerOutParameter(23, Types.NUMERIC); //assestamento_competenza_meno
			cs1.registerOutParameter(24, Types.NUMERIC); //variazione_cassa_piu
			cs1.registerOutParameter(25, Types.NUMERIC); //variazione_cassa_meno
			cs1.registerOutParameter(26, Types.NUMERIC); //assestamento_cassa_piu
			cs1.registerOutParameter(27, Types.NUMERIC); //assestamento_cassa_meno
			cs1.registerOutParameter(28, Types.NUMERIC); //codice_risposta
			cs1.registerOutParameter(29, Types.VARCHAR); //descrizione_risposta

			// Setto il valore per i parametri di INPUT
			cs1.setInt(1, Integer.parseInt(annoBilancio));  //anno
			cs1.setString(2, capitolo);  //capitolo

			// Execute the stored procedure and retrieve the IN/OUT value
			cs1.execute();

			//ibr.setCassa(cs1.getDouble(7));
			ibr.setCodiceRisposta(cs1.getInt(28));
			//ibr.setCompetenza(cs1.getDouble(3));
			ibr.setDescrizioneRisposta(cs1.getString(29));
			ibr.setDispCassa(cs1.getDouble(10));
			ibr.setDispCompetenza(cs1.getDouble(6));

			//modifiche Leo: recupero la lista dei COG tramite una query
			if(connection!=null&&connection.isClosed())
				connection=getConnection();

			Statement s = null;
			s = connection.createStatement();

			ListaCOGType listaCOG = new ListaCOGTypeImpl();

			String query = "SELECT a.FIN_T_ASP_CODICE ||a.FIN_T_DIRETTRICE_CODICE  ||a.FIN_T_RISULTATI_CODICE   ||a.CODICE_PARTE1   ||a.CODICE_PARTE2   ||a.CODICE_PARTE3 COG,   "
					+ "			a.DESCRIZIONE,   "
					+ "			b.CAPITOLO_EU   ||b.CAPITOLO_CODICE CAPITOLO,   "
					+ "			a.VALIDO_DAL VALIDO_DAL,   "
					+ "			a.VALIDO_AL VALIDO_AL "
					+ "		FROM fin_t_obiettivi_gestionali a,   fin_t_obiettivi_gest_capitoli b "
					+ "			WHERE a.anno ='" + annoBilancio + "' AND "
							+ "		a.data_assestamento = to_date('01011900','ddmmyyyy') AND "
							+ "		a.fin_t_asp_anno = b.fin_t_asp_anno AND "
							+ "		a.fin_t_asp_codice = b.fin_t_asp_codice AND "
							+ "		a.fin_t_direttrice_anno = b.fin_t_direttrice_anno AND "
							+ "		a.fin_t_direttrice_codice = b.fin_t_direttrice_codice AND "
							+ "		a.fin_t_risultati_anno = b.fin_t_risultati_anno AND "
							+ "		a.fin_t_risultati_codice = b.fin_t_risultati_codice AND "
							+ "		a.anno = b.fin_t_og_anno AND "
							+ "		a.codice_parte1 = b.fin_t_og_codice_parte1 AND "
							+ "		a.codice_parte2 = b.fin_t_og_codice_parte2 AND "
							+ "		a.codice_parte3 = b.fin_t_og_codice_parte3 AND "
							+ "		NVL(a.data_assestamento,TRUNC(sysdate,'dd')) = NVL(b.data_assestamento,TRUNC(sysdate,'dd')) AND "
							+ "		b.capitolo_eu = SUBSTR('" + capitolo + "',1,1) AND "
							+ "		b.capitolo_codice = SUBSTR('" + capitolo + "',2,5) AND "
							+ "		a.ufficio = '" + struttura + "'"; 
			ResultSet rs = s.executeQuery(query);

			COGType cog = null;
			while (rs.next())
			{
				//aggiungo i COG
				cog = new COGTypeImpl();
				cog.setCodice(rs.getString("COG"));
				cog.setDescrizione(rs.getString("DESCRIZIONE"));
				listaCOG.getCOG().add(cog);
			}

			ibr.setListaCOG(listaCOG);

			//ibr.setImpegni(cs1.getDouble(13));
			//ibr.setMandati(cs1.getDouble(16));
			//ibr.setPreImpegni(cs1.getDouble(12));
			//ibr.setResiduo(0);
			
			/** simonebrunox 07/02/2018: inizio aggiungo missione_programma e verifica blocco */			
			s = connection.createStatement();
						
			if (cliente.equalsIgnoreCase("consiglio"))
				query = "select "
							+ "nvl(BLOCCO_PREIMPEGNI_IMPEGNI,'N') BLOCCO_PREIMPEGNI_IMPEGNI, "
							+ "nvl(competenza_bloccata,0) competenza_bloccata, "
							+ "a.liv1||'.'||a.liv2||'.'|| a.macro_agg||'.'|| a.NB_TITOLO miss_prog, "
							+ "nvl(d.attivo_provvedimenti,'N') attivo_provvedimenti, "
							+ "nvl(d.manutenzione_package, 'N') manutenzione_package, "
							+ "d.msg_manutenzione_package "							
						+ "from vw_nb_articoli a, fin_t_dati_generali d, fin_t_bilancio b "
						+ "where a.anno = "+annoBilancio+" and "
								+ "a.eu||a.codice = '"+capitolo+"' and "
								+ "nvl(a.abilitato,'N') = 'S' and "
								+ "b.anno=a.anno and "
								+ "b.eu||b.articolo=a.eu||a.codice and "
								+ "d.anno_finanziario = a.anno and "
								//+ "nvl(d.attivo_provvedimenti,'N') = 'S' and "
								+ "nvl(a.nb_fpv,'N') <> 'S'";
			else
				// if (cliente.equalsIgnoreCase("giunta") || cliente.equalsIgnoreCase("arpab") || cliente.equalsIgnoreCase("alsia"))
				query = "select "
							+ "nvl(BLOCCO_PREIMPEGNI_IMPEGNI,'N') BLOCCO_PREIMPEGNI_IMPEGNI, "
							+ "nvl(competenza_bloccata,0) competenza_bloccata, "
							+ "a.liv1||'.'||a.liv2 miss_prog, "
							+ "nvl(d.attivo_provvedimenti,'N') attivo_provvedimenti, "
							+ "nvl(d.manutenzione_package, 'N') manutenzione_package, "
							+ "d.msg_manutenzione_package "
						+ "from vw_nb_articoli a, fin_t_dati_generali d, fin_t_bilancio b "
						+ "where a.anno = "+annoBilancio+" and "
								+ "a.eu||a.codice = '"+capitolo+"' and "
								+ "nvl(a.abilitato,'N') = 'S' and "
								+ "b.anno=a.anno and	"
								+ "b.eu||b.articolo=a.eu||a.codice and	"
								+ "d.anno_finanziario = a.anno and	"
								//+ "nvl(d.attivo_provvedimenti,'N') = 'S' and "
								+ "nvl(a.nb_fpv,'N') <> 'S'";	

			rs = s.executeQuery(query);

			if (rs.next())
			{
				ibr.setMissioneProgramma(rs.getString("miss_prog"));
				double cassaBloccata = rs.getDouble("competenza_bloccata");
				if (rs.getString("attivo_provvedimenti").equalsIgnoreCase("N")) {
					ibr.setCodiceRispostaBlocco(2);
					ibr.setDescrizioneRispostaBlocco("Non attivo per l'applicazione provvedimenti.");					
				} else {				
					if (rs.getString("manutenzione_package").equalsIgnoreCase("S")) {
						ibr.setCodiceRispostaBlocco(3);
						ibr.setDescrizioneRispostaBlocco(rs.getString("msg_manutenzione_package"));					
					} else if (rs.getString("BLOCCO_PREIMPEGNI_IMPEGNI").equalsIgnoreCase("S")){
						ibr.setCodiceRispostaBlocco(1);
						ibr.setDescrizioneRispostaBlocco("Blocco Generazione Impegni: " + messaggioBlocco);
					} else {
						ibr.setCodiceRispostaBlocco(0);
						ibr.setDescrizioneRispostaBlocco("Nessun blocco");
						if (cassaBloccata!=0) {
							DecimalFormat df = new DecimalFormat("###,###.###"); 
							ibr.setDescrizioneRispostaBlocco("Blocchi su competenza per euro "+ df.format(cassaBloccata) + ". " + messaggioBlocco);
						}
					}
				}
			}

			s.close();			
			/** simonebrunox 07/02/2018: fine aggiungo missione_programma e verifica blocco */
		}
		return ibr;
	}


	/**
	 * @param generazionePreImpegno
	 * @return
	 */
	/*	public NumeroDocumentoResult generazionePreImpegno(GenerazionePreImpegnoTypes gpi) throws SQLException
	{
		NumeroDocumentoResult ndr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		 CallableStatement cs;

		 cs = connection.prepareCall("{call GENERO_DOCUMENTI.aggiorno_bilancio(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		 // Setto il tipo dei parametri di OUTPUT
		 cs.registerOutParameter(16, Types.NUMERIC); //codice_risposta
		 cs.registerOutParameter(17, Types.NUMERIC); //numero_documento_generato
		 cs.registerOutParameter(18, Types.VARCHAR); //descrizione_risposta

		 // Setto il valore per i parametri di INPUT
		 cs.setString(1, "PRE-IMP");  //tipo_documento
		 cs.setNull(2, Types.NUMERIC); //documento da cancellare non si setta

		 java.util.Date utilDate = gpi.getDataMovimento().getTime();
		 java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

		 cs.setDate(3, sqlDate);  //data_movimento
		 cs.setString(4, gpi.getOggettoPreimpegno());  //oggetto
		 cs.setString(5, gpi.getCodiceCapitoloUscita());  //capitolo
		 cs.setDouble(6, gpi.getImporto());  //Importo
		 cs.setString(7, gpi.getTipoAtto());  //Tipo Atto

		 utilDate = gpi.getDataAtto().getTime();
		 sqlDate = new java.sql.Date(utilDate.getTime());

		 cs.setDate(8, sqlDate);  //data_atto
		 cs.setInt(9, Integer.parseInt(gpi.getNumeroAtto()));  //numero atto

		 cs.setNull(10, Types.VARCHAR);  //dipartimento non si setta
		 cs.setNull(11, Types.VARCHAR);  //conto economica non si setta
		 cs.setNull(12, Types.NUMERIC);  //ratei non si setta
		 cs.setNull(13, Types.NUMERIC);  //imposta irap non si setta
		 cs.setNull(14, Types.NUMERIC);  //risconti non si setta
		 cs.setNull(15, Types.NUMERIC);  //importo iva non si setta


		 // Execute the stored procedure and retrieve the IN/OUT value
		 cs.execute();

		 ndr = new NumeroDocumentoResult();

		 if (cs.getInt(16)!=0) //SE codiceRisposta contiene la segnalazione di un errore
		 {
			ndr.setCodiceRisposta(cs.getInt(16));
			ndr.setDescrizioneRisposta(cs.getString(18));
		 }
		 else
		 {
			ndr.setNumeroDocumento(String.valueOf(cs.getInt(17)));
		 }

		 return ndr;
	}
	 */
	/**
	 * @param numeroPreimpegno
	 * @return
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 */
	/*	public NumeroDocumentoResult eliminazionePreImpegno(String numeroPreimpegno) throws SQLException 
	{
		NumeroDocumentoResult ndr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		 CallableStatement cs;

		 cs = connection.prepareCall("{call GENERO_DOCUMENTI.aggiorno_bilancio(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		 // Setto il tipo dei parametri di OUTPUT
		 cs.registerOutParameter(16, Types.NUMERIC); //codice_risposta
		 cs.registerOutParameter(17, Types.NUMERIC); //numero_documento_eliminato
		 cs.registerOutParameter(18, Types.VARCHAR); //descrizione_risposta

		 // Setto il valore per i parametri di INPUT
		 cs.setString(1, "DEL-PREIMP");  //tipo_documento

		 if (numeroPreimpegno.equals("") || numeroPreimpegno == null)
		 {
			 cs.setNull(2, Types.NUMERIC);
		 }
		 else
		 {
			 cs.setInt(2, Integer.parseInt(numeroPreimpegno));     //numero preimpegno da cancellare
		 }

		 java.util.Date utilDate = Calendar.getInstance().getTime();  // data corrente
		 java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

		 cs.setDate(3, sqlDate);  //data_movimento

		 cs.setNull(4, Types.VARCHAR);  //oggetto
		 cs.setNull(5, Types.VARCHAR);  //capitolo
		 cs.setNull(6, Types.DOUBLE);  //Importo
		 cs.setNull(7, Types.VARCHAR);  //Tipo Atto
		 cs.setNull(8, Types.DATE);  //data_atto
		 cs.setNull(9, Types.NUMERIC);  //numero atto
		 cs.setNull(10, Types.VARCHAR);  //dipartimento non si setta
		 cs.setNull(11, Types.VARCHAR);  //conto economica non si setta
		 cs.setNull(12, Types.NUMERIC);  //ratei non si setta
		 cs.setNull(13, Types.NUMERIC);  //imposta irap non si setta
		 cs.setNull(14, Types.NUMERIC);  //risconti non si setta
		 cs.setNull(15, Types.NUMERIC);  //importo iva non si setta

		 // Execute the stored procedure and retrieve the IN/OUT value
		 cs.execute();

		 ndr = new NumeroDocumentoResult();

		 if (cs.getInt(16)!=0) //SE codiceRisposta contiene la segnalazione di un errore
		 {
			ndr.setCodiceRisposta(cs.getInt(16));
			ndr.setDescrizioneRisposta(cs.getString(18));
		 }
		 else
		 {
			String ndoc = String.valueOf(cs.getInt(17));
			if (ndoc==null)
			{
				 ndr.setNumeroDocumento(numeroPreimpegno);
			}
			else
			{
				if (Integer.parseInt(ndoc)==0)
				{
					ndr.setNumeroDocumento(numeroPreimpegno);
				}
				else
				{
					ndr.setNumeroDocumento(String.valueOf(cs.getInt(17)));
				}
			}

		 }

		 return ndr;
	}
	 */
	/**
	 * @param generazioneImpegno
	 * @return
	 */
	/*	public NumeroDocumentoResult generazioneImpegno(GenerazioneImpegnoTypes gi) throws SQLException
	{
		NumeroDocumentoResult ndr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		 CallableStatement cs;

		 cs = connection.prepareCall("{call GENERO_DOCUMENTI.aggiorno_bilancio(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		 // Setto il tipo dei parametri di OUTPUT
		 cs.registerOutParameter(16, Types.NUMERIC); //codice_risposta
		 cs.registerOutParameter(17, Types.NUMERIC); //numero_documento_generato
		 cs.registerOutParameter(18, Types.VARCHAR); //descrizione_risposta

		 // Setto il valore per i parametri di INPUT
		 cs.setString(1, "IMP-DEF");  //tipo_documento
		 cs.setInt(2, Integer.parseInt(gi.getNumeroPreimpegno())); //documento da cancellare in questo caso rappresenta il documento padre ossia PRE-IMP

		 java.util.Date utilDate = gi.getDataMovimento().getTime();
		 java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

		 cs.setDate(3, sqlDate);  //data_movimento
		 cs.setNull(4, Types.VARCHAR);  //oggetto non si setta
		 cs.setNull(5, Types.VARCHAR);  //capitolo non si setta
		 cs.setDouble(6, gi.getImporto());  //Importo
		 cs.setString(7, gi.getTipoAtto());  //Tipo Atto

		 utilDate = gi.getDataAtto().getTime();
		 sqlDate = new java.sql.Date(utilDate.getTime());

		 cs.setDate(8, sqlDate);  //data_atto

		 if (gi.getNumeroAtto().equals(""))
		 {
			 cs.setNull(9, Types.NUMERIC);
		 }
		 else
		 {
			 cs.setInt(9, Integer.parseInt(gi.getNumeroAtto()));  //numero atto
		 }


		 cs.setString(10, gi.getDipartimento());  //dipartimento
		 cs.setString(11, gi.getContoEconomica());  //conto economica
		 cs.setDouble(12, gi.getRatei());  //ratei
		 cs.setDouble(13, gi.getImpostaIrap());  //imposta irap
		 cs.setDouble(14, gi.getRisconti());  //risconti
		 cs.setNull(15, Types.NUMERIC);  //importo iva non si setta


		 // Execute the stored procedure and retrieve the IN/OUT value
		 cs.execute();

		 ndr = new NumeroDocumentoResult();

		 if (cs.getInt(16)!=0) //SE codiceRisposta contiene la segnalazione di un errore
		 {
			ndr.setCodiceRisposta(cs.getInt(16));
			ndr.setDescrizioneRisposta(cs.getString(18));
		 }
		 else
		 {
			ndr.setNumeroDocumento(String.valueOf(cs.getInt(17)));
		 }

		 return ndr;
	}
	 */
	/**
	 * @param generazioneLiquidazione
	 * @return
	 */
	/*	public NumeroDocumentoResult generazioneLiquidazione(GenerazioneLiquidazioneTypes gl) throws SQLException
	{
		NumeroDocumentoResult ndr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		 CallableStatement cs;

		 cs = connection.prepareCall("{call GENERO_DOCUMENTI.aggiorno_bilancio(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		 // Setto il tipo dei parametri di OUTPUT
		 cs.registerOutParameter(16, Types.NUMERIC); //codice_risposta
		 cs.registerOutParameter(17, Types.NUMERIC); //numero_documento_generato
		 cs.registerOutParameter(18, Types.VARCHAR); //descrizione_risposta

		 // Setto il valore per i parametri di INPUT
		 cs.setString(1, "LIQ");  //tipo_documento
		 cs.setInt(2, Integer.parseInt(gl.getNumeroImpegno())); //documento da cancellare in questo caso rappresenta il documento padre ossia IMP-DEF

		 java.util.Date utilDate = gl.getDataMovimento().getTime();
		 java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

		 cs.setDate(3, sqlDate);  //data_movimento
		 cs.setNull(4, Types.VARCHAR);  //oggetto non si setta
		 cs.setNull(5, Types.VARCHAR);  //capitolo non si setta
		 cs.setDouble(6, gl.getImporto());  //Importo
		 cs.setString(7, gl.getTipoAtto());  //Tipo Atto

		 utilDate = gl.getDataAtto().getTime();
		 sqlDate = new java.sql.Date(utilDate.getTime());

		 cs.setDate(8, sqlDate);  //data_atto


		 if (gl.getNumeroAtto().equals(""))
		 {
			 cs.setNull(9, Types.NUMERIC);
		 }
		 else
		 {
			 cs.setInt(9, Integer.parseInt(gl.getNumeroAtto()));  //numero atto
		 }


		 cs.setString(10, gl.getStruttura());  //struttura o dipartimento
		 cs.setString(11, gl.getContoEconomica());  //conto economica
		 cs.setNull(12, Types.NUMERIC);  //ratei non si setta
		 cs.setDouble(13, Types.NUMERIC);  //imposta irap non si setta
		 cs.setNull(14, Types.NUMERIC);  //risconti non si setta
		 cs.setDouble(15, gl.getImportoIVA());  //importo iva



		 //Imposto l'eventuale allegato
		 if (gl.getAllegato() != null)
		 {
			 String causale = gl.getAllegato().getCausale();
			 String cf = gl.getAllegato().getCodiceFiscaleBeneficiario();
			 String cp = gl.getAllegato().getCodiceProgetto();
			 double importo = gl.getAllegato().getImporto();
//			TODO



		 }



		 // Execute the stored procedure and retrieve the IN/OUT value
		 cs.execute();

		 ndr = new NumeroDocumentoResult();

		 if (cs.getInt(16)!=0) //SE codiceRisposta contiene la segnalazione di un errore
		 {
			ndr.setCodiceRisposta(cs.getInt(16));
			ndr.setDescrizioneRisposta(cs.getString(18));
		 }
		 else
		 {
			ndr.setNumeroDocumento(String.valueOf(cs.getInt(17)));
		 }

		 return ndr;
	}
	 */
	/**
	 * @param numeroPreimpegno
	 * @return
	 */
	public DisponibilitaResult getDisponibilitaPreImp(String numeroPreimpegno, String struttura) throws SQLException
	{
		DisponibilitaResult dr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		//String query="select nvl(d.open_balance_proposed,0) disponibilita ,d.date_created data ,d.ATTRIBUTE13 tipo_atto ,d.attribute14 numero_atto ,d.data_atto_autorizzativo data_atto ,d.DESCRIPTION oggetto ,d.segment2 capitolo ,a.capitolo upb from fin_t_documenti d ,fin_t_articoli a where d.doc_type ='PRE-IMP' and d.doc_sequence_value = " + numeroPreimpegno + " and nvl(d.deletion_status_flag,'X') <> 'Y' and a.ANNO = d.segment8 and a.eu||a.CODICE = d.segment2";
		String query="select (nvl(d.open_balance_proposed,0) - nvl(p.importo_prenotato,0)) disponibilita ,d.date_created data ,d.ATTRIBUTE13 tipo_atto ,d.attribute14 numero_atto ,d.data_atto_autorizzativo data_atto ,d.DESCRIPTION oggetto ,d.segment2 capitolo ,a.capitolo upb,nb1.codice||'.'||nb2.codice miss_prog  from   fin_t_dati_generali g, fina_documenti d, fina_prenota_preimp p,fina_articoli a,nb_livello1 nb1, nb_livello2 nb2 where d.doc_type ='PRE-IMP' and d.doc_sequence_value = " + numeroPreimpegno + " and nvl(d.deletion_status_flag,'X') <> 'Y' and p.numero_preimpegno(+) = d.doc_sequence_value and a.ANNO = d.segment8 and a.eu||a.CODICE = d.segment2 and a.nb_id_padre = nb2.id ( + ) and nb1.id (+ ) = nb2.id_livello1   and g.anno_finanziario = a.anno and nvl(g.attivo_provvedimenti,'N') = 'S'";
		// 24/11/2016 Ferrante
		//String query = "SELECT decode( sign(disponibilita_preimp (d.segment8,d.doc_sequence_value,'"+struttura+"', sysdate) - nvl(p.importo_prenotato,0)),-1,0,(disponibilita_preimp (d.segment8,d.doc_sequence_value,'"+struttura+"', sysdate) - nvl(p.importo_prenotato,0)) ) disponibilita, d.date_created data, d.attribute13 tipo_atto, d.attribute14 numero_atto, d.data_atto_autorizzativo data_atto, d.description oggetto, d.segment2 capitolo, a.capitolo upb, nb1.codice || '.' ||  nb2.codice miss_prog FROM fin_t_dati_generali g, fina_documenti d, fina_prenota_preimp p, fina_articoli a, nb_livello1 nb1, nb_livello2 nb2 WHERE d.doc_type = 'PRE-IMP' AND d.doc_sequence_value = " + numeroPreimpegno + " AND nvl(d.deletion_status_flag,'X') <> 'Y' AND p.numero_preimpegno (+) = d.doc_sequence_value AND a.anno = d.segment8 AND a.eu || a.codice = d.segment2	AND a.nb_id_padre = nb2.id (+) AND nb1.id (+) = nb2.id_livello1 AND g.anno_finanziario = a.anno AND nvl(g.attivo_provvedimenti,'N') = 'S' AND (d.doc_sequence_value IN (SELECT r.ID_PREIMPEGNO FROM fin_t_cdg_richieste r WHERE r.struttura_richiedente = '"+struttura+"' AND TRUNC(sysdate,'dd') BETWEEN TRUNC(r.data_creazione,'dd') AND TRUNC(r.data_fine_cdg,'dd') AND NVL(r.fl_decisione_cdg,'N') = 'A') OR d.segment3 = '"+struttura+"')";
		ResultSet rs = s.executeQuery(query);


		if (rs.next())
		{
			dr = new DisponibilitaResult();
			dr.setDisponibilita(rs.getDouble("disponibilita"));

			//Michele Modifica
			java.sql.Date dataS = rs.getDate("data");
			java.util.Date dataU = new Date(dataS.getTime());
			dr.setDataPreimpegno(dataU);
			dr.setCodiceCapitolo(rs.getString("capitolo"));
			dr.setUPB(rs.getString("upb"));
			dr.setMissioneProgramma(rs.getString("miss_prog"));
			dr.setTipoAtto(rs.getString("tipo_atto"));
			java.sql.Date dataS2 = rs.getDate("data_atto");
			java.util.Date dataU2 = new Date(dataS2.getTime());
			dr.setDataAtto(dataU2);
			dr.setNumeroAtto(rs.getString("numero_atto"));
			dr.setOggettoPreimpegno(rs.getString("oggetto"));
			//Michele Modifica Fine
		}

		s.close();

		return dr;
	}

	//Modifica Leo: aggiungere il parametro struttura
	public List getImpegniAperti(String annoFinanziario, String codiceCapitolo, String struttura)  throws SQLException {
		List r = new ArrayList();
		InterrogaImpegniApertiResult iiar = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		//Modificata la query sulla base della mail di Ferrante del 30/11/2011
		//String query = "SELECT doc_type,doc_sequence_value numero_imp ,NVL(document_amount,0) importo_iniziale, NVL(open_balance_proposed,0) importo_disponibile ,description oggetto_imp ,date_created data_imp ,attribute13 tipo_atto ,attribute14 numero_atto ,data_atto_autorizzativo data_atto, nvl(CODICE_ASP||CODICE_DIRETTRICE||CODICE_RISULTATI||CODICE_OG_PARTE1||CODICE_OG_PARTE2||CODICE_OG_PARTE3,'XXXXXXXXXX') COG FROM fin_t_documenti a, bas_ind b,  fin_t_dati_generali g WHERE b.ind_cap = '"+codiceCapitolo+"' AND b.ind_anno = '"+annoFinanziario+"' AND a.doc_type LIKE 'IMP%' AND b.doc_id= a.doc_id AND NVL(a.deletion_status_flag,'X')<>'Y' AND NVL(a.open_balance_proposed,0)  > 0 AND NVL(a.proposal_status_flag,'X')||NVL(a.auditing_status_flag,'X')='PI' AND ('"+struttura+"' = (select distinct s.fin_t_strutture_codice from fin_t_strutture_capitoli s where s.capitolo_anno = '"+annoFinanziario+"' and s.capitolo_eu||s.capitolo_codice = '"+codiceCapitolo+"' and s.fin_t_strutture_codice = '"+struttura+"' and (s.titolarita in ('T','C') OR (s.titolarita = 'D' and nvl(s.importo_condivisione,0) > 0) and trunc(sysdate,'dd') between trunc(s.data_i_condivisione,'dd') and trunc(s.data_f_condivisione,'dd')) and s.data_assestamento = to_date('01011900','ddmmyyyy')) OR a.doc_sequence_value IN (select i.numero_impegno from fin_t_stru_cap_impegni i, fin_t_cdg_richieste r where r.id = i.richiesta and r.struttura_richiedente = '"+struttura+"' and trunc(sysdate,'dd') between trunc(r.data_creazione,'dd') and trunc(r.data_fine_cdg,'dd') and nvl(r.fl_decisione_cdg,'N') = 'A') or a.segment3 = '"+struttura+"')   and g.anno_finanziario = b.ind_anno and nvl(g.attivo_provvedimenti,'N') = 'S' ";
		
		
		//String query = "SELECT doc_type,   doc_sequence_value numero_imp ,   NVL(document_amount,0) importo_iniziale,   NVL(open_balance_proposed,0) importo_disponibile ,   description oggetto_imp ,   date_created data_imp ,   attribute13 tipo_atto ,   attribute14 numero_atto ,   data_atto_autorizzativo data_atto,   NVL(CODICE_ASP   ||CODICE_DIRETTRICE   ||CODICE_RISULTATI   ||CODICE_OG_PARTE1   ||CODICE_OG_PARTE2   ||CODICE_OG_PARTE3,'XXXXXXXXXX') COG FROM fin_t_documenti a,   bas_ind b,   fin_t_dati_generali g WHERE b.ind_cap = '"+codiceCapitolo+"' AND b.ind_anno  = '"+annoFinanziario+"' AND a.doc_type LIKE 'IMP%' AND b.doc_id                        = a.doc_id AND NVL(a.deletion_status_flag,'X')<>'Y' AND NVL(a.open_balance_proposed,0)  > 0 AND NVL(a.proposal_status_flag,'X')   ||NVL(a.auditing_status_flag,'X')='PI' AND ('"+struttura+"'               =   (SELECT DISTINCT s.fin_t_strutture_codice   FROM fin_t_strutture_capitoli s   WHERE s.capitolo_anno = '"+annoFinanziario+"'   AND s.capitolo_eu     ||s.capitolo_codice             = '"+codiceCapitolo+"'   AND s.fin_t_strutture_codice      = '"+struttura+"'   AND (s.titolarita                IN ('T','C')   OR (s.titolarita                  = 'D'   AND NVL(s.importo_condivisione,0) > 0)   AND TRUNC(sysdate,'dd') BETWEEN TRUNC(s.data_i_condivisione,'dd') AND TRUNC(s.data_f_condivisione,'dd'))   AND s.data_assestamento = to_date('01011900','ddmmyyyy')   ) OR a.doc_sequence_value IN   (SELECT i.numero_impegno   FROM fin_t_stru_cap_impegni i,     fin_t_cdg_richieste r   WHERE r.id                  = i.richiesta   AND r.struttura_richiedente = '"+struttura+"'   AND TRUNC(sysdate,'dd') BETWEEN TRUNC(r.data_creazione,'dd') AND TRUNC(r.data_fine_cdg,'dd')   AND NVL(r.fl_decisione_cdg,'N') = 'A'   ) OR a.segment3                       = '"+struttura+"') AND g.anno_finanziario              = b.ind_anno AND NVL(g.attivo_provvedimenti,'N') = 'S' AND a.doc_sequence_value not in (select p.doc_sequence_value from fin_t_perenti p where nvl(p.auditing_status_flag,'X') = 'I' and p.segment8 <= b.ind_anno - 2) " ;
		int annoPrec = Integer.valueOf(annoFinanziario).intValue() - 1;
		
		//String query="SELECT doc_type,   doc_sequence_value numero_imp ,   NVL(document_amount,0) importo_iniziale,   NVL(open_balance_proposed,0) importo_disponibile ,   description oggetto_imp ,   date_created data_imp ,   attribute13 tipo_atto ,   attribute14 numero_atto , attribute16 pcf , data_atto_autorizzativo data_atto,   NVL(CODICE_ASP   ||CODICE_DIRETTRICE   ||CODICE_RISULTATI   ||CODICE_OG_PARTE1   ||CODICE_OG_PARTE2   ||CODICE_OG_PARTE3,'XXXXXXXXXX') COG FROM fin_t_documenti a,   bas_ind b,   fin_t_dati_generali g WHERE b.ind_cap = '"+codiceCapitolo+"' AND b.ind_anno  = '"+annoFinanziario+"' AND a.doc_type LIKE 'IMP%' AND b.doc_id                        = a.doc_id AND NVL(a.deletion_status_flag,'X')<>'Y' AND NVL(a.open_balance_proposed,0)  > 0 AND NVL(a.proposal_status_flag,'X')   ||NVL(a.auditing_status_flag,'X')='PI' AND (a.doc_sequence_value IN   (SELECT i.numero_impegno   FROM fin_t_stru_cap_impegni i,     fin_t_cdg_richieste r   WHERE r.id                  = i.richiesta   AND r.struttura_richiedente = '"+struttura+"'   AND TRUNC(sysdate,'dd') BETWEEN TRUNC(r.data_creazione,'dd') AND TRUNC(r.data_fine_cdg,'dd')   AND NVL(r.fl_decisione_cdg,'N') = 'A' ) OR a.segment3                       = '"+struttura+"') AND g.anno_finanziario              = b.ind_anno AND NVL(g.attivo_provvedimenti,'N') = 'S' AND a.doc_sequence_value not in (select p.doc_sequence_value from fin_t_perenti p where nvl(p.auditing_status_flag,'X') = 'I' and p.segment8 <= b.ind_anno - 2) ";
		/*String query="SELECT doc_type, doc_sequence_value numero_imp , NVL(document_amount,0) importo_iniziale, NVL(open_balance_data(a.doc_id,trunc(sysdate,'dd')),0) importo_disponibile , description oggetto_imp , date_created data_imp , attribute13 tipo_atto , attribute14 numero_atto , attribute16 pcf , data_atto_autorizzativo data_atto, NVL(CODICE_ASP ||CODICE_DIRETTRICE ||CODICE_RISULTATI ||CODICE_OG_PARTE1 ||CODICE_OG_PARTE2  ||CODICE_OG_PARTE3,'XXXXXXXXXX') COG	FROM fin_t_documenti a, bas_ind b, fin_t_dati_generali g "
				+ "WHERE b.ind_cap    = '"+codiceCapitolo+"' AND b.ind_anno = '"+annoFinanziario+"' AND a.doc_type LIKE 'IMP%' AND b.doc_id = a.doc_id AND NVL(a.deletion_status_flag,'X')<>'Y' AND NVL(open_balance_data(a.doc_id,trunc(sysdate,'dd')),0) > 0 AND NVL(a.proposal_status_flag,'X') ||NVL(a.auditing_status_flag,'X')='PI'	"
						+ "AND (a.doc_sequence_value IN (SELECT i.numero_impegno FROM fin_t_stru_cap_impegni i, fin_t_cdg_richieste r WHERE r.id = i.richiesta AND r.struttura_richiedente = '"+struttura+"' AND TRUNC(sysdate,'dd') BETWEEN TRUNC(r.data_creazione,'dd') AND TRUNC(r.data_fine_cdg,'dd') AND NVL(r.fl_decisione_cdg,'N') = 'A') OR a.segment3 = '"+struttura+"') AND g.anno_finanziario = b.ind_anno	AND NVL(g.attivo_provvedimenti,'N') = 'S'"; // 17042015 Francesco Ferrante: Serve per evitare di inviare a Provvedimenti una lista impegni con un importo_residuo non corretto
		 */
		// Francesco Ferrante 23/03/2016: modifica per la nuova gestione dei riaccertamenti
		String query="SELECT doc_type, doc_sequence_value numero_imp , NVL(document_amount,0) importo_iniziale, (NVL(open_balance_data(a.doc_id,TRUNC(sysdate,'dd')),0)- importo_riaccertamento(a.doc_id)) importo_disponibile , description oggetto_imp , date_created data_imp , attribute13 tipo_atto , attribute14 numero_atto , attribute16 pcf , data_atto_autorizzativo data_atto, NVL(CODICE_ASP ||CODICE_DIRETTRICE ||CODICE_RISULTATI ||CODICE_OG_PARTE1 ||CODICE_OG_PARTE2 ||CODICE_OG_PARTE3,'XXXXXXXXXX') COG "
				+ "FROM fin_t_documenti a, bas_ind b, fin_t_dati_generali g "
				+ "WHERE b.ind_cap = '"+codiceCapitolo+"' AND b.ind_anno  = '"+annoFinanziario+"' AND a.doc_type LIKE 'IMP%' AND b.doc_id = a.doc_id AND NVL(a.deletion_status_flag,'X') <>'Y' AND (NVL(open_balance_data(a.doc_id,TRUNC(sysdate,'dd')),0)- importo_riaccertamento(a.doc_id)) > 0 AND NVL(a.proposal_status_flag,'X') ||NVL(a.auditing_status_flag,'X')='PI' "
						+ "AND (a.doc_sequence_value IN (SELECT i.numero_impegno FROM fin_t_stru_cap_impegni i, fin_t_cdg_richieste r  WHERE r.id = i.richiesta AND r.struttura_richiedente = '"+struttura+"' AND TRUNC(sysdate,'dd') BETWEEN TRUNC(r.data_creazione,'dd') AND TRUNC(r.data_fine_cdg,'dd')  AND NVL(r.fl_decisione_cdg,'N') = 'A') OR a.segment3 = '"+struttura+"') AND g.anno_finanziario = b.ind_anno AND NVL(g.attivo_provvedimenti,'N') = 'S'";				
		  
		//da inserire ogni anno
		//AND a.doc_sequence_value not in (select ri.numero_documento from fin_t_riaccertamenti ri where ri.tipo_documento like 'IMP%' and nvl(ri.importo_riaccertamento,0) > 0 and ri.anno_finanziario ='"+ annoPrec +"')
		ResultSet rs = s.executeQuery(query);
		
		while (rs.next())
		{
			iiar = new InterrogaImpegniApertiResult();

			if (rs.getDate("data_atto") == null)
			{
				iiar.setDataAtto(null);
			}
			else
			{
				java.sql.Date dataS = rs.getDate("data_atto");
				java.util.Date dataU = new Date(dataS.getTime());
				iiar.setDataAtto(dataU);
			}

			if (rs.getDate("data_imp") == null)
			{
				iiar.setDataImpegno(null);
			}
			else
			{
				java.sql.Date dataS2 = rs.getDate("data_imp");
				java.util.Date dataU2 = new Date(dataS2.getTime());
				iiar.setDataImpegno(dataU2);
			}

			//iiar.setUtilizzabile(rs.getString("utilizzabile"));
			iiar.setUtilizzabile(null);

			//iiar.setMotivoNonUtilizzabile(rs.getString("motivo_non_utilizzabile"));
			iiar.setMotivoNonUtilizzabile(null);

			iiar.setTipoImpegno(rs.getString("doc_type"));
			iiar.setImportoDisponibile(rs.getDouble("importo_disponibile"));
			iiar.setImportoIniziale(rs.getDouble("importo_iniziale"));
			iiar.setNumeroAtto(rs.getString("numero_atto"));
			iiar.setNumeroImpegno(rs.getString("numero_imp"));
			iiar.setOggettoImpegno(rs.getString("oggetto_imp"));
			iiar.setTipoAtto(rs.getString("tipo_atto"));
			
			iiar.setCodicePCF(rs.getString("pcf"));
			iiar.setDescrizionePCF("");
			//Modifica Leo: settare il valore COG all'oggetto di ritorno
			iiar.setCOG(rs.getString("COG"));			

			r.add(iiar);
		}

		s.close();

		return r;
	}

	public List getPreimpegniAperti(String annoFinanziario,	String codiceCapitolo, String struttura)  throws SQLException {
		/*
		 * Se in fint_bilancio i campi blocco_preimpegni_impegni  vale 'S' devo restituire come codiceRisposta 2 
		 * Nella descrizioneCapitolo devo inserire 'Blocco Generazione Liquidazioni' 
		 */
		List r = new ArrayList();
		InterrogaPreimpegniApertiResult ipiar = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();
		String query = "select  a.doc_sequence_value numero_imp , nvl(a.document_amount,0) importo_iniziale , (nvl(a.open_balance_proposed,0) - nvl(b.importo_prenotato,0)) importo_disponibile , a.description oggetto_imp , a.date_created data_imp , a.attribute13 tipo_atto , a.attribute14 numero_atto , a.attribute16 pcf ,  a.data_atto_autorizzativo data_atto  from  fina_documenti a, fina_prenota_preimp b, FIN_T_BILANCIO C where a.doc_type = 'PRE-IMP' AND A.SEGMENT8 = C.ANNO AND A.SEGMENT2 = C.EU||C.ARTICOLO AND NVL(C.BLOCCO_PREIMPEGNI_IMPEGNI, 'N') <> 'S'  and nvl(a.deletion_status_flag,'X')<>'Y'  and a.segment2 = '"+codiceCapitolo+"'  and a.segment8 = '"+annoFinanziario+"'  and nvl(a.open_balance_proposed,0) > 0  and b.numero_preimpegno(+) = a.doc_sequence_value";
		// 24/11/2016 Ferrante
		//String query = "SELECT a.doc_sequence_value numero_imp, nvl(a.document_amount,0) importo_iniziale, decode(sign(disponibilita_preimp ('"+annoFinanziario+"',a.doc_sequence_value,'"+struttura+"', sysdate) - nvl(b.importo_prenotato,0)),-1,0,(disponibilita_preimp ('"+annoFinanziario+"',a.doc_sequence_value,'"+struttura+"', sysdate) - nvl(b.importo_prenotato,0))) importo_disponibile, a.description oggetto_imp, a.date_created data_imp, a.attribute13 tipo_atto, a.attribute14 numero_atto, a.attribute16 pcf, a.data_atto_autorizzativo data_atto FROM fina_documenti a, fina_prenota_preimp b, fin_t_bilancio c WHERE a.doc_type = 'PRE-IMP' AND a.segment8 = c.anno AND a.segment2 = c.eu || c.articolo AND nvl(c.blocco_preimpegni_impegni,'N') <> 'S' AND nvl(a.deletion_status_flag,'X') <> 'Y' AND a.segment2 = '"+codiceCapitolo+"' AND a.segment8 = '"+annoFinanziario+"' AND nvl(a.open_balance_proposed,0) > 0 AND b.numero_preimpegno (+) = a.doc_sequence_value AND (a.doc_sequence_value IN (SELECT r.ID_PREIMPEGNO FROM fin_t_cdg_richieste r WHERE r.struttura_richiedente = '"+struttura+"' AND TRUNC(sysdate,'dd') BETWEEN TRUNC(r.data_creazione,'dd') AND TRUNC(r.data_fine_cdg,'dd') AND NVL(r.fl_decisione_cdg,'N') = 'A') OR a.segment3 = '"+struttura+"')";
		ResultSet rs = s.executeQuery(query);


		while (rs.next())
		{
			ipiar = new InterrogaPreimpegniApertiResult();

			if (rs.getDate("data_atto") == null)
			{
				ipiar.setDataAtto(null);
			}
			else
			{
				java.sql.Date dataS = rs.getDate("data_atto");
				java.util.Date dataU = new Date(dataS.getTime());
				ipiar.setDataAtto(dataU);
			}

			if (rs.getDate("data_imp") == null)
			{
				ipiar.setDataPreimpegno(null);
			}
			else
			{
				java.sql.Date dataS2 = rs.getDate("data_imp");
				java.util.Date dataU2 = new Date(dataS2.getTime());
				ipiar.setDataPreimpegno(dataU2);
			}

			ipiar.setImportoDisponibile(rs.getDouble("importo_disponibile"));
			ipiar.setImportoIniziale(rs.getDouble("importo_iniziale"));
			ipiar.setNumeroAtto(rs.getString("numero_atto"));
			ipiar.setNumeroPreimpegno(rs.getString("numero_imp"));
			ipiar.setOggettoPreimpegno(rs.getString("oggetto_imp"));
			ipiar.setTipoAtto(rs.getString("tipo_atto"));
			
			ipiar.setCodicePCF(rs.getString("pcf"));
			ipiar.setDescrizionePCF("");

			r.add(ipiar);
		}

		s.close();

		return r;
	}

	public PrenotazionePreimpegnoResult getPrenotazionePreimpegno(String tipoOperazione,String numeroPreimpegno, double importo) throws SQLException {
		PrenotazionePreimpegnoResult ppr = new PrenotazionePreimpegnoResult();

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call GENERO_DOCUMENTI.prenotazione_preimpegni(?,?,?,?,?)}");

		// Setto il valore per i parametri di INPUT
		cs.setString(1, tipoOperazione);  //tipo Operazione P o C
		cs.setString(2, numeroPreimpegno);  //numero preimpegno
		cs.setDouble(3, importo); //importo

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(4, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(5, Types.VARCHAR); //descrizione_risposta

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();


		ppr.setCodiceRisposta(cs.getInt(4));
		ppr.setDescrizioneRisposta(cs.getString(5));

		return ppr;
	}

	public NumeroDocumentoResult createDelDocumento(CreateDelDocumentoTypes createDelDocumento) throws SQLException {
		//Modificata da Angelo chiamata stored procedure  GENERO_DOCUMENTI.aggiorno_bilancio
		NumeroDocumentoResult ndr = null;
		java.util.Date utilDate;
		java.sql.Date sqlDate;
		int idTerzeParti = 0;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call GENERO_DOCUMENTI.aggiorno_bilancio(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(46, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(47, Types.NUMERIC); //numero_documento_generato
		cs.registerOutParameter(48, Types.NUMERIC); //numero_documento_pluriennale1
		cs.registerOutParameter(49, Types.NUMERIC); //numero_documento_pluriennale2
		cs.registerOutParameter(50, Types.VARCHAR); //descrizione_risposta
		cs.registerOutParameter(51, Types.NUMERIC); //docid
		cs.registerOutParameter(52, Types.NUMERIC); //docid1
		cs.registerOutParameter(53, Types.NUMERIC); //docid2

		// Setto il valore per i parametri di INPUT
		cs.setString(1, createDelDocumento.getTipoDocumento());  //tipo_documento


		if (createDelDocumento.getNumeroDocumentoPC() == null)
		{
			cs.setNull(2, Types.NUMERIC);  //numero del documento padre o da cancellare non si setta			 
		}
		else
		{
			cs.setInt(2, Integer.parseInt(createDelDocumento.getNumeroDocumentoPC())); //numero del documento padre o da cancellare
		}

		if (createDelDocumento.getDataMovimento() == null) //Se la data non è presente allora imposta la data odierna
		{
			utilDate = Calendar.getInstance().getTime();
			sqlDate = new java.sql.Date(utilDate.getTime());
			cs.setDate(3, sqlDate);  //data_movimento
		}
		else
		{
			utilDate = createDelDocumento.getDataMovimento().getTime();
			sqlDate = new java.sql.Date(utilDate.getTime());
			cs.setDate(3, sqlDate);  //data_movimento
		}


		if (createDelDocumento.getOggetto() == null)
		{
			cs.setNull(4, Types.VARCHAR);  //oggetto non si setta
		}
		else
		{
			cs.setString(4, createDelDocumento.getOggetto());  //oggetto 
		}

		if (createDelDocumento.getCodiceCapitolo() == null)
		{
			cs.setNull(5, Types.VARCHAR);  //capitolo non si setta			 
		}
		else
		{
			cs.setString(5, createDelDocumento.getCodiceCapitolo());  //capitolo 
		}

		if (createDelDocumento.getImporto() == 0)
		{
			cs.setNull(6, Types.NUMERIC);  //importo non si setta			 
		}
		else
		{
			cs.setDouble(6, createDelDocumento.getImporto());  //Importo
		}

		if (createDelDocumento.getImportoPlur1() == 0)
		{
			cs.setNull(7, Types.NUMERIC);  //importo pluriennale 1 non si setta			 
		}
		else
		{
			cs.setDouble(7, createDelDocumento.getImportoPlur1());  //Importo pluriennale 1
		}

		if (createDelDocumento.getImportoPlur2() == 0)
		{
			cs.setNull(8, Types.NUMERIC);  //importo pluriennale 2 non si setta			 
		}
		else
		{
			cs.setDouble(8, createDelDocumento.getImportoPlur2());  //Importo pluriennale 2
		}

		if (createDelDocumento.getTipoAtto() == null)
		{
			cs.setNull(9, Types.VARCHAR);  //Tipo Atto non si setta			 
		}
		else
		{
			cs.setString(9, createDelDocumento.getTipoAtto());  //Tipo Atto
		}

		if (createDelDocumento.getDataAtto() == null)
		{
			cs.setNull(10, Types.DATE);  //data dell'atto non si setta
		}
		else
		{
			utilDate = createDelDocumento.getDataAtto().getTime();
			sqlDate = new java.sql.Date(utilDate.getTime());
			cs.setDate(10, sqlDate);  //data_atto
		}

		if (createDelDocumento.getNumeroAtto() == null)
		{
			cs.setNull(11, Types.NUMERIC);  //numero dell'atto non si setta
		}
		else
		{
			cs.setInt(11, Integer.parseInt(createDelDocumento.getNumeroAtto()));  //numero atto
		}

		if (createDelDocumento.getStruttura() == null)
		{
			cs.setNull(12, Types.VARCHAR);  //struttura non si setta
		}
		else
		{
			cs.setString(12, createDelDocumento.getStruttura());  //struttura o dipartimento
		}


		if (createDelDocumento.getContoEconomica() == null)
		{
			cs.setNull(13, Types.VARCHAR);  //conto econmica non si setta
		}
		else
		{
			cs.setString(13, createDelDocumento.getContoEconomica());  //conto economica
		}

		if(createDelDocumento.getCodiceFiscale()==null){
			cs.setNull(14, Types.VARCHAR);  //codice fiscale operatore non si setta
		}
		else
		{
			cs.setString(14, createDelDocumento.getCodiceFiscale());  //codice fiscale operatore
		}

		if (createDelDocumento.getRatei() == 0)
		{
			cs.setNull(15, Types.NUMERIC);  //ratei non si setta			 
		}
		else
		{
			cs.setDouble(15, createDelDocumento.getRatei());  //ratei
		}

		if (createDelDocumento.getImpostaIrap() == 0)
		{
			cs.setNull(16, Types.NUMERIC);  //imposta IRAP non si setta			 
		}
		else
		{
			cs.setDouble(16, createDelDocumento.getImpostaIrap());  //imposta irap
		}

		if (createDelDocumento.getRisconti() == 0)
		{
			cs.setNull(17, Types.NUMERIC);  //risconti non si setta			 
		}
		else
		{
			cs.setDouble(17, createDelDocumento.getRisconti());  //risconti
		}

		if (createDelDocumento.getImportoIVA() == 0)
		{
			cs.setNull(18, Types.NUMERIC);  //importo IVA non si setta			 
		}
		else
		{
			cs.setDouble(18, createDelDocumento.getImportoIVA());  //importo iva 
		}

		//Imposto l'eventuale allegato
		if (createDelDocumento.getAllegato() != null)
		{
			String causale = createDelDocumento.getAllegato().getCausale();
			String cf = createDelDocumento.getAllegato().getCodiceFiscaleBeneficiario();
			String cp = createDelDocumento.getAllegato().getCodiceProgetto();
			double importo = createDelDocumento.getAllegato().getImporto();
			//			TODO
		}

		//modifica by leo: impostare anche il COG con modifica alla stored procedure
		if(createDelDocumento.getCOG() == null)
		{
			cs.setNull(19, Types.VARCHAR);  //COG non si setta		
		}
		else
		{
			cs.setString(19, createDelDocumento.getCOG());      //COG
		}

		if(createDelDocumento.getCOGPlur1() == null)
		{
			cs.setNull(20, Types.VARCHAR);  //COG Pluriennale 1 non si setta		
		}
		else
		{
			cs.setString(20, createDelDocumento.getCOGPlur1());      //COG Pluriennale 1
		}

		if(createDelDocumento.getCOGPlur2() == null)
		{
			cs.setNull(21, Types.VARCHAR);  //COG Pluriennale 2 non si setta		
		}
		else
		{
			cs.setString(21, createDelDocumento.getCOGPlur2());      //COG Pluriennale 2
		}

		if(createDelDocumento.getValido770() == null)
		{
			cs.setNull(22, Types.VARCHAR);  //Valido770 non si setta		
		}
		else
		{
			if (createDelDocumento.getValido770().equals("S"))
			{
				cs.setString(22, "Y");      //Valido770
			}
			else
			{
				cs.setString(22, createDelDocumento.getValido770());      //Valido770
			}
		}

		if(createDelDocumento.getTipologiaReddito() == null)
		{
			cs.setNull(23, Types.VARCHAR);  //Tipologia Reddito non si setta		
		}
		else
		{
			cs.setString(23, createDelDocumento.getTipologiaReddito());      //Tipologia Reddito
		}

		if (createDelDocumento.getImponibileIrap() == 0)
		{
			cs.setNull(24, Types.NUMERIC);  //imponibile IRAP non si setta			 
		}
		else
		{
			cs.setDouble(24, createDelDocumento.getImponibileIrap());  //imponibile IRAP 
		}		 

		//Gestisco la lista dei beneficiari se presente
		if (createDelDocumento.getBeneficiari() != null)
		{

			List beneficiarioTypeList = createDelDocumento.getBeneficiari().getBeneficiario();

			Statement stSeq = connection.createStatement();
			ResultSet rsSeq = stSeq.executeQuery("SELECT ID_SEQ.nextval progressivo FROM DUAL");

			if (rsSeq.next()) 
			{
				idTerzeParti = rsSeq.getInt("progressivo");
			}
			stSeq.close();

			if (idTerzeParti != 0)
			{
				for ( Iterator iter = beneficiarioTypeList.iterator(); iter.hasNext(); )
				{			 
					it.latraccia.entity.sic.richiesta.BeneficiarioTypes item = (it.latraccia.entity.sic.richiesta.BeneficiarioTypes)iter.next();

					Statement stmt = connection.createStatement();

					String esenzCommBon=null;
					if (item.getEsenzCommBonifico().equals("S"))
					{
						esenzCommBon = "Y";
					}
					else
					{
						esenzCommBon = item.getEsenzCommBonifico();
					}

					String bollo=null;
					if (item.getBollo().equals("S"))
					{
						bollo = "Y";
					}
					else
					{
						bollo = item.getBollo();
					}

					String stampaAvv=null;
					if (item.getStampaAvviso().equals("S"))
					{
						stampaAvv = "Y";
					}
					else
					{
						stampaAvv = item.getStampaAvviso();
					}
					
					String datoSensibile="";
					if (item.isDatoSensibile()){
						datoSensibile="S";	
					}
					else{
						datoSensibile="N";
					}

					String queryInt = "INSERT INTO FIN_T_DOCUMENTI_TERZE_PARTI_AP(ID, AMOUNT, PREVIOUS_DOC_ID, REMARKS, ATTRIBUTE1, ATTRIBUTE2," +
					" ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5, ATTRIBUTE6, ATTRIBUTE7, ATTRIBUTE8, ATTRIBUTE9, ATTRIBUTE10, ATTRIBUTE11," +
					" ATTRIBUTE12, ATTRIBUTE13, ATTRIBUTE14, ATTRIBUTE15, ATTRIBUTE16, ATTRIBUTE17, ATTRIBUTE18, ATTRIBUTE19, ATTRIBUTE20," +
					" ATTRIBUTE21, ATTRIBUTE22, ATTRIBUTE23, ATTRIBUTE24, ATTRIBUTE25, ATTRIBUTE26, ATTRIBUTE27, ATTRIBUTE28, ATTRIBUTE29," +
					" ATTRIBUTE30, CREATED_BY, CREATION_DATE, LAST_UPDATED_BY, LAST_UPDATE_DATE, ID_ANA, ID_SEDE, ID_PAGAMENTO, ID_CONTO_BANCARIO," +
					" CODICE_GESTIONALE_SIOPE, CODICE_GESTIONALE_AGGIUNTIVO, ANAGRAFICA_DELEGATO, CODICE_FISCALE_DELEGATO, ID_DELEGATO, ID_SEDE_DELEGATO," +
					" ID_CONTO_BANCARIO_DELEGATO, IBAN_DELEGATO, COMMISSIONI, BOLLO, CODICE_CUP, CODICE_CIG, STAMPA_AVVISO, OPEN_BALANCE_PROPOSED, DOC_TYPE, DOC_ID, DATO_SENSIBILE) " +
					" VALUES" +
					" ("+idTerzeParti+","+item.getImportoLordo()+", null, null, null, null,"+item.getImponibileIrpef()+","+item.getRitenuteIrpef()+","+item.getRitenutePrevidenzialiBen()+
					" , "+item.getAltreRitenute()+", "+item.getImponibilePrevidenziale()+", "+item.getRitenutePrevidenzialiEnte()+", "+item.getAddizionaleComunale()+
					" , "+item.getAddizionaleRegionale()+", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, " +
					" null, null, null, 1601, sysdate, null, null, "+item.getCodiceBeneficiario()+", "+item.getCodiceSede()+", "+item.getCodiceTipoPagamento()+
					" , "+( (item.getCodiceContoCorrente()==null || item.getCodiceContoCorrente().equalsIgnoreCase("0")) ?"null":"'"+item.getCodiceContoCorrente()+"'")+", "+(item.getCodiceSIOPE()==null?"null":"'"+item.getCodiceSIOPE()+"'")+", "+(item.getCodiceSIOPEAggiuntivo()==null?"null":"'"+item.getCodiceSIOPEAggiuntivo()+"'")+
					" , null, null, null, null, null, null, "+(esenzCommBon==null?"null":"'"+esenzCommBon+"'")+
					" , "+(bollo==null?"null":"'"+bollo+"'")+", "+(item.getCodiceCUP()==null?"null":"'"+item.getCodiceCUP()+"'")+", "+(item.getCodiceCIG()==null?"null":"'"+item.getCodiceCIG()+"'")+
					" , "+(stampaAvv==null?"null":"'"+stampaAvv+"'")+", null, '"+createDelDocumento.getTipoDocumento()+"', null, '"+datoSensibile+"')";

					stmt.executeUpdate(queryInt);
					stmt.close();
				}
			}			 
		}

		//Gestisco la lista delle reversali collegate se presente
		if (createDelDocumento.getReversaliColl() != null)
		{

			List reversaliCollTypeList = createDelDocumento.getReversaliColl().getReversaleColl();

			if (idTerzeParti == 0)
			{
				Statement stSeq = connection.createStatement();
				ResultSet rsSeq = stSeq.executeQuery("SELECT ID_SEQ.nextval progressivo FROM DUAL");

				if (rsSeq.next()) 
				{
					idTerzeParti = rsSeq.getInt("progressivo");
				}
				stSeq.close();
			}

			if (idTerzeParti != 0)
			{
				for ( Iterator iter = reversaliCollTypeList.iterator(); iter.hasNext(); )
				{			 
					it.latraccia.entity.sic.richiesta.ReversaleCollTypes item = (it.latraccia.entity.sic.richiesta.ReversaleCollTypes)iter.next();


					Statement stmt = connection.createStatement();

					String queryInt = "INSERT INTO FIN_T_REV_COLL_APP(ID, REV_CODICE, REV_IMPORTO)" +
					" VALUES" +
					" ("+idTerzeParti+", "+item.getNumeroReversale()+", "+item.getImportoReversale()+")";

					stmt.executeUpdate(queryInt);
					stmt.close();
				}
			}			 
		}

		cs.setInt(25, idTerzeParti);  //identificativo delle terze parti 

		if (createDelDocumento.getCollegatoAMandato() == null)
		{
			cs.setNull(26, Types.VARCHAR);  //Collegato a Mandato non si setta		
		}
		else
		{
			if (createDelDocumento.getCollegatoAMandato().equals("S"))
			{
				cs.setString(26, "Y");      //Collegato a Mandato (valido soltanto per le reversali)
			}
			else
			{
				cs.setString(26, createDelDocumento.getCollegatoAMandato());      //Collegato a Mandato (valido soltanto per le reversali)
			}
		}

		if(createDelDocumento.getPianoContiFina() == null)
		{
			cs.setNull(27, Types.VARCHAR);  //Piano dei Conti Finanziario non si setta		
		}
		else
		{
			cs.setString(27, createDelDocumento.getPianoContiFina());      //Piano dei Conti Finanziario
		}

		if(createDelDocumento.getPianoContiFinaPlur1() == null)
		{
			cs.setNull(28, Types.VARCHAR);  //Piano dei Conti Finanziario Pluriennale 1 non si setta		
		}
		else
		{
			cs.setString(28, createDelDocumento.getPianoContiFinaPlur1());     //Piano dei Conti Finanziario Pluriennale 1
		}

		if(createDelDocumento.getPianoContiFinaPlur2() == null)
		{
			cs.setNull(29, Types.VARCHAR);  //Piano dei Conti Finanziario Pluriennale 2 non si setta		
		}
		else
		{
			cs.setString(29, createDelDocumento.getPianoContiFinaPlur2());     //Piano dei Conti Finanziario Pluriennale 2
		}
		
		if(createDelDocumento.getMandRevSostituito() == 0)
		{
			cs.setNull(30, Types.NUMERIC);  //Mandato/reversale sostituitonon si setta		
		}
		else
		{
			cs.setInt(30, createDelDocumento.getMandRevSostituito());     //Mandato/reversale
		}
		
		if(createDelDocumento.getCodContoEvidenza() == null)
		{
			cs.setNull(31, Types.VARCHAR);  //Conto evidenza non si setta		
		}
		else
		{
			cs.setString(31, createDelDocumento.getCodContoEvidenza());     //Conto evidenza
		}
		
		if(createDelDocumento.getPreImpegnoAutomatico() == null)
		{
			cs.setNull(32, Types.VARCHAR);  //PRE-Impegno automatico non si setta		
		}
		else
		{
			cs.setString(32, createDelDocumento.getPreImpegnoAutomatico());     //PRE-Impegno automatico
		}
		
		if(createDelDocumento.getSistemazioneContabile() == null)
		{
			cs.setNull(33, Types.VARCHAR);  //SistemazioneContabile non si setta		
		}
		else
		{
			cs.setString(33, createDelDocumento.getSistemazioneContabile());     //SistemazioneContabile
		}
		
		if(createDelDocumento.getDataEsecuzionePagamento() == null)
		{
			cs.setNull(34, Types.DATE);  //data_esecuzione_pagamento non si setta		
		}
		else
		{
			cs.setDate(34, new java.sql.Date(createDelDocumento.getDataEsecuzionePagamento().getTime().getTime()));     //data_esecuzione_pagamento
		}
		
		if(createDelDocumento.getMotivazione() == null)
		{
			cs.setNull(35, Types.VARCHAR);  //Motivazione non si setta		
		}
		else
		{
			cs.setString(35, createDelDocumento.getMotivazione());     //Motivazione
		}
		
		if(createDelDocumento.getRiaccertamento() == null)
		{
			cs.setNull(36, Types.VARCHAR);  //riaccertamento non si setta		
		}
		else
		{
			cs.setString(36, createDelDocumento.getRiaccertamento());     //riaccertamento
		}
		
		if(createDelDocumento.getIdentificativoAtto() == null)
		{
			cs.setNull(37, Types.VARCHAR);  //Identificativo Atto non si setta		
		}
		else
		{
			cs.setString(37, createDelDocumento.getIdentificativoAtto());     //Identificativo Atto
		}
		
		if(createDelDocumento.getIdUnivocoChiamata() == null)
		{
			cs.setNull(38, Types.VARCHAR);  //ID Univoco Chiamata non si setta		
		}
		else
		{
			cs.setString(38, createDelDocumento.getIdUnivocoChiamata()); //ID Univoco Chiamata
		}
		
		if(createDelDocumento.getIdUnivocoSistemaChiamante() == null)
		{
			cs.setNull(39, Types.VARCHAR);  //ID Univoco Sistema Chiamante non si setta		
		}
		else
		{
			cs.setString(39, createDelDocumento.getIdUnivocoSistemaChiamante());     //ID Univoco Sistema Chiamante
		}
		
		if(createDelDocumento.getCampoDispN01() == 0.0d)
		{
			cs.setNull(40, Types.VARCHAR);  //CampoDispN01 non si setta		
		}
		else
		{
			cs.setDouble(40, createDelDocumento.getCampoDispN01());     //CampoDispN01
		}
		
		if(createDelDocumento.getCampoDispN02() == 0.0d)
		{
			cs.setNull(41, Types.VARCHAR);  //CampoDispN02 non si setta		
		}
		else
		{
			cs.setDouble(41, createDelDocumento.getCampoDispN02());     //CampoDispN02
		}

		if(createDelDocumento.getCampoDispV01() == null)
		{
			cs.setNull(42, Types.VARCHAR);  //CampoDispV01 non si setta		
		}
		else
		{
			cs.setString(42, createDelDocumento.getCampoDispV01());     //CampoDispV01
		}

		if(createDelDocumento.getCampoDispV02() == null)
		{
			cs.setNull(43, Types.VARCHAR);  //CampoDispV02 non si setta		
		}
		else
		{
			cs.setString(43, createDelDocumento.getCampoDispV02());     //CampoDispV02
		}

		if(createDelDocumento.getCampoDispD01() == null)
		{
			cs.setNull(44, Types.DATE);  //CampoDispD01 non si setta		
		}
		else
		{
			cs.setDate(44, new java.sql.Date(createDelDocumento.getCampoDispD01().getTime().getTime())); //CampoDispD01 
		}

		if(createDelDocumento.getCampoDispD02() == null)
		{
			cs.setNull(45, Types.DATE);  // CampoDispD02 non si setta		
		}
		else
		{
			cs.setDate(45, new java.sql.Date(createDelDocumento.getCampoDispD02().getTime().getTime()));     // CampoDispD02
		}
		
		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		ndr = new NumeroDocumentoResult();

		/** simonebrunox 16/12/2019: inizio mostra sempre codice e messaggio risposta */
		// if (cs.getInt(46)!=0) //SE codiceRisposta contiene la segnalazione di un errore {
			ndr.setCodiceRisposta(cs.getInt(46));
			ndr.setDescrizioneRisposta(cs.getString(50));
		// } else {
			//modificato da Angelo: faccio il controllo su NumeroDocumento restituito solo se TipoDocumento è diverso da PRE-IMP
			if (cs.getInt(47)==0 && (!createDelDocumento.getTipoDocumento().equalsIgnoreCase("PRE-IMP")))
			{
				ndr.setNumeroDocumento(createDelDocumento.getNumeroDocumentoPC());
			}
			else
			{
				ndr.setNumeroDocumento(String.valueOf(cs.getInt(47)));
				ndr.setNumeroDocumentoPlur1(String.valueOf(cs.getInt(48)));
				ndr.setNumeroDocumentoPlur2(String.valueOf(cs.getInt(49)));
			}
			ndr.setDocId(cs.getInt(51));
			ndr.setDocId1(cs.getInt(52));
			ndr.setDocId2(cs.getInt(53));
		//}
		/** simonebrunox 16/12/2019: fine mostra sempre codice e messaggio risposta */

		return ndr;
	}

	public DisponibilitaImpResult getDisponibilitaImp(String numeroImpegno)  throws SQLException {

		DisponibilitaImpResult dr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		//modifica Leo: aggiunto il COG alla query
		String query="select segment2 capitolo,description oggetto,document_amount importo_impegno,(NVL(open_balance_data(doc_id,TRUNC(sysdate,'dd')),0)- importo_riaccertamento(doc_id)) importo_residuo,decode(auditing_status_flag,'T','SI','NO') perente_S_N, nvl(codice_asp,'X')||nvl(codice_direttrice,'X')||nvl(codice_risultati,'X')||nvl(codice_og_parte1,'XX')||nvl(codice_og_parte2,'XX')||nvl(codice_og_parte3,'XX') COG from fin_t_documenti where doc_type like'IMP%' and nvl(deletion_status_flag,'N') = 'N' and nvl(proposal_status_flag,'X') = 'P' and doc_sequence_value = "+numeroImpegno;

		ResultSet rs = s.executeQuery(query);

		if (rs.next())
		{
			dr = new DisponibilitaImpResult();
			dr.setNumeroImpegno(numeroImpegno);
			dr.setCodiceCapitolo(rs.getString("capitolo"));
			dr.setOggetto(rs.getString("oggetto"));
			dr.setImportoImpegno(rs.getDouble("importo_impegno"));
			dr.setImportoResiduo(rs.getDouble("importo_residuo"));
			dr.setPerente(rs.getString("perente_S_N"));

			//modifica leo: aggiunto COG
			dr.setCOG(rs.getString("COG"));
			

		}

		s.close();

		return dr;

	}

	public List getLiquidazioniAperte(String annoFinanziario, String codiceCapitolo) throws SQLException {
		List r = new ArrayList();
		InterrogaLiquidazioniAperteResult ilar = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();
		String query = "select d.doc_sequence_value numero_liquidazione, d.DESCRIPTION oggetto_liquidazione, d.segment2 capitolo_liquidazione, nvl(d.document_amount,0) importo_liquidazione, nvl(d.open_balance_proposed,0) residuo_liquidazione from fin_t_documenti d where d.segment2 = '"+codiceCapitolo+"' and d.doc_type = 'LIQ' and d.segment8 = '"+annoFinanziario+"' and nvl(d.deletion_status_flag,'N') = 'N' and nvl(d.proposal_status_flag,'X')||nvl(d.auditing_status_flag,'X') = 'PI' and nvl(d.open_balance_proposed,0) > 0";

		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			ilar = new InterrogaLiquidazioniAperteResult();

			ilar.setCapitoloLiquidazione(rs.getString("capitolo_liquidazione"));
			ilar.setImportoLiquidazione(rs.getDouble("importo_liquidazione"));
			ilar.setNumeroLiquidazione(rs.getString("numero_liquidazione"));
			ilar.setOggettoLiquidazione(rs.getString("oggetto_liquidazione"));
			ilar.setResiduoLiquidazione(rs.getDouble("residuo_liquidazione"));

			r.add(ilar);
		}

		s.close();

		return r;
	}


	
	public List getImpegniPerenti(String annoFinanziario, String codiceCapitolo) throws SQLException {
		List r = new ArrayList();
		InterrogaImpegniPerentiResult iipr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "SELECT d.doc_sequence_value numero_impegno, " +
		"d.DESCRIPTION oggetto_impegno, " +
		"d.segment2 capitolo_originario, " +
		"b.IND_CAP capitolo_attuale, " +
		"NVL(d.document_amount,0) importo_originario, " +
		"NVL(disponibilita_perente(d.doc_id,b.IND_CAP,b.ind_anno),0) importo_residuo, " + 
		"d.attribute16 pcf " +
		"FROM  fin_t_documenti d,  bas_ind b " +
		"WHERE b.ind_anno  = '"+annoFinanziario+"' " +
		"AND b.IND_CAP = '"+codiceCapitolo+"' " +
		"AND b.DOC_ID         = d.DOC_ID " +
		"AND NVL(d.proposal_status_flag,'X')||NVL(d.auditing_status_flag,'X') = 'PT' " +
		"AND d.DATA_PERENZIONE IS NOT NULL " +
		"AND NVL(d.open_balance_proposed,0)  > 0 " +
		"ORDER BY  4 ";
		ResultSet rs = s.executeQuery(query);


		while (rs.next())
		{
			iipr = new InterrogaImpegniPerentiResult();

			iipr.setCapitoloAttuale(rs.getString("capitolo_attuale"));
			iipr.setCapitoloOriginario(rs.getString("capitolo_originario"));
			iipr.setImportoOriginario(rs.getDouble("importo_originario"));
			iipr.setImportoResiduo(rs.getDouble("importo_residuo"));
			iipr.setNumeroImpegno(rs.getString("numero_impegno"));
			iipr.setOggettoImpegno(rs.getString("oggetto_impegno"));
			
			iipr.setCodicePCF(rs.getString("pcf"));
			iipr.setDescrizionePCF("");
			r.add(iipr);
		}

		s.close();

		return r;
	}


	public List interrogaObiettiviGestionali(String anno, String codiceCapitolo, String ufficio) throws SQLException 
	{
		List r = new ArrayList();
		InterrogaObiettivoGestionaleResult iogr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		//		String query = "select c.fin_t_asp_codice asp, c.fin_t_direttrice_codice dir, c.fin_t_risultati_codice ris, c.fin_t_og_codice_parte1 og1, c.fin_t_og_codice_parte2 og2, c.fin_t_og_codice_parte3 og3, o.descrizione desc_obiettivo from fin_t_obiettivi_gest_capitoli c, fin_t_obiettivi_gestionali o where c.capitolo_anno = "+anno+" and c.capitolo_eu|| c.capitolo_codice = '"+codiceCapitolo+"' and c.fin_t_og_codice_parte1|| c.fin_t_og_codice_parte2 = '"+ufficio+"' and c.data_assestamento = to_date('01011900','ddmmyyyy') and c.fin_t_og_anno = o.anno and c.fin_t_asp_codice = o.fin_t_asp_codice and c.fin_t_direttrice_codice = o.fin_t_direttrice_codice and c.fin_t_risultati_codice = o.fin_t_risultati_codice and c.fin_t_og_codice_parte1 = o.codice_parte1 and c.fin_t_og_codice_parte2 = o.codice_parte2 and c.fin_t_og_codice_parte3 = o.codice_parte3 and c.data_assestamento = o.data_assestamento";
		//modifica Leo: restituisce il codice obiettivo gestionale concatenato, come negli altri casi
		String query = "SELECT c.fin_t_asp_codice   ||c.fin_t_direttrice_codice   ||c.fin_t_risultati_codice   ||c.fin_t_og_codice_parte1   ||c.fin_t_og_codice_parte2   ||c.fin_t_og_codice_parte3 COG,   o.descrizione desc_obiettivo FROM fin_t_obiettivi_gest_capitoli c,   fin_t_obiettivi_gestionali o WHERE c.capitolo_anno = "+anno+" AND c.capitolo_eu   || c.capitolo_codice = '"+codiceCapitolo+"' AND o.ufficio = '"+ufficio+"' AND c.data_assestamento       = to_date('01011900','ddmmyyyy') AND c.fin_t_og_anno           = o.anno AND c.fin_t_asp_codice        = o.fin_t_asp_codice AND c.fin_t_direttrice_codice = o.fin_t_direttrice_codice AND c.fin_t_risultati_codice  = o.fin_t_risultati_codice AND c.fin_t_og_codice_parte1  = o.codice_parte1 AND c.fin_t_og_codice_parte2  = o.codice_parte2 AND c.fin_t_og_codice_parte3  = o.codice_parte3 AND c.data_assestamento       = o.data_assestamento"; 
		ResultSet rs = s.executeQuery(query);


		while (rs.next())
		{
			iogr = new InterrogaObiettivoGestionaleResult();

			//			iogr.setASP(rs.getString("asp"));
			//			iogr.setDirettrice(rs.getString("dir"));
			//			iogr.setOG1(rs.getString("og1"));
			//			iogr.setOG2(rs.getString("og2"));
			//			iogr.setOG3(rs.getString("og3"));
			//			iogr.setRisultato(rs.getString("ris"));

			iogr.setCOG(rs.getString("COG"));
			iogr.setDescrizione(rs.getString("desc_obiettivo"));

			r.add(iogr);
		}

		s.close();

		return r;
	}

	public List interrogaTipologiaReddito() throws SQLException 
	{
		List r = new ArrayList();
		InterrogaTipologiaResult itr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "SELECT F.CODICE COD, F.DESCRIZIONE DESCR FROM FIN_T_TIPOLOGIE_REDDITO F ORDER BY CODICE";
		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			itr = new InterrogaTipologiaResult();

			itr.setCodice(rs.getString("COD"));
			itr.setDescrizione(rs.getString("DESCR"));
			r.add(itr);
		}
		s.close();

		return r;
	}

	public List interrogaCodiceSiope(String annoBilancio, String codiceCapitolo) throws SQLException
	{
		List r = new ArrayList();
		InterrogaCodiceSiopeResult icsr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "select s.codice_gestionale COD, s.descrizione DESCR from fin_t_siope s, fin_t_articoli a where a.anno = "+annoBilancio+" and a.eu||a.codice = '"+codiceCapitolo+"' and a.codice_bilancio_siope = s.codice_bilancio and a.eu = s.eu order by 1";
		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			icsr = new InterrogaCodiceSiopeResult();

			icsr.setCodice(rs.getString("COD"));
			icsr.setDescrizione(rs.getString("DESCR"));

			r.add(icsr);
		}

		s.close();

		return r;
	}

	public List interrogaCapitoloEntrate(String annoBilancio) throws SQLException
	{
		List r = new ArrayList();
		InterrogaCapitoloEntrateResult icer = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "select a.anno anno, a.eu||a.codice capitolo_entrata, a.descrizione descr from fin_t_articoli a where a.anno = "+annoBilancio+" and a.eu = 'E' and nvl(a.abilitato,'N') = 'S' and codice not like '0000%' order by 1,2";
		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			icer = new InterrogaCapitoloEntrateResult();

			icer.setAnnoBilancio(rs.getString("anno"));
			icer.setCodiceCapitolo(rs.getString("capitolo_entrata"));
			icer.setDescrizioneCapitolo(rs.getString("descr"));

			r.add(icer);
		}

		s.close();

		return r;
	}

	public List interrogaReversaliCollegabili(String annoBilancio) throws SQLException
	{
		List r = new ArrayList();
		InterrogaReversaliCollegabiliResult ircr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "select doc_sequence_value numero_reversale, nvl(document_amount,0) importo_reversale from fin_t_documenti where segment8 = "+annoBilancio+" and doc_type = 'REV' " +
		" and nvl(open_balance_proposed,0) > 0 and nvl(attribute1,'N') = 'Y' " +
		" minus" +
		" select rev_codice numero_reversale, rev_importo importo_reversale from fin_t_rev_coll where substr(rev_codice,1,4) = "+annoBilancio;

		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			ircr = new InterrogaReversaliCollegabiliResult();

			ircr.setNumero(rs.getString("numero_reversale"));
			ircr.setImporto(rs.getDouble("importo_reversale"));

			r.add(ircr);
		}

		s.close();

		return r;
	}

	public List interrogaBeneficiariLiq(String numeroLiquidazione) throws SQLException
	{
		List r = new ArrayList();
		InterrogaBeneficiariLiqResult iblr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "select t.id_ana idBeneficiario, t.id_sede idSede, nvl(t.amount,0) importoOriginario, nvl(t.open_balance_proposed,0) importoResiduo" +
		" from fin_t_documenti_terze_parti t, fin_t_documenti d " +
		" where d.doc_type = 'LIQ' and nvl(d.deletion_status_flag,'N') = 'N' and d.doc_sequence_value = "+numeroLiquidazione+" and t.doc_id = d.doc_id";

		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			iblr = new InterrogaBeneficiariLiqResult();

			iblr.setIdBeneficiario(rs.getString("idBeneficiario"));
			iblr.setIdSede(rs.getString("idSede"));
			iblr.setImportoOriginario(rs.getDouble("importoOriginario"));
			iblr.setImportoResiduo(rs.getDouble("importoResiduo"));

			r.add(iblr);
		}

		s.close();

		return r;
	}

	/* simonebrunox 16/06/2017: esposizione beneficiari dell'impegno */
	public List interrogaBeneficiariImp(String numeroImpegno) throws SQLException
	{
		List r = new ArrayList();
		InterrogaBeneficiariImpResult ibir = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		/*String query = "select a.doc_id numero_impegno, b.amount importoOriginario, b.remarks infoAggiuntive, b.attribute2 metodo_pagamento_alternativo, b.attribute3 imponibile_irpef, b.attribute4 ritenute_irpef, b.attribute5 ritenute_previd_benef, b.attribute6 altre_ritenute, b.attribute7 imponibile_previd, b.attribute8 ritenute_previd_ente, b.attribute9 addizionale_comunale, b.attribute10 addizionale_regionale, b.attribute12 iban, "
				+ "b.id_ana idBeneficiario, decode(c.denominazione,null,c.cognome||' '||c.nome,c.denominazione) descrizioneBeneficiario, b.id_sede idSede, b.id_pagamento metodoPagamento, b.id_conto_bancario contoBancario, b.anagrafica_delegato anagraficaDelegato, b.codice_fiscale_delegato codiceFiscaleDelegato, b.id_delegato idDelegato, b.id_sede_delegato idSedeDelegato, b.id_conto_bancario_delegato contoBancarioDelegato, b.iban_delegato ibanDelegato, b.commissioni, b.bollo, b.codice_cup codiceCup, b.codice_cig codiceCig, open_balance_imp_tp(a.doc_id,b.id_ana,b.id_sede) importoResiduo, b.dato_sensibile datoSensibile "
				+ "from fin_t_documenti a, fin_t_documenti_terze_parti b, fin_t_anagrafica c where doc_type like 'IMP%' and nvl(deletion_status_flag,'N') <>'Y' and doc_sequence_value="+numeroImpegno+" and b.doc_id = a.doc_id and c.id = b.id_ana";*/
		
		String query = "select a.doc_id numero_impegno, b.amount importoOriginario, b.remarks infoAggiuntive, b.attribute2 metodo_pagamento_alternativo, b.attribute3 imponibile_irpef, b.attribute4 ritenute_irpef, b.attribute5 ritenute_previd_benef, b.attribute6 altre_ritenute, b.attribute7 imponibile_previd, b.attribute8 ritenute_previd_ente, b.attribute9 addizionale_comunale, b.attribute10 addizionale_regionale, b.attribute12 ivaSplitPayment, "
				+ "b.id_ana idBeneficiario, decode(c.denominazione,null,c.cognome||' '||c.nome,c.denominazione) descrizioneBeneficiario, b.id_sede idSede, b.id_pagamento metodoPagamento, b.id_conto_bancario contoBancario, b.anagrafica_delegato anagraficaDelegato, b.codice_fiscale_delegato codiceFiscaleDelegato, b.id_delegato idDelegato, b.id_sede_delegato idSedeDelegato, b.id_conto_bancario_delegato contoBancarioDelegato, "
				+ "b.iban_delegato ibanDelegato, b.commissioni, b.bollo, b.codice_cup codiceCup, b.codice_cig codiceCig, open_balance_imp_tp(a.doc_id,b.id_ana,b.id_sede) importoResiduo, b.dato_sensibile datoSensibile, c.tipo_anagrafica tipoAnagrafica, c.partita_iva partitaIva, c.codice_fiscale codiceFiscale, d.nome_sede nomeSede, d.indirizzo indirizzo, d.cap_sede cap, e.descrizione comune, e.provincia provincia, f.descrizione nomeMetodoPagamento, g.iban iban "
				+ "from fin_t_documenti a, fin_t_documenti_terze_parti b, fin_t_anagrafica c, fin_t_ana_sedi d, fin_t_comuni e, fin_t_tipo_pagamenti f, fin_t_ana_conti_bancari g "
				+ "where doc_type like 'IMP%' and nvl(deletion_status_flag,'N') <>'Y' and doc_sequence_value="+numeroImpegno+" and b.doc_id(+) = a.doc_id and c.id(+) = b.id_ana and d.id_sede(+) = b.id_sede and d.id_ana(+) = b.id_ana and e.id(+) = d.id_comune_sede	and f.id(+) = b.id_pagamento and g.id(+) = b.id_conto_bancario and g.id_ana(+) = b.id_ana and g.id_sede(+) = b.id_sede";
		
		ResultSet rs = s.executeQuery(query);

		while (rs.next() && rs.getString("idBeneficiario")!=null)
		{
			ibir = new InterrogaBeneficiariImpResult();

			ibir.setIdBeneficiario(rs.getString("idBeneficiario"));
			ibir.setDescrizioneBeneficiario(rs.getString("descrizioneBeneficiario"));
			ibir.setIdSede(rs.getString("idSede"));
			ibir.setImportoOriginario(rs.getDouble("importoOriginario"));
			ibir.setImportoResiduo(rs.getDouble("importoResiduo"));
			
			InfoAggiuntiveBeneficiariImpResult iabir = new InfoAggiuntiveBeneficiariImpResult();
			iabir.setTipoAnagrafica(rs.getString("tipoAnagrafica"));
			iabir.setPartitaIva(rs.getString("partitaIva"));
			iabir.setCodiceFiscale(rs.getString("codiceFiscale"));
			iabir.setNomeSede(rs.getString("nomeSede"));
			iabir.setIndirizzoSede(rs.getString("indirizzo") + " " + rs.getString("cap") + " - " + rs.getString("comune") + " (" + rs.getString("provincia") + ")");
			iabir.setNomeMetodoPagamento(rs.getString("nomeMetodoPagamento"));
			
			iabir.setInfoAggiuntive(rs.getString("infoAggiuntive"));
			iabir.setMetodoPagamento(rs.getString("metodoPagamento"));
			iabir.setContoBancario(rs.getString("contoBancario"));
			iabir.setIban(rs.getString("iban"));
			iabir.setIdDelegato(rs.getString("idDelegato"));
			iabir.setAnagraficaDelegato(rs.getString("anagraficaDelegato"));
			iabir.setCodiceFiscaleDelegato(rs.getString("codiceFiscaleDelegato"));
			iabir.setIdSedeDelegato(rs.getString("idSedeDelegato"));
			iabir.setContoBancarioDelegato(rs.getString("contoBancarioDelegato"));
			iabir.setIbanDelegato(rs.getString("ibanDelegato"));
			iabir.setCommissioni(rs.getString("commissioni"));
			iabir.setBollo(rs.getString("bollo"));
			iabir.setCodiceCup(rs.getString("codiceCup"));
			iabir.setCodiceCig(rs.getString("codiceCig"));
			iabir.setDatoSensibile(rs.getString("datoSensibile"));
			ibir.setInfoAggiuntiveBeneficiarioType(iabir);
			
			r.add(ibir);
		}

		s.close();

		return r;
	}
	/* simonebrunox 16/06/2017: esposizione beneficiari dell'impegno */
	
	public it.latraccia.entity.sic.risposta.RispostaInterrogaMandatiBeneficiariAttoTypes interrogaMandatiBeneficiariAtto(String tipoAtto, String numeroAtto, Calendar dataAtto, String ufficio, String numeroLiquidazione) throws SQLException  {

		//risposta
		it.latraccia.entity.sic.risposta.RispostaInterrogaMandatiBeneficiariAttoTypes risp = new RispostaInterrogaMandatiBeneficiariAttoTypesImpl();

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();
		Date dataAttoD= dataAtto.getTime();
		String dataAttoString=StringFormat.dateToString(dataAttoD, "dd/MM/yyyy");
		//seleziona tutti i mandati che soddisfano i parametri di ricerca
		String queryMandati ="SELECT DISTINCT a.DOC_ID, b.NUMERO_MANDATO, b.DATA_EMISSIONE_MANDATO, b.VALIDO_ANNULLATO, b.NUMERO_STORNO, " +
		"b.DATA_STORNO, b.IMPORTO_LORDO_DOCUMENTO IMPORTO_TOTALE_MANDATO, b.OGGETTO_MANDATO, b.NUMERO_LIQUIDAZIONE, b.NUMERO_IMPEGNO," +
		"b.NUMERO_IMPEGNO_RIF_PERENTE, b.IMPORTO_IMPEGNO, b.TIPO_IMPEGNO, b.ESERCIZIO_MANDATO, b.ESERCIZIO_IMPEGNO, b.ESERCIZIO_IMPEGNO_RIF_PERENTE, " +
		"b.DIPARTIMENTO, b.TIPO_ATTO_LIQ, b.NUMERO_ATTO_LIQ, b.DATA_ATTO_LIQ, b.TIPO_ATTO_IMP, b.NUMERO_ATTO_IMP, b.DATA_ATTO_IMP," +
		"c.NUMERO_DISTINTA, c.IN_CARICO, c.DATA_IN_CARICO, c.DATA_QUIETANZA, c.NUMERO_QUIETANZA " +
		"FROM fin_t_documenti a, fin_mandati1 b, fin_distinte_mand_dettaglio c " +
		"WHERE c.anno_finanziario = a.segment8 " +
		"and   c.tipo_mandato <> 'A' " +
		"and   c.numero_mandato = a.doc_sequence_value " +
		"and   a.doc_id = b.doc_id " +
		"and   b.valido_annullato = 'V' " +
		"and a.doc_type = 'MAND' ";
		//se eseguo la ricerca per numero di liquidazione dell'atto
		if (!numeroLiquidazione.equalsIgnoreCase("0")){
			queryMandati=queryMandati +"and b.numero_liquidazione="+ numeroLiquidazione;
		}
		//se eseguo la ricerca in base alle info di un atto
		else{
			queryMandati=queryMandati + "and   a.attribute13 = '"+tipoAtto+ "' " +
			"and a.attribute14 = '"+ numeroAtto+"' " +
			"and a.data_atto_autorizzativo = to_date('" + dataAttoString +"','dd/MM/yyyy') " +
			"and   a.segment3 ='" + ufficio + "' " ;
		}
		ResultSet rsMandati = s.executeQuery(queryMandati);
		while (rsMandati.next()){



			//crea un nuovo oggetto Mandato
			it.latraccia.entity.sic.risposta.RispostaInterrogaMandatiBeneficiariAttoTypes.MandatoType mandato=new it.latraccia.entity.sic.risposta.impl.RispostaInterrogaMandatiBeneficiariAttoTypesImpl.MandatoTypeImpl();

			//riempi i campi del mandato

			mandato.setIdDocumento(rsMandati.getString("DOC_ID"));
			
			String numeroMandato=rsMandati.getString("NUMERO_MANDATO");

			mandato.setNumeroMandato(numeroMandato);

			java.util.Calendar dataEmissioneMandato = Calendar.getInstance();
			java.sql.Date dataEmissioneMandatoRec = rsMandati.getDate("DATA_EMISSIONE_MANDATO");
			if (dataEmissioneMandatoRec!=null){
				dataEmissioneMandato.setTime(dataEmissioneMandatoRec);
				mandato.setDataEmissioneMandato(dataEmissioneMandato);

			}

			mandato.setValidoAnnullato(rsMandati.getString("VALIDO_ANNULLATO"));

			mandato.setNumeroStorno(rsMandati.getLong("NUMERO_STORNO"));

			java.util.Calendar dataStorno = Calendar.getInstance();
			java.sql.Date dataStornoRec = rsMandati.getDate("DATA_STORNO");
			if (dataStornoRec!=null){
				dataStorno.setTime(dataStornoRec);
				mandato.setDataStorno(dataStorno);
			}


			//mandato.setImportoLordoDocumento(rsMandati.getDouble("IMPORTO_LORDO_DOCUMENTO"));

			mandato.setImportoTotaleMandato(rsMandati.getDouble("IMPORTO_TOTALE_MANDATO"));

			mandato.setOggettoMandato(rsMandati.getString("OGGETTO_MANDATO"));

			mandato.setNumeroLiquidazione(rsMandati.getLong("NUMERO_LIQUIDAZIONE"));

			mandato.setNumeroImpegno(rsMandati.getLong("NUMERO_LIQUIDAZIONE"));

			mandato.setNumeroImpegnoRifPerEnte(rsMandati.getLong("NUMERO_IMPEGNO_RIF_PERENTE"));

			mandato.setImportoImpegno(rsMandati.getDouble("IMPORTO_IMPEGNO"));

			mandato.setTipoImpegno(rsMandati.getString("TIPO_IMPEGNO"));

			mandato.setEsercizioMandato(rsMandati.getInt("ESERCIZIO_MANDATO"));

			mandato.setEsercizioImpegno(rsMandati.getInt("ESERCIZIO_IMPEGNO"));

			mandato.setEsercizioImpegnoRifPerEnte(rsMandati.getInt("ESERCIZIO_IMPEGNO_RIF_PERENTE"));

			mandato.setDipartimento(rsMandati.getString("DIPARTIMENTO"));

			mandato.setTipoAttoLiq(rsMandati.getString("TIPO_ATTO_LIQ"));

			mandato.setNumeroAttoLiq(rsMandati.getString("NUMERO_ATTO_LIQ"));

			//data stringa
			java.util.Calendar dataAttoLiq = Calendar.getInstance();
			String dataAttoLiqString=rsMandati.getString("DATA_ATTO_LIQ");
			if (dataAttoLiqString!=null){
				dataAttoLiqString=dataAttoLiqString.substring(0, 10);
				java.util.Date dataAttoLiqRec = null;
				try {
					dataAttoLiqRec = StringFormat.toDate(dataAttoLiqString, "yyyy/MM/dd");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				dataAttoLiq.setTime(dataAttoLiqRec);
				mandato.setDataAttoLiq(dataAttoLiq);
			}





			mandato.setTipoAttoImp(rsMandati.getString("TIPO_ATTO_IMP"));

			mandato.setNumeroAttoImp(rsMandati.getString("NUMERO_ATTO_IMP"));

			//data stringa
			java.util.Calendar dataAttoImp = Calendar.getInstance();
			String dataAttoImpString=rsMandati.getString("DATA_ATTO_IMP");
			if (dataAttoImpString!=null){
				dataAttoImpString=dataAttoImpString.substring(0, 10);
				java.util.Date dataAttoImpRec = null;
				try {
					dataAttoImpRec = StringFormat.toDate(dataAttoImpString, "yyyy/MM/dd");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				dataAttoImp.setTime(dataAttoImpRec);
				mandato.setDataAttoImp(dataAttoLiq);
			}


			mandato.setNumeroDistinta(rsMandati.getLong("NUMERO_DISTINTA"));

			mandato.setIncarico(rsMandati.getString("IN_CARICO"));

			java.util.Calendar dataIncarico = Calendar.getInstance();
			java.sql.Date dataIncaricoRec = rsMandati.getDate("DATA_IN_CARICO");
			if (dataIncaricoRec!=null){
				dataIncarico.setTime(dataIncaricoRec);
				mandato.setDataIncarico(dataIncarico);
			}


			java.util.Calendar dataQuietanza = Calendar.getInstance();
			Date dataQuitenzaRec=rsMandati.getDate("DATA_QUIETANZA");
			if (dataQuitenzaRec!=null){
				dataQuietanza.setTime(dataQuitenzaRec);
				mandato.setDataQuietanza(dataQuietanza);
			}


			mandato.setNumeroQuietanza(rsMandati.getLong("NUMERO_QUIETANZA"));

			//Se non c'è la data e il numero di storno carico i Beneficiari del Mandato
			if(dataStornoRec == null){

				String queryBeneficiari ="SELECT  b.NOME_FORNITORE, b.DATA_NASCITA, b.CODICE_FISCALE, b.PARTITA_IVA, " +
				"b.RAPPRESENTANTE_LEGALE, b.CODFISC_RAPPRESENTANTE_LEGALE, b.DATANASC_RAPPRESENTANTE_LEGALE, b.INDIRIZZO_FORNITORE, b.CITTA_FORNITORE," +
				"b.PROVINCIA_FORNITORE, b.CAP_FORNITORE, b.NAZIONE_FORNITORE, b.STATO_FORNITORE, b.IMPORTO_TOTALE_MANDATO IMPORTO_LORDO_BENEFICIARIO, b.IMPORTO_NETTO IMPORTO_NETTO_BENEFICIARIO," +
				"b.IMPONIBILE_IRPEF, b.RITENUTA_IRPEF, b.ADDIZIONALE_COMUNALE, b.ADDIZIONALE_REGIONALE, b.IMPONIBILE_PREVIDENZIALE, b.RITENUTE_PREVIDENZIALI_ENTE," +
				"b.RITENUTE_PREVIDENZIALI_BEN, b.ALTRE_RITENUTE, b.IMPONIBILE_IRAP, b.IMPOSTA_IRAP, b.METODO_PAGAMENTO, b.INFO_PAGAMENTO, " +
				"b.INFO_AGGIUNTIVE, b.CONTO_CORRENTE, b.IBAN, b.ABI, b.CAB, b.BANCA, b.INDIRIZZO_BANCA, b.CITTA_BANCA, b.NAZIONE_BANCA, b.CAP_BANCA," +
				"b.PROVINCIA_BANCA, b.DESC_PAGAMENTO_TESORIERE, b.CODICE_CUP,b.CODICE_CIG " +
				"FROM fin_t_documenti a, fin_mandati1 b, fin_distinte_mand_dettaglio c " +
				"WHERE c.anno_finanziario = a.segment8 " +
				"and   c.tipo_mandato <> 'A' " +
				"and   c.numero_mandato = a.doc_sequence_value " +
				"and   a.doc_id = b.doc_id " +
				"and   b.valido_annullato = 'V' " +
				"and a.doc_type = 'MAND' " +
				"and a.doc_sequence_value =" + numeroMandato ;

				//se eseguo la ricerca per numero di liquidazione dell'atto
				if (!numeroLiquidazione.equalsIgnoreCase("0")){
					queryBeneficiari=queryBeneficiari +"and b.numero_liquidazione="+ numeroLiquidazione;
				}
				
				//se eseguo la ricerca in base alle info di un atto
				else{
					queryBeneficiari=queryBeneficiari + "and   a.attribute13 = '"+tipoAtto+ "' " +
					"and a.attribute14 = '"+ numeroAtto+"' " +
					"and a.data_atto_autorizzativo = to_date('" + dataAttoString +"','dd/MM/yyyy') " +
					"and   a.segment3 ='" + ufficio + "' " ;
				}
				
				
				Statement s1 = connection.createStatement();
				ResultSet rsBeneficiari = s1.executeQuery(queryBeneficiari);

				while (rsBeneficiari.next()){

					//crea un nuovo oggetto Beneficiario
					it.latraccia.entity.sic.risposta.RispostaInterrogaMandatiBeneficiariAttoTypes.MandatoType.BeneficiarioType beneficiario=new it.latraccia.entity.sic.risposta.impl.RispostaInterrogaMandatiBeneficiariAttoTypesImpl.MandatoTypeImpl.BeneficiarioTypeImpl();

					//riempe i campi del beneficiario

					beneficiario.setNomeFornitore(rsBeneficiari.getString("NOME_FORNITORE"));

					java.util.Calendar dataNascitaFornitore = Calendar.getInstance();
					Date dataNascitaFornitoreRec=rsBeneficiari.getDate("DATA_NASCITA");
					if (dataNascitaFornitoreRec!=null){
						dataNascitaFornitore.setTime(rsBeneficiari.getDate("DATA_NASCITA"));
						beneficiario.setDataNascitaFornitore(dataNascitaFornitore);
					}


					beneficiario.setCodiceFiscaleFornitore(rsBeneficiari.getString("CODICE_FISCALE"));

					beneficiario.setPartitaIvaFornitore(rsBeneficiari.getString("PARTITA_IVA"));

					beneficiario.setRappresentanteLegale(rsBeneficiari.getString("RAPPRESENTANTE_LEGALE"));

					beneficiario.setCodiceFiscaleRappresentanteLegale(rsBeneficiari.getString("CODFISC_RAPPRESENTANTE_LEGALE"));

					beneficiario.setIndirizzoFornitore(rsBeneficiari.getString("INDIRIZZO_FORNITORE"));

					beneficiario.setCittaFornitore(rsBeneficiari.getString("CITTA_FORNITORE"));

					beneficiario.setProvinciaFornitore(rsBeneficiari.getString("PROVINCIA_FORNITORE"));

					beneficiario.setCapFornitore(rsBeneficiari.getString("CAP_FORNITORE"));

					beneficiario.setNazioneFornitore(rsBeneficiari.getString("NAZIONE_FORNITORE"));

					beneficiario.setStatoFornitore(rsBeneficiari.getString("STATO_FORNITORE"));

					//beneficiario.setImportoTotaleMandato(rsBeneficiari.getDouble("IMPORTO_TOTALE_MANDATO"));

					beneficiario.setImportoLordoBeneficiario(rsBeneficiari.getDouble("IMPORTO_LORDO_BENEFICIARIO"));

					//beneficiario.setImportoNetto(rsBeneficiari.getDouble("IMPORTO_NETTO"));

					beneficiario.setImportoNettoBeneficiario(rsBeneficiari.getDouble("IMPORTO_NETTO_BENEFICIARIO"));

					beneficiario.setImponibileIrpef(rsBeneficiari.getDouble("IMPONIBILE_IRPEF"));

					beneficiario.setRitenutaIrpef(rsBeneficiari.getDouble("RITENUTA_IRPEF"));

					beneficiario.setAddizionaleComunale(rsBeneficiari.getDouble("ADDIZIONALE_COMUNALE"));

					beneficiario.setAddizionaleRegionale(rsBeneficiari.getDouble("ADDIZIONALE_REGIONALE"));

					beneficiario.setImponibilePrevidenziale(rsBeneficiari.getDouble("IMPONIBILE_PREVIDENZIALE"));

					beneficiario.setRitenutePrevidenzialiEnte(rsBeneficiari.getDouble("RITENUTE_PREVIDENZIALI_ENTE"));

					beneficiario.setRitenutePrevidenzialiBeneficiario(rsBeneficiari.getDouble("RITENUTE_PREVIDENZIALI_BEN"));

					beneficiario.setAltreRitenute(rsBeneficiari.getDouble("ALTRE_RITENUTE"));

					beneficiario.setImponibileIrap(rsBeneficiari.getDouble("IMPONIBILE_IRAP"));

					beneficiario.setImpostaIrap(rsBeneficiari.getDouble("IMPOSTA_IRAP"));

					beneficiario.setMetodoPagamento(rsBeneficiari.getString("METODO_PAGAMENTO"));

					beneficiario.setInfoPagamento(rsBeneficiari.getString("INFO_PAGAMENTO"));

					beneficiario.setInfoAggiuntive(rsBeneficiari.getString("INFO_AGGIUNTIVE"));

					beneficiario.setContoCorrente(rsBeneficiari.getString("CONTO_CORRENTE"));

					beneficiario.setIBAN(rsBeneficiari.getString("IBAN"));

					beneficiario.setABI(rsBeneficiari.getString("ABI"));

					beneficiario.setCAB(rsBeneficiari.getString("CAB"));

					beneficiario.setBanca(rsBeneficiari.getString("BANCA"));

					beneficiario.setIndirizzoBanca(rsBeneficiari.getString("INDIRIZZO_BANCA"));

					beneficiario.setCittaBanca(rsBeneficiari.getString("CITTA_BANCA"));

					beneficiario.setNazioneBanca(rsBeneficiari.getString("NAZIONE_BANCA"));

					beneficiario.setCapBanca(rsBeneficiari.getString("CAP_BANCA"));

					beneficiario.setProvinciaBanca(rsBeneficiari.getString("PROVINCIA_BANCA"));

					beneficiario.setDescrizionePagamentoTesoriere(rsBeneficiari.getString("DESC_PAGAMENTO_TESORIERE"));

					beneficiario.setCodiceCup(rsBeneficiari.getString("CODICE_CUP"));

					beneficiario.setCodiceCig(rsBeneficiari.getString("CODICE_CIG"));	

					//aggiungi il beneficiario al mandato

					mandato.getBeneficiario().add(beneficiario);

				}
				s1.close();

			}
			

			//aggiungi il mandato alla risposta
			risp.getMandato().add(mandato);


		}

		s.close();
		
		return risp;

	}

	public List interrogazionePCF(String anno, String codiceCapitolo) throws SQLException {
		List r = new ArrayList();
		InterrogazionePCFResult ipcfr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();
		//query piani contabili finanziari
		/*String query = "select a.codice_finale COD,a.descrizione DESCR " +
				       "from nb_pcf_05 a " +
				       "where a.anno ="+anno+" " +
				       "and substr(a.codice_finale,1,13) in (select substr(b.nb_piano_conti_fina,1,13) from fin_t_articoli b where b.anno =" +anno + " and b.eu||b.codice='"+codiceCapitolo+"') order by a.codice_finale";
		 */
		String query= "select a.codice_finale COD, a.descrizione DESCR	" +
		"from nb_pcf_05 a	" +
		"where a.anno ="+anno+" " +
		"and substr(a.codice_finale,1,13) in (select substr(b.nb_piano_conti_fina,1,13) from fin_t_articoli b where b.anno =" +anno + " and b.eu||b.codice='"+codiceCapitolo+"') " +
		"order by 1";

		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			ipcfr = new InterrogazionePCFResult();

			ipcfr.setCodice(rs.getString("COD"));
			ipcfr.setDescrizione(rs.getString("DESCR"));

			r.add(ipcfr);
		}

		s.close();

		return r;
	}

	public it.latraccia.entity.sic.risposta.RispostaInterrogazioneMandatiTrasparenzaTypes interrogaMandatiTrasparenza(String anno) throws SQLException {
		//risposta
		it.latraccia.entity.sic.risposta.RispostaInterrogazioneMandatiTrasparenzaTypes risp = new RispostaInterrogazioneMandatiTrasparenzaTypesImpl();

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement sm=null;
		sm = connection.createStatement();

		//seleziona tutti i mandati che soddisfano i parametri di ricerca		
		/*String queryMandati ="SELECT DISTINCT numero_mandato, " +
		"data_emissione_mandato, " +
		"tipo_atto_liq , " +
		"lpad(numero_atto_liq,5,'0') numeroAtto, " +
		"substr(data_atto_liq,9,2)||'/'||substr(data_atto_liq,6,2)||'/'||substr(data_atto_liq,1,4) dataAtto, " +
		"dipartimento , " +
		"dipartimento||'.'||substr(data_atto_liq,1,4)||'/'||decode(tipo_atto_liq,'DETERMINA','D','DISPOSIZIONE','L','X')||'.'||lpad(numero_atto_liq,5,'0') idProvvedimento, NUMERO_LIQUIDAZIONE idLiquidazione " +
		"FROM fin_mandati1 " +
		"WHERE valido_annullato = 'V' " +
		"and esercizio_mandato = nvl("+anno+",esercizio_mandato) " +
		"and id_fornitore not in (23160,28679) " +
		"and numero_storno is null " +
		"ORDER BY numero_mandato desc ";*/

		String queryMandati ="SELECT DISTINCT a.numero_mandato, "
					+ "a.data_emissione_mandato, "
					+ "a.tipo_atto_liq , "
					+ "lpad(a.numero_atto_liq,5,'0') numeroAtto, "
					+ "substr(a.data_atto_liq,9,2)||'/'||substr(a.data_atto_liq,6,2)||'/'||substr(a.data_atto_liq,1,4) dataAtto, "
					+ "a.dipartimento, "
					+ "nvl(a.id_provvedimento,a.dipartimento||'.'||substr(a.data_atto_liq,1,4)||'/'||decode(a.tipo_atto_liq,'DETERMINA','D','DISPOSIZIONE','L','X')||'.'||lpad(a.numero_atto_liq,5,'0')) idProvvedimento, "
					+ "a.NUMERO_LIQUIDAZIONE idLiquidazione "
				+ "FROM fin_mandati1 a,"
				+ "fin_distinte_mand_dettaglio b "
				+ "WHERE a.valido_annullato = 'V' "
					+ "and a.esercizio_mandato = nvl("+anno+",a.esercizio_mandato) "
						+ "and a.id_fornitore not in (23160,28679) "
						+ "and a.numero_storno is null "
						+ "and b.anno_finanziario = a.esercizio_mandato "
						+ "and b.numero_mandato = a.numero_mandato ";
		
		if (cliente.equalsIgnoreCase("giunta"))
			queryMandati += "and b.data_quietanza is not null ";
		
		queryMandati += "ORDER BY a.numero_mandato desc";		

		ResultSet rsMandati = sm.executeQuery(queryMandati);
		while (rsMandati.next()){



			//crea un nuovo oggetto Mandato
			it.latraccia.entity.sic.risposta.RispostaInterrogazioneMandatiTrasparenzaTypes.MandatoType mandato=new RispostaInterrogazioneMandatiTrasparenzaTypesImpl.MandatoTypeImpl();

			//riempi i campi del mandato


			mandato.setNumeroMandato(rsMandati.getString("NUMERO_MANDATO"));

			java.util.Calendar dataEmissioneMandato = Calendar.getInstance();
			java.sql.Date dataEmissioneMandatoRec = rsMandati.getDate("DATA_EMISSIONE_MANDATO");
			if (dataEmissioneMandatoRec!=null){
				dataEmissioneMandato.setTime(dataEmissioneMandatoRec);
				mandato.setDataMandato(dataEmissioneMandato);

			}

			mandato.setTipoAtto(rsMandati.getString("TIPO_ATTO_LIQ"));

			mandato.setNumeroAtto(rsMandati.getString("numeroAtto"));

			//data stringa
			java.util.Calendar dataAttoLiq = Calendar.getInstance();
			String dataAttoLiqString=rsMandati.getString("dataAtto");
			if (dataAttoLiqString!=null){
				dataAttoLiqString=dataAttoLiqString.substring(0, 10);
				java.util.Date dataAttoLiqRec = null;
				try {
					dataAttoLiqRec = StringFormat.toDate(dataAttoLiqString, "dd/MM/yyyy");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				dataAttoLiq.setTime(dataAttoLiqRec);
				mandato.setDataAtto(dataAttoLiq);
			}




			mandato.setUfficio(rsMandati.getString("DIPARTIMENTO"));

			mandato.setIdProvvedimento(rsMandati.getString("idProvvedimento"));
			
			mandato.setIdLiquidazione(rsMandati.getString("idLiquidazione"));


			//Cerca i Beneficiari del Mandato

			Statement sb=null;
			sb = connection.createStatement();

			String queryBeneficiari ="SELECT importo_totale_mandato as importo_lordo , " +
			"nvl(importo_totale_mandato,0)- nvl(importo_netto,0) as importo_ritenute , " +
			"importo_netto as importo_netto, " +
			"decode(nvl(dato_sensibile,'N'),'S',TO_CHAR(id_fornitore) ,nome_fornitore) NOME_FORNITORE, " +
			"decode(nvl(dato_sensibile,'N'),'S','D.S.',decode(partita_iva,null,codice_fiscale,partita_iva)) CFPI, " +
			"decode(nvl(dato_sensibile,'N'),'S',TO_CHAR(codice_sede_fornitore),indirizzo_fornitore||' - '||citta_fornitore||' - '||cap_fornitore) indirizzoFornitore, " +
			"id_fornitore, codice_sede_fornitore " +
			"FROM fin_mandati1 " +
			"WHERE valido_annullato = 'V' " +
			"and esercizio_mandato = nvl("+anno+",esercizio_mandato) " +
			"and id_fornitore not in (23160,28679) " +
			"and numero_storno is null " +
			"and numero_mandato="+ mandato.getNumeroMandato() + " " +
			"ORDER BY nome_fornitore ";


			ResultSet rsBeneficiari = sb.executeQuery(queryBeneficiari);

			while (rsBeneficiari.next()){

				//crea un nuovo oggetto Beneficiario
				it.latraccia.entity.sic.risposta.RispostaInterrogazioneMandatiTrasparenzaTypes.MandatoType.BeneficiarioType beneficiario=new it.latraccia.entity.sic.risposta.impl.RispostaInterrogazioneMandatiTrasparenzaTypesImpl.MandatoTypeImpl.BeneficiarioTypeImpl();

				//riempe i campi del beneficiario

				beneficiario.setNomeFornitore(rsBeneficiari.getString("NOME_FORNITORE"));

				beneficiario.setCodiceFiscalePartitaIvaFornitore(rsBeneficiari.getString("CFPI"));

				beneficiario.setIndirizzoFornitore(rsBeneficiari.getString("indirizzoFornitore"));

				/*beneficiario.setImportoLordoBeneficiario(rsBeneficiari.getDouble("IMPORTO_LORDO"));

				beneficiario.setImportoRitenuteBeneficiario(rsBeneficiari.getDouble("IMPORTO_RITENUTE"));

				beneficiario.setImportoNettoBeneficiario(rsBeneficiari.getDouble("IMPORTO_NETTO"));*/

				beneficiario.setImportoLordoBeneficiario(rsBeneficiari.getDouble("IMPORTO_LORDO"));
				
				beneficiario.setIdFornitore(rsBeneficiari.getString("id_fornitore"));
				
				beneficiario.setCodiceSedeFornitore(rsBeneficiari.getString("codice_sede_fornitore"));


				//aggiungi il beneficiario al mandato

				mandato.getBeneficiario().add(beneficiario);

			}
			sb.close();


			//aggiungi il mandato alla risposta
			risp.getMandato().add(mandato);


		}

		sm.close();
		return risp;

	}

	public RispostaInterrogazioneContrattiTypes interrogaContratti(String numeroContratto, String oggettoContratto) throws SQLException {
		//risposta
		it.latraccia.entity.sic.risposta.RispostaInterrogazioneContrattiTypes risp = new RispostaInterrogazioneContrattiTypesImpl();

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();
		String queryContratti ="select lpad(g.tab_codice,3,0)||lpad(g.progr,4,0) chiave, " +
		"g.num_contratto numero_repertorio, " +
		"g.ult_descr descrizione, " +
		"g.cig cig, " +
		"g.codice_cup cup " +
		"from bi_generale_gest g,bi_tabelle_gest t " +
		"where t.codice=g.tab_codice and " +
		"nvl(t.att_pass,'A')='P' and " +
		"g.ult_descr is not null and " +
		"g.chiuso is null ";

		if (numeroContratto!=null){
			queryContratti=queryContratti + "and g.num_contratto = '"+numeroContratto+"' ";
		}

		if (oggettoContratto!=null){
			queryContratti=queryContratti + "and g.ult_descr LIKE '%"+oggettoContratto+"%' ";
		}

		ResultSet rsContratti = s.executeQuery(queryContratti);
		while (rsContratti.next()){
			//crea un nuovo oggetto Contratto
			it.latraccia.entity.sic.risposta.RispostaInterrogazioneContrattiTypes.ContrattoType contratto=new RispostaInterrogazioneContrattiTypesImpl.ContrattoTypeImpl();

			//riempi i campi del contatto
			contratto.setChiave(rsContratti.getString("chiave"));
			contratto.setNumeroRepertorio(rsContratti.getString("numero_repertorio"));
			contratto.setDescrizione(rsContratti.getString("descrizione"));
			contratto.setCodiceCig(rsContratti.getString("cig"));
			contratto.setCodiceCup(rsContratti.getString("cup"));		

			//aggiungi il contratto alla risposta
			risp.getContratto().add(contratto);

		}

		s.close();
		return risp;




	}


	public it.latraccia.entity.sic.risposta.RispostaInterrogazioneContrattiTypes interrogaListaContratti(it.latraccia.entity.sic.richiesta.InterrogazioneListaContrattiTypes listaChiavi ) throws SQLException {

		//risposta
		it.latraccia.entity.sic.risposta.RispostaInterrogazioneContrattiTypes risp = new RispostaInterrogazioneContrattiTypesImpl();

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		List chiavi=listaChiavi.getChiave();
		Iterator itChiavi=chiavi.iterator();
		Statement s=null;
		while (itChiavi.hasNext()){
			String chiave=(String)itChiavi.next();
			s = connection.createStatement();
			String queryContratti ="select lpad(g.tab_codice,3,0)||lpad(g.progr,4,0) chiave, " +
			"g.num_contratto numero_repertorio, " +
			"g.ult_descr descrizione, " +
			"g.cig cig, " +
			"g.codice_cup cup " +			
			"from bi_generale_gest g,bi_tabelle_gest t " +
			"where t.codice=g.tab_codice and " +
			"nvl(t.att_pass,'A')='P' and " +
			"g.ult_descr is not null and " +
			"g.chiuso is null " + 
			"and lpad(g.tab_codice,3,0)||lpad(g.progr,4,0) = '"+chiave+"' ";;


			ResultSet rsContratti = s.executeQuery(queryContratti);
			while (rsContratti.next()){

				//crea un nuovo oggetto Contratto
				it.latraccia.entity.sic.risposta.RispostaInterrogazioneContrattiTypes.ContrattoType contratto=new RispostaInterrogazioneContrattiTypesImpl.ContrattoTypeImpl();

				//riempi i campi del contatto
				contratto.setChiave(rsContratti.getString("chiave"));
				contratto.setNumeroRepertorio(rsContratti.getString("numero_repertorio"));
				contratto.setDescrizione(rsContratti.getString("descrizione"));
				contratto.setCodiceCig(rsContratti.getString("cig"));
				contratto.setCodiceCup(rsContratti.getString("cup"));						

				//aggiungi il contratto alla risposta
				risp.getContratto().add(contratto);				

			}

		}
		s.close();

		return risp;

	}

	public String getCliente() {
		return cliente;
	}

	public void setCliente(String cliente) {
		this.cliente = cliente;
	}

	public String getMessaggioBlocco() {
		return messaggioBlocco;
	}

	public void setMessaggioBlocco(String messaggioBlocco) {
		this.messaggioBlocco = messaggioBlocco;
	}
}
