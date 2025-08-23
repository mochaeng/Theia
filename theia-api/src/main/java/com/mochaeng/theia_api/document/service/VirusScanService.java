package com.mochaeng.theia_api.document.service;

import com.mochaeng.theia_api.document.model.Document;

public interface VirusScanService {
  boolean hasVirus(Document document);
}
