package com.qizhifu.jiaoxuepeiyu.file.controller;

import com.qizhifu.jiaoxuepeiyu.auth.AuthenticatedUserContext;
import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.file.FileStorageService;
import com.qizhifu.jiaoxuepeiyu.file.model.StoredFile;
import com.qizhifu.jiaoxuepeiyu.ue.UeIdentityResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "Local file upload APIs for admin, student, and UE training clients.")
public class FileUploadController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final FileStorageService storageService;
    private final UeIdentityResolver ueIdentityResolver;

    public FileUploadController(FileStorageService storageService, UeIdentityResolver ueIdentityResolver) {
        this.storageService = storageService;
        this.ueIdentityResolver = ueIdentityResolver;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "Stores a multipart file locally and returns metadata. UE clients can upload training recordings with category=recordings, then pass data.fileUrl to the training result callback.")
    public ApiResponse<StoredFile> upload(@Parameter(description = "Multipart file content.")
                                          @RequestParam("file") MultipartFile file,
                                          @Parameter(description = "Optional storage category such as resources, covers, assignments, or recordings.")
                                          @RequestParam(value = "category", required = false) String category,
                                          HttpServletRequest request) {
        requireUploadUserId(request);
        return ApiResponse.ok(storageService.store(file, category));
    }

    private Long requireUploadUserId(HttpServletRequest request) {
        String ueToken = request.getHeader(UeIdentityResolver.UE_TOKEN_HEADER);
        if (ueToken != null && ueToken.trim().length() > 0) {
            return ueIdentityResolver.requireUeStudentId(request);
        }
        Object currentUser = request.getAttribute(AuthenticatedUserContext.REQUEST_ATTRIBUTE);
        if (currentUser instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) currentUser).getId();
        }
        String value = request.getHeader(USER_ID_HEADER);
        if (value == null || value.trim().length() == 0) {
            throw new BusinessException(401, "Missing upload identity");
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(401, "Invalid upload identity");
        }
    }
}
