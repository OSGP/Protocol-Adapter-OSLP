/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.oslp.infra.networking;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alliander.osgp.adapter.protocol.oslp.domain.entities.OslpDevice;
import com.alliander.osgp.adapter.protocol.oslp.domain.repositories.OslpDeviceRepository;
import com.alliander.osgp.adapter.protocol.oslp.infra.messaging.OslpLogItemRequestMessage;
import com.alliander.osgp.adapter.protocol.oslp.infra.messaging.OslpLogItemRequestMessageSender;
import com.alliander.osgp.core.db.api.application.services.DeviceDataService;
import com.alliander.osgp.oslp.Oslp;
import com.alliander.osgp.oslp.OslpEnvelope;

public abstract class OslpChannelHandler extends SimpleChannelHandler {

    private final Logger logger;

    @Resource
    protected String oslpSignatureProvider;

    @Resource
    protected String oslpSignature;

    @Resource
    protected int connectionTimeout;

    @Autowired
    private OslpDeviceRepository oslpDeviceRepository;

    @Autowired
    private OslpLogItemRequestMessageSender oslpLogItemRequestMessageSender;

    @Autowired
    private DeviceDataService deviceDataService;

    protected final ConcurrentMap<Integer, OslpCallbackHandler> callbackHandlers = new ConcurrentHashMap<>();

    protected OslpChannelHandler(final Logger logger) {
        this.logger = logger;
    }

    public void setProvider(final String provider) {
        this.oslpSignatureProvider = provider;
    }

    public void setSignature(final String signature) {
        this.oslpSignature = signature;
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel opened", e.getChannel().getId());
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel disconnected", e.getChannel().getId());
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel closed", e.getChannel().getId());
        super.channelClosed(ctx, e);
    }

    @Override
    public void channelUnbound(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        this.logger.info("{} Channel unbound", e.getChannel().getId());
        super.channelUnbound(ctx, e);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        final int channelId = e.getChannel().getId();
        if (this.isConnectionReset(e.getCause())) {
            this.logger.info("{} Connection was (as expected) reset by the device.", channelId);
        } else {
            this.logger.warn("{} Unexpected exception from downstream. {}", channelId, e.getCause());
            this.callbackHandlers.get(channelId).getDeviceResponseHandler().handleException(e.getCause());
        }
        e.getChannel().close();
    }

    protected void logMessage(final OslpEnvelope message, final boolean incoming) {

        final String deviceUid = Base64.encodeBase64String(message.getDeviceId());
        String deviceIdentification = this.getDeviceIdentificationFromMessage(message.getPayloadMessage());

        // Assume outgoing messages always valid.
        final boolean isValid = incoming ? message.isValid() : true;

        if (StringUtils.isEmpty(deviceIdentification)) {
            // Getting the deviceIdentification from the oslpDevice instance
            final OslpDevice oslpDevice = this.oslpDeviceRepository.findByDeviceUid(deviceUid);
            if (oslpDevice != null) {
                deviceIdentification = oslpDevice.getDeviceIdentification();
            }
        }

        final OslpLogItemRequestMessage oslpLogItemRequestMessage = new OslpLogItemRequestMessage(null, deviceUid,
                deviceIdentification, incoming, isValid, message.getPayloadMessage(), message.getSize());

        this.oslpLogItemRequestMessageSender.send(oslpLogItemRequestMessage);
    }

    private boolean isConnectionReset(final Throwable e) {
        return e != null && e instanceof IOException && e.getMessage() != null
                && e.getMessage().contains("Connection reset by peer");
    }

    protected boolean isOslpResponse(final OslpEnvelope envelope) {
        return envelope.getPayloadMessage().hasRegisterDeviceResponse()
                || envelope.getPayloadMessage().hasConfirmRegisterDeviceResponse()
                || envelope.getPayloadMessage().hasStartSelfTestResponse()
                || envelope.getPayloadMessage().hasStopSelfTestResponse()
                || envelope.getPayloadMessage().hasUpdateFirmwareResponse()
                || envelope.getPayloadMessage().hasSetLightResponse()
                || envelope.getPayloadMessage().hasSetEventNotificationsResponse()
                || envelope.getPayloadMessage().hasEventNotificationResponse()
                || envelope.getPayloadMessage().hasSetScheduleResponse()
                || envelope.getPayloadMessage().hasGetFirmwareVersionResponse()
                || envelope.getPayloadMessage().hasGetStatusResponse()
                || envelope.getPayloadMessage().hasResumeScheduleResponse()
                || envelope.getPayloadMessage().hasSetRebootResponse()
                || envelope.getPayloadMessage().hasSetTransitionResponse()
                || envelope.getPayloadMessage().hasSetConfigurationResponse()
                || envelope.getPayloadMessage().hasGetConfigurationResponse()
                || envelope.getPayloadMessage().hasGetActualPowerUsageResponse()
                || envelope.getPayloadMessage().hasGetPowerUsageHistoryResponse();
    }

    private String getDeviceIdentificationFromMessage(final Oslp.Message message) {
        String deviceIdentification = "";

        if (message.hasRegisterDeviceRequest()) {
            deviceIdentification = message.getRegisterDeviceRequest().getDeviceIdentification();
        }

        return deviceIdentification;
    }
}
