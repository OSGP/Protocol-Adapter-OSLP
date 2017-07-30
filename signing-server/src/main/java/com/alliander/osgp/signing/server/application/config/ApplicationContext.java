/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.signing.server.application.config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.alliander.osgp.shared.application.config.AbstractConfig;
import com.alliander.osgp.shared.security.CertificateHelper;
import com.alliander.osgp.signing.server.domain.exceptions.SigningServerException;

/**
 * An application context Java configuration class.
 */
@Configuration
@ComponentScan(basePackages = { "com.alliander.osgp.signing.server" })
@PropertySources({ @PropertySource("classpath:signing-server.properties"),
        @PropertySource(value = "file:${osgp/Global/config}", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${osgp/SigningServer/config}", ignoreResourceNotFound = true), })
public class ApplicationContext extends AbstractConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContext.class);

    private static final String PROPERTY_NAME_SIGNING_SERVER_SECURITY_SIGNKEY_PATH = "signing.server.security.signkey.path";
    private static final String PROPERTY_NAME_SIGNING_SERVER_SECURITY_KEYTYPE = "signing.server.security.keytype";
    private static final String PROPERTY_NAME_SIGNING_SERVER_SECURITY_SIGNATURE = "signing.server.security.signature";
    private static final String PROPERTY_NAME_SIGNING_SERVER_SECURITY_PROVIDER = "signing.server.security.provider";

    @Bean
    @Qualifier("signingServerPrivateKey")
    public PrivateKey privateKey() throws SigningServerException {
        try {
            return CertificateHelper.createPrivateKey(
                    this.environment.getRequiredProperty(PROPERTY_NAME_SIGNING_SERVER_SECURITY_SIGNKEY_PATH),
                    this.environment.getRequiredProperty(PROPERTY_NAME_SIGNING_SERVER_SECURITY_KEYTYPE),
                    this.environment.getRequiredProperty(PROPERTY_NAME_SIGNING_SERVER_SECURITY_PROVIDER));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchProviderException e) {
            final String msg = "Error creating private key bean";
            LOGGER.error(msg, e);
            throw new SigningServerException(msg, e);
        }
    }

    @Bean
    @Qualifier("signingServerKeyType")
    public String keyType() {
        return this.environment.getRequiredProperty(PROPERTY_NAME_SIGNING_SERVER_SECURITY_KEYTYPE);
    }

    @Bean
    @Qualifier("signingServerSignatureProvider")
    public String signatureProvider() {
        return this.environment.getRequiredProperty(PROPERTY_NAME_SIGNING_SERVER_SECURITY_PROVIDER);
    }

    @Bean
    @Qualifier("signingServerSignature")
    public String signature() {
        return this.environment.getRequiredProperty(PROPERTY_NAME_SIGNING_SERVER_SECURITY_SIGNATURE);
    }
}
