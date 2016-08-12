/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.oslp.elster.application.mapping;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alliander.osgp.dto.valueobjects.ConfigurationDto;
import com.alliander.osgp.oslp.Oslp;
import com.alliander.osgp.oslp.Oslp.SetConfigurationRequest;
import com.google.protobuf.ByteString;

public class ConfigurationToOslpSetConfigurationRequestConverter extends
        CustomConverter<ConfigurationDto, Oslp.SetConfigurationRequest> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConfigurationToOslpSetConfigurationRequestConverter.class);

    @Override
    public SetConfigurationRequest convert(final ConfigurationDto source,
            final Type<? extends Oslp.SetConfigurationRequest> destinationType) {

        final Oslp.SetConfigurationRequest.Builder setConfigurationRequest = Oslp.SetConfigurationRequest.newBuilder();

        if (source.getLightType() != null) {
            setConfigurationRequest.setLightType(this.mapperFacade.map(source.getLightType(), Oslp.LightType.class));
        }
        if (source.getDaliConfiguration() != null) {
            setConfigurationRequest.setDaliConfiguration(this.mapperFacade.map(source.getDaliConfiguration(),
                    Oslp.DaliConfiguration.class));
        }
        if (source.getRelayConfiguration() != null) {
            setConfigurationRequest.setRelayConfiguration(this.mapperFacade.map(source.getRelayConfiguration(),
                    Oslp.RelayConfiguration.class));
        }
        if (source.getShortTermHistoryIntervalMinutes() != null) {
            setConfigurationRequest.setShortTermHistoryIntervalMinutes(this.mapperFacade.map(
                    source.getShortTermHistoryIntervalMinutes(), Integer.class));
        }
        if (source.getLongTermHistoryInterval() != null) {
            setConfigurationRequest.setLongTermHistoryInterval(this.mapperFacade.map(
                    source.getLongTermHistoryInterval(), Integer.class));
        }
        if (source.getLongTermHistoryIntervalType() != null) {
            setConfigurationRequest.setLongTermHistoryIntervalType(this.mapperFacade.map(
                    source.getLongTermHistoryIntervalType(), Oslp.LongTermIntervalType.class));
        }
        if (source.getPreferredLinkType() != null) {
            setConfigurationRequest.setPreferredLinkType(this.mapperFacade.map(source.getPreferredLinkType(),
                    Oslp.LinkType.class));
        }
        if (source.getMeterType() != null) {
            setConfigurationRequest.setMeterType(this.mapperFacade.map(source.getMeterType(), Oslp.MeterType.class));
        }
        if (source.getAstroGateSunRiseOffset() != null) {
            setConfigurationRequest.setAstroGateSunRiseOffset(source.getAstroGateSunRiseOffset());
        }
        if (source.getAstroGateSunSetOffset() != null) {
            setConfigurationRequest.setAstroGateSunSetOffset(source.getAstroGateSunSetOffset());
        }
        if (source.isAutomaticSummerTimingEnabled() != null) {
            setConfigurationRequest.setIsAutomaticSummerTimingEnabled(source.isAutomaticSummerTimingEnabled());
        }
        if (source.getCommunicationNumberOfRetries() != null) {
            setConfigurationRequest.setCommunicationNumberOfRetries(source.getCommunicationNumberOfRetries());
        }
        if (source.getCommunicationPauseTimeBetweenConnectionTrials() != null) {
            setConfigurationRequest.setCommunicationPauseTimeBetweenConnectionTrials(source
                    .getCommunicationPauseTimeBetweenConnectionTrials());
        }
        if (source.getCommunicationTimeout() != null) {
            setConfigurationRequest.setCommunicationTimeout(source.getCommunicationTimeout());
        }
        if (source.getDeviceFixedIp() != null) {
            setConfigurationRequest.setDeviceFixIpValue(this.convertTextualIpAddressToByteString(source
                    .getDeviceFixedIp().getIpAddress()));
            setConfigurationRequest.setNetMask(this.convertTextualIpAddressToByteString(source.getDeviceFixedIp()
                    .getNetMask()));
            setConfigurationRequest.setGateWay(this.convertTextualIpAddressToByteString(source.getDeviceFixedIp()
                    .getGateWay()));
        }
        if (source.isDhcpEnabled() != null) {
            setConfigurationRequest.setIsDhcpEnabled(source.isDhcpEnabled());
        }
        if (source.getOsgpPortNumber() != null) {
            setConfigurationRequest.setOsgpPortNumber(source.getOsgpPortNumber());
        }
        if (source.getOspgIpAddress() != null) {
            setConfigurationRequest
                    .setOspgIpAddress(this.convertTextualIpAddressToByteString(source.getOspgIpAddress()));
        }
        if (source.isRelayRefreshing() != null) {
            setConfigurationRequest.setRelayRefreshing(source.isRelayRefreshing());
        }
        if (source.getSummerTimeDetails() != null) {
            final String summerTimeDetails = this.convertSummerTimeWinterTimeDetails(source.getSummerTimeDetails());
            setConfigurationRequest.setSummerTimeDetails(summerTimeDetails);
        }
        if (source.isTestButtonEnabled() != null) {
            setConfigurationRequest.setIsTestButtonEnabled(source.isTestButtonEnabled());
        }
        if (source.getTimeSyncFrequency() != null) {
            setConfigurationRequest.setTimeSyncFrequency(source.getTimeSyncFrequency());
        }
        if (source.getWinterTimeDetails() != null) {
            final String winterTimeDetails = this.convertSummerTimeWinterTimeDetails(source.getWinterTimeDetails());
            setConfigurationRequest.setWinterTimeDetails(winterTimeDetails);
        }
        if (source.getSwitchingDelays() != null) {
            setConfigurationRequest.addAllSwitchingDelay(source.getSwitchingDelays());
        }
        if (source.getRelayLinking() != null) {
            setConfigurationRequest.addAllRelayLinking(this.mapperFacade.mapAsList(source.getRelayLinking(),
                    Oslp.RelayMatrix.class));
        }

        return setConfigurationRequest.build();
    }

    private ByteString convertTextualIpAddressToByteString(final String ipAddress) {
        try {
            LOGGER.info("textual IP address or netmask: {}", ipAddress);
            final InetAddress inetAddress = InetAddress.getByName(ipAddress);
            final byte[] bytes = inetAddress.getAddress();
            LOGGER.info("bytes.length: {}", bytes.length);
            for (final byte b : bytes) {
                LOGGER.info("byte: {}", b);
            }
            return ByteString.copyFrom(bytes);
        } catch (final UnknownHostException e) {
            LOGGER.error("UnknownHostException", e);
            return null;
        }
    }

    // @formatter:off
    /*
     * SummerTimeDetails/WinterTimeDetails string: MMWHHmi
     *
     * where: (note, north hemisphere summer begins at the end of march) MM:
     * month W: day of the week (0- Monday, 6- Sunday) HH: hour of the changing
     * time mi: minutes of the changing time
     *
     * Default value for summer time: 0360100 Default value for summer time:
     * 1060200
     */
    // @formatter:on
    private String convertSummerTimeWinterTimeDetails(final DateTime dateTime) {
        LOGGER.info("dateTime: {}", dateTime);

        final StringBuilder timeDetails = new StringBuilder();
        timeDetails.append(String.format("%02d", dateTime.getMonthOfYear()));
        timeDetails.append(dateTime.getDayOfWeek() - 1);
        timeDetails.append(String.format("%02d", dateTime.getHourOfDay()));
        timeDetails.append(String.format("%02d", dateTime.getMinuteOfHour()));
        final String formattedTimeDetails = timeDetails.toString();

        LOGGER.info("formattedTimeDetails: {}", formattedTimeDetails);

        return formattedTimeDetails;
    }
}
