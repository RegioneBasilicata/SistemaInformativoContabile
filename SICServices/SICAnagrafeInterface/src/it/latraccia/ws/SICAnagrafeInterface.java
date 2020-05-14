/**
 * Creato il 22 Giugno 2011
 * Modificato il 06 Ottobre 2011
 */
package it.latraccia.ws;

import java.math.BigInteger;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.swing.text.rtf.RTFEditorKit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import it.latraccia.utility.*;
import sun.font.CreatedFontTracker;
import it.latraccia.entity.anasic.richiesta.*;
import it.latraccia.entity.anasic.richiesta.AnagraficaTypes;
import it.latraccia.entity.anasic.richiesta.CaricamentoAnagraficheTypes.AnagraficaMassivaType.ElementoAnagraficaMassivaType;
import it.latraccia.entity.anasic.richiesta.DatiBancariTypes;
import it.latraccia.entity.anasic.richiesta.SedeTypes;
import it.latraccia.entity.anasic.risposta.*;
import it.latraccia.entity.anasic.risposta.MessaggioRispostaMassivaTypes.RispostaMassivaTypeType;
import it.latraccia.entity.anasic.risposta.impl.RispostaInterrogaAnagraficaContrattiTypesImpl;


/**
 *
 * Web Service che consente di interfacciarsi con l'anagrafica del SIC
 *
 * @author Vincenzo Gioviale
 *  
 */
