package com.mochaeng.theia_api.processing.infrastructure.adapter.xpath;

import com.mochaeng.theia_api.processing.infrastructure.constants.TeiNamespaces;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

public class TeiNamespaceContext implements NamespaceContext {

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
