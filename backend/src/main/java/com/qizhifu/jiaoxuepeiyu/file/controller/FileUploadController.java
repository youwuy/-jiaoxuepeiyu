package com.qizhifu.jiaoxuepeiyu.file.controller;

import com.qizhifu.jiaoxuepeiyu.auth.AuthenticatedUserContext;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.file.FileStorageService;
import com.qizhifu.jiaoxuepeiyu.file.model.StoredFile;
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
@Tag(name = "Files", description = "Local file upload APIs for admin and student clients.")
public class FileUploadController {

    private final FileStorageService storageService;

    public FileUploadController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "Stores a multipart file locally and returns metadata that can be used by resource create or update APIs.")
    public ApiResponse<StoredFile> upload(@Parameter(description = "Multipart file content.")
                                          @RequestParam("file") MultipartFile file,
                                          @Parameter(description = "Optional storage category such as resources, covers, or assignments.")
                                          @RequestParam(value = "category", required = false) String category,
                                          HttpServletRequest request) {
        AuthenticatedUserContext.requireUserId(request);
        return ApiResponse.ok(storageService.store(file, category));
    }
}
