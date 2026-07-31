package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.TagDto;
import dev.rafotijero.cms.api.dto.TagRequest;
import dev.rafotijero.cms.domain.Tag;
import dev.rafotijero.cms.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagDto> findAll() {
        return tagRepository.findAll().stream().map(TagDto::from).toList();
    }

    public TagDto findById(UUID id) {
        return TagDto.from(getOrThrow(id));
    }

    @Transactional
    public TagDto create(TagRequest request) {
        Tag tag = new Tag();
        apply(tag, request);
        return TagDto.from(tagRepository.save(tag));
    }

    @Transactional
    public TagDto update(UUID id, TagRequest request) {
        Tag tag = getOrThrow(id);
        apply(tag, request);
        return TagDto.from(tagRepository.save(tag));
    }

    @Transactional
    public void delete(UUID id) {
        Tag tag = getOrThrow(id);
        tagRepository.delete(tag);
    }

    private Tag getOrThrow(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(Tag tag, TagRequest request) {
        tag.setName(request.name());
        tag.setSlug(request.slug());
    }
}
