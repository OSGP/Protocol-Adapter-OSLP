/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.oslp.elster.application.services.oslp;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alliander.osgp.adapter.protocol.oslp.elster.infra.messaging.DeviceRequestMessageProcessorMap;
import com.alliander.osgp.adapter.protocol.oslp.elster.infra.messaging.DeviceRequestMessageType;
import com.alliander.osgp.adapter.protocol.oslp.elster.infra.messaging.DeviceResponseMessageSender;
import com.alliander.osgp.adapter.protocol.oslp.elster.infra.messaging.OslpEnvelopeProcessor;
import com.alliander.osgp.adapter.protocol.oslp.elster.infra.messaging.SigningServerRequestMessageSender;
import com.alliander.osgp.adapter.protocol.oslp.elster.infra.networking.OslpChannelHandlerServer;
import com.alliander.osgp.oslp.Oslp;
import com.alliander.osgp.oslp.OslpEnvelope;
import com.alliander.osgp.oslp.SignedOslpEnvelopeDto;
import com.alliander.osgp.oslp.UnsignedOslpEnvelopeDto;
import com.alliander.osgp.shared.infra.jms.DeviceMessageMetadata;
import com.alliander.osgp.shared.infra.jms.ProtocolResponseMessage;
import com.alliander.osgp.shared.infra.jms.RequestMessage;
import com.alliander.osgp.shared.infra.jms.ResponseMessage;
import com.alliander.osgp.shared.wsheaderattribute.priority.MessagePriorityEnum;

