package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

public class GrobidConstants {

    public static final String TITLE =
        "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[@level='a'][@type='main']";

    public static final String ABSTRACT =
        "/tei:TEI/tei:teiHeader/tei:profileDesc/tei:abstract/tei:p";

    public static class Author {

        public static final String NODES =
            "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:analytic/tei:author";

        public static final String FORENAME =
            "tei:persName/tei:forename[@type='first']";

        public static final String SURNAME =
            "tei:persName/tei:surname";

        public static final String EMAIL = "tei:email";
    }

    public static final String KEYWORDS =
        "/tei:TEI/tei:teiHeader/tei:profileDesc/tei:textClass/tei:keywords/tei:term";
}
