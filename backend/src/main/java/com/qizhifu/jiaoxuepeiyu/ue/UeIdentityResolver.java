package com.qizhifu.jiaoxuepeiyu.ue;

import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class UeIdentityResolver {

    public static final String UE_TOKEN_HEADER = "X-UE-Token";

    private final UeLaunchSessionService launchSessionService;

    public UeIdentityResolver(UeLaunchSessionService launchSessionService) {
        this.launchSessionService = launchSessionService;
    }

    public Long requireStudentId(HttpServletRequest request, Long trainingId) {
        String token = request.getHeader(UE_TOKEN_HEADER);
        if (InputValidator.hasText(token)) {
            return launchSessionService.requireStudentId(token, trainingId);
        }
        return StudentContext.requireStudentId(request);
    }

    public Long requireUeStudentId(HttpServletRequest request) {
        return launchSessionService.requireStudentId(request.getHeader(UE_TOKEN_HEADER));
    }
}
