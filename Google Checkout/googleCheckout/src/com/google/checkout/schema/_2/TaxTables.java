//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.10 at 05:31:30 PM PDT 
//


package com.google.checkout.schema._2;


/**
 * Java content class for TaxTables complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/home/colinc/projects/apache-tomcat-5.5.12/apiv2.xsd line 266)
 * <p>
 * <pre>
 * &lt;complexType name="TaxTables">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="alternate-tax-tables" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="alternate-tax-table" type="{http://checkout.google.com/schema/2}AlternateTaxTable" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="default-tax-table" type="{http://checkout.google.com/schema/2}DefaultTaxTable"/>
 *       &lt;/all>
 *       &lt;attribute name="merchant-calculated" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface TaxTables {


    /**
     * Gets the value of the alternateTaxTables property.
     * 
     * @return
     *     possible object is
     *     {@link com.google.checkout.schema._2.TaxTables.AlternateTaxTablesType}
     */
    com.google.checkout.schema._2.TaxTables.AlternateTaxTablesType getAlternateTaxTables();

    /**
     * Sets the value of the alternateTaxTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.google.checkout.schema._2.TaxTables.AlternateTaxTablesType}
     */
    void setAlternateTaxTables(com.google.checkout.schema._2.TaxTables.AlternateTaxTablesType value);

    /**
     * Gets the value of the merchantCalculated property.
     * 
     */
    boolean isMerchantCalculated();

    /**
     * Sets the value of the merchantCalculated property.
     * 
     */
    void setMerchantCalculated(boolean value);

    /**
     * Gets the value of the defaultTaxTable property.
     * 
     * @return
     *     possible object is
     *     {@link com.google.checkout.schema._2.DefaultTaxTable}
     */
    com.google.checkout.schema._2.DefaultTaxTable getDefaultTaxTable();

    /**
     * Sets the value of the defaultTaxTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.google.checkout.schema._2.DefaultTaxTable}
     */
    void setDefaultTaxTable(com.google.checkout.schema._2.DefaultTaxTable value);


    /**
     * Java content class for anonymous complex type.
     * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/home/colinc/projects/apache-tomcat-5.5.12/apiv2.xsd line 269)
     * <p>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="alternate-tax-table" type="{http://checkout.google.com/schema/2}AlternateTaxTable" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     */
    public interface AlternateTaxTablesType {


        /**
         * Gets the value of the AlternateTaxTable property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the AlternateTaxTable property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAlternateTaxTable().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link com.google.checkout.schema._2.AlternateTaxTable}
         * 
         */
        java.util.List getAlternateTaxTable();

    }

}
