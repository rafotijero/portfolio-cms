package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.AboutParagraphDto;
import dev.rafotijero.cms.api.dto.AboutParagraphRequest;
import dev.rafotijero.cms.domain.AboutParagraph;
import dev.rafotijero.cms.repository.AboutParagraphRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AboutService {

    private final AboutParagraphRepository aboutParagraphRepository;

    public AboutService(AboutParagraphRepository aboutParagraphRepository) {
        this.aboutParagraphRepository = aboutParagraphRepository;
    }

    public List<String> findAllParagraphs() {
        return aboutParagraphRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AboutParagraph::getContent)
                .toList();
    }

    @Transactional
    public AboutParagraphDto create(AboutParagraphRequest request) {
        AboutParagraph paragraph = new AboutParagraph();
        apply(paragraph, request);
        return AboutParagraphDto.from(aboutParagraphRepository.saveAndFlush(paragraph));
    }

    @Transactional
    public AboutParagraphDto update(UUID id, AboutParagraphRequest request) {
        AboutParagraph paragraph = getOrThrow(id);
        apply(paragraph, request);
        return AboutParagraphDto.from(aboutParagraphRepository.saveAndFlush(paragraph));
    }

    @Transactional
    public void delete(UUID id) {
        AboutParagraph paragraph = getOrThrow(id);
        aboutParagraphRepository.delete(paragraph);
    }

    private AboutParagraph getOrThrow(UUID id) {
        return aboutParagraphRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(AboutParagraph paragraph, AboutParagraphRequest request) {
        paragraph.setContent(request.content());
        paragraph.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
    }
}
