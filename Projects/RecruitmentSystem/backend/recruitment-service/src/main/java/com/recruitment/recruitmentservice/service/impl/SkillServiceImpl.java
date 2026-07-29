package com.recruitment.recruitmentservice.service.impl;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.skill.CreateSkillRequest;
import com.recruitment.recruitmentservice.dto.skill.SkillResponse;
import com.recruitment.recruitmentservice.dto.skill.UpdateSkillRequest;
import com.recruitment.recruitmentservice.entity.Skill;
import com.recruitment.recruitmentservice.exception.BusinessException;
import com.recruitment.recruitmentservice.exception.ErrorCode;
import com.recruitment.recruitmentservice.mapper.SkillMapper;
import com.recruitment.recruitmentservice.repository.SkillRepository;
import com.recruitment.recruitmentservice.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    private final SkillMapper skillMapper;

    @Override
    public SkillResponse create(CreateSkillRequest request) {

        validateSkillName(request.getName());
        validateSkillSlug(request.getSlug());

        Skill entity = skillMapper.toEntity(request);

        Skill savedSkill = skillRepository.save(entity);

        return skillMapper.toResponse(savedSkill);
    }

    @Override
    public SkillResponse update(
            UUID id,
            UpdateSkillRequest request
    ) {

        Skill skill = getSkillById(id);

        validateSkillNameForUpdate(
                id,
                request.getName()
        );

        skillMapper.updateEntity(
                request,
                skill
        );

        Skill updatedSkill =
                skillRepository.save(skill);

        return skillMapper.toResponse(updatedSkill);
    }
    @Override
    public void delete(UUID id) {

        Skill skill = getSkillById(id);

        skill.setActive(false);

        skillRepository.save(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponse getById(UUID id) {

        return skillMapper.toResponse(
                getSkillById(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SkillResponse> getAll(
            Pageable pageable
    ) {

        return PageResponse.from(
                skillRepository.findByActiveTrue(pageable),
                skillMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SkillResponse> search(
            String keyword,
            Pageable pageable
    ) {

        if (keyword == null || keyword.isBlank()) {
            return getAll(pageable);
        }

        return PageResponse.from(
                skillRepository.findByActiveTrueAndNameContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                ),
                skillMapper::toResponse
        );
    }
    private Skill getSkillById(UUID id) {

        return skillRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SKILL_NOT_FOUND
                        ));
    }

    private void validateSkillName(String name) {

        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException(
                    ErrorCode.SKILL_NAME_ALREADY_EXISTS
            );
        }
    }

    private void validateSkillSlug(String slug) {

        if (skillRepository.existsBySlug(slug)) {
            throw new BusinessException(
                    ErrorCode.SKILL_SLUG_ALREADY_EXISTS
            );
        }
    }

    private void validateSkillNameForUpdate(
            UUID id,
            String name
    ) {

        if (skillRepository.existsByNameIgnoreCaseAndIdNot(
                name,
                id
        )) {

            throw new BusinessException(
                    ErrorCode.SKILL_NAME_ALREADY_EXISTS
            );

        }

    }

}
