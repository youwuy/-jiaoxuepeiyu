package com.qizhifu.jiaoxuepeiyu.admin.org;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.port.AdminOrgRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminOrgService {

    private final AdminOrgRepository repository;

    public AdminOrgService(AdminOrgRepository repository) {
        this.repository = repository;
    }

    public List<AdminOrg> getTree() {
        List<AdminOrg> all = new ArrayList<AdminOrg>(repository.findAll());
        Collections.sort(all, new OrgComparator());
        Map<Long, AdminOrg> byId = new LinkedHashMap<Long, AdminOrg>();
        for (AdminOrg org : all) {
            org.setChildren(new ArrayList<AdminOrg>());
            byId.put(org.getOrgId(), org);
        }
        List<AdminOrg> roots = new ArrayList<AdminOrg>();
        for (AdminOrg org : all) {
            if (org.getParentId() == null || !byId.containsKey(org.getParentId())) {
                roots.add(org);
            } else {
                byId.get(org.getParentId()).getChildren().add(org);
            }
        }
        return roots;
    }

    public Long create(AdminOrgCommand command) {
        validate(command);
        return repository.create(normalized(command));
    }

    public void update(Long orgId, AdminOrgCommand command) {
        validate(command);
        if (orgId != null && orgId.equals(command.getParentId())) {
            throw new BusinessException(400, "Organization cannot use itself as parent");
        }
        repository.update(orgId, normalized(command));
    }

    public void enable(Long orgId) {
        repository.updateStatus(orgId, true);
    }

    public void disable(Long orgId) {
        repository.updateStatus(orgId, false);
    }

    private void validate(AdminOrgCommand command) {
        if (command == null || command.getOrgName() == null || command.getOrgName().trim().length() == 0) {
            throw new BusinessException(400, "Organization name is required");
        }
    }

    private AdminOrgCommand normalized(AdminOrgCommand command) {
        return new AdminOrgCommand(command.getParentId(), command.getOrgName().trim(), command.getSortOrder());
    }

    private static class OrgComparator implements Comparator<AdminOrg> {
        @Override
        public int compare(AdminOrg left, AdminOrg right) {
            int sort = Integer.compare(left.getSortOrder(), right.getSortOrder());
            if (sort != 0) {
                return sort;
            }
            return left.getOrgId().compareTo(right.getOrgId());
        }
    }
}
