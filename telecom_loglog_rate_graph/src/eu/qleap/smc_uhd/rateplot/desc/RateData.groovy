package eu.qleap.smc_uhd.rateplot.desc;

import static com.example.BasicChecks.*

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A. 
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 *******************************************************************************
 *******************************************************************************
 * Represent an element of the rate plot (in general, a point)
 * 
 * The upload and download rates are express in mega-bit-per-second, which
 * is supposed to be 10^6 bit per second, not 2^20 bit per second. 
 * Additionally, it should be the "usuable data rate", which is another problem
 * thougj. 
 * 
 * The "download rate" may be missing in some cases: the Europe 2020 limits
 * say nothing about "download rates" for example.
 * 
 * The meaning of the (down,up) tuple may differ
 * =============================================
 * 
 * It is "technical"
 * -----------------
 * 
 *    It expressed the "maximum technically reachable rate"
 *    In that case, there should be a "cloud" of lower (down, up) values
 *    And also possibly a minimum point.
 *    The upstream rate should NOT be null.                   
 *                       
 * It is "political"
 * -----------------
 * 
 *    It expresses a "goal to reach", it basically is a minimum.
 *    The upper limit is open-ended.
 *    The upstream rate MAY be null.
 *    
 * If it is "product"
 * ------------------
 * 
 *    It expresses the advertised "case for the customer", which may or may
 *    not be exceeded or even reached in the real world.
 *    In that case, there should be a "cloud" around that point, with
 *    some actually seen values possibly exceeding the point, but most
 *    dropping below.
 *    The actual form of that "cloud" depends on how optimistic the 
 *    service provider is. It may also depend on the geographical location.
 *    The upstream rate should NOT be null.
 *  
 * 2013.07.29 - Written
 * 2013.09.04 - Reactivated, converted to Groovy
 * 2013.11.01 - Changes. Lots of changes.
 ******************************************************************************/

class RateData {

    final Double     down_mbps        // max. downstream rate in Mbps, not null
    final Double     up_mbps          // max. upstream rate, may be null if unspecified
    final Origin     origin           // political, product, technical?, not null
    final String     desc             // some text, not null
    final Bearer     bearer           // coax, copper, landline, radio... or null if unspecified
    final Operator   operator         // set to a known operator or null if irrelevant

    final Set hints  = new HashSet()  // hints on how to draw; interpretation is open!

    /**
     * Explicit map Constructor. 
     */

    RateData(Map map) {
        // Someone may have passed the null map, so make sure this is handled
        this(map?.down_mbps as Double,
        map?.up_mbps as Double,
        map?.origin,
        map?.desc,
        map?.bearer,
        map?.operator)
    }

    /**
     * Constructor
     */

    RateData(Double down_mbps, Double up_mbps, Origin origin, String desc, Bearer bearer, Operator operator) {
        checkNotNull(down_mbps, "down_mbps");
        checkTrue(Math.signum(down_mbps) > 0, "Downstream rate is negative or 0");
        Check.notNullAndNotOnlyWhitespace(desc, "desc");
        checkNotNull(origin, "origin");
        checkTrue(Check.imply(up_mbps == null, origin == Origin.POLITICAL), "Upstream may only be null in case of origin = '%s'", Origin.POLITICAL)
        this.down_mbps  = down_mbps
        this.up_mbps    = up_mbps
        this.origin     = origin
        this.desc       = desc
        this.bearer     = bearer
        this.operator   = operator
        if (this.up_mbps != null) {
            checkTrue(Math.signum(up_mbps) > 0, "Upstream rate is negative or 0");
        }
    }

    /**
     * GPRS, see http://en.wikipedia.org/wiki/General_Packet_Radio_Service
     *           http://de.wikipedia.org/wiki/General_Packet_Radio_Service
     *           http://www.surfstickvergleich.com/mobiles-internet-geschwindigkeit-umts-hspa-lte-co
     */

    static List<RateData> defineRateData_GRPS() {
        def res = []
        res << new RateData(
                down_mbps:    80.0/1000,
                up_mbps:      20.0/1000,
                origin:       Origin.TECHNICAL,
                desc:         "GPRS Class 8 & 10 and CS-4",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    60.0/1000,
                up_mbps:      40.0/1000,
                origin:       Origin.TECHNICAL,
                desc:         "GPRS Class 10 and CS-4",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    53.6/1000,
                up_mbps:      26.8/1000,
                origin:       Origin.TECHNICAL,
                desc:         "'GPRS' en Allemagne",
                bearer:       Bearer.RADIO)
        return res
    }