@Service
public class OslpSigningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OslpSigningService.class);

    private static final String SIGNING_REQUEST_MESSAGE_TYPE = "SIGNING_REQUEST";

    private static final String LINES = "-----------------------------------------------------------------------------";

    @Autowired
    private SigningServerRequestMessageSender signingServerRequestMessageSender;

    @Autowired
    private DeviceResponseMessageSender deviceResponseMessageSender;

    private OslpChannelHandlerServer oslpChannelHandlerServer;

    @Autowired
    @Qualifier("protocolOslpDeviceRequestMessageProcessorMap")
    private DeviceRequestMessageProcessorMap deviceRequestMessageProcessorMap;

    /**
     * Build OslpEnvelope for an OSLP request using the arguments supplied and
     * have the envelope signed by the signing server.
     */
    public void buildAndSignEnvelope(final String organisationIdentification, final String deviceIdentification,
            final String correlationUid, final byte[] deviceId, final byte[] sequenceNumber, final String ipAddress,
            final String domain, final String domainVersion, final String messageType, final int retryCount,
            final boolean isScheduled, final Oslp.Message payloadMessage, final Serializable extraData) {

        // Create DTO to transfer data using request message.
        final UnsignedOslpEnvelopeDto oslpEnvelopeDto = new UnsignedOslpEnvelopeDto(sequenceNumber, deviceId,
                payloadMessage, ipAddress, domain, domainVersion, messageType, retryCount, isScheduled,
                organisationIdentification, correlationUid, extraData);
        final RequestMessage requestMessage = new RequestMessage(correlationUid, organisationIdentification,
                deviceIdentification, oslpEnvelopeDto);

        // Send request message to signing server.
        this.signingServerRequestMessageSender.send(requestMessage, SIGNING_REQUEST_MESSAGE_TYPE);
    }

    /**
     * Build OslpEnvelope for an OSLP response using the arguments supplied and
     * have the envelope signed by the signing server.
     */
    public void buildAndSignEnvelope(final byte[] deviceId, final byte[] sequenceNumber,
            final Oslp.Message payloadMessage, final Integer channelId,
            final OslpChannelHandlerServer oslpChannelHandlerServer) {

        this.oslpChannelHandlerServer = oslpChannelHandlerServer;
        final String correlationUid = channelId.toString();

        // Create DTO to transfer data using request message.
        final UnsignedOslpEnvelopeDto unsignedOslpEnvelopeDto = new UnsignedOslpEnvelopeDto(sequenceNumber, deviceId,
                payloadMessage, correlationUid);
        final RequestMessage requestMessage = new RequestMessage(correlationUid, "organisationIdentification",
                "deviceIdentification", unsignedOslpEnvelopeDto);

        // Send request message to signing server.
        this.signingServerRequestMessageSender.send(requestMessage, SIGNING_REQUEST_MESSAGE_TYPE);
    }

    /**
     * Handle incoming signed OslpEnvelope from signing server.
     */
    public void handleSignedOslpEnvelope(final SignedOslpEnvelopeDto signedOslpEnvelopeDto,
            final String deviceIdentification) {

        final UnsignedOslpEnvelopeDto unsignedOslpEnvelopeDto = signedOslpEnvelopeDto.getUnsignedOslpEnvelopeDto();

        // Check if it's a request or response message.
        if (unsignedOslpEnvelopeDto.getType().equals(UnsignedOslpEnvelopeDto.OSLP_RESPONSE_TYPE)) {
            // Handle OSLP response message.
            this.handleSignedOslpResponse(signedOslpEnvelopeDto);
        } else {
            // Handle OSLP request message.
            this.handleSignedOslpRequest(signedOslpEnvelopeDto, deviceIdentification);
        }
    }

    private void handleSignedOslpRequest(final SignedOslpEnvelopeDto signedOslpEnvelopeDto,
            final String deviceIdentification) {

        final OslpEnvelope oslpEnvelope = signedOslpEnvelopeDto.getOslpEnvelope();
        final UnsignedOslpEnvelopeDto unsignedOslpEnvelopeDto = signedOslpEnvelopeDto.getUnsignedOslpEnvelopeDto();

        // Handle OSLP request message.
        LOGGER.debug(LINES);
        LOGGER.info("oslpEnvelope.size: {} for message type: {}", oslpEnvelope.getSize(),
                unsignedOslpEnvelopeDto.getMessageType());
        LOGGER.debug(LINES);
        LOGGER.debug("unsignedOslpEnvelopeDto.getCorrelationUid() : {}", unsignedOslpEnvelopeDto.getCorrelationUid());
        LOGGER.debug("unsignedOslpEnvelopeDto.getDeviceId() : {}", unsignedOslpEnvelopeDto.getDeviceId());
        LOGGER.debug("unsignedOslpEnvelopeDto.getDomain() : {}", unsignedOslpEnvelopeDto.getDomain());
        LOGGER.debug("unsignedOslpEnvelopeDto.getDomainVersion() : {}", unsignedOslpEnvelopeDto.getDomainVersion());
        LOGGER.debug("unsignedOslpEnvelopeDto.getIpAddress() : {}", unsignedOslpEnvelopeDto.getIpAddress());
        LOGGER.debug("unsignedOslpEnvelopeDto.getMessageType() : {}", unsignedOslpEnvelopeDto.getMessageType());
        LOGGER.debug("unsignedOslpEnvelopeDto.getOrganisationIdentification() : {}",
                unsignedOslpEnvelopeDto.getOrganisationIdentification());
        LOGGER.debug("unsignedOslpEnvelopeDto.getPayloadMessage() : {}", unsignedOslpEnvelopeDto.getPayloadMessage()
                .toString());
        LOGGER.debug("unsignedOslpEnvelopeDto.getRetryCount() : {}", unsignedOslpEnvelopeDto.getRetryCount());
        LOGGER.debug("unsignedOslpEnvelopeDto.getSequenceNumber() : {}", unsignedOslpEnvelopeDto.getSequenceNumber());
        LOGGER.debug("unsignedOslpEnvelopeDto.isScheduled() : {}", unsignedOslpEnvelopeDto.isScheduled());
        LOGGER.debug(LINES);

        // Try to convert message type to DeviceRequestMessageType member.
        final DeviceRequestMessageType deviceRequestMessageType = DeviceRequestMessageType
                .valueOf(unsignedOslpEnvelopeDto.getMessageType());

        // Handle message for message type.
        final OslpEnvelopeProcessor messageProcessor = this.deviceRequestMessageProcessorMap
                .getOslpEnvelopeProcessor(deviceRequestMessageType);
        if (messageProcessor == null) {
            LOGGER.error("No message processor for messageType: {}", unsignedOslpEnvelopeDto.getMessageType());
            return;
        }
        messageProcessor.processSignedOslpEnvelope(deviceIdentification, signedOslpEnvelopeDto);
    }

    private void handleSignedOslpResponse(final SignedOslpEnvelopeDto signedOslpEnvelopeDto) {

        final OslpEnvelope oslpEnvelope = signedOslpEnvelopeDto.getOslpEnvelope();
        final UnsignedOslpEnvelopeDto unsignedOslpEnvelopeDto = signedOslpEnvelopeDto.getUnsignedOslpEnvelopeDto();

        // Handle OSLP response message.
        LOGGER.debug(LINES);
        LOGGER.info("oslpEnvelope.size: {} for message type: {}", oslpEnvelope.getSize(),
                unsignedOslpEnvelopeDto.getMessageType());
        LOGGER.debug(LINES);
        LOGGER.debug("unsignedOslpEnvelopeDto.getCorrelationUid() : {}", unsignedOslpEnvelopeDto.getCorrelationUid());
        LOGGER.debug("unsignedOslpEnvelopeDto.getDeviceId() : {}", unsignedOslpEnvelopeDto.getDeviceId());
        LOGGER.debug("unsignedOslpEnvelopeDto.getSequenceNumber() : {}", unsignedOslpEnvelopeDto.getSequenceNumber());
        LOGGER.debug("unsignedOslpEnvelopeDto.getPayloadMessage() : {}", unsignedOslpEnvelopeDto.getPayloadMessage()
                .toString());

        // Send the signed OSLP envelope to the channel handler server.
        this.oslpChannelHandlerServer.processSignedOslpEnvelope(signedOslpEnvelopeDto);
    }

    /**
     * Handle an error from the signing server.
     */
    public void handleError(final String deviceIdentification, final ResponseMessage responseMessage) {

        final UnsignedOslpEnvelopeDto unsignedOslpEnvelopeDto = (UnsignedOslpEnvelopeDto) responseMessage
                .getDataObject();
        final DeviceMessageMetadata deviceMessageMetadata = new DeviceMessageMetadata(deviceIdentification,
                unsignedOslpEnvelopeDto.getOrganisationIdentification(), unsignedOslpEnvelopeDto.getCorrelationUid(),
                unsignedOslpEnvelopeDto.getMessageType(), MessagePriorityEnum.DEFAULT.getPriority());
        final ProtocolResponseMessage protocolResponseMessage = ProtocolResponseMessage.newBuilder()
                .domain(unsignedOslpEnvelopeDto.getDomain()).domainVersion(unsignedOslpEnvelopeDto.getDomainVersion())
                .deviceMessageMetadata(deviceMessageMetadata).result(responseMessage.getResult())
                .osgpException(responseMessage.getOsgpException()).scheduled(unsignedOslpEnvelopeDto.isScheduled())
                .build();
        this.deviceResponseMessageSender.send(protocolResponseMessage);
    }
}
