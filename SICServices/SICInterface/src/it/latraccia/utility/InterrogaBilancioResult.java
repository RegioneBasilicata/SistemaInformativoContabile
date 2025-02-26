/**
 * 
 */
package it.latraccia.utility;

import it.latraccia.entity.sic.risposta.ListaCOGType;

/**
 * @author vgioviale
 *
 */
public class InterrogaBilancioResult 
{
	
	int annoBilancio;
	String codiceCapitolo;
	String descrizioneCapitolo;
	String por;
	String perenti;
	String contoEconomica1;
	String contoEconomica2;
	String contoEconomica3;
	String contoEconomica4;
	String contoEconomica5;
	double competenza;
	double residuo;
	double cassa;
	double preImpegni;
	double impegni;
	double mandati;
	double dispCompetenza;
	double dispCassa;
	int codiceRisposta;
	String descrizioneRisposta;
	
	/** simonebrunox 07/02/2018: aggiungo missione_programma e verifica blocco */
	String missioneProgramma;
	int codiceRispostaBlocco;
	String descrizioneRispostaBlocco;
	/** simonebrunox 07/02/2018: aggiungo missione_programma e verifica blocco */
	
	// Modifica Leo: aggiunto il campo listaCOG con relativi getter e setter
	ListaCOGType listaCOG;
	/**
	 * @param annoBilancio
	 * @param codiceCapitolo
	 * @param descrizioneCapitolo
	 * @param por
	 * @param contoEconomica1
	 * @param contoEconomica2
	 * @param contoEconomica3
	 * @param contoEconomica4
	 * @param contoEconomica5
	 * @param competenza
	 * @param residuo
	 * @param cassa
	 * @param preImpegni
	 * @param impegni
	 * @param mandati
	 * @param dispCompetenza
	 * @param dispCassa
	 * @param listaCOG
	 * @param missioneProgramma
	 * @param codiceRispostaBlocco
	 * @param descrizioneRispostaBlocco 
	 */
	public InterrogaBilancioResult() 
	{
		super();
		this.annoBilancio = 0;
		this.codiceCapitolo = null;
		this.descrizioneCapitolo = null;
		this.por = null;
		this.perenti = null;
		this.contoEconomica1 = null;
		this.contoEconomica2 = null;
		this.contoEconomica3 = null;
		this.contoEconomica4 = null;
		this.contoEconomica5 = null;
		this.competenza = 0;
		this.residuo = 0;
		this.cassa = 0;
		this.preImpegni = 0;
		this.impegni = 0;
		this.mandati = 0;
		this.dispCompetenza = 0;
		this.dispCassa = 0;
		this.codiceRisposta = 0;
		this.descrizioneRisposta = null;
		//Modifica Leo
		this.listaCOG = null;
		
		/** simonebrunox 07/02/2018: aggiungo missione_programma e verifica blocco */
		this.missioneProgramma = null;
		this.codiceRispostaBlocco = 0;
		this.descrizioneRispostaBlocco = null;
		/** simonebrunox 07/02/2018: aggiungo missione_programma e verifica blocco */		
	}
	
	/**
	 * @return the listaCOG
	 */
	public ListaCOGType getListaCOG() {
		return listaCOG;
	}
	
	/**
	 * @param listaCOG the listaCOG to set
	 */
	public void setListaCOG(ListaCOGType listaCOG) {
		this.listaCOG = listaCOG;
	}
	
