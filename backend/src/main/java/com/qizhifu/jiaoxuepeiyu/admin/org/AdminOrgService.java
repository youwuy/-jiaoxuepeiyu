package com.qizhifu.jiaoxuepeiyu.admin.org;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgSortCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.port.AdminOrgRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrgService {

    private static final int MAX_ORG_NAME_LENGTH = 20;

    private final AdminOrgRepository repository;

    public AdminOrgService(AdminOrgRepository repository) {
        this.repository = repository;
    }

    public List<AdminOrg> getTree() {
        return getTree(null);
    }

    public List<AdminOrg> getTree(String keyword) {
        return getTree(keyword, false);
    }

    public List<AdminOrg> getTree(String keyword, boolean enabledOnly) {
        List<AdminOrg> tree = buildTree(repository.findAll());
        if (enabledOnly) {
            tree = filterEnabledTree(tree);
        }
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null) {
            return tree;
        }
        return filterTree(tree, normalizedKeyword);
    }

    @Transactional
    public Long create(AdminOrgCommand command, Long creatorId) {
        Long operatorId = requireOperator(creatorId);
        AdminOrgCommand normalized = normalized(command);
        AdminOrg parent = resolveParent(normalized.getParentId());
        if (parent != null && !parent.isEnabled()) {
            throw new BusinessException(400, "Parent organization must be enabled");
        }
        ensureUniqueName(null, parent == null ? null : parent.getOrgId(), normalized.getOrgName());
        return repository.create(normalized, operatorId);
    }

    @Transactional
    public void update(Long orgId, AdminOrgCommand command, Long operatorId) {
        Long currentOperatorId = requireOperator(operatorId);
        AdminOrg current = resolveOrg(orgId);
        AdminOrgCommand normalized = normalized(command);
        if (orgId != null && orgId.equals(command.getParentId())) {
            throw new BusinessException(400, "Organization cannot use itself as parent");
        }
        if (normalized.getParentId() != null && !equals(current.getParentId(), normalized.getParentId())) {
            throw new BusinessException(400, "Organization parent cannot be changed");
        }
        ensureUniqueName(orgId, current.getParentId(), normalized.getOrgName());
        repository.update(orgId, new AdminOrgCommand(current.getParentId(), normalized.getOrgName(), normalized.getSortOrder()),
                currentOperatorId);
    }

    @Transactional
    public void enable(Long orgId, Long operatorId) {
        Long currentOperatorId = requireOperator(operatorId);
        resolveOrg(orgId);
        repository.updateStatus(orgId, true, currentOperatorId);
    }

    @Transactional
    public void disable(Long orgId, Long operatorId) {
        Long currentOperatorId = requireOperator(operatorId);
        AdminOrg current = resolveOrg(orgId);
        for (Long descendantId : descendantIds(orgId)) {
            repository.updateStatus(descendantId, false, currentOperatorId);
        }
        repository.updateStatus(current.getOrgId(), false, currentOperatorId);
    }

    @Transactional
    public void updateSort(List<AdminOrgSortCommand> commands, Long operatorId) {
        Long currentOperatorId = requireOperator(operatorId);
        if (commands == null || commands.isEmpty()) {
            throw new BusinessException(400, "Organization sort data is required");
        }
        Map<Long, AdminOrg> byId = orgsById();
        for (AdminOrgSortCommand command : commands) {
            if (command == null || command.getOrgId() == null || command.getSortOrder() == null) {
                throw new BusinessException(400, "Organization sort data is invalid");
            }
            if (!byId.containsKey(command.getOrgId())) {
                throw new BusinessException(404, "Organization not found");
            }
            repository.updateSort(command, currentOperatorId);
        }
    }

    private List<AdminOrg> buildTree(List<AdminOrg> source) {
        List<AdminOrg> all = new ArrayList<AdminOrg>();
        if (source != null) {
            for (AdminOrg org : source) {
                all.add(copyNode(org));
            }
        }
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
        sortChildren(roots);
        return roots;
    }

    private void sortChildren(List<AdminOrg> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        Collections.sort(nodes, new OrgComparator());
        for (AdminOrg node : nodes) {
            sortChildren(node.getChildren());
        }
    }

    private List<AdminOrg> filterTree(List<AdminOrg> nodes, String keyword) {
        List<AdminOrg> filtered = new ArrayList<AdminOrg>();
        for (AdminOrg node : nodes) {
            AdminOrg copy = filterNode(node, keyword);
            if (copy != null) {
                filtered.add(copy);
            }
        }
        return filtered;
    }

    private List<AdminOrg> filterEnabledTree(List<AdminOrg> nodes) {
        List<AdminOrg> filtered = new ArrayList<AdminOrg>();
        for (AdminOrg node : nodes) {
            if (!node.isEnabled()) {
                continue;
            }
            AdminOrg copy = copyNode(node);
            copy.setChildren(filterEnabledTree(node.getChildren()));
            filtered.add(copy);
        }
        return filtered;
    }

    private AdminOrg filterNode(AdminOrg source, String keyword) {
        if (containsIgnoreCase(source.getOrgName(), keyword)) {
            return copyTree(source);
        }
        List<AdminOrg> children = new ArrayList<AdminOrg>();
        for (AdminOrg child : source.getChildren()) {
            AdminOrg filteredChild = filterNode(child, keyword);
            if (filteredChild != null) {
                children.add(filteredChild);
            }
        }
        if (children.isEmpty()) {
            return null;
        }
        AdminOrg copy = copyNode(source);
        copy.setChildren(children);
        return copy;
    }

    private AdminOrg copyTree(AdminOrg source) {
        AdminOrg copy = copyNode(source);
        List<AdminOrg> children = new ArrayList<AdminOrg>();
        for (AdminOrg child : source.getChildren()) {
            children.add(copyTree(child));
        }
        copy.setChildren(children);
        return copy;
    }

    private AdminOrg copyNode(AdminOrg source) {
        AdminOrg copy = new AdminOrg();
        copy.setOrgId(source.getOrgId());
        copy.setParentId(source.getParentId());
        copy.setOrgName(source.getOrgName());
        copy.setSortOrder(source.getSortOrder());
        copy.setEnabled(source.isEnabled());
        copy.setCreatedBy(source.getCreatedBy());
        copy.setCreatedName(source.getCreatedName());
        copy.setUpdatedBy(source.getUpdatedBy());
        copy.setUpdatedName(source.getUpdatedName());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }

    private AdminOrgCommand normalized(AdminOrgCommand command) {
        if (command == null) {
            throw new BusinessException(400, "Organization information is required");
        }
        String orgName = trimToNull(command.getOrgName());
        if (orgName == null) {
            throw new BusinessException(400, "Organization name is required");
        }
        if (orgName.length() > MAX_ORG_NAME_LENGTH) {
            throw new BusinessException(400, "Organization name cannot exceed 20 characters");
        }
        AdminOrgCommand normalized = new AdminOrgCommand();
        normalized.setParentId(command.getParentId());
        normalized.setOrgName(orgName);
        normalized.setSortOrder(command.getSortOrder());
        return normalized;
    }

    private AdminOrg resolveOrg(Long orgId) {
        if (orgId == null) {
            throw new BusinessException(400, "Organization id is required");
        }
        AdminOrg org = orgsById().get(orgId);
        if (org != null) {
            return copyNode(org);
        }
        throw new BusinessException(404, "Organization not found");
    }

    private Map<Long, AdminOrg> orgsById() {
        Map<Long, AdminOrg> byId = new LinkedHashMap<Long, AdminOrg>();
        for (AdminOrg org : repository.findAll()) {
            byId.put(org.getOrgId(), org);
        }
        return byId;
    }

    private AdminOrg resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return resolveOrg(parentId);
    }

    private void ensureUniqueName(Long orgId, Long parentId, String orgName) {
        for (AdminOrg org : repository.findAll()) {
            if (orgId != null && orgId.equals(org.getOrgId())) {
                continue;
            }
            if (equals(parentId, org.getParentId()) && orgName.equals(trimToNull(org.getOrgName()))) {
                throw new BusinessException(400, "Organization name already exists under the same parent");
            }
        }
    }

    private List<Long> descendantIds(Long orgId) {
        List<AdminOrg> all = repository.findAll();
        Map<Long, List<Long>> childrenByParent = new LinkedHashMap<Long, List<Long>>();
        for (AdminOrg org : all) {
            Long parentId = org.getParentId();
            if (!childrenByParent.containsKey(parentId)) {
                childrenByParent.put(parentId, new ArrayList<Long>());
            }
            childrenByParent.get(parentId).add(org.getOrgId());
        }
        List<Long> descendantIds = new ArrayList<Long>();
        collectDescendants(orgId, childrenByParent, descendantIds);
        return descendantIds;
    }

    private void collectDescendants(Long orgId, Map<Long, List<Long>> childrenByParent, List<Long> descendantIds) {
        List<Long> children = childrenByParent.get(orgId);
        if (children == null) {
            return;
        }
        for (Long childId : children) {
            descendantIds.add(childId);
            collectDescendants(childId, childrenByParent, descendantIds);
        }
    }

    private Long requireOperator(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(401, "Missing admin identity");
        }
        return operatorId;
    }

    private String normalizeKeyword(String keyword) {
        String normalized = trimToNull(keyword);
        if (normalized == null) {
            return null;
        }
        return normalized.toLowerCase(Locale.ENGLISH);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        String normalizedValue = trimToNull(value);
        return normalizedValue != null && normalizedValue.toLowerCase(Locale.ENGLISH).contains(keyword);
    }

    private String trimToNull(String value) {
        return value == null ? null : (value.trim().length() == 0 ? null : value.trim());
    }

    private boolean equals(Long left, Long right) {
        return left == null ? right == null : left.equals(right);
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
