//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.6-01/24/2006 06:08 PM(kohsuke)-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.17 at 12:28:37 PM CET 
//


package it.latraccia.entity.sic.risposta;


/**
 * Java content class for Risposta_InterrogazionePreimpegniAperti_Types complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/simonebrunox/Desktop/SIC/RispostaSIC.xsd line 555)
 * <p>
 * <pre>
 * &lt;complexType name="Risposta_InterrogazionePreimpegniAperti_Types">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Preimpegno" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="DataPreimpegno" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *                   &lt;element name="NumeroPreimpegno">
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;pattern value="[0-9]{9}"/>
 *                     &lt;/restriction>
 *                   &lt;/element>
 *                   &lt;element name="ImportoIniziale" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="ImportoDisponibile" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="OggettoPreimpegno">
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;minLength value="1"/>
 *                       &lt;maxLength value="240"/>
 *                     &lt;/restriction>
 *                   &lt;/element>
 *                   &lt;element name="TipoAtto">
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;minLength value="1"/>
 *                       &lt;maxLength value="15"/>
 *                     &lt;/restriction>
 *                   &lt;/element>
 *                   &lt;element name="DataAtto" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *                   &lt;element name="NumeroAtto">
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;pattern value="[0-99999999999999999999]"/>
 *                     &lt;/restriction>
 *                   &lt;/element>
 *                   &lt;element name="PCF" type="{}PCF_Type"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface RispostaInterrogazionePreimpegniApertiTypes {


    /**
     * Gets the value of the Preimpegno property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the Preimpegno property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPreimpegno().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link it.latraccia.entity.sic.risposta.RispostaInterrogazionePreimpegniApertiTypes.PreimpegnoType}
     * 
     */
    java.util.List getPreimpegno();


    /**
     * Java content class for anonymous complex type.
     * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/simonebrunox/Desktop/SIC/RispostaSIC.xsd line 558)
     * <p>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="DataPreimpegno" type="{http://www.w3.org/2001/XMLSchema}date"/>
     *         &lt;element name="NumeroPreimpegno">
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;pattern value="[0-9]{9}"/>
     *           &lt;/restriction>
     *         &lt;/element>
     *         &lt;element name="ImportoIniziale" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="ImportoDisponibile" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="OggettoPreimpegno">
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;minLength value="1"/>
     *             &lt;maxLength value="240"/>
     *           &lt;/restriction>
     *         &lt;/element>
     *         &lt;element name="TipoAtto">
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;minLength value="1"/>
     *             &lt;maxLength value="15"/>
     *           &lt;/restriction>
     *         &lt;/element>
     *         &lt;element name="DataAtto" type="{http://www.w3.org/2001/XMLSchema}date"/>
     *         &lt;element name="NumeroAtto">
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;pattern value="[0-99999999999999999999]"/>
     *           &lt;/restriction>
     *         &lt;/element>
     *         &lt;element name="PCF" type="{}PCF_Type"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     */
    public interface PreimpegnoType {


        /**
         * Numero del pre-impegno
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getNumeroPreimpegno();

        /**
         * Numero del pre-impegno
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setNumeroPreimpegno(java.lang.String value);

        /**
         * Oggetto del pre-impegno
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getOggettoPreimpegno();

        /**
         * Oggetto del pre-impegno
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setOggettoPreimpegno(java.lang.String value);

        /**
         * Gets the value of the pcf property.
         * 
         * @return
         *     possible object is
         *     {@link it.latraccia.entity.sic.risposta.PCFType}
         */
        it.latraccia.entity.sic.risposta.PCFType getPCF();

        /**
         * Sets the value of the pcf property.
         * 
         * @param value
         *     allowed object is
         *     {@link it.latraccia.entity.sic.risposta.PCFType}
         */
        void setPCF(it.latraccia.entity.sic.risposta.PCFType value);

        /**
         * Data del pre-impegno
         * 
         * @return
         *     possible object is
         *     {@link java.util.Calendar}
         */
        java.util.Calendar getDataPreimpegno();

        /**
         * Data del pre-impegno
         * 
         * @param value
         *     allowed object is
         *     {@link java.util.Calendar}
         */
        void setDataPreimpegno(java.util.Calendar value);

        /**
         * Importo disponibile
         * 
         */
        double getImportoDisponibile();

        /**
         * Importo disponibile
         * 
         */
        void setImportoDisponibile(double value);

        /**
         * Tipo dell'atto
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getTipoAtto();

        /**
         * Tipo dell'atto
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setTipoAtto(java.lang.String value);

        /**
         * Numero dell'atto
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getNumeroAtto();

        /**
         * Numero dell'atto
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setNumeroAtto(java.lang.String value);

        /**
         * Importo iniziale
         * 
         */
        double getImportoIniziale();

        /**
         * Importo iniziale
         * 
         */
        void setImportoIniziale(double value);

        /**
         * Data dell'atto
         * 
         * @return
         *     possible object is
         *     {@link java.util.Calendar}
         */
        java.util.Calendar getDataAtto();

        /**
         * Data dell'atto
         * 
         * @param value
         *     allowed object is
         *     {@link java.util.Calendar}
         */
        void setDataAtto(java.util.Calendar value);

    }

}