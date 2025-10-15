package com.mochaeng.theia_api.processing.infrastructure.adapter.xpath;

import com.mochaeng.theia_api.processing.application.port.out.ParseGrobidResponsePort;
import com.mochaeng.theia_api.processing.domain.model.Author;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.Keyword;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.GrobidConstants;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.GrobidData;
import com.mochaeng.theia_api.processing.infrastructure.constants.TeiNamespaces;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
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
            var authors = extractAuthors(xpath, document);
            var keywords = extractKeywords(xpath, document);

            return DocumentMetadata.builder()
                .title(title)
                .abstractText(abstractText)
                .authors(authors)
                .keywords(keywords)
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
            xpath.evaluate(GrobidConstants.TITLE, document)
        ).getOrNull();
    }

    private String extractAbstract(XPath xpath, Document document) {
        return Try.of(() ->
            xpath.evaluate(GrobidConstants.ABSTRACT, document)
        ).getOrNull();
    }

    private List<Author> extractAuthors(XPath xpath, Document document) {
        var authorNodes = evaluateXPath(
            xpath,
            GrobidConstants.AUTHORS,
            document,
            XPathConstants.NODESET
        );

        return authorNodes
            .map(this::nodeListToStream)
            .map(stream ->
                stream
                    .map(node -> extractAuthorFromNode(xpath, node))
                    .filter(Option::isDefined)
                    .map(Option::get)
                    .toList()
            )
            .getOrElse(Collections::emptyList);
    }

    private List<Keyword> extractKeywords(XPath xpath, Document document) {
        var keywordNodes = evaluateXPath(
            xpath,
            GrobidConstants.KEYWORDS,
            document,
            XPathConstants.NODESET
        );

        return nodeListToStream(keywordNodes.get())
            .map(this::extractKeywordFromNode)
            .filter(keyword -> !keyword.isEmpty())
            .toList();
    }

    private Keyword extractKeywordFromNode(Node keywordNode) {
        return Try.of(keywordNode::getTextContent)
            .map(Keyword::new)
            .getOrElse(Keyword::empty);
    }

    private Option<Author> extractAuthorFromNode(XPath xpath, Node authorNode) {
        var firstName = evaluateXPath(
            xpath,
            GrobidConstants.FORENAME,
            authorNode
        );

        if (firstName == null) {
            return Option.none();
        }

        return Option.of(Author.builder().firstName(firstName).build());
    }

    private String evaluateXPath(XPath xpath, String expression, Node context) {
        return Try.of(() -> xpath.evaluate(expression, context)).getOrNull();
    }

    private Option<NodeList> evaluateXPath(
        XPath xpath,
        String expression,
        Document document,
        QName constants
    ) {
        return Try.of(() ->
            (NodeList) xpath.evaluate(expression, document, constants)
        ).toOption();
    }

    private Stream<Node> nodeListToStream(NodeList nodes) {
        return IntStream.range(0, nodes.getLength()).mapToObj(nodes::item);
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
}
