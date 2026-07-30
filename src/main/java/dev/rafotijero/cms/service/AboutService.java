package dev.rafotijero.cms.service;

import dev.rafotijero.cms.domain.AboutParagraph;
import dev.rafotijero.cms.repository.AboutParagraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
