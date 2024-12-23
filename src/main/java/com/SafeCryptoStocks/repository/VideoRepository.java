package com.SafeCryptoStocks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SafeCryptoStocks.model.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}
