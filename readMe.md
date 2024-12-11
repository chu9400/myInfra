# MyInfra 프로젝트

- 이 프로젝트는 Docker 기반의 컨테이너 환경에서 네트워크 설계와 서버 간 통신을 학습하기 위해 설계되었습니다.

- 실제 환경을 가상화하여 서버 통신 흐름을 시뮬레이션하며, 인프라 설계 및 운영에 대한 실무적인 경험을 쌓는 것을 목표로 합니다.

<hr />



## 프로젝트 설계도
  ![myinfra.jpg](readme_img%2Fmyinfra.jpg)

- 외부 요청이 Host PC의 80번 포트로 전달됩니다.

- Nginx 컨테이너는 이를 app_network를 통해 Spring Boot 웹 서버(8080번 포트)로 프록시합니다.

- Spring Boot 서버는 db_network를 통해 MySQL 데이터베이스(3306번 포트)와 통신합니다.

- MySQL은 요청 데이터를 처리한 뒤 결과를 Spring Boot로 반환합니다.

- Spring Boot는 최종 응답을 Nginx로 전달하며, Nginx는 이를 클라이언트에게 반환합니다.


<hr />


## 프로젝트 주요 목표
### 1. 컨테이너 네트워크 통신 구현
  - Docker 네트워크를 활용하여 서버 간 안정적이고 독립적인 통신 구조를 설계합니다.

### 2. 리버스 프록시 구현
- Nginx를 사용해 클라이언트 요청을 백엔드 서버로 라우팅하며, 리버스 프록시의 원리와 설정 방법을 학습합니다.

### 3. 데이터 흐름 이해 및 최적화
- 클라이언트 요청에서 데이터베이스 응답까지의 전 과정을 시뮬레이션하며 네트워크 통신과 데이터 처리 흐름을 분석합니다.


<hr />


## 사용 기술 스택
- Docker, Docker Compose
- Java Spring Boot (JPA, Thymeleaf)
- MySQL 
- Nginx


<hr />


## 프로젝트 실행 방법
### 1. 환경 요구 사항
- Docker Desktop 설치


### 2. 프로젝트 실행
- GitHub에서 프로젝트를 다운로드하고 압축을 풉니다.
- 터미널에서 프로젝트 폴더로 이동합니다.
- 아래 명령어로 필요한 Docker 이미지를 빌드합니다.
  - mysql
    - ```shell
      docker build -t custom-mysql:v1 ./mysql
      ```

  - Nginx
    - ```shell
      docker build -t img_nginx:v1 ./nginx
      ```
- Docker Compose로 컨테이너 실행:
  - ```docker compose up --build```


-  브라우저에서 http://localhost:80으로 접속하여 애플리케이션이 정상적으로 작동하는지 확인합니다.
![homepage.png](readme_img%2Fhomepage.png)


<hr />

## 프로젝트 구조 및 설명
### 1. 리버스 프록시 서버
- Nginx를 사용하여 클라이언트 요청을 백엔드 웹 서버로 라우팅합니다. 
- Nginx의 기본 설정(default.conf)을 제거하고 myconfig.conf로 새롭게 구성하여 80번 포트 요청을 웹 서버로 전달하도록 설정했습니다.


### 2. 웹 서버
- Spring Boot와 JPA를 사용하여 구현되었으며, 각 기능을 서비스 레이어로 분리하여 유지보수성을 높였습니다. 
- 가상의 쇼핑몰 환경을 시뮬레이션하며 CRUD 기능을 통해 데이터베이스와의 통신을 검증할 수 있습니다.

### 3. 데이터베이스 서버
- MySQL로 구현되었으며, 초기 데이터베이스 설정을 자동화하기 위해 init.sql 파일을 작성했습니다.
- 보안 강화를 위해 Docker 환경에서 root 계정 대신 별도의 사용자 계정을 생성하여 사용합니다.


### 4. 네트워크 설계
- 컨테이너 네트워크를 활용하여 특정 서버 간 통신만 허용하도록 구성했습니다.
- Docker Compose에서 네트워크를 정의하여 보안성을 높였습니다.
  ```
  networks:
    app_network: # Nginx와 Spring Boot 간 통신 네트워크
    db_network: # Spring Boot와 MySQL 간 통신 네트워크
  ```

<hr />

## 오류 노트 및 최적화 기록
### MySQL 계정 권한 오류
- root 계정으로 접속 시 권한 문제로 인해 데이터베이스와의 통신이 실패하는 경우가 있었습니다.
- MySQL은 root 계정을 기본적으로 외부 접속에 제한하기 때문에, 새 계정을 생성하여 문제를 해결했습니다.
  ```sql
  # CREATE USER 'newuser'@'%' IDENTIFIED BY 'password';
  # GRANT ALL PRIVILEGES ON *.* TO 'newuser'@'%' WITH GRANT OPTION;
  # FLUSH PRIVILEGES;
  ```

<hr />

### Docker 내 DB URL 문제
- Docker 컨테이너 환경에서는 데이터베이스 URL에 컨테이너 이름을 사용해야 함을 학습했습니다.
  ```properties
  spring.datasource.url=jdbc:mysql://mysql-container:3306/myinfradb
  ```

<hr />  

### Graceful Shutdown 적용
- Spring Boot
    - 컨테이너 종료 시 지연을 최소화하고 종료 전에 필요한 작업을 안전하게 처리하기 위해, SIGTERM 및 SIGINT 신호를 처리하는 코드를 구현했습니다.
    - 아래는 종료 시 실행되는 @PreDestroy 메서드의 예제입니다.
       ```java
       @PreDestroy
       public void onShutdown() {
            System.out.println("애플리케이션 종료 중...");
            // 추가적인 정리 작업 수행
       }
      ```

- Nginx 서버
    - 종료 지연 문제를 해결하기 위해 stop_grace_period 설정을 추가했습니다.
      ```yaml
         nginx-container:
            stop_grace_period: 1s 
      ```

<hr />

### 파일 시스템 권한 오류 (Windows → Docker)

#### 문제 상황 
- NTFS 파일 시스템으로 인해 Docker가 프로젝트 폴더에 접근하지 못하는 문제가 발생했습니다.
  ```shell
  ERROR [spring-boot-container internal] load build context
  failed to solve: archive/tar: unknown file mode ?rwxr-xr-x
  ```

#### 해결 방법
- Windows Subsystem for Linux 2(WSL2)를 설치하여 Docker 명령어를 WSL2 환경에서 실행함으로써 해결했습니다. 
- WSL2는 리눅스 환경으로 Docker를 실행하므로, 리눅스 기반의 Docker 작업이 원활히 이루어졌습니다.
- 아래 명령어로 폴더에 권한을 부여할 수도 있습니다.
  ```shell
    chmod -R 755 ./project-folder
  ```

<hr />

## 추후 개선 사항
- 보안 강화
  - Nginx에 SSL 인증서를 추가하여 HTTPS 통신을 지원.
- 확장성
  - 다수의 웹 서버 인스턴스를 생성하고 리버스 프록시를 통해 로드 밸런싱 구현. 