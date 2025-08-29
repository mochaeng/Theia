package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mochaeng.theia_api.processing.infrastructure.constants.TeiNamespaces;

@JacksonXmlRootElement(localName = "TEI", namespace = TeiNamespaces.TEI_NS)
public class GrobidData {

    @JacksonXmlProperty(localName = "title", namespace = TeiNamespaces.TEI_NS)
    public String title;
}
