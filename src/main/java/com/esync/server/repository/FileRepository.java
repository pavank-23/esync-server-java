package com.esync.server.repository;

import java.util.Optional;

import com.esync.server.models.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByName(String name);
    Boolean existsByName(String name);
}