package com.oa.web.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.oa.common.dto.LoginUser;
import com.oa.common.result.AjaxResult;
import com.oa.web.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    private static final long MAX_IMAGE_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CurrentUser currentUser;

    @Value("${oa.upload.image-dir:uploads/images}")
    private String imageDir;

    public FileUploadController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @PostMapping("/images")
    @SentinelResource(value = "file:upload:image", blockHandler = "uploadImageBlocked", fallback = "uploadImageFallback")
    public AjaxResult<String> uploadImage(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        LoginUser user = currentUser.get(request);
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择图片文件");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            return AjaxResult.error("图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return AjaxResult.error("只能上传图片文件");
        }

        String extension = extension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return AjaxResult.error("仅支持 jpg、jpeg、png、gif、webp 图片");
        }

        String dateDir = DATE_FORMATTER.format(LocalDate.now());
        String filename = UUID.randomUUID() + "." + extension;
        Path root = Paths.get(imageDir).toAbsolutePath().normalize();
        Path targetDir = root.resolve(dateDir).normalize();
        Path target = targetDir.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            return AjaxResult.error("文件路径不合法");
        }

        try {
            Files.createDirectories(targetDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            return AjaxResult.error("图片保存失败");
        }

        return AjaxResult.success("/uploads/images/" + dateDir + "/" + filename);
    }

    public AjaxResult<String> uploadImageBlocked(MultipartFile file,
                                                 HttpServletRequest request,
                                                 BlockException exception) {
        return AjaxResult.error("图片上传请求过于频繁，请稍后再试");
    }

    public AjaxResult<String> uploadImageFallback(MultipartFile file,
                                                  HttpServletRequest request,
                                                  Throwable throwable) {
        return AjaxResult.error("图片上传暂不可用，请稍后重试");
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
