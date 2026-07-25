package com.aerotech.ced_ops_backend.attachment.repository;

import com.aerotech.ced_ops_backend.attachment.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByRelatedModuleAndRelatedEntityIdAndIsActiveTrue(
            String relatedModule, String relatedEntityId);

    Page<Attachment> findByIsActiveTrue(Pageable pageable);

    Page<Attachment> findByRelatedModuleAndIsActiveTrue(String relatedModule, Pageable pageable);

    Page<Attachment> findByIsActiveTrueAndOriginalFileNameContainingIgnoreCase(
            String fileName, Pageable pageable);

    boolean existsByFileHash(String fileHash);

}
