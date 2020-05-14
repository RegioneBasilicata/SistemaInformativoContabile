package it.latraccia.utility;

import java.util.Date;

public class InterrogaPreimpegniApertiResult {
	private Date dataPreimpegno;
	private String numeroPreimpegno;
	private double importoIniziale;
	private double importoDisponibile;
	private String oggettoPreimpegno;
	private String tipoAtto;
	private Date dataAtto;
	private String numeroAtto;
	private String codicePCF;
	private String descrizionePCF;
	
	public Date getDataPreimpegno() {
		return dataPreimpegno;
	}
	public void setDataPreimpegno(Date dataPreimpegno) {
		this.dataPreimpegno = dataPreimpegno;
	}
	public String getNumeroPreimpegno() {
		return numeroPreimpegno;
	}
	public void setNumeroPreimpegno(String numeroPreimpegno) {
		this.numeroPreimpegno = numeroPreimpegno;
	}
	public double getImportoIniziale() {
		return importoIniziale;
	}
	public void setImportoIniziale(double importoIniziale) {
		this.importoIniziale = importoIniziale;
	}
	public double getImportoDisponibile() {
		return importoDisponibile;
	}
	public void setImportoDisponibile(double importoDisponibile) {
		this.importoDisponibile = importoDisponibile;
	}
	public String getOggettoPreimpegno() {
		return oggettoPreimpegno;
	}
	public void setOggettoPreimpegno(String oggettoPreimpegno) {
		this.oggettoPreimpegno = oggettoPreimpegno;
	}
	public String getTipoAtto() {
		return tipoAtto;
	}
	public void setTipoAtto(String tipoAtto) {
		this.tipoAtto = tipoAtto;
	}
	public Date getDataAtto() {
		return dataAtto;
	}
	public void setDataAtto(Date dataAtto) {
		this.dataAtto = dataAtto;
	}
	public String getNumeroAtto() {
		return numeroAtto;
	}
	public void setNumeroAtto(String numeroAtto) {
		this.numeroAtto = numeroAtto;
	}
	public String getCodicePCF() {
		return codicePCF;
	}
	public void setCodicePCF(String codicePCF) {
		this.codicePCF = codicePCF;
	}
	public String getDescrizionePCF() {
		return descrizionePCF;
	}
	public void setDescrizionePCF(String descrizionePCF) {
		this.descrizionePCF = descrizionePCF;
	}
}
