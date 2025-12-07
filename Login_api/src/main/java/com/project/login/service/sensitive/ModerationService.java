package com.project.login.service.sensitive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.login.mapper.NoteModerationMapper;
import com.project.login.model.dataobject.NoteModerationDO;
import com.project.login.model.vo.SensitiveCheckResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final NoteModerationMapper noteModerationMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveResult(SensitiveCheckResult r) {
        Long noteId = r.getNoteMeta() == null ? null : r.getNoteMeta().getNoteId();
        NoteModerationDO existing = noteId == null ? null : noteModerationMapper.selectLatestPendingByNoteId(noteId);

        NoteModerationDO d = existing == null ? new NoteModerationDO() : existing;
        if (noteId != null) d.setNoteId(noteId);
        d.setStatus(r.getStatus());
        d.setRiskLevel(r.getRiskLevel());
        d.setScore(r.getScore() == null ? null : r.getScore().intValue());
        try {
            d.setCategoriesJson(objectMapper.writeValueAsString(r.getCategories()));
            d.setFindingsJson(objectMapper.writeValueAsString(r.getFindings()));
        } catch (Exception e) {
            d.setCategoriesJson("[]");
            d.setFindingsJson("[]");
        }
        d.setSource("LLM");
        d.setCreatedAt(LocalDateTime.now());
        d.setIsHandled(Boolean.FALSE);

        if (existing == null) {
            noteModerationMapper.insert(d);
        } else {
            noteModerationMapper.updateFields(d);
        }
    }
}
