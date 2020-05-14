/**
 * Creato il 22 Giugno 2011
 * Modificato il 06 Ottobre 2011
 */
package it.latraccia.utility;


import it.latraccia.entity.anasic.richiesta.AnagraficaTypes;
import it.latraccia.entity.anasic.richiesta.CreaContoBancarioTypes;
import it.latraccia.entity.anasic.richiesta.CreaSedeTypes;
import it.latraccia.entity.anasic.richiesta.InterrogaAnagraficaContrattiTypes;
import it.latraccia.entity.anasic.richiesta.ModificaAnagraficaTypes;
import it.latraccia.entity.anasic.richiesta.ModificaContoBancarioTypes;
import it.latraccia.entity.anasic.richiesta.ModificaSedeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
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
	private Connection connection = null;

	public DB() 
	{
		try 
		{
			// LETTURA XML
			InputStream in = getClass().getResourceAsStream("dati_db_cluster.xml");
			//InputStream in = getClass().getResourceAsStream("dati_db_cluster_POTENZA.xml");
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

	public List getAnagraficaContratti(InterrogaAnagraficaContrattiTypes listaChiavi) throws SQLException, JAXBException {
		List r=new ArrayList();
		//it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();    
		List chiavi=listaChiavi.getChiave();
		Iterator itChiavi=chiavi.iterator();

		while (itChiavi.hasNext()){
			AnagraficaContratto anagraficaContratto= new AnagraficaContratto();
			String chiave=(String)itChiavi.next();
			anagraficaContratto.setChiave(chiave);
			anagraficaContratto.setListaAnagrafica((List<Anagrafica>)this.getAnagrafica(chiave, 5));
			r.add(anagraficaContratto);
		}
		return r;
	}

	public List getAnagrafica(String identificativo, int tipologia) throws SQLException, JAXBException
	{
		List r = new ArrayList();
		it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();    	
		String codFisc = "";
		String denom = "";
		String pIva = "";
		String idAna = "";
		String numContr="";
		Anagrafica iar = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		if (tipologia == 1) 
		{
			codFisc = identificativo;
		}
		else if (tipologia == 2)
		{
			denom = identificativo;
		}
		else if (tipologia == 3)
		{
			pIva = identificativo;
		}
		else if (tipologia == 4)
		{
			idAna = identificativo;
		}


		Statement s=null;
		s = connection.createStatement();



		String query ="";


		/* QUERY SE PASSO IL NUMERO DI CONTRATTO*/
		if (tipologia==5){
			numContr=identificativo;
			query="select distinct A.ID idAnagrafica, A.TIPO_ANAGRAFICA tipoAnagrafica, A.PARTITA_IVA partitaIva, A.DENOMINAZIONE denominazione,A.COGNOME_LR cognomeLR,	" +
			"A.NOME_LR nomeLR,A.SESSO_LR sessoLR,A.COMUNE_NS_LR comuneNSLR,A.DATA_NS_LR dataNSLR,A.INDIRIZZO_LR indirizzoLR,A.COMUNE_RES_LR comuneRESLR, " +
			"A.CAP_RES_LR capRESLR,A.CF_LR codiceFiscaleLR,A.CODICE_FISCALE codiceFiscale,A.COGNOME cognome,A.NOME nome,A.ALTRI_NOMI altriNomi,A.SESSO sesso, " +
			"A.DATA_NS dataNS,A.COMUNE_NS comuneNS,A.PIGNORAMENTO pignoramento,A.NOTA_PIGNORAMENTO notaPignoramento,A.ESTERO estero,A.COMMISSIONI commissioni	" +
			"from bi_fornitori_contratti fc,vw_anagrafica_sic a " +
			"where lpad(fc.tab_codice,3,0)||lpad(fc.gen_progr,4,0)='"+numContr+"' and a.id=fc.fornitore";
		}
		else{
			query="select distinct A.ID idAnagrafica, A.TIPO_ANAGRAFICA tipoAnagrafica, A.PARTITA_IVA partitaIva, A.DENOMINAZIONE denominazione,A.COGNOME_LR cognomeLR " +
			",A.NOME_LR nomeLR,A.SESSO_LR sessoLR,A.COMUNE_NS_LR comuneNSLR,A.DATA_NS_LR dataNSLR,A.INDIRIZZO_LR indirizzoLR,A.COMUNE_RES_LR comuneRESLR, " +
			"A.CAP_RES_LR capRESLR,A.CF_LR codiceFiscaleLR,A.CODICE_FISCALE codiceFiscale,A.COGNOME cognome,A.NOME nome,A.ALTRI_NOMI altriNomi,A.SESSO sesso, " +
			"A.DATA_NS dataNS,A.COMUNE_NS comuneNS,A.PIGNORAMENTO pignoramento,A.NOTA_PIGNORAMENTO notaPignoramento,A.ESTERO estero,A.COMMISSIONI commissioni " +
			" from vw_anagrafica_sic a where (upper(replace(nvl(a.denominazione,replace(a.cognome||a.nome||a.altri_nomi,' ')),' ')) like " +
			" '%'||upper(replace(nvl('"+denom+"','$$'),' '))||'%'  or a.partita_iva = '"+pIva+"'  or upper(a.codice_fiscale) = upper('"+codFisc+"') or a.id = '"+idAna+"')";
		}

		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			iar = new Anagrafica();
			iar.setAltriNomi(rs.getString("altriNomi"));
			iar.setCapResLR(rs.getString("capRESLR"));
			iar.setCodiceFiscale(rs.getString("codiceFiscale"));
			iar.setCodiceFiscaleLR(rs.getString("codiceFiscaleLR"));
			iar.setCognome(rs.getString("cognome"));
			iar.setCognomeLR(rs.getString("cognomeLR"));
			iar.setComuneNS(rs.getString("comuneNS"));
			iar.setComuneNSLR(rs.getString("comuneNSLR"));
			iar.setComuneRESLR(rs.getString("comuneRESLR"));

			if (rs.getString("commissioni") != null)
			{
				iar.setCommissioni(rs.getString("commissioni"));
			}
			else
			{
				iar.setCommissioni(null);
			}

			if (rs.getDate("dataNS") != null)
			{
				java.sql.Date dataS = rs.getDate("dataNS");
				java.util.Date dataU = new Date(dataS.getTime());
				iar.setDataNascita(dataU);
			}
			else
			{
				iar.setDataNascita(null);
			}

			if (rs.getDate("dataNSLR") != null)
			{
				java.sql.Date dataS1 = rs.getDate("dataNSLR");
				java.util.Date dataU1 = new Date(dataS1.getTime());
				iar.setDataNascitaLR(dataU1);
			}
			else
			{
				iar.setDataNascitaLR(null);
			}

			iar.setDenominazione(rs.getString("denominazione"));

			if (rs.getString("estero") != null)
			{
				iar.setEstero(rs.getString("estero"));
			}
			else
			{
				iar.setEstero(null);
			}

			iar.setIdAnagrafica(rs.getString("idAnagrafica"));
			iar.setIdComuneNS(null);
			iar.setIdComuneNSLR(null);
			iar.setIdComuneRESLR(null);
			iar.setIndirizzoLR(rs.getString("indirizzoLR"));
			iar.setNome(rs.getString("nome"));
			iar.setNomeLR(rs.getString("nomeLR"));
			iar.setNotaPignoramento(rs.getString("notaPignoramento"));
			iar.setPartitaIva(rs.getString("partitaIva"));
			iar.setPignoramento(rs.getString("pignoramento"));
			iar.setSesso(rs.getString("sesso"));
			iar.setSessoLR(rs.getString("sessoLR"));
			iar.setTipoAnagrafica(rs.getString("tipoAnagrafica"));


			it.latraccia.entity.anasic.risposta.ListaSediTypes lSedi = objFactory1.createListaSediTypes();
			Statement s1=null;
			s1 = connection.createStatement();

			String query1 = "select distinct A.ID_SEDE idSede, A.INDIRIZZO indirizzo, A.COMUNE_SEDE comuneSede, A.CAP_SEDE capSede, A.TELEFONO telefono," +
			" A.E_MAIL eMail, A.FAX fax, A.BOLLO bollo, A.DATA_CREAZIONE dataCreazione, A.NOME_SEDE nomeSede, A.ID_TIPO_PAGAMENTO idTipoPagamento," +
			" A.descrizione_pagamento descTipoPagamento from vw_anagrafica_sic a where A.ID = "+rs.getString("idAnagrafica")+" order by A.ID_SEDE";

			ResultSet rs1 = s1.executeQuery(query1);
			while (rs1.next())
			{
				if (rs1.getString("idSede") != null)
				{
					it.latraccia.entity.anasic.risposta.SedeTypes ss = objFactory1.createSedeTypes();

					ss.setBollo(rs1.getString("bollo"));

					ss.setCAP(rs1.getString("capSede"));
					ss.setComune(rs1.getString("comuneSede"));

					if (rs1.getDate("dataCreazione") != null)
					{
						Calendar cal = Calendar.getInstance();
						java.sql.Date dataS2 = rs1.getDate("dataCreazione");
						java.util.Date dataU2 = new Date(dataS2.getTime());
						cal.setTime(dataU2);
						ss.setDataCreazione(cal);
					}
					else
					{
						ss.setDataCreazione(null);
					}

					ss.setDescTipoPagamento(rs1.getString("descTipoPagamento"));
					ss.setEMail(rs1.getString("eMail"));
					ss.setFax(rs1.getString("fax"));
					ss.setIdComune(null);
					ss.setIdSede(rs1.getString("idSede"));
					ss.setIdTipoPagamento(rs1.getString("idTipoPagamento"));
					ss.setIndirizzo(rs1.getString("indirizzo"));
					ss.setNomeSede(rs1.getString("nomeSede"));
					ss.setTelefono(rs1.getString("telefono"));


					it.latraccia.entity.anasic.risposta.ListaContiBancariTypes lContiBancari = objFactory1.createListaContiBancariTypes();
					Statement s2=null;
					s2 = connection.createStatement();

					String query2 = "select distinct A.id_conto_corrente idContoCorrente, A.NOME_BANCA nomeBanca, A.ID_AGENZIA idAgenzia, " +
					" A.CONTO_CORRENTE contoCorrente, A.MODALITA_PRINCIPALE_SN CONTO_PRINCIPALE_SN, A.sede_agenzia sedeAgenzia," +
					" A.indirizzo_agenzia indirizzoAgenzia, A.CITTA citta, A.ABI abi, A.CAB cab, A.CIN cin, A.IBAN iban " +
					" from vw_anagrafica_sic a where A.ID = "+rs.getString("idAnagrafica")+" AND A.ID_SEDE ="+rs1.getString("idSede")+" order by A.id_conto_corrente";

					ResultSet rs2 = s2.executeQuery(query2);

					while (rs2.next())
					{
						if (rs2.getString("idContoCorrente") != null)
						{
							it.latraccia.entity.anasic.risposta.DatiBancariTypes bb = objFactory1.createDatiBancariTypes();
							bb.setABI(rs2.getString("abi"));
							bb.setCAB(rs2.getString("cab"));
							bb.setCIN(rs2.getString("cin"));
							bb.setCitta(rs2.getString("citta"));
							bb.setContoCorrente(rs2.getString("contoCorrente"));
							bb.setIBAN(rs2.getString("iban"));
							bb.setIdAgenzia(rs2.getString("idAgenzia"));
							bb.setIdContoCorrente(rs2.getString("idContoCorrente"));
							bb.setIndirizzo(rs2.getString("indirizzoAgenzia"));
							bb.setModalitaPrincipale(rs2.getString("CONTO_PRINCIPALE_SN"));

							bb.setNomeBanca(rs2.getString("nomeBanca"));
							bb.setSedeAgenzia(rs2.getString("sedeAgenzia"));

							lContiBancari.getContoBancario().add(bb);
						}
					}
					ss.setListaDatiBancari(lContiBancari);

					lSedi.getSede().add(ss);
					s2.close();
				}
			}
			iar.setListaSedi(lSedi);

			s1.close();


			r.add(iar);
		}
		s.close();

		return r;
	}

	/**
	 * @param numeroPreimpegno
	 * @return
	 * @throws JAXBException 
	 */
	public Anagrafica verificaAnagrafe(AnagraficaTypes at) throws SQLException, JAXBException
	{
		Anagrafica iar = null;
		it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();    	
		it.latraccia.entity.anasic.risposta.ListaSediTypes lSedi = objFactory1.createListaSediTypes();
		it.latraccia.entity.anasic.risposta.ListaContiBancariTypes lContiBancari = objFactory1.createListaContiBancariTypes();
		String dNascLR = null;
		String dNasc = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();	

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


		if (at.getDataNascitaLR() != null)
		{
			dNascLR = formatter.format(at.getDataNascitaLR().getTime());
		}

		if (at.getDataNascita() != null)
		{
			dNasc = formatter.format(at.getDataNascita().getTime());
		}

		String query="select ID idAnagrafica,TIPO_ANAGRAFICA tipoAnagrafica,PARTITA_IVA partitaIva,DENOMINAZIONE denominazione,COGNOME_LR cognomeLR" +
		",NOME_LR nomeLR,SESSO_LR sessoLR,ID_COMUNE_NS_LR idComuneNSLR, COMUNE_NS_LR comuneNSLR, DATA_NS_LR dataNSLR,INDIRIZZO_LR indirizzoLR,ID_COMUNE_RES_LR idComuneRESLR, COMUNE_RES_LR comuneRESLR" +
		",CAP_RES_LR capRESLR,CF_LR codiceFiscaleLR,CODICE_FISCALE codiceFiscale,COGNOME cognome,NOME nome,ALTRI_NOMI altriNomi,SESSO sesso" +
		",DATA_NS dataNS,ID_COMUNE_NS idComuneNS, COMUNE_NS comuneNS, PIGNORAMENTO pignoramento,NOTA_PIGNORAMENTO notaPignoramento,ESTERO estero,COMMISSIONI commissioni" +
		",ID_SEDE idSede,INDIRIZZO indirizzo,ID_COMUNE_SEDE idComuneSede, COMUNE_SEDE comuneSede, CAP_SEDE capSede,TELEFONO telefono,E_MAIL email,FAX fax,BOLLO bollo" +
		",DATA_CREAZIONE dataCreazione,NOME_SEDE nomeSede,ID_TIPO_PAGAMENTO idTipoPagamento,descrizione_pagamento descTipoPagamento" +
		",ID_conto_corrente idContoCorrente,ID_agenzia idAgenzia, NOME_BANCA nomeBanca, CONTO_CORRENTE contoCorrente,MODALITA_PRINCIPALE_SN modalitaPrincipale" +
		",SEDE_AGENZIA sedeAgenzia,INDIRIZZO_AGENZIA indirizzoAgenzia,CITTA citta,ABI abi,CAB cab,CIN cin,IBAN iban" +
		" from vw_anagrafica_sic" +
		" where TIPO_ANAGRAFICA        ='"+at.getTipoAnagrafica()+"'" +
		(at.getPartitaIva() != null ? " AND PARTITA_IVA      ='"+at.getPartitaIva()+"'" : "") +
		(at.getDenominazione() != null ? " AND DENOMINAZIONE    ='"+StringUtil.escapeApici(at.getDenominazione())+"'" : "") +
		(at.getCognomeLR() != null ? " AND COGNOME_LR       ='"+StringUtil.escapeApici(at.getCognomeLR())+"'" : "") +
		(at.getNomeLR() != null ? " AND NOME_LR          ='"+StringUtil.escapeApici(at.getNomeLR())+"'" : "") +
		(at.getSessoLR() != null ? " AND SESSO_LR         ='"+at.getSessoLR()+"'" : "") +
		(at.getComuneNSLR() != null ? " AND COMUNE_NS_LR     ='"+StringUtil.escapeApici(at.getComuneNSLR())+"'" : "") +
		(dNascLR != null ? " AND DATA_NS_LR=TO_DATE('"+dNascLR+"','dd/mm/yyyy')" : "") +
		(at.getIndirizzoLR() != null ? " AND INDIRIZZO_LR     ='"+StringUtil.escapeApici(at.getIndirizzoLR())+"'" : "") +
		(at.getComuneRESLR() != null ? " AND COMUNE_RES_LR    ='"+StringUtil.escapeApici(at.getComuneRESLR())+"'" : "") +
		(at.getCAPRESLR() != null ? " AND CAP_RES_LR       ='"+at.getCAPRESLR()+"'" : "") +
		(at.getCodiceFiscaleLR() != null ? " AND CF_LR='"+at.getCodiceFiscaleLR()+"'" : "") +
		(at.getCodiceFiscale() != null ? " AND CODICE_FISCALE   ='"+at.getCodiceFiscale()+"'" : "") +
		(at.getCognome() != null ? " AND COGNOME ='"+StringUtil.escapeApici(at.getCognome())+"'" : "") +
		(at.getNome() != null ? " AND NOME ='"+StringUtil.escapeApici(at.getNome())+"'" : "") +
		(at.getAltriNomi() != null ? " AND ALTRI_NOMI ='"+StringUtil.escapeApici(at.getAltriNomi())+"'" : "") +
		(at.getSesso() != null ? " AND SESSO ='"+at.getSesso()+"'" : "") +
		(dNasc != null ? " AND DATA_NS =TO_DATE('"+dNasc+"','dd/mm/yyyy')" : "") +
		(at.getComuneNS() != null ? " AND COMUNE_NS ='"+StringUtil.escapeApici(at.getComuneNS())+"'" : "") +
		(at.getPignoramento() != null ? " AND PIGNORAMENTO     ='"+at.getPignoramento()+"'" : "") +
		(at.getNotaPignoramento() != null ? " AND NOTA_PIGNORAMENTO='"+at.getNotaPignoramento()+"'" : "") +
		(at.getEstero() != null ? " AND ESTERO ='"+at.getEstero()+"'" :"") +
		(at.getCommissioni() != null ? " AND nvl(COMMISSIONI,'N') ='"+at.getCommissioni()+"'" : "") +
		(at.getSede().getIndirizzo() != null ? " AND INDIRIZZO ='"+StringUtil.escapeApici(at.getSede().getIndirizzo())+"'" : "") +
		(at.getSede().getComune() != null ? " AND COMUNE_SEDE ='"+StringUtil.escapeApici(at.getSede().getComune())+"'" : "") +
		(at.getSede().getCAP() != null ? " AND CAP_SEDE ='"+at.getSede().getCAP()+"'" : "") +
		(at.getSede().getTelefono() != null ? " AND TELEFONO='"+at.getSede().getTelefono()+"'" : "") +
		(at.getSede().getEMail() != null ? " AND E_MAIL='"+at.getSede().getEMail()+"'" : "") +
		(at.getSede().getFax() != null ? " AND FAX='"+at.getSede().getFax()+"'" : "") +
		(at.getSede().getBollo() != null ? " AND nvl(BOLLO,'N') ='"+at.getSede().getBollo()+"'" : "") +
		(at.getSede().getNomeSede() != null ? " AND NOME_SEDE        ='"+StringUtil.escapeApici(at.getSede().getNomeSede())+"'" : "") +
		(at.getSede().getTipoPagamento() != null ? " AND upper(DESCRIZIONE_PAGAMENTO) ='"+StringUtil.escapeApici(at.getSede().getTipoPagamento().toUpperCase())+"'" : "") +
		(at.getDatiBancari().getNomeBanca() != null ? " AND NOME_BANCA       ='"+StringUtil.escapeApici(at.getDatiBancari().getNomeBanca())+"'" : "") +
		(at.getDatiBancari().getContoCorrente() != null ? "AND conto_corrente   ='"+at.getDatiBancari().getContoCorrente()+"'" : "") +
		(at.getDatiBancari().getModalitaPrincipale() != null ?" AND MODALITA_PRINCIPALE_SN   ='"+at.getDatiBancari().getModalitaPrincipale()+"'" : "") +
		(at.getDatiBancari().getSedeAgenzia() != null ? " AND SEDE_AGENZIA     ='"+StringUtil.escapeApici(at.getDatiBancari().getSedeAgenzia())+"'" : "") +
		(at.getDatiBancari().getIndirizzo() != null ? " AND INDIRIZZO_AGENZIA='"+StringUtil.escapeApici(at.getDatiBancari().getIndirizzo())+"'" : "") +
		(at.getDatiBancari().getCitta() != null ? " AND CITTA='"+StringUtil.escapeApici(at.getDatiBancari().getCitta())+"'" : "") +
		(at.getDatiBancari().getABI() != null ? " AND ABI='"+at.getDatiBancari().getABI()+"'" : "") +
		(at.getDatiBancari().getCAB() != null ? " AND CAB='"+at.getDatiBancari().getCAB()+"'" : "") +
		(at.getDatiBancari().getCIN() != null ? " AND CIN='"+at.getDatiBancari().getCIN()+"'" : "") +
		(at.getDatiBancari().getIBAN() != null ? " AND IBAN ='"+at.getDatiBancari().getIBAN()+"'" : "");

		ResultSet rs = s.executeQuery(query);

		if (rs.next())
		{
			iar = new Anagrafica();
			iar.setAltriNomi(rs.getString("altriNomi"));
			iar.setCapResLR(rs.getString("capRESLR"));
			iar.setCodiceFiscale(rs.getString("codiceFiscale"));
			iar.setCodiceFiscaleLR(rs.getString("codiceFiscaleLR"));
			iar.setCognome(rs.getString("cognome"));
			iar.setCognomeLR(rs.getString("cognomeLR"));
			iar.setCommissioni(rs.getString("commissioni"));
			iar.setComuneNS(rs.getString("comuneNS"));
			iar.setComuneNSLR(rs.getString("comuneNSLR"));
			iar.setComuneRESLR(rs.getString("comuneRESLR"));

			java.sql.Date dataS = rs.getDate("dataNS");
			if (dataS!=null) {
				java.util.Date dataU = new Date(dataS.getTime());
				iar.setDataNascita(dataU);
			}

			java.sql.Date dataS1 = rs.getDate("dataNSLR");
			if (dataS1!=null) {
				java.util.Date dataU1 = new Date(dataS1.getTime());
				iar.setDataNascitaLR(dataU1);
			}

			iar.setDenominazione(rs.getString("denominazione"));
			iar.setEstero(rs.getString("estero"));
			iar.setIdAnagrafica(rs.getString("idAnagrafica"));
			iar.setIdComuneNS(rs.getString("idComuneNS"));
			iar.setIdComuneNSLR(rs.getString("idComuneNSLR"));
			iar.setIdComuneRESLR(rs.getString("idComuneRESLR"));
			iar.setIndirizzoLR(rs.getString("indirizzoLR"));
			iar.setNome(rs.getString("nome"));
			iar.setNomeLR(rs.getString("nomeLR"));
			iar.setNotaPignoramento(rs.getString("notaPignoramento"));
			iar.setPartitaIva(rs.getString("partitaIva"));
			iar.setPignoramento(rs.getString("pignoramento"));
			iar.setSesso(rs.getString("sesso"));
			iar.setSessoLR(rs.getString("sessoLR"));
			iar.setTipoAnagrafica(rs.getString("tipoAnagrafica"));

			if (rs.getString("idSede") != null)
			{
				it.latraccia.entity.anasic.risposta.SedeTypes ss = objFactory1.createSedeTypes();
				ss.setBollo(rs.getString("bollo"));
				ss.setCAP(rs.getString("capSede"));
				ss.setComune(rs.getString("comuneSede"));

				Calendar cal = Calendar.getInstance();
				java.sql.Date dataS2 = rs.getDate("dataCreazione");
				java.util.Date dataU2 = new Date(dataS2.getTime());
				cal.setTime(dataU2);
				ss.setDataCreazione(cal);

				ss.setDescTipoPagamento(rs.getString("descTipoPagamento"));
				ss.setEMail(rs.getString("eMail"));
				ss.setFax(rs.getString("fax"));
				ss.setIdComune(rs.getString("idComuneSede"));
				ss.setIdSede(rs.getString("idSede"));
				ss.setIdTipoPagamento(rs.getString("idTipoPagamento"));
				ss.setIndirizzo(rs.getString("indirizzo"));
				ss.setNomeSede(rs.getString("nomeSede"));
				ss.setTelefono(rs.getString("telefono"));

				if (rs.getString("idContoCorrente") != null)
				{
					it.latraccia.entity.anasic.risposta.DatiBancariTypes bb = objFactory1.createDatiBancariTypes();
					bb.setABI(rs.getString("abi"));
					bb.setCAB(rs.getString("cab"));
					bb.setCIN(rs.getString("cin"));
					bb.setCitta(rs.getString("citta"));
					bb.setContoCorrente(rs.getString("contoCorrente"));
					bb.setIBAN(rs.getString("iban"));
					bb.setIdAgenzia(rs.getString("idAgenzia"));
					bb.setIdContoCorrente(rs.getString("idContoCorrente"));
					bb.setIndirizzo(rs.getString("indirizzoAgenzia"));
					bb.setModalitaPrincipale(rs.getString("modalitaPrincipale"));
					bb.setNomeBanca(rs.getString("nomeBanca"));
					bb.setSedeAgenzia(rs.getString("sedeAgenzia"));

					lContiBancari.getContoBancario().add(bb);
				}
				ss.setListaDatiBancari(lContiBancari);

				lSedi.getSede().add(ss);
			}

			iar.setListaSedi(lSedi);

		}

		s.close();

		return iar;
	}

	public CreaAnagrafeResult creaAnagrafe(AnagraficaTypes anagrafica, String codiceFiscaleOperatore) throws SQLException
	{
		java.util.Date utilDate;
		java.sql.Date sqlDate;

		CreaAnagrafeResult car = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call crea_anagrafica(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(45, Types.NUMERIC); //id_anagrafica
		cs.registerOutParameter(46, Types.NUMERIC); //id_sede
		cs.registerOutParameter(47, Types.NUMERIC); //id_tipo_pagamento
		cs.registerOutParameter(48, Types.NUMERIC); //id_conto_corrente
		cs.registerOutParameter(49, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(50, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT
		cs.setString(1, anagrafica.getTipoAnagrafica());  //Tipo Anagrafica

		if (anagrafica.getPartitaIva() == null)
		{
			cs.setNull(2, Types.VARCHAR);  //Partita Iva non si setta
		}
		else
		{
			cs.setString(2, anagrafica.getPartitaIva());  //Partita Iva
		}

		if (anagrafica.getDenominazione() == null)
		{
			cs.setNull(3, Types.VARCHAR);  //Denominazione non si setta
		}
		else
		{
			cs.setString(3, anagrafica.getDenominazione());  //Denominazione
		}

		if (anagrafica.getCognomeLR() == null)
		{
			cs.setNull(4, Types.VARCHAR);  //Cognome Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(4, anagrafica.getCognomeLR());  //Cognome Legale Rappresentante
		}

		if (anagrafica.getNomeLR() == null)
		{
			cs.setNull(5, Types.VARCHAR);  //Nome Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(5, anagrafica.getNomeLR());  //Nome Legale Rappresentante
		}

		if (anagrafica.getSessoLR() == null)
		{
			cs.setNull(6, Types.VARCHAR);  //Sesso Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(6, anagrafica.getSessoLR());  //Sesso Legale Rappresentante
		}

		if (anagrafica.getComuneNSLR() == null)
		{
			cs.setNull(7, Types.VARCHAR);  //Comune Nascita Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(7, anagrafica.getComuneNSLR());  //Comune Nascita Legale Rappresentante
		}

		if (anagrafica.getDataNascitaLR() == null)
		{
			cs.setNull(8, Types.DATE);  //Data di Nascita Legale Rappresentante non si setta
		}
		else
		{
			utilDate = anagrafica.getDataNascitaLR().getTime();
			sqlDate = new java.sql.Date(utilDate.getTime());
			cs.setDate(8, sqlDate);  //Data di Nascita Legale Rappresentante
		}

		if (anagrafica.getIndirizzoLR() == null)
		{
			cs.setNull(9, Types.VARCHAR);  //Indirizzo Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(9, anagrafica.getIndirizzoLR());  //Indirizzo Legale Rappresentante
		}

		if (anagrafica.getComuneRESLR() == null)
		{
			cs.setNull(10, Types.VARCHAR);  //Comune Residenza Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(10, anagrafica.getComuneRESLR());  //Comune Residenza Legale Rappresentante
		}

		if (anagrafica.getCAPRESLR() == null)
		{
			cs.setNull(11, Types.VARCHAR);  //CAP Residenza Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(11, anagrafica.getCAPRESLR());  //CAP Residenza Legale Rappresentante
		}

		if (anagrafica.getCodiceFiscaleLR() == null)
		{
			cs.setNull(12, Types.VARCHAR);  //Codice Fiscale Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(12, anagrafica.getCodiceFiscaleLR());  //Codice Fiscale Legale Rappresentante
		}

		if (anagrafica.getCodiceFiscale() == null)
		{
			cs.setNull(13, Types.VARCHAR);  //Codice Fiscale  non si setta
		}
		else
		{
			cs.setString(13, anagrafica.getCodiceFiscale());  //Codice Fiscale
		}

		if (anagrafica.getCognome() == null)
		{
			cs.setNull(14, Types.VARCHAR);  //Cognome  non si setta
		}
		else
		{
			cs.setString(14, anagrafica.getCognome());  //Cognome
		}

		if (anagrafica.getNome() == null)
		{
			cs.setNull(15, Types.VARCHAR);  //Nome  non si setta
		}
		else
		{
			cs.setString(15, anagrafica.getNome());  //Nome
		}

		if (anagrafica.getAltriNomi() == null)
		{
			cs.setNull(16, Types.VARCHAR);  //Altri Nomi  non si setta
		}
		else
		{
			cs.setString(16, anagrafica.getAltriNomi());  //Altri Nomi
		}

		if (anagrafica.getSesso() == null)
		{
			cs.setNull(17, Types.VARCHAR);  //Sesso  non si setta
		}
		else
		{
			cs.setString(17, anagrafica.getSesso());  //Sesso
		}

		if (anagrafica.getDataNascita() == null)
		{
			cs.setNull(18, Types.DATE);  //Data di Nascita non si setta
		}
		else
		{
			utilDate = anagrafica.getDataNascita().getTime();
			sqlDate = new java.sql.Date(utilDate.getTime());
			cs.setDate(18, sqlDate);  //Data di Nascita
		}

		if (anagrafica.getComuneNS() == null)
		{
			cs.setNull(19, Types.VARCHAR);  //Comune di Nascita  non si setta
		}
		else
		{
			cs.setString(19, anagrafica.getComuneNS());  //Comune di Nascita
		}

		if (anagrafica.getPignoramento() == null)
		{
			cs.setNull(20, Types.VARCHAR);  //Pignoramento  non si setta
		}
		else
		{
			cs.setString(20, anagrafica.getPignoramento());  //Pignoramento
		}

		if (anagrafica.getNotaPignoramento() == null)
		{
			cs.setNull(21, Types.VARCHAR);  //Nota Pignoramento  non si setta
		}
		else
		{
			cs.setString(21, anagrafica.getNotaPignoramento());  //Nota Pignoramento
		}

		if (anagrafica.getEstero() == null)
		{
			cs.setNull(22, Types.VARCHAR);  //Estero  non si setta
		}
		else
		{
			cs.setString(22, anagrafica.getEstero());  //Estero
		}

		if (anagrafica.getCommissioni() == null)
		{
			cs.setNull(23, Types.VARCHAR);  //Commissioni  non si setta
		}
		else
		{
			cs.setString(23, anagrafica.getCommissioni());  //Commissioni
		}

		if (anagrafica.getSede().getIndirizzo() == null)
		{
			cs.setNull(24, Types.VARCHAR);  //Indirizzo della sede  non si setta
		}
		else
		{
			cs.setString(24, anagrafica.getSede().getIndirizzo());  //Indirizzo della sede
		}

		if (anagrafica.getSede().getComune() == null)
		{
			cs.setNull(25, Types.VARCHAR);  //Comune della sede  non si setta
		}
		else
		{
			cs.setString(25, anagrafica.getSede().getComune());  //Comune della sede
		}

		if (anagrafica.getSede().getCAP() == null)
		{
			cs.setNull(26, Types.VARCHAR);  //CAP della sede  non si setta
		}
		else
		{
			cs.setString(26, anagrafica.getSede().getCAP());  //CAP della sede
		}

		if (anagrafica.getSede().getTelefono() == null)
		{
			cs.setNull(27, Types.VARCHAR);  //Telefono della sede  non si setta
		}
		else
		{
			cs.setString(27, anagrafica.getSede().getTelefono());  //Telefono della sede
		}

		if (anagrafica.getSede().getEMail() == null)
		{
			cs.setNull(28, Types.VARCHAR);  //E-Mail della sede  non si setta
		}
		else
		{
			cs.setString(28, anagrafica.getSede().getEMail());  //E-Mail della sede
		}

		if (anagrafica.getSede().getFax() == null)
		{
			cs.setNull(29, Types.VARCHAR);  //FAX della sede  non si setta
		}
		else
		{
			cs.setString(29, anagrafica.getSede().getFax());  //Fax della sede
		}

		if (anagrafica.getSede().getBollo() == null)
		{
			cs.setNull(30, Types.VARCHAR);  //Bollo della sede  non si setta
		}
		else
		{
			cs.setString(30, anagrafica.getSede().getBollo());  //Bollo della sede
		}

		if (anagrafica.getSede().getTipoPagamento() == null)
		{
			cs.setNull(31, Types.VARCHAR);  //Tipo Pagamento della sede  non si setta
		}
		else
		{
			cs.setString(31, anagrafica.getSede().getTipoPagamento());  //Tipo Pagamento della sede
		}

		if (anagrafica.getDatiBancari().getContoCorrente() == null)
		{
			cs.setNull(32, Types.VARCHAR);  //Conto Corrente  non si setta
		}
		else
		{
			cs.setString(32, anagrafica.getDatiBancari().getContoCorrente());  //Conto Corrente
		}

		if (anagrafica.getDatiBancari().getModalitaPrincipale() == null)
		{
			cs.setNull(33, Types.VARCHAR);  //Modalit� Principale non si setta
		}
		else
		{
			cs.setString(33, anagrafica.getDatiBancari().getModalitaPrincipale());  //Modalit� Principale
		}

		if (anagrafica.getDatiBancari().getNomeBanca() == null)
		{
			cs.setNull(34, Types.VARCHAR);  //Nome della banca non si setta
		}
		else
		{
			cs.setString(34, anagrafica.getDatiBancari().getNomeBanca());  //Nome della banca
		}

		if (anagrafica.getDatiBancari().getSedeAgenzia() == null)
		{
			cs.setNull(35, Types.VARCHAR);  //Sede Agenzia non si setta
		}
		else
		{
			cs.setString(35, anagrafica.getDatiBancari().getSedeAgenzia());  //Sede Agenzia
		}

		if (anagrafica.getDatiBancari().getIndirizzo() == null)
		{
			cs.setNull(36, Types.VARCHAR);  //Indirizzo Agenzia non si setta
		}
		else
		{
			cs.setString(36, anagrafica.getDatiBancari().getIndirizzo());  //Indirizzo Agenzia
		}

		if (anagrafica.getDatiBancari().getCitta() == null)
		{
			cs.setNull(37, Types.VARCHAR);  //Citt� Agenzia non si setta
		}
		else
		{
			cs.setString(37, anagrafica.getDatiBancari().getCitta());  //Citt� Agenzia
		}

		if (anagrafica.getDatiBancari().getCAP() == null)
		{
			cs.setNull(38, Types.VARCHAR);  //CAP Agenzia non si setta
		}
		else
		{
			cs.setString(38, anagrafica.getDatiBancari().getCAP());  //CAP Agenzia
		}

		if (anagrafica.getDatiBancari().getProvincia() == null)
		{
			cs.setNull(39, Types.VARCHAR);  //Provincia Agenzia non si setta
		}
		else
		{
			cs.setString(39, anagrafica.getDatiBancari().getProvincia());  //Provincia Agenzia
		}

		if (anagrafica.getDatiBancari().getABI() == null)
		{
			cs.setNull(40, Types.VARCHAR);  //ABI non si setta
		}
		else
		{
			cs.setString(40, anagrafica.getDatiBancari().getABI());  //ABI
		}

		if (anagrafica.getDatiBancari().getCAB() == null)
		{
			cs.setNull(41, Types.VARCHAR);  //CAB non si setta
		}
		else
		{
			cs.setString(41, anagrafica.getDatiBancari().getCAB());  //CAB
		}

		if (anagrafica.getDatiBancari().getCIN() == null)
		{
			cs.setNull(42, Types.VARCHAR);  //CIN non si setta
		}
		else
		{
			cs.setString(42, anagrafica.getDatiBancari().getCIN());  //CIN
		}

		if (anagrafica.getDatiBancari().getIBAN() == null)
		{
			cs.setNull(43, Types.VARCHAR);  //IBAN non si setta
		}
		else
		{
			cs.setString(43, anagrafica.getDatiBancari().getIBAN());  //IBAN
		}

		if (codiceFiscaleOperatore == null)
		{
			cs.setNull(44, Types.VARCHAR);  //Codice Fiscale dell'operatore non si setta
		}
		else
		{
			cs.setString(44, codiceFiscaleOperatore);  //Codice Fiscale dell'operatore
		}

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		car = new CreaAnagrafeResult();
		if (cs.getInt(49)==0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			car.setCodiceRisposta(cs.getInt(49));

			if (cs.getString(50) == null || cs.getString(50) == "")
			{
				car.setDescrizioneRisposta("Nessuna");
			}
			else
			{
				car.setDescrizioneRisposta(cs.getString(50));
			}

		}
		else
		{
			car.setIdAnagrafica(String.valueOf(cs.getInt(45)));
			car.setIdSede(String.valueOf(cs.getInt(46)));
			car.setIdTipoPagamento(String.valueOf(cs.getInt(47)));
			car.setIdContoCorrente(String.valueOf(cs.getInt(48)));

		}

		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		if (cs != null) {
			cs.close();
		}
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		
		return car;
	}

	public CreaSedeResult creaSede(CreaSedeTypes creaSede) throws SQLException
	{
		CreaSedeResult csr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call crea_sede(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(23, Types.NUMERIC); //id_anagrafica
		cs.registerOutParameter(24, Types.NUMERIC); //id_sede
		cs.registerOutParameter(25, Types.NUMERIC); //id_tipo_pagamento
		cs.registerOutParameter(26, Types.NUMERIC); //id_conto_corrente
		cs.registerOutParameter(27, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(28, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT
		cs.setString(1, creaSede.getIdAnagrafica());  //Id Anagrafica
		cs.setString(2, creaSede.getSede().getIndirizzo());  //Indirizzo Sede
		cs.setString(3, creaSede.getSede().getComune());  //Comune Sede
		cs.setString(4, creaSede.getSede().getCAP());  //CAP Sede

		if (creaSede.getSede().getTelefono() == null)
		{
			cs.setNull(5, Types.VARCHAR);  //Telefono Sede non si setta
		}
		else
		{
			cs.setString(5, creaSede.getSede().getTelefono());  //Telefono Sede
		}

		if (creaSede.getSede().getEMail() == null)
		{
			cs.setNull(6, Types.VARCHAR);  //E-Mail Sede non si setta
		}
		else
		{
			cs.setString(6, creaSede.getSede().getEMail());  //E-Mail Sede
		}

		if (creaSede.getSede().getFax() == null)
		{
			cs.setNull(7, Types.VARCHAR);  //Fax Sede non si setta
		}
		else
		{
			cs.setString(7, creaSede.getSede().getFax());  //Fax Sede
		}

		if (creaSede.getSede().getBollo() == null)
		{
			cs.setNull(8, Types.VARCHAR);  //Bollo Sede non si setta
		}
		else
		{
			cs.setString(8, creaSede.getSede().getBollo());  //Bollo Sede
		}

		if (creaSede.getSede().getTipoPagamento() == null)
		{
			cs.setNull(9, Types.VARCHAR);  //Tipo Pagamento Sede non si setta
		}
		else
		{
			cs.setString(9, creaSede.getSede().getTipoPagamento());  //Tipo Pagamento Sede
		}

		if (creaSede.getDatiBancari().getContoCorrente() == null)
		{
			cs.setNull(10, Types.VARCHAR);  //Conto Corrente non si setta
		}
		else
		{
			cs.setString(10, creaSede.getDatiBancari().getContoCorrente());  //Conto Corrente
		}

		if (creaSede.getDatiBancari().getModalitaPrincipale() == null)
		{
			cs.setNull(11, Types.VARCHAR);  //Modalit� Principale non si setta
		}
		else
		{
			cs.setString(11, creaSede.getDatiBancari().getModalitaPrincipale());  //Modalit� Principale
		}

		if (creaSede.getDatiBancari().getNomeBanca() == null)
		{
			cs.setNull(12, Types.VARCHAR);  //Nome Banca non si setta
		}
		else
		{
			cs.setString(12, creaSede.getDatiBancari().getNomeBanca());  //Nome Banca
		}

		if (creaSede.getDatiBancari().getSedeAgenzia() == null)
		{
			cs.setNull(13, Types.VARCHAR);  //Sede Agenzia non si setta
		}
		else
		{
			cs.setString(13, creaSede.getDatiBancari().getSedeAgenzia());  //Sede Agenzia
		}

		if (creaSede.getDatiBancari().getIndirizzo() == null)
		{
			cs.setNull(14, Types.VARCHAR);  //Indirizzo Agenzia non si setta
		}
		else
		{
			cs.setString(14, creaSede.getDatiBancari().getIndirizzo());  //Indirizzo Agenzia
		}

		if (creaSede.getDatiBancari().getCitta() == null)
		{
			cs.setNull(15, Types.VARCHAR);  //Citt� Agenzia non si setta
		}
		else
		{
			cs.setString(15, creaSede.getDatiBancari().getCitta());  //Citt� Agenzia
		}

		if (creaSede.getDatiBancari().getCAP() == null)
		{
			cs.setNull(16, Types.VARCHAR);  //CAP Agenzia non si setta
		}
		else
		{
			cs.setString(16, creaSede.getDatiBancari().getCAP());  //CAP Agenzia
		}

		if (creaSede.getDatiBancari().getProvincia() == null)
		{
			cs.setNull(17, Types.VARCHAR);  //Provincia Agenzia non si setta
		}
		else
		{
			cs.setString(17, creaSede.getDatiBancari().getProvincia());  //Provincia Agenzia
		}

		if (creaSede.getDatiBancari().getABI() == null)
		{
			cs.setNull(18, Types.VARCHAR);  //ABI Agenzia non si setta
		}
		else
		{
			cs.setString(18, creaSede.getDatiBancari().getABI());  //ABI Agenzia
		}

		if (creaSede.getDatiBancari().getCAB() == null)
		{
			cs.setNull(19, Types.VARCHAR);  //CAB Agenzia non si setta
		}
		else
		{
			cs.setString(19, creaSede.getDatiBancari().getCAB());  //CAB Agenzia
		}

		if (creaSede.getDatiBancari().getCIN() == null)
		{
			cs.setNull(20, Types.VARCHAR);  //CIN Agenzia non si setta
		}
		else
		{
			cs.setString(20, creaSede.getDatiBancari().getCIN());  //CIN Agenzia
		}

		if (creaSede.getDatiBancari().getIBAN() == null)
		{
			cs.setNull(21, Types.VARCHAR);  //IBAN non si setta
		}
		else
		{
			cs.setString(21, creaSede.getDatiBancari().getIBAN());  //IBAN
		}

		if (creaSede.getCodiceFiscaleOperatore() == null)
		{
			cs.setNull(22, Types.VARCHAR);  //Codice Fiscale dell'operatore non si setta
		}
		else
		{
			cs.setString(22, creaSede.getCodiceFiscaleOperatore());  //Codice Fiscale dell'operatore
		}

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		csr = new CreaSedeResult();
		if (cs.getInt(27)==0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			csr.setCodiceRisposta(cs.getInt(27));

			if (cs.getString(28) == null || cs.getString(28) == "")
			{
				csr.setDescrizioneRisposta("Nessuna");
			}
			else
			{
				csr.setDescrizioneRisposta(cs.getString(28));
			}

		}
		else
		{
			csr.setIdAnagrafica(String.valueOf(cs.getInt(23)));
			csr.setIdSede(String.valueOf(cs.getInt(24)));
			csr.setIdTipoPagamento(String.valueOf(cs.getInt(25)));
			csr.setIdContoCorrente(String.valueOf(cs.getInt(26)));

		}
		
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		if (cs != null) {
			cs.close();
		}
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		
		return csr;
	}

	public CreaContoBancarioResult creaContoBancario(CreaContoBancarioTypes creaContoBancario) throws SQLException
	{
		CreaContoBancarioResult ccbr = null;


		if ( (creaContoBancario.getDatiBancari().getModalitaPrincipale()==null) || (creaContoBancario.getDatiBancari().getIBAN()==null) )
		{

			CreaContoBancarioResult ccbr1 = new CreaContoBancarioResult();
			ccbr1.setCodiceRisposta(0);
			ccbr1.setDescrizioneRisposta("Manca un dato bancario obbligatorio");			
			return ccbr1;
		}

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call crea_conto(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(16, Types.NUMERIC); //id_anagrafica
		cs.registerOutParameter(17, Types.NUMERIC); //id_sede
		cs.registerOutParameter(18, Types.NUMERIC); //id_tipo_pagamento
		cs.registerOutParameter(19, Types.NUMERIC); //id_conto_corrente
		cs.registerOutParameter(20, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(21, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT

		cs.setString(1, creaContoBancario.getIdAnagrafica());  //Id Anagrafica
		cs.setString(2, creaContoBancario.getIdSede());  //Id Sede

		if (creaContoBancario.getDatiBancari().getContoCorrente() == null)
		{
			cs.setNull(3, Types.VARCHAR);  //Conto Corrente non si setta
		}
		else
		{
			cs.setString(3, creaContoBancario.getDatiBancari().getContoCorrente());  //Conto Corrente
		}

		if (creaContoBancario.getDatiBancari().getModalitaPrincipale() == null)
		{
			cs.setNull(4, Types.VARCHAR);  //Modalit� Principale non si setta
		}
		else
		{
			cs.setString(4, creaContoBancario.getDatiBancari().getModalitaPrincipale());  //Modalit� Principale
		}

		if (creaContoBancario.getDatiBancari().getNomeBanca() == null)
		{
			cs.setNull(5, Types.VARCHAR);  //Nome Banca non si setta
		}
		else
		{
			cs.setString(5, creaContoBancario.getDatiBancari().getNomeBanca());  //Nome Banca
		}

		if (creaContoBancario.getDatiBancari().getSedeAgenzia() == null)
		{
			cs.setNull(6, Types.VARCHAR);  //Sede Agenzia non si setta
		}
		else
		{
			cs.setString(6, creaContoBancario.getDatiBancari().getSedeAgenzia());  //Sede Agenzia
		}

		if (creaContoBancario.getDatiBancari().getIndirizzo() == null)
		{
			cs.setNull(7, Types.VARCHAR);  //Indirizzo Agenzia non si setta
		}
		else
		{
			cs.setString(7, creaContoBancario.getDatiBancari().getIndirizzo());  //Indirizzo Agenzia
		}

		if (creaContoBancario.getDatiBancari().getCitta() == null)
		{
			cs.setNull(8, Types.VARCHAR);  //Citt� Agenzia non si setta
		}
		else
		{
			cs.setString(8, creaContoBancario.getDatiBancari().getCitta());  //Citt� Agenzia
		}

		if (creaContoBancario.getDatiBancari().getCAP() == null)
		{
			cs.setNull(9, Types.VARCHAR);  //CAP Agenzia non si setta
		}
		else
		{
			cs.setString(9, creaContoBancario.getDatiBancari().getCAP());  //CAP Agenzia
		}

		if (creaContoBancario.getDatiBancari().getProvincia() == null)
		{
			cs.setNull(10, Types.VARCHAR);  //Provincia Agenzia non si setta
		}
		else
		{
			cs.setString(10, creaContoBancario.getDatiBancari().getProvincia());  //Provincia Agenzia
		}

		if (creaContoBancario.getDatiBancari().getABI() == null)
		{
			cs.setNull(11, Types.VARCHAR);  //ABI non si setta
		}
		else
		{
			cs.setString(11, creaContoBancario.getDatiBancari().getABI());  //ABI
		}

		if (creaContoBancario.getDatiBancari().getCAB() == null)
		{
			cs.setNull(12, Types.VARCHAR);  //CAB non si setta
		}
		else
		{
			cs.setString(12, creaContoBancario.getDatiBancari().getCAB());  //CAB
		}

		if (creaContoBancario.getDatiBancari().getCIN() == null)
		{
			cs.setNull(13, Types.VARCHAR);  //CIN non si setta
		}
		else
		{
			cs.setString(13, creaContoBancario.getDatiBancari().getCIN());  //CIN
		}

		if (creaContoBancario.getDatiBancari().getIBAN() == null)
		{
			cs.setNull(14, Types.VARCHAR);  //IBAN non si setta
		}
		else
		{
			cs.setString(14, creaContoBancario.getDatiBancari().getIBAN());  //IBAN
		}

		if (creaContoBancario.getCodiceFiscaleOperatore() == null)
		{
			cs.setNull(15, Types.VARCHAR);  //Codice Fiscale dell'operatore non si setta
		}
		else
		{
			cs.setString(15, creaContoBancario.getCodiceFiscaleOperatore());  //Codice Fiscale dell'Operatore
		}

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		ccbr = new CreaContoBancarioResult();
		if (cs.getInt(20)==0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			ccbr.setCodiceRisposta(cs.getInt(20));

			if (cs.getString(21) == null || cs.getString(21) == "")
			{
				ccbr.setDescrizioneRisposta("Nessuna");
			}
			else
			{
				ccbr.setDescrizioneRisposta(cs.getString(21));
			}
		}
		else
		{
			ccbr.setIdAnagrafica(String.valueOf(cs.getInt(16)));
			ccbr.setIdSede(String.valueOf(cs.getInt(17)));
			ccbr.setIdTipoPagamento(String.valueOf(cs.getInt(18)));
			ccbr.setIdContoCorrente(String.valueOf(cs.getInt(19)));
		}
		
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		if (cs != null) {
			cs.close();
		}
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		
		return ccbr;
	}

	public List getComune(String nomeComune) throws SQLException, JAXBException 
	{
		List r = new ArrayList();
		it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();    	
		Comune icr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "select distinct ID idComune, DESCRIZIONE descr, CODICE_ISTAT codiceIstat, CAP cap, CODICE_BEL_FIORE codiceBelFiore, COD_PROVINCIA codiceProvincia, " +
		" PROVINCIA provincia, COD_REGIONE codRegione, REGIONE regione, COD_RIPARTIZIONE_GEOGRAFICA codRipartizioneGeografica, " +
		" DESC_RIPARTIZIONE_GEOGRAFICA descRipartizioneGeografica, NAZIONE nazione" +
		" from fin_t_comuni" +
		" where upper(descrizione) like upper('"+nomeComune+"')||'%' order by 2";

		ResultSet rs = s.executeQuery(query);

		while (rs.next())
		{
			icr = new Comune();
			icr.setCAP(rs.getString("cap"));
			icr.setCodiceBelFiore(rs.getString("codiceBelFiore"));
			icr.setCodiceIstat(rs.getString("codiceIstat"));
			icr.setCodiceProvincia(rs.getString("codiceProvincia"));
			icr.setCodiceRegione(rs.getString("codRegione"));
			icr.setCodRipartizioneGeo(rs.getString("codRipartizioneGeografica"));
			icr.setDescRipartizioneGeo(rs.getString("descRipartizioneGeografica"));
			icr.setDescrizione(rs.getString("descr"));
			icr.setIdComune(rs.getString("idComune"));
			icr.setNazione(rs.getString("nazione"));
			icr.setProvincia(rs.getString("provincia"));
			icr.setRegione(rs.getString("regione"));
			r.add(icr);
		}
		s.close();

		return r;
	}

	public String getSede(String idAnagrafica, String nomeSede) throws SQLException 
	{
		String idSede = null;
		
		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "select id_sede from fin_t_ana_sedi where id_ana='"+idAnagrafica+"' and replace(replace(upper(nome_sede),' ',''),'N.','') = replace(replace(upper('"+nomeSede+"'),' ',''),'N.','')";

		ResultSet rs = s.executeQuery(query);

		if (rs.next()) {
			idSede = rs.getString("id_sede");
		}
		s.close();

		return idSede;
	}
	
	public String getContoBancario(String iban) throws SQLException 
	{
		String idConto = null;
		
		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		Statement s=null;
		s = connection.createStatement();

		String query = "select id from fin_t_ana_conti_bancari where upper(iban)=upper('"+iban+"')";

		ResultSet rs = s.executeQuery(query);

		if (rs.next()) {
			idConto = rs.getString("id");
		}
		s.close();

		return idConto;
	}

	public ModificaAnagraficaResult modificaAnagrafica(ModificaAnagraficaTypes modificaAnagrafica) throws SQLException 
	{
		ModificaAnagraficaResult mar = null;
		java.util.Date utilDate;
		java.sql.Date sqlDate;


		if ((modificaAnagrafica.getIdAnagrafica()==null) ||	(modificaAnagrafica.getTipoAnagrafica()==null))
		{

			ModificaAnagraficaResult mar1 = new ModificaAnagraficaResult();
			mar1.setCodiceRisposta(0);
			mar1.setDescrizioneRisposta("Manca un dato anagrafico obbligatorio");			
			return mar1;
		}

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call varia_anagrafica(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(26, Types.NUMERIC); //id_anagrafica
		cs.registerOutParameter(27, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(28, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT

		cs.setString(1, modificaAnagrafica.getIdAnagrafica());  //Id Anagrafica
		cs.setString(2, modificaAnagrafica.getTipoAnagrafica());  //Tipo Anagrafica

		if (modificaAnagrafica.getPartitaIva() == null)
		{
			cs.setNull(3, Types.VARCHAR);  //Partita Iva non si setta
		}
		else
		{
			cs.setString(3, modificaAnagrafica.getPartitaIva());  //Partita Iva
		}

		if (modificaAnagrafica.getDenominazione() == null)
		{
			cs.setNull(4, Types.VARCHAR);  //Denominazione non si setta
		}
		else
		{
			cs.setString(4, modificaAnagrafica.getDenominazione());  //Denominazione
		}

		if (modificaAnagrafica.getCognomeLR() == null)
		{
			cs.setNull(5, Types.VARCHAR);  //Cognome Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(5, modificaAnagrafica.getCognomeLR());  //Cognome Legale Rappresentante
		}

		if (modificaAnagrafica.getNomeLR() == null)
		{
			cs.setNull(6, Types.VARCHAR);  //Nome Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(6, modificaAnagrafica.getNomeLR());  //Nome Legale Rappresentante
		}

		if (modificaAnagrafica.getSessoLR() == null)
		{
			cs.setNull(7, Types.VARCHAR);  //Sesso Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(7, modificaAnagrafica.getSessoLR());  //Sesso Legale Rappresentante
		}

		if (modificaAnagrafica.getComuneNSLR() == null)
		{
			cs.setNull(8, Types.VARCHAR);  //Comune Nascita Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(8, modificaAnagrafica.getComuneNSLR());  //Comune Nascita Legale Rappresentante
		}

		if (modificaAnagrafica.getDataNascitaLR() == null)
		{
			cs.setNull(9, Types.DATE);  //Data di Nascita Legale Rappresentante non si setta
		}
		else
		{
			utilDate = modificaAnagrafica.getDataNascitaLR().getTime();
			sqlDate = new java.sql.Date(utilDate.getTime());
			cs.setDate(9, sqlDate);  //Data di Nascita Legale Rappresentante
		}

		if (modificaAnagrafica.getIndirizzoLR() == null)
		{
			cs.setNull(10, Types.VARCHAR);  //Indirizzo Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(10, modificaAnagrafica.getIndirizzoLR());  //Indirizzo Legale Rappresentante
		}

		if (modificaAnagrafica.getComuneRESLR() == null)
		{
			cs.setNull(11, Types.VARCHAR);  //Comune Residenza Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(11, modificaAnagrafica.getComuneRESLR());  //Comune Residenza Legale Rappresentante
		}

		if (modificaAnagrafica.getCAPRESLR() == null)
		{
			cs.setNull(12, Types.VARCHAR);  //CAP Residenza Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(12, modificaAnagrafica.getCAPRESLR());  //CAP Residenza Legale Rappresentante
		}

		if (modificaAnagrafica.getCodiceFiscaleLR() == null)
		{
			cs.setNull(13, Types.VARCHAR);  //Codice Fiscale Legale Rappresentante non si setta
		}
		else
		{
			cs.setString(13, modificaAnagrafica.getCodiceFiscaleLR());  //Codice Fiscale Legale Rappresentante
		}

		if (modificaAnagrafica.getCodiceFiscale() == null)
		{
			cs.setNull(14, Types.VARCHAR);  //Codice Fiscale non si setta
		}
		else
		{
			cs.setString(14, modificaAnagrafica.getCodiceFiscale());  //Codice Fiscale 
		}

		if (modificaAnagrafica.getCognome() == null)
		{
			cs.setNull(15, Types.VARCHAR);  //Cognome non si setta
		}
		else
		{
			cs.setString(15, modificaAnagrafica.getCognome());  //Cognome
		}

		if (modificaAnagrafica.getNome() == null)
		{
			cs.setNull(16, Types.VARCHAR);  //Nome non si setta
		}
		else
		{
			cs.setString(16, modificaAnagrafica.getNome());  //Nome
		}

		if (modificaAnagrafica.getAltriNomi() == null)
		{
			cs.setNull(17, Types.VARCHAR);  //Altri Nomi non si setta
		}
		else
		{
			cs.setString(17, modificaAnagrafica.getAltriNomi());  //Altri Nomi
		}

		if (modificaAnagrafica.getSesso() == null)
		{
			cs.setNull(18, Types.VARCHAR);  //Sesso non si setta
		}
		else
		{
			cs.setString(18, modificaAnagrafica.getSesso());  //Sesso 
		}

		if (modificaAnagrafica.getDataNascita() == null)
		{
			cs.setNull(19, Types.DATE);  //Data di Nascita non si setta
		}
		else
		{
			utilDate = modificaAnagrafica.getDataNascita().getTime();
			sqlDate = new java.sql.Date(utilDate.getTime());
			cs.setDate(19, sqlDate);  //Data di Nascita
		}

		if (modificaAnagrafica.getComuneNS() == null)
		{
			cs.setNull(20, Types.VARCHAR);  //Comune di Nascita  non si setta
		}
		else
		{
			cs.setString(20, modificaAnagrafica.getComuneNS());  //Comune di Nascita
		}

		if (modificaAnagrafica.getPignoramento() == null)
		{
			cs.setNull(21, Types.VARCHAR);  //Pignoramento  non si setta
		}
		else
		{
			cs.setString(21, modificaAnagrafica.getPignoramento());  //Pignoramento
		}

		if (modificaAnagrafica.getNotaPignoramento() == null)
		{
			cs.setNull(22, Types.VARCHAR);  //Nota Pignoramento  non si setta
		}
		else
		{
			cs.setString(22, modificaAnagrafica.getNotaPignoramento());  //Nota Pignoramento
		}

		if (modificaAnagrafica.getEstero() == null)
		{
			cs.setNull(23, Types.VARCHAR);  //Estero  non si setta
		}
		else
		{
			cs.setString(23, modificaAnagrafica.getEstero());  //Estero
		}

		if (modificaAnagrafica.getCommissioni() == null)
		{
			cs.setNull(24, Types.VARCHAR);  //Commissioni  non si setta
		}
		else
		{
			cs.setString(24, modificaAnagrafica.getCommissioni());  //Commissioni
		}

		if (modificaAnagrafica.getCodiceFiscaleOperatore() == null)
		{
			cs.setNull(25, Types.VARCHAR);  //Codice Fiscale dell'operatore non si setta
		}
		else
		{
			cs.setString(25, modificaAnagrafica.getCodiceFiscaleOperatore());  //Codice Fiscale dell'operatore
		}

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		mar = new ModificaAnagraficaResult();
		if (cs.getInt(27)==0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			mar.setCodiceRisposta(cs.getInt(27));

			if (cs.getString(28) == null || cs.getString(28) == "")
			{
				mar.setDescrizioneRisposta("Nessuna");
			}
			else
			{
				mar.setDescrizioneRisposta(cs.getString(28));
			}
		}
		else
		{
			mar.setIdAnagrafica(String.valueOf(cs.getInt(26)));
		}
		
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		if (cs != null) {
			cs.close();
		}
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */

		return mar;
	}

	public ModificaSedeResult modificaSede(ModificaSedeTypes modificaSede) throws SQLException 
	{
		ModificaSedeResult csr = null;

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call varia_sede(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(12, Types.NUMERIC); //id_anagrafica
		cs.registerOutParameter(13, Types.NUMERIC); //id_sede
		cs.registerOutParameter(14, Types.NUMERIC); //id_tipo_pagamento
		cs.registerOutParameter(15, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(16, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT
		cs.setString(1, modificaSede.getIdAnagrafica());  //Id Anagrafica
		cs.setString(2, modificaSede.getIdSede());  //Id Sede
		cs.setString(3, modificaSede.getIndirizzo());  //Indirizzo Sede
		cs.setString(4, modificaSede.getComune());  //Comune Sede
		cs.setString(5, modificaSede.getCAP());  //CAP Sede

		if (modificaSede.getTelefono() == null)
		{
			cs.setNull(6, Types.VARCHAR);  //Telefono Sede non si setta
		}
		else
		{
			cs.setString(6, modificaSede.getTelefono());  //Telefono Sede
		}

		if (modificaSede.getEMail() == null)
		{
			cs.setNull(7, Types.VARCHAR);  //E-Mail Sede non si setta
		}
		else
		{
			cs.setString(7, modificaSede.getEMail());  //E-Mail Sede
		}

		if (modificaSede.getFax() == null)
		{
			cs.setNull(8, Types.VARCHAR);  //Fax Sede non si setta
		}
		else
		{
			cs.setString(8, modificaSede.getFax());  //Fax Sede
		}

		if (modificaSede.getBollo() == null)
		{
			cs.setNull(9, Types.VARCHAR);  //Bollo Sede non si setta
		}
		else
		{
			cs.setString(9, modificaSede.getBollo());  //Bollo Sede
		}

		if (modificaSede.getTipoPagamento() == null)
		{
			cs.setNull(10, Types.VARCHAR);  //Tipo Pagamento Sede non si setta
		}
		else
		{
			cs.setString(10, modificaSede.getTipoPagamento());  //Tipo Pagamento Sede
		}

		if (modificaSede.getCodiceFiscaleOperatore() == null)
		{
			cs.setNull(11, Types.VARCHAR);  //Codice Fiscale dell'operatore non si setta
		}
		else
		{
			cs.setString(11, modificaSede.getCodiceFiscaleOperatore());  //Codice Fiscale dell'operatore
		}

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		csr = new ModificaSedeResult();
		if (cs.getInt(15)==0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			csr.setCodiceRisposta(cs.getInt(15));

			if (cs.getString(16) == null || cs.getString(16) == "")
			{
				csr.setDescrizioneRisposta("Nessuna");
			}
			else
			{
				csr.setDescrizioneRisposta(cs.getString(16));
			}

		}
		else
		{
			csr.setIdAnagrafica(String.valueOf(cs.getInt(12)));
			csr.setIdSede(String.valueOf(cs.getInt(13)));
			csr.setIdTipoPagamento(String.valueOf(cs.getInt(14)));

		}

		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		if (cs != null) {
			cs.close();
		}
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */

		return csr;
	}

	public ModificaContoBancarioResult modificaContoBancario(ModificaContoBancarioTypes modificaContoBancario) throws SQLException 
	{
		ModificaContoBancarioResult mcbr = null;


		if ((modificaContoBancario.getIdAnagrafica()==null) ||
				(modificaContoBancario.getIdSede()==null) ||
				(modificaContoBancario.getIdContoCorrente()==null) ||
				(modificaContoBancario.getModalitaPrincipale()==null) ||
				(modificaContoBancario.getIBAN()==null) ||
				(modificaContoBancario.getCodiceFiscaleOperatore()==null))
		{

			ModificaContoBancarioResult mcbr1 = new ModificaContoBancarioResult();
			mcbr1.setCodiceRisposta(0);
			mcbr1.setDescrizioneRisposta("Manca un dato bancario obbligatorio");			
			return mcbr1;
		}

		if(connection!=null&&connection.isClosed())
			connection=getConnection();

		CallableStatement cs;

		cs = connection.prepareCall("{call varia_conto(?,?,?,?,?,?,?,?,?,?,?,?,?)}");

		// Setto il tipo dei parametri di OUTPUT
		cs.registerOutParameter(9, Types.NUMERIC); //id_anagrafica
		cs.registerOutParameter(10, Types.NUMERIC); //id_sede
		cs.registerOutParameter(11, Types.NUMERIC); //id_conto_corrente
		cs.registerOutParameter(12, Types.NUMERIC); //codice_risposta
		cs.registerOutParameter(13, Types.VARCHAR); //descrizione_risposta

		// Setto il valore per i parametri di INPUT

		cs.setString(1, modificaContoBancario.getIdAnagrafica());  //Id Anagrafica
		cs.setString(2, modificaContoBancario.getIdSede());  //Id Sede
		cs.setString(3, modificaContoBancario.getIdContoCorrente());  //Id Conto Corrente

		if (modificaContoBancario.getModalitaPrincipale() == null)
		{
			cs.setNull(4, Types.VARCHAR);  //Modalit� Principale non si setta
		}
		else
		{
			cs.setString(4, modificaContoBancario.getModalitaPrincipale());  //Modalit� Principale
		}

		if (modificaContoBancario.getContoCorrente() == null)
		{
			cs.setNull(5, Types.VARCHAR);  //Conto Corrente non si setta
		}
		else
		{
			cs.setString(5, modificaContoBancario.getContoCorrente());  //Conto Corrente
		}

		if (modificaContoBancario.getCIN() == null)
		{
			cs.setNull(6, Types.VARCHAR);  //CIN non si setta
		}
		else
		{
			cs.setString(6, modificaContoBancario.getCIN());  //CIN
		}

		if (modificaContoBancario.getIBAN() == null)
		{
			cs.setNull(7, Types.VARCHAR);  //IBAN non si setta
		}
		else
		{
			cs.setString(7, modificaContoBancario.getIBAN());  //IBAN
		}

		if (modificaContoBancario.getCodiceFiscaleOperatore() == null)
		{
			cs.setNull(8, Types.VARCHAR);  //Codice Fiscale dell'operatore non si setta
		}
		else
		{
			cs.setString(8, modificaContoBancario.getCodiceFiscaleOperatore());  //Codice Fiscale dell'Operatore
		}

		// Execute the stored procedure and retrieve the IN/OUT value
		cs.execute();

		mcbr = new ModificaContoBancarioResult();
		if (cs.getInt(12)==0) //SE codiceRisposta contiene la segnalazione di un errore
		{
			mcbr.setCodiceRisposta(cs.getInt(12));

			if (cs.getString(13) == null || cs.getString(13) == "")
			{
				mcbr.setDescrizioneRisposta("Nessuna");
			}
			else
			{
				mcbr.setDescrizioneRisposta(cs.getString(13));
			}
		}
		else
		{
			mcbr.setIdAnagrafica(String.valueOf(cs.getInt(9)));
			mcbr.setIdSede(String.valueOf(cs.getInt(10)));
			mcbr.setIdContoCorrente(String.valueOf(cs.getInt(11)));
		}
		
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		if (cs != null) {
			cs.close();
		}
		/** simonebrunox 02/03/2017: risoluzione bug SQLException: ORA-01000: numero massimo di cursori aperti superato */
		
		return mcbr;
	}


}
