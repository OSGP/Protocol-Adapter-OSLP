package com.alliander.osgp.adapter.protocol.oslp.application.mapping;

import com.alliander.osgp.dto.valueobjects.HistoryTermTypeDto;
import com.alliander.osgp.oslp.Oslp;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

public class HistoryTermTypeConverter extends BidirectionalConverter<HistoryTermTypeDto, Oslp.HistoryTermType> {

    @Override
    public com.alliander.osgp.oslp.Oslp.HistoryTermType convertTo(final HistoryTermTypeDto source,
            final Type<com.alliander.osgp.oslp.Oslp.HistoryTermType> destinationType, final MappingContext context) {
        if (source == null) {
            return null;
        }

        if (source.equals(HistoryTermTypeDto.LONG)) {
            return com.alliander.osgp.oslp.Oslp.HistoryTermType.Long;
        } else {
            return com.alliander.osgp.oslp.Oslp.HistoryTermType.Short;
        }
    }

    @Override
    public HistoryTermTypeDto convertFrom(final com.alliander.osgp.oslp.Oslp.HistoryTermType source,
            final Type<HistoryTermTypeDto> destinationType, final MappingContext context) {
        if (source == null) {
            return null;
        }

        if (source.equals(com.alliander.osgp.oslp.Oslp.HistoryTermType.Long)) {
            return HistoryTermTypeDto.LONG;
        } else {
            return HistoryTermTypeDto.SHORT;
        }
    }

}
