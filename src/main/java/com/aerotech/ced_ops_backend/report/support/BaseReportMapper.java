package com.aerotech.ced_ops_backend.report.support;

import com.aerotech.ced_ops_backend.common.entity.BaseReport;
import com.aerotech.ced_ops_backend.user.entity.User;

import java.util.List;

/**
 * Shared base for report response mappers.
 *
 * <p>Concentrates the cross-cutting mapping helpers that were previously duplicated in
 * every report module - the empty-safe list adapters and the {@code fullName} derived
 * value. Each concrete mapper supplies its own report-specific header fields via the
 * abstract hooks, so the REST response shape stays exactly the same as before.
 *
 * @param <R>   the concrete report entity
 * @param <E>   the concrete report entry entity
 * @param <RO>  the report response DTO
 * @param <ERO> the entry response DTO
 */
public abstract class BaseReportMapper<R extends BaseReport, E, RO, ERO> {

    /**
     * Maps a report (with its entries) into the module response DTO.
     */
    public abstract RO toResponse(R report, List<E> entries);

    /**
     * Maps a single entry into the module entry response DTO.
     */
    protected abstract ERO toSingleEntryResponse(E entry);

    public List<RO> toResponseList(List<R> reports) {

        if (reports == null) {
            return List.of();
        }

        return reports.stream()
                .map(report -> toResponse(report, List.of()))
                .toList();

    }

    public List<ERO> toEntryResponseList(List<E> entries) {

        if (entries == null) {
            return List.of();
        }

        return entries.stream()
                .map(this::toSingleEntryResponse)
                .toList();

    }

    protected String fullName(User user) {

        if (user == null) {
            return null;
        }

        return (user.getFirstName() + " " + user.getLastName()).trim();

    }

}