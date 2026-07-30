package com.qizhifu.jiaoxuepeiyu.student.resource;

import com.qizhifu.jiaoxuepeiyu.student.resource.model.PublicResourceCard;
import com.qizhifu.jiaoxuepeiyu.student.resource.port.StudentResourceRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudentResourceService {

    private final StudentResourceRepository repository;

    public StudentResourceService(StudentResourceRepository repository) {
        this.repository = repository;
    }

    public List<PublicResourceCard> listPublicResources(String keyword, String resourceType, Long majorId) {
        return repository.findPublicResources(normalize(keyword), normalize(resourceType), majorId);
    }

    private String normalize(String value) {
        return value == null || value.trim().length() == 0 ? null : value.trim();
    }
}
