/**
 * 
 */
package it.latraccia.utility;

/**
 * @author vgioviale
 *
 */
public class NumeroDocumentoResult
{
	String numeroDocumento;
	String numeroDocumentoPlur1;
	String numeroDocumentoPlur2;
	int docId;
	int docId1;
	int docId2;
	int codiceRisposta;
	String descrizioneRisposta;
	
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
	 * @return the numeroDocumento
	 */
	public String getNumeroDocumento() {
		return numeroDocumento;
	}
	/**
	 * @param numeroDocumento the numeroDocumento to set
	 */
	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}
	public String getNumeroDocumentoPlur1() {
		return numeroDocumentoPlur1;
	}
	public void setNumeroDocumentoPlur1(String numeroDocumentoPlur1) {
		this.numeroDocumentoPlur1 = numeroDocumentoPlur1;
	}
	public String getNumeroDocumentoPlur2() {
		return numeroDocumentoPlur2;
	}
	public void setNumeroDocumentoPlur2(String numeroDocumentoPlur2) {
		this.numeroDocumentoPlur2 = numeroDocumentoPlur2;
	}
	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	public int getDocId1() {
		return docId1;
	}
	public void setDocId1(int docId1) {
		this.docId1 = docId1;
	}
	public int getDocId2() {
		return docId2;
	}
	public void setDocId2(int docId2) {
		this.docId2 = docId2;
	}
}
