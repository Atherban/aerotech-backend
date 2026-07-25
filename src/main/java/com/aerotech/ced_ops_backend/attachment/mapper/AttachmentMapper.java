package com.aerotech.ced_ops_backend.attachment.mapper;

import com.aerotech.ced_ops_backend.attachment.dto.response.AttachmentResponse;
import com.aerotech.ced_ops_backend.attachment.entity.Attachment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .originalFileName(attachment.getOriginalFileName())
                .storedFileName(attachment.getStoredFileName())
                .fileExtension(attachment.getFileExtension())
                .mimeType(attachment.getMimeType())
                .fileSize(attachment.getFileSize())
                .fileHash(attachment.getFileHash())
                .uploadedBy(attachment.getUploadedBy() != null
                        ? attachment.getUploadedBy().getEmployeeId() : null)
                .uploadedAt(attachment.getUploadedAt())
                .relatedModule(attachment.getRelatedModule())
                .relatedEntityId(attachment.getRelatedEntityId())
                .category(attachment.getAttachmentCategory() != null
                        ? attachment.getAttachmentCategory().name() : null)
                .description(attachment.getDescription())
                .isActive(attachment.getIsActive())
                .build();
    }

    public List<AttachmentResponse> toResponseList(List<Attachment> attachments) {
        return attachments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
