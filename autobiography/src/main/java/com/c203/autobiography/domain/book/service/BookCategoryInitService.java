package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.book.repository.BookCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookCategoryInitService {
    private final BookCategoryRepository repo;

    public void seed() {
        List<String> names = List.of(
                "자서전","일기","소설/시","에세이","자기계발","역사",
                "경제/경영","사회/정치","청소년","어린이/동화",
                "문화/예술","종교","여행","스포츠"
        );

        for (String name : names) {
            if (!repo.existsByCategoryName(name)) {
                repo.save(BookCategory.of(name));
            }
        }
    }
}
