package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mochaeng.theia_api.processing.infrastructure.constants.TeiNamespaces;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "TEI", namespace = TeiNamespaces.TEI_NS)
public class GrobidData {

    @JacksonXmlProperty(
        localName = "teiHeader",
        namespace = TeiNamespaces.TEI_NS
    )
    private TeiHeader teiHeader;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeiHeader {

        @JacksonXmlProperty(
            localName = "fileDesc",
            namespace = TeiNamespaces.TEI_NS
        )
        private FileDesc fileDesc;

        @JacksonXmlProperty(
            localName = "profileDesc",
            namespace = TeiNamespaces.TEI_NS
        )
        private ProfileDesc profileDesc;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileDesc {

        @JacksonXmlProperty(
            localName = "titleStmt",
            namespace = TeiNamespaces.TEI_NS
        )
        private TitleStmt titleStmt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TitleStmt {

        @JacksonXmlProperty(
            localName = "title",
            namespace = TeiNamespaces.TEI_NS
        )
        private String title;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileDesc {

        @JacksonXmlProperty(
            localName = "textClass",
            namespace = TeiNamespaces.TEI_NS
        )
        private TextClass textClass;

        @JacksonXmlProperty(
            localName = "abstract",
            namespace = TeiNamespaces.TEI_NS
        )
        private Abstract abstract_;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextClass {

        @JacksonXmlProperty(
            localName = "keywords",
            namespace = TeiNamespaces.TEI_NS
        )
        private Keywords keywords;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Keywords {

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(
            localName = "term",
            namespace = TeiNamespaces.TEI_NS
        )
        private List<String> terms;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Abstract {

        @JacksonXmlProperty(localName = "p", namespace = TeiNamespaces.TEI_NS)
        private String p;
    }
}