    /**
     * EGPRS (EDGE) http://en.wikipedia.org/wiki/General_Packet_Radio_Service
     *              http://www.surfstickvergleich.com/mobiles-internet-geschwindigkeit-umts-hspa-lte-co
     *              http://en.wikipedia.org/wiki/Comparison_of_wireless_data_standards
     */

    static List<RateData> defineRateData_EGRPS() {
        def res = []
        res << new RateData(
                down_mbps:    236.8/1000,
                up_mbps:      59.2/1000,
                origin:       Origin.TECHNICAL,
                desc:         "EGPRS (EDGE) Class 8, 10 and MCS-9",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    177.6/1000,
                up_mbps:      118.4/1000,
                origin:       Origin.TECHNICAL,
                desc:         "EGPRS (EDGE) Class 10 and MCS-9",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    220/1000,
                up_mbps:      110/1000,
                origin:       Origin.TECHNICAL,
                desc:         "'EDGE' en Allemagne",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    1.6,
                up_mbps:      0.5,
                origin:       Origin.TECHNICAL,
                desc:         "EDGE Evolution (3GPP Rel. 7)",
                bearer:       Bearer.RADIO)
        return res
    }

    /**
     * http://en.wikipedia.org/wiki/Comparison_of_wireless_data_standards
     */

    static List<RateData> defineRateData_LTE() {
        def res = []
        res << new RateData(
                down_mbps:    50,
                up_mbps:      10,
                origin:       Origin.TECHNICAL,
                desc:         "'LTE 800' en Allemagne (campagne)",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    100,
                up_mbps:      50,
                origin:       Origin.TECHNICAL,
                desc:         "'LTE 2600' en Allemagne (ville)",
                bearer:       Bearer.RADIO)
    }

    /**
     * Political limits: Haut débit
     */

