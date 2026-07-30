package com.qizhifu.jiaoxuepeiyu.student.resource.port;

import com.qizhifu.jiaoxuepeiyu.student.resource.model.PublicResourceCard;
import java.util.List;

public interface StudentResourceRepository {

    List<PublicResourceCard> findPublicResources(String keyword, String resourceType, Long majorId);
}