	/**
	 * @return the annoBilancio
	 */
	public int getAnnoBilancio() {
		return annoBilancio;
	}
	/**
	 * @param annoBilancio the annoBilancio to set
	 */
	public void setAnnoBilancio(int annoBilancio) {
		this.annoBilancio = annoBilancio;
	}
	/**
	 * @return the cassa
	 */
	public double getCassa() {
		return cassa;
	}
	/**
	 * @param cassa the cassa to set
	 */
	public void setCassa(double cassa) {
		this.cassa = cassa;
	}
	/**
	 * @return the codiceCapitolo
	 */
	public String getCodiceCapitolo() {
		return codiceCapitolo;
	}
	/**
	 * @param codiceCapitolo the codiceCapitolo to set
	 */
	public void setCodiceCapitolo(String codiceCapitolo) {
		this.codiceCapitolo = codiceCapitolo;
	}
	/**
	 * @return the competenza
	 */
	public double getCompetenza() {
		return competenza;
	}
	/**
	 * @param competenza the competenza to set
	 */
	public void setCompetenza(double competenza) {
		this.competenza = competenza;
	}
	/**
	 * @return the contoEconomica1
	 */
	public String getContoEconomica1() {
		return contoEconomica1;
	}
	/**
	 * @param contoEconomica1 the contoEconomica1 to set
	 */
	public void setContoEconomica1(String contoEconomica1) {
		this.contoEconomica1 = contoEconomica1;
	}
	/**
	 * @return the contoEconomica2
	 */
	public String getContoEconomica2() {
		return contoEconomica2;
	}
	/**
	 * @param contoEconomica2 the contoEconomica2 to set
	 */
	public void setContoEconomica2(String contoEconomica2) {
		this.contoEconomica2 = contoEconomica2;
	}
	/**
	 * @return the contoEconomica3
	 */
	public String getContoEconomica3() {
		return contoEconomica3;
	}
	/**
	 * @param contoEconomica3 the contoEconomica3 to set
	 */
	public void setContoEconomica3(String contoEconomica3) {
		this.contoEconomica3 = contoEconomica3;
	}
	/**
	 * @return the contoEconomica4
	 */
	public String getContoEconomica4() {
		return contoEconomica4;
	}
	/**
	 * @param contoEconomica4 the contoEconomica4 to set
	 */
	public void setContoEconomica4(String contoEconomica4) {
		this.contoEconomica4 = contoEconomica4;
	}
	/**
	 * @return the contoEconomica5
	 */
	public String getContoEconomica5() {
		return contoEconomica5;
	}
	/**
	 * @param contoEconomica5 the contoEconomica5 to set
	 */
	public void setContoEconomica5(String contoEconomica5) {
		this.contoEconomica5 = contoEconomica5;
	}
	/**
	 * @return the descrizioneCapitolo
	 */
	public String getDescrizioneCapitolo() {
		return descrizioneCapitolo;
	}
	/**
	 * @param descrizioneCapitolo the descrizioneCapitolo to set
	 */
	public void setDescrizioneCapitolo(String descrizioneCapitolo) {
		this.descrizioneCapitolo = descrizioneCapitolo;
	}
	/**
	 * @return the dispCassa
	 */
	public double getDispCassa() {
		return dispCassa;
	}
	/**
	 * @param dispCassa the dispCassa to set
	 */
	public void setDispCassa(double dispCassa) {
		this.dispCassa = dispCassa;
	}
	/**
	 * @return the dispCompetenza
	 */
	public double getDispCompetenza() {
		return dispCompetenza;
	}
	/**
	 * @param dispCompetenza the dispCompetenza to set
	 */
	public void setDispCompetenza(double dispCompetenza) {
		this.dispCompetenza = dispCompetenza;
	}
	/**
	 * @return the impegni
	 */
	public double getImpegni() {
		return impegni;
	}
	/**
	 * @param impegni the impegni to set
	 */
	public void setImpegni(double impegni) {
		this.impegni = impegni;
	}
	/**
	 * @return the mandati
	 */
	public double getMandati() {
		return mandati;
	}
	/**
	 * @param mandati the mandati to set
	 */
	public void setMandati(double mandati) {
		this.mandati = mandati;
	}
	/**
	 * @return the por
	 */
	public String getPor() {
		return por;
	}
	/**
	 * @param por the por to set
	 */
	public void setPor(String por) {
		this.por = por;
	}
	/**
	 * @return the preImpegni
	 */
	public double getPreImpegni() {
		return preImpegni;
	}
	/**
	 * @param preImpegni the preImpegni to set
	 */
	public void setPreImpegni(double preImpegni) {
		this.preImpegni = preImpegni;
	}
	/**
	 * @return the residuo
	 */
	public double getResiduo() {
		return residuo;
	}
	/**
	 * @param residuo the residuo to set
	 */
	public void setResiduo(double residuo) {
		this.residuo = residuo;
	}
	/**
	 * @return the codiceRisposta
	 */
	public int getCodiceRisposta() {
		return codiceRisposta;
	}
	/**
	 * @param codiceRisposta the codiceRisposta to set
	 */
	public void setCodiceRisposta(int codiceRisposta) {
		this.codiceRisposta = codiceRisposta;
	}
	/**
	 * @return the descrizioneRisposta
	 */
	public String getDescrizioneRisposta() {
		return descrizioneRisposta;
	}
	/**
	 * @param descrizioneRisposta the descrizioneRisposta to set
	 */
	public void setDescrizioneRisposta(String descrizioneRisposta) {
		this.descrizioneRisposta = descrizioneRisposta;
	}
	/**
	 * @return the perenti
	 */
	public String getPerenti() {
		return perenti;
	}
	/**
	 * @param perenti the perenti to set
	 */
	public void setPerenti(String perenti) {
		this.perenti = perenti;
	}

	public String getMissioneProgramma() {
		return missioneProgramma;
	}

	public void setMissioneProgramma(String missioneProgramma) {
		this.missioneProgramma = missioneProgramma;
	}

	public int getCodiceRispostaBlocco() {
		return codiceRispostaBlocco;
	}

	public void setCodiceRispostaBlocco(int codiceRispostaBlocco) {
		this.codiceRispostaBlocco = codiceRispostaBlocco;
	}

	public String getDescrizioneRispostaBlocco() {
		return descrizioneRispostaBlocco;
	}

	public void setDescrizioneRispostaBlocco(String descrizioneRispostaBlocco) {
		this.descrizioneRispostaBlocco = descrizioneRispostaBlocco;
	}
}
