-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS myinfradb;

-- 데이터베이스 선택
USE myinfradb;

-- 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS 'newuser'@'%' IDENTIFIED BY 'qwer1234';
GRANT ALL PRIVILEGES ON *.* TO 'newuser'@'%';
FLUSH PRIVILEGES;

-- 테이블 생성 및 데이터 삽입
CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price INT NOT NULL
);

INSERT INTO item (title, price) VALUES
('Shirt', 4000),
('Pants', 5000),
('Shoes', 6000);