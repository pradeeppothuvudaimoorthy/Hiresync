-- HireSync Database Seeder Script
-- Populates the application with test users, profiles, and job listings on startup

-- 1. Seed Users (All passwords are set to 'password')
-- BCrypt hash of 'password': $2a$10$Ev7O1N2O09G26f2v4P7UDe1r5aR/t3W7k8p4t9z2N4q0M6m5o6v3K
INSERT INTO users (name, email, password, role, created_at) VALUES 
('System Administrator', 'admin@hiresync.com', '$2a$10$Ev7O1N2O09G26f2v4P7UDe1r5aR/t3W7k8p4t9z2N4q0M6m5o6v3K', 'ADMIN', CURRENT_TIMESTAMP),
('Jane Smith', 'recruiter@hiresync.com', '$2a$10$Ev7O1N2O09G26f2v4P7UDe1r5aR/t3W7k8p4t9z2N4q0M6m5o6v3K', 'RECRUITER', CURRENT_TIMESTAMP),
('John Doe', 'candidate@hiresync.com', '$2a$10$Ev7O1N2O09G26f2v4P7UDe1r5aR/t3W7k8p4t9z2N4q0M6m5o6v3K', 'CANDIDATE', CURRENT_TIMESTAMP);

-- 2. Seed Recruiter Profile (linked to user_id = 2, representing Jane Smith)
INSERT INTO recruiters (user_id, company_name, company_website, company_location, phone, status) VALUES 
(2, 'TechCorp Solutions', 'https://techcorp.com', 'Bangalore, India', '9876543210', 'APPROVED');

-- 3. Seed Candidate Profile (linked to user_id = 3, representing John Doe)
INSERT INTO candidates (user_id, phone, education, skills, experience, location, resume_path) VALUES 
(3, '1234567890', 'Bachelor of Technology', 'Java, Spring Boot, MySQL, Hibernate, Git', '2 Years', 'Bangalore', NULL);

-- 4. Seed 8 Active Job Openings (linked to recruiter_id = 1, representing TechCorp Solutions)
INSERT INTO jobs (
    recruiter_id, title, company_name, company_logo, description, required_skills, 
    experience_level, experience_required, education_required, employment_type, workplace_type, 
    location, city, state, country, industry, salary_min, salary_max, salary_range, 
    openings, applicant_count, easy_apply, last_date_to_apply, posted_date, status, 
    created_at, updated_at
) VALUES 
(
    1, 'Java Developer', 'TechCorp Solutions', NULL, 
    'We are looking for a Java Developer to join our backend team. You will be responsible for building robust backend services using Java 17 and Spring Boot. Good knowledge of REST APIs and JPA is required.', 
    'Java, Spring Boot, MySQL', 'ENTRY_LEVEL', '1 Year', 'Bachelor''s Degree', 'FULL_TIME', 'ONSITE', 
    'Bangalore', 'Bangalore', 'Karnataka', 'India', 'Technology', 600000.0, 900000.0, '₹6,00,000 - ₹9,00,000', 
    3, 0, TRUE, '2026-12-31', '2026-06-15', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    1, 'Senior Spring Boot Engineer', 'TechCorp Solutions', NULL, 
    'Lead the development of our enterprise cloud services. Design scalable microservices with Spring Boot, Spring Cloud, Hibernate, and MySQL. Guide junior developers and enforce best coding practices.', 
    'Java, Spring Boot, MySQL, Hibernate', 'SENIOR_LEVEL', '5 Years', 'Bachelor''s Degree', 'FULL_TIME', 'HYBRID', 
    'Bangalore', 'Bangalore', 'Karnataka', 'India', 'Technology', 1500000.0, 2200000.0, '₹15,00,000 - ₹22,00,000', 
    2, 0, TRUE, '2026-12-31', '2026-06-16', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    1, 'Frontend Developer (React)', 'TechCorp Solutions', NULL, 
    'Develop rich interactive user interfaces using React.js, Tailwind CSS, and JavaScript. Collaborate with backend developers to integrate APIs.', 
    'React, JavaScript, HTML, CSS', 'MID_LEVEL', '3 Years', 'Bachelor''s Degree', 'FULL_TIME', 'REMOTE', 
    'Remote', 'Remote', 'N/A', 'India', 'Technology', 800000.0, 1200000.0, '₹8,00,000 - ₹12,00,000', 
    1, 0, FALSE, '2026-12-31', '2026-06-17', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    1, 'UI/UX Designer', 'TechCorp Solutions', NULL, 
    'Design user flows, wireframes, prototypes, and high-fidelity mockups for our web and mobile applications using Figma. Conduct user research and usability testing.', 
    'Figma, UI Design, Wireframing', 'MID_LEVEL', '2 Years', 'Any', 'FULL_TIME', 'HYBRID', 
    'Delhi', 'Delhi', 'Delhi', 'India', 'Design', 500000.0, 800000.0, '₹5,00,000 - ₹8,00,000', 
    2, 0, TRUE, '2026-12-31', '2026-06-18', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    1, 'Full Stack Java Developer', 'TechCorp Solutions', NULL, 
    'Join our team to work on both backend (Java, Spring Boot) and frontend (JavaScript, HTML, CSS). Design and implement new features end-to-end.', 
    'Java, Spring Boot, JavaScript, HTML, CSS, MySQL', 'MID_LEVEL', '3 Years', 'Bachelor''s Degree', 'FULL_TIME', 'ONSITE', 
    'Bangalore', 'Bangalore', 'Karnataka', 'India', 'Technology', 900000.0, 1400000.0, '₹9,00,000 - ₹14,00,000', 
    4, 0, TRUE, '2026-12-31', '2026-06-18', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    1, 'Python Backend Engineer', 'TechCorp Solutions', NULL, 
    'Develop backend services using Python, Django, Fast API, and PostgreSQL. Design and optimize database schemas and queries.', 
    'Python, Django, PostgreSQL', 'ENTRY_LEVEL', '1 Year', 'Bachelor''s Degree', 'CONTRACT', 'REMOTE', 
    'Remote', 'Remote', 'N/A', 'India', 'Technology', 700000.0, 1000000.0, '₹7,00,000 - ₹10,00,000', 
    1, 0, TRUE, '2026-12-31', '2026-06-14', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    1, 'DevOps Engineer', 'TechCorp Solutions', NULL, 
    'Manage CI/CD pipelines, cloud infrastructure on AWS, and Docker/Kubernetes container orchestration. Automate deployments and ensure high availability.', 
    'AWS, Docker, Jenkins, Kubernetes', 'MID_LEVEL', '3 Years', 'Bachelor''s Degree', 'FULL_TIME', 'HYBRID', 
    'Mumbai', 'Mumbai', 'Maharashtra', 'India', 'Infrastructure', 1200000.0, 1800000.0, '₹12,00,000 - ₹18,00,000', 
    2, 0, FALSE, '2026-12-31', '2026-06-10', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    1, 'Database Administrator', 'TechCorp Solutions', NULL, 
    'Ensure MySQL and PostgreSQL databases are optimized, secure, and backed up. Troubleshoot performance issues, write stored procedures, and manage database access.', 
    'MySQL, PostgreSQL, Database Tuning', 'MID_LEVEL', '4 Years', 'Bachelor''s Degree', 'FULL_TIME', 'ONSITE', 
    'Bangalore', 'Bangalore', 'Karnataka', 'India', 'Technology', 1000000.0, 1500000.0, '₹10,00,000 - ₹15,00,000', 
    1, 0, TRUE, '2026-12-31', '2026-06-11', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
