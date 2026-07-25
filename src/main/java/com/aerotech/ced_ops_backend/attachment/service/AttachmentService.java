package com.aerotech.ced_ops_backend.attachment.service;

import com.aerotech.ced_ops_backend.attachment.dto.request.UpdateAttachmentRequest;
import com.aerotech.ced_ops_backend.attachment.dto.response.AttachmentResponse;
import com.aerotech.ced_ops_backend.attachment.entity.Attachment;
import com.aerotech.ced_ops_backend.attachment.mapper.AttachmentMapper;
import com.aerotech.ced_ops_backend.attachment.repository.AttachmentRepository;
import com.aerotech.ced_ops_backend.attachment.service.StorageService;
import com.aerotech.ced_ops_backend.common.enums.AttachmentCategory;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;
    private final StorageService storageService;
    private final UserRepository userRepository;

    @Value("${app.storage.max-file-size:10485760}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "csv"
    );

    @Transactional
    public AttachmentResponse upload(MultipartFile file, String relatedModule,
                                     String relatedEntityId, String category,
                                     String description) {
        validateFile(file);

        String fileHash = computeFileHash(file);
        if (attachmentRepository.existsByFileHash(fileHash)) {
            throw new BadRequestException("File with identical content already exists");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = extractExtension(originalFileName);
        String storedFileName = generateStoredFileName(extension);

        String storagePath;
        try {
            storagePath = storageService.store(file, storedFileName);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }

        User currentUser = currentUser();

        Attachment attachment = Attachment.builder()
                .originalFileName(originalFileName != null ? originalFileName : "unknown")
                .storedFileName(storedFileName)
                .fileExtension(extension)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(storagePath)
                .fileHash(fileHash)
                .uploadedBy(currentUser)
                .uploadedAt(LocalDateTime.now())
                .relatedModule(relatedModule)
                .relatedEntityId(relatedEntityId)
                .attachmentCategory(parseCategory(category))
                .description(description)
                .isActive(true)
                .build();

        Attachment saved = attachmentRepository.save(attachment);
        log.info("File uploaded: id={}, fileName={}, size={}, mimeType={}, uploadedBy={}",
                saved.getId(), originalFileName, file.getSize(), file.getContentType(),
                currentUser().getEmployeeId());
        return attachmentMapper.toResponse(saved);
    }

    @Transactional
    public List<AttachmentResponse> uploadMultiple(List<MultipartFile> files, String relatedModule,
                                                   String relatedEntityId, String category,
                                                   String description) {
        List<AttachmentResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(upload(file, relatedModule, relatedEntityId, category, description));
        }
        log.info("Multiple files uploaded: count={}, uploadedBy={}", files.size(), currentUser().getEmployeeId());
        return responses;
    }

    @Transactional(readOnly = true)
    public AttachmentResponse getById(Long id) {
        Attachment attachment = findActiveById(id);
        return attachmentMapper.toResponse(attachment);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getByEntity(String relatedModule, String relatedEntityId) {
        List<Attachment> attachments = attachmentRepository
                .findByRelatedModuleAndRelatedEntityIdAndIsActiveTrue(relatedModule, relatedEntityId);
        return attachmentMapper.toResponseList(attachments);
    }

    @Transactional
    public AttachmentResponse update(Long id, UpdateAttachmentRequest request) {
        Attachment attachment = findActiveById(id);

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            attachment.setAttachmentCategory(parseCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            attachment.setDescription(request.getDescription());
        }
        if (request.getRelatedModule() != null) {
            attachment.setRelatedModule(request.getRelatedModule());
        }
        if (request.getRelatedEntityId() != null) {
            attachment.setRelatedEntityId(request.getRelatedEntityId());
        }

        Attachment saved = attachmentRepository.save(attachment);
        log.info("Attachment updated: id={}, updatedBy={}", id, currentUser().getEmployeeId());
        return attachmentMapper.toResponse(saved);
    }

    @Transactional
    public void softDelete(Long id) {
        Attachment attachment = findActiveById(id);
        attachment.setIsActive(false);
        attachmentRepository.save(attachment);
        log.info("Attachment soft-deleted: id={}, fileName={}, deletedBy={}",
                id, attachment.getOriginalFileName(), currentUser().getEmployeeId());
    }

    @Transactional(readOnly = true)
    public PageResponse<AttachmentResponse> search(String keyword, String relatedModule,
                                                   String category, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "uploadedAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Attachment> attachmentPage;

        if (relatedModule != null && !relatedModule.isBlank()) {
            attachmentPage = attachmentRepository
                    .findByRelatedModuleAndIsActiveTrue(relatedModule, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            attachmentPage = attachmentRepository
                    .findByIsActiveTrueAndOriginalFileNameContainingIgnoreCase(keyword, pageable);
        } else {
            attachmentPage = attachmentRepository.findByIsActiveTrue(pageable);
        }

        return PageResponse.from(attachmentPage.map(attachmentMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public AttachmentResponse getAttachment(Long id) {
        return getById(id);
    }

    private Attachment findActiveById(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + id));
        if (!Boolean.TRUE.equals(attachment.getIsActive())) {
            throw new ResourceNotFoundException("Attachment not found with id: " + id);
        }
        return attachment;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BadRequestException("File type " + contentType + " is not allowed");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BadRequestException("Invalid file name");
        }

        String extension = extractExtension(originalFileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("File extension ." + extension + " is not allowed");
        }
    }

    private String computeFileHash(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new BadRequestException("Failed to compute file hash");
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private String generateStoredFileName(String extension) {
        String uuid = UUID.randomUUID().toString();
        return extension != null ? uuid + "." + extension : uuid;
    }

    private AttachmentCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return AttachmentCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResourceNotFoundException("User not found");
        }
        return userRepository.findByEmployeeId(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

}
