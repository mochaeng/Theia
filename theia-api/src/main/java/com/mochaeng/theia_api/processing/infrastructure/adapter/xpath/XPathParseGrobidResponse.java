package com.mochaeng.theia_api.processing.infrastructure.adapter.xpath;

import com.mochaeng.theia_api.processing.application.port.out.ParseGrobidResponsePort;
import com.mochaeng.theia_api.processing.domain.model.Author;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.GrobidData;
import com.mochaeng.theia_api.processing.infrastructure.constants.TeiNamespaces;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Component("xpathExtractDocumentData")
@Slf4j
public class XPathParseGrobidResponse implements ParseGrobidResponsePort {

    @Override
    public Option<DocumentMetadata> parse(String response) {
        log.info("start parse grobid response with xpath");

        return Try.of(() -> {
            var document = parseXmlDocument(response);
            var xpath = createXpathNamespace();

            var title = extractTitle(xpath, document);
            var abstractText = extractAbstract(xpath, document);

            return DocumentMetadata.builder()
                .title(title)
                .abstractText(abstractText)
                .build();
        }).toOption();
    }

    private Document parseXmlDocument(String xmlContent)
        throws IOException, SAXException, ParserConfigurationException {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        var builder = factory.newDocumentBuilder();
        var bytes = xmlContent.getBytes(StandardCharsets.UTF_8);
        return builder.parse(new ByteArrayInputStream(bytes));
    }

    private XPath createXpathNamespace() {
        var xpathFactory = XPathFactory.newInstance();
        var xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new TeiNamespaceContext());
        return xpath;
    }

    private String extractTitle(XPath xpath, Document document) {
        return Try.of(() ->
            xpath.evaluate(
                "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title[@level='a'][@type='main']",
                document
            )
        ).getOrNull();
    }

    private String extractAbstract(XPath xpath, Document document) {
        return Try.of(() ->
            xpath.evaluate(
                "/tei:TEI/tei:teiHeader/tei:profileDesc/tei:abstract/tei:p",
                document
            )
        ).getOrNull();
    }

    private Option<Author> extractAuthorFromNode(XPath xpath, Node authorNode) {
        var firstName = evaluateXPath(
            xpath,
            "tei:persName/tei:forename[@type='first']",
            authorNode
        );

        if (Objects.equals(firstName, "")) {
            return Option.none();
        }

        return Option.of(Author.builder().firstName(firstName).build());
    }

    private String evaluateXPath(XPath xpath, String expression, Node context) {
        return Try.of(() -> xpath.evaluate(expression, context)).getOrElse("");
    }

    private static class TeiNamespaceContext
        implements javax.xml.namespace.NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            if ("tei".equals(prefix)) {
                return TeiNamespaces.TEI_NS;
            }
            return javax.xml.XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            if (TeiNamespaces.TEI_NS.equals(namespaceURI)) {
                return "tei";
            }
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            if (TeiNamespaces.TEI_NS.equals(namespaceURI)) {
                return Collections.singletonList("tei").iterator();
            }
            return Collections.emptyIterator();
        }
    }

    private DocumentMetadata createMetadata(
        UUID documentId,
        GrobidData grobidData
    ) {
        var title = grobidData
            .getTeiHeader()
            .getFileDesc()
            .getTitleStmt()
            .getTitle();

        //        var abstract_ = grobidData
        //            .getTeiHeader()
        //            .getFileDesc()

        return DocumentMetadata.builder()
            //            .documentId(documentId
            .title(title)
            .authors(new ArrayList<>())
            .abstractText(null)
            .additionalMetadata(Map.of("processEngine", "GROBID"))
            .build();
    }
}
