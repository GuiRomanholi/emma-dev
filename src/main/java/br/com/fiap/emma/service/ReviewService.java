package br.com.fiap.emma.service;

import br.com.fiap.emma.dto.ReviewRequest;
import br.com.fiap.emma.model.Reading;
import br.com.fiap.emma.model.Review;
import br.com.fiap.emma.repository.ReadingRepository;
import br.com.fiap.emma.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReadingRepository readingRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ReadingRepository readingRepository) {
        this.reviewRepository = reviewRepository;
        this.readingRepository = readingRepository;
    }

    @Cacheable(value = "reviews")
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    @Cacheable(value = "review", key = "#id")
    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review não encontrada: " + id));
    }

    @CachePut(value = "review", key = "#result.id")
    @CacheEvict(value = "reviews", allEntries = true)
    public Review create(Long readingId, Review review) {
        Reading reading = readingRepository.findById(readingId)
                .orElseThrow(() -> new RuntimeException("Reading não encontrada: " + readingId));

        review.setReading(reading);
        return reviewRepository.save(review);
    }

    @CachePut(value = "review", key = "#id")
    @CacheEvict(value = "reviews", allEntries = true)
    public Review update(Long id, ReviewRequest request) {
        Review existing = findById(id);

        existing.setDescription(request.getDescription());

        if (request.getReadingId() != null) {
            Reading reading = readingRepository.findById(request.getReadingId())
                    .orElseThrow(() -> new RuntimeException("Reading não encontrada: " + request.getReadingId()));
            existing.setReading(reading);
        } else {
            existing.setReading(null);
        }

        return reviewRepository.save(existing);
    }

    @CacheEvict(value = {"review", "reviews"}, key = "#id", allEntries = true)
    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }

    public Page<Review> findAllPageable(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

}
