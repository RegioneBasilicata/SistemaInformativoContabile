//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.6-01/24/2006 06:08 PM(kohsuke)-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.09.27 at 01:25:11 AM CEST 
//


package it.latraccia.entity.sic.richiesta;


/**
 * Java content class for DisponibilitaImpegno_Types complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/simonebrunox/Desktop/SIC/RichiestaSIC.xsd line 1029)
 * <p>
 * <pre>
 * &lt;complexType name="DisponibilitaImpegno_Types">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NumeroImpegno">
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;pattern value="[0-9]{9}"/>
 *           &lt;/restriction>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface DisponibilitaImpegnoTypes {


    /**
     * Numero dell Impegno
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getNumeroImpegno();

    /**
     * Numero dell Impegno
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setNumeroImpegno(java.lang.String value);

}