package com.mochaeng.theia_api.processing.infrastructure.adapter.xpath;

import com.mochaeng.theia_api.processing.application.port.out.ParseGrobidResponsePort;
import com.mochaeng.theia_api.processing.domain.model.Author;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.Keyword;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.GrobidConstants;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Component("xpathExtractDocumentData")
@Slf4j
public class XPathParseGrobidResponse implements ParseGrobidResponsePort {

    private static final XPathFactory XPATH_FACTORY =
        XPathFactory.newInstance();
    private static final NamespaceContext TEI_CONTEXT =
        new TeiNamespaceContext();

    @Override
    public Option<DocumentMetadata> parse(String response) {
        log.info("start parse grobid response with xpath");

        var documentResult = parseXmlDocument(response);
        if (documentResult.isLeft()) {
            log.info(
                "parse grobid response failed: {}",
                documentResult.getLeft()
            );
            return Option.none();
        }

        var xpath = createXpath();

        var title = extract(xpath, GrobidConstants.TITLE, documentResult.get());
        if (!StringUtils.hasText(title)) {
            return Option.none();
        }

        var document = documentResult.get();

        var abstract_ = extract(xpath, GrobidConstants.ABSTRACT, document);
        var authors = extractAuthors(xpath, document);
        var keywords = extractKeywords(xpath, document);

        var metadata = DocumentMetadata.builder()
            .title(title)
            .abstractText(abstract_)
            .authors(authors)
            .keywords(keywords)
            .build();

        return Option.of(metadata);
    }

    private Either<String, Document> parseXmlDocument(String xmlContent) {
        return Try.of(() -> {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            var builder = factory.newDocumentBuilder();
            var bytes = xmlContent.getBytes(StandardCharsets.UTF_8);

            return builder.parse(new ByteArrayInputStream(bytes));
        })
            .toEither()
            .mapLeft(Throwable::getMessage);
    }

    private XPath createXpath() {
        var xpath = XPATH_FACTORY.newXPath();
        xpath.setNamespaceContext(TEI_CONTEXT);
        return xpath;
    }

    private String extract(XPath xpath, String expression, Node node) {
        return Try.of(() -> xpath.evaluate(expression, node)).getOrElse("");
    }

    private List<Author> extractAuthors(XPath xpath, Document document) {
        var authorNodes = evaluateNodeSet(
            xpath,
            GrobidConstants.Author.NODES,
            document
        );

        return authorNodes
            .map(this::streamOf)
            .map(stream ->
                stream
                    .flatMap(node ->
                        extractAuthorFromNode(xpath, node).toJavaStream()
                    )
                    .toList()
            )
            .getOrElse(Collections::emptyList);
    }

    private List<Keyword> extractKeywords(XPath xpath, Document document) {
        var keywordNodes = evaluateNodeSet(
            xpath,
            GrobidConstants.KEYWORDS,
            document
        );

        return streamOf(keywordNodes.get())
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
            GrobidConstants.Author.FORENAME,
            authorNode
        );

        var lastName = evaluateXPath(
            xpath,
            GrobidConstants.Author.SURNAME,
            authorNode
        );

        var email = evaluateXPath(
            xpath,
            GrobidConstants.Author.EMAIL,
            authorNode
        );

        if (!StringUtils.hasText(firstName) && !StringUtils.hasText(lastName)) {
            return Option.none();
        }

        return Option.of(
            Author.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build()
        );
    }

    private String evaluateXPath(XPath xpath, String expression, Node context) {
        return Try.of(() -> xpath.evaluate(expression, context)).getOrNull();
    }

    private Option<NodeList> evaluateNodeSet(
        XPath xpath,
        String expression,
        Document document
    ) {
        return Try.of(() ->
            (NodeList) xpath.evaluate(
                expression,
                document,
                XPathConstants.NODESET
            )
        ).toOption();
    }

    private Stream<Node> streamOf(NodeList nodes) {
        return IntStream.range(0, nodes.getLength()).mapToObj(nodes::item);
    }
}
