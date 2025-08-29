package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

import com.mochaeng.theia_api.processing.application.port.out.ExtractDocumentDataPort;
import org.springframework.stereotype.Component;

@Component("grobidExtractDocumentData")
public class GrobidExtractDocumentData implements ExtractDocumentDataPort {

    @Override
    public void extract() {}
}