public class SICAnagrafeInterface 
{
	/**
	 * Metodo per la richiesta all'anagrafica del SIC
	 *
	 * @return String - risposta xml
	 * @param  q  - stringa contenente lo xml della richiesta 
	 * 
	 */
	public String queryAnagrafica(String q) 
	{
		String res = null;
		MessaggioRichiesta anaRequest=null;
		DB db = new DB();

		try 
		{
			ByteArrayInputStream bAIS = new ByteArrayInputStream(q.getBytes("UTF-8"));

			it.latraccia.entity.anasic.richiesta.ObjectFactory objFactory = new it.latraccia.entity.anasic.richiesta.ObjectFactory();
			Unmarshaller unmarshaller = objFactory.createUnmarshaller();

			anaRequest = (MessaggioRichiesta) unmarshaller.unmarshal(bAIS);

			if (anaRequest.getIntestazione().getApplicazione().length()==0)
			{
				return esitoNegativo(new BigInteger("0"),"Nome applicazione non indicato","Anagrafe SIC","Regione Basilicata");
			}

			if (anaRequest.getIntestazione().getInfoMittDest().length()==0)
			{
				return esitoNegativo(new BigInteger("0"),"Informazioni del mittente e destinatario non fornite","Anagrafe SIC","Regione Basilicata");
			}
			if (anaRequest.getRichiesta().getInterrogaAnagrafica()!=null)
			{
				List iarl=null;
				if (anaRequest.getRichiesta().getInterrogaAnagrafica().getCodiceFiscale() != null)
				{
					iarl = db.getAnagrafica(anaRequest.getRichiesta().getInterrogaAnagrafica().getCodiceFiscale(),1);					
				}
				if (anaRequest.getRichiesta().getInterrogaAnagrafica().getDenominazione() != null)
				{
					iarl = db.getAnagrafica(anaRequest.getRichiesta().getInterrogaAnagrafica().getDenominazione(),2);					
				}
				if (anaRequest.getRichiesta().getInterrogaAnagrafica().getPartitaIva() != null)
				{
					iarl = db.getAnagrafica(anaRequest.getRichiesta().getInterrogaAnagrafica().getPartitaIva(),3);					
				}
				if (anaRequest.getRichiesta().getInterrogaAnagrafica().getIdAnagrafica() != null)
				{
					iarl = db.getAnagrafica(anaRequest.getRichiesta().getInterrogaAnagrafica().getIdAnagrafica(),4);					
				}

				if (anaRequest.getRichiesta().getInterrogaAnagrafica().getNumeroContratto()!= null)
				{
					iarl = db.getAnagrafica(anaRequest.getRichiesta().getInterrogaAnagrafica().getNumeroContratto(),5);					
				}
				ListIterator li = iarl.listIterator();
				if (li.hasNext()==false)
				{
					res = esitoNegativo(BigInteger.valueOf(1),"Nessun risultato restituito","Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
					it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
					MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
					it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
					it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

					iti.setApplicazione("Anagrafe SIC");
					iti.setInfoMittDest("Regione Basilicata");
					mr.setIntestazione(iti);
					it.latraccia.entity.anasic.risposta.RispostaInterrogaAnagraficaTypes riat = objFactory1.createRispostaInterrogaAnagraficaTypes();
					List as = riat.getAnagrafica();

					while (li.hasNext()) 
					{
						Anagrafica iar = (Anagrafica)li.next();    
						it.latraccia.entity.anasic.risposta.AnagraficaTypes aa = objFactory1.createAnagraficaTypes();
						aa.setCommissioni(iar.getCommissioni());
						aa.setAltriNomi(iar.getAltriNomi());
						aa.setCAPRESLR(iar.getCapResLR());
						aa.setCodiceFiscale(iar.getCodiceFiscale());
						aa.setCodiceFiscaleLR(iar.getCodiceFiscaleLR());
						aa.setCognome(iar.getCognome());
						aa.setCognomeLR(iar.getCognomeLR());
						aa.setComuneNS(iar.getComuneNS());
						aa.setComuneNSLR(iar.getComuneNSLR());
						aa.setComuneRESLR(iar.getComuneRESLR());

						if (iar.getDataNascita() != null)
						{
							Calendar cal = Calendar.getInstance();
							cal.setTime(iar.getDataNascita());
							aa.setDataNascita(cal);
						}
						else
						{
							aa.setDataNascita(null);
						}

						if (iar.getDataNascitaLR() != null)
						{
							Calendar cal1 = Calendar.getInstance();
							cal1.setTime(iar.getDataNascitaLR());
							aa.setDataNascitaLR(cal1);
						}
						else
						{
							aa.setDataNascitaLR(null);
						}

						aa.setDenominazione(iar.getDenominazione());
						aa.setEstero(iar.getEstero());
						aa.setIdAnagrafica(iar.getIdAnagrafica());
						aa.setIdComuneNS(iar.getIdComuneNS());
						aa.setIdComuneNSLR(iar.getComuneNSLR());
						aa.setIdComuneRESLR(iar.getIdComuneRESLR());
						aa.setIndirizzoLR(iar.getIndirizzoLR());
						aa.setNome(iar.getNome());
						aa.setNomeLR(iar.getNomeLR());
						aa.setNotaPignoramento(iar.getNotaPignoramento());
						aa.setPartitaIva(iar.getPartitaIva());
						aa.setPignoramento(iar.getPignoramento());
						aa.setSesso(iar.getSesso());
						aa.setSessoLR(iar.getSessoLR());
						aa.setTipoAnagrafica(iar.getTipoAnagrafica());

						aa.setListaSedi(iar.getListaSedi());

						as.add(aa);
					}
					st.setRispostaInterrogaAnagrafica(riat);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}
			
			

			if (anaRequest.getRichiesta().getInterrogaAnagraficaContratti()!=null)
			{
				List iacrl=null;
				it.latraccia.entity.anasic.richiesta.InterrogaAnagraficaContrattiTypes listaChiavi=anaRequest.getRichiesta().getInterrogaAnagraficaContratti();

				iacrl = db.getAnagraficaContratti(listaChiavi);					

				ListIterator li = iacrl.listIterator();
				if (li.hasNext()==false)
				{
					res = esitoNegativo(BigInteger.valueOf(1),"Nessun risultato restituito","Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
					it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
					MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
					it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
					it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

					iti.setApplicazione("Anagrafe SIC");
					iti.setInfoMittDest("Regione Basilicata");
					mr.setIntestazione(iti);

					//lista risposta
					it.latraccia.entity.anasic.risposta.RispostaInterrogaAnagraficaContrattiTypes riact = objFactory1.createRispostaInterrogaAnagraficaContrattiTypes();


					while (li.hasNext()) 
					{


						AnagraficaContratto iacr = (AnagraficaContratto)li.next();    

						it.latraccia.entity.anasic.risposta.RispostaInterrogaAnagraficaContrattoTypes ac=objFactory1.createRispostaInterrogaAnagraficaContrattoTypes();
						//imposta chiave contratto
						ac.setChiave(iacr.getChiave());

						//imposta anagrafiche associate al contratto
						List <Anagrafica> lia = iacr.getListaAnagrafica();
						Iterator itlia=lia.iterator();

						while(itlia.hasNext()){

							it.latraccia.entity.anasic.risposta.AnagraficaTypes aa = objFactory1.createAnagraficaTypes();
							Anagrafica iar=(Anagrafica)itlia.next();

							aa.setCommissioni(iar.getCommissioni());
							aa.setAltriNomi(iar.getAltriNomi());
							aa.setCAPRESLR(iar.getCapResLR());
							aa.setCodiceFiscale(iar.getCodiceFiscale());
							aa.setCodiceFiscaleLR(iar.getCodiceFiscaleLR());
							aa.setCognome(iar.getCognome());
							aa.setCognomeLR(iar.getCognomeLR());
							aa.setComuneNS(iar.getComuneNS());
							aa.setComuneNSLR(iar.getComuneNSLR());
							aa.setComuneRESLR(iar.getComuneRESLR());

							if (iar.getDataNascita() != null)
							{
								Calendar cal = Calendar.getInstance();
								cal.setTime(iar.getDataNascita());
								aa.setDataNascita(cal);
							}
							else
							{
								aa.setDataNascita(null);
							}

							if (iar.getDataNascitaLR() != null)
							{
								Calendar cal1 = Calendar.getInstance();
								cal1.setTime(iar.getDataNascitaLR());
								aa.setDataNascitaLR(cal1);
							}
							else
							{
								aa.setDataNascitaLR(null);
							}

							aa.setDenominazione(iar.getDenominazione());
							aa.setEstero(iar.getEstero());
							aa.setIdAnagrafica(iar.getIdAnagrafica());
							aa.setIdComuneNS(iar.getIdComuneNS());
							aa.setIdComuneNSLR(iar.getComuneNSLR());
							aa.setIdComuneRESLR(iar.getIdComuneRESLR());
							aa.setIndirizzoLR(iar.getIndirizzoLR());
							aa.setNome(iar.getNome());
							aa.setNomeLR(iar.getNomeLR());
							aa.setNotaPignoramento(iar.getNotaPignoramento());
							aa.setPartitaIva(iar.getPartitaIva());
							aa.setPignoramento(iar.getPignoramento());
							aa.setSesso(iar.getSesso());
							aa.setSessoLR(iar.getSessoLR());
							aa.setTipoAnagrafica(iar.getTipoAnagrafica());

							aa.setListaSedi(iar.getListaSedi());


							ac.getAnagrafica().add(aa);
						}

						riact.getContratto().add(ac);
					}

					st.setRispostaInterrogaAnagraficaContratti(riact);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}

			if (anaRequest.getRichiesta().getVerificaAnagrafica()!=null)
			{
				Anagrafica var = db.verificaAnagrafe(anaRequest.getRichiesta().getVerificaAnagrafica().getAnagrafica());

				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);
				it.latraccia.entity.anasic.risposta.RispostaVerificaAnagraficaTypes rvat = objFactory1.createRispostaVerificaAnagraficaTypes();

				it.latraccia.entity.anasic.risposta.AnagraficaTypes aa = objFactory1.createAnagraficaTypes();

				if (var == null)
				{
					res = esitoNegativo(BigInteger.valueOf(1),"Nessun risultato restituito","Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					aa.setAltriNomi(var.getAltriNomi());
					aa.setCAPRESLR(var.getCapResLR());
					aa.setCodiceFiscale(var.getCodiceFiscale());
					aa.setCodiceFiscaleLR(var.getCodiceFiscaleLR());
					aa.setCognome(var.getCognome());
					aa.setCognomeLR(var.getCognomeLR());
					aa.setComuneNS(var.getComuneNS());
					aa.setComuneNSLR(var.getComuneNSLR());
					aa.setComuneRESLR(var.getComuneRESLR());
					aa.setCommissioni(var.getCommissioni());

					if (var.getDataNascita() != null)
					{
						Calendar cal = Calendar.getInstance();
						cal.setTime(var.getDataNascita());
						aa.setDataNascita(cal);
					}
					else
					{
						aa.setDataNascita(null);
					}

					if (var.getDataNascita() != null)
					{
						Calendar cal1 = Calendar.getInstance();
						cal1.setTime(var.getDataNascitaLR());
						aa.setDataNascitaLR(cal1);
					}
					else
					{
						aa.setDataNascitaLR(null);
					}
					aa.setDenominazione(aa.getDenominazione());
					aa.setEstero(var.getEstero());
					aa.setIdAnagrafica(var.getIdAnagrafica());
					aa.setIdComuneNS(var.getIdComuneNS());
					aa.setIdComuneNSLR(var.getComuneNSLR());
					aa.setIdComuneRESLR(var.getIdComuneRESLR());
					aa.setIndirizzoLR(var.getIndirizzoLR());
					aa.setNome(var.getNome());
					aa.setNomeLR(var.getNomeLR());
					aa.setNotaPignoramento(var.getNotaPignoramento());
					aa.setPartitaIva(var.getPartitaIva());
					aa.setPignoramento(var.getPignoramento());
					aa.setSesso(var.getSesso());
					aa.setSessoLR(var.getSessoLR());

					it.latraccia.entity.anasic.risposta.SedeTypes st1 = objFactory1.createSedeTypes();


					aa.setListaSedi(var.getListaSedi());

					rvat.setAnagrafica(aa);

					st.setRispostaVerificaAnagrafica(rvat);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}
			if (anaRequest.getRichiesta().getCreaAnagrafica()!=null)
			{
				CreaAnagrafeResult var = db.creaAnagrafe(anaRequest.getRichiesta().getCreaAnagrafica().getAnagrafica(),anaRequest.getRichiesta().getCreaAnagrafica().getCodiceFiscaleOperatore());

				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);

				it.latraccia.entity.anasic.risposta.RispostaCreaAnagraficaTypes rcat = objFactory1.createRispostaCreaAnagraficaTypes();

				if (var.getCodiceRisposta()==0)//SE c'è stato qualche problema restituisco l'eccezione
				{
					res = esitoNegativo(new BigInteger(String.valueOf(var.getCodiceRisposta())),var.getDescrizioneRisposta(),"Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					rcat.setIdAnagrafica(var.getIdAnagrafica());
					rcat.setIdContoCorrente(var.getIdContoCorrente());
					rcat.setIdSede(var.getIdSede());
					rcat.setIdTipoPagamento(var.getIdTipoPagamento());

					st.setRispostaCreaAnagrafica(rcat);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}

			if (anaRequest.getRichiesta().getCreaSede()!=null)
			{
				CreaSedeResult var = db.creaSede(anaRequest.getRichiesta().getCreaSede());

				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);

				it.latraccia.entity.anasic.risposta.RispostaCreaSedeTypes rcst = objFactory1.createRispostaCreaSedeTypes();

				if (var.getCodiceRisposta()==0)//SE c'è stato qualche problema restituisco l'eccezione
				{
					res = esitoNegativo(new BigInteger(String.valueOf(var.getCodiceRisposta())),var.getDescrizioneRisposta(),"Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					rcst.setIdAnagrafica(var.getIdAnagrafica());
					rcst.setIdContoCorrente(var.getIdContoCorrente());
					rcst.setIdSede(var.getIdSede());
					rcst.setIdTipoPagamento(var.getIdTipoPagamento());

					st.setRispostaCreaSede(rcst);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}

			if (anaRequest.getRichiesta().getCreaContoBancario()!=null)
			{
				CreaContoBancarioResult var = db.creaContoBancario(anaRequest.getRichiesta().getCreaContoBancario());

				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);

				it.latraccia.entity.anasic.risposta.RispostaCreaContoBancarioTypes rccbt = objFactory1.createRispostaCreaContoBancarioTypes();

				if (var.getCodiceRisposta()==0)//SE c'è stato qualche problema restituisco l'eccezione
				{
					res = esitoNegativo(new BigInteger(String.valueOf(var.getCodiceRisposta())),var.getDescrizioneRisposta(),"Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					rccbt.setIdAnagrafica(var.getIdAnagrafica());
					rccbt.setIdContoCorrente(var.getIdContoCorrente());
					rccbt.setIdSede(var.getIdSede());
					rccbt.setIdTipoPagamento(var.getIdTipoPagamento());

					st.setRispostaCreaContoBancario(rccbt);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}

			if (anaRequest.getRichiesta().getInterrogaComuni()!=null)
			{
				List icrl=null;
				if (anaRequest.getRichiesta().getInterrogaComuni().getNomeComune() != null)
				{
					icrl = db.getComune(anaRequest.getRichiesta().getInterrogaComuni().getNomeComune());					
				}
				ListIterator li = icrl.listIterator();
				if (li.hasNext()==false)
				{
					res = esitoNegativo(BigInteger.valueOf(1),"Nessun risultato restituito","Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
					it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
					MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
					it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
					it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

					iti.setApplicazione("Anagrafe SIC");
					iti.setInfoMittDest("Regione Basilicata");
					mr.setIntestazione(iti);
					it.latraccia.entity.anasic.risposta.RispostaInterrogaComuniTypes rict = objFactory1.createRispostaInterrogaComuniTypes();
					List ac = rict.getComune();

					while (li.hasNext()) 
					{
						Comune iar = (Comune)li.next();    
						it.latraccia.entity.anasic.risposta.ComuneTypes aa = objFactory1.createComuneTypes();
						aa.setCAP(iar.getCAP());
						aa.setCodiceBelFiore(iar.getCodiceBelFiore());
						aa.setCodiceIstat(iar.getCodiceIstat());
						aa.setCodProvincia(iar.getCodiceProvincia());
						aa.setCodRegione(iar.getCodiceRegione());
						aa.setCodRipartizioneGeografica(iar.getCodRipartizioneGeo());
						aa.setDescRipartizioneGeografica(iar.getDescRipartizioneGeo());
						aa.setDescrizione(iar.getDescrizione());
						aa.setIdComune(iar.getIdComune());
						aa.setNazione(iar.getNazione());
						aa.setProvincia(iar.getProvincia());
						aa.setRegione(iar.getRegione());

						ac.add(aa);
					}
					st.setRispostaInterrogaComuni(rict);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}

			if (anaRequest.getRichiesta().getModificaAnagrafica()!=null)
			{
				ModificaAnagraficaResult var = db.modificaAnagrafica(anaRequest.getRichiesta().getModificaAnagrafica());

				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);

				it.latraccia.entity.anasic.risposta.RispostaModificaAnagraficaTypes rmat = objFactory1.createRispostaModificaAnagraficaTypes();

				if (var.getCodiceRisposta()==0)//SE c'è stato qualche problema restituisco l'eccezione
				{
					res = esitoNegativo(new BigInteger(String.valueOf(var.getCodiceRisposta())),var.getDescrizioneRisposta(),"Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					rmat.setIdAnagrafica(var.getIdAnagrafica());

					st.setRispostaModificaAnagrafica(rmat);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}

			if (anaRequest.getRichiesta().getModificaSede()!=null)
			{
				ModificaSedeResult var = db.modificaSede(anaRequest.getRichiesta().getModificaSede());

				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);

				it.latraccia.entity.anasic.risposta.RispostaModificaSedeTypes rmst = objFactory1.createRispostaModificaSedeTypes();

				if (var.getCodiceRisposta()==0)//SE c'è stato qualche problema restituisco l'eccezione
				{
					res = esitoNegativo(new BigInteger(String.valueOf(var.getCodiceRisposta())),var.getDescrizioneRisposta(),"Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					rmst.setIdAnagrafica(var.getIdAnagrafica());
					rmst.setIdSede(var.getIdSede());
					rmst.setIdTipoPagamento(var.getIdTipoPagamento());

					st.setRispostaModificaSede(rmst);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}

			if (anaRequest.getRichiesta().getModificaContoBancario() != null)
			{
				ModificaContoBancarioResult var = db.modificaContoBancario(anaRequest.getRichiesta().getModificaContoBancario());

				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.SuccessoTypes st = objFactory1.createSuccessoTypes();

				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);

				it.latraccia.entity.anasic.risposta.RispostaModificaContoBancarioTypes rmcbt = objFactory1.createRispostaModificaContoBancarioTypes();

				if (var.getCodiceRisposta()==0)//SE c'è stato qualche problema restituisco l'eccezione
				{
					res = esitoNegativo(new BigInteger(String.valueOf(var.getCodiceRisposta())),var.getDescrizioneRisposta(),"Anagrafe SIC","Regione Basilicata");
				}
				else
				{
					rmcbt.setIdAnagrafica(var.getIdAnagrafica());
					rmcbt.setIdSede(var.getIdSede());
					rmcbt.setIdContoCorrente(var.getIdContoCorrente());

					st.setRispostaModificaContoBancario(rmcbt);
					mr.setSuccesso(st);

					Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshal(mr, baos);
					res = baos.toString("UTF-8");
				}
			}
			
			/** simonebrunox 27/12/2017: caricamento massivo beneficiari */
			if (anaRequest.getRichiesta().getCaricamentoAnagrafiche() != null) {				
				JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
				it.latraccia.entity.anasic.risposta.ObjectFactory objFactory1 = new it.latraccia.entity.anasic.risposta.ObjectFactory();
				MessaggioRisposta mr = objFactory1.createMessaggioRisposta();
				it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory1.createIntestazioneTypes();
				it.latraccia.entity.anasic.risposta.MessaggioRispostaMassivaTypes mrmt = objFactory1.createMessaggioRispostaMassivaTypes();
				
				iti.setApplicazione("Anagrafe SIC");
				iti.setInfoMittDest("Regione Basilicata");
				mr.setIntestazione(iti);
				List<RispostaMassivaTypeType> listaRisposta = mrmt.getRispostaMassivaType();
				
				List<ElementoAnagraficaMassivaType> listaAnagrafiche=anaRequest.getRichiesta().getCaricamentoAnagrafiche().getAnagraficaMassiva().getElementoAnagraficaMassiva();
				// per ogni anagrafica inviata...
				for (ElementoAnagraficaMassivaType elementoAnagraficaMassiva : listaAnagrafiche) {
					it.latraccia.entity.anasic.risposta.MessaggioRispostaMassivaTypes.RispostaMassivaTypeType rmt = objFactory1.createMessaggioRispostaMassivaTypesRispostaMassivaTypeType();
					it.latraccia.entity.anasic.risposta.RispostaProcessaAnagraficaTypes rpat = objFactory1.createRispostaProcessaAnagraficaTypes();
					
					AnagraficaTypes anagrafica = elementoAnagraficaMassiva.getAnagrafica();
					String iban = anagrafica.getDatiBancari().getIBAN();
					if (iban != null)
						anagrafica.getDatiBancari().setIBAN(iban.replace(" ", ""));
					
					// controlla se esiste
					List anagraficheEsistenti = new ArrayList();
					if (anagrafica.getPartitaIva() != null) {
						anagraficheEsistenti = db.getAnagrafica(anagrafica.getPartitaIva(),3);
					}
					if (anagraficheEsistenti.size() == 0 && anagrafica.getCodiceFiscale() != null) {
						anagraficheEsistenti = db.getAnagrafica(anagrafica.getCodiceFiscale(),1);					
					}										
					
					// se esiste ...
					if (anagraficheEsistenti.size()>0) {
						Anagrafica anagraficaEsistente = (Anagrafica) anagraficheEsistenti.get(0);
						String operazioneAccessoria = "";
						
						// controlla sedi
						SedeTypes sede = anagrafica.getSede();
						DatiBancariTypes datiBancari = anagrafica.getDatiBancari();
						// verifica esistenza sede			
						String idSede = db.getSede(anagraficaEsistente.getIdAnagrafica(), sede.getNomeSede());
						String idTipoPagamento = "";
						if (idSede == null) {
							// se non esiste, creala
							CreaSedeTypes cst = objFactory.createCreaSedeTypes();
							cst.setIdAnagrafica(anagraficaEsistente.getIdAnagrafica());
							cst.setCodiceFiscaleOperatore(anaRequest.getRichiesta().getCaricamentoAnagrafiche().getCodiceFiscaleOperatore());
							SedeTypes st = objFactory.createSedeTypes();
							cst.setSede(sede);
							cst.setDatiBancari(datiBancari);
							CreaSedeResult creaSedeResult = db.creaSede(cst);		
							if (creaSedeResult.getCodiceRisposta()==0) {
								//SE c'è stato qualche problema restituisco l'eccezione
								it.latraccia.entity.anasic.risposta.EccezioneTypes et = objFactory1.createEccezioneTypes();
								et.setCodice(BigInteger.valueOf(creaSedeResult.getCodiceRisposta()));
								et.setDescrizione(creaSedeResult.getDescrizioneRisposta());
								rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
								rmt.setEccezione(et);
								listaRisposta.add(rmt);		
								continue; // in seguito all'eccezione salta alla prossima anagrafica
							} else {
								idSede = creaSedeResult.getIdSede();
								idTipoPagamento = creaSedeResult.getIdTipoPagamento();
								operazioneAccessoria  += " - Sede creata";
							}
						} else {
							// altrimenti aggiornala
							ModificaSedeTypes mst = objFactory.createModificaSedeTypes();
							mst.setIdAnagrafica(anagraficaEsistente.getIdAnagrafica());
							mst.setIdSede(idSede);
							mst.setNomeSede(sede.getNomeSede());
							mst.setCodiceFiscaleOperatore(anaRequest.getRichiesta().getCaricamentoAnagrafiche().getCodiceFiscaleOperatore());
							mst.setBollo(sede.getBollo());
							mst.setCAP(sede.getCAP());
							mst.setComune(sede.getComune());
							mst.setEMail(sede.getEMail());
							mst.setFax(sede.getFax());
							mst.setIndirizzo(sede.getIndirizzo());
							mst.setTelefono(sede.getTelefono());
							mst.setTipoPagamento(sede.getTipoPagamento());
							ModificaSedeResult modificaSedeResult = db.modificaSede(mst);
							if (modificaSedeResult.getCodiceRisposta()==0) {
								//SE c'è stato qualche problema restituisco l'eccezione
								it.latraccia.entity.anasic.risposta.EccezioneTypes et = objFactory1.createEccezioneTypes();
								et.setCodice(BigInteger.valueOf(modificaSedeResult.getCodiceRisposta()));
								et.setDescrizione(modificaSedeResult.getDescrizioneRisposta());
								rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
								rmt.setEccezione(et);
								listaRisposta.add(rmt);		
								continue; // in seguito all'eccezione salta alla prossima anagrafica
							} else {							
								idTipoPagamento = modificaSedeResult.getIdTipoPagamento();								
								operazioneAccessoria  += " - Sede modificata";
							}
						}
						
						// controlla dati bancari
						// verifica esistenza conto solo se è indicato l'iban
						String idConto = "0";
						if (datiBancari.getIBAN() != null && datiBancari.getIBAN().compareTo("")!=0) {						
							idConto = db.getContoBancario(datiBancari.getIBAN());						
							if (idConto == null) {
								// se non esiste, crealo
								CreaContoBancarioTypes ccbt = objFactory.createCreaContoBancarioTypes();
								ccbt.setIdAnagrafica(anagraficaEsistente.getIdAnagrafica());
								ccbt.setCodiceFiscaleOperatore(anaRequest.getRichiesta().getCaricamentoAnagrafiche().getCodiceFiscaleOperatore());
								ccbt.setDatiBancari(datiBancari);
								ccbt.setIdSede(idSede);
								CreaContoBancarioResult creaContoBancarioResult = db.creaContoBancario(ccbt);
								if (creaContoBancarioResult.getCodiceRisposta()==0) {
									//SE c'è stato qualche problema restituisco l'eccezione
									it.latraccia.entity.anasic.risposta.EccezioneTypes et = objFactory1.createEccezioneTypes();
									et.setCodice(BigInteger.valueOf(creaContoBancarioResult.getCodiceRisposta()));
									et.setDescrizione(creaContoBancarioResult.getDescrizioneRisposta());
									rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
									rmt.setEccezione(et);
									listaRisposta.add(rmt);		
									continue; // in seguito all'eccezione salta alla prossima anagrafica
								} else {			
									idConto = creaContoBancarioResult.getIdContoCorrente();
									operazioneAccessoria  += " - Conto Bancario creato";
								}
							} else {						
								// altrimenti aggiornalo
								ModificaContoBancarioTypes mcbt = objFactory.createModificaContoBancarioTypes();
								mcbt.setIdAnagrafica(anagraficaEsistente.getIdAnagrafica());
								mcbt.setIdSede(idSede);
								mcbt.setIdContoCorrente(idConto);
								mcbt.setCodiceFiscaleOperatore(anaRequest.getRichiesta().getCaricamentoAnagrafiche().getCodiceFiscaleOperatore());
								mcbt.setCIN(datiBancari.getCIN());
								mcbt.setContoCorrente(datiBancari.getContoCorrente());
								mcbt.setIBAN(datiBancari.getIBAN());
								mcbt.setModalitaPrincipale(datiBancari.getModalitaPrincipale());	
								ModificaContoBancarioResult modificaContoBancarioResult = db.modificaContoBancario(mcbt);
								if (modificaContoBancarioResult.getCodiceRisposta()==0) {
									//SE c'è stato qualche problema restituisco l'eccezione
									it.latraccia.entity.anasic.risposta.EccezioneTypes et = objFactory1.createEccezioneTypes();
									et.setCodice(BigInteger.valueOf(modificaContoBancarioResult.getCodiceRisposta()));
									et.setDescrizione(modificaContoBancarioResult.getDescrizioneRisposta());
									rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
									rmt.setEccezione(et);
									listaRisposta.add(rmt);		
									continue; // in seguito all'eccezione salta alla prossima anagrafica
								} else {			
									idConto = modificaContoBancarioResult.getIdContoCorrente();
									operazioneAccessoria  += " - Conto Bancario modificato";
								}
							}
						}
						
						// verifica anagrafica
						Anagrafica anagraficaVerificata = db.verificaAnagrafe(anagrafica);
						// se la verifica fallisce...
						if (anagraficaVerificata == null) {	
							// aggiorna i dati			
							it.latraccia.entity.anasic.richiesta.ModificaAnagraficaTypes modificaAnagraficaTypes = objFactory.createModificaAnagraficaTypes();
							modificaAnagraficaTypes.setAltriNomi(anagrafica.getAltriNomi());
							modificaAnagraficaTypes.setCAPRESLR(anagrafica.getCAPRESLR());
							modificaAnagraficaTypes.setCodiceFiscale(anagrafica.getCodiceFiscale());
							modificaAnagraficaTypes.setCodiceFiscaleLR(anagrafica.getCodiceFiscaleLR());
							modificaAnagraficaTypes.setCodiceFiscaleOperatore(anaRequest.getRichiesta().getCaricamentoAnagrafiche().getCodiceFiscaleOperatore());
							modificaAnagraficaTypes.setCognome(anagrafica.getCognome());
							modificaAnagraficaTypes.setCognomeLR(anagrafica.getCognomeLR());
							modificaAnagraficaTypes.setCommissioni(anagrafica.getCommissioni());
							modificaAnagraficaTypes.setComuneNS(anagrafica.getComuneNS());
							modificaAnagraficaTypes.setComuneNSLR(anagrafica.getComuneNSLR());
							modificaAnagraficaTypes.setComuneRESLR(anagrafica.getComuneRESLR());
							modificaAnagraficaTypes.setDataNascita(anagrafica.getDataNascita());
							modificaAnagraficaTypes.setDataNascitaLR(anagrafica.getDataNascitaLR());
							modificaAnagraficaTypes.setDenominazione(anagrafica.getDenominazione());
							modificaAnagraficaTypes.setEstero(anagrafica.getEstero());
							modificaAnagraficaTypes.setIdAnagrafica(anagraficaEsistente.getIdAnagrafica());
							modificaAnagraficaTypes.setIndirizzoLR(anagrafica.getIndirizzoLR());
							modificaAnagraficaTypes.setNome(anagrafica.getNome());
							modificaAnagraficaTypes.setNomeLR(anagrafica.getNomeLR());
							modificaAnagraficaTypes.setNotaPignoramento(anagrafica.getNotaPignoramento());
							modificaAnagraficaTypes.setPartitaIva(anagrafica.getPartitaIva());
							modificaAnagraficaTypes.setPignoramento(anagrafica.getPignoramento());
							modificaAnagraficaTypes.setSesso(anagrafica.getSesso());
							modificaAnagraficaTypes.setSessoLR(anagrafica.getSessoLR());
							modificaAnagraficaTypes.setTipoAnagrafica(anagrafica.getTipoAnagrafica());
							ModificaAnagraficaResult modificaAnagraficaResult = db.modificaAnagrafica(modificaAnagraficaTypes);
							if (modificaAnagraficaResult.getCodiceRisposta()==0) { //SE c'è stato qualche problema restituisco l'eccezione
								it.latraccia.entity.anasic.risposta.EccezioneTypes et = objFactory1.createEccezioneTypes();
								et.setCodice(BigInteger.valueOf(modificaAnagraficaResult.getCodiceRisposta()));
								et.setDescrizione(modificaAnagraficaResult.getDescrizioneRisposta());
								rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
								rmt.setEccezione(et);
								listaRisposta.add(rmt);							
							} else {
								rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
								rpat.setOperazione("Modifica" + operazioneAccessoria);
								rpat.setIdAnagrafica(modificaAnagraficaResult.getIdAnagrafica());	
								rpat.setIdContoCorrente(idConto);									
								rpat.setIdSede(idSede);
								rpat.setIdTipoPagamento(idTipoPagamento);															
								rmt.setRispostaProcessaAnagrafica(rpat);
								listaRisposta.add(rmt);	
							}
						} else {
							// restituisci anagrafica confermata
							rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
							rpat.setOperazione("Verifica" + operazioneAccessoria);
							rpat.setIdAnagrafica(anagraficaVerificata.getIdAnagrafica());							
							rpat.setIdContoCorrente(idConto);
							rpat.setIdSede(idSede);
							rpat.setIdTipoPagamento(idTipoPagamento);							
							rmt.setRispostaProcessaAnagrafica(rpat);
							listaRisposta.add(rmt);								
						}
					} else {
						// se non esiste, crea nuova anagrafica
						CreaAnagrafeResult creaAnagrafeResult = db.creaAnagrafe(anagrafica, anaRequest.getRichiesta().getCaricamentoAnagrafiche().getCodiceFiscaleOperatore());
						if (creaAnagrafeResult.getCodiceRisposta()==0) { //SE c'è stato qualche problema restituisco l'eccezione
							it.latraccia.entity.anasic.risposta.EccezioneTypes et = objFactory1.createEccezioneTypes();
							et.setCodice(BigInteger.valueOf(creaAnagrafeResult.getCodiceRisposta()));
							et.setDescrizione(creaAnagrafeResult.getDescrizioneRisposta());
							rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
							rmt.setEccezione(et);
							listaRisposta.add(rmt);							
						} else {
							rmt.setIdAnagraficaMittente(elementoAnagraficaMassiva.getIdAnagraficaMittente());
							rpat.setOperazione("Inserimento");
							rpat.setIdAnagrafica(creaAnagrafeResult.getIdAnagrafica());
							rpat.setIdContoCorrente(creaAnagrafeResult.getIdContoCorrente());
							rpat.setIdSede(creaAnagrafeResult.getIdSede());
							rpat.setIdTipoPagamento(creaAnagrafeResult.getIdTipoPagamento());							
							rmt.setRispostaProcessaAnagrafica(rpat);
							listaRisposta.add(rmt);	
						}						
					}
				}
				mr.setMessaggioRispostaMassiva(mrmt);
				
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				marshaller.marshal(mr, baos);
				res = baos.toString("UTF-8");
				
			}
			/** simonebrunox 27/12/2017: caricamento massivo beneficiari */


		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
			return esitoNegativo(new BigInteger("0"),"XML non valido","Anagrafe SIC","Regione Basilicata");
		}
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			return esitoNegativo(new BigInteger("0"),"UnsupportedEncodingException: "+e.getMessage(),"Anagrafe SIC","Regione Basilicata");
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			return esitoNegativo(new BigInteger("0"),"SQLException: "+e.getMessage(),"Anagrafe SIC","Regione Basilicata");
		}
		finally
		{
			db.closeDB();
		}
		return res;
	}

	/**
	 * Metodo privato per la preparazione di esiti negativi
	 *
	 * @return String - risposta xml
	 * 
	 */
	private String esitoNegativo(BigInteger codiceEsito, String descrizioneEsito, String applicazione, String infomittdest) 
	{
		String ack="";
		try 
		{
			JAXBContext jaxbContext=JAXBContext.newInstance("it.latraccia.entity.anasic.risposta");
			it.latraccia.entity.anasic.risposta.ObjectFactory objFactory = new it.latraccia.entity.anasic.risposta.ObjectFactory();

			MessaggioRisposta mr = objFactory.createMessaggioRisposta();
			it.latraccia.entity.anasic.risposta.IntestazioneTypes iti = objFactory.createIntestazioneTypes();
			it.latraccia.entity.anasic.risposta.EccezioneTypes et = objFactory.createEccezioneTypes();

			iti.setApplicazione(applicazione);
			iti.setInfoMittDest(infomittdest);
			mr.setIntestazione(iti);
			et.setCodice(codiceEsito);

			if (descrizioneEsito == null || descrizioneEsito == "")
			{
				et.setDescrizione("Nessuna");
				et.setException("Nessuna");
			}
			else
			{
				et.setDescrizione(descrizioneEsito);
				et.setException(descrizioneEsito);
			}

			mr.setEccezione(et);

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(mr, baos);

			ack = baos.toString("UTF-8");
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
			ack = "<?xml version=\"1.0\"?> <Messaggio_Risposta>  <Intestazione> <InfoMittDest>Regione Basilicata</InfoMittDest> <Applicazione>Anagrafe SIC</Applicazione> </Intestazione> <Eccezione> <Codice>0</Codice>  <Descrizione>JAXBException</Descrizione> <Exception>"+e.getMessage()+"</Exception> </Eccezione> </Messaggio_Risposta>";
			return ack;
		}
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			ack = "<?xml version=\"1.0\"?> <Messaggio_Risposta>  <Intestazione> <InfoMittDest>Regione Basilicata</InfoMittDest> <Applicazione>Anagrafe SIC</Applicazione> </Intestazione> <Eccezione> <Codice>0</Codice>  <Descrizione>UnsupportedEncodingException</Descrizione> <Exception>"+e.getMessage()+"</Exception> </Eccezione> </Messaggio_Risposta>";
			return ack;
		}
		return ack;
	}
}