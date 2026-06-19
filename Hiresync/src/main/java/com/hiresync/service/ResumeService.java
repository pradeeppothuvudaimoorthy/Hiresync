package com.hiresync.service;

import com.hiresync.entity.Resume;
import com.hiresync.model.Candidate;
import com.hiresync.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResumeService {

    @Value("${app.upload.dir:uploads/resumes}")
    private String uploadDir;

    @Autowired
    private ResumeRepository resumeRepository;

    public Resume uploadResume(MultipartFile file, Candidate candidate) throws IOException {
        // 1. Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file. Please select a resume file.");
        }

        // 2. Validate file extension (Allow only .pdf, .doc, .docx)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String extension = "";
        int lastIndex = originalFilename.lastIndexOf('.');
        if (lastIndex >= 0) {
            extension = originalFilename.substring(lastIndex).toLowerCase();
        }

        if (!extension.equals(".pdf") && !extension.equals(".doc") && !extension.equals(".docx")) {
            throw new IllegalArgumentException("Only PDF, DOC, and DOCX files are allowed.");
        }

        // 3. Validate maximum file size is 5 MB (5 * 1024 * 1024 bytes)
        long maxSizeBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds limit of 5MB.");
        }

        // 4. Create folder uploads/resumes if not present
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 5. Generate unique file name using UUID
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("\\s+", "_");
        Path targetLocation = uploadPath.resolve(uniqueFilename);

        // 6. Save file to folder
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 7. Deactivate previous active resumes for the candidate
        List<Resume> candidateResumes = resumeRepository.findByCandidateId(candidate.getId());
        for (Resume r : candidateResumes) {
            if (r.isActive()) {
                r.setActive(false);
                resumeRepository.save(r);
            }
        }

        // 8. Store resume details in database
        Resume resume = new Resume(candidate, originalFilename, uniqueFilename, file.getContentType(), file.getSize(), targetLocation.toString());
        return resumeRepository.save(resume);
    }

    public Path loadResume(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }
}