    static List<RateData> defineRateData_Hauts_Débits() {
        def res = []
        res << new RateData(
                down_mbps:    0.25,
                up_mbps:      0.125,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' OCDE")
        res << new RateData(
                down_mbps:    0.51,
                up_mbps:      0.255,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' France (Service Universel)")
        res << new RateData(
                down_mbps:    0.60,
                up_mbps:      0.3,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' Suisse (Service Universel)")
        res << new RateData(
                down_mbps:    0.76,
                up_mbps:      0.38,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' US")
        res << new RateData(
                down_mbps:    1.00,
                up_mbps:      0.5,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' Finlande (Service Universel)")
        res << new RateData(
                down_mbps:    1.50,
                up_mbps:      0.75,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' Canada")
        res << new RateData(
                down_mbps:    2.00,
                up_mbps:      0.512,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' Luxembourg (Service Universel en 2010)")
        res << new RateData(
                down_mbps:    100.00,
                up_mbps:      50.0,
                origin:       Origin.POLITICAL,
                desc:         "'Haut Débit' Japon")
        return res
    }


    /**
     * Eschspeed offer (COAX)
     * http://www.esch.lu/citoyen/eschspeed/Pages/default.aspx
     */

    static List<RateData> defineRateData_Eschspeed(boolean withPro) {
        def res = []
        if (withPro) {
            res << new RateData(
                    down_mbps:    2.00,
                    up_mbps:      2.0,
                    origin:       Origin.PRODUCT,
                    desc:         "eschspeed PRO 1 (Quota: illimité)",
                    bearer:       Bearer.COAX,
                    operator:     Operator.VILLE_ESCH)
            res << new RateData(
                    down_mbps:    4.00,
                    up_mbps:      4.0,
                    origin:       Origin.PRODUCT,
                    desc:         "eschspeed PRO 2 (Quota: illimité)",
                    bearer:       Bearer.COAX,
                    operator:     Operator.VILLE_ESCH)
        }
        res << new RateData(
                down_mbps:    10.00,
                up_mbps:      256.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "eschspeed Lite (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.VILLE_ESCH)
        res << new RateData(
                down_mbps:    20.00,
                up_mbps:      384.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "eschspeed 1 (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.VILLE_ESCH)
        res << new RateData(
                down_mbps:    30.00,
                up_mbps:      512/1000,
                origin:       Origin.PRODUCT,
                desc:         "eschspeed 2 (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.VILLE_ESCH)
        res << new RateData(
                down_mbps:    50.00,
                up_mbps:      2048.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "eschspeed 3 (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.VILLE_ESCH)
        res << new RateData(
                down_mbps:    100.00,
                up_mbps:      5120.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "eschspeed Power (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.VILLE_ESCH)
        return res
    }

    /**
     * LOL Cable Offer
     * http://www.acett.lu/docs/flyer_lol_2.pdf
     * Réseau: Ettelbruck, Dudelange, Walferdange, Eltrona, Nokia-Siemens-Networks (now Eltrona Imagin)
     */

    static List<RateData> defineRateData_LOLCable() {
        def res = []
        res << new RateData(
                down_mbps:    8.00,
                up_mbps:      256/1000,
                origin:       Origin.PRODUCT,
                desc:         "LOL 'Câble DSL Bronze' (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.LUXEMBOURG_ONLINE)
        res << new RateData(
                down_mbps:    16.00,
                up_mbps:      384.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "LOL 'Câble DSL Silver' (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.LUXEMBOURG_ONLINE)
        res << new RateData(
                down_mbps:    24.00,
                up_mbps:      512.0/1000,
                origin:       Origin.PRODUCT,
                desc:        "LOL 'Câble DSL Gold' (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.LUXEMBOURG_ONLINE)
        return res
    }

    /**
     * Optical Hierarchy - Payload Bandwidth
     * http://en.wikipedia.org/wiki/Synchronous_optical_networking
     */

    static List<RateData> defineRateData_SONET() {
        List res = []
        res << new RateData(
                down_mbps:     50112.0/1000,
                up_mbps:       50112.0/1000,
                origin:        Origin.TECHNICAL,
                desc:          "SONET/SDH OC-1",
                bearer:        Bearer.LANDLINE)
        res << new RateData(
                down_mbps:      150336.0/1000,
                up_mbps:        150336.0/1000,
                origin:         Origin.TECHNICAL,
                desc:           "SONET/SDH OC-3",
                bearer:         Bearer.LANDLINE)
        res << new RateData(
                down_mbps:      601344.0/1000,
                up_mbps:        601344.0/1000,
                origin:         Origin.TECHNICAL,
                desc:           "SONET/SDH OC-12",
                bearer:         Bearer.LANDLINE)
        res << new RateData(
                down_mbps:      1202688.0/1000,
                up_mbps:        1202688.0/1000,
                origin:         Origin.TECHNICAL,
                desc:           "SONET/SDH OC-24",
                bearer:         Bearer.LANDLINE)
        res << new RateData(
                down_mbps:      2405376.0/1000,
                up_mbps:        2405376.0/1000,
                origin:         Origin.TECHNICAL,
                desc:           "SONET/SDH OC-48",
                bearer:         Bearer.LANDLINE)
        return res
    }

    /**
     * http://en.wikipedia.org/wiki/4G
     * http://www.surfstickvergleich.com/mobiles-internet-geschwindigkeit-umts-hspa-lte-co
     * Interpretations differ!! 
     */

    static List<RateData> defineRateData_UMTS_HSPA() {
        List res = []
        res << new RateData(
                down_mbps:    21.0,
                up_mbps:      5.8,
                origin:       Origin.TECHNICAL,
                desc:         "UMTS HSPA+",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    42.0,
                up_mbps:      11.5,
                origin:       Origin.TECHNICAL,
                desc:         "UMTS HSPA+",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    84.0,
                up_mbps:      22.0,
                origin:       Origin.TECHNICAL,
                desc:         "UMTS HSPA+",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    672.0,
                up_mbps:      168.0,
                origin:       Origin.TECHNICAL,
                desc:         "UMTS HSPA+ Rev. 11",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    1.8,
                up_mbps:      384/1000,
                origin:       Origin.TECHNICAL,
                desc:         "'HSDPA' en Allemagne",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    3.6,
                up_mbps:      384/1000,
                origin:       Origin.TECHNICAL,
                desc:         "'HSDPA' en Allemagne",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    7.2,
                up_mbps:      3,
                origin:       Origin.TECHNICAL,
                desc:         "'HSDPA/HSUPA' en Allemagne",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    14.4,
                up_mbps:      5.76,
                origin:       Origin.TECHNICAL,
                desc:         "'HSPA+' en Allemagne",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    21.6,
                up_mbps:      5.76,
                origin:       Origin.TECHNICAL,
                desc:         "'HSPA+' en Allemagne",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    28.8,
                up_mbps:      5.76,
                origin:       Origin.TECHNICAL,
                desc:         "'HSPA+' en Allemagne",
                bearer:       Bearer.RADIO)
        res << new RateData(
                down_mbps:    42.2,
                up_mbps:      5.76,
                origin:       Origin.TECHNICAL,
                desc:         "'HSPA+' en Allemagne",
                bearer:       Bearer.RADIO)
        return res
    }

    /**
     * Docsis
     * http://en.wikipedia.org/wiki/DOCSIS
     */

    static List<RateData> defineRateData_EuroDOCSIS() {
        List res = []
        res << new RateData(
                down_mbps:    50.0,
                up_mbps:      9.0,
                origin:       Origin.TECHNICAL,
                desc:         "EuroDOCSIS 1.x",
                bearer:       Bearer.COAX)
        res << new RateData(
                down_mbps:    50.0,
                up_mbps:      27.0,
                origin:       Origin.TECHNICAL,
                desc:         "EuroDOCSIS 2.0",
                bearer:       Bearer.COAX)
        res << new RateData(
                down_mbps:    50.0,
                up_mbps:      27.0,
                origin:       Origin.TECHNICAL,
                desc:         "EuroDOCSIS 3.0 (1 channel)",
                bearer:       Bearer.COAX)
        res << new RateData(
                down_mbps:    200,
                up_mbps:      108 ,
                origin:       Origin.TECHNICAL,
                desc:         "EuroDOCSIS 3.0 (4 channels)",
                bearer:       Bearer.COAX)
        res << new RateData(
                down_mbps:    400,
                up_mbps:      108,
                origin:       Origin.TECHNICAL,
                desc:         "EuroDOCSIS 3.0 (8 channels)",
                bearer:       Bearer.COAX)
        return res
    }

    /**
     * Numericable
     * http://www.numericable.lu/
     */

    static List<RateData> defineRateData_Numericable() {
        List res = []
        res << new RateData(
                down_mbps:    30.00,
                up_mbps:      1.0,
                origin:       Origin.PRODUCT,
                desc:         "Numericable 'Fiber 30' (arrêté)",
                bearer:       Bearer.COAX,
                operator : Operator.NUMERICABLE)
        res << new RateData(
                down_mbps:    100.00,
                up_mbps:      1.0,
                origin:       Origin.PRODUCT,
                desc:         "Numericable 'Fiber 50' (Quota: 50 GB)",
                bearer:       Bearer.COAX,
                operator : Operator.NUMERICABLE)
        res << new RateData(
                down_mbps:      70.00,
                up_mbps:        5.0,
                origin:         Origin.PRODUCT,
                desc:           "Numericable 'Fiber 70' (arrêté)",
                bearer:         Bearer.COAX,
                operator : Operator.NUMERICABLE)
        res << new RateData(
                down_mbps:      100.00,
                up_mbps:        5.0,
                origin:         Origin.PRODUCT,
                desc:           "Numericable 'Fiber 100' (Quota: 100 GB)",
                bearer:         Bearer.COAX,
                operator : Operator.NUMERICABLE)
        res << new RateData(
                down_mbps:      120.00,
                up_mbps:        5.0,
                origin:         Origin.PRODUCT,
                desc:           "Numericable 'Fiber 120' (Quota: illimité)",
                bearer:         Bearer.COAX,
                operator : Operator.NUMERICABLE)
        return res
    }

    /**
     * Eltrona
     * http://www.eltrona.lu/internet.htm
     */

    static List<RateData> defineRateData_Eltrona() {
        List res = []
        res << new RateData(
                down_mbps:    10.00,
                up_mbps:      1.0,
                origin:       Origin.PRODUCT,
                desc:         "Eltrona 'Trio 10' (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.ELTRONA)
        res << new RateData(
                down_mbps:    30.00,
                up_mbps:      1.0,
                origin:       Origin.PRODUCT,
                desc:         "Eltrona 'Trio 30' (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.ELTRONA)
        res << new RateData(
                down_mbps:    60.00,
                up_mbps:      5.0,
                origin:       Origin.PRODUCT,
                desc:         "Eltrona 'Trio 60' (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.ELTRONA)
        res << new RateData(
                down_mbps:    120.00,
                up_mbps:      5.0,
                origin:       Origin.PRODUCT,
                desc:         "Eltrona 'Trio 120' (Quota: illimité)",
                bearer:       Bearer.COAX,
                operator:     Operator.ELTRONA)
        return res
    }

    /**
     * Ultra Haut débits
     */

    static List<RateData> defineRateData_Ultra_Hauts_Débits() {
        List res = []
        res << new RateData(
                down_mbps:    25.00,
                up_mbps:      10.0,
                origin:       Origin.POLITICAL,
                desc:         "95% de la population du Luxembourg couverte en 2011")
        res << new RateData(
                down_mbps:    30.00,
                origin:       Origin.POLITICAL,
                desc:         "'Très Haut Débit' de la Commission Européenne")
        res << new RateData(
                down_mbps:    30.00,
                up_mbps:      15.0,
                origin:       Origin.POLITICAL,
                desc:         "'Ultra Haut Débit' Niveau #1")
        res << new RateData(
                down_mbps:    50.00,
                up_mbps:      25.0,
                origin:       Origin.PRODUCT,
                desc:         "'Ultra Haut Débit' Allemagne - couverture 75% des ménages en 2014")
        res << new RateData(
                down_mbps:    100.00,
                up_mbps:      50.0,
                origin:       Origin.POLITICAL,
                desc:         "'Ultra Haut Débit' Niveau #2")
        res << new RateData(
                down_mbps:    1000.00,
                up_mbps:      500.0,
                origin:       Origin.POLITICAL,
                desc:         "'Ultra Haut Débit' Niveau #3")
        return res
    }

    /**
     * DSL
     */

    static List<RateData> defineRateData_DSL(boolean withVdsl) {
        List res = []
        res << new RateData(
                down_mbps:    10.00,
                up_mbps:      1.0,
                origin:       Origin.TECHNICAL,
                desc:         "ADSL over POTS",
                bearer: Bearer.LANDLINE)
        res << new RateData(
                down_mbps:    24.00,
                up_mbps:      1.0,
                origin:       Origin.TECHNICAL,
                desc:         "ADSL2+ over POTS",
                bearer:       Bearer.LANDLINE)
        if (withVdsl) {
            res << new RateData(
                    down_mbps:     52.00,
                    up_mbps:       11.0,
                    origin:        Origin.TECHNICAL,
                    desc:          "VDSL1",
                    bearer:        Bearer.LANDLINE)
            res << new RateData(
                    down_mbps:      100.00,
                    up_mbps:        100.0,
                    origin:         Origin.TECHNICAL,
                    desc:           "VDSL 2",
                    bearer:         Bearer.LANDLINE)
        }
        return res
    }

    /**
     * P&T Luxembourg
     */

    static List<RateData> defineRateData_LuxFibre() {
        List res = []
        res << new RateData(
                down_mbps:    30.0,
                up_mbps:      10.0,
                origin:       Origin.PRODUCT,
                desc:         "LuxFibre S (ex. P&T LuxFibre 30) (Quota: 30 GB/mois)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        res << new RateData(
                down_mbps:    50.0,
                up_mbps:      25.0,
                origin:       Origin.PRODUCT,
                desc:         "P&T LuxFibre 50 (arrêté)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        res << new RateData(
                down_mbps:    100.0,
                up_mbps:      50.0,
                origin:       Origin.PRODUCT,
                desc:         "LuxFibre M (ex. P&T LuxFibre 100) (Quota: 50 GB/mois)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        res << new RateData(
                down_mbps:    200.0,
                up_mbps:      100.0,
                origin:       Origin.PRODUCT,
                desc:         "LuxFibre L (nouveau) (Quota: 100 GB/mois)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        res << new RateData(
                down_mbps:    1000.0,
                up_mbps:      500.0,
                origin:       Origin.PRODUCT,
                desc:         "LuxFibre XL (nouveau) (Quota: 200 GB/mois)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        res << new RateData(
                down_mbps:    1000.0,
                up_mbps:      1000.0,
                origin:       Origin.PRODUCT,
                desc:         "P&T 'Direct Internet Access' (clients professionnels)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        return res
    }


    /**
     * P&T Luxembourg
     */

    static List<RateData> defineRateData_LuxDSL() {
        List res = []
        res << new RateData(
                down_mbps:    8.0, // before September: 5.0
                up_mbps:      512.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "LuxDSL/SpeedSurf Junior (Quota: 2 GB/mois)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        res << new RateData(
                down_mbps:    12.0, // before September: 10.0
                up_mbps:      640.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "LuxDSL/SpeedSurf Run (Quota: 15 GB/mois)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        res << new RateData(
                down_mbps:    20.0,
                up_mbps:      768.0/1000,
                origin:       Origin.PRODUCT,
                desc:         "LuxDSL/SpeedSurf for professionals (Quota: illimité)",
                bearer:       Bearer.LANDLINE,
                operator:     Operator.POST)
        return res
    }

}